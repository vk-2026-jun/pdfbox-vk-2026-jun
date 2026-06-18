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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlyphLayoutFontLoaderAwt
 */
public class GlyphLayoutFontLoaderAwtTest {

    private GlyphLayoutFontLoaderAwt fontLoader;

    @BeforeEach
    public void setUp() {
        fontLoader = new GlyphLayoutFontLoaderAwt();
    }

    @Test
    public void testCacheInitiallyEmpty() {
        assertEquals(0, fontLoader.getCachedFontCount());
    }

    @Test
    public void testClearFontCache() {
        fontLoader.clearFontCache();
        assertEquals(0, fontLoader.getCachedFontCount());
    }

    @Test
    public void testEmptyFontStreamThrowsException() {
        assertThrows(Exception.class, () -> {
            ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);
            fontLoader.loadFont(new PDDocument(), emptyStream, true);
        });
    }

    @Test
    public void testNullInputStreamThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            fontLoader.loadFont(new PDDocument(), null, true);
        });
    }

    @Test
    public void testNullPDDocumentThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ByteArrayInputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});
            fontLoader.loadFont(null, stream, true);
        });
    }

    @Test
    public void testFontOptionsBuilder() {
        GlyphLayoutFontLoaderAwt.FontOptions options = new GlyphLayoutFontLoaderAwt.FontOptions()
                .setKerningOn()
                .setLigaturesOn();
        
        assertNotNull(options.getTextAttributes());
        assertFalse(options.getTextAttributes().isEmpty());
    }

    @Test
    public void testFontOptionsChaining() {
        GlyphLayoutFontLoaderAwt.FontOptions options = new GlyphLayoutFontLoaderAwt.FontOptions()
                .setKerningOn();
        
        // Verify it returns the same instance for chaining
        GlyphLayoutFontLoaderAwt.FontOptions options2 = options.setLigaturesOn();
        assertSame(options, options2);
    }
}
