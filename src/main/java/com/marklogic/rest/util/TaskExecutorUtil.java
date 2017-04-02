package com.marklogic.rest.util;

import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ClassUtils;

/**
 * Contains convenience methods for constructing an instance of Spring's ThreadPoolTaskExecutor.
 */
public abstract class TaskExecutorUtil {

	/**
	 * Public so that it can be easily configured, such as setting it to 1 as a bit of a "kill switch" to ensure
	 * operations run synchronously in case any problems arise with parallel requests against the Management API.
	 */
	public static Integer defaultThreadCount = 8;

	public static TaskExecutor newThreadPoolTaskExecutor(Class<?> requestingClass) {
		return newThreadPoolTaskExecutor(defaultThreadCount, requestingClass);
	}

	public static TaskExecutor newThreadPoolTaskExecutor(int threadCount, Class<?> requestingClass) {
		return newThreadPoolTaskExecutor(threadCount, ClassUtils.getShortName(requestingClass) + "-");
	}

	public static TaskExecutor newThreadPoolTaskExecutor(int threadCount, String threadNamePrefix) {
		ThreadPoolTaskExecutor tpte = new ThreadPoolTaskExecutor();
		tpte.setCorePoolSize(threadCount);
		tpte.setWaitForTasksToCompleteOnShutdown(true);
		tpte.setAwaitTerminationSeconds(60 * 60 * 12); // wait up to 12 hours for threads to finish
		tpte.setThreadNamePrefix(threadNamePrefix);
		tpte.afterPropertiesSet();
		return tpte;
	}
}
