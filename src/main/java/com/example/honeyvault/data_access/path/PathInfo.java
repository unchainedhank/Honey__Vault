package com.example.honeyvault.data_access.path;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PathInfo {
    private String path;
    private int length;
    private int lengthMinusDelete;
}