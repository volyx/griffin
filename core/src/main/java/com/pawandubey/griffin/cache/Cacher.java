package com.pawandubey.griffin.cache;

import com.pawandubey.griffin.model.Parsable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public interface Cacher {


	static Cacher getCacher() {
//		return new SqliteCacher();
//		return new MVStoreCacherImpl();
//		return new MapDbCacher();
		return new EmptyCacher();
	}

	void cacheTaggedParsables(ConcurrentMap<String, List<Parsable>> tags);

	void cacheFileQueue(List<Parsable> fileQueue);

	boolean cacheExists();

	ConcurrentMap<String, List<Parsable>> getTags();

	List<Parsable> getFileQueue();

	class EmptyCacher implements Cacher {
		@Override
		public void cacheTaggedParsables(ConcurrentMap<String, List<Parsable>> tags) {

		}

		@Override
		public void cacheFileQueue(List<Parsable> fileQueue) {

		}

		@Override
		public boolean cacheExists() {
			return false;
		}

		@Override
		public ConcurrentMap<String, List<Parsable>> getTags() {
			return new ConcurrentHashMap<>();
		}

		@Override
		public List<Parsable> getFileQueue() {
			return Collections.emptyList();
		}
	}
}
