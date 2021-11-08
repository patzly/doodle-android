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
 * Copyright (c) 2019-2021 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.parser;

import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

public class PathParser {

  private static final String TAG = PathParser.class.getSimpleName();

  /*
   * This is where the hard-to-parse paths are handled.
   * Uppercase rules are absolute positions, lowercase are relative.
   * Types of path rules:
   * <p/>
   * <ol>
   * <li>M/m - (x y)+ - Move to (without drawing)
   * <li>Z/z - (no params) - Close path (back to starting point)
   * <li>L/l - (x y)+ - Line to
   * <li>H/h - x+ - Horizontal ine to
   * <li>V/v - y+ - Vertical line to
   * <li>C/c - (x1 y1 x2 y2 x y)+ - Cubic bezier to
   * <li>S/s - (x2 y2 x y)+ - Smooth cubic bezier to (shorthand that assumes the x2, y2 from previous C/S is the x1, y1 of this bezier)
   * <li>Q/q - (x1 y1 x y)+ - Quadratic bezier to
   * <li>T/t - (x y)+ - Smooth quadratic bezier to (assumes previous control point is "reflection" of last one w.r.t. to current point)
   * </ol>
   * <p/>
   * Numbers are separate by whitespace, comma or nothing at all (!) if they are self-delimiting, (ie. begin with a - sign)
   */
  public static Path getPath(String s) {
    int n = s.length();
    ParserHelper ph = new ParserHelper(s);
    ph.skipWhitespace();
    Path p = new Path();
    float lastX = 0;
    float lastY = 0;
    float lastX1 = 0;
    float lastY1 = 0;
    float contourInitialX = 0;
    float contourInitialY = 0;
    RectF r = new RectF();
    char prevCmd = 'm';
    char cmd = 'x';
    while (ph.pos < n) {
      char next = s.charAt(ph.pos);
      if (!Character.isDigit(next) && !(next == '.') && !(next == '-')) {
        cmd = next;
        ph.advance();
      } else if (cmd == 'M') { // implied command
        cmd = 'L';
      } else if (cmd == 'm') { // implied command
        cmd = 'l';
      }

      p.computeBounds(r, true);
      boolean wasCurve = false;
      switch (cmd) {
        case 'M':
        case 'm': {
          float x = ph.nextFloat();
          float y = ph.nextFloat();
          if (cmd == 'm') {
            p.rMoveTo(x, y);
            lastX += x;
            lastY += y;
          } else {
            p.moveTo(x, y);
            lastX = x;
            lastY = y;
          }
          contourInitialX = lastX;
          contourInitialY = lastY;
          break;
        }
        case 'Z':
        case 'z': {
          /// p.lineTo(contourInitialX, contourInitialY);
          p.close();
          lastX = contourInitialX;
          lastY = contourInitialY;
          break;
        }
        case 'L':
        case 'l': {
          float x = ph.nextFloat();
          float y = ph.nextFloat();
          if (cmd == 'l') {
            if ((prevCmd == 'M' || prevCmd == 'm') && x == 0 && y == 0) {
              p.addCircle(x, y, 1f, Path.Direction.CW);
            } else {
              p.rLineTo(x, y);
              lastX += x;
              lastY += y;
            }
          } else {
            if ((prevCmd == 'M' || prevCmd == 'm') && x == lastX && y == lastY) {
              p.addCircle(x, y, 1f, Path.Direction.CW);
            } else {
              p.lineTo(x, y);
              lastX = x;
              lastY = y;
            }
          }
          break;
        }
        case 'H':
        case 'h': {
          float x = ph.nextFloat();
          if (cmd == 'h') {
            p.rLineTo(x, 0);
            lastX += x;
          } else {
            p.lineTo(x, lastY);
            lastX = x;
          }
          break;
        }
        case 'V':
        case 'v': {
          float y = ph.nextFloat();
          if (cmd == 'v') {
            p.rLineTo(0, y);
            lastY += y;
          } else {
            p.lineTo(lastX, y);
            lastY = y;
          }
          break;
        }
        case 'C':
        case 'c': {
          wasCurve = true;
          float x1 = ph.nextFloat();
          float y1 = ph.nextFloat();
          float x2 = ph.nextFloat();
          float y2 = ph.nextFloat();
          float x = ph.nextFloat();
          float y = ph.nextFloat();
          if (cmd == 'c') {
            x1 += lastX;
            x2 += lastX;
            x += lastX;
            y1 += lastY;
            y2 += lastY;
            y += lastY;
          }
          p.cubicTo(x1, y1, x2, y2, x, y);
          lastX1 = x2;
          lastY1 = y2;
          lastX = x;
          lastY = y;
          break;
        }
        case 'S':
        case 's': {
          wasCurve = true;
          float x2 = ph.nextFloat();
          float y2 = ph.nextFloat();
          float x = ph.nextFloat();
          float y = ph.nextFloat();
          if (cmd == 's') {
            x2 += lastX;
            x += lastX;
            y2 += lastY;
            y += lastY;
          }
          float x1 = 2 * lastX - lastX1;
          float y1 = 2 * lastY - lastY1;
          p.cubicTo(x1, y1, x2, y2, x, y);
          lastX1 = x2;
          lastY1 = y2;
          lastX = x;
          lastY = y;
          break;
        }
        case 'A':
        case 'a': {
          float rx = ph.nextFloat();
          float ry = ph.nextFloat();
          float theta = ph.nextFloat();
          int largeArc = (int) ph.nextFloat();
          int sweepArc = (int) ph.nextFloat();
          float x = ph.nextFloat();
          float y = ph.nextFloat();
          if (cmd == 'a') {
            x += lastX;
            y += lastY;
          }
          drawArc(p, lastX, lastY, x, y, rx, ry, theta, largeArc == 1, sweepArc == 1);
          lastX = x;
          lastY = y;
          break;
        }
        case 'T':
        case 't': {
          wasCurve = true;
          float x = ph.nextFloat();
          float y = ph.nextFloat();
          if (cmd == 't') {
            x += lastX;
            y += lastY;
          }
          float x1 = 2 * lastX - lastX1;
          float y1 = 2 * lastY - lastY1;
          p.cubicTo(lastX, lastY, x1, y1, x, y);
          lastX = x;
          lastY = y;
          lastX1 = x1;
          lastY1 = y1;
          break;
        }
        case 'Q':
        case 'q': {
          wasCurve = true;
          float x1 = ph.nextFloat();
          float y1 = ph.nextFloat();
          float x = ph.nextFloat();
          float y = ph.nextFloat();
          if (cmd == 'q') {
            x += lastX;
            y += lastY;
            x1 += lastX;
            y1 += lastY;
          }
          p.cubicTo(lastX, lastY, x1, y1, x, y);
          lastX1 = x1;
          lastY1 = y1;
          lastX = x;
          lastY = y;
          break;
        }
        default:
          Log.w(TAG, "Invalid path command: " + cmd);
          ph.advance();
      }
      prevCmd = cmd;
      if (!wasCurve) {
        lastX1 = lastX;
        lastY1 = lastY;
      }
      ph.skipWhitespace();
    }
    return p;
  }

