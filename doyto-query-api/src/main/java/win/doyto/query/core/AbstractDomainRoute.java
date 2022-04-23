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

package win.doyto.query.core;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import javax.persistence.Transient;

/**
 * AbstractDomainRoute
 *
 * @author f0rb on 2022-04-23
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AbstractDomainRoute implements DomainRoute {
    @Transient
    private List<String> path;
    @Transient
    private boolean reverse;
    @NonNull
    @Builder.Default
    @Transient
    private String lastDomainIdColumn = "id";
}