package com.pawandubey.griffin.cache;

import com.pawandubey.griffin.DirectoryStructure;
import com.pawandubey.griffin.model.Parsable;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.pawandubey.griffin.DirectoryStructure.*;

public class MVStoreCacherImpl implements Cacher {
	public static final String CACHE_PATH = DirectoryStructure.getInstance().ROOT_DIRECTORY + FILE_SEPARATOR + "cache.db";
	private final MVStore mvStore;
	private final MVMap<String, byte[]> mainMap;

	public MVStoreCacherImpl() {

		final Path root = Paths.get(DirectoryStructure.getInstance().ROOT_DIRECTORY);
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
	public void cacheTaggedParsables(ConcurrentMap<String, List<Parsable>> tags) {
		mvStore.openMap("data");
//		mainMap.put("tags", tags);
		mvStore.commit();
	}

	@Override
	public void cacheFileQueue(List<Parsable> fileQueue) {
//		mainMap.put("fileQueue", fileQueue);
		mvStore.commit();
	}

	@Override
	public boolean cacheExists() {
		return !mainMap.isEmpty();
	}

	@Override
	public ConcurrentMap<String, List<Parsable>> getTags() {
//		return (ConcurrentMap<String, List<Parsable>>) mainMap.get("tags");
		return new ConcurrentHashMap<>();
	}

	@Override
	public List<Parsable> getFileQueue() {
//		return (List<Parsable>) mainMap.get("fileQueue");
		return Collections.emptyList();
	}
}
