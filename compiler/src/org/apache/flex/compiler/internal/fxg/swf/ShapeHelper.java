/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.flex.compiler.internal.fxg.swf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.flex.compiler.internal.fxg.dom.AbstractFXGNode;
import org.apache.flex.compiler.internal.fxg.dom.PathNode;
import org.apache.flex.compiler.internal.fxg.dom.strokes.AbstractStrokeNode;
import org.apache.flex.compiler.problems.FXGInvalidPathDataProblem;
import org.apache.flex.compiler.problems.ICompilerProblem;

import org.apache.flex.swf.ISWFConstants;
import org.apache.flex.swf.types.CurvedEdgeRecord;
import org.apache.flex.swf.types.LineStyle;
import org.apache.flex.swf.types.LineStyle2;
import org.apache.flex.swf.types.Rect;
import org.apache.flex.swf.types.ShapeRecord;
import org.apache.flex.swf.types.StraightEdgeRecord;
import org.apache.flex.swf.types.StyleChangeRecord;
import org.apache.flex.swf.types.Styles;

/**
 * A collection of utilities to help create SWF Shapes and ShapeRecords.
 */
public class ShapeHelper implements ISWFConstants
{
    /**
     * A value object for an x and y pair.
     */
    private static class Point
    {
        public Point()
        {
            x = 0.0;
            y = 0.0;
        }

        public Point(double x, double y)
        {
            this.x = x;
            this.y = y;
        }

        public double x;
        public double y;
    }
    
    /**
     * Creates a List of ShapeRecord to draw a line from the given
     * origin (startX, startY) to the specified coordinates (in pixels).
     * 
     * @param startX The origin x coordinate in pixels.
     * @param startY The origin y coordinate in pixels.
     * @param endX The end x coordinate in pixels.
     * @param endY The end y coordinate in pixels.
     * @return list of ShapeRecords representing the rectangle.
     */
    public static List<ShapeRecord> line(double startX, double startY, double endX, double endY)
    {
        List<ShapeRecord> shapeRecords = new ArrayList<ShapeRecord>();
        shapeRecords.add(move(startX, startY));
        shapeRecords.addAll(straightEdge(startX, startY, endX, endY));
        return shapeRecords;
    }


    /**
     * Creates a List of ShapeRecord to draw a line that represents an implicit closepath
     * origin (startX, startY) to the specified coordinates (in pixels). 
     * 
     * @param startX The origin x coordinate in pixels.
     * @param startY The origin y coordinate in pixels.
     * @param endX The end x coordinate in pixels.
     * @param endY The end y coordinate in pixels.
     * @return list of ShapeRecords representing the rectangle.
     */
    public static List<ShapeRecord> implicitClosepath(double startX, double startY, double endX, double endY)
    {
        List<ShapeRecord> shapeRecords = new ArrayList<ShapeRecord>();
        StyleChangeRecord scr = move(startX, startY);
        scr.defaultStyles(false, false, true);
        shapeRecords.add(scr);
        shapeRecords.addAll(straightEdge(startX, startY, endX, endY));
        return shapeRecords;
    }
    
    /**
     * Creates a List of ShapeRecord to draw a rectangle from the given
     * origin (startX, startY) for the specified width and height (in pixels).
     * 
     * @param startX The origin x coordinate in pixels.
     * @param startY The origin y coordinate in pixels.
     * @param width The rectangle width in pixels.
     * @param height The rectangle width in pixels.
     * @return list of ShapeRecords representing the rectangle.
     */
    public static List<ShapeRecord> rectangle(double startX, double startY, double width, double height)
    {
        List<ShapeRecord> shapeRecords = new ArrayList<ShapeRecord>();
        shapeRecords.add(move(startX, startY));
        shapeRecords.addAll(straightEdge(startX, startY, width, startY));
        shapeRecords.addAll(straightEdge(width, startY, width, height));
        shapeRecords.addAll(straightEdge(width, height, startX, height));
        shapeRecords.addAll(straightEdge(startX, height, startX, startY));
        return shapeRecords;
    }

