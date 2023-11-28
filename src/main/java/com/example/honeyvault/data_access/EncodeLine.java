package com.example.honeyvault.data_access;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class EncodeLine<T> implements Serializable {
    T originValue;
    Double prob;
    BigInteger lowerBound = BigInteger.valueOf(0);
    BigInteger upperBound;

}
