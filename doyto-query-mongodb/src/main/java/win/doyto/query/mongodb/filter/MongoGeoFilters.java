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

import com.mongodb.client.model.Filters;
import lombok.experimental.UtilityClass;
import org.bson.conversions.Bson;
import win.doyto.query.geo.*;

import java.util.List;

/**
 * MongoQuerySuffix
 *
 * @author f0rb on 2021-11-30
 */
@SuppressWarnings("java:S115")
@UtilityClass
public class MongoGeoFilters {

    public static Bson near(String column, Object value) {
        if (value instanceof NearSphere) {
            return nearSphere(column, value);
        } else {
            Near near = (Near) value;
            return Filters.near(column, near.getX(), near.getY(), near.getMaxDistance(), near.getMinDistance());
        }
    }

    public static Bson nearSphere(String column, Object value) {
        Near near = (Near) value;
        return Filters.nearSphere(column, near.getX(), near.getY(), near.getMaxDistance(), near.getMinDistance());
    }

    public static Bson withinCenter(String column, Object value) {
        Circle circle = (Circle) value;
        return Filters.geoWithinCenter(column, circle.getX(), circle.getY(), circle.getRadius());
    }

    public static Bson withinCenterSphere(String column, Object value) {
        Circle circle = (Circle) value;
        return Filters.geoWithinCenterSphere(column, circle.getX(), circle.getY(), circle.getRadius());
    }

    public static Bson withinBox(String column, Object value) {
        Box box = (Box) value;
        Point p1 = box.getP1();
        Point p2 = box.getP2();
        return Filters.geoWithinBox(column, p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    @SuppressWarnings("unchecked")
    public static Bson withinPolygon(String column, Object value) {
        List<List<Double>> points = ((List<Point>) value)
                .stream().map(Point::toList).toList();

        return Filters.geoWithinPolygon(column, points);
    }

    static Bson withIn(String column, Object value) {
        if (GeoTransformer.support(value)) {
            return Filters.geoWithin(column, GeoTransformer.transform(value));
        } else if (value instanceof Bson bson) {
            return Filters.geoWithin(column, bson);
        } else {
            throw new UnsupportedGeoTypeException(value.getClass().getName());
        }
    }

    public static Bson intersects(String column, Object value) {
        if (GeoTransformer.support(value)) {
            return Filters.geoIntersects(column, GeoTransformer.transform(value));
        } else if (value instanceof Bson bson) {
            return Filters.geoIntersects(column, bson);
        } else {
            throw new UnsupportedGeoTypeException(value.getClass().getName());
        }
    }
}
