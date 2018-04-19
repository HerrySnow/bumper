package com.chsoft.jdk8.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.AtomicDouble;
import com.chsoft.jdk8.driver.PlanningScheme;
import com.chsoft.jdk8.driver.Points;

public class Test {
	private static double EARTH_RADIUS = 6378.137;
	static CountDownLatch latch = null;
    static AtomicDouble totalDistance = new AtomicDouble(0.0);
    static ExecutorService executorService = Executors.newFixedThreadPool(10);
	public static void main(String[] args) throws Exception{
		//��ȡ�û���ʵ�ʹ켣
		String str = reader("e://xm.txt");
		System.out.println(str);
		//������ʻ�켣
		List<Points> list = parseJson(str);
		//ָ�����ڵĹ켣
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date start = sdf.parse("2018-04-19 07:46:00");//�޷��ж�
		Date end = sdf.parse("2018-04-19 07:48:00");
//		Date start = sdf.parse("2018-04-19 08:40:00");//�����ж�
//		Date end = sdf.parse("2018-04-19 08:42:00");
//		Date start = sdf.parse("2018-04-19 08:48:00");//����
//		Date end = sdf.parse("2018-04-19 08:50:00");
		
		List<Points> filterList = filterPoint(list,start,end);
		
		//��ʻ�켣����
		Double ridingDistance = calcDistance(filterList);
		//��ʻ�켣�Ŀ�ʼ��ͽ�����
		Map<String,Object> map = findPoint(filterList);
		Points startPoint = (Points)map.get("first");
		Points endPoint = (Points)map.get("last");
		String url ="http://api.map.baidu.com/direction/v2/riding?origin="+startPoint.getLatitude()+","+startPoint.getLongitude()+"&destination="+endPoint.getLatitude()+","+endPoint.getLongitude()+"&ak=AEf8c2dd0939f70c6b26fb56183edd09";
		//��������ʱ����������
		long interval = countSeconds(startPoint.getTime(), endPoint.getTime());
		//����ʱ����������Ƿ�ȱʧ
		int count = (int) (interval/5);
		if(Math.abs(count-filterList.size())>6){
			System.out.println("==============����ȱʧ==============");
			return;
		}
		
		//�滮·��
		String planStr = planningPath(url);
		//�滮·��
		List<PlanningScheme> planList = parsePlan(planStr);
		//����滮·��
		String reverseUrl ="http://api.map.baidu.com/direction/v2/riding?origin="+endPoint.getLatitude()+","+endPoint.getLongitude()+"&destination="+startPoint.getLatitude()+","+startPoint.getLongitude()+"&ak=AEf8c2dd0939f70c6b26fb56183edd09";
		//����滮·��
		String reversePlanStr = planningPath(reverseUrl);
		//����滮·��
		List<PlanningScheme> reversePlanList = parsePlan(reversePlanStr);
		judgeRetrograde(ridingDistance,planList,reversePlanList.get(0).getDistance(),planStr,filterList);
	}
	/**
	 * 1.��ȡtxt�ļ�����
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
	 * 2.�����ı����ݵ�json�ַ���,���ڼ������Զ���洢
	 * @param txt
	 * @throws ParseException 
	 * @throws NumberFormatException 
	 */
	public static List<Points> parseJson(String txtStr) throws Exception{
		JSONObject json = JSONObject.parseObject(txtStr);
		JSONArray datas = json.getJSONObject("detail").getJSONArray("pointList");
        List<Points> list = new ArrayList<Points>();
        for (int i = 0; i < datas.size(); i++) {
            JSONObject obj = datas.getJSONObject(i);
            String datetime = obj.getString("time");
            String longitude = obj.getString("longitude");
            String latitude =  obj.getString("latitude");
            Points p = new Points(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datetime),Double.valueOf(longitude),Double.valueOf(latitude));
            list.add(p);
        }
        return list;
	}
	/**
	 * 3.��������Ŀ�ʼʱ��ͽ���ʱ����˳����ʱ��Ĺ켣��
	 * @param points���еĹ켣��
	 * @param start ��ʼʱ��
	 * @param end ����ʱ��
	 * @return
	 */
	public static List<Points> filterPoint(List<Points> points,Date start,Date end){
		if(points.size()>0){
			//�Ȱ�ʱ��˳����������������֮����밴��ʱ��Ĵ�С�������򣬷�������Ļ�����������ܵľ�����������ģ�
			//�������List�ǰ�ʱ���С��������ģ���ʼ�ڵ���ǵ�һ���������ڵ�������һ��
			List<Points> filterPoints = points.stream().filter(point->between(point.getTime(),start,end))
					.sorted(Comparator.comparing(item->item.getTime())).collect(Collectors.toList());
			return filterPoints;
		}else{
			return null;
		}
	}
	/**
	 * 4.������ʻ�켣��ȡ��ʼ��ͽ�����
	 * @return
	 */
	public static Map<String,Object> findPoint(List<Points> points){
		//�Ƚ���ʻ�켣������
		Collections.sort(points, (a,b)->a.getTime().compareTo(b.getTime()));
		//����켣�Ŀ�ʼ��
		Points first = points.stream().findFirst().get();
		//����켣�Ľ�����
		Points last = points.get(points.size()-1);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("first", first);
		map.put("last", last);
		return map;
	}
	/**
	 * ˽�з��������Ƿ���ĳ��ʱ��η�Χ��
	 * @param target
	 * @param start
	 * @param end
	 * @return
	 */
	private static boolean between(Date target,Date start,Date end){
		return target.after(start)&&target.before(end);
	}
	/**
	 * 5.������ʻ�켣���ܾ���
	 * @return
	 * @throws InterruptedException 
	 */
	public static double calcDistance(List<Points> list) throws InterruptedException{
//		AtomicDouble totalDistance = new AtomicDouble(0.0);
//		for (int i = 0; i < list.size()-1; i++) {
//        	List<Points> collect = list.stream().skip(i).limit(2).collect(Collectors.toList());
//            Points p1 = collect.stream().findFirst().get();
//            Points p2 = collect.stream().skip(1).findFirst().get();
//            Double distance = travelDistance(p1.getLongitude(),p1.getLatitude(),p2.getLongitude(),p2.getLatitude()) ;
//            totalDistance.addAndGet(distance);
//		}
		
		int loop = list.size()-1;
	    latch = new CountDownLatch(loop);

        for (int i = 0; i < loop; i++) {
            List<Points> collect = list.stream().skip(i).limit(2).collect(Collectors.toList());
            Points item0 = collect.stream().findFirst().get();
            Points item1 =collect.stream().skip(1).findFirst().get();
            //��Ҫ��������ľ���,ʹ���̳߳���ʵ��

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    double distance = travelDistance(item0.getLongitude(),item0.getLatitude(),item1.getLongitude(),item1.getLatitude());
                    //ÿ�μ������ԭ���Եĺϲ����ܵľ������
                    totalDistance.addAndGet(distance);
                }
            });
        }
        //�������߳�ִ����ɲ�����ִ��
        latch.await();
        double total = totalDistance.doubleValue();//��������ܵľ���
		return total;
	}
	
	/**
	 * 6.���ݹ켣�Ŀ�ʼ��ͽ������ȡ��ȡ�滮·��
	 * @param startPoint ��ʻ�켣�Ŀ�ʼ��
	 * @param endPoint ��ʻ�켣�Ľ�����
	 * @return
	 */
	public static String planningPath(String url){
		// ��Ҫ���ʵĽӿ�·��
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
	 * 7.���滮·�����ܾ������--���еĹ滮·��
	 * @param planStr
	 * @return
	 */
	public static List<PlanningScheme> parsePlan(String planStr){
		JSONObject json = JSONObject.parseObject(planStr);
		JSONArray datas = json.getJSONObject("result").getJSONArray("routes");//�滮·��
		//�滮·��
		List<PlanningScheme> planList = new ArrayList<PlanningScheme>();
		
        if(datas.size()>0){
        	for(int i=0;i<datas.size();i++){
        		JSONObject obj = datas.getJSONObject(i);
        		Double distance = Double.valueOf(obj.get("distance")+"");
        		PlanningScheme ps = new PlanningScheme();
        		ps.setDistance(distance);
        		planList.add(ps);
        	}
	        return planList;
        }else{
        	return null;
        }
	}
	/**
	 * 9.���ȡ����ʵ�ʹ켣��������Ƿ񿿽��滮·��
	 */
	public static double nearPlan(String planStr,List<Points> point,double debat,AtomicInteger index){
		JSONObject json = JSONObject.parseObject(planStr);
		JSONArray datas = json.getJSONObject("result").getJSONArray("routes");//�滮·��
        JSONObject obj = datas.getJSONObject(index.intValue());
        
        JSONArray step = obj.getJSONArray("steps");
        List<Points> list = new ArrayList<Points>();
        for(int i=0;i<point.size();i++){
          Points p = point.get(i);
	      for(int j=0;j<step.size();j++){
	    	JSONObject s = step.getJSONObject(j);
	    	if(s.get("path")!=null){
	    		String[] location = s.get("path").toString().split(";");
	    		if(location.length>0){
	    			for(int k=0;k<location.length-1;k++){
	    				double space = pointToLine(p.getLongitude(),p.getLatitude(),Double.valueOf(location[k].split(",")[0]),Double.valueOf(location[k].split(",")[1]),
	    						Double.valueOf(location[k+1].split(",")[0]),Double.valueOf(location[k+1].split(",")[1]));
	    				if(space<debat){
	    					list.add(p);
	    				}
	    			}
	    		}
	    	}
	      }
        }
        return list.size()/point.size();
	}
	/**
	 * �ж��Ƿ������������
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x0
	 * @param y0
	 * @return
	 */
	public static boolean ifAcuteAngle(double x1, double y1, double x2, double y2, double x0, double y0){
	    double ab = travelDistance(x1, y1, x2, y2);
	    double ac = travelDistance(x1, y1, x0, y0);
	    double bc = travelDistance(x0, y0, x2, y2);
	    return  ((ab*ab +ac*ac - bc*bc )> 0)&&((ab*ab +bc*bc - ac*ac) >0) ;
	}

	/**
	 * �㵽ֱ����̾��� ---���������
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x0
	 * @param y0
	 * @return
	 */
	public static double pointToLine(double x1, double y1, double x2, double y2, double x0, double y0){
	    double space = 0;
	    double a, b, c;
	    a = travelDistance(x1, y1, x2, y2);// �߶εĳ���
	    b = travelDistance(x1, y1, x0, y0);// (x1,y1)����ľ���
	    c = travelDistance(x2, y2, x0, y0);// (x2,y2)����ľ���
	    if (c <= 0.000001 || b <= 0.000001) {
	        space = 0;
	        return space;
	    }
	    if (a <= 0.000001) {
	        space = b;
	        return space;
	    }
	    if (c * c >= a * a + b * b) {
	        space = b;
	        return space;
	    }
	    if (b * b >= a * a + c * c) {
	        space = c;
	        return space;
	    }
	    double p = (a + b + c) / 2;// ���ܳ�
	    double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// ���׹�ʽ�����
	    space = 2 * s / a;// ���ص㵽�ߵľ��루���������������ʽ��ߣ�
	    return space;
	}
	
	/**
	 * ͨ����γ�Ȼ�ȡ����(��λ����)
	 * @param lat1 ά��
	 * @param lng1 ����
	 * @param lat2
	 * @param lng2
	 * @return
	 */
	public static double travelDistance(double lng1, double lat1, double lng2,
	                                 double lat2) {
	    double radLat1 = rad(lat1);
	    double radLat2 = rad(lat2);
	    double a = radLat1 - radLat2;
	    double b = rad(lng1) - rad(lng2);
	    double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
	            + Math.cos(radLat1) * Math.cos(radLat2)
	            * Math.pow(Math.sin(b / 2), 2)));
	    s = s * EARTH_RADIUS;
	    s = Math.round(s * 10000d) / 10000d;
	    s = s*1000;
	    latch.countDown();
	    return s;
	}
	private static double rad(double d) {
	    return d * Math.PI / 180.0;
	}
	/**
	 * ��������ʱ����������
	 * @return
	 */
	public static long countSeconds(Date start,Date end) throws Exception{
	   long interval = (end.getTime() - start.getTime())/1000;
		   
	   System.out.println("����ʱ�����"+interval+"��");//���ӡ�����3��
	   return interval;
	}
	static AtomicInteger index = new AtomicInteger(0);
	/**
	 * �ж������Ƿ�����
	 */
	public static void judgeRetrograde(double ridingDistance,List<PlanningScheme> planList,Double reverseDistance,String planStr,List<Points> filterList){
		if(index.intValue()<planList.size()){
			System.out.println(index.intValue());
			PlanningScheme p = planList.get(index.intValue());
			index.getAndIncrement();
			//ʵ�ʹ켣С�ڹ滮�켣
			if(ridingDistance-p.getDistance()<0.1){
				if(Math.abs(p.getDistance()-reverseDistance)<0.1){
					System.out.println("����");
				}else if(Math.abs(p.getDistance()-reverseDistance)>0.1){
					System.out.println("����");
				}
			}else if(ridingDistance-p.getDistance()==0.1){//ʵ�ʹ滮�ӽ��滮·��
				double d = nearPlan(planStr,filterList,15,index);
				if(d>=0.6){
					System.out.println("˳��");
				}else{
					if(index.intValue()<planList.size()){
						judgeRetrograde(ridingDistance,planList,reverseDistance,planStr,filterList);
					}else{
						System.out.println("================�����ж�=================");
					}
				}
			}else if(ridingDistance-p.getDistance()>0.1){
				if(index.intValue()<planList.size()){
					judgeRetrograde(ridingDistance,planList,reverseDistance,planStr,filterList);
				}else{
					System.out.println("==============�����ж�===================");
				}
			}
			
		}
		
	}
}
