package com.chsoft.guava;

import java.util.List;
import org.junit.Test;
import com.google.common.base.Splitter;

public class ListTest {
	
	@Test
	public void splitTest(){
		String name="aaa,bbb,ccc  ,ddd,      ,   ,,";
		List<String> splitToList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(name);
		splitToList.forEach(k->System.out.println(k+"============="));
	}
	
}
