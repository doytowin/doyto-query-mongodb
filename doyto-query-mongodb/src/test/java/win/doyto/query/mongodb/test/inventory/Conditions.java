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

package win.doyto.query.mongodb.test.inventory;

import lombok.*;
import win.doyto.query.core.Query;

import java.util.List;

/**
 * Conditions
 *
 * @author f0rb on 2022-02-19
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conditions implements Query {
    private List<String> statusIn;
    private Integer qtyGt;
}