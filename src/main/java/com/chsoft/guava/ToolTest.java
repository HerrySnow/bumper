package com.chsoft.guava;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;

public class ToolTest {
	/**
	 * 将字符串以逗号分隔，以及去掉空格Splitter
	 */
	@Test
	public void splitTest(){
		String name="aaa,bbb,ccc  ,ddd,      ,   ,,";
		List<String> splitToList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(name);
		splitToList.forEach(k->System.out.println(k+"============="));
	}
	/**
	 * Optional程序需要使用null表示不存在的情况
	 * 
	 */
	@Test
	public void optainalTest(){
		Optional<Integer> poss = Optional.of(null);
		boolean ispresent = poss.isPresent();
		System.out.println(ispresent==true);
	}
}
