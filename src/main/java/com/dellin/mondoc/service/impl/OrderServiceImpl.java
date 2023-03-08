package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.entity.Company;
import com.dellin.mondoc.model.entity.Document;
import com.dellin.mondoc.model.entity.Order;
import com.dellin.mondoc.model.entity.Role;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.enums.OrderDocType;
import com.dellin.mondoc.model.pojo.CommentHistory;
import com.dellin.mondoc.model.pojo.OrderModel;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderRequestBuilder;
import com.dellin.mondoc.model.pojo.OrderResponse;
import com.dellin.mondoc.model.repository.CompanyRepository;
import com.dellin.mondoc.model.repository.DocumentRepository;
import com.dellin.mondoc.model.repository.OrderRepository;
import com.dellin.mondoc.service.OrderService;
import com.dellin.mondoc.service.UserService;
import com.dellin.mondoc.utils.EncodingUtil;
import com.dellin.mondoc.utils.OrderUtil;
import com.dellin.mondoc.utils.PaginationUtil;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import retrofit2.Call;
import retrofit2.Response;

import java.util.*;
import java.util.stream.*;

/**
 * Service class to work with Orders
 *
 * @see Order
 * @see OrderRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	
	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	/**
	 * user service class
	 */
	private final UserService userService;
	/**
	 * Repository which contains orders
	 */
	private final OrderRepository orderRepository;
	/**
	 * Repository which contains companies
	 */
	private final CompanyRepository companyRepository;
	/**
	 * Repository which contains documents
	 */
	private final DocumentRepository documentRepository;
	/**
	 * Injection of Retrofit service requests
	 */
	private final SyncService syncService;
	/**
	 * Service thread is used for updating orders
	 */
	@Setter
	private Thread taskThread;
	/**
	 * Switcher of thread state
	 */
	@Setter
	private boolean initializedThread = false;
	
	/**
	 * Method that updates order database by connecting to Dellin API
	 * <p>
	 * Method connects to the Dellin API by sending a request that contains the following
	 * required parameters: pre-decrypted appkey, pre-decrypted sessionID and optional
	 * parameters in two ways.
	 * <pre>
	 * Required parameters:
	 *  <b>appkey</b> - the encrypted API key
	 *  <b>sessionID</b> - the encrypted unique session value previously received by the
	 *  user after authorization in the API through the method
	 *  {@link SessionServiceImpl#getLoginResponse(SessionDTO)}
	 * Optional parameters that based on target of request:
	 *  1. <b>docIds</b> - orders by their id - the {@link Collection}&lt;{@link String}&gt; of orders id
	 *  2. <b>dateStart</b>, <b>dateEnd</b> - orders by time interval (start date, end
	 *  date as String in format "yyyy-MM-dd")
	 * </pre>
	 * Orders in the response from the API are received in the form of a page -based
	 * object, so the method also accepts the number of a certain page for updating in the
	 * request.
	 * <p>
	 * Using multithreading, the method checks all available orders that require updating.
	 * The timeout between requests set up as the recommended Dellin interval of 10
	 * seconds. That`s why method checks the time of receipt of the response and send the
	 * request for the next order after the remaining time.
	 *
	 * <p>
	 * Updating data is possible only for authorized users, since any change is recorded
	 * in the history.
	 *
	 * @param orderRequest the {@link OrderRequest} object for sending to API
	 *
	 * @throws IOException by Retrofit method with synchronized {@link Call#execute()} if
	 *                     a problem occurred talking to the server
	 * @see #extracted(Thread, OrderRequest, User, int, int, OrderRequestBuilder, Date)
	 * @see #createAndUpdateOrders(Collection)
	 * @see #stopUpdate()
	 */
	@Override
	@Transactional
	public void update(OrderRequest orderRequest) throws IOException {
		
		log.info("Method [update() orders] started to work");
		Date programStart = new Date();
		
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userService.getUser(email);
		
		OrderRequestBuilder requestBuilder = OrderRequest.builder()
				.setAppKey(EncodingUtil.getDecrypted(user.getSession().getAppkey()))
				.setSessionID(
						EncodingUtil.getDecrypted(user.getSession().getSessionDl()));
		
		if (orderRequest.getDocIds() != null && !orderRequest.getDocIds().isEmpty()) {
			requestBuilder.setDocIds(orderRequest.getDocIds());
			log.info("User: [name: {}] set docIds to orderRequest", user.getUsername());
		} else {
			
			Date dateStart = OrderUtil.getParsedDate(orderRequest.getDateStart());
			Date dateEnd = OrderUtil.getParsedDate(orderRequest.getDateEnd());
			
			SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN);
			requestBuilder.setDateStart(formatter.format(dateStart))
					.setDateEnd(formatter.format(dateEnd));
			log.info("User: [name: {}] set startDate: [{}], endDate: [{}]",
					user.getUsername(), dateStart, dateEnd);
		}
		
		Runnable task = new Runnable() {
			
			int currentPage = 1;
			int totalPages = Integer.MAX_VALUE;
			
			@Override
			public void run() {
				Thread thread = Thread.currentThread();
				
				extracted(thread, orderRequest, user, currentPage, totalPages,
						requestBuilder, programStart);
			}
		};
		
		if (!initializedThread) {
			initializedThread = true;
			taskThread = new Thread(task);
			taskThread.start();
		}
	}
	
	/**
	 * Method that update current Orders by API order response values
	 * <p>
	 * Method iterates each order of received Response to fill in empty {@link Order}
	 * fields or update them and finally save to database
	 *
	 * @param orders the {@link Collection}&lt;{@link OrderResponse.Order}&gt;
	 *
	 * @see #update(OrderRequest)
	 * @see #extracted(Thread, OrderRequest, User, int, int, OrderRequestBuilder, Date)
	 * @see #stopUpdate()
	 */
	@Override
	public void createAndUpdateOrders(Collection<OrderResponse.Order> orders) {
		orders.forEach(o -> {
			Collection<OrderResponse.Order.Document> docs = o.getDocuments();
			docs.stream()
					.filter(d -> d.getType().equalsIgnoreCase("shipping"))
					.forEach(d -> {
						String orderId = d.getId();
						String orderUID = d.getUid();
						String innPayer = d.getPayer().getInn();
						
						Optional<Company> optionalCompany =
								companyRepository.findByInn(innPayer);
						Optional<Order> optionalOrder =
								orderRepository.findByDocId(orderId);
						
						//WORKING WITH COMPANY
						Company company;
						if (optionalCompany.isEmpty()) {
							//CREATE
							company = new Company();
							company.setStatus(EntityStatus.CREATED);
							company.setInn(innPayer);
							company.setName(d.getPayer().getName());
							log.info("Company's database updated. New "
											+ "company: [NAME: {}, INN: {}] added",
									company.getName(), company.getInn());
							//NOW WE HAVE COMPANY
						} else {
							//USE EX FROM DB
							company = optionalCompany.get();
							if (company.getName().length() == 0 || !company.getName()
																		   .equals(d.getPayer()
																					.getName())) {
								company.setName(d.getPayer().getName());
								company.setStatus(EntityStatus.UPDATED);
								company.setUpdatedAt(LocalDateTime.now());
							}
							log.info("Loaded company from DB: [NAME: {}, " + "INN: {}]",
									company.getName(), company.getInn());
							//NOW WE HAVE COMPANY
						}
						
						//WORKING WITH ORDER
						Order order;
						if (optionalOrder.isEmpty()) {
							order = new Order();
							order.setUid(orderUID);
							order.setStatus(EntityStatus.CREATED);
							order.setState(o.getState());
							order.setDocId(orderId);
							order.setCompany(company);
							Collection<Order> companyOrders = company.getOrders();
							companyOrders.add(order);
							company.setOrders(companyOrders);
							company.setStatus(EntityStatus.UPDATED);
							company.setUpdatedAt(LocalDateTime.now());
							log.info("Orders database updated. New "
											+ "order: [UID: {}, DOC_ID: {}] " + "added",
									order.getUid(), order.getDocId());
							//NOW WE HAVE ORDER
						} else {
							//USE EX FROM DB
							order = optionalOrder.get();
							//order checks:
							if (!o.getState().equals(order.getState())) {
								order.setState(o.getState());
								order.setStatus(EntityStatus.UPDATED);
								order.setUpdatedAt(LocalDateTime.now());
							}
							
							log.info("Loaded order from DB: [UID: {}, " + "DOC_ID: {}]",
									order.getUid(), order.getDocId());
							//NOW WE HAVE ORDER
						}
						
						//WORKING WITH ORDER DOCS
						Collection<String> avDocs = d.getAvailableDocs();
						
						//COLLECTION OF UPDATES
						avDocs.stream()
								.filter(doc -> documentRepository.findByUidAndType(
																		 orderUID, OrderDocType.valueOf(doc.toUpperCase()))
																 .isEmpty())
								.forEach(doc -> {
									Document document = new Document();
									document.setType(
											OrderDocType.valueOf(doc.toUpperCase()));
									document.setOrder(order);
									document.setUid(orderUID);
									document.setStatus(EntityStatus.CREATED);
									log.info("Document's database updated. "
													+ "New document: [TYPE:"
													+ " {}, UID: {}] added",
											document.getType().name(), document.getUid());
									
									order.getDocuments().add(document);
								});
						
						order.setStatus(EntityStatus.UPDATED);
						order.setUpdatedAt(LocalDateTime.now());
						companyRepository.save(company);
					});
		});
	}
	
	/**
	 * Method that find an {@link Order} in the database by docId
	 * <p>
	 * Returns the Order object if found or else a {@link CustomException} with http
	 * <b>404</b> status
	 *
	 * @param docId the order docId
	 *
	 * @return the {@link Order} object
	 */
	@Override
	public Order getOrder(String docId) {
		return orderRepository.findByDocId(docId).orElseThrow(() -> new CustomException(
				String.format("Order with ID: %s not found", docId),
				HttpStatus.NOT_FOUND));
	}
	
	/**
	 * Method that gets all available Orders in view of {@link OrderModel}
	 * <p>
	 * Returns a model map of all objects orders in a limited size list sorted by chosen
	 * parameter.
	 * <p>
	 * The final ModelMap depends on Users authority. User with basic authority can get
	 * orders: 1. only with earlier set up comments 2. only with companies for which they
	 * have rights
	 * <p>
	 * Working with data is possible only for authorized users, since any change is
	 * recorded in the history.
	 *
	 * @param page    the serial number of page
	 * @param perPage the number of elements on page
	 * @param sort    the main parameter of sorting (see available before
	 *                {@link OrderModel})
	 * @param order   ASC or DESC
	 *
	 * @return the ModelMap of sorted {@link OrderModel}
	 *
	 * @see ModelMap
	 * @see Pageable
	 * @see Page
	 */
	@Override
	public ModelMap getOrders(Integer page, Integer perPage, String sort,
			Sort.Direction order) {
		if (perPage == 0) {
			throw new CustomException("Page size must not be less than one",
					HttpStatus.BAD_REQUEST);
		}
		
		Pageable pageRequest = PaginationUtil.getPageRequest(page, perPage, sort, order);
		
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userService.getUser(email);
		Collection<Company> companies = user.getCompanies();
		boolean role_admin = user.getRoles()
				.stream()
				.map(Role::getRoleName)
				.collect(Collectors.toList()).contains("ROLE_ADMIN");
		
		Page<Order> pageResult = orderRepository.findByCompanyIn(companies, pageRequest);
		
		List<Order> collect;
		if (role_admin) {
			collect = pageResult.getContent();
		} else {
			collect = pageResult.getContent()
					.stream()
					.filter(o -> !o.getComments().isEmpty())
					.collect(Collectors.toList());
		}
		
		List<OrderModel> content = collect.stream()
				.map(o -> {
					OrderModel orderModel = new OrderModel();
					
					List<CommentHistory> history = o.getComments()
							.stream()
							.map(c -> {
								
								CommentHistory commentHistory = new CommentHistory();
								commentHistory.setUserName(c.getUser().getUsername());
								commentHistory.setUpdatedAt(c.getUpdatedAt());
								commentHistory.setText(c.getText());
								return commentHistory;
							})
							.collect(Collectors.toList());
					orderModel.setComments(history);
					
					orderModel.setCompanyName(o.getCompany().getName());
					orderModel.setState(o.getState());
					orderModel.setDocId(o.getDocId());
					orderModel.setUid(o.getUid());
					return orderModel;
				})
				.collect(Collectors.toList());
		
		ModelMap map = new ModelMap();
		map.addAttribute("content", content);
		map.addAttribute("pageNumber", page);
		map.addAttribute("PageSize", pageResult.getNumberOfElements());
		map.addAttribute("totalPages", pageResult.getTotalPages());
		
		return map;
	}
	
	/**
	 * Method that interrupt process of updating order database
	 * <p>
	 * Method interrupts earlier started thread of updating orders
	 *
	 * @see OrderServiceImpl#update(OrderRequest)
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
	 * Extracted method that continue logic of update method. Separated for better view
	 *
	 * @param thread         current {@link Thread} of updating orders
	 * @param orderRequest   the {@link OrderRequest} object for sending to API
	 * @param user           the user that send the request to API
	 * @param currentPage    the value of start position of iterating
	 * @param totalPages     the value of all available pages
	 * @param requestBuilder the API request builder before call
	 * @param programStart   the time when program started
	 *
	 * @see #update(OrderRequest)
	 * @see #createAndUpdateOrders(Collection)
	 * @see #stopUpdate()
	 */
	public void extracted(Thread thread, OrderRequest orderRequest, User user,
			int currentPage, int totalPages, OrderRequestBuilder requestBuilder,
			Date programStart) {
		Integer page = orderRequest.getPage();
		if (page != null) {
			log.info("User [EMAIL: {}] chose page [{}] for update", user.getUsername(),
					page);
			currentPage = page;
			totalPages = page;
		}
		
		log.info("Starting cycle of updating orders at [{}] page", currentPage);
		
		while (currentPage <= totalPages && !thread.isInterrupted()) {
			try {
				requestBuilder.setPage(currentPage);
					
					/*
					User can update database by two different ways:
					1. Including the list of interested in doc uid to the request
					2. Including dates from-to, setting the range by it
					These ways exclude each other from the request
					* */
				
				OrderRequest build = requestBuilder.build();
				
				Call<OrderResponse> orders = syncService.getRemoteData().update(build);
				
				Date start = new Date();
				log.info("Sending request to API");
				Response<OrderResponse> response = orders.execute();
				long responseTime = new Date().getTime() - start.getTime();
				log.info("Got the response in {} sec", responseTime / 1000.);
				
				if (response.body() == null) {
					throw new CustomException("Response body is empty",
							HttpStatus.BAD_REQUEST);
				}
				Collection<OrderResponse.Order> ord = response.body().getOrders();
				createAndUpdateOrders(ord);
				if (page == null) {
					totalPages = response.body().getMetadata().getTotalPages();
				}
				log.info("End of page: [{}]. Total pages: [{}]", currentPage,
						response.body().getMetadata().getTotalPages());
				currentPage++;
				long timeout = 10000L - responseTime;
				log.info("Timeout before next request {} sec", timeout / 1000.);
				Thread.sleep(timeout);
			} catch (InterruptedException | IOException e) {
				log.error(e.getMessage());
				thread.interrupt();
			}
		}
		
		Date programEnd = new Date();
		long ms = programEnd.getTime() - programStart.getTime();
		log.info("Method [update() orders] finished after {} seconds of working",
				(ms / 1000L));
	}
}
