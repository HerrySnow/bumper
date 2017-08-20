package com.chsoft.echarts;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

public class EchartsTest {
    
	
	public void setUp(){
		Map<String, Object> data = Maps.newHashMap();
		for(int i=0;i<23;i++){
			data.put(i<10?"0"+i:i+"", new Object());
		}
		
		//TODO ¹¹½¨data forearch
		//{"00":"23","05":"334","23":"333"}
		//for
		
		data.putIfAbsent("key","value");
		data.putIfAbsent("key1","value1");
		data.putIfAbsent("key2","value2");
	}
	
	
}
