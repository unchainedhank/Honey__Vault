package com.example.honeyvault.data_access;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DaoCN12306 extends JpaRepository<CN12306, Long> {

}
