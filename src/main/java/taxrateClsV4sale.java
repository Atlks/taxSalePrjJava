
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.MapSerializer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;

//import com.google.common.collect.RegularImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * encode ：：utf8 auto；；attilax v7 修正了fmt格式化问题，更加的可读性增强 v6 317 fix fmt prblm v5
 * 可读性增强 增加注释 v4增加可读性
 * 
 * 
 */
/*
 * 计算税率主要思路流程：： 1. 内存数据表LIST 进行投影运算 循环 获取税率，UDF计算单项税金， 2. 聚合运算 计算总税 2. 格式化展示
 * 需要fix的地方 map 初始化guava提升可读性,print cell oo化,udf税率计算优化,tax rate table habin
 */
@SuppressWarnings("all")
public class taxrateClsV4sale {

	// 第一个范例
	@Test
	public void testInput1() {
		// System.out.println( Maps.newLinkedHashMap);
		System.out.println("----------------第一个测试数据----------------");
		Map input1 = new LinkedHashMap() {
			{

				put("loc地点", "CA");
				put("shoplist物品列表", new ArrayList<Map>() {
					{
						add(MapBldr.newx().put("item物品名", "book").put("qty数量", 1).put("price价格", 17.99).build());
						add(MapBldr.newx().put("item物品名", "potato chips").put("qty数量", 1).put("price价格", 3.99).build());
					}

				});
			}
		};

		Map rzt = calcProcsss(input1);
		formatShow(rzt);
		System.out.println("--------------end------------------");

		// calcTax(input1);
//        //    System.out.println(gettype("potato chips"));//
//
//    System.out.println(roundUP("0.16"));
//        System.out.println(roundUP("5.16"));
	}

	// 第2个范例
	@Test
	public void testInput2() {

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
		Map rzt = calcProcsss(input1);
		formatShow(rzt);
//        System.out.println(rzt);
//        System.out.println(JSON.toJSONString(rzt,true));
		System.out.println("----------------  end----------------");
	}

	// 第3个范例
	@Test
	public void testInput3() {
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
		Map rzt = calcProcsss(input1);
		formatShow(rzt);
		System.out.println("---------------- - end---------------");

	}

