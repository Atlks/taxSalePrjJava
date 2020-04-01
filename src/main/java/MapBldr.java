import java.util.Map;

import com.google.common.collect.Maps;

public class MapBldr {
	public static void main(String[] args) {
		//System.out.println(MapBldr.put("item物品名", "book").put("qty数量", 1).put("price价格", 17.99).build());
	}
	
	   Map m=Maps.newLinkedHashMap();
//	static MapBldr  MapBldr1=new MapBldr();

	public   MapBldr put(String k, Object v) {
		m.put(k,v);
		return this;
	}

	public Map build() {
	 
		return this.m;
	}

	public static MapBldr newx() {
		MapBldr MapBldr1=new MapBldr();
		return MapBldr1;
	}

}
