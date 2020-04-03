package com.pawandubey.griffin.cache;

import java.util.concurrent.ConcurrentMap;

public class SimpleCacher implements Cacher {
	@Override
	public void cacheTaggedParsables() {

	}

	@Override
	public void cacheFileQueue() {

	}

	@Override
	public ConcurrentMap<String, Object> readFromCacheIfExists() {
		return null;
	}

	@Override
	public boolean cacheExists() {
		return false;
	}
}
