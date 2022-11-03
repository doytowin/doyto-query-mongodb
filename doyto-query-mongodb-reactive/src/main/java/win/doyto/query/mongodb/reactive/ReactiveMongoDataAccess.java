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
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import win.doyto.query.core.DoytoQuery;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.entity.Persistable;
import win.doyto.query.mongodb.filter.MongoFilterBuilder;
import win.doyto.query.reactive.core.ReactiveDataAccess;

import java.io.Serializable;
import javax.persistence.Entity;

/**
 * ReactiveMongoDataAccess
 *
 * @author f0rb on 2021-12-14
 */
@Slf4j
public class ReactiveMongoDataAccess<E extends Persistable<I>, I extends Serializable, Q extends DoytoQuery> implements ReactiveDataAccess<E, I, Q> {
    @Getter
    private final MongoCollection<Document> collection;

    public ReactiveMongoDataAccess(MongoClient mongoClient, Class<E> entityClass) {
        Entity entityAnno = entityClass.getAnnotation(Entity.class);
        MongoDatabase database = mongoClient.getDatabase(entityAnno.database());
        this.collection = database.getCollection(entityAnno.name());
    }

    @Override
    public Mono<E> create(E e) {
        return null;
    }

    @Override
    public Flux<E> query(Q q) {
        return null;
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
        return (Mono<Long>) collection.countDocuments(filter);
    }

    @Override
    public Mono<E> get(I id) {
        return null;
    }

    @Override
    public Mono<E> get(IdWrapper<I> w) {
        return null;
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