    /**
     * Creates a List of ShapeRecord to draw a rectangle from the given
     * origin (startX, startY) for the specified width and height (in pixels)
     * and radiusX and radiusY for rounded corners.
     * 
     * @param startx The origin x coordinate in pixels.
     * @param starty The origin y coordinate in pixels.
     * @param width The rectangle width in pixels.
     * @param height The rectangle width in pixels.
     * @param radiusX The radiusX for rounded corner in pixels
     * @param radiusY The radius for rounded corner in pixels
     * @return list of ShapeRecords representing the rectangle.
     */
    public static List<ShapeRecord> rectangle(double startx, double starty, 
    		double width, double height, double radiusX, double radiusY, 
    		double topLeftRadiusX, double topLeftRadiusY, double topRightRadiusX, 
    		double topRightRadiusY, double bottomLeftRadiusX, double bottomLeftRadiusY,
    		double bottomRightRadiusX, double bottomRightRadiusY)
    {

        //YAY MAGIC NUMBERS!
        final double SIN_67_5 = 0.923879532511;//sin .75 * 90
        final double SIN_22_5 = 0.382683432365;//sin .25 * 90
        final double SIN_45_0 = 0.707106781187;//sin .50 * 90
        
        List<ShapeRecord> shapeRecords = new ArrayList<ShapeRecord>();
        
        if (radiusX == 0.0) 
        {
            radiusY = radiusX = 0;
         }
        else if (radiusY == 0.0)
        {
            radiusY = radiusX;
        }
        
        if ( radiusX > width/2.0 )
            radiusX = width/2.0;
        if ( radiusY > height/2.0 )
            radiusY = height/2.0;          

        double[] topLeftRadius = getCornerRadius(topLeftRadiusX, topLeftRadiusY, radiusX, radiusY, width, height);
        topLeftRadiusX = topLeftRadius[0];
        topLeftRadiusY = topLeftRadius[1];
        
        double[] topRightRadius = getCornerRadius(topRightRadiusX, topRightRadiusY, radiusX, radiusY, width, height);
        topRightRadiusX = topRightRadius[0];
        topRightRadiusY = topRightRadius[1];

        double[] bottomLeftRadius = getCornerRadius(bottomLeftRadiusX, bottomLeftRadiusY, radiusX, radiusY, width, height);
        bottomLeftRadiusX = bottomLeftRadius[0];
        bottomLeftRadiusY = bottomLeftRadius[1];
        
        double[] bottomRightRadius = getCornerRadius(bottomRightRadiusX, bottomRightRadiusY, radiusX, radiusY, width, height);
        bottomRightRadiusX = bottomRightRadius[0];
        bottomRightRadiusY = bottomRightRadius[1];
 
        double c0 = SIN_67_5;
        double c1 = SIN_22_5;
        double c3 = SIN_45_0;
  
        double rx = bottomRightRadiusX;
        double ry = bottomRightRadiusY;
                
        double tx = rx / SIN_67_5;
        double ty = ry / SIN_67_5;

        double dx, currentx;
        double dy, currenty;

        dx = startx + width - rx;
        dy = starty + height - ry;
        shapeRecords.add(move( (dx + rx), dy ));
        currentx = (dx + rx);
        currenty = dy;
        if ( bottomRightRadiusX != 0.0 ) 
        {
            shapeRecords.add(curvedEdge(currentx, currenty, (dx + c0 * tx), (dy + c1 * ty), (dx + c3 * rx), (dy + c3 * ry) ));
            shapeRecords.add(curvedEdge((dx + c3 * rx), (dy + c3 * ry), (dx + c1 * tx), (dy + c0 * ty), dx, (dy + ry)) );
            currentx = dx;
            currenty = dy + ry;
        }
        
        rx = bottomLeftRadiusX;
        ry = bottomLeftRadiusY;
        tx = rx / SIN_67_5;
        ty = ry / SIN_67_5;
        dx = startx + rx;
        dy = starty + height - ry;
        shapeRecords.addAll(straightEdge(currentx, currenty, dx, (dy + ry) ));
        currentx = dx;
        currenty = dy + ry;
        if ( bottomLeftRadiusX != 0.0 ) 
        {
            shapeRecords.add(curvedEdge(currentx, currenty, (dx - c1 * tx), (dy + c0 * ty), (dx - c3 * rx), (dy + c3 * ry) ));
            shapeRecords.add(curvedEdge((dx - c3 * rx), (dy + c3 * ry), (dx - c0 * tx), (dy + c1 * ty), (dx - rx), dy ));
            currentx = dx - rx;
            currenty = dy;
        }
        
        rx = topLeftRadiusX;
        ry = topLeftRadiusY;
        tx = rx / SIN_67_5;
        ty = ry / SIN_67_5;
        dx = startx + rx;
        dy = starty + ry;
        shapeRecords.addAll(straightEdge(currentx, currenty, (dx - rx), dy ));
        currentx = dx - rx;
        currenty = dy;
        if ( topLeftRadiusX != 0.0 ) 
        {
            shapeRecords.add(curvedEdge(currentx, currenty, (dx - c0 * tx), (dy - c1 * ty), (dx - c3 * rx), (dy - c3 * ry) ));
            shapeRecords.add(curvedEdge((dx - c3 * rx), (dy - c3 * ry), (dx - c1 * tx), (dy - c0 * ty), dx, (dy - ry) ));
            currentx = dx;
            currenty = dy - ry;
        }
        
        rx = topRightRadiusX;
        ry = topRightRadiusY;
        tx = rx / SIN_67_5;
        ty = ry / SIN_67_5;
        dx = startx + width - rx;
        dy = starty + ry;
        shapeRecords.addAll(straightEdge(currentx, currenty, dx, (dy - ry) ));
        currentx = dx;
        currenty = dy - ry;
        if ( topRightRadiusX != 0.0 ) 
        {
            shapeRecords.add(curvedEdge(currentx, currenty, (dx + c1 * tx), (dy - c0 * ty), (dx + c3 * rx), (dy - c3 * ry) ));
            shapeRecords.add(curvedEdge((dx + c3 * rx), (dy - c3 * ry), (dx + c0 * tx), (dy - c1 * ty), (dx + rx), dy ));
            currentx = (dx + rx);
            currenty = dy;
        }
        
        rx = bottomRightRadiusX;
        ry = bottomRightRadiusY;
        tx = rx / SIN_67_5;
        ty = ry / SIN_67_5;
        dx = startx + width - rx;
        dy = starty + height - ry;
        shapeRecords.addAll(straightEdge(currentx, currenty, (dx + rx), dy ));

        return shapeRecords;
        
    }

    /**
     * Creates a List of ShapeRecord to draw a rectangle from the
     * origin (0.0, 0.0) for the specified width and height (in pixels).
     * 
     * @param width The rectangle width in pixels.
     * @param height The rectangle width in pixels.
     * @return list of ShapeRecords representing the rectangle.
     */
    public static List<ShapeRecord> rectangle(double width, double height)
    {
        return rectangle(0.0, 0.0, width, height);
    }
    
    /**
     * Sets the style information for the first StyleChangeRecord in a list
     * of ShapeRecords.
     * 
     * @param shapeRecords A list of shape records.
     * @param lineStyleIndex The ShapeWithStyle LineStyle index (starting at 1)
     * or 0 if none.
     * @param fillStyle0Index The ShapeWithStyle FillStyle index (starting at 1)
     * or 0 if none.
     * @param fillStyle1Index The ShapeWithStyle FillStyle index (starting at 1)
     * or 0 if none. 
     */
    public static void setStyles(List<ShapeRecord> shapeRecords,
            int lineStyleIndex, int fillStyle0Index, int fillStyle1Index, Styles styles)
    {
        int fs0 = fillStyle0Index == 0 ? -1 : fillStyle0Index;
        int fs1 = fillStyle1Index == 0 ? -1 : fillStyle1Index;
        int ls = lineStyleIndex == 0 ? -1 : lineStyleIndex;
        if (shapeRecords != null && shapeRecords.size() > 0)
        {
            ShapeRecord firstRecord = shapeRecords.get(0);
            if (firstRecord instanceof StyleChangeRecord)
            {
                StyleChangeRecord scr = (StyleChangeRecord)firstRecord;
                scr.setDefinedStyles(fs0, fs1, ls, styles);
            }
        }
    }


    /**
     * Sets the style information for the all the StyleChangeRecords in a list
     * of ShapeRecords.
     * 
     * @param shapeRecords A list of shape records.
     * @param lineStyleIndex The ShapeWithStyle LineStyle index (starting at 1)
     * or 0 if none.
     * @param fillStyle0Index The ShapeWithStyle FillStyle index (starting at 1)
     * or 0 if none.
     * @param fillStyle1Index The ShapeWithStyle FillStyle index (starting at 1)
     * or 0 if none. 
     */
    public static void setPathStyles(List<ShapeRecord> shapeRecords,
            int lineStyleIndex, int fillStyle0Index, int fillStyle1Index, Styles styles)
    {
        int fs0 = fillStyle0Index == 0 ? -1 : fillStyle0Index;
        int fs1 = fillStyle1Index == 0 ? -1 : fillStyle1Index;
        int ls = lineStyleIndex == 0 ? -1 : lineStyleIndex;
        
        if (shapeRecords != null && shapeRecords.size() > 0)
        {
            for (int i = 0; i < shapeRecords.size(); i++)
            {
                ShapeRecord record = shapeRecords.get(i);
                if (record instanceof StyleChangeRecord)
                {
                    StyleChangeRecord scr = (StyleChangeRecord)record;
                    
                    scr.setDefinedStyles(fs0, fs1, (!scr.isStateLineStyle() && ls > 0) ? ls : -1, styles);
                }
            }
        }
    }

