package win.doyto.query.data;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CountOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import win.doyto.query.core.DataAccess;
import win.doyto.query.core.IdWrapper;
import win.doyto.query.core.PageQuery;
import win.doyto.query.util.BeanUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.persistence.Table;

import static com.mongodb.client.model.Filters.eq;
import static win.doyto.query.core.MongoFilterUtil.buildFilter;

/**
 * MongoDataAccess
 *
 * @author f0rb on 2021-11-23
 */
@Slf4j
public class MongoDataAccess<E extends MongoPersistable<I>, I extends Serializable, Q extends PageQuery> implements DataAccess<E, I, Q> {
    private final Class<E> entityClass;
    @Getter
    private final MongoCollection<Document> collection;

    public MongoDataAccess(MongoClient mongoClient, Class<E> testEntityClass) {
        this.entityClass = testEntityClass;
        Table table = testEntityClass.getAnnotation(Table.class);
        MongoDatabase database = mongoClient.getDatabase(table.catalog());
        this.collection = database.getCollection(table.name());
    }

    private Bson getIdFilter(Object id) {
        return eq("_id", new ObjectId(id.toString()));
    }

    @Override
    public List<E> query(Q query) {
        FindIterable<Document> findIterable = collection
                .find(buildFilter(query))
                .skip(query.calcOffset())
                .limit(query.getPageSize());
        List<E> list = new ArrayList<>();
        findIterable.forEach((Consumer<Document>) document -> {
            E e = BeanUtil.parse(document.toJson(), entityClass);
            if (log.isDebugEnabled()) {
                log.debug("Entity parsed: {}", BeanUtil.stringify(e));
            }
            list.add(e);
        });
        return list;
    }

    @Override
    public long count(Q query) {
        return collection.countDocuments(buildFilter(query), new CountOptions().limit(query.getPageSize()).skip(query.calcOffset()));
    }

    @Override
    public <V> List<V> queryColumns(Q q, Class<V> clazz, String... columns) {
        return null;
    }

    @Override
    public E get(IdWrapper<I> w) {
        FindIterable<Document> findIterable = collection.find(getIdFilter(w.getId()));
        for (Document document : findIterable) {
            return BeanUtil.parse(document.toJson(), entityClass);
        }
        return null;
    }

    @Override
    public int delete(IdWrapper<I> w) {
        return 0;
    }

    @Override
    public int delete(Q query) {
        return (int) collection.deleteMany(buildFilter(query)).getDeletedCount();
    }

    @Override
    public void create(E e) {
        Document document = BeanUtil.convertToIgnoreNull(e, Document.class);
        collection.insertOne(document);
        e.setObjectId((ObjectId) document.get("_id"));
    }

    @Override
    public int update(E e) {
        Bson filter = getIdFilter(e.getId());
        Document replacement = BeanUtil.convertTo(e, Document.class);
        replacement.remove("_id");
        return (int) collection.replaceOne(filter, replacement).getModifiedCount();
    }

    @Override
    public int patch(E e) {
        return 0;
    }

    @Override
    public int patch(E e, Q q) {
        return 0;
    }

    @Override
    public List<I> queryIds(Q query) {
        return null;
    }
}
