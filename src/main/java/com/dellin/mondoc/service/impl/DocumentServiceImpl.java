package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.entity.Order;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.enums.OrderDocType;
import com.dellin.mondoc.model.pojo.DocumentRequest;
import com.dellin.mondoc.model.pojo.DocumentRequestBuilder;
import com.dellin.mondoc.model.pojo.DocumentResponse;
import com.dellin.mondoc.model.repository.DocumentRepository;
import com.dellin.mondoc.service.DocumentService;
import com.dellin.mondoc.service.UserService;
import com.dellin.mondoc.utils.EncodingUtil;
import java.io.*;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.util.*;
import java.util.stream.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
	
	private final UserService userService;
	
	private final DocumentRepository documentRepository;
	
	private final SyncService syncService;
	
	private Thread taskThread;
	
	private boolean initializedThread = false;
	
	@Override
	public void update() {
		Date programStart = new Date();
		log.info("Method [update() documents] started to work");
		
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userService.getUser(email);
		
		Runnable task = new Runnable() {
			
			final List<Document> documentList =
					getDocsByBase64NullAndCompanies(user.getCompanies());
			
			int count = 0;
			
			@Override
			public void run() {
				Thread thread = Thread.currentThread();
				
				extracted(thread, count, documentList, user);
				Date programEnd = new Date();
				long ms = programEnd.getTime() - programStart.getTime();
				log.info("Method [update() documents] finished after {} seconds of "
						+ "working", (ms / 1000L));
			}
		};
		
		if (!initializedThread) {
			initializedThread = true;
			taskThread = new Thread(task);
			taskThread.start();
		}
	}
	
	@Override
	public void stopUpdate() {
		
		if (initializedThread) {
			initializedThread = false;
			taskThread.interrupt();
			log.warn("Update was manually stopped");
		}
	}
	
	@Override
	public void updateDocData(Document document, Collection<DocumentResponse.Data> data) {
		data.forEach(d -> {
			
			if (d.getBase64() != null && !d.getBase64().isEmpty()) {
				document.setBase64(d.getBase64());
			}
			if (document.getType() == OrderDocType.GIVEOUT) {
				if (!d.getUrls().isEmpty()) {
					d.getUrls()
							.stream()
							.filter(Objects::nonNull).findFirst()
													 .ifPresent(document::setUrl);
				}
			}
			Order order = document.getOrder();
			order.setStatus(EntityStatus.UPDATED);
			order.setUpdatedAt(LocalDateTime.now());
			
			document.setStatus(EntityStatus.UPDATED);
			document.setUpdatedAt(LocalDateTime.now());
			documentRepository.save(document);
			log.info("Document: [TYPE: {}, UID: {}] updated", document.getType().name(),
					document.getUid());
		});
	}
	
	@Override
	public List<Document> getDocsByBase64Null() {
		return documentRepository.findByBase64Null();
	}
	
	public void extracted(Thread thread, int count, List<Document> documentList,
			User user) {
		while (count < documentList.size() && !thread.isInterrupted()) {
			try {
				log.info("Starting cycle of updating documents at [{}] of [{}]",
						count + 1, documentList.size());
				Document document = documentList.get(count);
				
				log.info("Current document to update: [ID: {}, TYPE:{}, UID: "
								+ "{}, OrdID: {}]", document.getId(), document.getType().name(),
						document.getUid(), document.getOrder().getDocId());
				
				DocumentRequestBuilder requestBuilder = DocumentRequest.builder()
						.setAppkey(
								EncodingUtil.getDecrypted(user.getSession().getAppkey()))
						.setSessionID(EncodingUtil.getDecrypted(
								user.getSession().getSessionDl()))
						.setMode(document.getType().name().toLowerCase())
						.setDocUID(document.getUid());
				DocumentRequest build = requestBuilder.build();
				Date start = new Date();
				log.info("Sending request to API");
				Call<DocumentResponse> availableDoc =
						syncService.getRemoteData().getPrintableDoc(build);
				
				Response<DocumentResponse> docResponse = availableDoc.execute();
				log.info("Got the response in {} ms",
						(new Date().getTime() - start.getTime()) / 1000.);
				
				if (!docResponse.isSuccessful()) {
					
					log.error(docResponse.errorBody() != null ? docResponse.errorBody()
																		   .string()
							: "Unknown error");
					count++;
					Thread.sleep(10000L);
					continue;
				}
				
				assert docResponse.body() != null;
				Collection<DocumentResponse.Data> data = docResponse.body().getData();
				updateDocData(document, data);
				
				count++;
				log.info("Method [update() documents] ended process on doc [{}] "
						+ "of [{}]", count, documentList.size());
				
				Thread.sleep(10000L);
			} catch (InterruptedException | IOException e) {
				log.error(e.getMessage());
				thread.interrupt();
			}
		}
	}
	
	public List<Document> getDocsByBase64NullAndCompanies(Collection<Company> companies) {
		
		return companies.stream()
				.flatMap(c -> documentRepository.findByBase64NullAndOrder_Company(c)
						.stream())
				.collect(Collectors.toList());
	}
	
	public void setTaskThread(Thread thread) {
		this.taskThread = thread;
	}
	
	public void setInitializedThread(boolean initializedThread) {
		this.initializedThread = initializedThread;
	}
}

