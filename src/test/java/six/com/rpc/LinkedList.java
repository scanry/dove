package six.com.rpc;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**   
 * @author sixliu   
 * @date   2017年12月13日 
 * @email  359852326@qq.com  
 * @Description 
 */
public class LinkedList {

	public static void main(String[] args) {
		java.util.LinkedList<String> list =new java.util.LinkedList<String>();
		System.out.println(20>>1);
		list.add("");
		list.add("");
		list.add("");
		list.add("");
		list.get(0);
		list.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// TODO Auto-generated method stub
				return 0;
			}
		});
		Map<String,String> map=new HashMap<String, String>();
		map.put("", "");
	}

}

