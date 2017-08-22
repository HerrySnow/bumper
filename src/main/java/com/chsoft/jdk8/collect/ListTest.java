package com.chsoft.jdk8.collect;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ListTest {
	
	@Test
	public void streamTest(){
		Map<String, Object> data = Maps.newHashMap();
		List<String> dataList = Lists.newArrayList();
		//parallelStream并行流
		//stream顺序流
		dataList.parallelStream().filter(k->!k.equals("李四")).forEach(k->data.put(k,k));
		
		
		for(int i=0;i<23;i++){
			data.put(i<10?"0"+i:i+"", new Object());
		}
		
		
		//TODO 构建data forearch
		//{"00":"23","05":"334","23":"333"}
		//for
		
		data.putIfAbsent("key","value");
		data.putIfAbsent("key1","value1");
		data.putIfAbsent("key2","value2");
	}
	
}
