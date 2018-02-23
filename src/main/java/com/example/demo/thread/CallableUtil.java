package com.example.demo.thread;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallableUtil {

	private static final Logger logger = LoggerFactory.getLogger(CallableUtil.class);

	public static final int taskSize = Runtime.getRuntime().availableProcessors();		
	
	public final static ExecutorService pool = Executors.newFixedThreadPool(taskSize);

	/**
	 * @param field javaBean 包含的字段名称
	 * @param targetList 目标集合
	 * @param sleepTime 睡眠时间 1L
	 * @return
	 */
	public static <T extends Serializable,Bean> Map<T, Bean> callableCaculation(String field, List<Bean> targetList,Long sleepTime) {
		Long from = System.currentTimeMillis();
		int len = targetList.size();
		int avg = len / taskSize;
		
		List<Future<Map<T, Bean>>> futureList = new ArrayList<>();		
		
		Map<T, Bean> finalResultMap = new HashMap<>();
		List<Bean> avgList = null;		
		// exe task
		if(len>=taskSize) {
			for (int k = 0; k < taskSize; k++) {
				avgList = (k != taskSize - 1) ? targetList.subList(k * avg, (k + 1) * avg): targetList.subList(k * avg, len);
				Future<Map<T, Bean>> submit = pool.submit(callbackFunc(avgList, field));
				logger.info("任务进行了切割, 任务的大小是: "+ avgList.size());
				futureList.add(submit);
			}
		}else {
			avgList = targetList;
			futureList.add(pool.submit(callbackFunc(avgList, field)));			
			logger.info("任务没有切割,任务的大小是: "+avgList.size() );
		}
		
		// get result
		for (int k = 0; k < taskSize; k++) {
			Future<Map<T, Bean>> future = futureList.get(k);
			try {
				Boolean flag = future.isDone();
				while ((null == flag) || !flag) {
					Thread.sleep(sleepTime);//具体的时间消耗需要特定时间
					flag = future.isDone();
				}
				if (future.isDone() && future.get() != null) {
					finalResultMap.putAll(future.get());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			future = null;
		}
		//pool.shutdown(); ///如果是应对网络请求的并发则不需要关闭pool
		logger.info("pool is terminated {}",pool.isTerminated());
		//销毁对象
		avgList = null;
		futureList = null;
		Long to = System.currentTimeMillis();
		logger.info("[callableCaculation spend time is :{} mm]", (to - from));
		return finalResultMap;
	}

	
	/**
	 * @param call  自定义的Callable对象
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map callableCaculation(Callable<Map> call) {
		Long from = System.currentTimeMillis();
		
		List<Future<Map>> futureList = new ArrayList<>();		
		
		Map finalResultMap = new HashMap<>();
		futureList.add(pool.submit(call));			
		
		// get result
		Future<Map> future = futureList.get(0);
		try {
			Boolean flag = future.isDone();
			while ((null == flag) || !flag) {
				Thread.sleep(1);
				flag = future.isDone();
			}
			if (future.isDone() && future.get() != null) {
				finalResultMap.putAll(future.get());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		future = null;
		futureList = null;
		Long to = System.currentTimeMillis();
		logger.info("[callableCaculation spend time is :{} mm]", (to - from));
		return finalResultMap;
	}

	/**
	 * @param avgList
	 *            计算的切割的List
	 * @param field
	 *            指定的字段名称
	 * @return
	 */
	private static <T extends Serializable,Bean> Callable<Map<T, Bean>> callbackFunc(List<Bean> avgList, String field) {
		Callable<Map<T, Bean>> call = new Callable<Map<T, Bean>>() {
			

			/**
			 * 业务部分需要根据自己的需求自己定义， 这个地方只是将Bean 的  field 和Bean对象对应起来， 封装到map返回
			 * 
			 */
			@Override
			public Map<T, Bean> call() throws Exception {
				Map<T, Bean> map = new HashMap<>();
				for (int i = 0; i < avgList.size(); i++) {
					Bean bean = avgList.get(i);
					T key = ReflectUtil.getTypeField(bean, field);
					map.put(key, bean);
				}
				logger.info("线程的名称："+ Thread.currentThread().getName());
				return map;
			}
		};
		return call;
	}

	public static void main(String[] args) {

	}

}
