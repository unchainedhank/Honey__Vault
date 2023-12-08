package com.example.honeyvault.tool;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PathInfo {
    private String path;
    private int length;

}