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

  public final static int REQUEST_CODE = 1;

  public final static class PREF {

    public final static String NIGHT_MODE = "night_mode";
    public final static String FOLLOW_SYSTEM = "follow_system";
    public final static String ZOOM = "zoom";
    public final static String WALLPAPER = "wallpaper";
    public final static String VARIANT = "variant";
    public final static String PARALLAX = "parallax";
    public final static String SIZE = "size";

    public final static String SETTINGS_CHANGED = "settings_changed";
    public final static String CHANGES_APPLIED = "changes_applied";

    public final static String LAST_VERSION = "last_version";
  }

  public final static class DEF {

    public final static boolean NIGHT_MODE = true;
    public final static boolean FOLLOW_SYSTEM = true;
    public final static int ZOOM = 1;
    public final static String WALLPAPER = Constants.WALLPAPER.DOODLE;
    public final static String VARIANT = Constants.VARIANT.BLACK;
    public final static int PARALLAX = 100;
    public final static float SIZE = 1;
  }

  public final static class WALLPAPER {

    public final static String DOODLE = "doodle";
    public final static String NEON = "neon";
    public final static String GEOMETRIC = "geometric";
  }

  public final static class VARIANT {

    public final static String BLACK = "black";
    public final static String WHITE = "white";
    public final static String ORANGE = "orange";
  }

  public final static class EXTRA {

    public final static String TITLE = "title";
    public final static String LINK = "link";
    public final static String FILE = "file";
  }
}
