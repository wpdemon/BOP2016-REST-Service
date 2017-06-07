package com.hust.app;

public class ExprUtils {	

	public static String and(String key1, long value1, String key2, long value2) {
		return "And(" + key1 + "=" + value1 + "," + key2 + "=" + value2 + ")";
	}

	
	/**
	 * 
	 * @param expr1 表达式1(可以为空)
	 * @param expr2 表达式2
	 * @return AND表达式
	 */
	public static String and(String expr1, String expr2) {
		if (expr1==null) {
			return expr2;
		}
		return "And(" + expr1 + "," + expr2 + ")";
	}

	public static String and(String key, long value, String expr) {
		return "And(" + key + "=" + value + "," + expr + ")";
	}
	
	public static String and(String expr) {
		return "And(" + expr + ")";
	}

	public static String or(String key1, long value1, String key2, long value2) {
		return "Or(" + key1 + "=" + value1 + "," + key2 + "=" + value2 + ")";
	}
	
	/**
	 * 
	 * @param expr1 表达式1(可以为空)
	 * @param expr2 表达式2
	 * @return OR表达式
	 */
	public static String or(String expr1, String expr2) {
		if (expr1==null) {
			return expr2;
		}
		return "Or(" + expr1 + "," + expr2 + ")";
	}

	public static String or(String key, long value, String expr) {
		return "Or(" + key + "=" + value + "," + expr + ")";
	}
	
	public static String or(String expr) {
		return "Or(" + expr + ")";
	}

	public static String composite(String expr) {
		return "Composite(" + expr + ")";
	}

	public static String composite(String key, long value) {
		return "Composite(" + key + "=" + value + ")";
	}

	public static String getAdjacentExpr(int attrIndex1, int attrIndex2,
			long value1, long value2) {
		switch (attrIndex1) {
		case 0:// Id 5种邻接关系
		{
			switch (attrIndex2) {
			case 0:
				return and("Id", value1, "RId", value2);
			case 1:
				return and("Id", value1, composite("F.FId", value2));
			case 2:
				return and("Id", value1, composite("J.JId", value2));
			case 3:
				return and("Id", value1, composite("C.CId", value2));
			case 4:
				return and("Id", value1, composite("AA.AuId", value2));
			}
		}
		case 1:// F.FId 1种邻接关系
			return and("Id", value2, composite("F.FId", value1));
		case 2:// J.JId 1种邻接关系
			return and("Id", value2, composite("J.JId", value1));
		case 3:// C.CId 1种邻接关系
			return and("Id", value2, composite("C.CId", value1));
		case 4:// AA.AuId 2种邻接关系
			if (attrIndex2 == 0) {
				return and("Id", value2, composite("AA.AuId", value1));
			} else {
				return composite(and("AA.AuId", value1, "AA.AfId", value2));
			}
		case 5:// AA.AfId 1种邻接关系
			return composite(and("AA.AfId", value1, "AA.AuId", value2));
		default:
			return null;
		}
	}

	public static String getAdjacentExpr(String attrIndex1, String attrIndex2,
			long value1, long value2) {
		int Index1 = Integer.parseInt(attrIndex1);
		int Index2 = Integer.parseInt(attrIndex2);
		return getAdjacentExpr(Index1, Index2, value1, value2);
	}
	
	public static String getAdjacentExpr(char attrIndex1, char attrIndex2,
			long value1, long value2) {
		int Index1 = attrIndex1-48;
		int Index2 = attrIndex2-48;
		return getAdjacentExpr(Index1, Index2, value1, value2);
	}
}
