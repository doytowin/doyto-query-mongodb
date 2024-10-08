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

package win.doyto.query.mongodb.filter;

import com.mongodb.client.model.geojson.codecs.GeoJsonCodecProvider;
import org.assertj.core.util.Lists;
import org.bson.Document;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import win.doyto.query.geo.GeoPolygon;
import win.doyto.query.geo.Point;
import win.doyto.query.mongodb.test.geo.GeoQuery;
import win.doyto.query.mongodb.test.inventory.InventoryQuery;
import win.doyto.query.test.TestQuery;
import win.doyto.query.util.BeanUtil;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MongoFilterUtilTest
 *
 * @author f0rb on 2021-11-24
 */
class MongoFilterBuilderTest {

    private final CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
            CodecRegistries.fromProviders(
                    new BsonValueCodecProvider(),
                    new DocumentCodecProvider(),
                    new GeoJsonCodecProvider()
            ));

    @ParameterizedTest
    @CsvSource({
            "{}, {}",
            "{\"username\": \"test\"}, {\"username\": \"test\"}",
            "{\"usernameContain\": \"admin\"}, '{\"username\": {\"$regex\": \"admin\"}}'",
            "{\"usernameNotContain\": \"admin\"}, '{\"username\": {\"$not\": {\"$regex\": \"admin\"}}}'",
            "{\"usernameStart\": \"admin\"}, '{\"username\": {\"$regex\": \"^admin\"}}'",
            "{\"usernameNotStart\": \"admin\"}, '{\"username\": {\"$not\": {\"$regex\": \"^admin\"}}}'",
            "{\"usernameEnd\": \"admin\"}, '{\"username\": {\"$regex\": \"admin$\"}}'",
            "{\"usernameNotEnd\": \"admin\"}, '{\"username\": {\"$not\": {\"$regex\": \"admin$\"}}}'",
            "{\"idLt\": 20}, {\"id\": {\"$lt\": 20}}",
            "{\"idLe\": 20}, {\"id\": {\"$lte\": 20}}",
            "{\"createTimeLt\": \"2021-11-24\"}, {\"createTime\": {\"$lt\": {\"$date\": \"2021-11-24T00:00:00Z\"}}}",
            "{\"createTimeGt\": \"2021-11-24\"}, {\"createTime\": {\"$gt\": {\"$date\": \"2021-11-24T00:00:00Z\"}}}",
            "{\"createTimeGe\": \"2021-11-24\"}, {\"createTime\": {\"$gte\": {\"$date\": \"2021-11-24T00:00:00Z\"}}}",
            "'{\"idIn\": [1,2,3]}', '{\"id\": {\"$in\": [1, 2, 3]}}'",
            "'{\"idNotIn\": [1,2,3]}', '{\"id\": {\"$nin\": [1, 2, 3]}}'",
            "{\"userLevel\": \"VIP\"}, {\"userLevel\": 0}",
            "{\"userLevelNot\": \"VIP\"}, {\"userLevel\": {\"$ne\": 0}}",
            "{\"memoNull\": true}, {\"memo\": null}",
            "{\"memoNull\": false}, {\"memo\": {\"$ne\": null}}",
            "{\"statusExists\": true}, {\"status\": {\"$exists\": true}}",
            "{\"usernameRx\": \"test\\\\d+\"}, {\"username\": {\"$regex\": \"test\\\\d+\"}}",
    })
    void testFilterSuffix(String data, String expected) {
        TestQuery query = BeanUtil.parse(data, TestQuery.class);
        Bson filters = MongoFilterBuilder.buildFilter(query);
        assertEquals(expected, filters.toBsonDocument().toJson());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "{\"size\":{\"hLt\":15}} | {\"size.h\": {\"$lt\": 15}}",
            "{\"size\":{\"hLt\":15,\"unit\":{\"name\":\"inch\"}}}" +
                    "| {\"$and\": [{\"size.h\": {\"$lt\": 15}}, {\"size.unit.name\": \"inch\"}]}",
    }, delimiter = '|')
    void testNestedFilter(String data, String expected) {
        InventoryQuery query = BeanUtil.parse(data, InventoryQuery.class);
        Bson filters = MongoFilterBuilder.buildFilter(query);
        assertEquals(expected, filters.toBsonDocument().toJson());
    }

    @SuppressWarnings("java:S4144")
    @ParameterizedTest
    @CsvSource(value = {
            "{\"conditionOr\":{\"statusIn\":[\"A\",\"D\"],\"qtyGt\":15}}" +
                    "| {\"$or\": [{\"status\": {\"$in\": [\"A\", \"D\"]}}, {\"qty\": {\"$gt\": 15}}]}",
            "{\"conditionOr\":{\"statusIn\":[\"A\",\"D\"],\"qtyGt\":15},\"itemContain\":\"test\"}" +
                    "| {\"$and\": [{\"item\": {\"$regex\": \"test\"}}, " +
                    "{\"$or\": [{\"status\": {\"$in\": [\"A\", \"D\"]}}, {\"qty\": {\"$gt\": 15}}]}]}",
            "{\"conditionOr\":{\"statusIn\":[\"A\",\"D\"]},\"itemContain\":\"test\"}" +
                    "| {\"$and\": [{\"item\": {\"$regex\": \"test\"}}, " +
                    "{\"status\": {\"$in\": [\"A\", \"D\"]}}]}",
            "{\"conditionOr\":{},\"itemContain\":\"test\"}" +
                    "| {\"item\": {\"$regex\": \"test\"}}",
            "{\"statusOr\":[\"A\", \"D\"],\"itemContain\":\"test\"}" +
                    "| {\"$and\": [{\"item\": {\"$regex\": \"test\"}}, {\"$or\": [{\"status\": \"A\"}, {\"status\": \"D\"}]}]}",
            "{\"conditionsOr\":[{\"statusIn\":[\"A\",\"D\"]},{\"statusIn\":[\"A\",\"C\"],\"qtyGt\":10}],\"status\":\"A\"}" +
                    "| {\"$and\": [{\"status\": \"A\"}, {\"$or\": [{\"status\": {\"$in\": [\"A\", \"D\"]}}, " +
                    "{\"$and\": [{\"status\": {\"$in\": [\"A\", \"C\"]}}, {\"qty\": {\"$gt\": 10}}]}]}]}",
    }, delimiter = '|')
    void testOrFilter(String data, String expected) {
        InventoryQuery query = BeanUtil.parse(data, InventoryQuery.class);
        Bson filters = MongoFilterBuilder.buildFilter(query);
        assertEquals(expected, filters.toBsonDocument().toJson());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "item,desc | {\"item\": -1}",
            "item,asc | {\"item\": 1}",
            "item | {\"item\": 1}",
            "item,desc;qty,asc | {\"item\": -1, \"qty\": 1}",
            "item;qty,asc | {\"item\": 1, \"qty\": 1}",
    }, delimiter = '|')
    void buildSort(String sort, String expected) {
        Bson orderBy = MongoFilterBuilder.buildSort(sort);
        assertEquals(expected, orderBy.toBsonDocument().toJson());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "{\"locNear\": {\"center\": {\"x\": 1.0, \"y\": 1.0}, \"maxDistance\": 5.0, \"minDistance\": 1.0}} " +
                    "| {\"loc\": {\"$near\": [1.0, 1.0], \"$maxDistance\": 5.0, \"$minDistance\": 1.0}}",
            "{\"locNearSphere\": {\"center\": {\"x\": 1.0, \"y\": 1.0}, \"maxDistance\": 5.0, \"minDistance\": 1.0}} " +
                    "| {\"loc\": {\"$nearSphere\": [1.0, 1.0], \"$maxDistance\": 5.0, \"$minDistance\": 1.0}}",
            "{\"loc2Near\": {\"center\": {\"x\": 1.0, \"y\": 1.0}, \"maxDistance\": 5.0, \"minDistance\": 1.0}} " +
                    "| {\"loc2\": {\"$nearSphere\": [1.0, 1.0], \"$maxDistance\": 5.0, \"$minDistance\": 1.0}}",
            "{\"locCenter\": {\"center\": {\"x\": 1.0, \"y\": 1.0}, \"radius\": 5.0}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$center\": [[1.0, 1.0], 5.0]}}}",
            "{\"locCenterSphere\": {\"center\": {\"x\": 1.0, \"y\": 1.0}, \"radius\": 5.0}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$centerSphere\": [[1.0, 1.0], 5.0]}}}",
            "{\"locBox\": {\"p1\": {\"x\": 1.0, \"y\": 2.0}, \"p2\": {\"x\": 2.0, \"y\": 1.0}}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$box\": [[1.0, 2.0], [2.0, 1.0]]}}}",
            "{\"locBsonBox\": {\"loc\": {\"$geoWithin\": {\"$box\": [[1.0, 2.0], [2.0, 1.0]]}}}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$box\": [[1.0, 2.0], [2.0, 1.0]]}}}",
            "{\"locPy\": [[1.0, 1.0], [1.0, 2.0], [2.0, 2.0], [2.0, 1.0]]}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$polygon\": [[1.0, 1.0], [1.0, 2.0], [2.0, 2.0], [2.0, 1.0]]}}}",
            "{\"locBsonWithin\":  {\"loc\": {\"$geoWithin\": {\"$geometry\":{\"$box\": [[1.0, 2.0], [2.0, 1.0]]}}}}}}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$geometry\": {\"$box\": [[1.0, 2.0], [2.0, 1.0]]}}}}",
            "{\"locBsonIntX\":  {\"loc\": {\"$geoIntersects\": {\"$geometry\":{\"type\": \"LineString\", \"coordinates\": [[1.0, 1.0], [2.0, 2.5]]}}}}}" +
                    "| {\"loc\": {\"$geoIntersects\": {\"$geometry\": {\"type\": \"LineString\", \"coordinates\": [[1.0, 1.0], [2.0, 2.5]]}}}}",

            "{\"locWithin\": {\"type\": \"Point\", \"coordinates\": [1.0, 2.5]}}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$geometry\": {\"type\": \"Point\", \"coordinates\": [1.0, 2.5]}}}}",
            "{\"locWithin\": {\"type\": \"Line\", \"coordinates\": [[1.0, 2.5], [3.2, 1.5]]}}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$geometry\": {\"type\": \"LineString\", \"coordinates\": [[1.0, 2.5], [3.2, 1.5]]}}}}",
            "{\"locWithin\": {\"type\": \"Polygon\", \"coordinates\": [[[0.0, 0.0], [3.0, 6.0], [6.0, 1.0]]]}}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[0.0, 0.0], [3.0, 6.0], [6.0, 1.0], [0.0, 0.0]]]}}}}",
            "{\"locWithin\": {\"type\": \"MultiPoint\", \"coordinates\": [[1.0, 2.5], [3.2, 1.5]]}}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$geometry\": {\"type\": \"MultiPoint\", \"coordinates\": [[1.0, 2.5], [3.2, 1.5]]}}}}",
            "{\"locWithin\": {\"type\": \"MultiLine\", \"coordinates\": [[[0.0, 0.0], [3.0, 6.0], [6.0, 1.0]], [[1.0, 2.0], [3.0, 3.0]]]}}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$geometry\": {\"type\": \"MultiLineString\", \"coordinates\": [[[0.0, 0.0], [3.0, 6.0], [6.0, 1.0]], [[1.0, 2.0], [3.0, 3.0]]]}}}}",
            "{\"locWithin\": {\"type\": \"MultiPolygon\", \"coordinates\": [[[[0.0, 0.0], [3.0, 6.0], [6.0, 1.0], [0.0, 0.0]], [[1.0, 2.0], [3.0, 3.0], [5.0, 2.0]]]]}}}" +
                    "| {\"loc\": {\"$geoWithin\": {\"$geometry\": {\"type\": \"MultiPolygon\", \"coordinates\": [[[[0.0, 0.0], [3.0, 6.0], [6.0, 1.0], [0.0, 0.0]], [[1.0, 2.0], [3.0, 3.0], [5.0, 2.0], [1.0, 2.0]]]]}}}}",
            "{\"locIntX\": {\"type\": \"MultiLine\", \"coordinates\": [[[0.0, 0.0], [3.0, 6.0], [6.0, 1.0]], [[1.0, 2.0], [3.0, 3.0]]]}}}" +
                    "| {\"loc\": {\"$geoIntersects\": {\"$geometry\": {\"type\": \"MultiLineString\", \"coordinates\": [[[0.0, 0.0], [3.0, 6.0], [6.0, 1.0]], [[1.0, 2.0], [3.0, 3.0]]]}}}}",
            "{\"locIntX\": {\"type\": \"GeometryCollection\", \"geometries\": [{\"type\": \"MultiPoint\", \"coordinates\": [[-73.9580, 40.8003], [-73.9498, 40.7968], [-73.9737, 40.7648], [-73.9814, 40.7681]]}, {\"type\": \"MultiLineString\", \"coordinates\": [[[-73.96943, 40.78519], [-73.96082, 40.78095]], [[-73.96415, 40.79229], [-73.95544, 40.78854]], [[-73.97162, 40.78205], [-73.96374, 40.77715]], [[-73.97880, 40.77247], [-73.97036, 40.76811]]]}]}}" +
                    "| {\"loc\": {\"$geoIntersects\": {\"$geometry\": {\"type\": \"GeometryCollection\", \"geometries\": [{\"type\": \"MultiPoint\", \"coordinates\": [[-73.958, 40.8003], [-73.9498, 40.7968], [-73.9737, 40.7648], [-73.9814, 40.7681]]}, {\"type\": \"MultiLineString\", \"coordinates\": [[[-73.96943, 40.78519], [-73.96082, 40.78095]], [[-73.96415, 40.79229], [-73.95544, 40.78854]], [[-73.97162, 40.78205], [-73.96374, 40.77715]], [[-73.9788, 40.77247], [-73.97036, 40.76811]]]}]}}}}",
    }, delimiter = '|')
    void testGeoQuery(String data, String expected) {
        GeoQuery query = BeanUtil.parse(data, GeoQuery.class);
        Bson filters = MongoFilterBuilder.buildFilter(query);
        assertEquals(expected, filters.toBsonDocument(Document.class, codecRegistry).toJson());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "{\"locPolygon\": [[1.0, 1.0], [1.0, 2.0]]}  | Polygon query should provide at lease 3 points.",
    }, delimiter = '|')
    void failureCaseForGeoQuery(String data, String message) {
        GeoQuery query = BeanUtil.parse(data, GeoQuery.class);
        Bson filters = MongoFilterBuilder.buildFilter(query);
        assertEquals("{}", filters.toBsonDocument().toJson(), message);
    }

    @Test
    void withInGeoPoly() {
        List<Point> exterior = Lists.newArrayList(new Point(0, 0), new Point(3, 6), new Point(6, 1));
        GeoPolygon geoPolygon = new GeoPolygon(List.of(exterior));

        Bson filters = MongoGeoFilters.withIn("loc", geoPolygon);

        String expected = "{\"loc\": {\"$geoWithin\": {\"$geometry\": {\"type\": \"Polygon\", \"coordinates\": " +
                "[[[0.0, 0.0], [3.0, 6.0], [6.0, 1.0], [0.0, 0.0]]]}}}}";
        assertThat(filters.toBsonDocument(Document.class, codecRegistry).toJson()).isEqualTo(expected);
    }
}