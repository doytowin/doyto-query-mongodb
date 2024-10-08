/*
 * Copyright © 2019-2023 Forb Yuan
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

package win.doyto.query.mongodb.session;

import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MongoSessionThreadLocalProvider
 *
 * @author f0rb on 2022-07-11
 */
public class MongoSessionThreadLocalSupplier implements MongoSessionSupplier {

    private static final Map<MongoClient, MongoSessionThreadLocalSupplier> MONGO_SESSION_SUPPLIER_MAP = new ConcurrentHashMap<>(4);

    private final MongoClient mongoClient;

    private final ThreadLocal<ClientSession> clientSessionThreadLocal = new ThreadLocal<>();

    private MongoSessionThreadLocalSupplier(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public static MongoSessionThreadLocalSupplier create(MongoClient mongoClient) {
        return MONGO_SESSION_SUPPLIER_MAP.computeIfAbsent(mongoClient, MongoSessionThreadLocalSupplier::new);
    }

    @Override
    public MongoClient getClient() {
        return mongoClient;
    }

    @Override
    public ClientSession get(boolean hold) {
        ClientSession clientSession = this.clientSessionThreadLocal.get();
        if (clientSession == null) {
            ClientSessionOptions options = ClientSessionOptions.builder().causallyConsistent(true).build();
            clientSession = mongoClient.startSession(options);
            if (hold) {
                this.clientSessionThreadLocal.set(clientSession);
            }
        }
        return clientSession;
    }

    @Override
    public void release() {
        this.clientSessionThreadLocal.remove();
    }
}
