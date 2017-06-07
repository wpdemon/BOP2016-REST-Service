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
	 * // hop-1�Ŀɴ���� private final static int[][] pathNum1 = { { 1, 1, 1, 1, 1,
	 * 0 }, { 1, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0 }, {
	 * 1, 0, 0, 0, 0, 1 }, { 0, 0, 0, 0, 1, 0 } };
	 * 
	 * // hop-2�Ŀɴ���� private final static int[][] pathNum2 = { { 5, 1, 1, 1, 1,
	 * 1 }, { 1, 1, 1, 1, 1, 0 }, { 1, 1, 1, 1, 1, 0 }, { 1, 1, 1, 1, 1, 0 }, {
	 * 1, 1, 1, 1, 2, 0 }, { 1, 0, 0, 0, 0, 1 } }; // hop-3�Ŀɴ���� private final
	 * static int[][] pathNum3 = { { 9, 5, 5, 5, 6, 1 }, { 5, 1, 1, 1, 1, 1 }, {
	 * 5, 1, 1, 1, 1, 1 }, { 5, 1, 1, 1, 1, 1 }, { 6, 1, 1, 1, 1, 2 }, { 1, 1,
	 * 1, 1, 2, 0 } };
	 */
	// ��¼ 1-hop���ܵ�·��
	private String[] path1 = { "00", "04", "40" };
	// ��¼ 2-hop���ܵ�·��
	private String[][] path2 = { { "000", "010", "020", "030", "040" },
			{ "004" }, { "400" }, { "404", "454" } };
	// ��¼ 3-hop���ܵ�·��
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
	// ����·����Ϣ
	private List<JSONArray> path = new ArrayList<JSONArray>();

	public PathParse() throws URISyntaxException {
		eval = new URIBuilder(
				"http://oxfordhk.azure-api.net/academic/v1.0/evaluate");
		hist = new URIBuilder(
				"http://oxfordhk.azure-api.net/academic/v1.0/calchistogram");
	}

	/**
	 * eval����
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
		// ���ؽ����˳�򣬸ò�����Ҫ�����������Ч��
		builder.setParameter("orderby", order);
		// ������Ȩkey
		builder.setParameter("subscription-key",
				"f7cc29509a8443c5b3a5e56b0e38b5a6");
		return builder.build();
	}

	/**
	 * hist����
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
		// ������Ȩkey
		builder.setParameter("subscription-key",
				"f7cc29509a8443c5b3a5e56b0e38b5a6");
		return builder.build();
	}
	
	/**
	 * ������㡢�յ���Ϣ��ȡ·���±�
	 * 
	 * @param startInfo
	 * @param endInfo
	 * @return ·���±�
	 */
	public int getPathIndex(String startInfo, String endInfo) {
		int pathIndex = 0;
		JSONArray sArray=JSONObject.parseObject(startInfo)
				.getJSONArray("entities");
		JSONArray eArray=JSONObject.parseObject(endInfo)
				.getJSONArray("entities");
		if (sArray.size()==0||eArray.size()==0) {//�ж�Id�Ƿ����
			return -1;
		}
		JSONObject sInfo = sArray.getJSONObject(0);
		JSONObject eInfo = eArray.getJSONObject(0);		
		// Ĭ�Ͼ�ΪAuId
		int sType = 4;
		int eType = 4;
		// ��ΪId,��RId��AA.AuId��F.FId��J.JId��C.CId������һ�Ϊ��
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

		// ӳ��·���±�
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
	 * ��ȡ����1-3 hop·��
	 * 
	 * @param startID
	 *            ���ID
	 * @param endID
	 *            �յ�ID
	 * @return ·���б�
	 * @throws URISyntaxException
	 */
	public String getAllPath(long startID, long endID)
			throws URISyntaxException {
		// ����ʵ����Ϣ
		List<String> entityInfo = new ArrayList<String>();
		// ���������URIs
		List<URI> uris = new ArrayList<URI>();
		ThreadPoolHttpClient httpClient;
		// �����Զ�Ӧ�Ĺ̶�����
		String[] attrs = { "RId,AA.AuId,F.FId,J.JId,C.CId,AA.AfId", "Id", "Id",
				"Id", "Id,RId,AA.AfId", "AA.AuId" };
		int[][] attrIndexes = { { 0, 1, 2, 3, 4 }, { 0, 5, 6 } };
		// ��ѯ�����̶�
		int count = 10000;
		// �̶�˳��
		String order = "Id:asc";

		// ��ȡ������յ���Ϣ,�жϽڵ����� ,����Ϊ���ΪId��AA.AuId;�յ�ΪId��AA.AuId
		String expr1 = "Id=" + startID;
		String expr2 = ExprUtils.composite("AA.AuId", startID);
		String expr3 = "Id=" + endID;
		String expr4 = ExprUtils.composite("AA.AuId", endID);
		String[] initExprs = { expr1, expr2, expr3, expr4 };
		// ��ȡʵ����Ϣ��ͳ����Ϣ
		for (int i = 0; i < initExprs.length; i++) {
			URI uri;
			if (i % 2 == 0) {// Id����
				uri = setRequestParams(eval, initExprs[i], attrs[0], 1, 0,
						order);
			} else {// AuId����
				uri = setRequestParams(hist, initExprs[i], attrs[4], count, 0);
			}
			uris.add(uri);
		}
		// 1-hop����
		for (int i = 0; i < path1.length; i++) {
			String expr = ExprUtils.getAdjacentExpr(path1[i].charAt(0),
					path1[i].charAt(1), startID, endID);
			URI uri = setRequestParams(hist, expr, "Id", 1, 0);
			uris.add(uri);
		}

		System.out.print("1-hop ");// Ŀǰ1-hopΪ��Ҫƿ�� 0.747s
		httpClient = new ThreadPoolHttpClient(uris);
		String[] results = httpClient.run();
		// ��ȡ���
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
		
		// �ж���㡢�յ�ID����,����Id��AuId���ظ�,�����ж��Ƿ�ΪAuId
		int pathIndex = getPathIndex(entityInfo.get(0), entityInfo.get(2));
		if (pathIndex==-1) {//������һ��ʵ��Id������
			System.out.println(path.size());
			return JSON.toJSONString(path);
		}
		System.out.println("·������Ϊ:" + pathIndex);
		// ��·������ӳ�䵽��㡢�յ�����
		int sIndex = path2[pathIndex][0].charAt(0) - 48;
		int eIndex = path2[pathIndex][0].charAt(2) - 48;
		JSONObject startInfo = JSONObject.parseObject(entityInfo
				.get(sIndex / 4));
		JSONObject endInfo = JSONObject.parseObject(entityInfo
				.get(eIndex / 4 + 2));

		// System.out.println(startInfo);
		// System.out.println(endInfo);
		// ������㡢�յ����ͽ�����Ӧ�ı�������
		List<List<String>> sLists = new ArrayList<List<String>>();
		List<List<String>> eLists = new ArrayList<List<String>>();
		for (int i = 0; i < attrIndexes[sIndex / 4].length; i++) {
			if (sIndex / 4 == 0) {// eval���
				sLists.add(SetUtils.getPropertySet(
						startInfo.getJSONArray("entities").getJSONObject(0),
						attrIndexes[0][i]));
			} else {// hist���
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
			if (eIndex / 4 == 0) {// eval���
				eLists.add(SetUtils.getPropertySet(
						endInfo.getJSONArray("entities").getJSONObject(0),
						attrIndexes[0][i]));
			} else {// hist���
				if (attrIndexes[1][i] == 5) {
					for (int j = 0; j < 4; j++) {
						eLists.add(null);
					}
				}
				eLists.add(SetUtils.getAdjacentSet(endInfo, eIndex,
						attrIndexes[1][i]));
			}
		}

		// �����󽻷�������,��Ҫ�Լ���������
		// ���2-hop
		System.out.print("2-hop ");
		// ����ڽӼ�
		List<String> adjacentID;
		for (int j = 0; j < path2[pathIndex].length; j++) {
			// �м�ڵ�����
			int mid = path2[pathIndex][j].charAt(1) - 48;
			adjacentID = sLists.get(mid);
			if (mid == 0 && eIndex == 0) {// [Id,Id]����ͨ��������������,��������
											// //����ͨ��1��request���
				// ����һ��ͨ�����Ӳ�ѯ���ʽ����URI,��ʱ����,��Ҫ�������󳤶�����
				// ��������URI���зֿ�,��Ĵ�С��ø����̳߳صĴ�С����
				int chunkSize = 30;
				int chunkNum = adjacentID.size() / chunkSize + 1;
				for (int k = 0; k < chunkNum; k++) {
					String request = null;
					for (int l = 0; l < chunkSize; l++) {
						if (k * chunkSize + l == adjacentID.size()) {// �Ƿ�Խ��
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
				
				// ��������ͨ��RId=endID����
				/*String request="RId="+endID;
				URI uri = setRequestParams(hist,request,"Id",count,0);
				uris.add(uri);*/
				
				// ����URI
				if (uris.size() > 0) {// ��֤URI��Ϊ��					
					httpClient = new ThreadPoolHttpClient(uris);
					results = httpClient.run();
					uris.clear();
					// ����һ����
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
					//����������
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

			} else {// ���ϵ�Id�������,�˴����Խ�һ���Ż�
				// �յ����ڼ�
				List<String> adjacentID1;
				// ��һ�������������,�ٶ���,���ٻ��ʸ�
				if (mid == 5) {
					uris.add(setRequestParams(eval, expr2,
							"Id,AA.AuId,AA.AfId", count, 0, order));// ���������ظ�����
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
				 * // �����������,�ٶȿ쵫�ٻ��ʵ� if (mid == 5) {//
				 * ���ڽ�������ʽ�������,ֻ��Ҫȡǰ10�����������֤����,����ʱ���Գ����Կ��Ǳ��ش���
				 * adjacentID1=eLists.get(mid); // ������
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
					// ������
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
		// ���3-hop
		List<Integer> pathID = new ArrayList<Integer>();// ��¼���������·�����
		// ����[id,id]�Ӻ���ǰ�ҵ���һ��������0�±�,�ϳ��Ĳ��ֽ���˳����չ,���������;����0000ֻ�ܽ��е�������,�����ɽ���memo
		if (pathIndex == 0) {
			// ���0000����ͳ����Ϣ����
			URI histURI = null;
			// ��ȡ��·��������URI
			for (int i = 0; i < path3[pathIndex].length; i++) {
				String request = null;
				if (i == 0) {// ��������0000
					for (String id : sLists.get(i)) {// ���ο����׹�����ܻ�Ҫ���зֿ�
						String expr = "Id=" + id;
						request = ExprUtils.or(request, expr);
					}
					if (request != null) {// ����0000ʱ����ͳ����Ϣ������
						URI uri = setRequestParams(eval, request, "Id,"
								+ ATTR_NAME[i], sLists.get(i).size(), 0, order);
						histURI = setRequestParams(hist, request, ATTR_NAME[i],
								sLists.get(i).size(), 0);
						pathID.add(i);
						uris.add(uri);
						System.out.println(path3[pathIndex][i]);
					}
				} else {
					// �Ӻ���ǰ�ҵ��ڶ���0��λ��,����·��Ϊ������,�Խϳ��Ĳ��ֽ�����չ
					int zeroIndex = path3[pathIndex][i].lastIndexOf("0",
							path3[pathIndex][i].length() - 2);
					int attrIndex;
					int reqNum = count;// ���ؽ����
					if (zeroIndex == 1) {
						attrIndex = path3[pathIndex][i].charAt(zeroIndex + 1) - 48;
						for (String id : sLists.get(0)) {// ���ο����׹�����ܻ�Ҫ���зֿ�
							String expr = "Id=" + id;
							request = ExprUtils.or(request, expr);
						}
						reqNum = sLists.get(0).size();

					} else {// zeroIndex=2
						attrIndex = path3[pathIndex][i].charAt(zeroIndex - 1) - 48;
						for (String id : sLists.get(attrIndex)) {// �������Զ�Ӧ��ֵһ����ٹʲ��÷ֿ�
							String expr = ExprUtils.and("RId", endID, ExprUtils
									.composite(ATTR_NAME[attrIndex],
											Long.parseLong(id)));
							request = ExprUtils.or(request, expr);
						}
					}
					if (request != null) {// ����Ϊ��,count�Ƿ���Ը���ͳ����Ϣ��ȷ��,����count�����Լӿ���Ӧ�ٶ�,reqNum����Ӧ
						URI uri = setRequestParams(eval, request, "Id,"
								+ ATTR_NAME[attrIndex], reqNum, 0,
								ATTR_NAME[attrIndex] + ":asc");
						pathID.add(i);
						uris.add(uri);
						System.out.println(path3[pathIndex][i]);
					}
				}
			}

			if (histURI != null) {// ����0000����ͳ����Ϣ����,������ĩβ
				uris.add(histURI);
			}

			// �ύ���󲢴�����,count�����������Ӧʱ��Ӱ��ܴ�,��Ҫ��count���к��������
			if (uris.size() > 0) {
				httpClient = new ThreadPoolHttpClient(uris);
				results = httpClient.run();
				uris.clear();
				System.out.println("results num:" + results.length);
				List<String> rids;// �ڽ�id�ļ���
				for (int i = 0; i < pathID.size(); i++) {
					int pid = pathID.get(i);
					// ����0000����,����Ҫ���н�һ������
					if (pid == 0) {
						// ��ȡRId����
						rids = SetUtils.getAdjacentSet(
								JSONObject.parseObject(results[pathID.size()]),
								0, 0);
						// ��rids�ֿ��������
						int chunkSize = 30;// ���С
						int chunkNum = rids.size() / chunkSize + 1;// ����
						for (int k = 0; k < chunkNum; k++) {
							String request = null;
							for (int l = 0; l < chunkSize; l++) {
								if (k * chunkSize + l == rids.size()) {// Խ����
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
						// 0000 ·������
						if (uris.size() > 0) {
							System.out.print("0000 test ");
							httpClient = new ThreadPoolHttpClient(uris);
							String[] reqResults = httpClient.run();
							uris.clear();
							System.out.println("result num:"
									+ reqResults.length);
							// �ϲ����
							rids.clear();
							for (String string : reqResults) {
								rids.addAll(SetUtils.getAdjacentSet(
										JSONObject.parseObject(string), 1, 0));
							}
							// ����ڶ����ڵ��Rid
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

					} else {// ����ʣ�����
						int zeroIndex = path3[pathIndex][pid].lastIndexOf("0",
								path3[pathIndex][pid].length() - 2);
						int attrIndex;
						List<String> propertyValues;
						JSONArray entities = JSONObject.parseObject(results[i])
								.getJSONArray("entities");
						if (zeroIndex == 1) {
							attrIndex = path3[pathIndex][pid]
									.charAt(zeroIndex + 1) - 48;
							rids = eLists.get(attrIndex);// �յ��ڽӵ�
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
							rids = sLists.get(attrIndex);// ����ڽӵ�
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
					}// ����ʣ�����
				}
			}// ����������

		} else if (pathIndex == 1) {// ����[id,Auid]��������0004,����˫��ȡ���(��������)
			// ��ÿ��·��������Ӧ������
			// ����0104��0204��0304��0404��0454�õ�
			uris.add(setRequestParams(eval, expr4,
					"Id,RId,F.FId,C.CId,J.JId,AA.AuId,AA.AfId", count, 0, order));
			for (int i = 0; i < path3[pathIndex].length; i++) {// ʵ����ֻ�账���������
				String request = null;
				int attrIndex1 = path3[pathIndex][i].charAt(1) - 48;
				int attrIndex2 = path3[pathIndex][i].charAt(2) - 48;
				if (attrIndex1 == 0 && attrIndex2 == 0) {// ����0004
					for (String id : sLists.get(i)) {// ���ο����׹�����ܻ�Ҫ���зֿ�
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

				} else if (attrIndex2 == 5) {// ����0454
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

			// ����������
			httpClient = new ThreadPoolHttpClient(uris);
			results = httpClient.run();
			uris.clear();
			// ����0104��0204��0304��0404
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
			// ����0004��0454
			for (Integer pid : pathID) {
				int Index = 1;// results�е��±�
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
					Index++;// ����0004����±��1

				} else {// 0454,�˴��Ļ�����Ϣ��������
					List<String> afids = SetUtils.getAdjacentSet(
							JSONObject.parseObject(results[0]), 4, 5, endID);// �յ�Ļ�����Ϣ
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

		} else if (pathIndex == 2) {// [Auid,id]��������4000,����˫��ȡ���(��������)
			// ����ÿ��·����Ӧ������
			// 4000��4010��4020��4030��4040��Ҫ�õ�
			uris.add(setRequestParams(eval, expr2,
					"Id,RId,F.FId,C.CId,J.JId,AA.AuId,AA.AfId", count, 0, order));
			for (int i = 0; i < path3[pathIndex].length; i++) {// ʵ����ֻ�账���������
				String request = null;
				int attrIndex1 = path3[pathIndex][i].charAt(1) - 48;
				int attrIndex2 = path3[pathIndex][i].charAt(2) - 48;
				if (attrIndex1 == 0 && attrIndex2 == 0) {// ����4000
					request = "RId=" + endID;
					URI uri = setRequestParams(hist, request, "Id", count, 0);
					uris.add(uri);
					pathID.add(i);
					System.out.println(path3[pathIndex][i]);
				} else if (attrIndex1 == 5) {// ����4540
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
			}// ����4010��4020��4030��4040��4540������������

			// ����������
			httpClient = new ThreadPoolHttpClient(uris);
			results = httpClient.run();
			uris.clear();
			// ����4000��4010��4020��4030��4040
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
			// ����4540
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

		} else {// ����[Auid,Auid]��������ǰ�벿�ֽ������󼴿� ����4004
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
		// �����жϷ�
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
