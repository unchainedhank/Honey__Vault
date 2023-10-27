package com.example.honeyvault.data_access;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DaoCN extends JpaRepository<Csdn, Long> {
    @Query(nativeQuery = true, value = "select passwd_CN from t_csdn ")
    List<Object[]> getPasswordAndPasswdCN();

}
