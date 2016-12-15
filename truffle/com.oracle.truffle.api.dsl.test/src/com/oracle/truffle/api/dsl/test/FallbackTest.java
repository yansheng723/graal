/*
 * Copyright (c) 2012, 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.api.dsl.test;

import static com.oracle.truffle.api.dsl.test.TestHelper.array;
import static com.oracle.truffle.api.dsl.test.TestHelper.assertRuns;
import static com.oracle.truffle.api.dsl.test.TestHelper.createRoot;
import static com.oracle.truffle.api.dsl.test.TestHelper.executeWith;

import org.junit.Assert;
import org.junit.Test;

import com.oracle.truffle.api.ExactMath;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.dsl.test.FallbackTestFactory.Fallback1Factory;
import com.oracle.truffle.api.dsl.test.FallbackTestFactory.Fallback2Factory;
import com.oracle.truffle.api.dsl.test.FallbackTestFactory.Fallback3Factory;
import com.oracle.truffle.api.dsl.test.FallbackTestFactory.Fallback4Factory;
import com.oracle.truffle.api.dsl.test.FallbackTestFactory.Fallback6Factory;
import com.oracle.truffle.api.dsl.test.FallbackTestFactory.Fallback7Factory;
import com.oracle.truffle.api.dsl.test.FallbackTestFactory.Fallback8NodeGen;
import com.oracle.truffle.api.dsl.test.TypeSystemTest.TestRootNode;
import com.oracle.truffle.api.dsl.test.TypeSystemTest.ValueNode;
import com.oracle.truffle.api.dsl.test.examples.ExampleTypes;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;

public class FallbackTest {

    private static final Object UNKNOWN_OBJECT = new Object() {
    };

    @Test
    public void testFallback1() {
        assertRuns(Fallback1Factory.getInstance(), //
                        array(42, UNKNOWN_OBJECT), //
                        array("(int)", "(fallback)"));
    }

    /**
     * Test with fallback handler defined.
     */
    @SuppressWarnings("unused")
    @NodeChild("a")
    abstract static class Fallback1 extends ValueNode {

        @Override
        public abstract String executeString(VirtualFrame frame);

        @Specialization
        String f1(int a) {
            return "(int)";
        }

        @Fallback
        String f2(Object a) {
            return "(fallback)";
        }
    }

    @Test
    public void testFallback2() {
        assertRuns(Fallback2Factory.getInstance(), //
                        array(42, UNKNOWN_OBJECT), //
                        array("(int)", UnsupportedSpecializationException.class));
    }

    /**
     * Test without fallback handler defined.
     */
    @SuppressWarnings("unused")
    @NodeChild("a")
    abstract static class Fallback2 extends ValueNode {

        @Specialization
        String f1(int a) {
            return "(int)";
        }

    }

    @Test
    public void testFallback3() {
        assertRuns(Fallback3Factory.getInstance(), //
                        array(42, 43, UNKNOWN_OBJECT, "somestring"), //
                        array("(int)", "(int)", "(object)", "(object)"));
    }

    /**
     * Test without fallback handler and unreachable.
     */
    @SuppressWarnings("unused")
    @NodeChild("a")
    abstract static class Fallback3 extends ValueNode {

        @Specialization
        String f1(int a) {
            return "(int)";
        }

        @Specialization(guards = "notInt(a)")
        String f2(Object a) {
            return "(object)";
        }

        boolean notInt(Object value) {
            return !(value instanceof Integer);
        }

    }

    /**
     * Tests the contents of the {@link UnsupportedSpecializationException} contents in polymorphic
     * nodes.
     */
    @Test
    public void testFallback4() {
        TestRootNode<Fallback4> node = createRoot(Fallback4Factory.getInstance());

        Assert.assertEquals("(int)", executeWith(node, 1));
        Assert.assertEquals("(boolean)", executeWith(node, true));
        try {
            executeWith(node, UNKNOWN_OBJECT);
            Assert.fail();
        } catch (UnsupportedSpecializationException e) {
            Assert.assertEquals(node.getNode(), e.getNode());
            Assert.assertArrayEquals(NodeUtil.findNodeChildren(node.getNode()).subList(0, 1).toArray(new Node[0]), e.getSuppliedNodes());
            Assert.assertArrayEquals(new Object[]{UNKNOWN_OBJECT}, e.getSuppliedValues());
        }
    }

    /**
     * Test without fallback handler and unreachable.
     */
    @SuppressWarnings("unused")
    @NodeChild("a")
    abstract static class Fallback4 extends ValueNode {

        @Specialization
        String f1(int a) {
            return "(int)";
        }

        @Specialization
        String f2(boolean a) {
            return "(boolean)";
        }
    }

    /**
     * Tests the contents of the {@link UnsupportedSpecializationException} contents in monomorphic
     * nodes.
     */
    @Test
    public void testFallback5() {
        TestRootNode<Fallback4> node = createRoot(Fallback4Factory.getInstance());

        Assert.assertEquals("(int)", executeWith(node, 1));
        try {
            executeWith(node, UNKNOWN_OBJECT);
            Assert.fail();
        } catch (UnsupportedSpecializationException e) {
            Assert.assertEquals(node.getNode(), e.getNode());
            Assert.assertArrayEquals(NodeUtil.findNodeChildren(node.getNode()).subList(0, 1).toArray(new Node[0]), e.getSuppliedNodes());
            Assert.assertArrayEquals(new Object[]{UNKNOWN_OBJECT}, e.getSuppliedValues());
        }
    }

    // test without fallback handler and unreachable
    @SuppressWarnings("unused")
    @NodeChild("a")
    abstract static class Fallback5 extends ValueNode {

        @Specialization
        String f1(int a) {
            return "(int)";
        }
    }

    @Test
    public void testFallback6() {
        TestRootNode<Fallback6> node = createRoot(Fallback6Factory.getInstance());
        Assert.assertEquals(2, executeWith(node, 1));
        try {
            Assert.assertEquals(2, executeWith(node, "foobar"));
            Assert.fail();
        } catch (FallbackException e) {
        }

        Assert.assertEquals((long) Integer.MAX_VALUE + (long) Integer.MAX_VALUE, executeWith(node, Integer.MAX_VALUE));
        try {
            executeWith(node, "foobar");
            Assert.fail();
        } catch (FallbackException e) {
        }
    }

    @SuppressWarnings("serial")
    private static class FallbackException extends RuntimeException {
    }

    @NodeChild("a")
    abstract static class Fallback6 extends ValueNode {

        @Specialization(rewriteOn = ArithmeticException.class)
        int f1(int a) throws ArithmeticException {
            return ExactMath.addExact(a, a);
        }

        @Specialization
        long f2(int a) {
            return (long) a + (long) a;
        }

        @Specialization
        boolean f3(boolean a) {
            return a;
        }

        @Fallback
        Object f2(@SuppressWarnings("unused") Object a) {
            throw new FallbackException();
        }
    }

    @Test
    public void testFallback7() {
        TestRootNode<Fallback7> node = createRoot(Fallback7Factory.getInstance());
        Assert.assertEquals(2, executeWith(node, 1));
        Assert.assertEquals(2, executeWith(node, "asdf"));
        Assert.assertEquals(2, executeWith(node, "asdf"));
    }

    @NodeChild("a")
    @SuppressWarnings("unused")
    abstract static class Fallback7 extends ValueNode {

        public abstract Object execute(VirtualFrame frame, Object arg);

        protected boolean guard(int value) {
            return true;
        }

        @Specialization(guards = {"guard(arg)"})
        protected static int access(int arg) {
            return 2;
        }

        @Fallback
        protected static Object access(Object arg) {
            return 2;
        }

    }

    @Test
    public void testFallback8() {
        Fallback8 node = Fallback8NodeGen.create();
        node.execute(1L);
        Assert.assertEquals(0, node.s0count);
        Assert.assertEquals(0, node.s1count);
        Assert.assertEquals(1, node.guard0count);
        Assert.assertEquals(1, node.guard1count);
        Assert.assertEquals(1, node.fcount);
        node.execute(1L);
        Assert.assertEquals(0, node.s0count);
        Assert.assertEquals(0, node.s1count);
        Assert.assertEquals(2, node.guard0count);
        Assert.assertEquals(2, node.guard1count);
        Assert.assertEquals(2, node.fcount);

        node = Fallback8NodeGen.create();
        node.execute(1L);
        Assert.assertEquals(0, node.s0count);
        Assert.assertEquals(0, node.s1count);
        Assert.assertEquals(1, node.guard0count);
        Assert.assertEquals(1, node.guard1count);
        Assert.assertEquals(1, node.fcount);
        node.execute(1);
        Assert.assertEquals(1, node.s0count);
        Assert.assertEquals(0, node.s1count);
        Assert.assertEquals(3, node.guard0count);
        Assert.assertEquals(1, node.guard1count);
        Assert.assertEquals(1, node.fcount);
        node.execute(1L);
        Assert.assertEquals(1, node.s0count);
        Assert.assertEquals(0, node.s1count);
        Assert.assertEquals(4, node.guard0count);
        Assert.assertEquals(2, node.guard1count);
        Assert.assertEquals(2, node.fcount);
    }

    @TypeSystemReference(ExampleTypes.class)
    abstract static class Fallback8 extends Node {

        private int s0count;
        private int s1count;
        private int guard0count;
        private int guard1count;
        private int fcount;

        public abstract Object execute(Object arg);

        @Specialization(guards = "guard0(arg)")
        protected Object s0(Object arg) {
            s0count++;
            return arg;
        }

        @Specialization(guards = "guard1(arg)")
        protected Object s1(Object arg) {
            s1count++;
            return arg;
        }

        protected boolean guard0(Object arg) {
            guard0count++;
            return arg instanceof Integer;
        }

        protected boolean guard1(Object arg) {
            guard1count++;
            return arg instanceof String;
        }

        @Fallback
        protected Object f(Object arg) {
            fcount++;
            return arg;
        }

    }

}
