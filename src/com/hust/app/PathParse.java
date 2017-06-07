package com.hust.app;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.http.client.utils.URIBuilder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@XmlRootElement
public class PathParse {

	/*
	 * // hop-1的可达矩阵 private final static int[][] pathNum1 = { { 1, 1, 1, 1, 1,
	 * 0 }, { 1, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0 }, {
	 * 1, 0, 0, 0, 0, 1 }, { 0, 0, 0, 0, 1, 0 } };
	 * 
	 * // hop-2的可达矩阵 private final static int[][] pathNum2 = { { 5, 1, 1, 1, 1,
	 * 1 }, { 1, 1, 1, 1, 1, 0 }, { 1, 1, 1, 1, 1, 0 }, { 1, 1, 1, 1, 1, 0 }, {
	 * 1, 1, 1, 1, 2, 0 }, { 1, 0, 0, 0, 0, 1 } }; // hop-3的可达矩阵 private final
	 * static int[][] pathNum3 = { { 9, 5, 5, 5, 6, 1 }, { 5, 1, 1, 1, 1, 1 }, {
	 * 5, 1, 1, 1, 1, 1 }, { 5, 1, 1, 1, 1, 1 }, { 6, 1, 1, 1, 1, 2 }, { 1, 1,
	 * 1, 1, 2, 0 } };
	 */
	// 记录 1-hop可能的路径
	private String[] path1 = { "00", "04", "40" };
	// 记录 2-hop可能的路径
	private String[][] path2 = { { "000", "010", "020", "030", "040" },
			{ "004" }, { "400" }, { "404", "454" } };
	// 记录 3-hop可能的路径
	private String[][] path3 = {
			{ "0000", "0010", "0020", "0030", "0040", "0100", "0200", "0300",
					"0400" },
			{ "0004", "0104", "0204", "0304", "0404", "0454" },
			{ "4000", "4010", "4020", "4030", "4040", "4540" }, { "4004" } };

	private final static String[] ATTR_NAME = { "RId", "F.FId", "J.JId",
			"C.CId", "AA.AuId", "AA.AfId" };

	// evaluate api
	private URIBuilder eval;
	// histogram api
	private URIBuilder hist;
	// 保存路径信息
	private List<JSONArray> path = new ArrayList<JSONArray>();

	public PathParse() throws URISyntaxException {
		eval = new URIBuilder(
				"http://oxfordhk.azure-api.net/academic/v1.0/evaluate");
		hist = new URIBuilder(
				"http://oxfordhk.azure-api.net/academic/v1.0/calchistogram");
	}

	/**
	 * eval传参
	 * 
	 * @param builder
	 * @param expr
	 * @param attr
	 * @param count
	 * @param offset
	 * @param order
	 * @return
	 * @throws URISyntaxException
	 */
	public URI setRequestParams(URIBuilder builder, String expr, String attr,
			int count, int offset, String order) throws URISyntaxException {
		builder.setParameter("expr", expr);
		builder.setParameter("attributes", attr);
		builder.setParameter("count", Integer.toString(count));
		builder.setParameter("offset", Integer.toString(offset));
		// 返回结果的顺序，该参数重要可以提高搜索效率
		builder.setParameter("orderby", order);
		// 传参授权key
		builder.setParameter("subscription-key",
				"f7cc29509a8443c5b3a5e56b0e38b5a6");
		return builder.build();
	}

	/**
	 * hist传参
	 * 
	 * @param builder
	 * @param expr
	 * @param attr
	 * @param count
	 * @param offset
	 * @return
	 * @throws URISyntaxException
	 */
	public URI setRequestParams(URIBuilder builder, String expr, String attr,
			int count, int offset) throws URISyntaxException {
		builder.setParameter("expr", expr);
		builder.setParameter("attributes", attr);
		builder.setParameter("count", Integer.toString(count));
		// 传参授权key
		builder.setParameter("subscription-key",
				"f7cc29509a8443c5b3a5e56b0e38b5a6");
		return builder.build();
	}
	
