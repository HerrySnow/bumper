package com.chsoft.jdk8.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.junit.Assert;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import com.sun.xml.internal.ws.policy.PolicyIntersector;

public class Retrograde {
	
	static ExecutorService threadPool = Executors.newFixedThreadPool(10);
	static AtomicInteger counter = new AtomicInteger(0);
	static AtomicDouble total = new AtomicDouble(0.0);
	CountDownLatch latch = new CountDownLatch(100);
	
	public static void main(String[] args) throws Exception{
//		String str = reader("e://xm.txt");
//		System.out.println(str);
//		parseJson(str,116.473326,39.935052);
//		System.out.println(planningPath());
		List<Point> list = new ArrayList<Point>();
		for(int i=0;i<20;i++){
			Point p = new Point("2016-08-"+(i<10?("0"+i):i)+" 15:23:23",30,40);
			list.add(p);
		}
		Date startDate=DateUtil.parseDate("2016-08-03");
		Date endDate=DateUtil.parseDate("2016-08-10");
		
		List<Point> result = list.stream().filter(point->between(point.getDateTime(),startDate,endDate))
				.collect(Collectors.toList());
		
		result.forEach(points -> {
			int index = counter.getAndDecrement();
			Future<Double> submit = threadPool.submit(new Callable<Double>() {
				@Override
				public Double call() throws Exception {
					Stream<Point> limit = result.stream().skip(index).limit(2);
					Point start = limit.findFirst().get();
					Point end = limit.skip(1).findFirst().get();
					return juli(start,end);
				} 
			});
			try {
				double juli = submit.get();
				total.addAndGet(juli);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		total.doubleValue();
		//101.222222222|34.5555555
		
		//101.422222234|34.5555567
		
		//0.000000001|0.0000002
		/*if ((Math.abs(d1 - d2) <= delta)) {
            return false;
        }*/
	}
	
	private static boolean between(String pointDate,Date startDate,Date endDate) {
		Date target = null;
		try {
			target = DateUtil.parseDate(pointDate);
		} catch (DateParseException e) {
			e.printStackTrace();
		}
		return target.after(startDate) && target.before(endDate);
	}
	
	private static double juli(Point start,Point end){
		//post
//		latch.countDown();
		return 0;
	}
	
	/**
	 * ��ȡtxt�ļ�����
	 * @return
	 */
	 public static String reader(String filePath) {
	        try {
	            File file = new File(filePath);
	            if (file.isFile() && file.exists()) {
	                InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
	                BufferedReader bufferedReader = new BufferedReader(read);
	                String lineTxt = bufferedReader.readLine();
	                while (lineTxt != null) {
	                    return lineTxt;
	                }
	            }
	        } catch (Exception e) {
	            System.out.println("Cannot find the file specified!");
	            e.printStackTrace();
	        }
	        return null;
	    }
	/**
	 * �����ı����ݵ�json�ַ���
	 * @param txt
	 */
	public static List<Map<String, Object>> parseJson(String txtStr,double lon,double lat){
		JSONObject json = JSONObject.parseObject(txtStr);
		JSONArray datas = json.getJSONObject("detail").getJSONArray("pointList");
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < datas.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            JSONObject obj = datas.getJSONObject(i);
            String datetime = obj.getString("time");
            String longitude = obj.getString("longitude");
            String latitude =  obj.getString("latitude");
            
            map.put("time", datetime);
            map.put("longitude", longitude);
            map.put("latitude", latitude);
            
            list.add(map);
            
        }
        System.out.println(list.size());
        return list;
	}
	/**
	 * ���㳵���켣��ʻ�ܾ���
	 * @return
	 */
	public static double calcMovingDistance(List<Map<String,Object>> list){
	
		String url ="http://api.map.baidu.com/routematrix/v2/driving?output=json&origins=40.45,116.34|40.54,116.35&destinations=40.34,116.45|40.35,116.46&ak=AEf8c2dd0939f70c6b26fb56183edd09";
		int sum =0;
		for(int i=0;i<list.size();i++){
			
		}
		return 0;
	}
	/**
	 * ��ȡ�滮�켣��
	 */
	public static String planningPath(){
		// ��Ҫ���ʵĽӿ�·��
		String url = "http://api.map.baidu.com/direction/v2/transit?origin=40.056878,116.30815&destination=31.222965,121.505821&ak=AEf8c2dd0939f70c6b26fb56183edd09";
		String data = null;  
	    //����HttpClient��ʵ��    
	    HttpClient httpClient = new HttpClient();    
	    //����GET������ʵ��    
	    GetMethod getMethod = new GetMethod(url);   
	    //����ͷ��Ϣ�����������User-Agent���ܻᱨ405������ȡ��������  
	    getMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:39.0) Gecko/20100101 Firefox/39.0"); 
	    httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
	    //ʹ��ϵͳ�ṩ��Ĭ�ϵĻָ�����    
	    getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());    
	    try{  
	        //��ʼִ��getMethod    
	        int statusCode = httpClient.executeMethod(getMethod);  
	        if (statusCode != HttpStatus.SC_OK) {  
	            System.err.println("Method failed:" + getMethod.getStatusLine());  
	        }  
	        //��ȡ����  
	        byte[] responseBody = getMethod.getResponseBody();  
	        //��������  
	        data = new String(responseBody,"utf-8");  
	    }catch (HttpException e){  
	        //�����쳣��������Э�鲻�Ի��߷��ص�����������  
	        System.out.println("Please check your provided http address!");  
	        data = "";  
	        e.printStackTrace();  
	    }catch(IOException e){  
	        //���������쳣  
	        data = "";  
	        e.printStackTrace();  
	    }finally{  
	        //�ͷ�����  
	        getMethod.releaseConnection();  
	    }  
	    return data;  
	}
	/** 
     * ���䣺��������֮����ʵ���� 
     * @return �� 
     */  
    public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {  
        // ά��  
        double lat1 = (Math.PI / 180) * latitude1;  
        double lat2 = (Math.PI / 180) * latitude2;  
  
        // ����  
        double lon1 = (Math.PI / 180) * longitude1;  
        double lon2 = (Math.PI / 180) * longitude2;  
  
        // ����뾶  
        double R = 6378.137;  
        
        
  
        // �������� km�������Ҫ�׵Ļ������*1000�Ϳ�����  
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;  
  
        return d * 1000;  
    }
    /**
     * �������2
     * @param lat_a
     * @param lng_a
     * @param lat_b
     * @param lng_b
     * @return
     */
    public static double getDistance2(double lat_a, double lng_a, double lat_b, double lng_b){
        double pk = 180 / 3.14169;
        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;
        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);
        return 6371000 * tt;
    }
}
