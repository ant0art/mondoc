package com.dellin.mondoc.model.repository;

import com.dellin.mondoc.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	
	Optional<Order> findByDocId(String docId);
}
