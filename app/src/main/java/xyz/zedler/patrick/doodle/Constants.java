/*
 * This file is part of Doodle Android.
 * Doodle Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Doodle Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Doodle Android. If not, see <http://www.gnu.org/licenses/>.
 * Copyright (c) 2021 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle;

public final class Constants {

  public final static int REQUEST_CODE = 1;

  public final static class PREF {

    public final static String NIGHT_MODE = "night_mode";
    public final static String FOLLOW_SYSTEM = "follow_system";
    public final static String THEME = "theme";
    public final static String VARIANT = "variant";
    public final static String PARALLAX = "parallax";
    public final static String SIZE = "size";
  }

  public final static class THEME {

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
