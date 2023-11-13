package com.example.honeyvault;

import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserVault {
    private List<String> vault;
    private String mainPassword;
    private String email;
    private String name;
    private String phone;
    private String idCard;
    private Date birthDate;
    private String accountName;

}
