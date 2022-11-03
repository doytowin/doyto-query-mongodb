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

import com.fasterxml.jackson.core.type.TypeReference;
import com.mongodb.reactivestreams.client.MongoClient;
import org.bson.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import win.doyto.query.mongodb.test.inventory.InventoryEntity;
import win.doyto.query.mongodb.test.inventory.InventoryQuery;
import win.doyto.query.mongodb.test.inventory.SizeQuery;
import win.doyto.query.util.BeanUtil;

import java.io.IOException;
import java.util.List;

/**
 * ReactiveMongoDataAccessTest
 *
 * @author f0rb on 2021-12-26
 */
@ActiveProfiles("test")
@SpringBootTest
class ReactiveMongoDataAccessTest {
    private static boolean initialized;
    private static ReactiveMongoDataAccess<InventoryEntity, String, InventoryQuery> inventoryDataAccess;

    @BeforeAll
    static synchronized void beforeAll(@Autowired MongoClient mongoClient) throws IOException {
        if (initialized) return;

        inventoryDataAccess = new ReactiveMongoDataAccess<>(mongoClient, InventoryEntity.class);
        List<? extends Document> data = BeanUtil.loadJsonData(
                "../test/inventory/inventory.json", new TypeReference<List<? extends Document>>() {});
        StepVerifier.create(inventoryDataAccess.getCollection().insertMany(data)).expectNextCount(1).verifyComplete();
        initialized = true;
    }

    @Test
    void count_by_size$h_lt_10_and_status_eq_A() {
        SizeQuery sizeQuery = SizeQuery.builder().hLt(10).build();
        InventoryQuery query = InventoryQuery.builder().size(sizeQuery).status("A").build();
        inventoryDataAccess.count(query)
                           .as(StepVerifier::create)
                           .expectNext(1L)
                           .verifyComplete();
    }

    @Test
    void query() {
        InventoryQuery query = InventoryQuery.builder().build();
        inventoryDataAccess.query(query)
            .as(StepVerifier::create)
            .expectNextMatches(inventoryEntity ->
                inventoryEntity.getItem().equals("journal")
                    && inventoryEntity.getQty().equals(25)
                    && inventoryEntity.getSize().getH().equals(14.0)
            )
            .expectNextCount(4L)
            .verifyComplete();
    }

    @Test
    void get() {
        InventoryQuery query = InventoryQuery.builder().build();
        String id = inventoryDataAccess.query(query).blockFirst().getId();
        inventoryDataAccess.get(id)
            .as(StepVerifier::create)
            .expectNextMatches(inventoryEntity ->
                inventoryEntity.getItem().equals("journal")
                    && inventoryEntity.getQty().equals(25)
                    && inventoryEntity.getSize().getH().equals(14.0)
            )
            .verifyComplete();
    }

}
