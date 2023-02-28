package com.dellin.mondoc.model.repository;

import com.dellin.mondoc.model.entity.Comment;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
	
	@NotNull
	@Override
	Optional<Comment> findById(@NotNull Long id);
}
