/*
 * #%L
 * servo-core
 * %%
 * Copyright (C) 2011 - 2012 Netflix
 * %%
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
 * #L%
 */
package com.netflix.servo.tag;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netflix.servo.tag.BasicTag;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class BasicTagListTest {

    @Test
    public void testCopyOfMap() throws Exception {
        Map<String,String> input = ImmutableMap.of("foo", "bar", "dee", "dum");
        TagList tags = BasicTagList.copyOf(input);
        assertEquals(tags.asMap(), input);
    }

    @Test
    public void testCopyOfIterableString() throws Exception {
        Map<String,String> map = ImmutableMap.of("foo", "bar", "dee", "dum");
        List<String> input = ImmutableList.of("foo=bar", "dee=dum");
        TagList tags = BasicTagList.copyOf(input);
        assertEquals(tags.asMap(), map);
    }

    @Test
    public void testCopyOfVarargString() throws Exception {
        Map<String,String> map = ImmutableMap.of("foo", "bar", "dee", "dum");
        TagList tags = BasicTagList.copyOf("foo=bar", "dee=dum");
        assertEquals(tags.asMap(), map);
    }

    @Test
    public void testCopyOfVarargTag() throws Exception {
        Map<String,String> map = ImmutableMap.of("foo", "bar", "dee", "dum");
        TagList tags = BasicTagList.copyOf(
            new BasicTag("foo", "bar"), new BasicTag("dee", "dum"));
        assertEquals(tags.asMap(), map);
    }

    @Test
    public void testConcatVararg() throws Exception {
        Map<String,String> map = ImmutableMap.of("foo", "bar", "dee", "dum");
        TagList t1 = BasicTagList.copyOf("foo=bar");
        TagList tags = BasicTagList.concat(t1, new BasicTag("dee", "dum"));
        assertEquals(tags.asMap(), map);
    }

    @Test
    public void testConcatTagList() throws Exception {
        Map<String,String> map = ImmutableMap.of("foo", "bar", "dee", "dum");
        TagList t1 = BasicTagList.copyOf("foo=bar");
        TagList t2 = BasicTagList.copyOf("dee=dum");
        TagList tags = BasicTagList.concat(t1, t2);
        assertEquals(tags.asMap(), map);
    }

    @Test
    public void testConcatOverride() throws Exception {
        Map<String,String> map = ImmutableMap.of("foo", "bar2");
        TagList t1 = BasicTagList.copyOf("foo=bar");
        TagList t2 = BasicTagList.copyOf("foo=bar2");
        TagList tags = BasicTagList.concat(t1, t2);
        assertEquals(tags.asMap(), map);
    }

    @Test
    public void testEmpty() throws Exception {
        TagList t1 = BasicTagList.EMPTY;
        assertTrue(t1.isEmpty());
        assertEquals(t1.size(), 0);
    }

    @Test
    public void testAccessors() throws Exception {
        TagList t1 = BasicTagList.copyOf("foo=bar");
        assertTrue(!t1.isEmpty());
        assertEquals(t1.size(), 1);
        assertEquals(t1.getTag("foo"), new BasicTag("foo", "bar"));
        assertEquals(t1.getTag("dee"), null);
        assertTrue(t1.containsKey("foo"));
        assertTrue(!t1.containsKey("dee"));
    }

    @Test
    public void testIterator() throws Exception {
        TagList t1 = BasicTagList.copyOf("foo=bar");
        for (Tag t : t1) {
            assertEquals(t, new BasicTag("foo", "bar"));
        }
    }

    @Test
    public void testCopyTagList() throws Exception {
        Map<String,String> map = ImmutableMap.of("foo", "bar", "dee", "dum");
        BasicTagList t1 = BasicTagList.copyOf("foo=bar");
        BasicTagList t2 = BasicTagList.copyOf("foo=bar2");
        BasicTagList t3 = BasicTagList.copyOf("dee=dum");
        assertEquals(t1.copy(t2), t2);
        assertEquals(t1.copy(t3).asMap(), map);
    }

    @Test
    public void testCopy() throws Exception {
        Map<String,String> map = ImmutableMap.of("foo", "bar", "dee", "dum");
        BasicTagList t1 = BasicTagList.copyOf("foo=bar");
        BasicTagList t2 = BasicTagList.copyOf("foo=bar2");
        assertEquals(t1.copy("foo", "bar2"), t2);
        assertEquals(t1.copy("dee", "dum").asMap(), map);
    }

    @Test
    public void testEquals() throws Exception {
        BasicTagList t1 = BasicTagList.copyOf("foo=bar");
        BasicTagList t2 = BasicTagList.copyOf("foo=bar2");
        BasicTagList t3 = BasicTagList.copyOf("foo=bar");

        assertFalse(t1.equals(null));
        assertFalse(t1.equals(t2.toString()));
        assertTrue(t1.equals(t1));
        assertFalse(t1.equals(t2));
        assertTrue(t1.equals(t3));
    }

    @Test
    public void testHashCode() throws Exception {
        BasicTagList t1 = BasicTagList.copyOf("foo=bar");
        BasicTagList t2 = BasicTagList.copyOf("foo=bar2");
        BasicTagList t3 = BasicTagList.copyOf("foo=bar");

        assertTrue(t1.hashCode() == t1.hashCode());
        assertTrue(t1.hashCode() != t2.hashCode());
        assertTrue(t1.hashCode() == t3.hashCode());
    }
}
