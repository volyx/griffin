package com.pawandubey.griffin.cache;

import com.pawandubey.griffin.model.Parsable;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public interface Cacher {


	static Cacher getCacher() {
		return new SqliteCacher();
//		return new MVStoreCacherImpl();
//		return new MapDbCacher();
//		return new SimpleCacher();
	}

	void cacheTaggedParsables(ConcurrentMap<String, List<Parsable>> tags);

	void cacheFileQueue(List<Parsable> fileQueue);

	boolean cacheExists();

	ConcurrentMap<String, List<Parsable>> getTags();

	List<Parsable> getFileQueue();
}
