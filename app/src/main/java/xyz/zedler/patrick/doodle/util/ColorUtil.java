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

package xyz.zedler.patrick.doodle.util;

import androidx.annotation.IntRange;

public class ColorUtil {

  /**
   * Checks whether the specified value is between (including bounds) 0 and 255
   *
   * @param colorValue Color value
   * @return Specified input value if between 0 and 255, otherwise 0
   */
  static int assertColorValueInRange(@IntRange(from = 0, to = 255) int colorValue) {
    return ((0 <= colorValue) && (colorValue <= 255)) ? colorValue : 0;
  }

  /**
   * Formats individual RGB values to be output as a HEX string.
   *
   * Beware: If color value is lower than 0 or higher than 255, it's reset to 0.
   *
   * @param red   Red color value
   * @param green Green color value
   * @param blue  Blue color value
   * @return HEX String containing the three values
   */
  static String formatColorValues(
      @IntRange(from = 0, to = 255) int red,
      @IntRange(from = 0, to = 255) int green,
      @IntRange(from = 0, to = 255) int blue) {

    return String.format("%02X%02X%02X",
        assertColorValueInRange(red),
        assertColorValueInRange(green),
        assertColorValueInRange(blue)
    );
  }

  /**
   * Formats individual ARGB values to be output as an 8 character HEX string.
   *
   * Beware: If any value is lower than 0 or higher than 255, it's reset to 0.
   *
   * @param alpha Alpha value
   * @param red   Red color value
   * @param green Green color value
   * @param blue  Blue color value
   * @return HEX String containing the three values
   * @since v1.1.0
   */
  static String formatColorValues(
      @IntRange(from = 0, to = 255) int alpha,
      @IntRange(from = 0, to = 255) int red,
      @IntRange(from = 0, to = 255) int green,
      @IntRange(from = 0, to = 255) int blue) {

    return String.format("%02X%02X%02X%02X",
        assertColorValueInRange(alpha),
        assertColorValueInRange(red),
        assertColorValueInRange(green),
        assertColorValueInRange(blue)
    );
  }
}
