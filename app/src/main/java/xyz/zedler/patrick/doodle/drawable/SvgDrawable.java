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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import xyz.zedler.patrick.doodle.parser.PathParser;
import xyz.zedler.patrick.doodle.util.UnitUtil;

public class SvgDrawable {

  private final static String TAG = SvgDrawable.class.getSimpleName();

  private final static boolean ENABLE_IMAGES = true;

  private final List<SvgObject> objects;
  private final List<String> ids;
  private float offsetX;
  private float offsetY;
  private float scale;
  private float zoom;
  private final float pixelUnit;
  private float svgWidth, svgHeight;
  private final Paint paint;
  private int backgroundColor;
  private RectF rectF;
  private PointF pointF;
  private final Random random;

  public SvgDrawable(Context context, @RawRes int resId) {
    pixelUnit = UnitUtil.getDp(context, 1) * 0.33f;

    objects = new ArrayList<>();
    ids = new ArrayList<>();

    try {
      parse(context.getResources().openRawResource(resId));
    } catch (IOException e) {
      Log.e(TAG, "Could not open SVG resource: ", e);
    }

    scale = 1;

    paint = new Paint();
    rectF = new RectF();
    random = new Random();
  }

  @Nullable
  public SvgObject findObjectById(String id) {
    if (ids.contains(id)) {
      return objects.get(ids.indexOf(id));
    } else {
      return null;
    }
  }

  /**
   * The final offset is calculated with the elevation
   */
  public void setOffset(float offsetX, float offsetY) {
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    // TODO: offsetX * elevation
  }

  public void setScale(float scale) {
    this.scale = scale;
  }

  /**
   * Set how much should be zoomed out. The final value is calculated with the elevation of each
   * object. An object with elevation of 1 (nearest) is zoomed out much more than an object with the
   * elevation 0.1 (almost no parallax/zoom effect).
   *
   * @param zoom value from 0-1: 0 = original size; 1 = max zoomed out (depending on the elevation)
   */
  public void setZoom(float zoom) {
    this.zoom = zoom;
    // TODO: final scale factor = scale - (zoom * elevation)
    // TODO: has to be
  }

  /**
   * Apply random elevation between 0 (no parallax/zoom) to 1 (maximal effects) to all objects
   * @param min Set the minimal parallax/zoom intensity (good if nothing should be completely still)
   */
  public void applyRandomElevationToAll(float min) {
    for (SvgObject object : objects) {
      object.elevation =  min + random.nextFloat() * (1 - min);
    }
  }

  public void draw(Canvas canvas) {
    canvas.drawColor(backgroundColor);

    for (SvgObject object : objects) {
      startTransformation(canvas, object);

      switch (object.type) {
        case SvgObject.TYPE_PATH:
          drawPath(canvas, object);
          break;
        case SvgObject.TYPE_RECT:
          drawRect(canvas, object);
          break;
        case SvgObject.TYPE_CIRCLE:
        case SvgObject.TYPE_ELLIPSE:
          drawCircle(canvas, object);
          break;
        case SvgObject.TYPE_IMAGE:
          if (ENABLE_IMAGES) {
            drawImage(canvas, object);
          }
          break;
      }
      stopTransformation(canvas, object);
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
      Log.e(TAG, "parse: ", e);
    } finally {
      inputStream.close();
    }
  }

  private void readSvg(XmlPullParser parser) throws IOException, XmlPullParserException {
    parser.require(XmlPullParser.START_TAG, null, "svg");
    String viewBox = parser.getAttributeValue(null, "viewBox");
    if (viewBox != null) {
      String[] metrics = viewBox.split(" ");
      svgWidth = Float.parseFloat(metrics[2]) - Float.parseFloat(metrics[0]);
      svgHeight = Float.parseFloat(metrics[3]) - Float.parseFloat(metrics[1]);
    } else {
      Log.e(TAG, "readSvg: required viewBox attribute is missing");
      return;
    }

    while (parser.next() != XmlPullParser.END_DOCUMENT) {
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      readObject(parser, false);
    }
  }

