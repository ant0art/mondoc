package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.entity.Order;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.enums.OrderDocType;
import com.dellin.mondoc.model.pojo.DocumentRequest;
import com.dellin.mondoc.model.pojo.DocumentRequestBuilder;
import com.dellin.mondoc.model.pojo.DocumentResponse;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.repository.DocumentRepository;
import com.dellin.mondoc.service.DocumentService;
import com.dellin.mondoc.service.UserService;
import com.dellin.mondoc.utils.EncodingUtil;
import java.io.*;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.util.*;
import java.util.stream.*;

/**
 * Service class to work with Documents
 *
 * @see Document
 * @see DocumentRepository
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
	
	/**
	 * User service class
	 */
	private final UserService userService;
	
	/**
	 * Repository which contains documents
	 */
	private final DocumentRepository documentRepository;
	
	/**
	 * Injection of Retrofit service requests
	 */
	private final SyncService syncService;
	
	/**
	 * Service thread is used for updating documents
	 */
	@Setter
	private Thread taskThread;
	
	/**
	 * Switcher of thread state
	 */
	@Setter
	private boolean initializedThread = false;
	
	/**
	 * Method that updates document database by connecting to Dellin API
	 * <p>
	 * The method connects to the Dellin API by sending a request that contains the
	 * following required parameters: pre-decrypted appkey, pre-decrypted sessionID, mode,
	 * docUID.
	 * <pre>
	 *  <b>appkey</b> - the encrypted API key
	 *  <b>sessionID</b> - the encrypted unique session value previously received by the
	 *  user after authorization in the API through the method
	 *  {@link SessionServiceImpl#getLoginResponse(SessionDTO)}
	 *  <b>mode</b> - one of the possible document types {@link OrderDocType}
	 *  <b>docUID</b> - the value docUID of {@link Document} that was received earlier by
	 *  another method {@link OrderServiceImpl#update(OrderRequest)}</pre>
	 * <p>
	 * Using multithreading, the method checks all available documents that require
	 * updating. The timeout between requests set up as the recommended Dellin interval of
	 * 10 seconds. That`s why method checks the time of receipt of the response and send
	 * the request for the next document after the remaining time.
	 * <p>
	 * One necessary check concerns the document type, since only GIVEOUT have a direct
	 * link.
	 * <p>
	 * Updating data is possible only for authorized users, since any change is recorded
	 * in the history.
	 *
	 * @see #extracted(Thread, int, List, User)
	 * @see #updateDocData(Document, Collection)
	 * @see #stopUpdate()
	 */
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
	
	/**
	 * Method that interrupt process of updating document database
	 * <p>
	 * Method interrupts earlier started thread of updating documents
	 *
	 * @see DocumentServiceImpl#update()
	 */
	@Override
	public void stopUpdate() {
		
		if (initializedThread) {
			initializedThread = false;
			taskThread.interrupt();
			log.warn("Update was manually stopped");
		}
	}
	
	/**
	 * Method that update current Document by API data values
	 * <p>
	 * Method iterates each document of received Response in order to fill in empty
	 * Document fields and save them to database
	 *
	 * @param document the {@link Document} object to update
	 * @param data     the collection of {@link DocumentResponse.Data}
	 *
	 * @see #update()
	 * @see #extracted(Thread, int, List, User)
	 * @see #stopUpdate()
	 */
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
	
	/**
	 * Method that searches the database for all documents whose base64 field is equal to
	 * Null
	 * <p>
	 * Returns the List of Documents with base64 null
	 *
	 * @return the {@link List}&lt;{@link Document}&gt;
	 */
	@Override
	public List<Document> getDocsByBase64Null() {
		return documentRepository.findByBase64Null();
	}
	
	/**
	 * Extracted method that continue logic of update method. Separated for better view
	 *
	 * @param thread       current {@link Thread} of updating documents
	 * @param count        the value of start position of iterating
	 * @param documentList the {@link List}&lt;{@link Document}&gt; to update
	 * @param user         the user that send the request to API
	 *
	 * @see #update()
	 * @see #updateDocData(Document, Collection)
	 * @see #stopUpdate()
	 */
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
				long responseTime = new Date().getTime() - start.getTime();
				log.info("Got the response in {} ms", responseTime / 1000.);
				
				if (!docResponse.isSuccessful()) {
					
					log.error(docResponse.errorBody() != null ? docResponse.errorBody()
																		   .string()
							: "Unknown error");
					count++;
					long timeout = 10000L - responseTime;
					log.info("Timeout before next request {} sec", timeout / 1000.);
					Thread.sleep(timeout);
					continue;
				}
				
				assert docResponse.body() != null;
				Collection<DocumentResponse.Data> data = docResponse.body().getData();
				updateDocData(document, data);
				
				count++;
				log.info("Method [update() documents] ended process on doc [{}] "
						+ "of [{}]", count, documentList.size());
				
				long timeout = 10000L - responseTime;
				log.info("Timeout before next request {} sec", timeout / 1000.);
				Thread.sleep(timeout);
			} catch (InterruptedException | IOException e) {
				log.error(e.getMessage());
				thread.interrupt();
			}
		}
	}
	
	/**
	 * Method that searches the database for all documents whose base64 field is equal to
	 * Null and who belongs to any Company in the given List
	 * <p>
	 * Returns the List of Documents with base64 null and Company from List
	 *
	 * @param companies the {@link List}&lt;{@link Company}&gt; to search in database
	 *
	 * @return the {@link List}&lt;{@link Document}&gt;
	 */
	public List<Document> getDocsByBase64NullAndCompanies(Collection<Company> companies) {
		
		return companies.stream()
				.flatMap(c -> documentRepository.findByBase64NullAndOrder_Company(c)
						.stream())
				.collect(Collectors.toList());
	}
}

