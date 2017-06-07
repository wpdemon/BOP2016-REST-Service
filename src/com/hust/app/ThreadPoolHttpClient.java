package com.hust.app;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class ThreadPoolHttpClient {
	// 线程池
	private ExecutorService es = null;
	private CloseableHttpClient httpClient = null;
	private URI[] uris = null;

	public ThreadPoolHttpClient(URI[] uris, int poolSize) {
		// 采用固定池
		es = Executors.newFixedThreadPool(poolSize);
		this.uris = uris;
	}

	public ThreadPoolHttpClient(URI[] uris) {
		//默认为uri数量大小的线程池
		es = Executors.newFixedThreadPool(uris.length);
		this.uris = uris;
	}
	
	public ThreadPoolHttpClient(List<URI> uris, int poolSize) {
		// 采用固定池
		es = Executors.newFixedThreadPool(poolSize);
		this.uris=new URI[uris.size()];
		for (int i = 0; i < uris.size(); i++) {
			this.uris[i]=uris.get(i);
		}
	}
	
	public ThreadPoolHttpClient(List<URI> uris) {
		es = Executors.newFixedThreadPool(uris.size());
		this.uris=new URI[uris.size()];
		for (int i = 0; i < uris.size(); i++) {
			this.uris[i]=uris.get(i);
		}
	}

	public String[] run() {// 此处参数需要调整
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		// 设置最大连接数为10,默认值为20
		cm.setMaxTotal(50);
		// 设置每个路由的基础连接为10,默认值为2
		cm.setDefaultMaxPerRoute(5);
		httpClient = HttpClients.custom().setConnectionManager(cm).build();
		// 超时设置
		RequestConfig config = RequestConfig.custom().setConnectTimeout(10000)
				.setConnectionRequestTimeout(10000).setSocketTimeout(10000) // 设置连接超时为10s,从连接池获取连接超时时间为10s,从服务器获取响应数据超时时间为10s
				.build();
		// 保存结果
		List<Future<String>> futures = new ArrayList<Future<String>>();
		String[] entities=new String[uris.length];
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < uris.length; i++) {
			HttpGet httpGet = new HttpGet(uris[i]);
			httpGet.setConfig(config);
			futures.add(es.submit(new GetCall(i,httpClient, httpGet)));
		}
		
		// 获取执行结果
		for (Future<String> future : futures) {
			try {
				String result=future.get();
				int index=Integer.parseInt(result.substring(0, result.indexOf(" ")));
				String info=result.substring(result.indexOf(" ")+1);
				entities[index]=info;
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}				
		es.shutdown();
		// 性能测试
		while (true) {
			if (es.isTerminated()) {
				long endTime = System.currentTimeMillis();
				System.out.println("runining time is " + (endTime - startTime)
						/ 1000f + "s");
				break;
			}
		}
		return entities;

	}

	static class GetThread extends Thread {

		private final CloseableHttpClient httpClient;
		private final HttpContext context;
		private final HttpGet httpGet;

		public GetThread(CloseableHttpClient httpClient, HttpGet httpGet) {
			// TODO Auto-generated constructor stub
			this.httpClient = httpClient;
			this.context = HttpClientContext.create();
			this.httpGet = httpGet;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				CloseableHttpResponse response = httpClient.execute(httpGet,
						context);
				try {
					HttpEntity entity = response.getEntity();
					System.out.println(EntityUtils.toString(entity));
				} finally {
					response.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	static class GetCall implements Callable<String> {

		private final CloseableHttpClient httpClient;
		private final HttpContext context;
		private final HttpGet httpGet;
		private final int taskIndex;

		public GetCall(int taskIndex,CloseableHttpClient httpClient, HttpGet httpGet) {
			// TODO Auto-generated constructor stub
			this.httpClient = httpClient;
			this.context = HttpClientContext.create();
			this.httpGet = httpGet;
			this.taskIndex=taskIndex;
		}

		@Override
		public String call() throws Exception {
			// TODO Auto-generated method stub
			try {
				CloseableHttpResponse response = httpClient.execute(httpGet,
						context);
				try {
					HttpEntity entity = response.getEntity();
					return taskIndex+" "+EntityUtils.toString(entity);
				//	return EntityUtils.toString(entity);
				} finally {
					response.close();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

}
