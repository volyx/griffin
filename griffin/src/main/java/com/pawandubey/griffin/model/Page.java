/*
 * Copyright 2015 Pawan Dubey pawandubey@outlook.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pawandubey.griffin.model;

import com.pawandubey.griffin.Data;
import com.pawandubey.griffin.DirectoryStructure;

import static com.pawandubey.griffin.DirectoryCrawler.EXCERPT_MARKER;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Pawan Dubey pawandubey@outlook.com
 */
public class Page implements Parsable {

    public static final long serialVersionUID = 1L;

    private final String title;
    private final String author;
    private final Path location;
    private String content;
    private String excerpt;
    private String data;
    private final String slug;
    private final String layout;
    private String permalink;
    private final String featuredImage;
    private final List<String> tags;

    /**
     * Creates a Page instance with the parameters.
     *
     * @param titl
     * @param auth
     * @param loc
     * @param cont
     * @param img
     * @param slu
     * @param lay
     * @param tag
     */
    public Page(String titl, String auth, Path loc,
                String cont,
                String data,
                String img, String slu, String lay, List<String> tag) {
        author = auth;
        title = titl;
        location = loc;
        content = cont;
        this.data = data;
        slug = slu;
        layout = lay;
        tags = tag;
        featuredImage = img;
    }

    /**
     * @return the title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * @return the author
     */
    @Override
    public String getAuthor() {
        return author;
    }

    /**
     * @return the location
     */
    @Override
    public Path getLocation() {
        return location;
    }

    /**
     * @return the content
     */
    @Override
    public String getContent() {
        return content;
    }

    /**
     * @return the slug
     */
    @Override
    public String getSlug() {
        if (slug == null || slug.equals(" ")) {
            return String.join("-", title.trim().toLowerCase().split(" "));
        }
        else {
            return String.join("-", slug.trim().toLowerCase().split(" "));
        }
    }

    @Override
    public LocalDate getDate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @return the layout
     */
    @Override
    public String getLayout() {
        return layout;
    }

    /**
     * @return the permalink, as decided by whether the user has specified a
     * custom slug or not. If the slug is not specified, then the permalink is
     * constructed from the post-title
     */
    @Override
    public String getPermalink() {
        Path parentDir = Paths.get(DirectoryStructure.getInstance().SOURCE_DIRECTORY).relativize(location.getParent());
        permalink = Data.config.getSiteBaseUrl().concat("/").concat(parentDir.resolve(getSlug()).toString()).concat("/");
        return permalink;
    }

    /**
     * Sets the content of the page.
     *
     * @param cont the content to be set.
     */
    @Override
    public void setContent(String cont) {
        this.content = cont.replace(EXCERPT_MARKER, "");
        int excInd = cont.indexOf(EXCERPT_MARKER);
//        System.out.println(excInd);
        excerpt = excInd > 0 ? cont.substring(0, excInd) : cont.substring(0, Math.min(cont.length(), 255));
    }

    /**
     *
     * @return the list of tags.
     */
    @Override
    public List<String> getTags() {
        return tags;
    }

    @Override
    public String getBody() {
        return data;
    }

    /**
     *
     * @return the excerpt
     */
    @Override
    public String getExcerpt() {
        return excerpt;
    }

    /**
     *
     * @return the featured image, if exists, for this post.
     */
    @Override
    public Path getFeaturedImage() {
        return featuredImage != null ? Paths.get(featuredImage) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Parsable)) {
            return false;
        }
        Parsable p = (Parsable) o;
        return p.getSlug().equals(this.getSlug());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.getSlug().hashCode();
        return hash;
    }
}