    /**
     * Replaces the style information for the all the StyleChangeRecords in a list
     * of ShapeRecords.
     * 
     * @param shapeRecords A list of shape records.
     * @param lineStyleIndex The ShapeWithStyle LineStyle index (starting at 1)
     * or 0 if none.
     * @param fillStyle0Index The ShapeWithStyle FillStyle index (starting at 1)
     * or 0 if none.
     * @param fillStyle1Index The ShapeWithStyle FillStyle index (starting at 1)
     * or 0 if none. 
     */
    public static void replaceStyles(List<ShapeRecord> shapeRecords,
            int lineStyleIndex, int fillStyle0Index, int fillStyle1Index, Styles styleContext)
    {

        int fs0 = fillStyle0Index == 0 ? -1 : fillStyle0Index;
        int fs1 = fillStyle1Index == 0 ? -1 : fillStyle1Index;
        int ls = lineStyleIndex == 0 ? -1 : lineStyleIndex;
        
        if (shapeRecords != null && shapeRecords.size() > 0)
        {
            for (int i = 0; i < shapeRecords.size(); i++)
            {
                ShapeRecord record = shapeRecords.get(i);
                if (record instanceof StyleChangeRecord)
                {
                    StyleChangeRecord old_scr = (StyleChangeRecord) record;
                    StyleChangeRecord new_scr =  new StyleChangeRecord();
                    int old_scrLineStyleIndex = 0;
                    
                    if (old_scr.getLinestyle() != null)
                        old_scrLineStyleIndex = styleContext.getLineStyles().indexOf(old_scr.getLinestyle());
                    
                    new_scr.setDefinedStyles(fs0, fs1, ((!old_scr.isStateLineStyle() && (ls > 0)) ? ls: old_scrLineStyleIndex), styleContext);
                    
                    if (old_scr.isStateMoveTo())
                        new_scr.setMove(old_scr.getMoveDeltaX(), old_scr.getMoveDeltaY());
                    
                    shapeRecords.set(i, new_scr);
                    
                }
            }
        }
    }

    /**
     * Creates a StyleChangeRecord to represent a move command without changing
     * style information. All coordinates are to be specified in pixels and will
     * be converted to twips.
     * 
     * @param x The x coordinate in pixels.
     * @param y The y coordinate in pixels.
     * @return StyleChangeRecord recording the move and styles. 
     */
    public static StyleChangeRecord move(double x, double y)
    {
        x *= TWIPS_PER_PIXEL;
        y *= TWIPS_PER_PIXEL;

        int moveX = (int)x;
        int moveY = (int)y;

        StyleChangeRecord scr = new StyleChangeRecord();
        
        scr.setMove(moveX, moveY);

        return scr;
    }

    
    private static final int MAX_EDGE_SIZE = 65535;
    
    /**
     * Creates a StraightEdgeRecord to represent a line as the delta between
     * the pair of coordinates (xFrom,yFrom) and (xTo,yTo). All coordinates
     * are to be specified in pixels and will be converted to twips.
     * 
     * @param xFrom The start x coordinate in pixels.
     * @param yFrom The start y coordinate in pixels.
     * @param xTo The end x coordinate in pixels.
     * @param yTo The end y coordinate in pixels. 
     */
    public static List<ShapeRecord> straightEdge(double xFrom, double yFrom, double xTo, double yTo)
    {
        List<ShapeRecord> shapeRecords = new ArrayList<ShapeRecord>();
        
        xFrom *= TWIPS_PER_PIXEL;
        yFrom *= TWIPS_PER_PIXEL;
        xTo *= TWIPS_PER_PIXEL;
        yTo *= TWIPS_PER_PIXEL;

        int dx = (int)xTo - (int)xFrom;
        int dy = (int)yTo - (int)yFrom;
        int abs_dx = Math.abs(dx);
        int abs_dy = Math.abs(dy);

        int numSegments = 1;
        if ((abs_dx > MAX_EDGE_SIZE) && (abs_dx > abs_dy))
        {
            numSegments = abs_dx/MAX_EDGE_SIZE + 1;
        }
        else if ((abs_dy > MAX_EDGE_SIZE) && (abs_dy > abs_dx))
        {
            numSegments = abs_dy/MAX_EDGE_SIZE + 1;
        }
        else
        {
            StraightEdgeRecord ser = new StraightEdgeRecord(dx, dy);
            shapeRecords.add(ser); 
            return shapeRecords;
        }

        int xSeg = dx/numSegments;
        int ySeg = dy/numSegments;
        for (int i=0; i < numSegments; i++)
        {
            if (i == numSegments-1)
            {
                //make up for any rounding errors
                int lastx = dx - xSeg*(numSegments-1);
                int lasty = dy - ySeg*(numSegments-1);
                StraightEdgeRecord ser = new StraightEdgeRecord(lastx, lasty);
                shapeRecords.add(ser);
            }
            else
            {
                StraightEdgeRecord ser = new StraightEdgeRecord(xSeg, ySeg);
                shapeRecords.add(ser);
            }
        }
        
        return shapeRecords;
    }

    /**
     * Creates a CurvedEdgeRecord to represent a quadratic curve by calculating
     * the deltas between the start coordinates and the control point
     * coordinates, and between the control point coordinates and the anchor
     * coordinates. All coordinates are to be specified in pixels and will be
     * converted to twips.
     * 
     * @param startX The start x coordinate in pixels.
     * @param startY The start y coordinate in pixels.
     * @param controlX The control point x coordinate in pixels.
     * @param controlY The control point y coordinate in pixels.
     * @param anchorX The anchor x coordinate in pixels.
     * @param anchorY The anchor y coordinate in pixels.
     * @return CurvedEdgeRecord representing a quadratic curve.
     */
    public static CurvedEdgeRecord curvedEdge(double startX, double startY,
            double controlX, double controlY, double anchorX, double anchorY)
    {
        startX *= TWIPS_PER_PIXEL;
        startY *= TWIPS_PER_PIXEL;
        controlX *= TWIPS_PER_PIXEL;
        controlY *= TWIPS_PER_PIXEL;
        anchorX *= TWIPS_PER_PIXEL;
        anchorY *= TWIPS_PER_PIXEL;

        int dcx = (int)controlX - (int)startX;
        int dcy = (int)controlY - (int)startY;
        int dax = (int)anchorX - (int)controlX;
        int day = (int)anchorY - (int)controlY;

        CurvedEdgeRecord cer = new CurvedEdgeRecord();
        cer.setControlDeltaX(dcx);
        cer.setControlDeltaY(dcy);
        cer.setAnchorDeltaX(dax);
        cer.setAnchorDeltaY(day);
        return cer;
    }

