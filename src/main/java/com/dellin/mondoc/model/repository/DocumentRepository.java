package com.dellin.mondoc.model.repository;

import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.enums.OrderDocType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
	
	Optional<Document> findByUidAndType(String uid, OrderDocType type);
}
