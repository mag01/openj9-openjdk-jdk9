/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 8029240
 * @summary Default methods not always visible under -source 7
 * @library /tools/javac/lib
 * @build ToolBox
 * @run main DefaultMethodsNotVisibileForSource7Test
 */

import java.nio.file.Files;
import java.nio.file.Paths;

public class DefaultMethodsNotVisibileForSource7Test {
    // common definitions

    // this one should be compiled with source 8, the rest with source 7
    static final String ISrc =
        "interface I {\n" +
        "    default void m() {}\n" +
        "}";

    static final String JSrc =
        "interface J extends I {}";

    static final String ASrc =
        "abstract class A implements I {}";

    static final String BSrc =
        "class B implements I {}";

    // test legacy implementations
    static final String C1Src =
        "class C1 implements I {\n" +
        "    @Override public void m() {}\n" +
        "}";

    static final String C2Src =
        "class C2 implements J {\n" +
        "    @Override public void m() {}\n" +
        "}";

    static final String C3Src =
        "class C3 extends A {\n" +
        "    @Override public void m() {}\n" +
        "}";

    static final String C4Src =
        "class C4 extends B {\n" +
        "    @Override public void m() {}\n" +
        "}";

    //test legacy invocations
    static final String LegacyInvocationSrc =
        "class LegacyInvocation {\n" +
        "    public static void test(I i, J j, A a, B b) {\n" +
        "        i.m();\n" +
        "        j.m();\n" +
        "        a.m();\n" +
        "        b.m();\n" +
        "    }\n" +
        "}";

    //test case super invocations
    static final String SubASrc =
        "class SubA extends A {\n" +
        "    public void test() {\n" +
        "        super.m();\n" +
        "    }\n" +
        "}";

    static final String SubBSrc =
        "class SubB extends B {\n" +
        "    public void test() {\n" +
        "        super.m();\n" +
        "    }\n" +
        "}";

    public static void main(String[] args) throws Exception {
        new DefaultMethodsNotVisibileForSource7Test().run();
    }

    void run() throws Exception {
        testsPreparation();
        testLegacyImplementations();
        testLegacyInvocations();
        testSuperInvocations();
    }

    void testsPreparation() throws Exception {
        Files.createDirectory(Paths.get("out"));

        /* as an extra check let's make sure that interface 'I' can't be compiled
         * with source 7
         */
        ToolBox.JavaToolArgs javacArgs =
                new ToolBox.JavaToolArgs(ToolBox.Expect.FAIL)
                .setOptions("-d", "out", "-source", "7")
                .setSources(ISrc);
        ToolBox.javac(javacArgs);

        //but it should compile with source >= 8
        javacArgs =
                new ToolBox.JavaToolArgs()
                .setOptions("-d", "out")
                .setSources(ISrc);
        ToolBox.javac(javacArgs);

        javacArgs =
                new ToolBox.JavaToolArgs()
                .setOptions("-cp", "out", "-d", "out", "-source", "7")
                .setSources(JSrc, ASrc, BSrc);
        ToolBox.javac(javacArgs);
    }

    void testLegacyImplementations() throws Exception {
        //compile C1-4
        ToolBox.JavaToolArgs javacArgs =
                new ToolBox.JavaToolArgs()
                .setOptions("-cp", "out", "-d", "out", "-source", "7")
                .setSources(C1Src, C2Src, C3Src, C4Src);
        ToolBox.javac(javacArgs);
    }

    void testLegacyInvocations() throws Exception {
        //compile LegacyInvocation
        ToolBox.JavaToolArgs javacArgs =
                new ToolBox.JavaToolArgs()
                .setOptions("-cp", "out", "-d", "out", "-source", "7")
                .setSources(LegacyInvocationSrc);
        ToolBox.javac(javacArgs);
    }

    void testSuperInvocations() throws Exception {
        //compile SubA, SubB
        ToolBox.JavaToolArgs javacArgs =
                new ToolBox.JavaToolArgs()
                .setOptions("-cp", "out", "-d", "out", "-source", "7")
                .setSources(SubASrc, SubBSrc);
        ToolBox.javac(javacArgs);
    }
}
