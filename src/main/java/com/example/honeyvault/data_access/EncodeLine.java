package com.example.honeyvault.data_access;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class EncodeLine<T> implements Serializable {
    T originValue;
    Double prob;
    double lowerBound = 0;
    double upperBound;
}
