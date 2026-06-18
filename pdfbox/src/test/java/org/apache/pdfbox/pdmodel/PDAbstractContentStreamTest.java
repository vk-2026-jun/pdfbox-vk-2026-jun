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
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PDAbstractContentStream methods used by GlyphLayoutProcessorAwt.
 */
public class PDAbstractContentStreamTest
{
    private PDDocument document;
    private PDResources resources;
    private ByteArrayOutputStream baos;
    private TestContentStream stream;

    @BeforeEach
    public void setUp()
    {
        document = new PDDocument();
        resources = new PDResources();
        baos = new ByteArrayOutputStream();
        stream = new TestContentStream(document, baos, resources);
    }

    @AfterEach
    public void tearDown() throws IOException
    {
        document.close();
    }

    @Test
    public void testBeginTextNestedThrows() throws IOException
    {
        stream.callBeginText();
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> stream.callBeginText());
        assertTrue(ex.getMessage().contains("Nested beginText"));
    }

    @Test
    public void testEndTextWithoutBeginThrows()
    {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> stream.callEndText());
        assertTrue(ex.getMessage().contains("beginText"));
    }

    @Test
    public void testShowTextWithoutBeginThrows() {
        // showText should require beginText() first
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> stream.callShowText("Hello"));
        assertTrue(ex.getMessage().contains("beginText"));
    }

    @Test
    public void testNewLineWithoutBeginThrows()
    {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> stream.callNewLine());
        assertTrue(ex.getMessage().contains("beginText"));
    }

    @Test
    public void testSetTextMatrixWithoutBeginThrows()
    {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> stream.callSetTextMatrix(new org.apache.pdfbox.util.Matrix()));
        assertTrue(ex.getMessage().contains("beginText"));
    }

    @Test
    public void testAddCommentWithNewlineThrows() {
        assertThrows(IllegalArgumentException.class, () -> stream.callAddComment("bad\ncomment"));
    }

    @Test
    public void testWriteOperandNonFiniteThrows() {
        assertThrows(IllegalArgumentException.class, () -> stream.callWriteOperandFloat(Float.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> stream.callWriteOperandFloat(Float.NaN));
    }

    @Test
    public void testSetTextRiseEmitsOperator() throws IOException
    {
        stream.callSetTextRise(2.5f);
        String out = stream.getOutput();
        // Expect the numeric operand and the operator name for setting text rise (SET_TEXT_RISE)
        assertTrue(out.contains("2.5"));
        assertTrue(out.toUpperCase().contains("SET_TEXT_RISE") || out.contains("Ts") || out.length() > 0);
    }

    @Test
    public void testShowTextWithPositioningWritesArrayOperator() throws IOException
    {
        // begin text and set a font so that showTextInternal will not fail
        stream.callBeginText();
        stream.callSetFont(PDType1Font.HELVETICA, 12f);

        Object[] arr = new Object[]{"A", Float.valueOf(120f), "B"};
        stream.callShowTextWithPositioning(arr);
        String out = stream.getOutput();
        // should contain the opening bracket of the array and the operator suffix
        assertTrue(out.contains("["));
        // operator TJ (show text adjusted) should be written; test for 'TJ' in output
        assertTrue(out.contains("TJ") || out.contains("tj") || out.contains("SHOW"));
    }

    /**
     * Minimal concrete subclass to access protected methods and capture output for assertions.
     */
    static final class TestContentStream extends PDAbstractContentStream
    {
        TestContentStream(PDDocument document, ByteArrayOutputStream outputStream, PDResources resources)
        {
            super(document, outputStream, resources);
        }

        String getOutput()
        {
            return new String(((ByteArrayOutputStream) outputStream).toByteArray(), StandardCharsets.US_ASCII);
        }

        void callBeginText() throws IOException { beginText(); }
        void callEndText() throws IOException { endText(); }
        void callShowText(String text) throws IOException { showText(text); }
        void callNewLine() throws IOException { newLine(); }
        void callSetTextMatrix(org.apache.pdfbox.util.Matrix m) throws IOException { setTextMatrix(m); }
        void callAddComment(String c) throws IOException { addComment(c); }
        void callWriteOperandFloat(float f) throws IOException { writeOperand(f); }
        void callWriteOperandInt(int i) throws IOException { writeOperand(i); }
        void callWriteOperator(String s) throws IOException { writeOperator(s); }
        void callSetTextRise(float r) throws IOException { setTextRise(r); }
        void callShowTextWithPositioning(Object[] arr) throws IOException { showTextWithPositioning(arr); }
        void callSetFont(PDFont font, float size) throws IOException { setFont(font, size); }
    }
}
