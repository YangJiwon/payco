package com.was.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class Server {
	private String domain;
	private String name;
	private String root;
	private String index;
	private String notFound;

	private String forbidden;
	private String serverError;
}
