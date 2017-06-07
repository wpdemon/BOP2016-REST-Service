package com.hust.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class SetUtils {

	// 属性名映射
	private final static String[] ATTR_NAME = { "Id", "FId", "JId", "CId",
			"AuId", "AfId" };
	private final static String[] ATTR_NAME2 = { "RId", "F.FId", "J.JId",
			"C.CId", "AA.AuId", "AA.AfId" };
	private final static String[] ATTR_NAME3 = { "Id", "F.FId", "J.JId",
		"C.CId", "AA.AuId", "AA.AfId","RId" };

	/**
	 * 获取相邻节点集by eval
	 * 
	 * @param entityInfo
	 *            相关实体信息
	 * @param attrIndex1
	 *            属性下标1
	 * @param attrIndex2
	 *            属性下标2
	 * @param attrId1
	 *            属性值1
	 * @return 相邻节点列表
	 */
	public static List<String> getAdjacentSet(JSONObject entityInfo,
			int attrIndex1, int attrIndex2, long attrId1) {
		List<String> adjacentID = new ArrayList<String>();
		switch (attrIndex1) {
		case 0:// Id
		{
			JSONObject entity = entityInfo.getJSONArray("entities")
					.getJSONObject(0);
			switch (attrIndex2) {
			case 0:// Id
				adjacentID = JSONArray.parseArray(entity.getString("RId"),
						String.class);
				break;
			// F.FId、J.JId、C.CId、AA.AuId(C、J均为JsonObject,AA、F均为JsonArray)
			case 1:// F.FId
				JSONArray FF;
				if ((FF = entity.getJSONArray("F")) != null) {
					for (Object F : FF) {
						adjacentID.add(JSONObject.parseObject(F.toString())
								.getString(ATTR_NAME[attrIndex2]));
					}
				}
				break;
			case 2:// J.JId
				JSONObject J;
				if ((J = entity.getJSONObject("J")) != null) {
					adjacentID.add(J.getString(ATTR_NAME[attrIndex2]));
				}
				break;
			case 3:// C.CId
				JSONObject C;
				if ((C = entity.getJSONObject("C")) != null) {
					adjacentID.add(C.getString(ATTR_NAME[attrIndex2]));
				}
				break;
			case 4:// AA.AuId
				JSONArray AA;
				if ((AA = entity.getJSONArray("AA")) != null) {
					for (Object A : AA) {
						adjacentID.add(JSONObject.parseObject(A.toString())
								.getString(ATTR_NAME[attrIndex2]));
					}
				}
				break;
			}
			if (adjacentID == null) {
				return new ArrayList<String>();
			}
			return adjacentID;
		}
		case 1:// F.FId
		case 2:// J.JId
		case 3:// C.CId
		case 4:// AA.AuId
			if (attrIndex2 == 0) {// [AA.AuId,Id]
				for (Object entity : entityInfo.getJSONArray("entities")) {
					adjacentID.add(JSONObject.parseObject(entity.toString())
							.getString("Id"));
				}
			} else {// [AA.AuId,AA.AfId] 处理效率略低
				String AfId = null;
				HashSet<String> set=new HashSet<String>();
				for (Object object : entityInfo.getJSONArray("entities")) {
					JSONObject entity = JSONObject.parseObject(object
							.toString());
					for (Object author : entity.getJSONArray("AA")) {
						long auid = JSONObject.parseObject(author.toString())
								.getLong("AuId");
						if (auid == attrId1) {
							AfId = JSONObject.parseObject(author.toString())
									.getString("AfId");	
							if (AfId != null) {
								set.add(AfId);								
							}
							break;
						}						
					}					
				}
				adjacentID.addAll(set);				
			}
			return adjacentID;
		case 5:// AA.AfId
			for (Object object : entityInfo.getJSONArray("entities")) {
				JSONObject entity = JSONObject.parseObject(object.toString());
				for (Object author : entity.getJSONArray("AA")) {
					adjacentID.add(JSONObject.parseObject(author.toString())
							.getString("AuId"));
				}
			}
			return adjacentID;
		default:
			return adjacentID;
		}
	}
	
	/**
	 * 获取实体属性集by eval
	 * @param entity 单个实体
	 * @param attrIndex
	 * @return 属性Id集合
	 */
	public static List<String> getPropertySet(JSONObject entity,int attrIndex) {
		List<String> adjacentID = new ArrayList<String>();
		switch (attrIndex) {
		case 0:// RId				
			adjacentID = JSONArray.parseArray(entity.getString("RId"),String.class);
			break;
		// F.FId、J.JId、C.CId、AA.AuId(C、J均为JsonObject,AA、F均为JsonArray)
		case 1:// F.FId
			JSONArray FF;
			if ((FF = entity.getJSONArray("F")) != null) {
				for (Object F : FF) {
					adjacentID.add(JSONObject.parseObject(F.toString())
							.getString(ATTR_NAME[attrIndex]));
				}
			}
			break;
		case 2:// J.JId
			JSONObject J;
			if ((J = entity.getJSONObject("J")) != null) {
					adjacentID.add(J.getString(ATTR_NAME[attrIndex]));
			}
			break;
		case 3:// C.CId
			JSONObject C;
			if ((C = entity.getJSONObject("C")) != null) {
				adjacentID.add(C.getString(ATTR_NAME[attrIndex]));
			}
			break;
		case 4:// AA.AuId
			JSONArray AA;
			if ((AA = entity.getJSONArray("AA")) != null) {
				for (Object A : AA) {
					adjacentID.add(JSONObject.parseObject(A.toString())
							.getString(ATTR_NAME[attrIndex]));
				}
			}
			break;
		}
		if (adjacentID == null) {
			return new ArrayList<String>();
		}
		return adjacentID;		
	}
	
	
	/**
	 * 获取相邻节点集by hist
	 * AuId中当attrIndex2为6时对应RId
	 * 
	 * @param entityInfo
	 * @param attrIndex1
	 * @param attrIndex2
	 * @return 相邻节点列表
	 */
	public static List<String> getAdjacentSet(JSONObject entityInfo,
			int attrIndex1, int attrIndex2) {
		List<String> adjacentID = new ArrayList<String>();
		List<String> valueList= new ArrayList<String>();
		JSONArray histograms = entityInfo.getJSONArray("histograms");
		JSONObject attr;
		switch (attrIndex1) {
		case 0:// Id		
			for (Object histogram : histograms) {
				attr = JSONObject.parseObject(histogram.toString());
				if (attr.getString("attribute").equals(ATTR_NAME2[attrIndex2])) {
					adjacentID = JSONArray.parseArray(
							attr.getString("histogram"), String.class);
					break;
				}				
			}		
			break;
		case 1:// F.FId
		case 2:// J.JId
		case 3:// C.CId
		//以上三个均只对应Id
			attr=histograms.getJSONObject(0);
			adjacentID = JSONArray.parseArray(attr.getString("histogram"), String.class);
			break;
		case 4:// AA.AuId 机构有效性检验
			for (Object histogram : histograms) {
				attr = JSONObject.parseObject(histogram.toString());
				if (attr.getString("attribute").equals(ATTR_NAME3[attrIndex2])) {
					adjacentID = JSONArray.parseArray(
							attr.getString("histogram"), String.class);
					break;
				}				
			}
			break;
		case 5:// AA.AfId
			attr=histograms.getJSONObject(0);
			adjacentID = JSONArray.parseArray(attr.getString("histogram"), String.class);
			break;
		}
		if (adjacentID==null) {
			return new ArrayList<String>();
		}		
		for (String string : adjacentID) {
			JSONObject object=JSONObject.parseObject(string);
			valueList.add(object.getString("value"));
		}
		return valueList;
	}
}
