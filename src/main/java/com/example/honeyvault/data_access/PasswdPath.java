package com.example.honeyvault.data_access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "PASSWD_PATH")
public class PasswdPath {
    @Id
    private Long id;
    private String passwd_CN;
    private String password12306;
    private String password163;
    private String email_CN;
    private String user_name_CN;
    private String user_name;
    private String real_name;
    private String id12306;
    private String phone;
    private String path;
}
