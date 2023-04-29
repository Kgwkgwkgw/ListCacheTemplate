package org.tommy.core.extractor;

import org.tommy.core.key.Key;

@FunctionalInterface
public interface KeyExtractor<V> {
    Key getKey(V data);
}
