package com.slopeoasis.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.slopeoasis.user.entity.VrednostX;

@Repository
public interface VrednostXRepo extends JpaRepository<VrednostX, Integer>{

}
