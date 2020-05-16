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

import java.util.Arrays;
import java.util.List;

import com.github.rjeschke.txtmark.BlockEmitter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Pawan Dubey pawandubey@outlook.com
 */
public class CodeBlockEmitterTest {

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
        BlockEmitter instance = new JygmentsCodeEmitter();
        instance.emitBlock(out, list, meta);
        System.out.println(out.toString());
        assertEquals(expected, out.toString());
    }

}
