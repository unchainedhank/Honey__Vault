package com.example.honeyvault.data_access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class Jdbc {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public Jdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertPasswdPath() {
        String sql = "INSERT INTO PASSWD_PATH\n" +
                "    (ID, EMAIL_CN, ID12306, PASSWD_CN, PASSWORD12306, PATH, PHONE, REAL_NAME, USER_NAME, " +
                "USER_NAME_CN)\n" +
                "select T_12306.id,email_CN,id12306,passwd_CN,PASSWORD12306,null,PHONE,real_name,user_name," +
                "user_name_cn\n" +
                "from t_csdn join T_12306 on T_12306.EMAIL0 = t_csdn.email_CN";
        jdbcTemplate.update(sql);
    }

}
