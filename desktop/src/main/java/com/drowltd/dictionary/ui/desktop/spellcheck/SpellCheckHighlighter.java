package com.drowltd.dictionary.ui.desktop.spellcheck;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author iivalchev
 * @since 0.2
 */
public class SpellCheckHighlighter {

    private static SpellCheckHighlighter INSTANCE;
    private static Logger LOGGER = LoggerFactory.getLogger(UnderlineHighlightPainter.class);
    private Highlighter highlighter;
    private final MisspelledWordsRegistry registry = MisspelledWordsRegistry.getInstance();
    private final javax.swing.text.Highlighter.HighlightPainter painter = new UnderlineHighlightPainter(Color.red);

    public static SpellCheckHighlighter init(javax.swing.text.Highlighter highlighter) {
        if (highlighter == null) {
            LOGGER.error("highlighter is null");
            throw new NullPointerException("highlighter is null");
        }

        INSTANCE = new SpellCheckHighlighter(highlighter);
        return INSTANCE;
    }

    public static SpellCheckHighlighter getInstance() {
        if (INSTANCE == null) {
            throw new NullPointerException("Highlighter init() should be invoked first");
        }
        return INSTANCE;
    }

    private SpellCheckHighlighter(javax.swing.text.Highlighter highlighter) {
        this.highlighter = highlighter;
    }

    public void highlightMisspelled() {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                synchronized (registry) {
                    LOGGER.info("Removing all highlights");
                    removeAllHighlights();
                    if (registry.getMisspelled().isEmpty()) {
                        return;
                    }
                    for (MisspelledWord misspelledWord : registry.getMisspelled()) {
                        for (MisspelledWord.Position position : misspelledWord.getOccurances()) {
                            try {
                                highlighter.addHighlight(position.getStartIndex(), position.getEndIndex() + 1, painter);
                            } catch (BadLocationException ex) {
                                LOGGER.error("start: " + position.getStartIndex() + " end:" + position.getEndIndex() + " " + ex.getMessage());
                            }
                        }
                    }
                }
            }
        });
    }

    public void removeHighlight(int start, int end) {
        if (start < 0 || end < 0) {
            LOGGER.error("start < 0 || end < 0");
            return;
        }

        if (end - start < 0) {
            LOGGER.error("end - start < 0");
            return;
        }

        Highlighter.Highlight[] highlights = highlighter.getHighlights();
        for (Highlighter.Highlight h : highlights) {
            if (h.getStartOffset() <= start && end <= h.getEndOffset()) {
                LOGGER.info("removing highlight: " + h.getStartOffset() + " " + h.getEndOffset());
                highlighter.removeHighlight(h);
            }
        }
    }

    public void removeAllHighlights() {
        
        Highlighter.Highlight[] highlights = highlighter.getHighlights();
        for (Highlighter.Highlight h : highlights) {
            highlighter.removeHighlight(h);
        }
    }

    public void addHighlight(int start, int end) {
        if (start < 0 || end < 0) {
            LOGGER.error("start < 0 || end < 0");
            return;
        }

        if (end - start <= 0) {
            LOGGER.error("end - start <= 0");
            return;
        }

        try {
            highlighter.addHighlight(start, end, painter);
        } catch (BadLocationException ex) {
            LOGGER.error("start: " + start + " end:" + end + " " + ex.getMessage());
        }
    }

    private class UnderlineHighlightPainter extends LayeredHighlighter.LayerPainter {

        private Color color; // The color for the underline

        public UnderlineHighlightPainter(Color c) {
            if (c == null) {
                color = Color.RED;
            }
            color = c;

        }

        @Override
        public void paint(Graphics g, int offs0, int offs1, Shape bounds,
                JTextComponent c) {
            // Do nothing: this method will never be called
        }

        @Override
        public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds,
                JTextComponent c, View view) {
            g.setColor(color == null ? c.getSelectionColor() : color);

            Rectangle alloc = null;
            if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
                if (bounds instanceof Rectangle) {
                    alloc = (Rectangle) bounds;
                } else {
                    alloc = bounds.getBounds();
                }
            } else {
                try {
                    Shape shape = view.modelToView(offs0,
                            Position.Bias.Forward, offs1,
                            Position.Bias.Backward, bounds);
                    alloc = (shape instanceof Rectangle) ? (Rectangle) shape
                            : shape.getBounds();
                } catch (BadLocationException e) {
                    LOGGER.error("BadLocationException : " + e.getMessage());
                }
            }

            FontMetrics fm = c.getFontMetrics(c.getFont());
            int baseline = alloc.y + alloc.height - fm.getDescent() + 1;
            g.drawLine(alloc.x, baseline, alloc.x + alloc.width, baseline);
            g.drawLine(alloc.x, baseline + 1, alloc.x + alloc.width,
                    baseline + 1);

            return alloc;
        }
    }
}
