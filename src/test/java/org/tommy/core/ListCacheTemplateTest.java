package org.tommy.core;

import org.tommy.core.callback.LoadUnCachedValueCallback;
import org.tommy.core.extractor.KeyExtractor;
import org.tommy.core.key.CompositeKey;
import org.tommy.core.key.Key;
import org.tommy.core.key.SingleKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.tommy.config.AppConfig;
import org.tommy.dto.CompositeKeyExampleDto;
import org.tommy.dto.LongSingleKeyExampleDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Import({AppConfig.class})
@ExtendWith(SpringExtension.class)
public class ListCacheTemplateTest {
    @Autowired
    private CacheManager cacheManager;
    private ListCacheTemplate listCacheTemplate;
    private KeyExtractor<LongSingleKeyExampleDto> idFieldKeyExtractor;
    private String compositeKeyCacheName = "compositeKeyExampleDto";
    private String longSingleKeyCacheName = "longSingleKeyExampleDto";
    ;


    @BeforeEach
    public void setup() {
        listCacheTemplate = new ListCacheTemplate(this.cacheManager);
        idFieldKeyExtractor = data -> {
            return new SingleKey(data.getId().toString());
        };
        Collection<String> cacheNames = cacheManager.getCacheNames();
        // 캐시 초기화
        for (String cache : cacheNames) {
            cacheManager.getCache(cache).clear();
        }
    }

    @Test
    public void 싱글_키에서_전부_캐싱되지_않은_키_일_경우_캐싱_테스트() {
        // given
        LongSingleKeyExampleDto uncached1 = LongSingleKeyExampleDto.builder()
                .id(1L)
                .data1(10.0D)
                .data2("TEST1")
                .build();
        LongSingleKeyExampleDto uncached2 = LongSingleKeyExampleDto.builder()
                .id(2L)
                .data1(11.0D)
                .data2("TEST2")
                .build();

        LoadUnCachedValueCallback loadUnCachedValueCallbackMock = Mockito.mock(LoadUnCachedValueCallback.class);
        Mockito.when(loadUnCachedValueCallbackMock.readThrough(Mockito.anyList())).thenReturn(List.of(uncached1, uncached2));
        List<Key> keyList = List.of(new SingleKey(uncached1.getId().toString()), new SingleKey(uncached2.getId().toString()));

        // when
        this.listCacheTemplate.readListByIdList(longSingleKeyCacheName, keyList, loadUnCachedValueCallbackMock, idFieldKeyExtractor, LongSingleKeyExampleDto.class);
        List<LongSingleKeyExampleDto> resList = this.listCacheTemplate.readListByIdList("longSingleKeyExampleDto", keyList, loadUnCachedValueCallbackMock, idFieldKeyExtractor, LongSingleKeyExampleDto.class);

        // then
        // 캐싱되지 않은 데이터가 1번만 호출되면, 캐싱된 데이터를 반환한 것이므로 성공이다.
        Mockito.verify(loadUnCachedValueCallbackMock, Mockito.times(1)).readThrough(keyList);

        Map<Key, LongSingleKeyExampleDto> idToLongSingleKeyExampleMap = resList.stream().collect(Collectors.toMap((data) -> idFieldKeyExtractor.getKey(data), Function.identity()));
        LongSingleKeyExampleDto resData1 = idToLongSingleKeyExampleMap.get(keyList.get(0));

        Assertions.assertNotNull(resData1);
        Assertions.assertEquals(uncached1.getId(), resData1.getId());
        Assertions.assertEquals(uncached1.getData1(), resData1.getData1());
        Assertions.assertEquals(uncached1.getData2(), resData1.getData2());

        LongSingleKeyExampleDto resData2 = idToLongSingleKeyExampleMap.get(keyList.get(1));
        Assertions.assertNotNull(resData2);
        Assertions.assertEquals(uncached2.getId(), resData2.getId());
        Assertions.assertEquals(uncached2.getData1(), resData2.getData1());
        Assertions.assertEquals(uncached2.getData2(), resData2.getData2());
    }

