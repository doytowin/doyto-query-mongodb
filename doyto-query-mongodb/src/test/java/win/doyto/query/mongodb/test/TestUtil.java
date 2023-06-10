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

package win.doyto.query.mongodb.test;

import lombok.SneakyThrows;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;
import org.springframework.util.StreamUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TestUtil
 *
 * @author f0rb on 2022-06-15
 * @since 1.0.0
 */
public class TestUtil {
    private static final JsonWriterSettings settings = JsonWriterSettings.builder().indent(true).build();

    @SneakyThrows
    public static String readString(String name) {
        return StreamUtils.copyToString(TestUtil.class.getResourceAsStream(name), Charset.defaultCharset());
    }

    public static String toJson(Bson bson) {
        return bson.toBsonDocument().toJson(settings);
    }

    public static String toJson(List<Bson> bsonList) {
        return bsonList.stream().map(TestUtil::toJson)
                       .collect(Collectors.joining(",\n", "[\n", "\n]"));
    }
}
