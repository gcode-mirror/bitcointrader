package com.jjoe64.graphview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.compatible.ScaleGestureDetector;
import de.dev.eth0.bitcointrader.R;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * GraphView is a Android View for creating zoomable and scrollable graphs. This is the abstract base class for all graphs. Extend this class and implement
 * {@link #drawSeries(Canvas, GraphViewData[], float, float, float, double, double, double, double, float)} to display a custom graph. Use {@link LineGraphView}
 * for creating a line chart.
 *
 * @author jjoe64 - jonas gehring - http://www.jjoe64.com
 *
 * Copyright (C) 2011 Jonas Gehring Licensed under the GNU Lesser General Public License (LGPL) http://www.gnu.org/licenses/lgpl.html
 */
public abstract class GraphView extends LinearLayout {

  private static final String TAG = GraphView.class.getSimpleName();

  static final private class GraphViewConfig {

    static final float BORDER = 20;
  }

  private class GraphViewContentView extends View {

    private float lastTouchEventX;
    private float graphwidth;

    /**
     * @param context
     */
    public GraphViewContentView(Context context) {
      super(context);
      setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    /**
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {

      paint.setAntiAlias(true);


      // normal
      paint.setStrokeWidth(0);

      float border = GraphViewConfig.BORDER + paint.getTextSize() / 2;
      // increase border if offset is enabled
      if (horizontalLabelOffset) {
        border += paint.getTextSize();
      }
      float horstart = 0;
      float height = getHeight();
      double maxY = getMaxY();
      double minY = getMinY();
      double maxX = getMaxX(false);
      double minX = getMinX(false);
      double diffX = maxX - minX;
      float graphheight = height - (2 * border);
      graphwidth = getWidth() - 1;

      if (horlabels == null) {
        horlabels = generateHorlabels(graphwidth);
      }
      if (verlabels == null) {
        verlabels = generateVerlabels(graphheight);
      }

      // horizontal lines
      paint.setTextAlign(Align.LEFT);
      int vers = verlabels.length - 1;
      for (int i = 0; i < verlabels.length; i++) {
        paint.setColor(graphViewStyle.getGridColor());
        float y = (((graphheight / vers) * i) + border);
        canvas.drawLine(horstart, y, graphwidth, y, paint);
      }

      // vertical lines and horizonal labels
      int hors = horlabels.length - 1;
      for (int i = 0; i < horlabels.length; i++) {
        paint.setColor(graphViewStyle.getGridColor());
        float x = ((graphwidth / hors) * i) + horstart;
        canvas.drawLine(x, height - border, x, border, paint);
        paint.setTextAlign(Align.CENTER);
        if (i == horlabels.length - 1) {
          paint.setTextAlign(Align.RIGHT);
        }
        if (i == 0) {
          paint.setTextAlign(Align.LEFT);
        }
        paint.setColor(graphViewStyle.getHorizontalLabelsColor());
        // display labels offset
        if (horizontalLabelOffset && (i % 2) == 0) {
          canvas.drawText(horlabels[i], x, height - 4 - paint.getTextSize(), paint);
        }
        else {
          canvas.drawText(horlabels[i], x, height - 4, paint);
        }
      }

      paint.setTextAlign(Align.CENTER);
      canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);

      if (maxY == minY) {
        // if min/max is the same, fake it so that we can render a line
        maxY = maxY * 1.05d;
        minY = minY * 0.95d;
      }

      double diffY = maxY - minY;
      paint.setStrokeCap(Paint.Cap.ROUND);

      for (int i = 0; i < graphSeries.size(); i++) {
        drawSeries(canvas, _values(i), graphwidth, graphheight, border, minX, minY, diffX, diffY, horstart, graphSeries.get(i).style);
      }

      if (showLegend) {
        drawLegend(canvas, height, graphwidth);
      }
    }

    private void onMoveGesture(float f) {
      // view port update
      if (viewportSize != 0) {
        viewportStart -= f * viewportSize / graphwidth;

        // minimal and maximal view limit
        double minX = getMinX(true);
        double maxX = getMaxX(true);
        if (viewportStart < minX) {
          viewportStart = minX;
        }
        else if (viewportStart + viewportSize > maxX) {
          viewportStart = maxX - viewportSize;
        }

        // labels have to be regenerated
        horlabels = null;
        verlabels = null;
        viewVerLabels.invalidate();
      }
      invalidate();
    }

    /**
     * @param event
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
      if (!isScrollable() || isDisableTouch()) {
        return super.onTouchEvent(event);
      }

      boolean handled = false;
      // first scale
      if (scalable && scaleDetector != null) {
        scaleDetector.onTouchEvent(event);
        handled = scaleDetector.isInProgress();
      }
      if (!handled) {
        // if not scaled, scroll
        if ((event.getAction() & MotionEvent.ACTION_DOWN) == MotionEvent.ACTION_DOWN) {
          handled = true;
        }
        if ((event.getAction() & MotionEvent.ACTION_UP) == MotionEvent.ACTION_UP) {
          lastTouchEventX = 0;
          handled = true;
        }
        if ((event.getAction() & MotionEvent.ACTION_MOVE) == MotionEvent.ACTION_MOVE) {
          if (lastTouchEventX != 0) {
            onMoveGesture(event.getX() - lastTouchEventX);
          }
          lastTouchEventX = event.getX();
          handled = true;
        }
        if (handled) {
          invalidate();
        }
      }
      return handled;
    }
  }

  /**
   * one data set for a graph series
   */
  static public class GraphViewData {

    public final double valueX;
    public final double valueY;

    public GraphViewData(double valueX, double valueY) {
      super();
      this.valueX = valueX;
      this.valueY = valueY;
    }
  }

  public enum LegendAlign {

    TOP, MIDDLE, BOTTOM
  }

  private class VerLabelsView extends View {

    /**
     * @param context
     */
    public VerLabelsView(Context context) {
      super(context);
      setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 10));
    }

    /**
     * @param canvas
     */
    @Override
    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
      // normal
      paint.setStrokeWidth(0);

      float border = GraphViewConfig.BORDER + paint.getTextSize() / 2;
      if (horizontalLabelOffset) {
        border += paint.getTextSize();
      }
      float height = getHeight();
      float graphheight = height - (2 * border);

      if (verlabels == null) {
        verlabels = generateVerlabels(graphheight);
      }
      // resize to match longest label
      float maxLabelLength = Integer.MIN_VALUE;
      for (String label : verlabels) {
        maxLabelLength = Math.max(paint.measureText(label), maxLabelLength);
      }
      if (maxLabelLength != Integer.MIN_VALUE) {
        maxLabelLength += GraphViewConfig.BORDER;
        setLayoutParams(new LayoutParams((int) maxLabelLength, LayoutParams.MATCH_PARENT));
      }


      // vertical labels, don't print last one as it might colide with horizontal label
      paint.setTextAlign(Align.LEFT);
      paint.setColor(graphViewStyle.getVerticalLabelsColor());
      int vers = verlabels.length - 1;
      for (int i = 0; i < verlabels.length; i++) {
        // calculate y postion of text, center text to lines
        float y = (((graphheight / vers) * i) + border) + (paint.getTextSize() / 2);
        canvas.drawText(verlabels[i], 0, y, paint);
      }
    }
  }
  protected final Paint paint;
  private String[] horlabels;
  private String[] verlabels;
  private String title;
  private boolean horizontalLabelOffset = false;
  private boolean scrollable;
  private boolean disableTouch;
  private double viewportStart;
  private double viewportSize;
  private final View viewVerLabels;
  private ScaleGestureDetector scaleDetector;
  private boolean scalable;
  private final Format[] numberformatter = new Format[2];
  private final Format[] customFormatter = new Format[2];
  private final List<GraphViewSeries> graphSeries;
  private boolean showLegend = false;
  private float legendWidth = 120;
  private LegendAlign legendAlign = LegendAlign.MIDDLE;
  private boolean manualYAxis;
  private double manualMaxYValue;
  private double manualMinYValue;
  private GraphViewStyle graphViewStyle;
  private GraphViewContentView graphViewContentView;

  public GraphView(Context context, AttributeSet attrs) {
    this(context, attrs.getAttributeValue(null, "title"));
  }

  /**
   *
   * @param context
   * @param title [optional]
   */
  public GraphView(Context context, String title) {
    super(context);
    setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    if (title == null) {
      this.title = "";
    }
    else {
      this.title = title;
    }

    graphViewStyle = new GraphViewStyle();

    paint = new Paint();
    paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_size_small));
    graphSeries = new ArrayList<GraphViewSeries>();

    viewVerLabels = new VerLabelsView(context);
    addView(viewVerLabels);
    graphViewContentView = new GraphViewContentView(context);
    addView(graphViewContentView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
  }

  public GraphViewStyle getGraphViewStyle() {
    return graphViewStyle;
  }

  public void setGraphViewStyle(GraphViewStyle style) {
    graphViewStyle = style;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setHorizontalLabelsOffset(boolean offset) {
    this.horizontalLabelOffset = offset;
  }

  public void setTextSize(float textsize) {
    paint.setTextSize(textsize);
  }

  public void setFormatter(Format format, boolean isXFormatter) {
    customFormatter[isXFormatter ? 1 : 0] = format;
  }

  private GraphViewData[] _values(int idxSeries) {
    GraphViewData[] values = graphSeries.get(idxSeries).values;
    if (viewportStart == 0 && viewportSize == 0) {
      // all data
      return values;
    }
    else {
      // viewport
      List<GraphViewData> listData = new ArrayList<GraphViewData>();
      for (int i = 0; i < values.length; i++) {
        if (values[i].valueX >= viewportStart) {
          if (values[i].valueX > viewportStart + viewportSize) {
            listData.add(values[i]); // one more for nice scrolling
            break;
          }
          else {
            listData.add(values[i]);
          }
        }
        else {
          if (listData.isEmpty()) {
            listData.add(values[i]);
          }
          listData.set(0, values[i]); // one before, for nice scrolling
        }
      }
      return listData.toArray(new GraphViewData[listData.size()]);
    }
  }

  public void addSeries(GraphViewSeries series) {
    series.addGraphView(this);
    graphSeries.add(series);
  }

  protected void drawLegend(Canvas canvas, float height, float width) {
    int shapeSize = 15;

    // rect
    paint.setARGB(180, 100, 100, 100);
    float legendHeight = (shapeSize + 5) * graphSeries.size() + 5;
    float lLeft = width - legendWidth - 10;
    float lTop;
    switch (legendAlign) {
      case TOP:
        lTop = 10;
        break;
      case MIDDLE:
        lTop = height / 2 - legendHeight / 2;
        break;
      default:
        lTop = height - GraphViewConfig.BORDER - legendHeight - 10;
    }
    float lRight = lLeft + legendWidth;
    float lBottom = lTop + legendHeight;
    canvas.drawRoundRect(new RectF(lLeft, lTop, lRight, lBottom), 8, 8, paint);

    for (int i = 0; i < graphSeries.size(); i++) {
      paint.setColor(graphSeries.get(i).style.color);
      canvas.drawRect(new RectF(lLeft + 5, lTop + 5 + (i * (shapeSize + 5)), lLeft + 5 + shapeSize, lTop + ((i + 1) * (shapeSize + 5))), paint);
      if (graphSeries.get(i).description != null) {
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Align.LEFT);
        canvas.drawText(graphSeries.get(i).description, lLeft + 5 + shapeSize + 5, lTop + shapeSize + (i * (shapeSize + 5)), paint);
      }
    }
  }

  abstract public void drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight, float border, double minX, double minY, double diffX, double diffY, float horstart, GraphViewSeriesStyle style);

  /**
   * formats the label can be overwritten
   *
   * @param value x and y values
   * @param isValueX if false, value y wants to be formatted
   * @return value to display
   */
  protected String formatLabel(double value, boolean isValueX) {
    int i = isValueX ? 1 : 0;
    if (numberformatter[i] == null) {
      if (customFormatter[i] != null) {
        numberformatter[i] = customFormatter[i];
      }
      else {
        NumberFormat numFormat = NumberFormat.getNumberInstance();
        double highestvalue = isValueX ? getMaxX(false) : getMaxY();
        double lowestvalue = isValueX ? getMinX(false) : getMinY();
        if (highestvalue - lowestvalue < 0.1) {
          numFormat.setMaximumFractionDigits(6);
        }
        else if (highestvalue - lowestvalue < 1) {
          numFormat.setMaximumFractionDigits(4);
        }
        else if (highestvalue - lowestvalue < 20) {
          numFormat.setMaximumFractionDigits(3);
        }
        else if (highestvalue - lowestvalue < 100) {
          numFormat.setMaximumFractionDigits(1);
        }
        else {
          numFormat.setMaximumFractionDigits(0);
        }
        numberformatter[i] = numFormat;
      }
    }
    String formatedLabel = numberformatter[i].format(value);
    Log.d(TAG, "formating label " + value + " on x axis " + isValueX + " to " + formatedLabel);
    return formatedLabel;
  }

  private String[] generateHorlabels(float graphwidth) {
    int numLabels = (int)(graphwidth / getResources().getDimensionPixelSize(R.dimen.graph_horizontal_label_width));
    String[] labels = new String[numLabels + 1];
    double min = getMinX(false);
    double max = getMaxX(false);
    for (int i = 0; i <= numLabels; i++) {
      labels[i] = formatLabel(min + ((max - min) * i / numLabels), true);
    }
    return labels;
  }

  synchronized private String[] generateVerlabels(float graphheight) {
    int numLabels = (int)(graphheight / getResources().getDimensionPixelSize(R.dimen.graph_vertical_label_height));
    String[] labels = new String[numLabels + 1];
    double min = getMinY();
    double max = getMaxY();
    if (max == min) {
      // if min/max is the same, fake it so that we can render a line
      max = max * 1.05d;
      min = min * 0.95d;
    }

    for (int i = 0; i <= numLabels; i++) {
      labels[numLabels - i] = formatLabel(min + ((max - min) * i / numLabels), false);
    }
    return labels;
  }

  public LegendAlign getLegendAlign() {
    return legendAlign;
  }

  public float getLegendWidth() {
    return legendWidth;
  }

  /**
   * returns the maximal X value of the current viewport (if viewport is set) otherwise maximal X value of all data.
   *
   * @param ignoreViewport
   *
   * warning: only override this, if you really know want you're doing!
   */
  protected double getMaxX(boolean ignoreViewport) {
    // if viewport is set, use this
    if (!ignoreViewport && viewportSize != 0) {
      return viewportStart + viewportSize;
    }
    else {
      // otherwise use the max x value
      // values must be sorted by x, so the last value has the largest X value
      double highest = 0;
      if (graphSeries.size() > 0) {
        GraphViewData[] values = graphSeries.get(0).values;
        if (values.length == 0) {
          highest = 0;
        }
        else {
          highest = values[values.length - 1].valueX;
          for (int i = 1; i < graphSeries.size(); i++) {
            values = graphSeries.get(i).values;
            highest = Math.max(highest, values[values.length - 1].valueX);
          }
        }
      }
      Log.d(TAG, "getMaxX() returns " + highest);
      return highest;
    }
  }

  /**
   * returns the maximal Y value of all data.
   *
   * warning: only override this, if you really know want you're doing!
   */
  protected double getMaxY() {
    double largest;
    if (manualYAxis) {
      largest = manualMaxYValue;
    }
    else {
      largest = Integer.MIN_VALUE;
      for (int i = 0; i < graphSeries.size(); i++) {
        GraphViewData[] values = _values(i);
        for (int ii = 0; ii < values.length; ii++) {
          if (values[ii].valueY > largest) {
            largest = values[ii].valueY;
          }
        }
      }
    }
    return largest;
  }

  /**
   * returns the minimal X value of the current viewport (if viewport is set) otherwise minimal X value of all data.
   *
   * @param ignoreViewport
   *
   * warning: only override this, if you really know want you're doing!
   */
  protected double getMinX(boolean ignoreViewport) {
    // if viewport is set, use this
    if (!ignoreViewport && viewportSize != 0) {
      return viewportStart;
    }
    else {
      // otherwise use the min x value
      // values must be sorted by x, so the first value has the smallest X value
      double lowest = 0;
      if (graphSeries.size() > 0) {
        GraphViewData[] values = graphSeries.get(0).values;
        if (values.length == 0) {
          lowest = 0;
        }
        else {
          lowest = values[0].valueX;
          for (int i = 1; i < graphSeries.size(); i++) {
            values = graphSeries.get(i).values;
            lowest = Math.min(lowest, values[0].valueX);
          }
        }
      }
      Log.d(TAG, "getMinX() returns " + lowest);
      return lowest;
    }
  }

  /**
   * returns the minimal Y value of all data.
   *
   * warning: only override this, if you really know want you're doing!
   */
  protected double getMinY() {
    double smallest;
    if (manualYAxis) {
      smallest = manualMinYValue;
    }
    else {
      smallest = Integer.MAX_VALUE;
      for (int i = 0; i < graphSeries.size(); i++) {
        GraphViewData[] values = _values(i);
        for (int ii = 0; ii < values.length; ii++) {
          if (values[ii].valueY < smallest) {
            smallest = values[ii].valueY;
          }
        }
      }
    }
    return smallest;
  }

  public boolean isScrollable() {
    return scrollable;
  }

  public boolean isDisableTouch() {
    return disableTouch;
  }

  public boolean isShowLegend() {
    return showLegend;
  }

  public void redrawAll() {
    verlabels = null;
    horlabels = null;
    numberformatter[0] = null;
    numberformatter[1] = null;
    invalidate();
    viewVerLabels.invalidate();
    graphViewContentView.invalidate();
  }

  public void removeSeries(GraphViewSeries series) {
    graphSeries.remove(series);
  }

  public void removeSeries(int index) {
    if (index < 0 || index >= graphSeries.size()) {
      throw new IndexOutOfBoundsException("No series at index " + index);
    }

    graphSeries.remove(index);
  }

  public void scrollToEnd() {
    if (!scrollable) {
      throw new IllegalStateException("This GraphView is not scrollable.");
    }
    double max = getMaxX(true);
    viewportStart = max - viewportSize;
    redrawAll();
  }

  /**
   * set's static horizontal labels (from left to right)
   *
   * @param horlabels if null, labels were generated automatically
   */
  public void setHorizontalLabels(String[] horlabels) {
    this.horlabels = horlabels;
  }

  public void setLegendAlign(LegendAlign legendAlign) {
    this.legendAlign = legendAlign;
  }

  public void setLegendWidth(float legendWidth) {
    this.legendWidth = legendWidth;
  }

  /**
   * you have to set the bounds {@link #setManualYAxisBounds(double, double)}. That automatically enables manualYAxis-flag. if you want to disable the menual y
   * axis, call this method with false.
   *
   * @param manualYAxis
   */
  public void setManualYAxis(boolean manualYAxis) {
    this.manualYAxis = manualYAxis;
  }

  /**
   * set manual Y axis limit
   *
   * @param max
   * @param min
   */
  public void setManualYAxisBounds(double max, double min) {
    manualMaxYValue = max;
    manualMinYValue = min;
    manualYAxis = true;
  }

  /**
   * this forces scrollable = true
   *
   * @param scalable
   */
  synchronized public void setScalable(boolean scalable) {
    this.scalable = scalable;
    if (scalable == true && scaleDetector == null) {
      scrollable = true; // automatically forces this
      scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
          double center = viewportStart + viewportSize / 2;
          viewportSize /= detector.getScaleFactor();
          viewportStart = center - viewportSize / 2;

          // viewportStart must not be < minX
          double minX = getMinX(true);
          if (viewportStart < minX) {
            viewportStart = minX;
          }

          // viewportStart + viewportSize must not be > maxX
          double maxX = getMaxX(true);
          double overlap = viewportStart + viewportSize - maxX;
          if (overlap > 0) {
            // scroll left
            if (viewportStart - overlap > minX) {
              viewportStart -= overlap;
            }
            else {
              // maximal scale
              viewportStart = minX;
              viewportSize = maxX - viewportStart;
            }
          }
          redrawAll();
          return true;
        }
      });
    }
  }

  /**
   * the user can scroll (horizontal) the graph. This is only useful if you use a viewport {@link #setViewPort(double, double)} which doesn't displays all data.
   *
   * @param scrollable
   */
  public void setScrollable(boolean scrollable) {
    this.scrollable = scrollable;
  }

  /**
   * The user can disable any touch gestures, this is useful if you are using a real time graph, but don't want the user to interact
   *
   * @param disableTouch
   */
  public void setDiscableTouch(boolean disableTouch) {
    this.disableTouch = disableTouch;
  }

  public void setShowLegend(boolean showLegend) {
    this.showLegend = showLegend;
  }

  /**
   * set's static vertical labels (from top to bottom)
   *
   * @param verlabels if null, labels were generated automatically
   */
  public void setVerticalLabels(String[] verlabels) {
    this.verlabels = verlabels;
  }

  /**
   * set's the viewport for the graph.
   *
   * @param start x-value
   * @param size
   */
  public void setViewPort(double start, double size) {
    viewportStart = start;
    viewportSize = size;
  }
}
