
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.MapSerializer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.FileUtils;
//import com.google.common.collect.RegularImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Executable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * encode ：：utf8 auto；； v8 使用sql和内嵌数据库加强可读性 attilax v7 修正了fmt格式化问题，更加的可读性增强 v6
 * 317 fix fmt prblm v5 可读性增强 增加注释 v4增加可读性
 * 
 * 
 */
/*
 * 计算税率主要思路流程：： 1. 内存数据表LIST 进行投影运算 循环 获取税率，UDF计算单项税金， 2. 聚合运算 计算总税 2. 格式化展示
 * 需要fix的地方 map 初始化guava提升可读性,print cell oo化,udf税率计算优化,tax rate table habin
 */
@SuppressWarnings("all")
public class taxrateClsV8sale {
	public static void exeUpdateSafe(Connection c, String sql2) throws Exception {

		Statement stmt = c.createStatement();
		try {
			System.out.println(sql2);
			System.out.println(stmt.executeUpdate(sql2));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	// 第一个范例
	@Test
	public void testInput1() throws Exception {
		// System.out.println( Maps.newLinkedHashMap);
		System.out.println("----------------第一个测试数据----------------");

		String struts_sql_f = taxrateClsV8sale.class.getResource("/").getPath() + "prod_table.sql";
		String struts_sql = FileUtils.readFileToString(new File(struts_sql_f));
		exeUpdateSafe(struts_sql);
//  loc地点" ,  "item物品名" ,  "price价格" ,  "qty数量" ,
		String sql = "INSERT INTO \"product商品表\" VALUES ('CA', 'book', 17.99, 1, NULL, NULL, NULL, NULL, NULL);";
		exeUpdateSafe(sql);

		sql = "INSERT INTO \"product商品表\" VALUES ('CA', 'potato chips', 3.99, 1, NULL, NULL, NULL, NULL, NULL);";
		exeUpdateSafe(sql);

		sql = "SELECT  * from  product商品表";

		List<Map<String, Object>> query = query(sql);

		System.out.println(query);

 		Map rzt = calcProcsss();
 		formatShow(rzt);
		System.out.println("--------------end------------------");

	}

	private static List<Map<String, Object>> query(String sql) throws Exception {
	    System.out.println(sql); 
		//Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:test.db");
		QueryRunner run = new QueryRunner();
		List<Map<String, Object>> query = run.query(c, sql, new MapListHandler());System.out.println(query);
		return query;
	}

	private static Object queryOject(String sql) throws Exception {
		  System.out.println(sql); 
		  //Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:test.db");
		QueryRunner run = new QueryRunner();
		Object query = run.query(c, sql, new ScalarHandler());
		System.out.println(query);
		return query;
	}

	private static void exeUpdateSafe(String sql) throws Exception {
		Class.forName("org.sqlite.JDBC");
		Connection c = DriverManager.getConnection("jdbc:sqlite:test.db");
		exeUpdateSafe(c, sql);
	}

	// 第2个范例
	@Test
	public void testInput2() throws Exception {

		System.out.println("----------------第2个测试数据----------------");
		Map input1 = new LinkedHashMap() {
			{
				put("loc地点", "NY");
				put("shoplist物品列表", new ArrayList<Map>() {
					{
						add(MapBldr.newx().put("item物品名", "book").put("qty数量", 1).put("price价格", 17.99).build());
						add(MapBldr.newx().put("item物品名", "pencil").put("qty数量", 3).put("price价格", 2.99).build());
					}
				});
			}
		};
		Map rzt = calcProcsss();
		formatShow(rzt);
//        System.out.println(rzt);
//        System.out.println(JSON.toJSONString(rzt,true));
		System.out.println("----------------  end----------------");
	}

	// 第3个范例
	@Test
	public void testInput3() throws Exception {
		System.out.println("----------------第3个测试数据----------------");

		Map input1 = new LinkedHashMap() {
			{
				put("loc地点", "NY");
				put("shoplist物品列表", new ArrayList<Map>() {
					{

						add(MapBldr.newx().put("item物品名", "pencil").put("qty数量", 2).put("price价格", 2.99).build());
						add(MapBldr.newx().put("item物品名", "shirt").put("qty数量", 1).put("price价格", 29.99).build());
					}
				});
			}
		};

		// System.out.println(JSON.toJSONString(input1,true));
		// System.out.println(input1);
		Map rzt = calcProcsss( );
		formatShow(rzt);
		System.out.println("---------------- - end---------------");

	}

	// 内存数据表LIST 进行投影运算 循环 获取税率，UDF计算单项税金，并聚合运算 计算总税
	public static Map calcProcsss() throws Exception {
		Map input1=Maps.newConcurrentMap();
		// syc prod type
		String sql = "	update product商品表 as p set itemtype物品类型=( select type from   prodType商品类型表 where item物品名=p.item物品名)";
		 
		exeUpdateSafe(sql);
		sql = "update product商品表 as p  set  itemtype物品类型='other' where  itemtype物品类型 is null ";
		exeUpdateSafe(sql);

		// calc item_total物品价格不含税
		sql = "update product商品表    set  item_total物品价格不含税=price价格*qty数量 ";
		exeUpdateSafe(sql);

		// sync textrate
		//  
		sql = "update product商品表 as p  set  taxrate税率=("
				+ " select tax_rate_num税率数字格式 from  Taxrate税率表  where loc地点=p.loc地点 and type=p. itemtype物品类型 "
				+ ") , taxrate税率文本格式=("
				+ " select \"tax rate税率\" from  Taxrate税率表  where loc地点=p.loc地点 and type=p. itemtype物品类型 "
				+ ") ";
		exeUpdateSafe(sql);

		// calc tax
		sql = "update product商品表  set item_tax物品税= item_total物品价格不含税*taxrate税率";
		exeUpdateSafe(sql);
		
		sql = "select * from product商品表  ";
		 
        input1.put("shoplist物品列表", query(sql));

 
		// 数据表聚合运算 物品总价不含税
		sql = "select round(sum(item_total物品价格不含税),2) as subtotal物品总价不含税 from product商品表  ";

		input1.put("subtotal物品总价不含税", queryOject(sql));

		// 内存数据表聚合运算 计算总税
		// select sum（物品税） from 物品列表
		DecimalFormat df2 = new DecimalFormat("##0.00");// 这样为保持2位
		sql = "select  sum(item_tax物品税) as tax税 from product商品表  ";

	//	String tax = queryOject(sql).toString().toString();
		input1.put("tax税", roundUP(df2.format( queryOject(sql))));
		// input1.put("tax税", roundUP(df2.format(all_sale_tax)));

		// 计算total总价含税
		
		Double total = Double.parseDouble(input1.get("tax税").toString())
				+ Double.parseDouble(input1.get("subtotal物品总价不含税").toString());
		input1.put("total总价含税", df2.format(total));

		return input1;

	}

	// 格式化计算税费
	private static Object roundUP(String format) {
		String price_pre = format.substring(0, format.length() - 1);
		int lastNum = Integer.parseInt(format.substring(format.length() - 1));
		if (lastNum > 0 && lastNum < 5)
			return price_pre + "5";
		if (lastNum >= 5 && lastNum <= 9) {
			Double d = Double.parseDouble(price_pre) + 0.10d;
			DecimalFormat df2 = new DecimalFormat("##0.00");// 这样为保持2位
			return df2.format(d);
		}

		return format; // 0 5
	}

	static int cellwidth2 = 30;

	private static void formatShow(Map input1) {

		PrintCellUtil.printCell("item", "left");
		PrintCellUtil.printCell("price", "mid");
		PrintCellUtil.printCell("qty", "right");
		System.out.print("\r\n");
		((List) input1.get("shoplist物品列表")).forEach(new Consumer<Map>() {

			@Override
			public void accept(Map map) {

				PrintCellUtil.printCell(map.get("item物品名"), "left");
				PrintCellUtil.printPriceByMidtitle("$" + map.get("price价格"), 0, "price".length());
				PrintCellUtil.printCell(map.get("qty数量"), "right");

				System.out.print("\r\n");
			}

		});

		PrintCellUtil.printCell("subtotal:", "left", cellwidth2);
		PrintCellUtil.printCell("$" + input1.get("subtotal物品总价不含税").toString(), "right", cellwidth2);
		System.out.print("\r\n");

		PrintCellUtil.printCell("tax:", "left", cellwidth2);
		PrintCellUtil.printCell("$" + input1.get("tax税").toString(), "right", cellwidth2);
		System.out.print("\r\n");

		PrintCellUtil.printCell("total:", "left", cellwidth2);
		PrintCellUtil.printCell("$" + input1.get("total总价含税").toString(), "right", cellwidth2);
		System.out.print("\r\n");

	}

	// 计算单项税务

 

	// 查询税率 数据表选择运算
 

	// 查询物品类型
	 
}
