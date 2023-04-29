package org.tommy.core;

import org.tommy.core.callback.LoadUnCachedValueCallback;
import org.tommy.core.extractor.KeyExtractor;
import org.tommy.core.key.Key;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
//import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListCacheTemplate {
    // 기존 DTO에 영향을 주지 않는다
    // redis는 클러스터에 mget을 지원하지 않음
    private CacheManager cacheManager;

    public ListCacheTemplate(CacheManager cacheManger) {
        if (cacheManger == null) {
            throw new IllegalArgumentException();
        }
        this.cacheManager = cacheManger;

    }

    public <V> List<V> readListByIdList(String cacheName, List<Key> keyList, LoadUnCachedValueCallback<V> notCachedIdListCallback, KeyExtractor<V> keyExtractor, Class<V> clazz) {
        Cache cache = cacheManager.getCache(cacheName);

        // 캐시 조회
        List<V> cachedValueList = new ArrayList<>();
        List<Key> notCachedKeyList = new ArrayList<>();
        for (Key k : keyList) {
            V v = cache.get(k.getKeyValue(), clazz);
            if (v != null) {
                cachedValueList.add(v);
            } else {
                notCachedKeyList.add(k);
            }
        }
        // 캐시로 조회되지 않은 keyList
        List<V> readThroughValueList = null;

        // 캐시 되지 않은 데이터 콜백에서 공급 받고, 캐싱하기
        if (notCachedKeyList.size() > 0) {
            readThroughValueList = notCachedIdListCallback.readThrough(notCachedKeyList);
            Map<String, V> keyToValueMap = readThroughValueList.stream().collect(Collectors.toMap(v -> keyExtractor.getKey(v).getKeyValue(), Function.identity()));
            for (String k : keyToValueMap.keySet()) {
                cache.put(k, keyToValueMap.get(k));
            }
        }

        if (readThroughValueList == null || readThroughValueList.isEmpty()) {
            return cachedValueList;
            // 데이터 합쳐서 반환
        } else {
            return Stream.concat(cachedValueList.stream(), readThroughValueList.stream()).collect(Collectors.toList());
        }
    }
}
