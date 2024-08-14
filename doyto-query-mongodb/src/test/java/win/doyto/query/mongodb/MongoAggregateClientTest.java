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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.beans.factory.annotation.Autowired;
import win.doyto.query.core.AggregateClient;
import win.doyto.query.mongodb.test.aggregate.QuantityAggrQuery;
import win.doyto.query.mongodb.test.aggregate.QuantityByStatusQuery;
import win.doyto.query.mongodb.test.aggregate.QuantityByStatusView;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MongoAggregateClientTest
 *
 * @author f0rb on 2024/8/14
 */
@ResourceLock(value = "inventory")
class MongoAggregateClientTest extends MongoApplicationTest {

    AggregateClient aggregateClient;

    MongoAggregateClientTest(@Autowired MongoClient mongoClient) {
        this.aggregateClient = new MongoAggregateClient(mongoClient);
    }

    @Test
    void groupByHavingCountGtWhereStatusNot() {
        loadData("test/inventory/inventory_2.json");

        QuantityByStatusQuery query = QuantityByStatusQuery.builder().statusNot("D").build();
        QuantityAggrQuery aggrQuery = QuantityAggrQuery.builder().countLt(3L).entityQuery(query).build();

        List<QuantityByStatusView> views = aggregateClient.aggregate(QuantityByStatusView.class, aggrQuery);
        assertThat(views).hasSize(1)
                         .first()
                         .extracting("sumQty", "status")
                         .containsExactly(144, "C")
        ;
    }


}