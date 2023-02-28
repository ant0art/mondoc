package com.dellin.mondoc.model.repository;

import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.enums.OrderDocType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
	
	Collection<Document> findByBase64NullAndOrder_Company(Company company);
	
	List<Document> findByBase64Null();
	
	Optional<Document> findByUidAndType(String uid, OrderDocType type);
}
