package com.was.common;

import java.time.format.DateTimeFormatter;

public class CommonConstants {
	public static final String SLASH = "/";
	public static final String POINT = ".";
	public static final String GAP = " ";
	public static final String EQUAL = "=";
	public static final String COLON = ":";

	public static final String POINT_DELIMITER = "\\.";
	public static final String AND_DELIMITER = "\\&";
	public static final String QUESTION_MARK_DELIMITER  = "\\?";
	public static final String OK = "200 OK";
	public static final String NOT_FOUND = "404 Not Found";

	public static final String FORBIDDEN = "403 Forbidden";
	public static final String INTERNAL_SERVER_ERROR = "500 Internal Server Error";
	public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
	public static final String POST = "POST";
}
