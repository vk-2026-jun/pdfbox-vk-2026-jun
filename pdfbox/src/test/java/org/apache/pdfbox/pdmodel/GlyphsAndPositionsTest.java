/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.pdmodel;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GlyphsAndPositionsTest {

    @Test
    public void testPositionsThenGlyphs() {
        GlyphsAndPositions gap = new GlyphsAndPositions();
        gap.add(10.0f); // position
        gap.add(12.0f); // position
        gap.add(65);    // glyph 'A'
        gap.add(66);    // glyph 'B'

        Object[] arr = gap.toArray();
        assertEquals(4, arr.length);
        assertTrue(arr[0] instanceof Float);
        assertTrue(arr[1] instanceof Float);
        assertTrue(arr[2] instanceof Integer);
        assertTrue(arr[3] instanceof Integer);

        List<Object> list = gap.asList();
        assertEquals(4, list.size());
        assertEquals(10.0f, (Float) list.get(0));
        assertEquals(12.0f, (Float) list.get(1));
        assertEquals(65, (Integer) list.get(2));
        assertEquals(66, (Integer) list.get(3));
    }

    @Test
    public void testGlyphsThenPositions() {
        GlyphsAndPositions gap = new GlyphsAndPositions();
        gap.add(65);
        gap.add(66);
        gap.add(-100.0f);
        gap.add(67);

        Object[] arr = gap.toArray();
        assertEquals(4, arr.length);
        assertTrue(arr[0] instanceof Integer);
        assertTrue(arr[1] instanceof Integer);
        assertTrue(arr[2] instanceof Float);
        assertTrue(arr[3] instanceof Integer);

        assertEquals(65, arr[0]);
        assertEquals(66, arr[1]);
        assertEquals(-100.0f, (Float) arr[2]);
        assertEquals(67, arr[3]);
    }

    @Test
    public void testMixedSequencesAndClearSizeEmpty() {
        GlyphsAndPositions gap = new GlyphsAndPositions();
        assertTrue(gap.isEmpty());
        gap.add(1);
        gap.add(2.5f);
        gap.add(2);
        assertFalse(gap.isEmpty());
        assertEquals(3, gap.size());

        gap.clear();
        assertTrue(gap.isEmpty());
        assertEquals(0, gap.size());
    }
}