    @Test
    public void 싱글_키에서_일부는_캐싱된_키_일부는_캐싱되지_않은_키_일_경우_캐싱_테스트() {
        // given
        LongSingleKeyExampleDto cached1 = LongSingleKeyExampleDto.builder()
                .id(1L)
                .data1(10.0D)
                .data2("TEST1")
                .build();
        LongSingleKeyExampleDto uncached1 = LongSingleKeyExampleDto.builder()
                .id(2L)
                .data1(11.0D)
                .data2("TEST2")
                .build();
        // 캐싱
        this.listCacheTemplate.readListByIdList(longSingleKeyCacheName, List.of(new SingleKey(cached1.getId().toString())), (key) -> List.of(cached1), idFieldKeyExtractor, LongSingleKeyExampleDto.class);
        List<Key> keyList = List.of(new SingleKey(cached1.getId().toString()), new SingleKey(uncached1.getId().toString()));


        LoadUnCachedValueCallback loadUnCachedValueCallbackMock = Mockito.mock(LoadUnCachedValueCallback.class);
        Mockito.when(loadUnCachedValueCallbackMock.readThrough(List.of(new SingleKey(uncached1.getId().toString())))).thenReturn(List.of(uncached1));

        // when
        List<LongSingleKeyExampleDto> resList = this.listCacheTemplate.readListByIdList("longSingleKeyExampleDto", keyList, loadUnCachedValueCallbackMock, idFieldKeyExtractor, LongSingleKeyExampleDto.class);

        // then
        // 캐싱 안된 데이터만 조회되야함
        Mockito.verify(loadUnCachedValueCallbackMock, Mockito.times(1)).readThrough(List.of(new SingleKey(uncached1.getId().toString())));

        Map<Key, LongSingleKeyExampleDto> idToLongSingleKeyExampleMap = resList.stream().collect(Collectors.toMap((data) -> idFieldKeyExtractor.getKey(data), Function.identity()));
        LongSingleKeyExampleDto resData1 = idToLongSingleKeyExampleMap.get(keyList.get(0));

        Assertions.assertNotNull(resData1);
        Assertions.assertEquals(cached1.getId(), resData1.getId());
        Assertions.assertEquals(cached1.getData1(), resData1.getData1());
        Assertions.assertEquals(cached1.getData2(), resData1.getData2());

        LongSingleKeyExampleDto resData2 = idToLongSingleKeyExampleMap.get(keyList.get(1));
        Assertions.assertNotNull(resData2);
        Assertions.assertEquals(uncached1.getId(), resData2.getId());
        Assertions.assertEquals(uncached1.getData1(), resData2.getData1());
        Assertions.assertEquals(uncached1.getData2(), resData2.getData2());
    }


    @Test
    public void 복합_키에서_전부_캐싱되지_않은_키_일_경우_캐싱_테스트() {
        // given
        CompositeKeyExampleDto uncached1 = CompositeKeyExampleDto.builder()
                .key(CompositeKeyExampleDto.CompositeKey.builder().id1(1L).id2("id1").build())
                .data1(10.0D)
                .data2("TEST1")
                .build();

        CompositeKeyExampleDto uncached2 = CompositeKeyExampleDto.builder()
                .key(CompositeKeyExampleDto.CompositeKey.builder().id1(2L).id2("id2").build())
                .data1(11.0D)
                .data2("TEST2")
                .build();
        CompositeKey unCachedKey1 = new CompositeKey(uncached1.getKey().getId1(), uncached1.getKey().getId2());
        CompositeKey unCachedKey2 = new CompositeKey(uncached2.getKey().getId1(), uncached2.getKey().getId2());

        KeyExtractor<CompositeKeyExampleDto> compositeKeyExtractor = (data) -> new CompositeKey(data.getKey().getId1(), data.getKey().getId2());


        List<Key> keyList = List.of(unCachedKey1, unCachedKey2);
        LoadUnCachedValueCallback loadUnCachedValueCallbackMock = Mockito.mock(LoadUnCachedValueCallback.class);
        Mockito.when(loadUnCachedValueCallbackMock.readThrough(keyList)).thenReturn(List.of(uncached1, uncached2));


        // when
        this.listCacheTemplate.readListByIdList(compositeKeyCacheName, keyList, loadUnCachedValueCallbackMock, compositeKeyExtractor, CompositeKeyExampleDto.class);
        List<CompositeKeyExampleDto> resList = this.listCacheTemplate.readListByIdList(compositeKeyCacheName, keyList, loadUnCachedValueCallbackMock, compositeKeyExtractor, CompositeKeyExampleDto.class);

        // then
        // 캐싱 안된 데이터만 조회되야함
        Mockito.verify(loadUnCachedValueCallbackMock, Mockito.times(1)).readThrough(List.of(unCachedKey1, unCachedKey2));
        Map<Key, CompositeKeyExampleDto> idToCompositeKeyExampleDtoMap = resList.stream().collect(Collectors.toMap((data) -> compositeKeyExtractor.getKey(data), Function.identity()));
        CompositeKeyExampleDto resData1 = idToCompositeKeyExampleDtoMap.get(keyList.get(0));

        Assertions.assertNotNull(resData1);
        Assertions.assertEquals(uncached1.getKey(), resData1.getKey());
        Assertions.assertEquals(uncached1.getData1(), resData1.getData1());
        Assertions.assertEquals(uncached1.getData2(), resData1.getData2());

        CompositeKeyExampleDto resData2 = idToCompositeKeyExampleDtoMap.get(keyList.get(1));
        Assertions.assertNotNull(resData2);
        Assertions.assertEquals(uncached2.getKey(), resData2.getKey());
        Assertions.assertEquals(uncached2.getData1(), resData2.getData1());
        Assertions.assertEquals(uncached2.getData2(), resData2.getData2());
    }

