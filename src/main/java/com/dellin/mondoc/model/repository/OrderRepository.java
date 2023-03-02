package com.dellin.mondoc.model.repository;

import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	
	@EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value = "order_entity-graph")
	Optional<Order> findByDocId(String docId);
	
	Page<Order> findByCompanyIn(Collection<Company> companies, Pageable pageable);
}
