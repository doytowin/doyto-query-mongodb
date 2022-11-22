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

package win.doyto.query.mongodb.aggregation;

import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import win.doyto.query.annotation.DomainPath;
import win.doyto.query.mongodb.test.TestUtil;
import win.doyto.query.mongodb.test.role.RoleQuery;
import win.doyto.query.mongodb.test.user.UserQuery;
import win.doyto.query.mongodb.test.user.UserView;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static win.doyto.query.mongodb.aggregation.DomainPathBuilder.buildLookUpForNestedQuery;
import static win.doyto.query.mongodb.test.TestUtil.readString;

/**
 * NestedQueryTest
 *
 * @author f0rb on 2022/11/21
 * @since 1.0.0
 */
class NestedQueryTest {

    @Test
    void supportNestedQueryForManyToManyWithReverseSign() throws NoSuchFieldException {
        Field field = RoleQuery.class.getDeclaredField("userQuery");

        Bson bson = buildLookUpForNestedQuery("user", field.getAnnotation(DomainPath.class));

        String actual = TestUtil.toJson(bson);
        String expected = readString("/query_roles_filter_by_user.json");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void supportNestedQueryForOneToMany() {
        UserQuery createUserQuery = UserQuery.builder().username("f0rb").build();
        UserQuery query = UserQuery.builder().createUser(createUserQuery).build();
        AggregationMetadata<Object> md = new AggregationMetadata<>(UserView.class, null);

        List<Bson> pipeline = md.buildAggregation(query);

        String result = TestUtil.toJson(pipeline);
        String expected = readString("/query_user_filter_by_create_user.json");
        assertThat(result).isEqualTo(expected);
    }

}
