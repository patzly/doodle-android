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

package xyz.zedler.patrick.doodle;

public final class Constants {

  public final static String VARIANT_PREFIX = "variant_";
  public final static String PREFS_NORMAL = "normal";

  public final static class PREF {

    // Appearance

    public final static String WALLPAPER = "wallpaper";
    public final static String VARIANT_PIXEL = "variant_pixel";
    public final static String VARIANT_JOHANNA = "variant_johanna";
    public final static String VARIANT_REIKO = "variant_reiko";
    public final static String VARIANT_ANTHONY = "variant_anthony";

    public final static String NIGHT_MODE = "night_mode";
    public final static String FOLLOW_SYSTEM = "follow_system";
    public final static String USE_WHITE_TEXT = "use_white_text";

    // Parallax

    public final static String PARALLAX = "parallax";
    public final static String TILT = "tilt";
    public final static String REFRESH_RATE = "refresh_rate";
    public final static String DAMPING = "damping";
    public final static String THRESHOLD = "threshold";

    // Size

    public final static String SCALE = "size";
    public final static String ZOOM = "zoom";
    public final static String ZOOM_LAUNCHER = "zoom_launcher";
    public final static String ZOOM_UNLOCK = "zoom_unlock";
    public final static String ZOOM_SYSTEM = "zoom_system";
    public final static String ZOOM_DURATION = "zoom_duration";
    public final static String ZOOM_ROTATION = "zoom_rotation";

    // Other

    public final static String LANGUAGE = "language";
    public final static String GPU = "hardware_acceleration";

    public final static String PREVIEW_RUNNING = "preview_running";

    public final static String LAST_VERSION = "last_version";
    public final static String FEEDBACK_POP_UP_COUNT = "feedback_pop_up_count";
  }

  public final static class DEF {

    public final static boolean NIGHT_MODE = true;
    public final static boolean FOLLOW_SYSTEM = true;
    public final static boolean USE_WHITE_TEXT = false;
    public final static boolean TILT = false;
    public final static int REFRESH_RATE = 30000;
    public final static int ZOOM = 2;
    public final static boolean ZOOM_LAUNCHER = true;
    public final static boolean ZOOM_UNLOCK = true;
    public final static boolean ZOOM_SYSTEM = false;
    public final static int ZOOM_DURATION = 1200;
    public final static int ZOOM_ROTATION = 40;
    public final static String WALLPAPER = Constants.WALLPAPER.PIXEL;
    public final static int PARALLAX = 1;
    public final static int DAMPING = 8;
    public final static int THRESHOLD = 5;
    public final static float SCALE = 1;
    public final static boolean GPU = true;
    public final static boolean LAUNCHER = false;
    public final static String LANGUAGE = null;
  }

  public final static class DESIGN {

    public final static String DOODLE = "doodle";
    public final static String MONET = "monet";
    public final static String ANNA = "anna";
  }

  public final static class WALLPAPER {

    public final static String PIXEL = "pixel";
    public final static String JOHANNA = "johanna";
    public final static String REIKO = "reiko";
    public final static String ANTHONY = "anthony";

    public final static String STONE = "stone";
    public final static String FLORAL = "floral";
    public final static String WATER = "water";
    public final static String MONET = "monet";

    public final static String LEAFY = "leafy";
    public final static String FOG = "fog";
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

  public final static class ACTION {

    public final static String THEME_CHANGED = "action_theme_changed";
    public final static String SETTINGS_CHANGED = "action_settings_changed";
  }
}
