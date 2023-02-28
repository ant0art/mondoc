package com.dellin.mondoc.model.repository;

import com.dellin.mondoc.model.entity.Company;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
	
	@EntityGraph(type = EntityGraph.EntityGraphType.FETCH, value = "company_entity-graph")
	Optional<Company> findByInn(String inn);
}
