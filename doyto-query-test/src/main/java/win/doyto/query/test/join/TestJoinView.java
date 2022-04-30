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

package win.doyto.query.test.join;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.annotation.Joins;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * TestJoinView
 *
 * @author f0rb on 2019-06-09
 */
@Getter
@Setter
@Table(name = "t_user u")
@Joins({
        "left join j_user_and_role ur on ur.user_id = u.id",
        "inner join t_role r on r.id = ur.role_id and r.role_name = #{roleName}"
})
public class TestJoinView {

    private String username;

    @Column(name = "r.role_name")
    private String roleName;
}