	/**
	 * 根据起点、终点信息获取路径下标
	 * 
	 * @param startInfo
	 * @param endInfo
	 * @return 路径下标
	 */
	public int getPathIndex(String startInfo, String endInfo) {
		int pathIndex = 0;
		JSONArray sArray=JSONObject.parseObject(startInfo)
				.getJSONArray("entities");
		JSONArray eArray=JSONObject.parseObject(endInfo)
				.getJSONArray("entities");
		if (sArray.size()==0||eArray.size()==0) {//判断Id是否存在
			return -1;
		}
		JSONObject sInfo = sArray.getJSONObject(0);
		JSONObject eInfo = eArray.getJSONObject(0);		
		// 默认均为AuId
		int sType = 4;
		int eType = 4;
		// 若为Id,则RId、AA.AuId、F.FId、J.JId、C.CId至少有一项不为空
		for (int i = 0; i < 5; i++) {
			List<String> start = SetUtils.getPropertySet(sInfo, i);
			List<String> end = SetUtils.getPropertySet(eInfo, i);
			if (start.size() != 0) {
				sType = 0;
			}
			if (end.size() != 0) {
				eType = 0;
			}
		}

		// 映射路径下标
		if (sType == 0 && eType == 4) {
			pathIndex = 1;
		}
		if (sType == 4 && eType == 0) {
			pathIndex = 2;
		}
		if (sType == 4 && eType == 4) {
			pathIndex = 3;
		}

		return pathIndex;
	}

