package com.example.demo.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.demo.bean.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {

	Page<UserEntity> findAll(Pageable pageable);

	UserEntity findById(String id);

}