  /*
   * Elliptical arc implementation based on the SVG specification notes
   * Adapted from the Batik library (Apache-2 license) by SAU
   */
  private static void drawArc(Path path, double x0, double y0, double x, double y, double rx,
      double ry, double angle, boolean largeArcFlag, boolean sweepFlag) {
    double dx2 = (x0 - x) / 2.0;
    double dy2 = (y0 - y) / 2.0;
    angle = Math.toRadians(angle % 360.0);
    double cosAngle = Math.cos(angle);
    double sinAngle = Math.sin(angle);

    double x1 = (cosAngle * dx2 + sinAngle * dy2);
    double y1 = (-sinAngle * dx2 + cosAngle * dy2);
    rx = Math.abs(rx);
    ry = Math.abs(ry);

    double Prx = rx * rx;
    double Pry = ry * ry;
    double Px1 = x1 * x1;
    double Py1 = y1 * y1;

    // check that radii are large enough
    double radiiCheck = Px1 / Prx + Py1 / Pry;
    if (radiiCheck > 1) {
      rx = Math.sqrt(radiiCheck) * rx;
      ry = Math.sqrt(radiiCheck) * ry;
      Prx = rx * rx;
      Pry = ry * ry;
    }

    // Step 2 : Compute (cx1, cy1)
    double sign = (largeArcFlag == sweepFlag) ? -1 : 1;
    double sq = ((Prx * Pry) - (Prx * Py1) - (Pry * Px1))
        / ((Prx * Py1) + (Pry * Px1));
    sq = (sq < 0) ? 0 : sq;
    double coef = (sign * Math.sqrt(sq));
    double cx1 = coef * ((rx * y1) / ry);
    double cy1 = coef * -((ry * x1) / rx);

    double sx2 = (x0 + x) / 2.0;
    double sy2 = (y0 + y) / 2.0;
    double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
    double cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);

    // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
    double ux = (x1 - cx1) / rx;
    double uy = (y1 - cy1) / ry;
    double vx = (-x1 - cx1) / rx;
    double vy = (-y1 - cy1) / ry;
    double p, n;

    // Compute the angle start
    n = Math.sqrt((ux * ux) + (uy * uy));
    p = ux; // (1 * ux) + (0 * uy)
    sign = (uy < 0) ? -1.0 : 1.0;
    double angleStart = Math.toDegrees(sign * Math.acos(p / n));

    // Compute the angle extent
    n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
    p = ux * vx + uy * vy;
    sign = (ux * vy - uy * vx < 0) ? -1.0 : 1.0;
    double angleExtent = Math.toDegrees(sign * Math.acos(p / n));
    if (!sweepFlag && angleExtent > 0) {
      angleExtent -= 360f;
    } else if (sweepFlag && angleExtent < 0) {
      angleExtent += 360f;
    }
    angleExtent %= 360f;
    angleStart %= 360f;

    RectF oval = new RectF((float) (cx - rx), (float) (cy - ry), (float) (cx + rx),
        (float) (cy + ry));
    path.addArc(oval, (float) angleStart, (float) angleExtent);
  }

  /**
   * Parses numbers from SVG text. Based on the Batik Number Parser (Apache 2 License).
   *
   * @author Apache Software Foundation, Larva Labs LLC
   */
  static class ParserHelper {

    private char current;
    private final CharSequence s;
    public int pos;
    private final int n;

    public ParserHelper(CharSequence s) {
      this.s = s;
      this.pos = 0;
      n = s.length();
      current = s.charAt(pos);
    }

    private char read() {
      if (pos < n) {
        pos++;
      }
      if (pos == n) {
        return '\0';
      } else {
        return s.charAt(pos);
      }
    }

    public void skipWhitespace() {
      while (pos < n) {
        if (Character.isWhitespace(s.charAt(pos))) {
          advance();
        } else {
          break;
        }
      }
    }

    void skipNumberSeparator() {
      while (pos < n) {
        char c = s.charAt(pos);
        switch (c) {
          case ' ':
          case ',':
          case '\n':
          case '\t':
            advance();
            break;
          default:
            return;
        }
      }
    }

    public void advance() {
      current = read();
    }

    //Parses the content of the buffer and converts it to a float.
    float parseFloat() {
      int mant = 0;
      int mantDig = 0;
      boolean mantPos = true;
      boolean mantRead = false;

      int exp = 0;
      int expDig = 0;
      int expAdj = 0;
      boolean expPos = true;

      switch (current) {
        case '-':
          mantPos = false;
          // fallthrough
        case '+':
          current = read();
      }
      m1:
      switch (current) {
        default:
          return Float.NaN;

        case '.':
          break;

        case '0':
          mantRead = true;
          l:
          for (; ; ) {
            current = read();
            switch (current) {
              case '1':
              case '2':
              case '3':
              case '4':
              case '5':
              case '6':
              case '7':
              case '8':
              case '9':
                break l;
              case '.':
              case 'e':
              case 'E':
                break m1;
              default:
                return 0.0f;
              case '0':
            }
          }

        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          mantRead = true;
          l:
          for (; ; ) {
            if (mantDig < 9) {
              mantDig++;
              mant = mant * 10 + (current - '0');
            } else {
              expAdj++;
            }
            current = read();
            switch (current) {
              default:
                break l;
              case '0':
              case '1':
              case '2':
              case '3':
              case '4':
              case '5':
              case '6':
              case '7':
              case '8':
              case '9':
            }
          }
      }

      if (current == '.') {
        current = read();
        m2:
        switch (current) {
          default:
          case 'e':
          case 'E':
            if (!mantRead) {
              reportUnexpectedCharacterError(current);
              return 0.0f;
            }
            break;

          case '0':
            if (mantDig == 0) {
              l:
              for (; ; ) {
                current = read();
                expAdj--;
                switch (current) {
                  case '1':
                  case '2':
                  case '3':
                  case '4':
                  case '5':
                  case '6':
                  case '7':
                  case '8':
                  case '9':
                    break l;
                  default:
                    if (!mantRead) {
                      return 0.0f;
                    }
                    break m2;
                  case '0':
                }
              }
            }
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            l:
            for (; ; ) {
              if (mantDig < 9) {
                mantDig++;
                mant = mant * 10 + (current - '0');
                expAdj--;
              }
              current = read();
              switch (current) {
                default:
                  break l;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
              }
            }
        }
      }

      switch (current) {
        case 'e':
        case 'E':
          current = read();
          switch (current) {
            default:
              reportUnexpectedCharacterError(current);
              return 0f;
            case '-':
              expPos = false;
            case '+':
              current = read();
              switch (current) {
                default:
                  reportUnexpectedCharacterError(current);
                  return 0f;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
              }
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
          }

          en:
          switch (current) {
            case '0':
              l:
              for (; ; ) {
                current = read();
                switch (current) {
                  case '1':
                  case '2':
                  case '3':
                  case '4':
                  case '5':
                  case '6':
                  case '7':
                  case '8':
                  case '9':
                    break l;
                  default:
                    break en;
                  case '0':
                }
              }

            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
              l:
              for (; ; ) {
                if (expDig < 3) {
                  expDig++;
                  exp = exp * 10 + (current - '0');
                }
                current = read();
                switch (current) {
                  default:
                    break l;
                  case '0':
                  case '1':
                  case '2':
                  case '3':
                  case '4':
                  case '5':
                  case '6':
                  case '7':
                  case '8':
                  case '9':
                }
              }
          }
        default:
      }

      if (!expPos) {
        exp = -exp;
      }
      exp += expAdj;
      if (!mantPos) {
        mant = -mant;
      }

      return buildFloat(mant, exp);
    }

    private void reportUnexpectedCharacterError(char c) {
      throw new RuntimeException("Unexpected char '" + c + "'.");
    }

    //Computes a float from mantissa and exponent.
    private static float buildFloat(int mant, int exp) {
      if (exp < -125 || mant == 0) {
        return 0.0f;
      }

      if (exp >= 128) {
        return (mant > 0)
            ? Float.POSITIVE_INFINITY
            : Float.NEGATIVE_INFINITY;
      }

      if (exp == 0) {
        return mant;
      }

      if (mant >= (1 << 26)) {
        mant++;  // round up trailing bits if they will be dropped.
      }

      return (float) ((exp > 0) ? mant * pow10[exp] : mant / pow10[-exp]);
    }

    /**
     * Array of powers of ten. Using double instead of float gives a tiny bit more precision.
     */
    private static final double[] pow10 = new double[128];

    static {
      for (int i = 0; i < pow10.length; i++) {
        pow10[i] = Math.pow(10, i);
      }
    }

    public float nextFloat() {
      skipWhitespace();
      float f = parseFloat();
      skipNumberSeparator();
      return f;
    }
  }
}
