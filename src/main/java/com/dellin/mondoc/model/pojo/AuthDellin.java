package com.dellin.mondoc.model.pojo;

import lombok.Getter;
import lombok.Setter;

/**
 * A request/response class that allows performing the following actions:
 * <pre>
 * - authorizing user (to get session ID);
 * - closing the current session </pre>
 */
@Getter
@Setter
public class AuthDellin {
	
	/**
	 * The class-field of system information
	 */
	private Metadata metadata;
	/**
	 * The class-field of session data
	 */
	private Data data;
	
	/**
	 * System information
	 */
	@Getter
	@Setter
	public static class Metadata {
		
		/**
		 * Emulated status http-code. If successful, code 200 is returned
		 */
		private Integer status;
		/**
		 * Server response date and time. Format: YYYY-MM-DD HH:MM:SS
		 */
		private String generated_at;
	}
	
	/**
	 * Session data
	 */
	@Getter
	@Setter
	public static class Data {
		
		/**
		 * Request status. Possible value - "success"
		 */
		private String state;
		/**
		 * Session ID for getting access to Personal account data
		 */
		private String sessionID;
	}
}


