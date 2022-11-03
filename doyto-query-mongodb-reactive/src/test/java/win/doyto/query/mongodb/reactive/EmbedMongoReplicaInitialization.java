/*
 * Copyright © 2019-2022 Forb Yuan
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

package win.doyto.query.mongodb.reactive;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.Arrays;


/**
 * EmbedMongoConfig
 *
 * @author f0rb on 2022/11/3
 * @since 1.0.0
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class EmbedMongoReplicaInitialization implements InitializingBean {

    MongodExecutable embeddedMongoServer;
    MongodConfig mongodConfig;

    public void afterPropertiesSet() {
        String host = mongodConfig.net().getBindIp() + ":" + mongodConfig.net().getPort();
        String uri = String.format("mongodb://%s/admin", host);
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase admin = mongoClient.getDatabase("admin");
            initReplica(host, admin);
            waitUntilReplicaReady(admin);
        }
    }

    private void initReplica(String host, MongoDatabase admin) {
        Document initDoc = new Document();
        initDoc.append("_id", mongodConfig.replication().getReplSetName());
        Document member0 = new Document();
        member0.append("_id", 0);
        member0.append("host", host);
        initDoc.append("members", Arrays.asList(member0));

        Document replSetInitiate = new Document("replSetInitiate", initDoc);
        Flux.from(admin.runCommand(replSetInitiate)).blockFirst();
    }

    private void waitUntilReplicaReady(MongoDatabase admin) {
        Document document;
        do {
            document = getReplicaStatus(admin);
        } while (isReplicaInitializing(document));
    }

    private boolean isReplicaInitializing(Document document) {
        Document electionCandidateMetrics = document.get("electionCandidateMetrics", Document.class);
        return electionCandidateMetrics == null
                || !electionCandidateMetrics.containsKey("wMajorityWriteAvailabilityDate");
    }

    private Document getReplicaStatus(MongoDatabase admin) {
        Document replSetGetStatus = new Document("replSetGetStatus", 1);
        return Flux.from(admin.runCommand(replSetGetStatus)).blockFirst();
    }

}
