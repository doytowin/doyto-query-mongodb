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

package win.doyto.query.mongodb;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.core.AggregationQuery;
import win.doyto.query.core.DataQueryClient;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.entity.Persistable;
import win.doyto.query.mongodb.aggregation.AggregationMetadata;
import win.doyto.query.mongodb.aggregation.CollectionProvider;
import win.doyto.query.mongodb.entity.BeanDocMapper;
import win.doyto.query.mongodb.entity.DocMapper;
import win.doyto.query.mongodb.session.MongoSessionThreadLocalSupplier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static win.doyto.query.mongodb.MongoConstant.COUNT_KEY;

/**
 * MongoDataQuery
 *
 * @author f0rb on 2022-01-25
 */
public class MongoDataQueryClient implements DataQueryClient {
    private final Supplier<ClientSession> mongoSessionSupplier;
    private final CollectionProvider collectionProvider;

    MongoDataQueryClient(MongoClient mongoClient) {
        this(mongoClient, MongoSessionThreadLocalSupplier.create(mongoClient));
    }

    public MongoDataQueryClient(MongoClient mongoClient, Supplier<ClientSession> mongoSessionSupplier) {
        this.mongoSessionSupplier = mongoSessionSupplier;
        this.collectionProvider = new CollectionProvider(mongoClient);
    }

    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    List<V> query(Q query, Class<V> viewClass) {
        return commonQuery(query, viewClass);
    }

    @SuppressWarnings("unchecked")
    private <V, Q extends DoytoQuery> List<V>
    commonQuery(Q query, Class<V> viewClass) {
        AggregationMetadata<MongoCollection<Document>> md =
                AggregationMetadata.build(viewClass, collectionProvider);
        List<Bson> pipeline = md.buildAggregation(query);
        DocMapper<V> docMapper = MongoConstant.getDocMapper(viewClass, this::newMapper);
        return md.getCollection()
                 .aggregate(mongoSessionSupplier.get(), pipeline)
                 .map(docMapper::map)
                 .into(new ArrayList<>());
    }

    @Override
    public <V extends Persistable<I>, I extends Serializable, Q extends DoytoQuery>
    long count(Q query, Class<V> viewClass) {
        AggregationMetadata<MongoCollection<Document>> md =
                AggregationMetadata.build(viewClass, collectionProvider);
        List<Bson> pipeline = md.buildCount(query);
        Integer count = md.getCollection()
                          .aggregate(mongoSessionSupplier.get(), pipeline)
                          .map(document -> document.getInteger(COUNT_KEY))
                          .first();
        return ObjectUtils.defaultIfNull(count, 0);
    }

    @Override
    public <V, Q extends DoytoQuery & AggregationQuery> List<V> aggregate(Q query, Class<V> viewClass) {
        return commonQuery(query, viewClass);
    }

    protected <V> BeanDocMapper<V> newMapper(Class<V> viewClass) {
        return new BeanDocMapper<>(viewClass);
    }
}