  private void readObject(XmlPullParser parser, boolean isInGroup)
      throws IOException, XmlPullParserException {
    switch (parser.getName()) {
      case SvgObject.TYPE_GROUP:
        if (!isInGroup) {
          readGroup(parser);
        } else {
          Log.w(TAG, "readSvg: child group in group not supported, skipping");
          skip(parser);
        }
        break;
      case SvgObject.TYPE_PATH:
        readPath(parser, isInGroup);
        break;
      case SvgObject.TYPE_RECT:
        readRect(parser, isInGroup);
        break;
      case SvgObject.TYPE_CIRCLE:
        readCircle(parser, isInGroup);
        break;
      case SvgObject.TYPE_ELLIPSE:
        readEllipse(parser, isInGroup);
        break;
      case SvgObject.TYPE_IMAGE:
        if (ENABLE_IMAGES) {
          readImage(parser, isInGroup);
        }
        break;
      default:
        skip(parser);
        break;
    }
  }

  private void readGroup(XmlPullParser parser) throws IOException, XmlPullParserException {
    SvgObject object = new SvgObject(SvgObject.TYPE_GROUP);
    parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_GROUP);
    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      readObject(parser, true);
    }


    /*if (rectF == null || rectF.isEmpty()) {
      rectF = new RectF();
    }
    object.path.computeBounds(rectF, true);
    object.width = rectF.width();
    object.height = rectF.height();
    object.cx = rectF.centerX();
    object.cy = rectF.centerY();

    readStyle(parser, object);
    parseTransformation(parser.getAttributeValue(null, "transform"), object);*/


    //parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_GROUP);

    // apply display metrics
    /*Matrix scaleMatrix = new Matrix();
    scaleMatrix.setScale(pixelUnit, pixelUnit, object.cx, object.cy);
    object.path.transform(scaleMatrix);

    objectHashMap.put(id, object);
    ids.add(id);*/
  }

  private void readPath(XmlPullParser parser, boolean isInGroup)
      throws IOException, XmlPullParserException {
    SvgObject object = new SvgObject(SvgObject.TYPE_PATH);
    object.isInGroup = isInGroup;
    parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_PATH);
    String tag = parser.getName();
    object.id = parser.getAttributeValue(null, "id");
    if (tag.equals(SvgObject.TYPE_PATH)) {
      if (object.id == null) {
        return;
      }
      String d = parser.getAttributeValue(null, "d");
      if (d != null && !d.isEmpty()) {
        try {
          object.path = androidx.core.graphics.PathParser.createPathFromPathData(d);
          if (object.path == null) {
            return;
          }
        } catch (RuntimeException e) {
          Log.w(
              TAG,
              "readPath: error with legacy path parser, tried with alternative instead ", e
          );
          object.path = PathParser.getPath(d);
        }
      } else {
        return;
      }

      if (rectF == null || rectF.isEmpty()) {
        rectF = new RectF();
      }
      object.path.computeBounds(rectF, true);
      object.width = rectF.width();
      object.height = rectF.height();
      object.cx = rectF.centerX();
      object.cy = rectF.centerY();

      readStyle(parser, object);
      parseTransformation(parser.getAttributeValue(null, "transform"), object);

      parser.nextTag();
    }
    parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_PATH);

    // apply display metrics
    Matrix scaleMatrix = new Matrix();
    scaleMatrix.setScale(pixelUnit, pixelUnit, object.cx, object.cy);
    object.path.transform(scaleMatrix);

    objects.add(object);
    ids.add(object.id);
  }

  private void drawPath(Canvas canvas, SvgObject object) {
    // start with fill and repeat with stroke if both are set
    int runs = applyPaintStyle(object, false) ? 2 : 1;
    for (int i = 0; i < runs; i++) {
      if (i == 1) {
        applyPaintStyle(object, true);
      }
      canvas.save();

      float offsetX = this.offsetX * object.elevation;
      float offsetY = this.offsetY * object.elevation;

      // draw path to required position
      float requiredCenterX = (object.cx / svgWidth) * canvas.getWidth() - offsetX;
      float requiredCenterY = (object.cy / svgHeight) * canvas.getHeight() - offsetY;
      float dx = requiredCenterX - object.cx;
      float dy = requiredCenterY - object.cy;
      canvas.translate(dx, dy);
      canvas.drawPath(object.path, paint);
      canvas.restore();
    }
  }

  private void readRect(XmlPullParser parser, boolean isInGroup)
      throws IOException, XmlPullParserException {
    SvgObject object = new SvgObject(SvgObject.TYPE_RECT);
    object.isInGroup = isInGroup;
    parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_RECT);
    String tag = parser.getName();
    object.id = parser.getAttributeValue(null, "id");
    if (tag.equals(SvgObject.TYPE_RECT)) {
      if (object.id == null) {
        return;
      }
      object.width = parseFloat(parser.getAttributeValue(null, "width"));
      object.height = parseFloat(parser.getAttributeValue(null, "height"));
      object.x = parseFloat(parser.getAttributeValue(null, "x"));
      object.y = parseFloat(parser.getAttributeValue(null, "y"));
      object.cx = object.x + object.width / 2;
      object.cy = object.y + object.height / 2;
      object.rx = parseFloat(parser.getAttributeValue(null, "rx"));
      object.ry = parseFloat(parser.getAttributeValue(null, "ry"));

      readStyle(parser, object);
      parseTransformation(parser.getAttributeValue(null, "transform"), object);

      // has same size as SVG? Use it as background color.
      if (object.width == svgWidth && object.height == svgHeight) {
        backgroundColor = object.fill;
        return;
      }

      parser.nextTag();
    }
    parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_RECT);

    // apply display metrics
    object.width *= pixelUnit;
    object.height *= pixelUnit;
    object.rx *= pixelUnit;
    object.ry *= pixelUnit;
    object.cx /= svgWidth;
    object.cy /= svgHeight;

    objects.add(object);
    ids.add(object.id);
  }

  private void drawRect(Canvas canvas, SvgObject object) {
    // start with fill and repeat with stroke if both are set
    int runs = applyPaintStyle(object, false) ? 2 : 1;
    for (int i = 0; i < runs; i++) {
      if (i == 1) {
        applyPaintStyle(object, true);
      }
      float offsetX = this.offsetX * object.elevation;
      float offsetY = this.offsetY * object.elevation;
      float cx = object.cx * canvas.getWidth() - offsetX;
      float cy = object.cy * canvas.getHeight() - offsetY;
      // TODO: shifts in direction of rotation when transformed...

      if (object.rx == 0 && object.ry == 0) {
        canvas.drawRect(
            cx - object.width / 2,
            cy - object.height / 2,
            cx + object.width / 2,
            cy + object.height / 2,
            paint
        );
      } else {
        float rx = object.rx != 0 ? object.rx : object.ry;
        float ry = object.ry != 0 ? object.ry : object.rx;
        canvas.drawRoundRect(
            cx - object.width / 2,
            cy - object.height / 2,
            cx + object.width / 2,
            cy + object.height / 2,
            rx,
            ry,
            paint
        );
      }
    }
  }

  private void readCircle(XmlPullParser parser, boolean isInGroup)
      throws IOException, XmlPullParserException {
    SvgObject object = new SvgObject(SvgObject.TYPE_CIRCLE);
    object.isInGroup = isInGroup;
    parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_CIRCLE);
    String tag = parser.getName();
    object.id = parser.getAttributeValue(null, "id");
    if (tag.equals(SvgObject.TYPE_CIRCLE)) {
      if (object.id == null) {
        return;
      }
      object.cx = parseFloat(parser.getAttributeValue(null, "cx"));
      object.cy = parseFloat(parser.getAttributeValue(null, "cy"));
      object.r = parseFloat(parser.getAttributeValue(null, "r"));

      readStyle(parser, object);
      parseTransformation(parser.getAttributeValue(null, "transform"), object);

      parser.nextTag();
    }
    parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_CIRCLE);

    // apply display metrics
    object.cx /= svgWidth;
    object.cy /= svgHeight;
    object.r *= pixelUnit;

    objects.add(object);
    ids.add(object.id);
  }

  private void drawCircle(Canvas canvas, SvgObject object) {
    // start with fill and repeat with stroke if both are set
    int runs = applyPaintStyle(object, false) ? 2 : 1;
    for (int i = 0; i < runs; i++) {
      if (i == 1) {
        applyPaintStyle(object, true);
      }
      if (object.type.equals(SvgObject.TYPE_CIRCLE) || object.rx == object.ry) {
        float radius = object.r > 0 ? object.r : object.rx;
        canvas.drawCircle(
            object.cx * canvas.getWidth(), object.cy * canvas.getHeight(), radius, paint
        );
      } else if (object.type.equals(SvgObject.TYPE_ELLIPSE)) {
        canvas.drawOval(
            object.cx * canvas.getWidth() - object.rx,
            object.cy * canvas.getHeight() - object.ry,
            object.cx * canvas.getWidth() + object.rx,
            object.cy * canvas.getHeight() + object.ry,
            paint
        );
      }
    }
  }

  private void readEllipse(XmlPullParser parser, boolean isInGroup)
      throws IOException, XmlPullParserException {
    SvgObject object = new SvgObject(SvgObject.TYPE_ELLIPSE);
    object.isInGroup = isInGroup;
    parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_ELLIPSE);
    String tag = parser.getName();
    object.id = parser.getAttributeValue(null, "id");
    if (tag.equals(SvgObject.TYPE_ELLIPSE)) {
      if (object.id == null) {
        return;
      }
      object.cx = parseFloat(parser.getAttributeValue(null, "cx")) / svgWidth;
      object.cy = parseFloat(parser.getAttributeValue(null, "cy")) / svgHeight;
      object.rx = parseFloat(parser.getAttributeValue(null, "rx")) * pixelUnit;
      object.ry = parseFloat(parser.getAttributeValue(null, "ry")) * pixelUnit;

      readStyle(parser, object);
      parseTransformation(parser.getAttributeValue(null, "transform"), object);

      parser.nextTag();
    }
    parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_ELLIPSE);

    objects.add(object);
    ids.add(object.id);
  }

  private void readImage(XmlPullParser parser, boolean isInGroup)
      throws IOException, XmlPullParserException {
    SvgObject object = new SvgObject(SvgObject.TYPE_IMAGE);
    object.isInGroup = isInGroup;
    parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_IMAGE);
    String tag = parser.getName();
    object.id = parser.getAttributeValue(null, "id");
    if (tag.equals(SvgObject.TYPE_IMAGE)) {
      if (object.id == null) {
        return;
      }
      object.width = parseFloat(parser.getAttributeValue(null, "width"));
      object.height = parseFloat(parser.getAttributeValue(null, "height"));
      object.x = parseFloat(parser.getAttributeValue(null, "x"));
      object.y = parseFloat(parser.getAttributeValue(null, "y"));
      object.cx = object.x + object.width / 2;
      object.cy = object.y + object.height / 2;

      readStyle(parser, object);
      parseTransformation(parser.getAttributeValue(null, "transform"), object);

      String image = parser.getAttributeValue(parser.getNamespace("xlink"), "href");
      if (image != null) {
        image = image.substring(image.indexOf(",") + 1);
        byte[] decoded = Base64.decode(image, Base64.DEFAULT);
        object.bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
      }

      parser.nextTag();
    }
    parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_IMAGE);

    // apply display metrics
    object.width *= pixelUnit;
    object.height *= pixelUnit;
    object.cx /= svgWidth;
    object.cy /= svgHeight;

    objects.add(object);
    ids.add(object.id);
  }

  private void drawImage(Canvas canvas, SvgObject object) {
    applyPaintStyle(object, false);

    /*float offsetX = this.offsetX * object.elevation;
    float offsetY = this.offsetY * object.elevation;
    float cx = object.cx * canvas.getWidth() - offsetX;
    float cy = object.cy * canvas.getHeight() - offsetY;

    float centerX = canvas.getWidth() / 2f;
    if (cx < centerX) {
      float dist = centerX - cx;
      cx += dist * object.elevation * zoom;
    } else {
      float dist = cx - centerX;
      cx -= dist * object.elevation * zoom;
    }*/
    pointF = getFinalCenter(canvas, object);

    /*if (rectF == null || rectF.isEmpty()) {
      rectF = new RectF();
    }*/
    float scale = getFinalScale(object);
    rectF.set(
        pointF.x - (object.width * scale) / 2,
        pointF.y - (object.height * scale) / 2,
        pointF.x + (object.width * scale) / 2,
        pointF.y + (object.height * scale) / 2
    );

    canvas.drawBitmap(object.bitmap, null, rectF, paint);
  }

  private void parseTransformation(String transformation, SvgObject object) {
    if (transformation == null || transformation.isEmpty()) {
      return;
    }
    String[] transform = transformation.split("[ ](?=[^)]*?(?:\\(|$))");
    for (String action : transform) {
      String value = action.substring(action.indexOf("(") + 1, action.indexOf(")"));
      if (action.contains("rotate")) {
        String[] rotation = value.split("[\\n\\r\\s]+");

        object.rotation = Float.parseFloat(rotation[0]);
        if (rotation.length == 3) {
          object.rotationX = Float.parseFloat(rotation[1]);
          object.rotationY = Float.parseFloat(rotation[2]);
        }
        pointF = getRotatedPoint(
            object.cx, object.cy, object.rotationX, object.rotationY, object.rotation
        );
        object.cx = pointF.x;
        object.cy = pointF.y;
      } else if (action.contains("scale")) {
        String[] scale = value.split("[\\n\\r\\s]+");
        if (scale.length > 1) {
          Log.e(TAG, "parseTransformation: scale: multiple values are not supported");
          return;
        }
        object.scale = Float.parseFloat(scale[0]);
        /*if (object.scale != 1) { TODO: at that time the cx and cy values are floats from 0-1...
          object.cx *= object.scale;
          object.cy *= object.scale;
        }*/
      }
    }
  }

  private void startTransformation(Canvas canvas, SvgObject object) {
    if (object.rotation != 0) {
      canvas.save();
      canvas.rotate(
          object.rotation, object.cx * canvas.getWidth(), object.cy * canvas.getHeight()
      );
    }
  }

  private void stopTransformation(Canvas canvas, SvgObject object) {
    if (object.rotation != 0) {
      canvas.restore();
    }
  }

  private void readStyle(XmlPullParser parser, SvgObject object) {
    object.fill = parseColor(parser.getAttributeValue(null, "fill"));
    object.stroke = parseColor(parser.getAttributeValue(null, "stroke"));
    object.fillOpacity = parseOpacity(
        parser.getAttributeValue(null, "fill-opacity")
    );
    object.strokeOpacity = parseOpacity(
        parser.getAttributeValue(null, "stroke-opacity")
    );
    object.strokeWidth = parseFloat(parser.getAttributeValue(null, "stroke-width"));
    object.strokeLineCap = parser.getAttributeValue(null, "stroke-linecap");
    object.strokeLineJoin = parser.getAttributeValue(null, "stroke-linejoin");
  }

  /**
   * @return true if a second draw for a separate stroke style is needed
   */
  private boolean applyPaintStyle(SvgObject object, boolean applyStrokeIfBothSet) {
    paint.reset();
    paint.setAntiAlias(true);

    boolean hasFill = object.fill != 0;
    boolean hasStroke = object.stroke != 0 && object.strokeWidth > 0;
    boolean hasFillAndStroke = hasFill && hasStroke;

    if ((hasFillAndStroke && applyStrokeIfBothSet) || (!hasFill && hasStroke)) {
      paint.setStyle(Style.STROKE);
      paint.setARGB(
          (int) (object.strokeOpacity * 255),
          Color.red(object.stroke),
          Color.green(object.stroke),
          Color.blue(object.stroke)
      );
      paint.setStrokeWidth(object.strokeWidth * pixelUnit);
      if (object.strokeLineCap != null) {
        switch (object.strokeLineCap) {
          case SvgObject.LINE_CAP_BUTT:
            paint.setStrokeCap(Cap.BUTT);
            break;
          case SvgObject.LINE_CAP_ROUND:
            paint.setStrokeCap(Cap.ROUND);
            break;
          case SvgObject.LINE_CAP_SQUARE:
            paint.setStrokeCap(Cap.SQUARE);
            break;
        }
      }
      if (object.strokeLineJoin != null) {
        switch (object.strokeLineJoin) {
          case SvgObject.LINE_JOIN_MITER:
            paint.setStrokeJoin(Join.MITER);
            break;
          case SvgObject.LINE_JOIN_ROUND:
            paint.setStrokeJoin(Join.ROUND);
            break;
          case SvgObject.LINE_JOIN_BEVEL:
            paint.setStrokeJoin(Join.BEVEL);
            break;
        }
      }
    } else if (hasFillAndStroke || hasFill) {
      paint.setStyle(Style.FILL);
      paint.setARGB(
          (int) (object.fillOpacity * 255),
          Color.red(object.fill),
          Color.green(object.fill),
          Color.blue(object.fill)
      );
    }

    return hasFillAndStroke;
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

  private static class SvgObject {
    public final static String TYPE_GROUP = "g";
    public final static String TYPE_PATH = "path";
    public final static String TYPE_RECT = "rect";
    public final static String TYPE_CIRCLE = "circle";
    public final static String TYPE_ELLIPSE = "ellipse";
    public final static String TYPE_IMAGE = "image";
    // stroke line cap
    public final static String LINE_CAP_BUTT = "butt";
    public final static String LINE_CAP_ROUND = "round";
    public final static String LINE_CAP_SQUARE = "square";
    // stroke line join
    public final static String LINE_JOIN_ROUND = "round";
    public final static String LINE_JOIN_BEVEL = "bevel";
    public final static String LINE_JOIN_MITER = "miter";

    public String id;
    public final String type;
    public boolean isInGroup;
    public float elevation;

    public SvgObject(String type) {
      this.type = type;
    }

    // STYLE
    public int fill;
    public int stroke;
    public float fillOpacity, strokeOpacity;
    public String strokeLineCap, strokeLineJoin;
    public float strokeWidth;

    // TRANSFORMATION
    public float rotation, rotationX, rotationY;
    public float scale;

    // PATH
    public Path path;

    // RECT/IMAGE
    public float width, height;
    public float x, y;
    public float rx, ry;
    public Bitmap bitmap;

    // CIRCLE
    public float cx, cy;
    public float r;

    @NonNull
    @Override
    public String toString() {
      return "SvgObject{ id='" + id + "', type='" + type + "', isInGroup=" + isInGroup + '}';
    }
  }

  private float getFinalScale(SvgObject object) {
    return scale - (zoom * object.elevation);
  }

  private float parseFloat(String value) {
    if (value != null && !value.isEmpty()) {
      try {
        return Float.parseFloat(value);
      } catch (NumberFormatException e) {
        return 0;
      }
    } else {
      return 0;
    }
  }

  private float parseOpacity(String value) {
    if (value != null && !value.isEmpty()) {
      try {
        return Float.parseFloat(value);
      } catch (NumberFormatException e) {
        return 0;
      }
    } else {
      return 1;
    }
  }

  private int parseColor(String value) {
    if (value != null && !value.isEmpty() && !value.equals("#00000000") && !value.equals("none")) {
      try {
        return Color.parseColor(value);
      } catch (IllegalArgumentException e) {
        if (value.matches("#[a-fA-F0-9]{3}")) {
          String first = value.substring(1, 2);
          String second = value.substring(2, 3);
          String third = value.substring(3, 4);
          String hex = "#" + first + first + second + second + third + third;
          try {
            return Color.parseColor(hex);
          } catch (IllegalArgumentException exception) {
            return 0;
          }
        } else {
          return 0;
        }
      }
    } else {
      return 0;
    }
  }

  private PointF getRotatedPoint(float x, float y, float cx, float cy, float degrees) {
    double radians = Math.toRadians(degrees);

    float x1 = x - cx;
    float y1 = y - cy;

    float x2 = (float) (x1 * Math.cos(radians) - y1 * Math.sin(radians));
    float y2 = (float) (x1 * Math.sin(radians) + y1 * Math.cos(radians));

    return new PointF(x2 + cx, y2 + cy);
  }

  private PointF getFinalCenter(Canvas canvas, SvgObject object) {
    float cx = object.cx * canvas.getWidth() - (offsetX * object.elevation);
    float cy = object.cy * canvas.getHeight() - (offsetY * object.elevation);

    float centerX = canvas.getWidth() / 2f;
    if (cx < centerX) {
      float dist = centerX - cx;
      cx += dist * object.elevation * zoom;
    } else {
      float dist = cx - centerX;
      cx -= dist * object.elevation * zoom;
    }

    float centerY = canvas.getHeight() / 2f;
    if (cy < centerY) {
      float dist = centerY - cy;
      cy += dist * object.elevation * zoom;
    } else {
      float dist = cy - centerY;
      cy -= dist * object.elevation * zoom;
    }
    return new PointF(cx, cy);
  }
}