    @Test
    public void 복합_키에서_일부는_캐싱된_키_일부는_캐싱되지_않은_키_일_경우_캐싱_테스트() {
        // given
        CompositeKeyExampleDto cached1 = CompositeKeyExampleDto.builder()
                .key(CompositeKeyExampleDto.CompositeKey.builder().id1(1L).id2("id1").build())
                .data1(10.0D)
                .data2("TEST1")
                .build();

        CompositeKeyExampleDto uncached1 = CompositeKeyExampleDto.builder()
                .key(CompositeKeyExampleDto.CompositeKey.builder().id1(2L).id2("id2").build())
                .data1(11.0D)
                .data2("TEST2")
                .build();
        CompositeKey cachedKey1 = new CompositeKey(cached1.getKey().getId1(), cached1.getKey().getId2());
        CompositeKey unCachedKey1 = new CompositeKey(uncached1.getKey().getId1(), uncached1.getKey().getId2());

        KeyExtractor<CompositeKeyExampleDto> compositeKeyExtractor = (data) -> new CompositeKey(data.getKey().getId1(), data.getKey().getId2());

        // 캐싱
        this.listCacheTemplate.readListByIdList(compositeKeyCacheName, List.of(cachedKey1), (key) -> List.of(cached1), compositeKeyExtractor, CompositeKeyExampleDto.class);

        List<Key> keyList = List.of(cachedKey1, unCachedKey1);


        LoadUnCachedValueCallback loadUnCachedValueCallbackMock = Mockito.mock(LoadUnCachedValueCallback.class);
        Mockito.when(loadUnCachedValueCallbackMock.readThrough(List.of(unCachedKey1))).thenReturn(List.of(uncached1));


        // when
        List<CompositeKeyExampleDto> resList = this.listCacheTemplate.readListByIdList(compositeKeyCacheName, keyList, loadUnCachedValueCallbackMock, compositeKeyExtractor, CompositeKeyExampleDto.class);

        // then
        // 캐싱 안된 데이터만 조회되야함
        Mockito.verify(loadUnCachedValueCallbackMock, Mockito.times(1)).readThrough(List.of(unCachedKey1));

        Map<Key, CompositeKeyExampleDto> idToCompositeKeyExampleDtoMap = resList.stream().collect(Collectors.toMap((data) -> compositeKeyExtractor.getKey(data), Function.identity()));
        CompositeKeyExampleDto resData1 = idToCompositeKeyExampleDtoMap.get(keyList.get(0));

        Assertions.assertNotNull(resData1);
        Assertions.assertEquals(cached1.getKey(), resData1.getKey());
        Assertions.assertEquals(cached1.getData1(), resData1.getData1());
        Assertions.assertEquals(cached1.getData2(), resData1.getData2());

        CompositeKeyExampleDto resData2 = idToCompositeKeyExampleDtoMap.get(keyList.get(1));
        Assertions.assertNotNull(resData2);
        Assertions.assertEquals(uncached1.getKey(), resData2.getKey());
        Assertions.assertEquals(uncached1.getData1(), resData2.getData1());
        Assertions.assertEquals(uncached1.getData2(), resData2.getData2());
    }
}
