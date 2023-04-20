package com.was.model;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Settings {
	private int port;
	private String restrictedExtension;
	private Map<String, Server> serverMap;
	private Map<String, String> mappingMap;
}
