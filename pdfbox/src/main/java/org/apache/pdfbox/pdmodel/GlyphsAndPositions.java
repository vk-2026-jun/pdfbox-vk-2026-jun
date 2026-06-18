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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a sequence of glyphs interleaved with optional position adjustments.
 *
 * <p>The expected usage is to call {@link #add(float)} to specify a position adjustment that
 * will apply to the next glyph added via {@link #add(int)}. If no adjustment is set before
 * a glyph is added, that glyph is appended without an explicit position adjustment.</p>
 *
 * <p>This class intentionally keeps a compact representation suitable for emitting
 * PDF text showing operators that accept an array of numbers and glyph ids.
 * It provides typed add methods and enforces common invariants (no consecutive
 * position adjustments, no trailing position without a glyph) to avoid malformed
 * sequences.</p>
 *
 * <p>Instances are not synchronized; typical callers create one instance per
 * content-stream operation and use it from a single thread.</p>
 *
 * @author Volker Kunert
 */
public final class GlyphsAndPositions {

    // Internal list stores the interleaved values in insertion order. Each element
    // is either a Float (position adjustment) or an Integer (glyph id).
    private final ArrayList<Object> entries = new ArrayList<>();

    // A pending position adjustment that has been added but not yet attached to a glyph.
    // We keep it separate to give clearer error messages when callers forget to add the glyph.
    private Float pendingPosition = null;

    /**
     * Adds a glyph code to the sequence.
     * If a position adjustment was previously added (via {@link #add(float)}) it will be
     * emitted immediately before this glyph.
     *
     * @param glyphCode the glyph id to append (must be non-negative in most usages)
     */
    public void add(int glyphCode) {
        if (pendingPosition != null) {
            entries.add(pendingPosition);
            pendingPosition = null;
        }
        entries.add(glyphCode);
    }

    /**
     * Adds a position adjustment that will be applied to the next glyph added.
     * Calling this method twice in a row without an intervening {@link #add(int)}
     * will throw an IllegalStateException.
     *
     * @param position the position adjustment in text space units (may be negative)
     * @throws IllegalStateException if a previous position adjustment is still pending
     */
    public void add(float position) {
        if (pendingPosition != null) {
            throw new IllegalStateException("A position adjustment is already pending for the next glyph");
        }
        pendingPosition = position;
    }

    /**
     * Returns true if the sequence contains no fully-formed entries. Note that a pending
     * position (added via {@link #add(float)}) counts as non-empty because it is part of
     * an in-progress sequence.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return entries.isEmpty() && pendingPosition == null;
    }

    /**
     * Clears the contents and any pending position.
     */
    public void clear() {
        entries.clear();
        pendingPosition = null;
    }

    /**
     * Returns an immutable view of the underlying interleaved values as a list.
     * The returned list contains Float and Integer instances alternating according to
     * how the sequence was built.
     *
     * @return unmodifiable list of interleaved Float/Integer values
     * @throws IllegalStateException if there is a pending position that was not followed by a glyph
     */
    public List<Object> asList() {
        if (pendingPosition != null) {
            throw new IllegalStateException("There is a pending position adjustment that has not been followed by a glyph");
        }
        return Collections.unmodifiableList(entries);
    }

    /**
     * Returns the interleaved values as an Object array (Float and Integer elements).
     * Preferred for callers that need to emit the array directly to a content stream.
     *
     * @return array of interleaved values
     * @throws IllegalStateException if there is a pending position that was not followed by a glyph
     */
    public Object[] toArray() {
        if (pendingPosition != null) {
            throw new IllegalStateException("There is a pending position adjustment that has not been followed by a glyph");
        }
        return entries.toArray(new Object[0]);
    }

    /**
     * Number of interleaved elements currently stored. Note that this number counts the
     * Float and Integer objects separately (i.e. a single position+glyph pair contributes 2).
     *
     * @return number of stored items
     */
    public int size() {
        return entries.size() + (pendingPosition == null ? 0 : 1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GlyphsAndPositions[");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(entries.get(i));
        }
        if (pendingPosition != null) {
            if (!entries.isEmpty()) sb.append(',');
            sb.append(pendingPosition).append("(pending)");
        }
        sb.append(']');
        return sb.toString();
    }
}
