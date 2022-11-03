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

package win.doyto.query.mongodb.reactive;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;
import win.doyto.query.mongodb.aggregation.AggregationMetadata;
import win.doyto.query.mongodb.filter.MongoFilterBuilder;
import win.doyto.query.mongodb.reactive.session.ReactiveCollectionProvider;
import win.doyto.query.mongodb.reactive.session.ReactiveSessionSupplier;
import win.doyto.query.mongodb.reactive.session.ReactiveSessionThreadLocalSupplier;
import win.doyto.query.reactive.core.ReactiveDataAccess;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static win.doyto.query.mongodb.MongoConstant.MONGO_ID;

/**
 * ReactiveMongoDataAccess
 *
 * @author f0rb on 2021-12-14
 */
@Slf4j
public class ReactiveMongoDataAccess<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> implements ReactiveDataAccess<E, I, Q> {
    @Getter
    private final MongoCollection<Document> collection;
    private final Class<E> entityClass;
    private final AggregationMetadata<MongoCollection<Document>> md;
    private final ReactiveSessionSupplier reactiveSessionSupplier;

    public ReactiveMongoDataAccess(MongoClient mongoClient, Class<E> entityClass) {
        this(entityClass, ReactiveSessionThreadLocalSupplier.create(mongoClient));
    }

    public ReactiveMongoDataAccess(Class<E> entityClass, ReactiveSessionSupplier reactiveSessionSupplier) {
        this.entityClass = entityClass;
        this.reactiveSessionSupplier = reactiveSessionSupplier;
        ReactiveCollectionProvider collectionProvider = new ReactiveCollectionProvider(reactiveSessionSupplier.getMongoClient());
        this.md = AggregationMetadata.build(entityClass, collectionProvider);
        this.collection = md.getCollection();
    }

    @Override
    public Mono<E> create(E e) {
        return null;
    }

    @Override
    public Flux<E> query(Q q) {
        List<Bson> pipeline = md.buildAggregation(q);
        return Flux.from(md.getCollection().aggregate(reactiveSessionSupplier.get(), pipeline))
                   .map(document -> BeanUtil.parse(document.toJson(), entityClass));
    }

    @Override
    public <V> Flux<V> queryColumns(Q q, Class<V> clazz, String... columns) {
        return null;
    }

    @Override
    public Flux<I> queryIds(Q q) {
        return null;
    }

    @Override
    public Mono<Long> count(Q q) {
        Bson filter = MongoFilterBuilder.buildFilter(q);
        return (Mono<Long>) collection.countDocuments(reactiveSessionSupplier.get(), filter);
    }

    @Override
    public Mono<E> get(I id) {
        return get(IdWrapper.build(id));
    }

    private Bson getIdFilter(Object id) {
        return eq(MONGO_ID, new ObjectId(id.toString()));
    }

    @Override
    public Mono<E> get(IdWrapper<I> w) {
        return Mono.from(collection.find(reactiveSessionSupplier.get(), getIdFilter(w.getId())))
                   .map(document -> BeanUtil.parse(document.toJson(), entityClass));
    }

    @Override
    public Mono<Integer> delete(I id) {
        return null;
    }

    @Override
    public Mono<Integer> delete(IdWrapper<I> w) {
        return null;
    }

    @Override
    public Mono<Integer> delete(Q q) {
        return null;
    }

    @Override
    public Mono<Integer> update(E e) {
        return null;
    }

    @Override
    public Mono<Integer> patch(E e) {
        return null;
    }

    @Override
    public Mono<Integer> patch(E e, Q q) {
        return null;
    }
}