	/**
	 * 获取所有1-3 hop路径
	 * 
	 * @param startID
	 *            起点ID
	 * @param endID
	 *            终点ID
	 * @return 路径列表
	 * @throws URISyntaxException
	 */
	public String getAllPath(long startID, long endID)
			throws URISyntaxException {
		// 保存实体信息
		List<String> entityInfo = new ArrayList<String>();
		// 保存请求的URIs
		List<URI> uris = new ArrayList<URI>();
		ThreadPoolHttpClient httpClient;
		// 各属性对应的固定属性
		String[] attrs = { "RId,AA.AuId,F.FId,J.JId,C.CId,AA.AfId", "Id", "Id",
				"Id", "Id,RId,AA.AfId", "AA.AuId" };
		int[][] attrIndexes = { { 0, 1, 2, 3, 4 }, { 0, 5, 6 } };
		// 查询数量固定
		int count = 10000;
		// 固定顺序
		String order = "Id:asc";

		// 获取起点与终点信息,判断节点类型 ,依次为起点为Id、AA.AuId;终点为Id、AA.AuId
		String expr1 = "Id=" + startID;
		String expr2 = ExprUtils.composite("AA.AuId", startID);
		String expr3 = "Id=" + endID;
		String expr4 = ExprUtils.composite("AA.AuId", endID);
		String[] initExprs = { expr1, expr2, expr3, expr4 };
		// 获取实体信息及统计信息
		for (int i = 0; i < initExprs.length; i++) {
			URI uri;
			if (i % 2 == 0) {// Id类型
				uri = setRequestParams(eval, initExprs[i], attrs[0], 1, 0,
						order);
			} else {// AuId类型
				uri = setRequestParams(hist, initExprs[i], attrs[4], count, 0);
			}
			uris.add(uri);
		}
		// 1-hop请求
		for (int i = 0; i < path1.length; i++) {
			String expr = ExprUtils.getAdjacentExpr(path1[i].charAt(0),
					path1[i].charAt(1), startID, endID);
			URI uri = setRequestParams(hist, expr, "Id", 1, 0);
			uris.add(uri);
		}

		System.out.print("1-hop ");// 目前1-hop为主要瓶颈 0.747s
		httpClient = new ThreadPoolHttpClient(uris);
		String[] results = httpClient.run();
		// 获取结果
		for (int i = 0; i < initExprs.length; i++) {
			entityInfo.add(results[i]);
		}
		for (int i = initExprs.length; i < results.length; i++) {
			JSONObject json = JSONObject.parseObject(results[i]);
			if (json.getLong("num_entities") != 0) {
				String string = "[" + startID + "," + endID + "]";
				path.add(JSONArray.parseArray(string));
				break;
			}
		}
		uris.clear();		
		
		// 判断起点、终点ID类型,由于Id与AuId不重复,优先判断是否为AuId
		int pathIndex = getPathIndex(entityInfo.get(0), entityInfo.get(2));
		if (pathIndex==-1) {//至少有一个实体Id不存在
			System.out.println(path.size());
			return JSON.toJSONString(path);
		}
		System.out.println("路径类型为:" + pathIndex);
		// 将路径类型映射到起点、终点类型
		int sIndex = path2[pathIndex][0].charAt(0) - 48;
		int eIndex = path2[pathIndex][0].charAt(2) - 48;
		JSONObject startInfo = JSONObject.parseObject(entityInfo
				.get(sIndex / 4));
		JSONObject endInfo = JSONObject.parseObject(entityInfo
				.get(eIndex / 4 + 2));

		// System.out.println(startInfo);
		// System.out.println(endInfo);
		// 根据起点、终点类型进行相应的备忘机制
		List<List<String>> sLists = new ArrayList<List<String>>();
		List<List<String>> eLists = new ArrayList<List<String>>();
		for (int i = 0; i < attrIndexes[sIndex / 4].length; i++) {
			if (sIndex / 4 == 0) {// eval结果
				sLists.add(SetUtils.getPropertySet(
						startInfo.getJSONArray("entities").getJSONObject(0),
						attrIndexes[0][i]));
			} else {// hist结果
				if (attrIndexes[1][i] == 5) {
					for (int j = 0; j < 4; j++) {
						sLists.add(null);
					}
				}
				sLists.add(SetUtils.getAdjacentSet(startInfo, sIndex,
						attrIndexes[1][i]));
			}
		}
		for (int i = 0; i < attrIndexes[eIndex / 4].length; i++) {
			if (eIndex / 4 == 0) {// eval结果
				eLists.add(SetUtils.getPropertySet(
						endInfo.getJSONArray("entities").getJSONObject(0),
						attrIndexes[0][i]));
			} else {// hist结果
				if (attrIndexes[1][i] == 5) {
					for (int j = 0; j < 4; j++) {
						eLists.add(null);
					}
				}
				eLists.add(SetUtils.getAdjacentSet(endInfo, eIndex,
						attrIndexes[1][i]));
			}
		}

		// 集合求交法较请求法,需要自己处理数据
		// 检查2-hop
		System.out.print("2-hop ");
		// 起点邻接集
		List<String> adjacentID;
		for (int j = 0; j < path2[pathIndex].length; j++) {
			// 中间节点类型
			int mid = path2[pathIndex][j].charAt(1) - 48;
			adjacentID = sLists.get(mid);
			if (mid == 0 && eIndex == 0) {// [Id,Id]不能通过集合求交来计算,单独处理
											// //可以通过1次request解决
				// 方案一：通过连接查询表达式减少URI,耗时合理,但要考虑请求长度问题
				// 将过长的URI进行分块,块的大小最好根据线程池的大小划分
				int chunkSize = 30;
				int chunkNum = adjacentID.size() / chunkSize + 1;
				for (int k = 0; k < chunkNum; k++) {
					String request = null;
					for (int l = 0; l < chunkSize; l++) {
						if (k * chunkSize + l == adjacentID.size()) {// 是否越界
							break;
						}
						String value = adjacentID.get(k * chunkSize + l);
						String expr = ExprUtils.getAdjacentExpr(mid, eIndex,
								Long.parseLong(value), endID);
						request = ExprUtils.or(request, expr);
					}
					if (request != null) {
						URI uri = setRequestParams(hist, request, "Id",
								chunkSize, 0);
						uris.add(uri);
					}
				}
				
				// 方案二：通过RId=endID请求
				/*String request="RId="+endID;
				URI uri = setRequestParams(hist,request,"Id",count,0);
				uris.add(uri);*/
				
				// 处理URI
				if (uris.size() > 0) {// 保证URI不为空					
					httpClient = new ThreadPoolHttpClient(uris);
					results = httpClient.run();
					uris.clear();
					// 方案一处理
					for (int k = 0; k < results.length; k++) {
						// System.out.println(results[k]);
						List<String> entities = SetUtils.getAdjacentSet(
								JSONObject.parseObject(results[k]), 1, 0);
						if (entities.size() != 0) {
							for (String entity : entities) {
								String string = "[" + startID + "," + entity
										+ "," + endID + "]";
								path.add(JSONArray.parseArray(string));
							}
						}
					}				
					//方案二处理
				/*	List<String> rids=sLists.get(mid);
					List<String> rids1 = SetUtils.getAdjacentSet(
							JSONObject.parseObject(results[0]), 1, 0);
					rids1.retainAll(rids);
					for (String value : rids1) {
						String string = "[" + startID + "," + value
								+ "," + endID + "]";
						path.add(JSONArray.parseArray(string));
					}*/
					
				}

			} else {// 集合的Id是有序的,此处可以进一步优化
				// 终点相邻集
				List<String> adjacentID1;
				// 进一步处理机构问题,速度慢,但召回率高
				if (mid == 5) {
					uris.add(setRequestParams(eval, expr2,
							"Id,AA.AuId,AA.AfId", count, 0, order));// 与后面产生重复请求
					uris.add(setRequestParams(eval, expr4,
							"Id,AA.AuId,AA.AfId", count, 0, order));
					httpClient = new ThreadPoolHttpClient(uris);
					results = httpClient.run();
					adjacentID = SetUtils.getAdjacentSet(
							JSONObject.parseObject(results[0]), sIndex, mid,
							startID);
					adjacentID1 = SetUtils.getAdjacentSet(
							JSONObject.parseObject(results[1]), eIndex, mid,
							endID);
					adjacentID.retainAll(adjacentID1);
					for (String id : adjacentID) {
						String string = "[" + startID + "," + id + "," + endID
								+ "]";
						path.add(JSONArray.parseArray(string));
					}
					uris.clear();
				}
				/*
				 * // 处理机构问题,速度快但召回率低 if (mid == 5) {//
				 * 由于结果按概率降序排列,只需要取前10条结果进行验证即可,处理时间略长可以考虑本地处理
				 * adjacentID1=eLists.get(mid); // 集合求交
				 * adjacentID.retainAll(adjacentID1); for (int k = 0; k < 10 &&
				 * k < adjacentID.size(); k++) { long value =
				 * Long.parseLong(adjacentID.get(k)); String expr =
				 * ExprUtils.getAdjacentExpr(eIndex, mid, endID, value); URI uri
				 * = setRequestParams(eval, expr, "Id", 1, 0, order);
				 * uris.add(uri); } if (uris.size() > 0) { httpClient = new
				 * ThreadPoolHttpClient(uris); results = httpClient.run(); for
				 * (int k = 0; k < results.length; k++) { JSONObject json =
				 * JSONObject .parseObject(results[k]); JSONArray entities =
				 * json.getJSONArray("entities"); if (entities.size() != 0) {
				 * String ID = adjacentID.get(k); String string = "[" + startID
				 * + "," + ID + "," + endID + "]";
				 * path.add(JSONArray.parseArray(string)); } } uris.clear(); } }
				 */
				else {
					adjacentID1 = eLists.get(mid);
					// System.out.println(adjacentID.toString());
					// System.out.println(adjacentID1.toString());
					// 集合求交
					adjacentID.retainAll(adjacentID1);
					for (String ID : adjacentID) {
						String string = "[" + startID + "," + ID + "," + endID
								+ "]";
						path.add(JSONArray.parseArray(string));
					}
				}
			}
		}

		System.out.print("3-hop ");
		// 检查3-hop
		List<Integer> pathID = new ArrayList<Integer>();// 记录产生结果的路径编号
		// 对于[id,id]从后往前找到第一个非连续0下标,较长的部分进行顺序扩展,结果进行求交;对于0000只能进行单独处理,其结果可进行memo
		if (pathIndex == 0) {
			// 针对0000进行统计信息请求
			URI histURI = null;
			// 获取各路径产生的URI
			for (int i = 0; i < path3[pathIndex].length; i++) {
				String request = null;
				if (i == 0) {// 单独处理0000
					for (String id : sLists.get(i)) {// 若参考文献过多可能还要进行分块
						String expr = "Id=" + id;
						request = ExprUtils.or(request, expr);
					}
					if (request != null) {// 处理0000时计算统计信息便于求交
						URI uri = setRequestParams(eval, request, "Id,"
								+ ATTR_NAME[i], sLists.get(i).size(), 0, order);
						histURI = setRequestParams(hist, request, ATTR_NAME[i],
								sLists.get(i).size(), 0);
						pathID.add(i);
						uris.add(uri);
						System.out.println(path3[pathIndex][i]);
					}
				} else {
					// 从后往前找到第二个0的位置,划分路径为两部分,对较长的部分进行扩展
					int zeroIndex = path3[pathIndex][i].lastIndexOf("0",
							path3[pathIndex][i].length() - 2);
					int attrIndex;
					int reqNum = count;// 返回结果数
					if (zeroIndex == 1) {
						attrIndex = path3[pathIndex][i].charAt(zeroIndex + 1) - 48;
						for (String id : sLists.get(0)) {// 若参考文献过多可能还要进行分块
							String expr = "Id=" + id;
							request = ExprUtils.or(request, expr);
						}
						reqNum = sLists.get(0).size();

					} else {// zeroIndex=2
						attrIndex = path3[pathIndex][i].charAt(zeroIndex - 1) - 48;
						for (String id : sLists.get(attrIndex)) {// 复合属性对应的值一般较少故不用分块
							String expr = ExprUtils.and("RId", endID, ExprUtils
									.composite(ATTR_NAME[attrIndex],
											Long.parseLong(id)));
							request = ExprUtils.or(request, expr);
						}
					}
					if (request != null) {// 请求不为空,count是否可以根据统计信息来确定,减少count数可以加快响应速度,reqNum自适应
						URI uri = setRequestParams(eval, request, "Id,"
								+ ATTR_NAME[attrIndex], reqNum, 0,
								ATTR_NAME[attrIndex] + ":asc");
						pathID.add(i);
						uris.add(uri);
						System.out.println(path3[pathIndex][i]);
					}
				}
			}

			if (histURI != null) {// 对于0000增加统计信息请求,放在最末尾
				uris.add(histURI);
			}

			// 提交请求并处理结果,count对于请求的响应时间影响很大,需要对count进行合理的设置
			if (uris.size() > 0) {
				httpClient = new ThreadPoolHttpClient(uris);
				results = httpClient.run();
				uris.clear();
				System.out.println("results num:" + results.length);
				List<String> rids;// 邻近id的集合
				for (int i = 0; i < pathID.size(); i++) {
					int pid = pathID.get(i);
					// 包含0000请求,则需要进行进一步请求
					if (pid == 0) {
						// 获取RId集合
						rids = SetUtils.getAdjacentSet(
								JSONObject.parseObject(results[pathID.size()]),
								0, 0);
						// 对rids分块进行请求
						int chunkSize = 30;// 块大小
						int chunkNum = rids.size() / chunkSize + 1;// 块数
						for (int k = 0; k < chunkNum; k++) {
							String request = null;
							for (int l = 0; l < chunkSize; l++) {
								if (k * chunkSize + l == rids.size()) {// 越界检查
									break;
								}
								String expr = ExprUtils.getAdjacentExpr(
										0,
										0,
										Long.parseLong(rids.get(k * chunkSize
												+ l)), endID);
								request = ExprUtils.or(request, expr);
							}
							if (request != null) {
								URI uri = setRequestParams(hist, request, "Id",
										chunkSize, 0);
								uris.add(uri);
							}
						}
						// 0000 路径测试
						if (uris.size() > 0) {
							System.out.print("0000 test ");
							httpClient = new ThreadPoolHttpClient(uris);
							String[] reqResults = httpClient.run();
							uris.clear();
							System.out.println("result num:"
									+ reqResults.length);
							// 合并结果
							rids.clear();
							for (String string : reqResults) {
								rids.addAll(SetUtils.getAdjacentSet(
										JSONObject.parseObject(string), 1, 0));
							}
							// 保存第二个节点的Rid
							List<String> rids1;
							JSONArray entities = JSONObject.parseObject(
									results[i]).getJSONArray("entities");
							for (Object object : entities) {
								JSONObject entity = JSONObject
										.parseObject(object.toString());
								rids1 = SetUtils.getPropertySet(entity, 0);
								rids1.retainAll(rids);
								for (String id : rids1) {
									String string = "[" + startID + ","
											+ entity.getString("Id") + "," + id
											+ "," + endID + "]";
									path.add(JSONArray.parseArray(string));
								}
							}
						}

					} else {// 处理剩余情况
						int zeroIndex = path3[pathIndex][pid].lastIndexOf("0",
								path3[pathIndex][pid].length() - 2);
						int attrIndex;
						List<String> propertyValues;
						JSONArray entities = JSONObject.parseObject(results[i])
								.getJSONArray("entities");
						if (zeroIndex == 1) {
							attrIndex = path3[pathIndex][pid]
									.charAt(zeroIndex + 1) - 48;
							rids = eLists.get(attrIndex);// 终点邻接点
							for (Object object : entities) {
								JSONObject entity = JSONObject
										.parseObject(object.toString());
								String id = entity.getString("Id");
								propertyValues = SetUtils.getPropertySet(
										entity, attrIndex);
								propertyValues.retainAll(rids);
								for (String value : propertyValues) {
									String string = "[" + startID + "," + id
											+ "," + value + "," + endID + "]";
									path.add(JSONArray.parseArray(string));
								}
							}

						} else {// zeroIndex=2
							attrIndex = path3[pathIndex][pid]
									.charAt(zeroIndex - 1) - 48;
							rids = sLists.get(attrIndex);// 起点邻接点
							for (Object object : entities) {
								JSONObject entity = JSONObject
										.parseObject(object.toString());
								String id = entity.getString("Id");
								propertyValues = SetUtils.getPropertySet(
										entity, attrIndex);
								propertyValues.retainAll(rids);
								for (String value : propertyValues) {
									String string = "[" + startID + "," + value
											+ "," + id + "," + endID + "]";
									path.add(JSONArray.parseArray(string));
								}
							}
						}
					}// 处理剩余情况
				}
			}// 处理请求结果

		} else if (pathIndex == 1) {// 对于[id,Auid]单独处理0004,其余双向取结果(并发请求)
			// 对每条路径构建相应的请求
			// 处理0104、0204、0304、0404、0454用到
			uris.add(setRequestParams(eval, expr4,
					"Id,RId,F.FId,C.CId,J.JId,AA.AuId,AA.AfId", count, 0, order));
			for (int i = 0; i < path3[pathIndex].length; i++) {// 实际上只需处理两种情况
				String request = null;
				int attrIndex1 = path3[pathIndex][i].charAt(1) - 48;
				int attrIndex2 = path3[pathIndex][i].charAt(2) - 48;
				if (attrIndex1 == 0 && attrIndex2 == 0) {// 处理0004
					for (String id : sLists.get(i)) {// 若参考文献过多可能还要进行分块
						String expr = "Id=" + id;
						request = ExprUtils.or(request, expr);
					}
					if (request != null) {
						URI uri = setRequestParams(eval, request, "Id,RId",
								sLists.get(i).size(), 0, order);
						uris.add(uri);
						pathID.add(i);
						System.out.println(path3[pathIndex][i]);
					}

				} else if (attrIndex2 == 5) {// 处理0454
					for (String id : sLists.get(attrIndex1)) {
						String expr = ExprUtils.composite(
								ATTR_NAME[attrIndex1], Long.parseLong(id));
						request = ExprUtils.or(request, expr);
					}
					if (request != null) {
						URI uri = setRequestParams(eval, request,
								"Id,AA.AuId,AA.AfId", count, 0, order);
						uris.add(uri);
						pathID.add(i);
						System.out.println(path3[pathIndex][i]);
					}
				}
			}

			// 处理请求结果
			httpClient = new ThreadPoolHttpClient(uris);
			results = httpClient.run();
			uris.clear();
			// 处理0104、0204、0304、0404
			JSONArray entities = JSONObject.parseObject(results[0])
					.getJSONArray("entities");
			for (Object object : entities) {
				List<List<String>> propertyValues = new ArrayList<List<String>>();
				JSONObject entity = JSONObject.parseObject(object.toString());
				String id = entity.getString("Id");
				for (int i = 1; i <= 4; i++) {
					propertyValues.add(SetUtils.getPropertySet(entity, i));
					propertyValues.get(i - 1).retainAll(sLists.get(i));
				}
				for (List<String> values : propertyValues) {
					for (String value : values) {
						String string = "[" + startID + "," + value + "," + id
								+ "," + endID + "]";
						path.add(JSONArray.parseArray(string));
					}
				}
			}
			// 处理0004、0454
			for (Integer pid : pathID) {
				int Index = 1;// results中的下标
				if (pid == 0) {// 0004
					List<String> rids;
					entities = JSONObject.parseObject(results[Index])
							.getJSONArray("entities");
					for (Object object : entities) {
						JSONObject entity = JSONObject.parseObject(object
								.toString());
						String id = entity.getString("Id");
						rids = SetUtils.getPropertySet(entity, 0);
						rids.retainAll(eLists.get(0));
						for (String value : rids) {
							String string = "[" + startID + "," + id + ","
									+ value + "," + endID + "]";
							path.add(JSONArray.parseArray(string));
						}
					}
					Index++;// 包含0004结果下标加1

				} else {// 0454,此处的机构信息是完整的
					List<String> afids = SetUtils.getAdjacentSet(
							JSONObject.parseObject(results[0]), 4, 5, endID);// 终点的机构信息
					for (String auid : sLists.get(4)) {
						List<String> afids1 = SetUtils.getAdjacentSet(
								JSONObject.parseObject(results[Index]), 4, 5,
								Long.parseLong(auid));
						afids1.retainAll(afids);
						for (String value : afids1) {
							String string = "[" + startID + "," + auid + ","
									+ value + "," + endID + "]";
							path.add(JSONArray.parseArray(string));
						}
					}
				}
			}

		} else if (pathIndex == 2) {// [Auid,id]单独处理4000,其余双向取结果(并发请求)
			// 构建每条路径相应的请求
			// 4000、4010、4020、4030、4040均要用到
			uris.add(setRequestParams(eval, expr2,
					"Id,RId,F.FId,C.CId,J.JId,AA.AuId,AA.AfId", count, 0, order));
			for (int i = 0; i < path3[pathIndex].length; i++) {// 实际上只需处理两种情况
				String request = null;
				int attrIndex1 = path3[pathIndex][i].charAt(1) - 48;
				int attrIndex2 = path3[pathIndex][i].charAt(2) - 48;
				if (attrIndex1 == 0 && attrIndex2 == 0) {// 处理4000
					request = "RId=" + endID;
					URI uri = setRequestParams(hist, request, "Id", count, 0);
					uris.add(uri);
					pathID.add(i);
					System.out.println(path3[pathIndex][i]);
				} else if (attrIndex1 == 5) {// 处理4540
					for (String auid : eLists.get(attrIndex2)) {
						String expr = ExprUtils.composite(
								ATTR_NAME[attrIndex2], Long.parseLong(auid));
						request = ExprUtils.or(request, expr);
					}
					if (request != null) {
						URI uri = setRequestParams(eval, request,
								"Id,AA.AuId,AA.AfId", count, 0, order);
						uris.add(uri);
						pathID.add(i);
						System.out.println(path3[pathIndex][i]);
					}
				}
			}// 对于4010、4020、4030、4040、4540无需额外的请求

			// 处理请求结果
			httpClient = new ThreadPoolHttpClient(uris);
			results = httpClient.run();
			uris.clear();
			// 处理4000、4010、4020、4030、4040
			JSONArray entities = JSONObject.parseObject(results[0])
					.getJSONArray("entities");
			List<String> rids = SetUtils.getAdjacentSet(
					JSONObject.parseObject(results[1]), 1, 0);
			for (Object object : entities) {
				List<List<String>> propertyValues = new ArrayList<List<String>>();
				JSONObject entity = JSONObject.parseObject(object.toString());
				String id = entity.getString("Id");
				for (int i = 0; i < 5; i++) {
					propertyValues.add(SetUtils.getPropertySet(entity, i));
					if (i == 0) {
						propertyValues.get(i).retainAll(rids);
					} else {
						propertyValues.get(i).retainAll(eLists.get(i));
					}
				}
				for (List<String> values : propertyValues) {
					for (String value : values) {
						String string = "[" + startID + "," + id + "," + value
								+ "," + endID + "]";
						path.add(JSONArray.parseArray(string));
					}
				}
			}
			// 处理4540
			if (results.length > 2) {
				List<String> afids = SetUtils.getAdjacentSet(
						JSONObject.parseObject(results[0]), 4, 5, startID);
				JSONObject json = JSONObject.parseObject(results[2]);
				for (String auid : eLists.get(4)) {
					List<String> afids1 = SetUtils.getAdjacentSet(json, 4, 5,
							Long.parseLong(auid));
					afids1.retainAll(afids);
					for (String value : afids1) {
						String string = "[" + startID + "," + value + ","
								+ auid + "," + endID + "]";
						path.add(JSONArray.parseArray(string));
					}
				}
			}

		} else {// 对于[Auid,Auid]单独处理前半部分进行请求即可 处理4004
			uris.add(setRequestParams(eval, expr2, "Id,RId", count, 0, order));
			httpClient = new ThreadPoolHttpClient(uris);
			uris.clear();
			JSONArray entities = JSONObject.parseObject(results[0])
					.getJSONArray("entities");
			List<String> rids = eLists.get(0);
			List<String> rids1;
			for (Object object : entities) {
				JSONObject entity = JSONObject.parseObject(object.toString());
				String id = entity.getString("Id");
				rids1 = SetUtils.getPropertySet(entity, 0);
				rids1.retainAll(rids);
				for (String value : rids1) {
					String string = "[" + startID + "," + id + "," + value
							+ "," + endID + "]";
					path.add(JSONArray.parseArray(string));
				}
			}
		}
		// 请求判断法
		System.out.println(path.size());
		return JSON.toJSONString(path);
	}

/*	public static void main(String[] args) throws URISyntaxException {
		// TODO Auto-generated method stub

		long startID = 2019832499L;//4// 682752941L;4//2871918L;0//44514345L;0
		long endID = 2114332599L;//4// 6400277L;0//2150852454L;4//2117239687L;0
		PathParse path = new PathParse();
		long startTime = System.currentTimeMillis();
		// System.out.println(path.isAdjacent(startID, endID));
		System.out.println(path.getAllPath(startID, endID));
		long endTime = System.currentTimeMillis();
		System.out.println("runining time is " + (endTime - startTime) / 1000f
				+ "s");
	}*/
}
