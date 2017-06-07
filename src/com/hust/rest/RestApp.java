package com.hust.rest;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

public class RestApp extends ResourceConfig {
	public RestApp() {
		packages("com.hust.service");
		//ע��jsonת����
		register(JacksonJsonProvider.class);
	}
}