	// 内存数据表LIST 进行投影运算 循环 获取税率，UDF计算单项税金，并聚合运算 计算总税
	public static Map calcProcsss(Map input1) {

		((List) input1.get("shoplist物品列表")).forEach(new Consumer<Map>() {
			@Override
			public void accept(Map item) {
				Object itemtype = gettype(item.get("item物品名"));
				Map item_taxrate = selectTaxrateFrom_taxRateTable_where_loc_and_itemtype(input1.get("loc地点"), itemtype);
				item.put("itemtype物品类型", itemtype);
				item.put("taxrate税率", item_taxrate.get("tax_rate_num税率数字格式"));
				item.put("taxrate税率文本格式", item_taxrate.get("tax rate税率"));
				item.put("item_total物品价格不含税",
						(double) item.get("price价格") * Double.parseDouble(item.get("qty数量").toString()));

				item.put("item_tax物品税",
						get_item_tax(item.get("item_total物品价格不含税"), item_taxrate.get("tax_rate_num税率数字格式")));

				// item.put("taxrate_numFormat", Float.parseFloat(item_taxrate) );
			}

		});

		// 内存数据表聚合运算 物品总价不含税
		// select sum(物品价格不含税) from 物品列表
		DecimalFormat df2 = new DecimalFormat("##0.00");// 这样为保持2位
		double subtotal = ((List<Map>) input1.get("shoplist物品列表")).stream()
				.mapToDouble(i -> (double) i.get("item_total物品价格不含税")).sum();
		input1.put("subtotal物品总价不含税", (df2.format(subtotal + 0)));

		// 内存数据表聚合运算 计算总税
		// select sum（物品税） from 物品列表
		double all_sale_tax = ((List<Map>) input1.get("shoplist物品列表")).stream()
				.mapToDouble(i -> (double) i.get("item_tax物品税")).sum();

		input1.put("tax税", roundUP(df2.format(all_sale_tax)));

		// 计算total总价含税
		double total = Double.parseDouble(input1.get("tax税").toString())
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

	// 格式化展示 表格式cell展示数据
	// static int widthChar = 100;
	static int cellwidth = 20;
	static int cellwidth2 = 30;

	private static void formatShow(Map input1) {

		printCell("item", "left");
		printCell("price", "mid");
		printCell("qty", "right");
		System.out.print("\r\n");
		((List) input1.get("shoplist物品列表")).forEach(new Consumer<Map>() {

			@Override
			public void accept(Map map) {

				printCell(map.get("item物品名"), "left");
				printPrice("$" + map.get("price价格"), 0, "price".length());
				printCell(map.get("qty数量"), "right");

				System.out.print("\r\n");
			}

		});

		printCell("subtotal:", "left", cellwidth2);
		printCell("$" + input1.get("subtotal物品总价不含税").toString(), "right", cellwidth2);
		System.out.print("\r\n");

		printCell("tax:", "left", cellwidth2);
		printCell("$" + input1.get("tax税").toString(), "right", cellwidth2);
		System.out.print("\r\n");

		printCell("total:", "left", cellwidth2);
		printCell("$" + input1.get("total总价含税").toString(), "right", cellwidth2);
		System.out.print("\r\n");

	}

	protected static void printCell(Object lable, String Align) {
		Map cell1 = new HashMap();
		cell1.put("lable", lable);
		cell1.put("Align对齐模式", Align);
		cell1.put("width", cellwidth);
		printCell(cell1);
	}

	protected static void printCell(Object lable, String Align, int cellwidth22) {
		Map cell1 = new HashMap();
		cell1.put("lable", lable);
		cell1.put("Align对齐模式", Align);
		cell1.put("width", cellwidth22);
		printCell(cell1);
	}

	protected static void printPrice(Object prc, int paddAdj, int priceTitleLen) {
		int priceLastEndIdx = cellwidth / 2 + priceTitleLen / 2 + paddAdj;
		String lefted = StringUtils.leftPad(prc.toString(), priceLastEndIdx);
		System.out.print(StringUtils.rightPad(lefted, cellwidth));
	}

	private static void printCell(Map cell1) {
		int cellwidth = (int) cell1.get("width");
		if (cell1.get("Align对齐模式").toString() == "left") {
			System.out.print(StringUtils.rightPad(cell1.get("lable").toString(), cellwidth));
		} else if (cell1.get("Align对齐模式").toString() == "mid") {
			String lefted = StringUtils.leftPad(cell1.get("lable").toString(),
					cellwidth / 2 + cell1.get("lable").toString().length() / 2);
			System.out.print(StringUtils.rightPad(lefted, cellwidth));
		} else {// right
			System.out.print(StringUtils.leftPad(cell1.get("lable").toString(), cellwidth));
		}

	}

	// 计算单项税务

	private static Object get_item_tax(Object item_total, Object tax_rate_num) {
		if (tax_rate_num == null)
			return 0d;
		return (double) item_total * (double) tax_rate_num;
	}

	// 查询税率 数据表选择运算
	private static Map selectTaxrateFrom_taxRateTable_where_loc_and_itemtype(Object loc, Object itemtype) {

		// 税率表
	  List<Map> taxRateTable = new ArrayList() {
			{
				// loc,sale,type(food)
				add(MapBldr.newx().put("loc地点", "CA").put("type", "other").put("tax rate税率", "9.75%")
						.put("tax_rate_num税率数字格式", 0.0975).build());
				add(MapBldr.newx().put("loc地点", "CA").put("type", "food").put("tax rate税率", "0").build());

				add(MapBldr.newx().put("loc地点", "NY").put("type", "other").put("tax rate税率", " 8.875%")
						.put("tax_rate_num税率数字格式", 0.08875).build());
				add(MapBldr.newx().put("loc地点", "NY").put("type", "food").put("tax rate税率", "0").build());
				add(MapBldr.newx().put("loc地点", "NY").put("type", "cloth").put("tax rate税率", "0").build());

			}
		};
		List<Map> result = taxRateTable.stream().filter(map_item -> {

			return loc.equals(map_item.get("loc地点").toString()) && itemtype.equals(map_item.get("type").toString());
			// return true;

		}).collect(Collectors.toList());
		return result.get(0);

	}

	// 查询物品类型   
	private static Object gettype(Object name) {
		List<Map> itemTypetable = new ArrayList() {
			{
				// loc,sale,type(food)
				add(MapBldr.newx().put("item物品名", "potato chips").put("type", "food").build()

				);
				add(MapBldr.newx().put("item物品名", "shirt").put("type", "cloth").build());
			}
		};
		List<Map> result = itemTypetable.stream().filter(map_item -> {

			return name.equals(map_item.get("item物品名").toString());
			// return true;

		}).collect(Collectors.toList());
		if (result.size() == 0)
			return "other";
		return result.get(0).get("type");
	}

	

 
}
