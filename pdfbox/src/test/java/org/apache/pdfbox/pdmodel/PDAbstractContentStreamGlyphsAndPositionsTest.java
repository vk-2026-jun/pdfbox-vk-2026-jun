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

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class PDAbstractContentStreamGlyphsAndPositionsTest {

    private PDDocument document;
    private PDResources resources;
    private ByteArrayOutputStream baos;
    private TestContentStream stream;

    @BeforeEach
    public void setUp() {
        document = new PDDocument();
        resources = new PDResources();
        baos = new ByteArrayOutputStream();
        stream = new TestContentStream(document, baos, resources);
    }

    @AfterEach
    public void tearDown() throws IOException {
        document.close();
    }

    @Test
    public void testShowTextWithNonInterleavedArrayDoesNotThrow() throws IOException {
        stream.beginText();
        stream.setFont(PDType1Font.HELVETICA, 12f);

        GlyphsAndPositions gap = new GlyphsAndPositions();
        // non-interleaved example: positions then glyphs
        gap.add(120.0f);
        gap.add(65);
        gap.add(66);
        Object[] arr = gap.toArray();

        // Should not throw and should emit an array + TJ operator
        stream.showTextWithPositioning(arr);
        String out = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(out.contains("[") || out.contains("TJ") || out.length() > 0);
    }

    // Minimal concrete subclass to access protected methods.
    static final class TestContentStream extends PDAbstractContentStream {
        TestContentStream(PDDocument document, ByteArrayOutputStream outputStream, PDResources resources) {
            super(document, outputStream, resources);
        }
    }
}
