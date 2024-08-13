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

package win.doyto.query.mongodb.transaction.spring;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
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
import lombok.SneakyThrows;
import org.bson.BsonArray;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StreamUtils;
import win.doyto.query.mongodb.session.MongoSessionSupplier;
import win.doyto.query.mongodb.session.MongoSessionThreadLocalSupplier;

import java.nio.charset.Charset;

/**
 * MongoTransactionApplication
 *
 * @author f0rb on 2022-06-23
 */
@EnableRetry
@SpringBootApplication
@EnableTransactionManagement(proxyTargetClass = true)
public class MongoTransactionApplication {
    public static void main(String[] args) {
        SpringApplication.run(MongoTransactionApplication.class);
    }


    @Bean
    public MongoSessionSupplier mongoSessionSupplier(MongoClient mongoClient) {
        return MongoSessionThreadLocalSupplier.create(mongoClient);
    }

    @Bean
    public PlatformTransactionManager mongoTransactionManager(
            MongoClient mongoClient, MongoSessionSupplier mongoSessionSupplier) {
        TransactionOptions txOptions = TransactionOptions
                .builder()
                .readPreference(ReadPreference.primary())
                .readConcern(ReadConcern.LOCAL)
                .writeConcern(WriteConcern.MAJORITY)
                .build();
        return new MongoTransactionManager(mongoClient, txOptions) {
            @Override
            protected ClientSession getClientSession() {
                return mongoSessionSupplier.get(true);
            }

            @Override
            protected void doCleanupAfterCompletion(Object transaction) {
                super.doCleanupAfterCompletion(transaction);
                mongoSessionSupplier.release();
            }
        };
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
        initData(mongoClient.getDatabase("doyto"));
        return mongoClient;
    }

    private static void initData(MongoDatabase database) {
        database.getCollection("a_role_and_perm").drop();
        String text = readString("/data.json");
        BsonArray bsonValues = BsonArray.parse(text);
        bsonValues.forEach(bsonValue -> database.runCommand(bsonValue.asDocument()));
    }

    @SneakyThrows
    public static String readString(String name) {
        return StreamUtils.copyToString(TransactionTest.class.getResourceAsStream(name), Charset.defaultCharset());
    }

}