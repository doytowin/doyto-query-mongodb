package win.doyto.query.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Iterator;

/**
 * CollectionUtil
 *
 * @author f0rb
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CollectionUtil {
    public static <E> E first(Iterable<E> iterable) {
        Iterator<E> iterator = iterable.iterator();
        try {
            return iterator.hasNext() ? iterator.next() : null;
        } finally {
            if (iterator.hasNext()) {
                log.warn("Find more than one element of {}", iterator.next().getClass());
                StringBuilder sb = new StringBuilder();
                for (E e : iterable) {
                    sb.append("\n").append(e instanceof String ? e : ToStringBuilder.reflectionToString(e, NonNullToStringStyle.NON_NULL_STYLE));
                }
                log.warn("Repetitive elements: {}", sb.toString());
            }
        }
    }
}
