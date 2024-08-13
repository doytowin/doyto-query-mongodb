/*
 * Copyright © 2019-2024 Forb Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package win.doyto.query.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.client.ClientActions;
import de.flapdoodle.embed.mongo.client.SyncClientAdapter;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.commands.ServerAddress;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.Listener;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.bson.BsonArray;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import win.doyto.query.web.WebMvcConfigurerAdapter;

import static win.doyto.query.mongodb.test.TestUtil.readString;

/**
 * MongoApplication
 *
 * @author f0rb on 2022-03-17
 */
@SpringBootApplication
public class MongoApplication extends WebMvcConfigurerAdapter {
    public static void main(String[] args) {
        SpringApplication.run(MongoApplication.class);
    }

    @Bean
    public TransitionWalker.ReachedState<RunningMongodProcess> runningMongodProcessReachedState() {
        Version version = Version.V7_0_12;
        Storage storage = Storage.of("testRepSet", 5000);
        Listener withRunningMongod = ClientActions.initReplicaSet(new SyncClientAdapter(), version, storage);

        Mongod mongod = new Mongod() {
            @Override
            public Transition<MongodArguments> mongodArguments() {
                MongodArguments arguments = MongodArguments.defaults().withIsConfigServer(true).withReplication(storage);
                return Start.to(MongodArguments.class).initializedWith(arguments);
            }
        };

        return mongod.start(version, withRunningMongod);
    }

    @Bean
    public MongoClient mongoClient() {
        TransitionWalker.ReachedState<RunningMongodProcess> runningMongod = runningMongodProcessReachedState();
        ServerAddress serverAddress = runningMongod.current().getServerAddress();
        MongoClient mongoClient = MongoClients.create("mongodb://" + serverAddress);
        initData(mongoClient);
        return mongoClient;
    }

    private void initData(MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase("doyto");
        String text = readString("/init_data.json");
        BsonArray bsonValues = BsonArray.parse(text);
        bsonValues.forEach(bsonValue -> database.runCommand(bsonValue.asDocument()));
    }
}
