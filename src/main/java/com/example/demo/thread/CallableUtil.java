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

	public static <T extends Serializable,Bean> Map<T, Bean> callableCaculation(String field, List<Bean> targetList) {
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
				futureList.add(submit);
			}
		}else {
			avgList = targetList;
			futureList.add(pool.submit(callbackFunc(avgList, field)));			
		}
		
		// get result
		for (int k = 0; k < taskSize; k++) {
			Future<Map<T, Bean>> future = futureList.get(k);
			try {
				Boolean flag = future.isDone();
				while ((null == flag) || !flag) {
					Thread.sleep(100);
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

	
	public static <T extends Serializable,Bean> Map<T, Bean> callableCaculation(Callable<Map<T,Bean>> call) {
		Long from = System.currentTimeMillis();
//		int len = targetList.size();
//		int avg = len / taskSize;
		
		List<Future<Map<T, Bean>>> futureList = new ArrayList<>();		
		
		Map<T, Bean> finalResultMap = new HashMap<>();
//		List<Bean> avgList = null;		
//		// exe task
//		if(len>=taskSize) {
//			for (int k = 0; k < taskSize; k++) {
//				avgList = (k != taskSize - 1) ? targetList.subList(k * avg, (k + 1) * avg): targetList.subList(k * avg, len);
//				Future<Map<T, Bean>> submit = pool.submit(call);
//				futureList.add(submit);
//			}
//		}else {
//			avgList = targetList;
//		}
		futureList.add(pool.submit(call));			
		
		// get result
		for (int k = 0; k < taskSize; k++) {
			Future<Map<T, Bean>> future = futureList.get(k);
			try {
				Boolean flag = future.isDone();
				while ((null == flag) || !flag) {
					Thread.sleep(100);
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
//		avgList = null;
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
			

			@Override
			public Map<T, Bean> call() throws Exception {
				Map<T, Bean> map = new HashMap<>();
				for (int i = 0; i < avgList.size(); i++) {
					Bean bean = avgList.get(i);
					T key = ReflectUtil.getTypeField(bean, field);
					map.put(key, bean);
				}
				logger.info("current pool name is :{}",Thread.currentThread().getName());
				return map;
			}
		};
		return call;
	}

	public static void main(String[] args) {

	}

}
