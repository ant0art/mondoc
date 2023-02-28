package com.dellin.mondoc.model.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class OrderResponse {
	
	Metadata metadata;
	Collection<Order> orders;
	Collection<String> deleted;
	
	@Data
	public class Metadata {
		
		Integer status;
		Integer currentPage;
		Integer nextPage;
		Integer prevPage;
		Integer totalPages;
		String generatedAt;
	}
	
	@Data
	public class Order {
		
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
		public class DerivalArrival {
			
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
			class TerminalWorktables {
				
				String sun;
				String mon;
				String tue;
				String wed;
				String thu;
				String fri;
				String sat;
			}
			
			@Data
			class SpecialWorktable {
				
				Collection<String> receive;
				Collection<String> giveout;
			}
		}
		
		@Data
		public class Member {
			
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
		public class Freight {
			
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
		public class CargoPlace {
			
			String number;
			Integer amount;
		}
		
		@Data
		public class Air {
			
			String arrivalDate;
			String giveoutDate;
			String warehousingDate;
			String deliveryDate;
			String comment;
			String orderId;
		}
		
		@Data
		public class Lock {
			
			String name;
			String type;
			String setDate;
			String endDate;
		}
		
		@Data
		public class OrderDates {
			
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
		public class OrderTimeInDays {
			
			Integer delivery;
			Integer deliveryAccdoc;
		}
		
		@Data
		public class Sfrequest {
			
			Integer cityID;
			String docNumber;
			String price;
		}
		
		@Data
		public class Document {
			
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
			class Derival {
				
				String name;
				String code;
			}
			
			@Data
			class Arrival {
				
				String name;
				String code;
			}
			
			@Data
			class Service {
				
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
			class AcDoc {
				
				String documentDate;
				String documentNumber;
				String documentKind;
			}
		}
		
		@Data
		public class OrderDatesAdditional {
			
			Collection<Tracing> tracing;
			
			@Data
			class Tracing {
				
				String title;
				String date;
				String cityUID;
				String cityName;
				String status;
			}
		}
		
		@Data
		public class AcceptanceAct {
			
			String acceptanceActNumber;
			String acceptanceActDate;
			String acceptanceActType;
		}
	}
}


