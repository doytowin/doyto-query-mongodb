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

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.mongodb.session.MongoSessionSupplier;
import win.doyto.query.mongodb.test.inventory.InventoryEntity;

import javax.persistence.EntityType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MongoDataAccessFactoryTest
 *
 * @author f0rb on 2022/11/26
 * @since 1.0.0
 */
class MongoDataAccessFactoryTest {
    @Test
    void createDataAccessByMongoClient() {
        MongoDataAccessFactory factory = new MongoDataAccessFactory();
        BeanFactory mockBeanFactory = mock(BeanFactory.class);
        when(mockBeanFactory.getBean(MongoSessionSupplier.class))
                .thenThrow(new NoSuchBeanDefinitionException(MongoSessionSupplier.class));
        when(mockBeanFactory.getBean(MongoClient.class)).thenReturn(MongoClients.create());

        DataAccess<InventoryEntity, String, DoytoQuery> dataAccess =
                factory.createDataAccess(mockBeanFactory, InventoryEntity.class);

        assertThat(factory.getEntityType()).isEqualTo(EntityType.MONGO_DB);
        assertThat(dataAccess).isInstanceOf(MongoDataAccess.class);
    }
}