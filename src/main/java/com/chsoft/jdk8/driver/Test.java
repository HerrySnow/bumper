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
		//获取用户的实际轨迹
		String str = reader("e://xm.txt");
		System.out.println(str);
		//解析行驶轨迹
		List<Points> list = parseJson(str);
		//指定日期的轨迹
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date start = sdf.parse("2018-04-19 07:46:00");//无法判断
		Date end = sdf.parse("2018-04-19 07:48:00");
//		Date start = sdf.parse("2018-04-19 08:40:00");//不能判断
//		Date end = sdf.parse("2018-04-19 08:42:00");
//		Date start = sdf.parse("2018-04-19 08:48:00");//穿行
//		Date end = sdf.parse("2018-04-19 08:50:00");
		
		List<Points> filterList = filterPoint(list,start,end);
		
		//行驶轨迹长度
		Double ridingDistance = calcDistance(filterList);
		//行驶轨迹的开始点和结束点
		Map<String,Object> map = findPoint(filterList);
		Points startPoint = (Points)map.get("first");
		Points endPoint = (Points)map.get("last");
		String url ="http://api.map.baidu.com/direction/v2/riding?origin="+startPoint.getLatitude()+","+startPoint.getLongitude()+"&destination="+endPoint.getLatitude()+","+endPoint.getLongitude()+"&ak=AEf8c2dd0939f70c6b26fb56183edd09";
		//计算两个时间相差的秒数
		long interval = countSeconds(startPoint.getTime(), endPoint.getTime());
		//连续时间段内数据是否缺失
		int count = (int) (interval/5);
		if(Math.abs(count-filterList.size())>6){
			System.out.println("==============数据缺失==============");
			return;
		}
		
		//规划路径
		String planStr = planningPath(url);
		//规划路径
		List<PlanningScheme> planList = parsePlan(planStr);
		//反向规划路径
		String reverseUrl ="http://api.map.baidu.com/direction/v2/riding?origin="+endPoint.getLatitude()+","+endPoint.getLongitude()+"&destination="+startPoint.getLatitude()+","+startPoint.getLongitude()+"&ak=AEf8c2dd0939f70c6b26fb56183edd09";
		//反向规划路径
		String reversePlanStr = planningPath(reverseUrl);
		//反向规划路径
		List<PlanningScheme> reversePlanList = parsePlan(reversePlanStr);
		judgeRetrograde(ridingDistance,planList,reversePlanList.get(0).getDistance(),planStr,filterList);
	}
	/**
	 * 1.读取txt文件内容
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
	 * 2.解析文本内容的json字符串,放在集合中以对象存储
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
	 * 3.根据输入的开始时间和结束时间过滤出这段时间的轨迹点
	 * @param points所有的轨迹点
	 * @param start 开始时间
	 * @param end 结束时间
	 * @return
	 */
	public static List<Points> filterPoint(List<Points> points,Date start,Date end){
		if(points.size()>0){
			//先按时间顺序排序，这里过滤完成之后必须按照时间的从小到大排序，否则乱序的话计算出来的总的距离是有问题的，
			//所以这个List是按时间从小到大排序的，开始节点就是第一个，结束节点就是最后一个
			List<Points> filterPoints = points.stream().filter(point->between(point.getTime(),start,end))
					.sorted(Comparator.comparing(item->item.getTime())).collect(Collectors.toList());
			return filterPoints;
		}else{
			return null;
		}
	}
	/**
	 * 4.根据行驶轨迹获取开始点和结束点
	 * @return
	 */
	public static Map<String,Object> findPoint(List<Points> points){
		//先将行驶轨迹点排序
		Collections.sort(points, (a,b)->a.getTime().compareTo(b.getTime()));
		//输出轨迹的开始点
		Points first = points.stream().findFirst().get();
		//输出轨迹的结束点
		Points last = points.get(points.size()-1);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("first", first);
		map.put("last", last);
		return map;
	}
	/**
	 * 私有方法计算是否在某个时间段范围内
	 * @param target
	 * @param start
	 * @param end
	 * @return
	 */
	private static boolean between(Date target,Date start,Date end){
		return target.after(start)&&target.before(end);
	}
	/**
	 * 5.计算行驶轨迹的总距离
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
            //需要计算两点的距离,使用线程池来实现

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    double distance = travelDistance(item0.getLongitude(),item0.getLatitude(),item1.getLongitude(),item1.getLatitude());
                    //每次计算完成原子性的合并到总的距离变量
                    totalDistance.addAndGet(distance);
                }
            });
        }
        //等所有线程执行完成才往下执行
        latch.await();
        double total = totalDistance.doubleValue();//这个就是总的距离
		return total;
	}
	
	/**
	 * 6.根据轨迹的开始点和结束点获取获取规划路径
	 * @param startPoint 行驶轨迹的开始点
	 * @param endPoint 行驶轨迹的结束点
	 * @return
	 */
	public static String planningPath(String url){
		// 需要访问的接口路径
		String data = null;  
	    //构造HttpClient的实例    
	    HttpClient httpClient = new HttpClient();    
	    //创建GET方法的实例    
	    GetMethod getMethod = new GetMethod(url);   
	    //设置头信息：如果不设置User-Agent可能会报405，导致取不到数据  
	    getMethod.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:39.0) Gecko/20100101 Firefox/39.0"); 
	    httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
	    //使用系统提供的默认的恢复策略    
	    getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());    
	    try{  
	        //开始执行getMethod    
	        int statusCode = httpClient.executeMethod(getMethod);  
	        if (statusCode != HttpStatus.SC_OK) {  
	            System.err.println("Method failed:" + getMethod.getStatusLine());  
	        }  
	        //读取内容  
	        byte[] responseBody = getMethod.getResponseBody();  
	        //处理内容  
	        data = new String(responseBody,"utf-8");  
	    }catch (HttpException e){  
	        //发生异常，可能是协议不对或者返回的内容有问题  
	        System.out.println("Please check your provided http address!");  
	        data = "";  
	        e.printStackTrace();  
	    }catch(IOException e){  
	        //发生网络异常  
	        data = "";  
	        e.printStackTrace();  
	    }finally{  
	        //释放连接  
	        getMethod.releaseConnection();  
	    }  
	    return data;  
	}
	/**
	 * 7.将规划路径的总距离距离--骑行的规划路径
	 * @param planStr
	 * @return
	 */
	public static List<PlanningScheme> parsePlan(String planStr){
		JSONObject json = JSONObject.parseObject(planStr);
		JSONArray datas = json.getJSONObject("result").getJSONArray("routes");//规划路径
		//规划路线
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
	 * 9.随机取几个实际轨迹的坐标点是否靠近规划路径
	 */
	public static double nearPlan(String planStr,List<Points> point,double debat,AtomicInteger index){
		JSONObject json = JSONObject.parseObject(planStr);
		JSONArray datas = json.getJSONObject("result").getJSONArray("routes");//规划路径
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
	 * 判断是否是锐角三角形
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
	 * 点到直线最短距离 ---锐角三角形
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
	    a = travelDistance(x1, y1, x2, y2);// 线段的长度
	    b = travelDistance(x1, y1, x0, y0);// (x1,y1)到点的距离
	    c = travelDistance(x2, y2, x0, y0);// (x2,y2)到点的距离
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
	    double p = (a + b + c) / 2;// 半周长
	    double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// 海伦公式求面积
	    space = 2 * s / a;// 返回点到线的距离（利用三角形面积公式求高）
	    return space;
	}
	
	/**
	 * 通过经纬度获取距离(单位：米)
	 * @param lat1 维度
	 * @param lng1 经度
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
	 * 计算两个时间相差的秒数
	 * @return
	 */
	public static long countSeconds(Date start,Date end) throws Exception{
	   long interval = (end.getTime() - start.getTime())/1000;
		   
	   System.out.println("两个时间相差"+interval+"秒");//会打印出相差3秒
	   return interval;
	}
	static AtomicInteger index = new AtomicInteger(0);
	/**
	 * 判断骑行是否逆行
	 */
	public static void judgeRetrograde(double ridingDistance,List<PlanningScheme> planList,Double reverseDistance,String planStr,List<Points> filterList){
		if(index.intValue()<planList.size()){
			System.out.println(index.intValue());
			PlanningScheme p = planList.get(index.intValue());
			index.getAndIncrement();
			//实际轨迹小于规划轨迹
			if(ridingDistance-p.getDistance()<0.1){
				if(Math.abs(p.getDistance()-reverseDistance)<0.1){
					System.out.println("逆行");
				}else if(Math.abs(p.getDistance()-reverseDistance)>0.1){
					System.out.println("穿行");
				}
			}else if(ridingDistance-p.getDistance()==0.1){//实际规划接近规划路径
				double d = nearPlan(planStr,filterList,15,index);
				if(d>=0.6){
					System.out.println("顺行");
				}else{
					if(index.intValue()<planList.size()){
						judgeRetrograde(ridingDistance,planList,reverseDistance,planStr,filterList);
					}else{
						System.out.println("================不能判断=================");
					}
				}
			}else if(ridingDistance-p.getDistance()>0.1){
				if(index.intValue()<planList.size()){
					judgeRetrograde(ridingDistance,planList,reverseDistance,planStr,filterList);
				}else{
					System.out.println("==============不能判断===================");
				}
			}
			
		}
		
	}
}
