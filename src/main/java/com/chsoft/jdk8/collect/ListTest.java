package com.chsoft.jdk8.collect;

import java.util.List;
import java.util.Map;


import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ListTest {
	
	
	public static void main(String[] args){
		Map<String, Object> data = Maps.newHashMap();
		List<String> dataList = Lists.newArrayList();
		//parallelStream
		dataList.parallelStream().filter(k->!k.equals("李四")).forEach(k->data.put(k,k));
		
		String name="aaa,bbb,ccc  ,ddd,      ,   ,,";
		List<String> splitToList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(name);
		splitToList.forEach(k->System.out.println(k+"============="));
		
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