    /**
     * Approximates a cubic Bezier as a series of 4 quadratic CurvedEdgeRecord
     * with the method outlined by Timothee Groleau in ActionScript (which was
     * based on Helen Triolo's approach of using Casteljau's approximation).
     * 
     * Using a fixed level of 4 quadratic curves should be a fast way of
     * achieving a reasonable approximation of the original curve.
     * 
     * All coordinates are to be specified in pixels and will be converted to
     * twips.
     * 
     * @param startX The start x coordinate in pixels.
     * @param startY The start y coordinate in pixels.
     * @param control1X The first control point x coordinate in pixels.
     * @param control1Y The first control point y coordinate in pixels.
     * @param control2X The second control point x coordinate in pixels.
     * @param control2Y The second control point y coordinate in pixels.
     * @param anchorX The anchor x coordinate in pixels.
     * @param anchorY The anchor y coordinate in pixels.
     * @return a List of 4 CurvedEdgeRecords approximating the cubic Bezier.
     * 
     * {@link  <a href="http://timotheegroleau.com/Flash/articles/cubic_bezier_in_flash.htm">Cubic Bezier Curves in Flash</a>}
     */
    public static List<ShapeRecord> cubicToQuadratic(final double startX, final double startY,
            final double control1X, final double control1Y,
            final double control2X, final double control2Y,
            final double anchorX, final double anchorY)
    {
        // First, calculate useful base points
        double ratio = 3.0 / 4.0;
        double pax = startX + ((control1X - startX) * ratio);
        double pay = startY + ((control1Y - startY) * ratio);
        double pbx = anchorX + ((control2X - anchorX) * ratio);
        double pby = anchorY + ((control2Y - anchorY) * ratio);

        // Get 1/16 of the [anchor, start] segment
        double dx = (anchorX - startX) / 16.0;
        double dy = (anchorY - startY) / 16.0;

        // Calculate control point 1
        ratio = 3.0 / 8.0;
        double c1x = startX + ((control1X - startX) * ratio);
        double c1y = startY + ((control1Y - startY) * ratio);

        // Calculate control point 2
        double c2x = pax + ((pbx - pax) * ratio);
        double c2y = pay + ((pby - pay) * ratio);
        c2x = c2x - dx;
        c2y = c2y - dy;

        // Calculate control point 3
        double c3x = pbx + ((pax - pbx) * ratio);
        double c3y = pby + ((pay - pby) * ratio);
        c3x = c3x + dx;
        c3y = c3y + dy;

        // Calculate control point 4
        double c4x = anchorX + ((control2X - anchorX) * ratio);
        double c4y = anchorY + ((control2Y - anchorY) * ratio);

        // Calculate the 3 anchor points (as midpoints of the control segments)
        double a1x = (c1x + c2x) / 2.0;
        double a1y = (c1y + c2y) / 2.0;

        double a2x = (pax + pbx) / 2.0;
        double a2y = (pay + pby) / 2.0;

        double a3x = (c3x + c4x) / 2.0;
        double a3y = (c3y + c4y) / 2.0;

        // Create the four quadratic sub-segments
        List<ShapeRecord> shapeRecords = new ArrayList<ShapeRecord>(4);
        shapeRecords.add(curvedEdge(startX, startY, c1x, c1y, a1x, a1y));
        shapeRecords.add(curvedEdge(a1x, a1y, c2x, c2y, a2x, a2y));
        shapeRecords.add(curvedEdge(a2x, a2y, c3x, c3y, a3x, a3y));
        shapeRecords.add(curvedEdge(a3x, a3y, c4x, c4y, anchorX, anchorY));
        return shapeRecords;
    }

