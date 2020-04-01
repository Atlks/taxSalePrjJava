import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class PrintCellUtill {
	// 格式化展示 表格式cell展示数据
	// static int widthChar = 100;
	public	static int cellwidth = 20;
	public	static int cellwidth2 = 30;
	public static void printCell(Object lable, String Align) {
		Map cell1 = new HashMap();
		cell1.put("lable", lable);
		cell1.put("Align对齐模式", Align);
		cell1.put("width", cellwidth);
		printCell(cell1);
	}

	public static void printCell(Object lable, String Align, int cellwidth22) {
		Map cell1 = new HashMap();
		cell1.put("lable", lable);
		cell1.put("Align对齐模式", Align);
		cell1.put("width", cellwidth22);
		printCell(cell1);
	}

	public static void printPriceByMidtitle(Object prc, int paddAdj, int mdishowTitle) {
		int priceLastEndIdx = cellwidth / 2 + mdishowTitle / 2 + paddAdj;
		String lefted = StringUtils.leftPad(prc.toString(), priceLastEndIdx);
		System.out.print(StringUtils.rightPad(lefted, cellwidth));
	}

	public static void printCell(Map cell1) {
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


}
