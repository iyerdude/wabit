/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Wabit.
 *
 * Wabit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wabit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.wabit.report;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.wabit.AbstractWabitObject;
import ca.sqlpower.wabit.VariableContext;
import ca.sqlpower.wabit.WabitObject;
import ca.sqlpower.wabit.WabitVersion;
import ca.sqlpower.wabit.report.Page.StandardPageSizes;

/**
 * Represents a report layout in the Wabit.
 */
public class Layout extends AbstractWabitObject implements Pageable, Printable, VariableContext {

    private static final Logger logger = Logger.getLogger(Layout.class);
    
    /**
     * The page size and margin info.
     * <p>
     * TODO: In future versions, a Layout can have many pages so you can accomplish
     * left and right masters, cover pages, and so on. For now, a Layout can only
     * have one arrangement of page content, and this is it.
     */
    private Page page = new Page("Default Page", StandardPageSizes.US_LETTER);
    
    private int pageCount = Integer.MAX_VALUE;
    
    /**
     * The variables defined for this report.
     */
    private final Map<String, Object> vars = new HashMap<String, Object>();
    
    /**
     * This is the zoom level for the views of this layout.
     */
    private int zoomLevel;

    
    public Layout(String name) {
        setName(name);
        page.setParent(this);
        updateBuiltinVariables();
    }
    
    protected void updateBuiltinVariables() {
        setVariable("now", new Date());
        setVariable("system_user", System.getProperty("user.name"));
        setVariable("wabit_version", WabitVersion.VERSION);
        setVariable("page_number", 0);
        setVariable("page_count", 0);
    }
    
    public Page getPage() {
        return page;
    }
    
    public Set<String> getVariableNames() {
        return vars.keySet();
    }

    public Object getVariableValue(String name, Object defaultValue) {
        if (vars.containsKey(name)) {
            return vars.get(name);
        } else {
            return defaultValue;
        }
    }
    
    public void setVariable(String name, Object value) {
        vars.put(name, value);
    }

    public int childPositionOffset(Class<? extends WabitObject> childType) {
        if (childType == Page.class) {
            return 0;
        } else {
            throw new IllegalArgumentException("Layouts don't have children of type " + childType);
        }
    }

    public List<Page> getChildren() {
        return Collections.singletonList(page);
    }
    
    public boolean allowsChildren() {
    	return true;
    }

    /**
     * Prints a page of this report to the given graphics context.
     * 
     * @param pageIndex the zero-based page number to print
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                
        if (pageIndex == 0) {
            for (ContentBox cb : page.getContentBoxes()) {
                cb.getContentRenderer().resetToFirstPage();
            }
        }
        if (pageIndex >= pageCount) {
            return Printable.NO_SUCH_PAGE;
        }
        
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setColor(Color.BLACK);
        setVariable("page_number", pageIndex + 1);
        boolean needMorePages = false;
        for (ContentBox cb : page.getContentBoxes()) {
            logger.debug("(Page " + (pageIndex + 1) + ") rendering content box: "+ cb);
            ReportContentRenderer r = cb.getContentRenderer();
            if (r == null) {
                logger.debug("Skipping content box with no renderer: " + cb);
                continue;
            }
            Graphics2D contentGraphics = (Graphics2D) g2.create(
                    (int) cb.getX(), (int) cb.getY(),
                    (int) cb.getWidth(), (int) cb.getHeight());
            needMorePages |= r.renderReportContent(contentGraphics, cb, 1.0, pageIndex);
            contentGraphics.dispose();
        }
        if (!needMorePages) {
            pageCount = pageIndex + 1;
        }
        return Printable.PAGE_EXISTS;
    }

    public int getNumberOfPages() {
    	try {
    		countPages();
    		setVariable("page_count", pageCount);
    		return pageCount;
    	} catch (PrinterException ex) {
    		throw new RuntimeException("Print exception occured while counting pages", ex);
    	}
    }

    public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
        return page.getPageFormat();
    }

    public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
        return this;
    }

    private int countPages() throws PrinterException {
    	boolean done = false;
    	int pageNum = 0;
    	BufferedImage dummyImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    	Graphics g = dummyImage.getGraphics();
    	try {
	    	while (!done) {
	    		int result = print(g, getPageFormat(pageNum), pageNum);
	    		if (result == Printable.NO_SUCH_PAGE) break;
	    		pageNum++;
	    	}
    	} finally {
    		g.dispose();
    	}
    	return pageNum;
    }

	public void setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
	}

	public int getZoomLevel() {
		return zoomLevel;
	}
}
