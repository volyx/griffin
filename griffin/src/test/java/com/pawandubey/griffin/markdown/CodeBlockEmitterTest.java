/*
 * Copyright 2015 Pawan Dubey.
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
package com.pawandubey.griffin.markdown;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.github.rjeschke.txtmark.BlockEmitter;
import com.threecrickets.jygments.contrib.InplaceClassHtmlFormatter;
import com.threecrickets.jygments.contrib.InplaceStyleHtmlFormatter;
import com.threecrickets.jygments.style.Style;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Pawan Dubey pawandubey@outlook.com
 */
@Disabled
class CodeBlockEmitterTest {

    public static final String CODE = "class Solution {\n" +
            "    public int countSubstrings(String s) {\n" +
            "        int n = s.length();\n" +
            "\n" +
            "        int[][] a = new int[n][n];\n" +
            "        int count = 0;        \n" +
            "        for (int i = 0; i < n; i++) {\n" +
            "            a[i][i] = 1;\n" +
            "            count++;\n" +
            "        }\n" +
            "\n" +
            "\n" +
            "        for (int col = 1; col < n; col++) {\n" +
            "            for (int row = 0; row < col; row++) {\n" +
            "                if (row == col - 1 && s.charAt(col) == s.charAt(row)) {\n" +
            "                    a[row][col] = 1;\n" +
            "                    count++;\n" +
            "                } else if (a[row + 1][col - 1] == 1 && s.charAt(col) == s.charAt(row) ) {\n" +
            "                     a[row][col] = 1;\n" +
            "                    count++;\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "\n" +
            "        return count;\n" +
            "    }\n" +
            "}";

    public CodeBlockEmitterTest() {
    }

    /**
     * Test of emitBlock method, of class CodeBlockEmitter.
     */
//    @Test
    public void testEmitBlock() {
        StringBuilder out = new StringBuilder();
        String code = "public static void main(String[] args){"
                      + System.lineSeparator()
                      + "System.out.println(\"Hello World\")"
                      + System.lineSeparator()
                      + "}";
        String expected = "<pre><code class= \"java\">public static void main(String[] args){"
                          + System.lineSeparator()
                          + "System.out.println(\"Hello World\")"
                          + System.lineSeparator()
                          + "}"
                          + System.lineSeparator()
                          + "</code></pre>"
                          + System.lineSeparator();
        List<String> list = Arrays.asList(code.split(System.lineSeparator()));
        String meta = "java";
        CodeBlockEmitter instance = new CodeBlockEmitter();
        instance.emitBlock(out, list, meta);
        assertEquals(expected, out.toString());
    }

    @Test
    public void testJygmentsEmitBlock() {
        StringBuilder out = new StringBuilder();
        String code = "public static void main(String[] args){"
                      + System.lineSeparator()
                      + "System.out.println(\"Hello World\")"
                      + System.lineSeparator()
                      + "}";
        String expected = "<div><pre>\n" +
                "<span class=\"nf\">public static void </span><span class=\"nf\">main</span><span class=\"o\">(</span><span class=\"n\">String</span><span class=\"o\">[</span><span class=\"o\">]</span> <span class=\"n\">args</span><span class=\"o\">)</span><span class=\"o\">{</span>\n" +
                "<span class=\"n\">System</span><span class=\"o\">.</span><span class=\"na\">out</span><span class=\"o\">.</span><span class=\"na\">println</span><span class=\"o\">(</span><span class=\"s\">&quot;Hello World&quot;</span><span class=\"o\">)</span>\n" +
                "<span class=\"o\">}</span>\n" +
                "</pre></div>\n";
        List<String> list = Arrays.asList(code.split(System.lineSeparator()));
        String meta = "java";
        BlockEmitter instance = new JygmentsCodeEmitter(new InplaceStyleHtmlFormatter());
        instance.emitBlock(out, list, meta);
        System.out.println(out.toString());
        assertEquals(expected, out.toString());
    }


    @Test
    public void testStyleJygmentsEmitBlock() throws IOException {
        StringBuilder out = new StringBuilder();
        String code = "class Solution {\n" +
                "    public int countSubstrings(String s) {\n" +
                "        int n = s.length();\n" +
                "\n" +
                "        int[][] a = new int[n][n];\n" +
                "        int count = 0;        \n" +
                "        for (int i = 0; i < n; i++) {\n" +
                "            a[i][i] = 1;\n" +
                "            count++;\n" +
                "        }\n" +
                "\n" +
                "\n" +
                "        for (int col = 1; col < n; col++) {\n" +
                "            for (int row = 0; row < col; row++) {\n" +
                "                if (row == col - 1 && s.charAt(col) == s.charAt(row)) {\n" +
                "                    a[row][col] = 1;\n" +
                "                    count++;\n" +
                "                } else if (a[row + 1][col - 1] == 1 && s.charAt(col) == s.charAt(row) ) {\n" +
                "                     a[row][col] = 1;\n" +
                "                    count++;\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        return count;\n" +
                "    }\n" +
                "}";

        List<String> list = Arrays.asList(code.split(System.lineSeparator()));
        String meta = "java";
        BlockEmitter instance = new JygmentsCodeEmitter(new InplaceStyleHtmlFormatter());
        instance.emitBlock(out, list, meta);

        Path path = new File("src/test/resources/styles.html").toPath();
        Files.write(path, out.toString().getBytes());
    }


    @Test
    void testClassJygmentsEmitBlock() throws IOException {
        StringBuilder out = new StringBuilder();

        List<String> list = Arrays.asList(CODE.split(System.lineSeparator()));
        String meta = "java";
        BlockEmitter instance = new JygmentsCodeEmitter(new InplaceClassHtmlFormatter());
        instance.emitBlock(out, list, meta);
        Path path = new File("src/test/resources/classes.html").toPath();
        Files.write(path, out.toString().getBytes());
        Assertions.assertNotNull(instance);
    }

    @Test
    void testStyleJygmentsEmitBlockMonokai() throws IOException {
        final StringBuilder out = new StringBuilder();
        final List<String> list = Arrays.asList(CODE.split(System.lineSeparator()));
        final String meta = "java";
        final BlockEmitter instance = new JygmentsCodeEmitter(new InplaceStyleHtmlFormatter(Style.getByName( "monokai")));
        instance.emitBlock(out, list, meta);
        final Path path = new File("src/test/resources/monokai.html").toPath();
        Files.write(path, out.toString().getBytes());
        Assertions.assertNotNull(instance);
    }

}
