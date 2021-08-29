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

package xyz.zedler.patrick.doodle;

public final class Constants {

  public final static String VARIANT_PREFIX = "variant_";

  public final static class PREF {

    public final static String NIGHT_MODE = "night_mode";
    public final static String FOLLOW_SYSTEM = "follow_system";
    public final static String USE_WHITE_TEXT = "use_white_text";
    public final static String ZOOM = "zoom";
    public final static String ZOOM_LAUNCHER = "zoom_launcher";
    public final static String ZOOM_UNLOCK = "zoom_unlock";
    public final static String WALLPAPER = "wallpaper";
    public final static String VARIANT_PIXEL = "variant_pixel";
    public final static String VARIANT_JOHANNA = "variant_johanna";
    public final static String VARIANT_REIKO = "variant_reiko";
    public final static String VARIANT_ANTHONY = "variant_anthony";
    public final static String PARALLAX = "parallax";
    public final static String TILT = "tilt";
    public final static String REFRESH_RATE = "refresh_rate";
    public final static String SCALE = "size";
    public final static String GPU = "hardware_acceleration";

    public final static String SETTINGS_APPLIED = "settings_applied";
    public final static String THEME_APPLIED = "theme_applied";

    public final static String LAST_VERSION = "last_version";
    public final static String FEEDBACK_POP_UP_COUNT = "feedback_pop_up_count";
  }

  public final static class DEF {

    public final static boolean NIGHT_MODE = true;
    public final static boolean FOLLOW_SYSTEM = true;
    public final static boolean USE_WHITE_TEXT = false;
    public final static boolean TILT = false;
    public final static int REFRESH_RATE = 30000;
    public final static int ZOOM = 3;
    public final static boolean ZOOM_LAUNCHER = true;
    public final static boolean ZOOM_UNLOCK = true;
    public final static String WALLPAPER = Constants.WALLPAPER.PIXEL;
    public final static int PARALLAX = 1;
    public final static float SCALE = 1;
    public final static boolean GPU = true;
    public final static boolean LAUNCHER = false;
  }

  public final static class WALLPAPER {

    public final static String PIXEL = "pixel";
    public final static String JOHANNA = "johanna";
    public final static String REIKO = "reiko";
    public final static String ANTHONY = "anthony";
  }

  public final static class VARIANT {

    public final static String PIXEL1 = "pixel1";
    public final static String PIXEL2 = "pixel2";
    public final static String PIXEL3 = "pixel3";
    public final static String PIXEL4 = "pixel4";
    public final static String PIXEL5 = "pixel5";

    public final static String JOHANNA1 = "johanna1";

    public final static String REIKO1 = "reiko1";
    public final static String REIKO2 = "reiko2";

    public final static String ANTHONY1 = "anthony1";
  }

  public final static class EXTRA {

    public final static String TITLE = "title";
    public final static String LINK = "link";
    public final static String FILE = "file";
  }

  public final static class USER_PRESENCE {

    public final static String LOCKED = "locked";
    public final static String OFF = "off";
    public final static String UNLOCKED = "unlocked";
  }

  public final static class REQUEST_SOURCE {

    public final static String ZOOM_LAUNCHER = "zoom_launcher";
    public final static String ZOOM_UNLOCK = "zoom_unlock";
    public final static String TILT = "tilt";
  }
}
