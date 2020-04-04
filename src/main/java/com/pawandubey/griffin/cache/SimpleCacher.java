package com.pawandubey.griffin.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.pawandubey.griffin.Data.fileQueue;
import static com.pawandubey.griffin.Data.tags;

public class SimpleCacher implements Cacher {

	private ConcurrentMap<String, Object> mainMap = new ConcurrentHashMap<>();

	@Override
	public void cacheTaggedParsables() {
		mainMap.put("tags", tags);
	}

	@Override
	public void cacheFileQueue() {
		mainMap.put("fileQueue", fileQueue);
	}

	@Override
	public ConcurrentMap<String, Object> readFromCacheIfExists() {
		return mainMap;
	}

	@Override
	public boolean cacheExists() {
		return !mainMap.isEmpty();
	}
}
