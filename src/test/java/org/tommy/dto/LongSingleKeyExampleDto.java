package org.tommy.dto;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LongSingleKeyExampleDto {
    private Long id;
    private Double data1;
    private String data2;
}
