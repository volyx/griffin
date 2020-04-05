package com.pawandubey.griffin.cache;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentMap;

import static com.pawandubey.griffin.Data.fileQueue;
import static com.pawandubey.griffin.Data.tags;
import static com.pawandubey.griffin.DirectoryCrawler.FILE_SEPARATOR;
import static com.pawandubey.griffin.DirectoryCrawler.ROOT_DIRECTORY;

public class MVStoreCacherImpl implements Cacher {
	public static final String CACHE_PATH = ROOT_DIRECTORY + FILE_SEPARATOR + "cache.db";
	private final MVStore mvStore;
	private final MVMap<String, Object> mainMap;

	public MVStoreCacherImpl() {

		final Path root = Paths.get(ROOT_DIRECTORY);
		if (!Files.exists(root)) {
			try {
				Files.createDirectory(root);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		mvStore = new MVStore.Builder().
				fileName(CACHE_PATH).
//				.fileStore(new FileStore()).
//				encryptionKey("007".toCharArray()).
				compress().
				open();

		mainMap = mvStore.openMap("data");

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			mvStore.close();
			System.err.println("mvStore.close()");
		}));

	}

	@Override
	public void cacheTaggedParsables() {
		mvStore.openMap("data");
		mainMap.put("tags", tags);
		mvStore.commit();
	}

	@Override
	public void cacheFileQueue() {
		mainMap.put("fileQueue", fileQueue);
		mvStore.commit();
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