    /**
     * Note this utility was ported to Java from the ActionScript class
     * 'flex.graphics.Path' - specifically its 'data' property setter function.
     */
    public static List<ShapeRecord> path(PathNode node, boolean fill, Collection<ICompilerProblem> problems)
    {
    	String data = node.data;
    	
        List<ShapeRecord> shapeRecords = new ArrayList<ShapeRecord>();

        if (data.length() == 0)
            return shapeRecords;
               
        // Split letter followed by number (i.e. "M3" becomes "M 3")
        String temp = data.replaceAll("([A-Za-z])([0-9\\-\\.])", "$1 $2");

        // Split number followed by letter (i.e. "3M" becomes "3 M")
        temp = temp.replaceAll("([0-9\\.])([A-Za-z\\-])", "$1 $2");

        // Split letter followed by letter (i.e. "zM" becomes "z M")
        temp = temp.replaceAll("([A-Za-z\\-])([A-Za-z\\-])", "$1 $2");

        //support scientific notation for floats/doubles
        temp = temp.replaceAll("([0-9])( )([eE])( )([0-9\\-])", "$1$3$5");
 
        // Replace commas with spaces
        temp = temp.replace(',', ' ');

        // Trim leading and trailing spaces
        temp = temp.trim();

        // Finally, split the string into an array 
        String[] args = temp.split("\\s+");
        
        char ic = 0;
        char prevIc = 0;
        double lastMoveX = 0.0;
        double lastMoveY = 0.0;
        double prevX = 0.0;
        double prevY = 0.0;
        double x = 0.0;
        double y = 0.0;
        double controlX = 0.0;
        double controlY = 0.0;
        double control2X = 0.0;
        double control2Y = 0.0;
        boolean firstMove = true;
       
        for (int i = 0; i < args.length; )
        {
            boolean relative = false;
            char c = args[i].toCharArray()[0];
            if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')
            {
                ic = c;
                i++;
            }

            if ((firstMove) && (ic != 'm') && (ic != 'M'))
            {
                problems.add(new FXGInvalidPathDataProblem(node.getDocumentPath(), 
                        node.getStartLine(), node.getStartColumn()));
                return Collections.<ShapeRecord>emptyList();
            }
            		
            switch (ic)
            {
                case 'm':
                    relative = true;
                case 'M':
                     if (firstMove) {
                        x = Double.parseDouble(args[i++]);
                        y = Double.parseDouble(args[i++]);
                        shapeRecords.add(move(x, y));
                        firstMove = false;
                    }
                    else 
                    {
                        //add an implicit closepath, if needed
                        if (fill && (Math.abs(prevX-lastMoveX) > AbstractFXGNode.EPSILON || Math.abs(prevY-lastMoveY) > AbstractFXGNode.EPSILON)) 
                        {
                            if (node.stroke == null)
                                shapeRecords.addAll(straightEdge(prevX, prevY, lastMoveX, lastMoveY));
                            else
                                shapeRecords.addAll(implicitClosepath(prevX, prevY, lastMoveX, lastMoveY));
                        }
                        x = Double.parseDouble(args[i++]) + (relative ? prevX : 0);
                        y = Double.parseDouble(args[i++]) + (relative ? prevY : 0);
                        shapeRecords.add(move(x, y));
                    }
                    lastMoveX = x;
                    lastMoveY = y;
                    ic = (relative) ? 'l' : 'L';
                    break;

                case 'l':
                    relative = true;
                case 'L':
                    x = Double.parseDouble(args[i++]) + (relative ? prevX : 0);
                    y = Double.parseDouble(args[i++]) + (relative ? prevY : 0);
                    shapeRecords.addAll(straightEdge(prevX, prevY, x, y));
                    break;

                case 'h':
                    relative = true;
                case 'H':
                    x = Double.parseDouble(args[i++]) + (relative ? prevX : 0);
                    y = prevY;
                    shapeRecords.addAll(straightEdge(prevX, prevY, x, y));
                    break;

                case 'v':
                    relative = true;
                case 'V':
                    x = prevX;
                    y = Double.parseDouble(args[i++]) + (relative ? prevY : 0);
                    shapeRecords.addAll(straightEdge(prevX, prevY, x, y));
                    break;

                case 'q':
                    relative = true;
                case 'Q':
                    controlX = Double.parseDouble(args[i++]) + (relative ? prevX : 0);
                    controlY = Double.parseDouble(args[i++]) + (relative ? prevY : 0);
                    x = Double.parseDouble(args[i++]) + (relative ? prevX : 0);
                    y = Double.parseDouble(args[i++]) + (relative ? prevY : 0);
                    shapeRecords.add(curvedEdge(prevX, prevY, controlX, controlY, x, y));
                    break;

                case 't':
                    relative = true;
                case 'T':
                    // control is a reflection of the previous control point
                    if ((prevIc == 'T') || (prevIc == 't') || (prevIc == 'q') || (prevIc == 'Q'))
                    {
                        controlX = prevX + (prevX - controlX);
                        controlY = prevY + (prevY - controlY);
                    }
                    else
                    {
                        controlX = prevX;
                        controlY = prevY;
                    }
                    x = Double.parseDouble(args[i++]) + (relative ? prevX : 0);
                    y = Double.parseDouble(args[i++]) + (relative ? prevY : 0);
                    shapeRecords.add(curvedEdge(prevX, prevY, controlX, controlY, x, y));
                    break;

                case 'c':
                    relative = true;
                case 'C':
                    controlX = Double.parseDouble(args[i++]) + (relative ? prevX : 0);
                    controlY = Double.parseDouble(args[i++]) + (relative ? prevY : 0);
                    control2X = Double.parseDouble(args[i++]) + (relative ? prevX : 0);
                    control2Y = Double.parseDouble(args[i++]) + (relative ? prevY : 0);
                    x = Double.parseDouble(args[i++]) + (relative ? prevX : 0);
                    y = Double.parseDouble(args[i++]) + (relative ? prevY : 0);
                    shapeRecords.addAll(cubicToQuadratic(prevX, prevY, controlX, controlY, control2X, control2Y, x, y));
                    break;

                case 's':
                    relative = true;
                case 'S':
                    // Control1 is a reflection of the previous control2 point
                    if ((prevIc == 'S') || (prevIc == 's') || (prevIc == 'c') || (prevIc == 'C'))
                    {
                        controlX = prevX + (prevX - control2X);
                        controlY = prevY + (prevY - control2Y);
                    }
                    else
                    {
                        controlX = prevX;
                        controlY = prevY;
                    }
                    control2X = Double.parseDouble(args[i++]) + (relative ? prevX : 0);
                    control2Y = Double.parseDouble(args[i++]) + (relative ? prevY : 0);
                    x = Double.parseDouble(args[i++]) + (relative ? prevX : 0);
                    y = Double.parseDouble(args[i++]) + (relative ? prevY : 0);
                    shapeRecords.addAll(cubicToQuadratic(prevX, prevY, controlX, controlY, control2X, control2Y, x, y));
                    break;
                    
                case 'z':
                case 'Z':
                    shapeRecords.addAll(straightEdge(prevX, prevY, lastMoveX, lastMoveY));
                    x = lastMoveX;
                    y = lastMoveY;
                    break;

                default:
                    problems.add(new FXGInvalidPathDataProblem(node.getDocumentPath(), 
                            node.getStartLine(), node.getStartColumn()));
                    return Collections.<ShapeRecord>emptyList();
                    
            }

            prevX = x;
            prevY = y;
            prevIc = ic;
       }
        
        //do an implicit closepath, if needed
        if (fill && (Math.abs(prevX-lastMoveX) > AbstractFXGNode.EPSILON) || (Math.abs(prevY-lastMoveY) > AbstractFXGNode.EPSILON))  
        {
            if (node.stroke == null)
                shapeRecords.addAll(straightEdge(prevX, prevY, lastMoveX, lastMoveY));
            else
                shapeRecords.addAll(implicitClosepath(prevX, prevY, lastMoveX, lastMoveY));
        }
        return shapeRecords;
    }


    /**
     * Utility method that calculates the minimum bounding rectangle that
     * encloses a list of ShapeRecords, taking into account the possible maximum
     * stroke width of any of the supplied linestyles.
     * 
     * @return bounding box rectangle.
     */
    public static Rect getBounds(List<ShapeRecord> records, LineStyle ls, AbstractStrokeNode strokeNode)
    {
        if (records == null || records.size() == 0)
        {
            assert false: "null records";
            return new Rect(0,0);
        }
        int x1 = 0;
        int y1 = 0;
        int x2 = 0;
        int y2 = 0;
        int x = 0;
        int y = 0;
        boolean firstMove = true;

        Iterator<ShapeRecord> iterator = records.iterator();
        while (iterator.hasNext())
        {
            ShapeRecord r = iterator.next();

            if (r == null)
                continue;

            if (r instanceof StyleChangeRecord)
            {
                StyleChangeRecord scr = (StyleChangeRecord)r;
                x = scr.getMoveDeltaX();
                y = scr.getMoveDeltaY();
                scr.setMove(x, y);
                
                if (firstMove)
                {
                    x1 = x;
                    y1 = y;
                    x2 = x;
                    y2 = y;
                    firstMove = false;
                }
            }
            else if (r instanceof StraightEdgeRecord)
            {
                StraightEdgeRecord ser = (StraightEdgeRecord)r;
                x = x + ser.getDeltaX();
                y = y + ser.getDeltaY();
            }
            else if (r instanceof CurvedEdgeRecord)
            {
                CurvedEdgeRecord cer = (CurvedEdgeRecord)r;
                
                Rect currRect = new Rect(x1, x2, y1, y2);
                if (!curveControlPointInsideCurrentRect(x, y, cer, currRect))
                {                
                	Rect curvBounds = computeCurveBounds(x, y, cer);
                
                	if (curvBounds.xMin() < x1) x1 = curvBounds.xMin();
                	if (curvBounds.yMin() < y1) y1 = curvBounds.yMin();
                	if (curvBounds.xMax() > x2) x2 = curvBounds.xMax();
                	if (curvBounds.yMax() > y2) y2 = curvBounds.yMax();
                }
                                
                x = x + cer.getControlDeltaX() + cer.getAnchorDeltaX();
                y = y + cer.getControlDeltaY() + cer.getAnchorDeltaY();
            }

            //update x1, y1 to min values and x2, y2 to max values
            if (x < x1) x1 = x;
            if (y < y1) y1 = y;
            if (x > x2) x2 = x;
            if (y > y2) y2 = y;
        }
        
        Rect newRect = new Rect(x1, x2, y1, y2);

        if (ls == null)
        {
            return newRect;
        }
        
        // Inflate the bounding box from all sides with half of the stroke 
        // weight - pathBBox.inflate(weight/2, weight/2).
        Rect strokeExtents = getStrokeExtents(strokeNode, ls);

        newRect = new Rect(newRect.xMin() - strokeExtents.xMax(), newRect.xMax() + strokeExtents.xMax(), 
                newRect.yMin() - strokeExtents.yMax(), newRect.yMax() + strokeExtents.yMax());
        
        
        // If there are less than two segments, then or joint style is not 
        //"miterLimit" finish - return pathBBox.
        if (ls instanceof LineStyle2)
        {
            if (records.size() < 2 || ls == null || ((LineStyle2)ls).getJoinStyle() != LineStyle2.JS_MITER_JOIN)
            {
            	return newRect;
            }
        }
        // Use strokeExtents to get the transformed stroke weight.
        double halfWeight = (strokeExtents.xMax() - strokeExtents.xMin())/2.0;   
        newRect = addJoint2Bounds(records, ls, strokeNode, halfWeight, newRect);
        return newRect;
    }
    
