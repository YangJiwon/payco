package com.was.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.was.model.Server;
import com.was.model.Settings;

public class LoadConfig {
	private static final Logger logger = LoggerFactory.getLogger(LoadConfig.class);

	public Settings load(){
		Config config = ConfigFactory.load("settings.json");
		Map<String, Server> serverMap = new HashMap<>();

		for(Config con : config.getConfigList("server_list")){
			Server server = Server.builder()
					.domain(con.getString("domain"))
					.name(con.getString("name"))
					.root(con.getString("root"))
					.index(con.getString("index"))
					.forbidden(con.getString("403"))
					.notFound(con.getString("404"))
					.serverError(con.getString("500"))
					.build();
			serverMap.put(con.getString("domain"), server);
		}

		Map<String, String> mappingMap = getMappingMap();
		return Settings.builder()
				.port(config.getInt("port"))
				.restrictedExtension(config.getString("restricted_extension"))
				.serverMap(serverMap)
				.mappingMap(mappingMap)
				.build();
	}

	public Map<String, String> getMappingMap(){
		Map<String, String> mappingMap = new HashMap<>();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mapping.json");
		JSONParser jsonParser = new JSONParser();

		try {
			JSONObject jsonObject = (JSONObject)jsonParser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			for(Object key : jsonObject.keySet()){
				mappingMap.put(key.toString(), jsonObject.get(key).toString());
			}
		} catch (IOException | ParseException e) {
			logger.error(e.getMessage(), e);
		}
		return mappingMap;
	}
}
