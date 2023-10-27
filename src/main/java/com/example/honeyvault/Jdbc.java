//package com.example.honeyvault;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.BeanPropertyRowMapper;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public class Jdbc {
//    private final JdbcTemplate jdbcTemplate;
//
//    @Autowired
//    public Jdbc(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    public List<TCN12306_1> getAllDataFromTable() {
//        String sql = "SELECT * FROM T_CN12306_1";
//        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TCN12306_1.class));
//    }
//
//    public void saveCN12306(CN12306 cn12306) {
//        String sql = "INSERT INTO CN12306 (id, passwd_cn, password, path) VALUES (?, ?, ?, ?)";
//        Object[] params = {cn12306.getId(), cn12306.getPasswd_CN(), cn12306.getPassword(), cn12306.getPath()};
//
//// 执行 SQL 插入操作
//        jdbcTemplate.update(sql, params);
//
//    }
//
//    public void updatePath(CN12306 cn12306) {
//        String sql = "UPDATE cn12306 SET path = ? WHERE id = ?";
//        jdbcTemplate.update(sql, cn12306.getPath(), cn12306.getId());
//
//    }
//
//    public List<String> queryForList(String sql, Class<String> stringClass) {
//        return jdbcTemplate.queryForList(sql, String.class);
//    }
//
//    public void setPathNull() {
//        String sql = "update PASSWD_PATH set path = null";
//        jdbcTemplate.execute(sql);
//
//    }
//
//    public void unique() {
//        String sql = "DELETE FROM cn12306 WHERE ROWID NOT IN (SELECT MIN(ROWID) FROM cn12306 GROUP BY passwd_CN,PASSWORD)";
//        jdbcTemplate.execute(sql);
//    }
//
//    public List<String> get12306Email() {
//        String sql = "select EMAIL0 from T_12306";
//        return jdbcTemplate.queryForList(sql, String.class);
//    }
//
//    public List<PasswdPath> query4Path() {
//        String sql = "select * from PASSWD_PATH";
//        return jdbcTemplate.queryForList(sql, PasswdPath.class);
//    }
//
//    public void insertPasswdPath() {
//        String sql = "INSERT INTO PASSWD_PATH\n" +
//                "    (ID, EMAIL_CN, ID12306, PASSWD_CN, PASSWORD12306, PATH, PHONE, REAL_NAME, USER_NAME, " +
//                "USER_NAME_CN)\n" +
//                "select T_12306.id,email_CN,id12306,passwd_CN,PASSWORD12306,null,PHONE,real_name,user_name," +
//                "user_name_cn\n" +
//                "from t_csdn join T_12306 on T_12306.EMAIL0 = t_csdn.email_CN";
//        jdbcTemplate.update(sql);
//    }
//
//}