    public static Rect addJoint2Bounds(List<ShapeRecord> records, LineStyle ls, AbstractStrokeNode stroke, double halfWeight, Rect pathBBox)
    {
        Rect newRect = pathBBox;
        int count = records.size();
	    int start = 0;
	    int end = 0;
	    int lastMoveX = 0;
	    int lastMoveY = 0;
	    int lastOpenSegment = 0;
	    int x = 0, y = 0;
	    
        // Add miterLimit effect to the bounds.
	    double miterLimit = stroke.miterLimit;
        // Miter limit is always at least 1
        if (miterLimit < 1) miterLimit = 1;     
        
        int[][] cooridinates = getCoordinates(records);

        while (true)
        {
            // Find a segment with a valid tangent or stop at a MoveSegment
            while (start < count && !(records.get(start) instanceof StyleChangeRecord))
            {
                x = cooridinates[start-1][0];
                y = cooridinates[start-1][1];
                if (tangentIsValid(records.get(start), x, y))
                    break;
        
                start++;
            }

            if (start >= count)
                break; // No more segments with valid tangents

            ShapeRecord startSegment = records.get(start);
            if (startSegment instanceof StyleChangeRecord)
            {
                // remember the last move segment 
                lastOpenSegment = start + 1;
                lastMoveX = ((StyleChangeRecord)startSegment).getMoveDeltaX();
                lastMoveY = ((StyleChangeRecord)startSegment).getMoveDeltaY();
                
                // move onto next segment:
                start++;
                continue;
            }

            // Does the current segment close to a previous segment and form a 
            // joint with it? 
            // Note, even if the segment was originally a close segment, 
            // it may not form a joint with the segment it closes to, unless 
            // it's followed by a MoveSegment or it's the last segment in the 
            // sequence.
            int startSegmentX = cooridinates[start][0];
            int startSegmentY = cooridinates[start][1];
            if ((start == count - 1 || records.get(start + 1) instanceof StyleChangeRecord) && 
                    startSegmentX == lastMoveX &&
                    startSegmentY == lastMoveY)
            {
                end = lastOpenSegment;
            }
            else
            {
                end = start + 1;
            }
            
            // Find a segment with a valid tangent or stop at a MoveSegment 
            while (end < count && !(records.get(end) instanceof StyleChangeRecord))
            {       
                if (tangentIsValid(records.get(end), startSegmentX, startSegmentY))
                    break;
                
                end++;
            }

            if (end >= count)
                break; // No more segments with valid tangents

            ShapeRecord endSegment = records.get(end);
            if (!(endSegment instanceof StyleChangeRecord))
            {
                newRect = addMiterLimitStrokeToBounds(
                                            startSegment,
                                            endSegment, 
                                            miterLimit,
                                            halfWeight,
                                            newRect, x, y, startSegmentX, startSegmentY);
            }

            // Move on to the next segment, but never go back (end could be 
            // less than start, because of implicit/explicit CloseSegments)
            start = start > end ? start + 1 : end;
        }

        return newRect;
    }
    
    private static int[][] getCoordinates(List<ShapeRecord> records)
    {
        int[][] coordinates = new int[records.size()][2];
        ShapeRecord record;
        for(int i=0; i<records.size(); i++)
        {
            record = records.get(i);
            if (record instanceof StyleChangeRecord)
            {
                StyleChangeRecord scr = (StyleChangeRecord)record;
                coordinates[i][0] = scr.getMoveDeltaX();
                coordinates[i][1] = scr.getMoveDeltaY();
            }
            else if (record instanceof StraightEdgeRecord)
            {
                StraightEdgeRecord ser = (StraightEdgeRecord)record;
                coordinates[i][0] = coordinates[i-1][0] + ser.getDeltaX();
                coordinates[i][1] = coordinates[i-1][1] + ser.getDeltaY();
            }
            else if (record instanceof CurvedEdgeRecord)
            {
                CurvedEdgeRecord cer = (CurvedEdgeRecord)record;                    
                coordinates[i][0] = coordinates[i-1][0] + cer.getControlDeltaX() + cer.getAnchorDeltaX();
                coordinates[i][1] = coordinates[i-1][1] + cer.getControlDeltaY() + cer.getAnchorDeltaY();
            }                  
        }
        return coordinates;
    }
                         
                         
    public static Rect addMiterLimitStrokeToBounds(ShapeRecord segment1, 
            ShapeRecord segment2, double miterLimit, double halfWeight, Rect pathBBox,
            int xPrev, int yPrev, int x, int y)
    {
        // The tip of the joint
        Point jointPoint = new Point(x, y);
        
        //If a joint lies miterLimit*strokeWeight/2 away from pathBox, 
        //it is considered an inner joint and has no effect on bounds. So stop  
        //processing in this case.        
        if (isInnerJoint(jointPoint, pathBBox, miterLimit, halfWeight))
        {
            return pathBBox;
        }
        
        // End tangent for segment1:
        Point t0 = getTangent(segment1, false /*start*/, xPrev, yPrev);
  
        // Start tangent for segment2:
        Point t1 = getTangent(segment2, true /*start*/, x, y);
   
        // Valid tangents?
        if (getPointLength(t0) == 0 || getPointLength(t1) == 0)
        {
            return pathBBox;
        }

        // The tip of the stroke lies on the bisector of the angle and lies at 
        // a distance of weight / sin(A/2), where A is the angle between the 
        // tangents.
        t0 = normalize(t0, 1);
        t0.x = -t0.x;
        t0.y = -t0.y;
        t1 = normalize(t1, 1);
        
        // Find the vector from t0 to the midPoint from t0 to t1
        Point halfT0T1 = new Point((t1.x - t0.x) * 0.5, (t1.y - t0.y) * 0.5);
   
        // sin(A/2) == halfT0T1.length / t1.length()
        double sinHalfAlpha = getPointLength(halfT0T1);
        if (Math.abs(sinHalfAlpha) < 1.0E-9)
        {
            // Don't count degenerate joints that are close to 0 degrees so
            // we avoid cases like this one L 0 0  0 50  100 0  30 0 50 0 Z
            return pathBBox;
        }

        // Find the vector of the bisect
        Point bisect = new Point(-0.5 * (t0.x + t1.x), -0.5 * (t0.y + t1.y));
        double bisectLength = getPointLength(bisect);
        if (bisectLength == 0)
        {
            // 180 degrees, nothing to contribute
            return pathBBox;
        }
   
        Rect newRect = pathBBox;
        // Is there miter limit at play?
        if (sinHalfAlpha == 0 || miterLimit < 1 / sinHalfAlpha)
        {
            // The miter limit is reached. Calculate two extra points that may
            // contribute to the bounds.
            // The points lie on the line perpendicular to the bisect and 
            // intersecting it at offset of miterLimit * weight from the 
            // joint tip. The points are equally offset from the bisect by a 
            // factor of X, where X / sinAlpha == (weight / sinAlpha - 
            // miterLimit * weight) / bisect.lenght. 
   
            bisect = normalize(bisect, 1);
            halfT0T1 = normalize(halfT0T1, (halfWeight - miterLimit * halfWeight * sinHalfAlpha) / bisectLength);

            Point pt0 = new Point(jointPoint.x + miterLimit * halfWeight * bisect.x + halfT0T1.x,
                   jointPoint.y + miterLimit * halfWeight * bisect.y + halfT0T1.y);

            Point pt1 = new Point(jointPoint.x + miterLimit * halfWeight * bisect.x - halfT0T1.x,
                   jointPoint.y + miterLimit * halfWeight * bisect.y - halfT0T1.y);

            // Add it to the rectangle:
            newRect = rectUnion((int)StrictMath.rint(pt0.x), (int)StrictMath.rint(pt0.y), 
                    (int)StrictMath.rint(pt0.x), (int)StrictMath.rint(pt0.y), newRect);
            newRect = rectUnion((int)StrictMath.rint(pt1.x), (int)StrictMath.rint(pt1.y), 
                    (int)StrictMath.rint(pt1.x), (int)StrictMath.rint(pt1.y), newRect);
        }
        else
        {
            // miter limit is not reached, add the tip of the stroke
            bisect = normalize(bisect, 1);
            Point strokeTip = new Point(jointPoint.x + bisect.x * halfWeight / sinHalfAlpha,
                   jointPoint.y + bisect.y * halfWeight / sinHalfAlpha);
   
            // Add it to the rectangle:
            newRect = rectUnion((int)StrictMath.rint(strokeTip.x), (int)StrictMath.rint(strokeTip.y), 
                    (int)StrictMath.rint(strokeTip.x), (int)StrictMath.rint(strokeTip.y), newRect);
        }
        return newRect;
    } 
    
