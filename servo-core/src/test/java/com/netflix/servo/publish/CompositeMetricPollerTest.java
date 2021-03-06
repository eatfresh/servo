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
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.Metric;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.netflix.servo.tag.BasicTagList.*;
import static com.netflix.servo.publish.BasicMetricFilter.*;
import static org.testng.Assert.*;

public class CompositeMetricPollerTest {

    private List<Metric> mkList() {
        return ImmutableList.of(
            new Metric("m1", EMPTY, 0L, 0),
            new Metric("m2", BasicTagList.copyOf("c=a.b.c.d.M1"), 0L, 0),
            new Metric("m3", BasicTagList.copyOf("c=a.b.c.c.M3"), 0L, 0),
            new Metric("m4", BasicTagList.copyOf("c=a.b.c.d.M4"), 0L, 0),
            new Metric("m5", BasicTagList.copyOf("c=a.a.a.a.M5"), 0L, 0)
        );
    }

    private MetricPoller newPoller(long delay) {
        MockMetricPoller poller = new MockMetricPoller();
        poller.setMetrics(mkList());
        poller.setDelay(delay);
        return poller;
    }

    @Test
    public void testBasic() throws Exception {
        Map<String,MetricPoller> pollers = Maps.newHashMap();
        pollers.put("p1", newPoller(0));

        ExecutorService exec = Executors.newFixedThreadPool(1);
        MetricPoller poller = new CompositeMetricPoller(pollers, exec, 10000);

        assertEquals(poller.poll(MATCH_ALL), mkList());
    }

    @Test
    public void testMultiple() throws Exception {
        Map<String,MetricPoller> pollers = Maps.newHashMap();
        pollers.put("p1", newPoller(0));
        pollers.put("p2", newPoller(0));

        ExecutorService exec = Executors.newFixedThreadPool(1);
        MetricPoller poller = new CompositeMetricPoller(pollers, exec, 10000);

        assertEquals(poller.poll(MATCH_ALL),
            ImmutableList.copyOf(Iterables.concat(mkList(), mkList())));
    }

    @Test
    public void testTimeout() throws Exception {
        Map<String,MetricPoller> pollers = Maps.newHashMap();
        pollers.put("p1", newPoller(120000));
        pollers.put("p2", newPoller(0));

        ExecutorService exec = Executors.newFixedThreadPool(1);
        MetricPoller poller = new CompositeMetricPoller(pollers, exec, 500);

        assertEquals(poller.poll(MATCH_ALL), mkList());
    }

    @Test
    public void testException() throws Exception {
        MockMetricPoller mock = new MockMetricPoller();
        mock.setDie(true);

        Map<String,MetricPoller> pollers = Maps.newHashMap();
        pollers.put("p1", mock);
        pollers.put("p2", newPoller(0));

        ExecutorService exec = Executors.newFixedThreadPool(1);
        MetricPoller poller = new CompositeMetricPoller(pollers, exec, 10000);

        assertEquals(poller.poll(MATCH_ALL), mkList());
    }
}
