package com.example.twitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 单例模式的线程池工具类
 * 
 * @author hughes
 * 
 */
public class ThreadPoolUtil {
	private static ExecutorService instance = null;

	public ThreadPoolUtil() {

	}

	public static ExecutorService getInstance() {
		if (instance == null) {
			instance = Executors.newCachedThreadPool();
		}
		return instance;
	}
}
