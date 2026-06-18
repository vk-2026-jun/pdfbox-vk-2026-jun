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

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.TextAttribute;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads the PDType0Font and awt.Font for GlyphLayoutProcessorAwt
 * <p>
 * Use an object of this class only in one thread.
 * Implements LRU cache eviction to prevent unbounded memory growth.
 *
 * @author Volker Kunert
 */
public class GlyphLayoutFontLoaderAwt {

    /**
     * Maximum number of fonts to cache in memory.
     * Prevents memory exhaustion from loading many fonts.
     */
    private static final int MAX_CACHED_FONTS = 128;

    /**
     * Font read buffer size (2KB) for efficient I/O.
     */
    private static final int FONT_BUFFER_SIZE = 2048;

    /**
     * Mapping from PDFBox font to AWT font.
     * Uses LinkedHashMap to track access order for LRU eviction.
     */
    private final Map<PDType0Font, java.awt.Font> awtFontMap = 
            new ConcurrentHashMap<>();

    /**
     * Track font insertion order for LRU eviction strategy.
     */
    private final java.util.LinkedHashMap<PDType0Font, java.awt.Font> fontAccessOrder = 
            new java.util.LinkedHashMap<PDType0Font, java.awt.Font>(MAX_CACHED_FONTS, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_CACHED_FONTS;
        }
    };

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument  document
     * @param inputStream of the font
     * @return pdType0Font PDFBox font
     * @throws IOException         if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream, boolean embedSubset)
            throws IOException, FontFormatException {
        return loadFont(pdDocument, inputStream, embedSubset, null);
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument  document
     * @param inputStream of the font
     * @param fontOptions Options for font
     * @return pdType0Font PDFBox font
     * @throws IOException         if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream, boolean embedSubset, FontOptions fontOptions)
            throws IOException, FontFormatException {

        Objects.requireNonNull(inputStream, "InputStream must not be null");
        Objects.requireNonNull(pdDocument, "PDDocument must not be null");
        
        PDType0Font pdType0Font = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Copy font stream into memory to read it twice
            // for creation of PDType0Font and AWT Font
            byte[] buffer = new byte[FONT_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] fontData = baos.toByteArray();
            
            if (fontData.length == 0) {
                throw new FontFormatException("Font stream is empty");
            }
            
            try (ByteArrayInputStream bais = new ByteArrayInputStream(fontData)) {
                pdType0Font = PDType0Font.load(pdDocument, bais, embedSubset);
                bais.reset();
                loadAwtFont(pdType0Font, bais, fontOptions);
            }
        }
        return pdType0Font;
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument  document
     * @param inputStream of the font
     * @return pdType0Font PDFBox font
     * @throws IOException         if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream)
            throws IOException, FontFormatException {
        return loadFont(pdDocument, inputStream, true, null);
    }

    /**
     * Loads the AWT font needed for layout
     *
     * @param pdDocument  document
     * @param inputStream of the font
     * @param fontOptions options for font
     * @return pdType0Font PDFBox font
     * @throws IOException         if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    public PDType0Font loadFont(PDDocument pdDocument, InputStream inputStream, FontOptions fontOptions)
            throws IOException, FontFormatException {
        return loadFont(pdDocument, inputStream, true, fontOptions);
    }


    /**
     * Loads the AWT font needed for layout with LRU cache management.
     *
     * @param pdType0Font PDFBox font
     * @param inputStream of the font file
     * @throws IOException         if font can not be loaded
     * @throws FontFormatException if the font is bad
     */
    protected void loadAwtFont(PDType0Font pdType0Font, InputStream inputStream, FontOptions fontOptions)
            throws java.awt.FontFormatException, java.io.IOException {
        Font awtFont;
        if (fontOptions == null) {
            fontOptions = new FontOptions();
        }
        
        if (!awtFontMap.containsKey(pdType0Font)) {
            // Load new font
            awtFont = Font.createFont(java.awt.Font.TRUETYPE_FONT, inputStream)
                    .deriveFont(fontOptions.getTextAttributes());
            Objects.requireNonNull(awtFont, "AWT Font creation failed");
            
            // Add to cache with LRU eviction
            synchronized (fontAccessOrder) {
                if (fontAccessOrder.size() >= MAX_CACHED_FONTS) {
                    // Evict least recently used entry
                    Iterator<PDType0Font> iterator = fontAccessOrder.keySet().iterator();
                    if (iterator.hasNext()) {
                        PDType0Font lruFont = iterator.next();
                        fontAccessOrder.remove(lruFont);
                        awtFontMap.remove(lruFont);
                    }
                }
                fontAccessOrder.put(pdType0Font, awtFont);
            }
            awtFontMap.put(pdType0Font, awtFont);
        }
    }

    /**
     * Determines if glyph layout is supported for this font
     *
     * @param font PDFBox font
     * @return true if glyph layout is supported for this font and this font is a PDType0Font
     */
    public boolean supportsFont(PDFont font) {
        return  font instanceof PDType0Font &&
                awtFontMap.containsKey((PDType0Font) font);
    }

    /**
     * Gets the corresponding AWT-font for the given PDFBox-font
     *
     * @param font PDFBox font
     * @return AWT font if available
     */
    public Font getAwtFont(PDType0Font font) {
        return awtFontMap.get(font);
    }

    /**
     * Returns the current number of cached fonts
     * 
     * @return number of fonts in cache
     */
    public int getCachedFontCount() {
        return awtFontMap.size();
    }

    /**
     * Clears the font cache. Useful for testing and memory cleanup.
     */
    public void clearFontCache() {
        awtFontMap.clear();
        synchronized (fontAccessOrder) {
            fontAccessOrder.clear();
        }
    }

    /**
     * Specify Options for an AWT font
     */
    public static class FontOptions {

        private final Map<TextAttribute, Object> textAttributes = new HashMap<>();

        protected Map<TextAttribute, Object> getTextAttributes() {
            // always return an unmodifiableMap, so that internal state can not be changed
            // by changing the returned map
            return Collections.unmodifiableMap(textAttributes);
        }

        public FontOptions setKerningOn() {
            textAttributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
            return this;
        }

        public FontOptions setLigaturesOn() {
            textAttributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
            return this;
        }
    }
}
