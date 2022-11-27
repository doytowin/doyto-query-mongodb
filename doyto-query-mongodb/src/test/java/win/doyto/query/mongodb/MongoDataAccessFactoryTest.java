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

package win.doyto.query.mongodb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.mongodb.test.inventory.InventoryEntity;

import javax.annotation.Resource;
import javax.persistence.EntityType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MongoDataAccessFactoryTest
 *
 * @author f0rb on 2022/11/26
 * @since 1.0.0
 */
class MongoDataAccessFactoryTest extends MongoApplicationTest {

    @Resource
    private BeanFactory beanFactory;

    @Test
    void createDataAccessByMongoClient() {
        MongoDataAccessFactory factory = new MongoDataAccessFactory();

        DataAccess<InventoryEntity, String, DoytoQuery> dataAccess =
                factory.createDataAccess(beanFactory, InventoryEntity.class);

        assertThat(factory.getEntityType()).isEqualTo(EntityType.MONGO_DB);
        assertThat(dataAccess).isInstanceOf(MongoDataAccess.class);
    }
}