/*
 * This file is part of Doodle Android.
 *
 * Doodle Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Doodle Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Doodle Android. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2020-2021 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SvgDrawable {

  private final static String TAG = SvgDrawable.class.getSimpleName();

  private final static boolean ENABLE_IMAGES = true;

  private Context context;
  private HashMap<String, SvgObject> objectHashMap;
  private List<String> ids;
  private float offsetX;
  private float offsetY;
  private float scale;
  private float zoom;
  private float screenWidth, svgWidth, svgHeight, pixelUnit;
  private Paint paint;

  public SvgDrawable(Context context, @RawRes int resId) {
    this.context = context;

    DisplayMetrics metrics = new DisplayMetrics();
    WindowManager manager = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
    manager.getDefaultDisplay().getMetrics(metrics);
    int w = metrics.widthPixels;
    int h = metrics.heightPixels;
    screenWidth = Math.min(h, w);

    objectHashMap = new HashMap<>();
    ids = new ArrayList<>();

    try {
      parse(context.getResources().openRawResource(resId));
    } catch (IOException e) {
      Log.e(TAG, "SvgDrawable: " + e);
    }

    scale = 1;

    paint = new Paint();
  }

  @Nullable
  public SvgObject findObjectById(String id) {
    return objectHashMap.get(id);
  }

  public void setOffset(float offsetX) {
    setOffset(offsetX, 0);
  }

  public void setOffset(float offsetX, float offsetY) {
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }

  public void setScale(float scale) {
    this.scale = scale;
  }

  public void setZoom(float zoom) {
    this.zoom = zoom;
  }

  public void draw(Canvas canvas) {
    for (String id : ids) {
      SvgObject object = objectHashMap.get(id);
      if (object != null) {
        switch (object.type) {
          case SvgObject.TYPE_PATH:
            break;
          case SvgObject.TYPE_RECT:
            break;
          case SvgObject.TYPE_CIRCLE:
            drawCircle(canvas, object);
            break;
          case SvgObject.TYPE_IMAGE:
            if (ENABLE_IMAGES) {
              drawImage(canvas, object);
            }
            break;
        }
      }
    }
  }

  private void parse(InputStream inputStream) throws IOException {
    try {
      XmlPullParser parser = Xml.newPullParser();
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, ENABLE_IMAGES);
      parser.setInput(inputStream, null);
      parser.next();
      readSvg(parser);
    } catch (XmlPullParserException|IOException e) {
      Log.e(TAG, "parse: " + e);
    } finally {
      inputStream.close();
    }
  }

  private void readSvg(XmlPullParser parser) throws IOException, XmlPullParserException {
    parser.require(XmlPullParser.START_TAG, null, "svg");

    while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
      Log.i(TAG, "readSvg: hello" + parser.getEventType() + ", " + parser.getName());
      parser.next();
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      switch (parser.getName()) {
        case SvgObject.TYPE_PATH:
          readPath(parser);
          break;
        case SvgObject.TYPE_RECT:
          readRect(parser);
          break;
        case SvgObject.TYPE_CIRCLE:
          readCircle(parser);
          break;
        case SvgObject.TYPE_IMAGE:
          if (ENABLE_IMAGES) {
            readImage(parser);
          }
          break;
        default:
          //skip(parser);
          break;
      }
    }
  }

  private void readPath(XmlPullParser parser) throws IOException, XmlPullParserException {
    SvgObject object = new SvgObject(SvgObject.TYPE_PATH);
    parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_PATH);
    String tag = parser.getName();
    String id = parser.getAttributeValue(null, "id");
    if (tag.equals(SvgObject.TYPE_PATH)) {
      if (id != null){
        object.d = parser.getAttributeValue(null, "d");
        readStyle(parser, object);
        parser.nextTag();
      } else {
        return;
      }
    }
    parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_PATH);
    objectHashMap.put(id, object);
    ids.add(id);
  }

  private void readRect(XmlPullParser parser) throws IOException, XmlPullParserException {
    SvgObject object = new SvgObject(SvgObject.TYPE_RECT);
    parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_RECT);
    String tag = parser.getName();
    String id = parser.getAttributeValue(null, "id");
    if (tag.equals(SvgObject.TYPE_RECT)) {
      if (id != null){
        object.width = Float.parseFloat(parser.getAttributeValue(null, "width"));
        object.height = Float.parseFloat(parser.getAttributeValue(null, "height"));
        object.x = Float.parseFloat(parser.getAttributeValue(null, "x"));
        object.y = Float.parseFloat(parser.getAttributeValue(null, "y"));
        readStyle(parser, object);
        parser.nextTag();
      } else {
        return;
      }
    }
    parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_RECT);
    objectHashMap.put(id, object);
    ids.add(id);
  }

  private void readCircle(XmlPullParser parser) throws IOException, XmlPullParserException {
    SvgObject object = new SvgObject(SvgObject.TYPE_CIRCLE);
    parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_CIRCLE);
    String tag = parser.getName();
    String id = parser.getAttributeValue(null, "id");
    if (tag.equals(SvgObject.TYPE_CIRCLE)) {
      if (id != null){
        object.cx = Float.parseFloat(parser.getAttributeValue(null, "cx"));
        object.cy = Float.parseFloat(parser.getAttributeValue(null, "cy"));
        object.r = Float.parseFloat(parser.getAttributeValue(null, "r"));
        readStyle(parser, object);
        parser.nextTag();
      } else {
        return;
      }
    }
    parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_CIRCLE);
    objectHashMap.put(id, object);
    ids.add(id);
  }

  private void drawCircle(Canvas canvas, SvgObject object) {
    boolean fillAndStroke = applyPaintStyle(object, false);
    // draw with fill style (or stroke if only this is set)
    canvas.drawCircle(object.cx, object.cy, object.r, paint);
    // draw again with stroke style
    if (fillAndStroke) {
      applyPaintStyle(object, true);
      canvas.drawCircle(object.cx, object.cy, object.r, paint);
    }
  }

  private void readImage(XmlPullParser parser) throws IOException, XmlPullParserException {
    SvgObject object = new SvgObject(SvgObject.TYPE_IMAGE);
    parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_IMAGE);
    String tag = parser.getName();
    String id = parser.getAttributeValue(null, "id");
    if (tag.equals(SvgObject.TYPE_IMAGE)) {
      if (id != null){
        object.width = Float.parseFloat(parser.getAttributeValue(null, "width"));
        object.height = Float.parseFloat(parser.getAttributeValue(null, "height"));
        object.x = Float.parseFloat(parser.getAttributeValue(null, "x"));
        object.y = Float.parseFloat(parser.getAttributeValue(null, "y"));

        String image = parser.getAttributeValue(parser.getNamespace("xlink"), "href");
        if (image != null) {
          image = image.substring(image.indexOf(",") + 1);
          byte[] decoded = Base64.decode(image, Base64.DEFAULT);
          object.bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        }

        readStyle(parser, object);
        parser.nextTag();
      } else {
        return;
      }
    }
    parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_IMAGE);
    objectHashMap.put(id, object);
    ids.add(id);
  }

  private void drawImage(Canvas canvas, SvgObject object) {
    applyPaintStyle(object, false);
    canvas.drawBitmap(object.bitmap, object.x, object.y, paint); // TODO: get size from object
  }

  private void readStyle(XmlPullParser parser, SvgObject object) {
    String fill = parser.getAttributeValue(null, "fill");
    if (isValidColor(fill)) {
      object.fill = fill;
    }
    String stroke = parser.getAttributeValue(null, "stroke");
    if (isValidColor(stroke)) {
      object.stroke = stroke;
    }
    String strokeWidth = parser.getAttributeValue(null, "stroke-width");
    if (strokeWidth != null && !strokeWidth.isEmpty()) {
      object.strokeWidth = Float.parseFloat(strokeWidth);
    }
    object.strokeLineCap = parser.getAttributeValue(null, "stroke-linecap");
    object.strokeLineJoin = parser.getAttributeValue(null, "stroke-linejoin");
  }

  /**
   * @param object
   * @return true if a second draw for a separate stroke style is needed
   */
  private boolean applyPaintStyle(SvgObject object, boolean applyStrokeIfBothSet) {
    paint.reset();
    paint.setAntiAlias(true);

    boolean hasFill = object.fill != null;
    boolean hasStroke = object.stroke != null && object.strokeWidth > 0;
    boolean fillAndStroke = hasFill && hasStroke;

    if ((fillAndStroke && applyStrokeIfBothSet) || (!hasFill && hasStroke)) {
      paint.setColor(Color.parseColor(object.stroke));
      paint.setStrokeWidth(object.strokeWidth);
      paint.setStyle(Style.STROKE);
      // TODO: cap & join
    } else if (fillAndStroke || hasFill) {
      paint.setColor(Color.parseColor(object.fill));
      paint.setStyle(Style.FILL);
    }

    return hasFill && hasStroke;
  }

  private void getProportionsIfNotSet(float objectWidth) {
    if (pixelUnit == 0 && objectWidth > 0) {
      float svgProportion = objectWidth / svgWidth;
      float screenProportion = screenWidth * svgProportion;
      pixelUnit = screenProportion / objectWidth;
    }
  }

  private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
    if (parser.getEventType() != XmlPullParser.START_TAG) {
      throw new IllegalStateException();
    }
    int depth = 1;
    while (depth != 0) {
      switch (parser.next()) {
        case XmlPullParser.END_TAG:
          depth--;
          break;
        case XmlPullParser.START_TAG:
          depth++;
          break;
      }
    }
  }

  public static class SvgObject {
    public final static String TYPE_NONE = "none";
    public final static String TYPE_PATH = "path";
    public final static String TYPE_RECT = "rect";
    public final static String TYPE_CIRCLE = "circle";
    public final static String TYPE_ELLIPSE = "ellipse";
    public final static String TYPE_IMAGE = "image";
    // stroke line cap
    public final static String LINE_CAP_ROUND = "round";
    // stroke line join
    public final static String LINE_JOIN_ROUND = "round";

    private String type = TYPE_NONE;
    public float elevation;

    public SvgObject(String type) {
      this.type = type;
    }

    public String getType() {
      return type;
    }

    // STYLE
    public String fill;
    public String stroke;
    public String strokeLineCap, strokeLineJoin;
    public float strokeWidth;

    // PATH
    public String d;

    // RECT/IMAGE
    public float width, height;
    public float x, y;
    public Bitmap bitmap;

    // CIRCLE
    public float cx, cy;
    public float r;
  }

  private boolean isValidColor(String hex) {
    if (hex != null && !hex.isEmpty() && !hex.equals("#00000000") && !hex.equals("none")) {
      try {
        Color.parseColor(hex);
        return true;
      } catch (IllegalArgumentException e) {
        return false;
      }
    } else {
      return false;
    }
  }
}