    /**
     * Returns true when a joint is an inner joint (lies  
     * miterLimit*strokeWeight/2 away from pathBox).
     * @param jointPoint
     * @param miterLimit
     * @param weight
     * @return
     */
    private static boolean isInnerJoint(Point jointPoint, Rect pathBBox, double miterLimit, double halfWeight)
    {
        //If a joint lies miterLimit*strokeWeight/2 away from pathBox, 
        //it is considered an inner joint and has no effect on bounds.              
        if ((jointPoint.x - pathBBox.xMin())>miterLimit*halfWeight &&
                (pathBBox.xMax() - jointPoint.x)>miterLimit*halfWeight &&
                (jointPoint.y - pathBBox.yMin())>miterLimit*halfWeight &&
                (pathBBox.yMax() - jointPoint.y)>miterLimit*halfWeight)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Returns true when we have a valid tangent for curSegment. Pass 
     * prevSegment to know what the starting point of curSegment is.
     * @param prevSegment
     * @param curSegment
     * @param matrix
     * @return true where there is a valid tangent for curSegment. Returns false
     * otherwise.
     */
    private static boolean tangentIsValid(ShapeRecord curSegment, int x, int y)
    {
        // Check the start tangent only. If it's valid,
        // then there is a valid end tangent as well.
        Point tangentPoint = getTangent(curSegment, true, x, y);
        return (tangentPoint.x != 0 || tangentPoint.y != 0);
    }

    private static Point getTangent(ShapeRecord curSegment, boolean start, int x, int y)
    {
    	Point tangentPoint = new Point();
    	Point pt0 = new Point(x, y);
    	
    	if (curSegment instanceof StraightEdgeRecord)
    	{
	        Point pt1 = new Point(x+((StraightEdgeRecord)curSegment).getDeltaX(), y+((StraightEdgeRecord)curSegment).getDeltaY());
	    	tangentPoint.x = pt1.x - pt0.x;
	    	tangentPoint.y = pt1.y - pt0.y;
    	}
    	else if (curSegment instanceof CurvedEdgeRecord)
    	{
            Point pt1 = new Point(x+((CurvedEdgeRecord)curSegment).getControlDeltaX(), y+((CurvedEdgeRecord)curSegment).getControlDeltaY());
            Point pt2 = new Point(pt1.x+((CurvedEdgeRecord)curSegment).getAnchorDeltaX(), pt1.y+((CurvedEdgeRecord)curSegment).getAnchorDeltaY());          
	    	tangentPoint = getQTangent(pt0.x, pt0.y, pt1.x, pt1.y, pt2.x, pt2.y, start);
    	}   	
    	return tangentPoint;
    }

    private static Point getQTangent(double x0, double y0, double x1, double y1, double x2, double y2, boolean start)
    {
    	Point tangentPoint = new Point();
		if (start)
		{
			if (x0 == x1 && y0 == y1)
			{
			    tangentPoint.x = x2 - x0;
			    tangentPoint.y = y2 - y0;
			}
			else
			{
			    tangentPoint.x = x1 - x0;
			    tangentPoint.y = y1 - y0;
			}
		}
		else
		{
			if (x2 == x1 && y2 == y1)
			{
			    tangentPoint.x = x2 - x0;
				tangentPoint.y = y2 - y0;
			}
			else
			{
			    tangentPoint.x = x2 - x1;
				tangentPoint.y = y2 - y1;
			}
		}
		return tangentPoint;
	} 
    
    /**
     * Normalize a point. Scales the line segment between (0,0) and the current 
     * point to a set length. For example, if the current point is (0,5), and 
     * you normalize it to 1, the point returned is at (0,1).
     */
    public static Point normalize(Point p, double length)
    {
        double len = Math.sqrt(p.x * p.x + p.y * p.y);
        length = length/len; 
        return new Point(p.x * length, p.y * length); 
    }

    /**
     * Get length of a point.
     */
    public static double getPointLength(Point p)
    {
        double length;
        if (p.x == 0)
        {
            length = p.y;
        }
        else
        {
            length = Math.sqrt(p.x*p.x + p.y*p.y);
        }
        return length;
    }    
    /**
     *  @return Returns the union of <code>rect</code> and
     *  <code>Rectangle(left, top, right - left, bottom - top)</code>.
     *  Note that if rect is non-null, it will be updated to reflect the return value.  
     */
    private static Rect rectUnion(int left, int top, int right, int bottom, Rect rect)
    {
        if (rect == null)
        {
            return new Rect(left, right, top, bottom);
        }
        return new Rect(Math.min(rect.xMin(), left), Math.max(rect.xMax(), right),
                Math.min(rect.yMin(), top), Math.max(rect.yMax(), bottom));        
    }
    
    private static Rect getStrokeExtents(AbstractStrokeNode stroke, LineStyle ls )
    {
        // TODO: currently we take only scale into account,
        // but depending on joint style, cap style, etc. we need to take
        // the whole matrix into account as well as examine every line segment.
        if (stroke == null)
        {
            return new Rect(0, 0, 0 , 0);
        }
        
        int xMin, xMax, yMin, yMax;
        // Stroke with weight 0 or scaleMode "none" is always drawn
        // at "hairline" thickness, which is exactly one pixel.
        int lineWidth = ls.getWidth();   
        if (lineWidth == 0)
        {
            xMin = (int)StrictMath.rint(-0.5 * TWIPS_PER_PIXEL);
            xMax = (int)StrictMath.rint(0.5 * TWIPS_PER_PIXEL);
            yMin = (int)StrictMath.rint(-0.5 * TWIPS_PER_PIXEL);
            yMax = (int)StrictMath.rint(0.5 * TWIPS_PER_PIXEL);
        }
        else
        {
            xMin = (int)StrictMath.rint(-lineWidth * 0.5);
            xMax = (int)StrictMath.rint(lineWidth * 0.5);
            yMin = (int)StrictMath.rint(-lineWidth * 0.5);
            yMax = (int)StrictMath.rint(lineWidth * 0.5);   
        }
        return new Rect(xMin, xMax, yMin, yMax);
    }    
    
    
    private static Rect computeCurveBounds(int x0, int y0, CurvedEdgeRecord curve)
    {
        int x1 = x0 + curve.getControlDeltaX();
        int y1 = y0 + curve.getControlDeltaY();
        int x2 = x1 + curve.getAnchorDeltaX();
        int y2 = y1 + curve.getAnchorDeltaY();
        
        //initialize xmin, ymin, xmax, ymax to the anchor points of curve
        int xmin = x0, xmax = x0;
        int ymin = y0, ymax = y0;
        if (x2 < xmin) xmin = x2;
        if (y2 < ymin) ymin = y2;
        if (x2 > xmax) xmax = x2;
        if (y2 > ymax) ymax = y2;
     
        //compute t at extrema point for x and the corresponding x, y values 
        double t = computeTExtrema(x0, x1, x2);
        if (Double.isNaN(t))
        {
            //use control point
            if (x1 < xmin) xmin = x1;
            if (y1 < ymin) ymin = y1;
            if (x1 > xmax) xmax = x1;
            if (y1 > ymax) ymax = y1;
        }
        else if ((t > 0) && (t < 1))
        {
            int x, y;
            x = computeValueForCurve(x0, x1, x2, t);
            y = computeValueForCurve(y0, y1, y2, t);
            if (x < xmin) xmin = x;
            if (y < ymin) ymin = y;
            if (x > xmax) xmax = x;
            if (y > ymax) ymax = y;
        }
        
        //compute t at extrema point for y and the corresponding x, y values 
        t = computeTExtrema(y0, y1, y2);
        if (Double.isNaN(t))
        {
            //use control point
            if (x1 < xmin) xmin = x1;
            if (y1 < ymin) ymin = y1;
            if (x1 > xmax) xmax = x1;
            if (y1 > ymax) ymax = y1;
        }
        else if ((t > 0) && (t < 1))
        {
            int x, y;
            x = computeValueForCurve(x0, x1, x2, t);
            y = computeValueForCurve(y0, y1, y2, t);
            if (x < xmin) xmin = x;
            if (y < ymin) ymin = y;
            if (x > xmax) xmax = x;
            if (y > ymax) ymax = y;
        }
        
        Rect r = new Rect(xmin, xmax, ymin, ymax);        
        return r;
    }
    
    private static boolean curveControlPointInsideCurrentRect(int x0, int y0, CurvedEdgeRecord curve, Rect currRect)
    {
        int x = x0 + curve.getControlDeltaX();
        int y = y0 + curve.getControlDeltaY();
         
        //initialize xmin, ymin, xmax, ymax to the control points of curve
        int xmin = x0, xmax = x0;
        int ymin = y0, ymax = y0;
        if (x < xmin) xmin = x;
        if (y < ymin) ymin = y;
        if (x > xmax) xmax = x;
        if (y > ymax) ymax = y;
        
        if ((currRect.xMin() < xmin) && (currRect.xMax() > xmax) && (currRect.yMin() < ymin) && (currRect.yMax() > ymax))
            return true;
        else
            return false;      
    }
    
    //compute value for quadratic bezier curve at t
    // the quadratic bezier curve is p0*(1-t)^2 + 2*p1*(1-t)*t + p2*t^2 
    private static int computeValueForCurve(int p0, int p1, int p2, double t)
    {
        return (int)(p0*(1-t)*(1-t) + 2*p1*(1-t)*t + p2*t*t);
        
    }
    
    //compute the extrema which corresponds to derivative equal to 0
    private static double computeTExtrema(int p0, int p1, int p2)
    {
        // the quadratic bezier curve  is p0*(1-t)^2 + 2*p1*(1-t)*t + p2*t^2, 
        // its first derivative (with respect to t) is 2*(p0 - 2*p1 + p2)*t + 2*(p1 - p0),
        // which is zero for t = (p0 - p1)/(p0 - 2*p1 + p2)
        
        int denom = (p0 - 2*p1 + p2);
        if (denom == 0)
        {
            //cannot compute the derivative - use the control point for extrema
            return Double.NaN;
        }
        else
        {
            double t = (p0 - p1)/(double) denom;
            return t;
        }
        
    }
    
    private static double[] getCornerRadius(double cornerRadiusX, double cornerRadiusY, 
    		double radiusX, double radiusY, double width, double height)
    {
    	double[] newRadius = new double[2];
        if (Double.isNaN(cornerRadiusX))
        {
        	cornerRadiusX = radiusX; 
        	if (Double.isNaN(cornerRadiusY))
        		cornerRadiusY = radiusY;
        	else
        		cornerRadiusY = cornerRadiusX;
        }
        else if (Double.isNaN(cornerRadiusY))
        {
        	cornerRadiusY = cornerRadiusX;
        }
        if ( cornerRadiusX > width/2.0 )
        	cornerRadiusX = width/2.0;
        if ( cornerRadiusY > height/2.0 )
        	cornerRadiusY = height/2.0;     
        
        newRadius[0] = cornerRadiusX;
        newRadius[1] = cornerRadiusY;
        return newRadius;
    }
    
}
