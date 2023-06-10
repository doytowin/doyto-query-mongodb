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

package win.doyto.query.mongodb;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * InventoryMvcTest
 *
 * @author f0rb on 2022/12/6
 * @since 1.0.0
 */
class InventoryMvcTest extends MongoApplicationTest {
    @Resource
    protected MockMvc mockMvc;

    @Test
    void queryExamples() throws Exception {
        mockMvc.perform(get("/inventory/?status=A"))
                .andExpect(jsonPath("$.data.total").value(3));

        mockMvc.perform(get("/inventory/?size.hLt=12&status=A"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list[*].item",
                                    containsInRelativeOrder("notebook", "postcard")));

        mockMvc.perform(get("/inventory/?size.uom=in"))
               .andExpect(jsonPath("$.data.total").value(2))
               .andExpect(jsonPath("$.data.list[*].item",
                                   containsInRelativeOrder("notebook", "paper")));
    }
}
