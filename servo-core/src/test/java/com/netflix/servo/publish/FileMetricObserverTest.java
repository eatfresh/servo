/*
 * #%L
 * servo
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
package com.netflix.servo.publish;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.Metric;
import com.netflix.servo.tag.Tag;
import com.netflix.servo.tag.TagList;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.testng.Assert.*;

public class FileMetricObserverTest {

    private final TagList TAGS =
        BasicTagList.copyOf("cluster=foo", "zone=a", "node=i-123");

    private List<Metric> mkList(int v) {
        ImmutableList.Builder<Metric> builder = ImmutableList.builder();
        for (int i = 0; i < v; ++i) {
            builder.add(new Metric("m", TAGS, 0L, i));
        }
        return builder.build();
    }

    private void delete(File f) throws IOException {
        if (f.exists() && !f.delete()) {
            throw new IOException("could not delete " + f);
        }
    }

    private void deleteRecursively(File f) throws IOException {
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                deleteRecursively(file);
            }
            delete(f);
        } else {
            delete(f);
        }
    }

    private void checkLine(int i, String line) {
        String[] parts = line.split("\t");
        assertEquals(parts.length, 4);
        assertEquals(parts[0], "m");
        for (Tag tag : TAGS) {
            String tagStr = tag.getKey() + "=" + tag.getValue();
            assertTrue(parts[1].indexOf(tagStr) != -1, "missing " + tagStr);
        }
        assertEquals(parts[2], "1970-01-01T00:00:00.000");
        assertEquals(Integer.parseInt(parts[3]), i);
    }

    private void checkFile(File f) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(f));
        try {
            int i = 0;
            String line;
            while ((line = in.readLine()) != null) {
                checkLine(i, line);
                ++i;
            }
        } finally {
            in.close();
        }
    }

    @Test
    public void testUpdate() throws Exception {
        File dir = Files.createTempDir();
        try {
            MetricObserver fmo = new FileMetricObserver("test", dir);
            fmo.update(mkList(1));
            Thread.sleep(250);
            fmo.update(mkList(2));
            Thread.sleep(250);
            fmo.update(mkList(3));

            File[] files = dir.listFiles();
            assertEquals(files.length, 3);
            for (File f : files) {
                checkFile(f);
            }
        } finally {
            deleteRecursively(dir);
        }
    }
}
