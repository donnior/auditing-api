package com.example.demo.staff.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QWAccountRepository extends JpaRepository<QWAccount, String> {

}
