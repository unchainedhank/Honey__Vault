package com.example.honeyvault.data_access;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface PathRepo extends JpaRepository<PasswdPath, Long> {

    @Modifying
    @Query(value = "UPDATE passwd_path SET path = NULL WHERE INSTR(path, '()') > 0",nativeQuery = true)
    void updatePath();


}
