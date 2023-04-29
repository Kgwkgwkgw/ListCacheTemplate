package org.tommy.core.callback;

import org.tommy.core.key.Key;

import java.util.List;

@FunctionalInterface
public interface LoadUnCachedValueCallback<V> {
    List<V> readThrough(List<Key> noCachedIdList);
}