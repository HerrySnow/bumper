package com.chsoft.guava;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

public class ListTest {
	/**
	 * guava集合声明 (Map,List,Set)
	 * guava新的集合类型，并没有暴露原始的构造函数
	 * 一般集合
	 */
	@Test
	public void collectState(){
		Map<String,Object> map = Maps.newHashMap();
		List<String> list = Lists.newArrayList();
		Set<String> set = Sets.newHashSet();
		
	}
	/**
	 * MultiMap,MultiSet
	 */
	@Test
	public void MultiCollect(){
		//使用静态工厂声明集合
		Multimap<String, Object> multiMap = HashMultimap.create();
		
		Multiset<Integer> set = HashMultiset.create();
		set.add(10);
		set.add(30);
		set.add(30);
		set.add(40);
		set.count(30);//统计30重复的次数
		set.size();//集合长度
		System.out.println(set.count(30));
		for(Integer s:set){
			System.out.print("Multiset::"+s+"--");//无序的,可存重复的值
		}
		//传统的set集合无法统计某个值重复的次数
		Set<Integer> hashSet = new HashSet<Integer>();
		hashSet.add(10);
		hashSet.add(30);
		hashSet.add(30);
		hashSet.add(40);
		for(Integer s:hashSet){
			System.out.print("Set::"+s+"--");//无序的,不存重复的值
		}
	}
}
