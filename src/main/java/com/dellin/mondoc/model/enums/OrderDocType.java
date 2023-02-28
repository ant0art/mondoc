package com.dellin.mondoc.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderDocType {
	
	BILL("Счёт"),
	ORDER("Накладная"),
	INVOICE("Счёт-фактура"),
	GIVEOUT("Накладная на выдачу"),
	SHIPPING("Накладная");
	
	private final String description;
}
