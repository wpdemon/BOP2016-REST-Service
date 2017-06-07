package com.hust.service;

import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.alibaba.fastjson.JSONArray;
import com.hust.app.PathParse;

@Path("/rest")
public class RestService {

	@GET
	@Path("test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test(){
		return "Hello World!";
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JSONArray getAllPath(@QueryParam("id1") long id1,@QueryParam("id2") long id2) throws URISyntaxException{
		PathParse pathParse=new PathParse();
		long startTime = System.currentTimeMillis();
		JSONArray result=JSONArray.parseArray(pathParse.getAllPath(id1, id2));
		long endTime = System.currentTimeMillis();
		System.out.println("runining time is " + (endTime - startTime) / 1000f
				+ "s");
		return result;	
	}
}
