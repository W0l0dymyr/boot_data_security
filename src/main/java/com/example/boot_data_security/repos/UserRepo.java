package com.example.boot_data_security.repos;

import com.example.boot_data_security.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
    public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
    @Query(value = "select record from users" ,nativeQuery = true)
    int getRecord();
    @Transactional
@Modifying
    @Query(value = "update users u set u.record = ? where u.username = ?",nativeQuery = true)
    int setRecord(int count, String name);

    List<User> findAll();

    Optional<User> findById(Long id);

    User findByActivationCode(String code);

    User findByEmail(String username);
}
