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

package win.doyto.query.entity;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.validation.CreateGroup;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;

import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * AbstractId
 *
 * @author f0rb on 2021-06-27
 */
@Getter
@Setter
public abstract class AbstractPersistable<I extends Serializable> implements Persistable<I>, Serializable {
    private static final long serialVersionUID = -4538555675455803732L;
    @Id
    @GeneratedValue
    @Null(groups = CreateGroup.class)
    @NotNull(groups = {UpdateGroup.class, PatchGroup.class})
    protected I id;

    public void setId(I id) {
        this.id = id;
    }
}
