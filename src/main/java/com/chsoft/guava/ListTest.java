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
	 * guava�������� (Map,List,Set)
	 * guava�µļ������ͣ���û�б�¶ԭʼ�Ĺ��캯��
	 * һ�㼯��
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
		//ʹ�þ�̬������������
		Multimap<String, Object> multiMap = HashMultimap.create();
		
		Multiset<Integer> set = HashMultiset.create();
		set.add(10);
		set.add(30);
		set.add(30);
		set.add(40);
		set.count(30);//ͳ��30�ظ��Ĵ���
		set.size();//���ϳ���
		System.out.println(set.count(30));
		for(Integer s:set){
			System.out.print("Multiset::"+s+"--");//�����,�ɴ��ظ���ֵ
		}
		//��ͳ��set�����޷�ͳ��ĳ��ֵ�ظ��Ĵ���
		Set<Integer> hashSet = new HashSet<Integer>();
		hashSet.add(10);
		hashSet.add(30);
		hashSet.add(30);
		hashSet.add(40);
		for(Integer s:hashSet){
			System.out.print("Set::"+s+"--");//�����,�����ظ���ֵ
		}
	}
}
