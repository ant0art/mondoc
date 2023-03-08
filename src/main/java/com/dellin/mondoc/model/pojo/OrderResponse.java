package com.dellin.mondoc.model.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * The response class received as answer by API Dellin
 *
 * @see <a
 * href=https://dev.dellin.ru/api/en_orders/orders-search/>https://dev.dellin.ru/api/</a>
 */
@Getter
@Setter
public class OrderResponse {
	
	Metadata metadata;
	Collection<Order> orders;
	Collection<String> deleted;
	
	@Data
	public static class Metadata {
		
		Integer status;
		Integer currentPage;
		Integer nextPage;
		Integer prevPage;
		Integer totalPages;
		String generatedAt;
	}
	
	@Data
	public static class Order {
		
		String orderNumber;
		String orderDate;
		String orderId;
		String orderedAt;
		String state;
		String stateName;
		String stateDate;
		Integer progressPercent;
		DerivalArrival derival;
		DerivalArrival arrival;
		Member sender;
		Member receiver;
		Member payer;
		Freight freight;
		Collection<CargoPlace> cargoPlaces;
		Boolean isAir;
		Air air;
		Collection<Lock> locks;
		Float webOrderItemsSum;
		Boolean withWebOrder;
		String totalSum;
		String vat;
		Boolean isPaid;
		Boolean isPreorder;
		String produceDate;
		String declineReason;
		OrderDates orderDates;
		OrderTimeInDays orderTimeInDays;
		Boolean orderedDeliveryFromAddress;
		Boolean availableDeliveryFromAddress;
		Boolean orderedDeliveryToAddress;
		Boolean availableDeliveryToAddress;
		Boolean isFavorite;
		Boolean isContainer;
		Sfrequest sfrequest;
		Collection<Document> documents;
		OrderDatesAdditional orderDatesAdditional;
		String detailedStatus;
		String detailedStatusRus;
		String note;
		String documentsReturnDate;
		String priceComment;
		String customerUid;
		Collection<AcceptanceAct> acceptanceActs;
		String shipmentLabelCargoPlace;
		
		@Data
		public static class DerivalArrival {
			
			String city;
			Integer cityId;
			String cityCode;
			String address;
			String addressCode;
			String terminalName;
			String terminalAddress;
			Integer terminalId;
			String terminalCity;
			Collection<Float> terminalCoordinates;
			String terminalEmail;
			String terminalPhones;
			String callCenterPhones;
			TerminalWorktables terminalWorktables;
			SpecialWorktable specialWorktable;
			
			@Data
			static class TerminalWorktables {
				
				String sun;
				String mon;
				String tue;
				String wed;
				String thu;
				String fri;
				String sat;
			}
			
			@Data
			static class SpecialWorktable {
				
				Collection<String> receive;
				Collection<String> giveout;
			}
		}
		
		@Data
		public static class Member {
			
			//	String opf;
			//	Opf opf;
			String opfUid;
			String name;
			String address;
			Boolean isPhysical;
			String inn;
			String documentType;
			String documentSeries;
			String documentNumber;
			String counteragentUid;
			String contacts;
			String phones;
			Boolean anonym;
			String anonymEmail;
			String anonymPhone;
			
			/*@Data
			class Opf {
				
				String name;
				String fullName;
				Integer countryCode;
			}*/
		}
		
		@Data
		public static class Freight {
			
			String name;
			String weight;
			String oversizedWeight;
			String volume;
			String oversizedVolume;
			Integer places;
			Integer oversizedPlaces;
			String length;
			String width;
			String height;
		}
		
		@Data
		public static class CargoPlace {
			
			String number;
			Integer amount;
		}
		
		@Data
		public static class Air {
			
			String arrivalDate;
			String giveoutDate;
			String warehousingDate;
			String deliveryDate;
			String comment;
			String orderId;
		}
		
		@Data
		public static class Lock {
			
			String name;
			String type;
			String setDate;
			String endDate;
		}
		
		@Data
		public static class OrderDates {
			
			String arrivalToOspReceiver;
			String arrivalToOspReceiverMax;
			String arrivalToOspReceiverAccdoc;
			String arrivalToOspSender;
			String arrivalToReceiver;
			String declineDate;
			String derivalFromOspReceiver;
			String derivalFromOspReceiverMax;
			String derivalFromOspReceiverAccdoc;
			String giveoutFromOspReceiver;
			String giveoutFromOspReceiverMax;
			String derivalFromOspSender;
			String draftLastUpdate;
			String finish;
			String firstDocumentCreatedDate;
			String pickup;
			String processingDate;
			String warehousing;
		}
		
		@Data
		public static class OrderTimeInDays {
			
			Integer delivery;
			Integer deliveryAccdoc;
		}
		
		@Data
		public static class Sfrequest {
			
			Integer cityID;
			String docNumber;
			String price;
		}
		
		@Data
		public static class Document {
			
			String id;
			String uid;
			String type;
			String createDate;
			String state;
			Member sender;
			Member receiver;
			Member payer;
			String produceDate;
			String forwarderId;
			String comment;
			String fullDocumentId;
			Freight freight;
			Derival derival;
			Arrival arrival;
			String barcode;
			Boolean payment;
			String totalSum;
			String vat;
			String serviceKind;
			String organization;
			Collection<Service> services;
			Collection<AcDoc> accompanyingDocuments;
			Collection<String> availableDocs;
			
			@Data
			static class Derival {
				
				String name;
				String code;
			}
			
			@Data
			static class Arrival {
				
				String name;
				String code;
			}
			
			@Data
			static class Service {
				
				String name;
				String serviceUid;
				String createdAt;
				Integer quantity;
				String sum;
				String totalSum;
				String vat;
				String vatRate;
				String discountSum;
			}
			
			@Data
			static class AcDoc {
				
				String documentDate;
				String documentNumber;
				String documentKind;
			}
		}
		
		@Data
		public static class OrderDatesAdditional {
			
			Collection<Tracing> tracing;
			
			@Data
			static class Tracing {
				
				String title;
				String date;
				String cityUID;
				String cityName;
				String status;
			}
		}
		
		@Data
		public static class AcceptanceAct {
			
			String acceptanceActNumber;
			String acceptanceActDate;
			String acceptanceActType;
		}
	}
}


