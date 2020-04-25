package com.pawandubey.griffin.pipeline;

import com.pawandubey.griffin.model.Page;
import com.pawandubey.griffin.model.Parsable;
import com.pawandubey.griffin.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


public class ContentCollectors {

	//TODO refactor number of posts.

	/**
	 * Finds the N latest posts chronologically to display in the index and
	 * to write their paths in the info file.
	 * Here N is the number of posts per
	 * index page.
	 *
	 * @param collection the queue of Parsables
	 */
	public static List<Parsable> findLatestPosts(List<Parsable> collection, Integer numberOfPosts) {
		return collection.stream()
				.filter(p -> p instanceof Post)
				.sorted((a, b) -> b.getDate().compareTo(a.getDate()))
				.limit(numberOfPosts)
				.collect(Collectors.toList());

	}


	/**
	 * finds all pages tagged as "nav" for addition to the site's navigation.
	 *
	 * @param collection the queue of Parsables
	 */
	public static List<Parsable> findNavigationPages(List<Parsable> collection) {
		return collection.stream()
				.filter(p -> p instanceof Page)
				.filter(p -> p.getTags().contains("nav"))
				.collect(Collectors.toList());
	}

	public static ConcurrentMap<String, List<Parsable>> findTags(List<Parsable> collection) {
		ConcurrentMap<String, List<Parsable>> tags = new ConcurrentHashMap<>();
		for (Parsable p : collection) {
			if (p instanceof Post) {
				List<String> ptags = p.getTags();
				for (String t : ptags) {
					if (!t.equals("nav")) {
						if (tags.get(t) != null) {
							tags.get(t).removeIf(q -> q.getPermalink().equals(p.getPermalink()));
							tags.get(t).add(p);
						} else {
							List<Parsable> l = new ArrayList<>();
							l.add(p);
							tags.put(t, l);
						}
					}
				}
			}
		}
		return tags;
	}

}
