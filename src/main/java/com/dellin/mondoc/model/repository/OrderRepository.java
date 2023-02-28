package com.dellin.mondoc.model.repository;

import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	
	@Transactional
	Optional<Order> findByDocId(String docId);
	
	Page<Order> findByCompanyIn(Collection<Company> companies, Pageable pageable);
}
