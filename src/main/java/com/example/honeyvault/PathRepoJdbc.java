package com.example.honeyvault;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PathRepoJdbc {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void Jdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setPathNull() {
        String sql = "UPDATE passwd_path\n" +
                "SET path = NULL\n" +
                "WHERE INSTR(path, '()') > 0";
        jdbcTemplate.execute(sql);
    }


}
