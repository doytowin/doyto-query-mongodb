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

package win.doyto.query.mongodb.entity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import java.io.IOException;

/**
 * BsonDeserializer
 * <p>
 * Used by {@link win.doyto.query.util.BeanUtil}
 *
 * @author f0rb on 2021-12-04
 */
@SuppressWarnings("unused")
public class BsonDeserializer extends JsonDeserializer<Bson> {

    @Override
    public Bson deserialize(JsonParser p, DeserializationContext context) throws IOException {
        TreeNode treeNode = p.readValueAsTree();
        return BsonDocument.parse(treeNode.toString());
    }
}