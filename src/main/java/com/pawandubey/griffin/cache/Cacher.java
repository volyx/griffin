package com.pawandubey.griffin.cache;

import java.util.concurrent.ConcurrentMap;

public interface Cacher {


	static Cacher getCacher() {
		return new MapDbCacher();
//		return new SimpleCacher();
	}

	void cacheTaggedParsables();

	void cacheFileQueue();

	ConcurrentMap<String, Object> readFromCacheIfExists();

	boolean cacheExists();
}
