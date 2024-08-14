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
import org.bson.Document;
import org.bson.conversions.Bson;
import win.doyto.query.core.AggregateClient;
import win.doyto.query.core.AggregatedQuery;
import win.doyto.query.mongodb.aggregation.AggregationMetadata;
import win.doyto.query.mongodb.aggregation.CollectionProvider;
import win.doyto.query.mongodb.session.MongoSessionThreadLocalSupplier;
import win.doyto.query.util.BeanUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * MongoAggregateClient
 *
 * @author f0rb on 2024/8/14
 */
public class MongoAggregateClient implements AggregateClient {
    private final Supplier<ClientSession> mongoSessionSupplier;
    private final CollectionProvider collectionProvider;

    MongoAggregateClient(MongoClient mongoClient) {
        this(mongoClient, MongoSessionThreadLocalSupplier.create(mongoClient));
    }

    public MongoAggregateClient(MongoClient mongoClient, Supplier<ClientSession> mongoSessionSupplier) {
        this.mongoSessionSupplier = mongoSessionSupplier;
        this.collectionProvider = new CollectionProvider(mongoClient);
    }

    @Override
    public <V, A extends AggregatedQuery>
    List<V> aggregate(Class<V> viewClass, A aggregatedQuery) {
        AggregationMetadata<MongoCollection<Document>> md =
                AggregationMetadata.build(viewClass, collectionProvider);
        List<Bson> pipeline = md.buildByAggregatedQuery(aggregatedQuery);
        return md.getCollection()
                 .aggregate(mongoSessionSupplier.get(), pipeline)
                 .map(document -> BeanUtil.parse(document.toJson(), viewClass))
                 .into(new ArrayList<>());
    }
}
