package com.pawandubey.griffin.cache;

import com.pawandubey.griffin.DirectoryStructure;
import com.pawandubey.griffin.model.Content;
import com.pawandubey.griffin.model.Parsable;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.pawandubey.griffin.DirectoryStructure.FILE_SEPARATOR;

public class SqliteCacher implements Cacher {

	public static final String CACHE_PATH = DirectoryStructure.getInstance().ROOT_DIRECTORY + FILE_SEPARATOR + "cache.db";
	private final String url;

	public SqliteCacher() {
		this.url = CACHE_PATH;
		createSchema(CACHE_PATH);
	}

	public SqliteCacher(String path) {
		this.url = path;
		createSchema(path);
	}

	private static void createSchema(String url) {
		final DataSource dc = createDatasource(url);
		try (Connection connection = dc.getConnection();
			 Statement stmt = connection.createStatement()) {

			String sql = "CREATE TABLE IF NOT EXISTS CONTENT " +
					"(FILENAME TEXT PRIMARY KEY NOT NULL," +
					" CONTENT  TEXT NOT NULL) WITHOUT ROWID";
			stmt.executeUpdate(sql);

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	public void printTables() {
		final DataSource dc = createDatasource(url);
		try (Connection connection = dc.getConnection();
			 Statement stmt = connection.createStatement()) {
			System.out.println("filename, content");
			System.out.println("=========================================");
			try (ResultSet rs = stmt.executeQuery("SELECT filename, content FROM CONTENT")) {
				while (rs.next()) {
					final String filename = rs.getString("filename");
					final String content = rs.getString("content");

					System.out.println(filename);
					System.out.println(content);
					System.out.println("--------------------------------------------");
				}

			}
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		try (Connection connection = dc.getConnection();
			 Statement stmt = connection.createStatement()) {
			System.out.println("tag, filename");
			System.out.println("=========================================");
			try (ResultSet rs = stmt.executeQuery("SELECT tag, filename FROM TAG")) {
				while (rs.next()) {
					final String tag = rs.getString("tag");
					final String filename = rs.getString("filename");

					System.out.println(tag);
					System.out.println(filename);
					System.out.println("--------------------------------------------");
				}

			}
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	private static SQLiteDataSource createDatasource(String url) {
		final SQLiteDataSource dc = new SQLiteDataSource();
		dc.setUrl("jdbc:sqlite:" + url);
		return dc;
	}

	@Override
	public void cacheTaggedParsables(ConcurrentMap<String, List<Parsable>> tags) {
		final DataSource dc = createDatasource(url);
		String sql = "INSERT INTO TAG(tag,filename) VALUES(?,?)";
		try (Connection connection = dc.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql)) {
			connection.setAutoCommit(false);
			for (Map.Entry<String, List<Parsable>> entry : tags.entrySet()) {
				for (Parsable parsable : entry.getValue()) {
					stmt.setString(1, entry.getKey());
					stmt.setString(2, parsable.getLocation());
					stmt.addBatch();
				}
			}

			stmt.execute();

			connection.commit();

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public void cacheFileQueue(List<Parsable> fileQueue) {
		final DataSource dc = createDatasource(url);
		String sql = "INSERT INTO CONTENT(filename,content) VALUES(?,?)";
		try (Connection connection = dc.getConnection();
			 PreparedStatement stmt = connection.prepareStatement(sql)) {

			connection.setAutoCommit(false);

			for (Parsable parsable : fileQueue) {
				stmt.setString(1, parsable.getLocation());
				stmt.setString(2, parsable.getData());
				stmt.addBatch();
			}
			stmt.execute();

			connection.commit();

		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	@Override
	public boolean cacheExists() {
		return false;
	}

	@Override
	public ConcurrentMap<String, List<Parsable>> getTags() {
		final ConcurrentHashMap<String, List<Parsable>> map = new ConcurrentHashMap<>();
		final DataSource dc = createDatasource(url);
		final Map<String, Parsable> fileQueueMap = getFileQueueMap();
		try (Connection connection = dc.getConnection();
			 Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery("SELECT tag, filename FROM TAG")) {
				while (rs.next()) {
					final String filename = rs.getString("filename");
					final String tag = rs.getString("tag");
					map.compute(tag, (tag1, parsables) -> {
						if (parsables == null) {
							return Arrays.asList(fileQueueMap.get(filename));
						}
						parsables.add(fileQueueMap.get(filename));
						return parsables;
					});
				}
			}
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		return map;
	}

	private Map<String, Parsable> getFileQueueMap() {
		final DataSource dc = createDatasource(url);
		final Map<String, Parsable> parsables = new HashMap<>();
		try (Connection connection = dc.getConnection();
			 Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery("SELECT filename, content FROM CONTENT")) {
				while (rs.next()) {
					final String filename = rs.getString("filename");
					Parsable parsable = Content.parse(filename, rs.getString("content"));
					parsables.put(filename, parsable);
				}
			}
		} catch (SQLException e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		return parsables;
	}

	@Override
	public List<Parsable> getFileQueue() {
		return new ArrayList<>(getFileQueueMap().values());
	}
}
