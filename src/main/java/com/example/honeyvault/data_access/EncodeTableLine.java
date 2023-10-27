package com.example.honeyvault.data_access;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class EncodeTableLine<T> implements Serializable {
    T originValue;
    Double prob;
    double lowerBound = 0;
    double upperBound;
}
