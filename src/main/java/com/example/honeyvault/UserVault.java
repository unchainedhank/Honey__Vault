package com.example.honeyvault;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserVault {
    private List<String> vault;
    private String mainPassword;
    private String name;
    private String phone;
    private String idCard;
    private String birthDate;
    private String accountName;

}
