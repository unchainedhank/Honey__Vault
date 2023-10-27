package com.example.honeyvault.data_access;

import lombok.*;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "t_mkv_table")
public class MkvTableLine {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    String originValue;
    Double prob;
    int lowerBound = 0;
    int upperBound;
}
