/*
 * Copyright 2013 Gerrit Meinders
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lithium.display.java2d;

import javax.swing.*;

import lithium.*;
import lithium.animation.legacy.scrolling.*;
import lithium.display.*;

import static javax.swing.ScrollPaneConstants.*;

public class NewView extends JLayeredPane {
    private final ViewModel model;

    private JScrollPane scrollPane;

    private JTextPane view;

    public NewView(ViewModel model) {
        this.model = model;

        view = new JTextPane();

        // scrollpane
        scrollPane = new JScrollPane(view, VERTICAL_SCROLLBAR_ALWAYS,
                HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setUI(new LyricViewScrollPaneUI());
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setOpaque(false);
        // scrollPane.setAutoscrolls(false);

        // scrolling based on LyricViewModel
        scrollPane.getVerticalScrollBar()
                .setModel(createRangeModel(model));

        // set scrollbar visibility
        Config config = ConfigManager.getConfig();
        setScrollBarVisible(config.isScrollBarVisible());

        // set scroll bar increment
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement((int) (20 * getScale()));
    }

    private float getScale() {
        return getWidth() / 1024.0f;
    }

    private void updateScrollBarModels() {
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        BoundedRangeModel oldRangeModel = vertical.getModel();
        if (oldRangeModel instanceof AdvancedTextRangeModel) {
            AdvancedTextRangeModel textRangeModel = (AdvancedTextRangeModel) oldRangeModel;
            textRangeModel.dispose();
        }
        vertical.setModel(createRangeModel(model));
    }
    /**
     * Sets whether the view's scroll bar should be visible.
     *
     * @param scrollBarVisible whether the scroll bar should be visible
     */
    public void setScrollBarVisible(boolean scrollBarVisible) {
        if (scrollBarVisible) {
            scrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        } else {
            scrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
        }
    }

    private BoundedRangeModel createRangeModel(ViewModel model) {
        Scroller scroller = model.getScroller();
        Config config = ConfigManager.getConfig();
        ConversionStrategy strategy;
        switch (config.getScrollUnits()) {
        case CHARACTERS:
            strategy = new CharacterConversionStrategy(view);
        case LINES:
        default:
            strategy = new LineConversionStrategy(view);
            break;
        }
        strategy.setTopMarginFixed(true);
        AdvancedTextRangeModel rangeModel = new AdvancedTextRangeModel(
                scroller, strategy);
        return rangeModel;
    }

//} else if (e.getPropertyName() == LyricViewModel.SCROLLER_PROPERTY) {
//    scrollPane.getVerticalScrollBar().setModel(
//            createRangeModel(getModel()));
}
