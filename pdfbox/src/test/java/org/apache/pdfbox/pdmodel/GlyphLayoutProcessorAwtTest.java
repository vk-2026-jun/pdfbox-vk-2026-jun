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

import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Font;
import java.awt.FontFormatException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlyphLayoutProcessorAwt
 */
public class GlyphLayoutProcessorAwtTest {

    private GlyphLayoutProcessorAwt processor;

    @BeforeEach
    public void setUp() {
        processor = new GlyphLayoutProcessorAwt();
    }

    @Test
    public void testNullTextThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            processor.showText(null, null, 12f, null);
        });
    }

    @Test
    public void testEmptyTextThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Create mock objects for testing
            ContentStreamForGlyphLayoutInterface mockStream = new MockContentStream();
            PDType0Font mockFont = null; // Would need proper mock
            processor.computeGlyphVector(mockFont, 12f, "", 0);
        });
    }

    @Test
    public void testNegativeFontSizeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ContentStreamForGlyphLayoutInterface mockStream = new MockContentStream();
            processor.showText(mockStream, null, -1f, "test");
        });
    }

    @Test
    public void testZeroFontSizeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ContentStreamForGlyphLayoutInterface mockStream = new MockContentStream();
            processor.showText(mockStream, null, 0f, "test");
        });
    }

    @Test
    public void testTextExceedsMaxLengthThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ContentStreamForGlyphLayoutInterface mockStream = new MockContentStream();
            String longText = "a".repeat(2_000_000);
            processor.showText(mockStream, null, 12f, longText);
        });
    }

    @Test
    public void testCheckMissingGlyphsNullTextThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            Font testFont = new Font("Arial", Font.PLAIN, 12);
            GlyphLayoutProcessorAwt.checkMissingGlyphs(null, testFont);
        });
    }

    @Test
    public void testCheckMissingGlyphsNullFontThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            GlyphLayoutProcessorAwt.checkMissingGlyphs("test", null);
        });
    }

    @Test
    public void testFontLoaderCacheManagement() {
        GlyphLayoutFontLoaderAwt fontLoader = new GlyphLayoutFontLoaderAwt();
        assertEquals(0, fontLoader.getCachedFontCount());
        fontLoader.clearFontCache();
        assertEquals(0, fontLoader.getCachedFontCount());
    }

    @Test
    public void testFontOptions() {
        GlyphLayoutFontLoaderAwt.FontOptions options = new GlyphLayoutFontLoaderAwt.FontOptions()
                .setKerningOn()
                .setLigaturesOn();
        assertNotNull(options.getTextAttributes());
        assertTrue(options.getTextAttributes().size() >= 2);
    }

    /**
     * Mock implementation of ContentStreamForGlyphLayoutInterface for testing
     */
    private static class MockContentStream implements ContentStreamForGlyphLayoutInterface {
        @Override
        public void showGlyphsWithPositioning(GlyphsAndPositions glyphsAndPositions) {
            // Mock implementation
        }

        @Override
        public void showGlyphCodes(int[] glyphCodes) {
            // Mock implementation
        }

        @Override
        public void setTextRise(float rise) {
            // Mock implementation
        }
    }
}
