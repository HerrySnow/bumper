package com.chsoft.guava;

import java.util.List;

import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;

public class ToolTest {
	/**
	 * ���ַ����Զ��ŷָ����Լ�ȥ���ո�Splitter
	 */
	@Test
	public void splitTest(){
		String name="aaa,bbb,ccc  ,ddd,      ,   ,,";
		List<String> splitToList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(name);
		splitToList.forEach(k->System.out.println(k+"============="));
	}
	/**
	 * Optional������Ҫʹ��null��ʾ�����ڵ����
	 * 
	 */
	@Test
	public void optainalTest(){
		Optional<Integer> poss = Optional.of(null);
		boolean ispresent = poss.isPresent();
		System.out.println(ispresent==true);
	}
}
