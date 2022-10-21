/*
 * @Author: error: git config user.name && git config user.email & please set dead value or install git
 * @Date: 2022-07-22 13:29:20
 * @LastEditors: error: git config user.name && git config user.email & please set dead value or install git
 * @LastEditTime: 2022-07-22 14:51:14
 * @FilePath: /workspace/IPRecord.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class IPRecord {
	private static final String regex = "\\s+";
	private static final int limit = 4;   // split方法的这个参数的作用很值得深入学习一下
	private static final String ipRegex = "\\.";
	
	private static class Record {
		String ipStartStr;
		String ipEndStr;
		long ipStart;
		long ipEnd;
		String area;
		String location;
		
		public Record(String[] record) {
			this.ipStartStr = record[0];
			this.ipEndStr = record[1];
			this.ipStart = ip2Long(record[0]);   // 将ip地址字符串转换成long
			this.ipEnd = ip2Long(record[1]);     // 将ip地址字符串转换成long
			this.area = record[2];
			this.location = record[3];
		}
		
		@Override
		public String toString() {
			return ipStart + "," + ipEnd + "," + area + "," + location + "," + ipStartStr + "," + ipEndStr;  // 转成csv格式
		}
		
		public String toRedis() {
			return "ZADD\tqqwry\t" + ipEnd + "\t" + "{\"ipstart\":" + ipStart 
					+ ",\"ipend\":" + ipEnd
					+ ",\"area\":" + "\"" + area + "\""
					+ ",\"location\":" + "\"" + location + "\""
					+ ",\"ipstartstr\":" + "\"" + ipStartStr + "\""
					+ ",\"ipstartend\":" + "\"" + ipEndStr
					+ "\"}";
		}
	}
	
	public static void main(String[] args) throws IOException {
		// qqwry.tx 的文件路径
		Path path = Paths.get("C:/Users/Administrator/Desktop/qqwry.txt");
		// 使用指定字符集（UTF-8）读取文件
		List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
    //  输出测试，注意不要省略 limit，因为总共有 52万+行数据，直接输出的话，压力比较大。
//		lines.stream().map(IPRecord::split).limit(10).map(Record::toJson).forEach(System.out::println);
		// 将文件转为csv文件存放
		
		long start = System.currentTimeMillis();
		lines = lines.stream()
				     .map(IPRecord::split)
				     .map(Record::toString)
				     .limit(530737)     // 有效数据行数，剩下的是一些空行和最后的统计信息
				     .collect(Collectors.toList());
		
	    String csvHeader = "StartIPNum,EndIPNum,Country,Local,StartIPText,EndIPText\n";
		Files.write(Paths.get("C:/Users/Administrator/Desktop/qqwry.csv"), csvHeader.getBytes(), StandardOpenOption.CREATE);            //写入csv文件的头部
		Files.write(Paths.get("C:/Users/Administrator/Desktop/qqwry.csv"), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);  //写入csv文件的主体部分
		System.out.println("Execute Success! Time: " + (System.currentTimeMillis()-start) + " ms");
		
	}
	
	public static Record split(String record) {
		return new Record(record.split(regex, limit));
	}
	
	/**
	 * int类型是32位的，但是无法直接将ip和int类型进行转换，因为可能会溢出。
	 * int类型是有符号的，除非这里有无符号整型，但是Java是没有这个的，所以
	   *    这里需要转换成long型。
	 * */
	public static long ip2Long(String ip) {
		String[] strs = ip.split(ipRegex);
		int[] bs = new int[4];
		for (int i = 0; i < 4; i++) {
			bs[i] = Integer.parseInt(strs[i]);  // Byte.parseByte(strs[i]); 这个方法转成的字节范围是 -128-127，真是太坑了！
		}
		return ((bs[3] & 0xFFL)      ) +        // 这个转换方法是从jdk源码里面偷来的，挺好用的！
	               ((bs[2] & 0xFFL) <<  8) +
	               ((bs[1] & 0xFFL) << 16) +
	               ((bs[0] & 0xFFL) << 24);
	}
}