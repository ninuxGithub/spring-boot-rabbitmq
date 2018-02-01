package com.example.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 多线程调用commonAPI 的解决方案
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitMQAppTests {
	
	private static JsonBinder binder = JsonBinder.buildNormalBinder();
	
	String lighturl= "https://open.hscloud.cn";
	
	public static final int taskSize = 4;		
	
	static Map<String,String> failedMap = new ConcurrentHashMap<>();
	
	public final static ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public static void main(String[] args) {
		System.out.println("开始生产json");
		failedMap.clear();
		new RabbitMQAppTests().saveKlineData();
		for (String key : failedMap.keySet()) {
			System.err.println(key+" 原因" + failedMap.get(key));
		}
		
	}
	
	 @Bean  
     public ThreadPoolTaskExecutor taskExecutor(){  
             ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();  
             pool.setCorePoolSize(5);  
             pool.setMaxPoolSize(10);  
             pool.setWaitForTasksToCompleteOnShutdown(true);  
             return pool;  
     }  

	@SuppressWarnings("unchecked")
	private static List<Map<String,Object>> initData() {
		File file=null;
		try {
			file = ResourceUtils.getFile("classpath:data.json");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		System.out.println(file.exists());
		List<Map<String,Object>> dataList = null;
		
		try {
			FileInputStream in = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(in);
			 BufferedReader bufferedReader = new BufferedReader(reader);
			 StringBuffer sb = new StringBuffer();
			 String content = null;
			 while((content = bufferedReader.readLine()) != null) {
				 sb.append(content);
			 }
			 
			 if(null == sb.toString())
				 System.out.println(JSON.toJSONString(sb.toString(), SerializerFeature.PrettyFormat));
			 
			 reader.close();
			 bufferedReader.close();
			 in.close();
			 dataList = binder.fromJson(sb.toString(),List.class );
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataList;
	}

	@Test
	public void contextLoads() {}
	
	
	
	
	@SuppressWarnings("unchecked")
	public  Map<String, Object> commonCallData(String callUrl) {
		Map<String, Object> resultmap = new HashMap<>();
		String tokenStr = "22AAB1A05FB8478989F1295C803B5B19201801300600006064E5AD";// NXZLB_Token_KEY_STR
		// token不为空和接口中股票为必传项
		if (!tokenStr.equals("")) {
			String token = "bearer " + tokenStr;
			// 创建默认的HttpClient实例
			CloseableHttpClient chc = HttpClients.createDefault();
			// 创建HttpPost实例
			HttpPost hp = new HttpPost(lighturl + callUrl);
			// 设置请求头信息
			hp.setHeader("Authorization", token);
			hp.setHeader("Content-Type", "application/x-www-form-urlencoded");
			// 设置请求参数
			HttpEntity he = new StringEntity("grant_type=client_credentials", "utf-8");
			hp.setEntity(he);
			// 返回数据输入流
			BufferedReader in = null;
			try {
				CloseableHttpResponse res = chc.execute(hp);
				HttpEntity resEntry = res.getEntity();
				in = new BufferedReader(new InputStreamReader(resEntry.getContent(), "UTF-8"));
				String line;
				StringBuffer result = new StringBuffer();
				while ((line = in.readLine()) != null) {
					result.append(line);
				}

				Map<String,Object> map = binder.fromJson(result != null ? result.toString() : null, HashMap.class);
				if (null != map && map.containsKey("error") == false) {
					resultmap = map;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (null != in) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return resultmap;
	}
	
	public void saveKlineData() {
		multiThread2SaveJson("saveKlineDetail", this);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void saveKlineDetail(List<Map<String,Object>> avgList) {
		Map<String, Object> resultMap = new HashMap();
		for(int index=0; index<avgList.size(); index++) {
			Map<String,Object> map = avgList.get(index);
			Integer secuMarket = (Integer) map.get("SecuMarket");
			String preffix = secuMarket==90?".SZ":".SS";
			String secuCode = (String) map.get("SecuCode");
			String fullSecuCode = secuCode + preffix;			
			//开始调用lightService
			Map<String, Object> resmap = new HashMap<>();
			//String subDir = "klineData";
			String fieldsParam = "min_time,open_px,high_px,low_px,close_px";
			String requestUrl = "/quote/v1/kline?prod_code=" + fullSecuCode + "&fields=" + fieldsParam+"&search_direction=1&data_count=30&candle_period=6&get_type=offset";
			Map<String, Object> lightDataMap = commonCallData(requestUrl);
			if (null != lightDataMap && lightDataMap.containsKey("error") == false) {
				if(lightDataMap.get("data")==null) {
					failedMap.put(secuCode,  " data is null");
					continue;
				}
				Map<String, Object> data1 = (Map) lightDataMap.get("data");
				if(data1.get("candle")==null) {
					failedMap.put(secuCode, " candle is null");
					continue;
				}
				Map<String, Object> candle = (Map) data1.get("candle");
				if(candle.get("fields") == null) {
					failedMap.put(secuCode , " fields is null");
					continue;
				}
				List<String> fields = (List<String>) candle.get("fields");
				List<List> klineData = (List<List>) candle.get(fullSecuCode);
				List<Map> listMap = new ArrayList<>();
				
				for(int i=0; i<klineData.size(); i++) {
					List ilist = klineData.get(i);
					Map<String,Object> imap = new HashMap<>();
					for(int j=0; j<ilist.size(); j++) {
						imap.put(fields.get(j), ilist.get(j));
					}
					listMap.add(imap);
					
				}
				resultMap.put("listMap", listMap);
			}	
			resmap.put("klineData", resultMap); //行情数据	
			
			try {
				String jsonStr = binder.toJson(resmap);
				String subPath = "c://test//kline";
			    File dir = new File(subPath);
			    if(!dir.exists()){
			        dir.mkdirs();
			    }
			    File f = new File(subPath,secuCode + ".json");
			    FileOutputStream fos=null;
			    OutputStreamWriter osw = null;
			    try {
			        fos=new FileOutputStream(f);
			        osw = new OutputStreamWriter(fos,"utf-8");
			        osw.write(jsonStr);
			        osw.flush();
			    } catch (IOException e) {
			    	e.printStackTrace();
			    }finally{
			    	try {
			    		if(null != osw){
			    			osw.close();
			    		}
		                if(null != fos){
		                	fos.close();
		                }
		            } catch (IOException e) {
		            }
			    }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	
	
	public void multiThread2SaveJson(String methodName,Object targetBean) {
		List<Map<String,Object>> secuMainList = initData();
		int len = secuMainList.size();
		int avg = len / taskSize;
		List<Future<Void>> futureList = new ArrayList<>();		
		List<Map<String,Object>> avgList = null;		
		if(len>=taskSize) {
			for (int k = 0; k < taskSize; k++) {
				avgList = (k != taskSize - 1) ? secuMainList.subList(k * avg, (k + 1) * avg): secuMainList.subList(k * avg, len);
				Future<Void> future = pool.submit(callbackFunc(avgList, methodName,targetBean));
				futureList.add(future);
			}
		}else {
			avgList = secuMainList;
			Future<Void> future = pool.submit(callbackFunc(avgList,methodName,targetBean));
			futureList.add(future);
		}
		
		for (int k = 0; k < taskSize; k++) {
			try {
				futureList.get(k).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		pool.shutdown();
	}
	
	private  Callable<Void> callbackFunc(List<Map<String,Object>> avgList,String targetMethod, Object thisObj) {
		Callable<Void> call = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Class<?> clazz = thisObj.getClass();
				
				Method[] methods = clazz.getMethods();
				boolean methodDefined = false;
				for (int i = 0; i < methods.length; i++) {
					String methodName = methods[i].getName();
					if(methodName.equals(targetMethod)) {
						methodDefined = true; 
						break;
					}
				}				
				if(methodDefined) {
					Method declaredMethod = clazz.getDeclaredMethod(targetMethod, List.class);
					declaredMethod.invoke(thisObj, avgList);
					System.out.println(Thread.currentThread().getName() + "多线程正在调用"+targetMethod);
				}else {
					System.out.println(targetMethod +"方法在"+thisObj.getClass().getName()+"中没有定义这个方法");
				}				
				return null;
			}
		};
		return call;
	}


}
