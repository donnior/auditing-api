package com.xingcanai.csqe.auditing.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WxCardUserRepository extends JpaRepository<WxCardUser, String> {

    WxCardUser findTopByOrderByUpdateTimeDesc();

    WxCardUser findTopByOrderByCreateTimeDesc();
}

