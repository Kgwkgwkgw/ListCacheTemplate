package org.tommy.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
public class CompositeKeyExampleDto {
    private CompositeKey key;
    private Double data1;
    private String data2;

    @Builder
    @Getter
    @EqualsAndHashCode
    public static class CompositeKey {
        private Long id1;
        private String id2;
    }
}
