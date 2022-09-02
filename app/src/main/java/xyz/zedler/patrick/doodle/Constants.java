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
 * Copyright (c) 2019-2022 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle;

import android.util.Log;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import xyz.zedler.patrick.doodle.wallpaper.AnthonyWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.AutumnWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.BaseWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.FloralWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.FogWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.JohannaWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.LeafyWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.LeavesWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.MonetWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.OrioleWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.PixelWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.ReikoWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.SandWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.StoneWallpaper;
import xyz.zedler.patrick.doodle.wallpaper.WaterWallpaper;

public final class Constants {

  private static final String TAG = Constants.class.getSimpleName();

  public static final String VARIANT_PREFIX = "variant_";
  public static final String COLOR_PREFIX = "color_";
  // e.g. color_floral_0_0 (Floral, first variant, primary color)
  // e.g. color_pixel_1_2_dark (Pixel, second variant, tertiary color, dark mode)

  public static String getThemeColorPref(
      String wallpaperName, int variant, int priority, boolean isNight
  ) {
    return COLOR_PREFIX + wallpaperName + "_" + variant + "_" + priority + (isNight ? "_dark" : "");
  }

  public static final class PREF {

    // Appearance

    public static final String WALLPAPER = "wallpaper";
    public static final String VARIANT_PIXEL = "variant_pixel";
    public static final String VARIANT_JOHANNA = "variant_johanna";
    public static final String VARIANT_REIKO = "variant_reiko";
    public static final String VARIANT_ANTHONY = "variant_anthony";

    public static final String NIGHT_MODE = "night_mode";
    public static final String USE_DARK_TEXT = "dark_text";
    public static final String FORCE_LIGHT_TEXT = "light_text";
    public static final String RANDOM = "random";
    public static final String RANDOM_LIST = "random_list";
    public static final String RANDOM_CURRENT = "random_current_wallpaper";
    public static final String DAILY_TIME = "daily_time";

    // Parallax

    public static final String PARALLAX = "parallax";
    public static final String POWER_SAVE_SWIPE = "power_save_swipe";
    public static final String TILT = "tilt";
    public static final String TILT_REFRESH_RATE = "refresh_rate";
    public static final String DAMPING_TILT = "damping_tilt";
    public static final String THRESHOLD = "threshold";
    public static final String POWER_SAVE_TILT = "power_save_tilt";

    // Size

    public static final String SCALE = "size";
    public static final String ZOOM = "zoom";
    public static final String ZOOM_ROTATION = "zoom_rotation";
    public static final String POWER_SAVE_ZOOM = "power_save_zoom";
    public static final String ZOOM_LAUNCHER = "zoom_launcher";
    public static final String USE_ZOOM_DAMPING = "use_zoom_damping";
    public static final String DAMPING_ZOOM = "damping_zoom";
    public static final String ZOOM_SYSTEM = "zoom_system";
    public static final String ZOOM_UNLOCK = "zoom_unlock";
    public static final String ZOOM_DURATION = "zoom_duration";

    // Other

    public static final String LANGUAGE = "language";
    public static final String GPU = "hardware_acceleration";
    public static final String THEME = "app_theme";
    public static final String MODE = "mode";
    public static final String USE_SLIDING = "use_sliding_transition";

    public static final String LAST_VERSION = "last_version";
    public static final String FEEDBACK_POP_UP_COUNT = "feedback_pop_up_count";
  }

  public static final class DEF {

    public static final String WALLPAPER = Constants.WALLPAPER.PIXEL;
    public static final int NIGHT_MODE = -1;
    public static final boolean USE_DARK_TEXT = true;
    public static final boolean FORCE_LIGHT_TEXT = false;
    public static final String RANDOM = Constants.RANDOM.OFF;
    public static final Set<String> RANDOM_LIST = new HashSet<>(
        Arrays.asList(Constants.getAllWallpapers())
    );
    public static final String DAILY_TIME = "3:00";

    public static final int PARALLAX = 2;
    public static final boolean POWER_SAVE_SWIPE = false;
    public static final boolean TILT = false;
    public static final int TILT_REFRESH_RATE = 30000;
    public static final int DAMPING_TILT = 8;
    public static final int THRESHOLD = 5;
    public static final boolean POWER_SAVE_TILT = true;

    public static final float SCALE = 1;
    public static final int ZOOM = 2;
    public static final int ZOOM_ROTATION = 40;
    public static final boolean POWER_SAVE_ZOOM = false;
    public static final boolean ZOOM_LAUNCHER = true;
    public static final boolean USE_ZOOM_DAMPING = false;
    public static final int DAMPING_ZOOM = 12;
    public static final boolean ZOOM_SYSTEM = false;
    public static final boolean ZOOM_UNLOCK = true;
    public static final int ZOOM_DURATION = 1200;

    public static final String LANGUAGE = null;
    public static final boolean GPU = true;
    public static final String THEME = "";
    public static final int MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    public static final boolean USE_SLIDING = false;
  }

  public static BaseWallpaper getWallpaper(String wallpaper) {
    switch (wallpaper) {
      // DOODLE
      case WALLPAPER.JOHANNA:
        return new JohannaWallpaper();
      case WALLPAPER.REIKO:
        return new ReikoWallpaper();
      case WALLPAPER.ANTHONY:
        return new AnthonyWallpaper();
      // MATERIAL YOU
      case WALLPAPER.FLORAL:
        return new FloralWallpaper();
      case WALLPAPER.AUTUMN:
        return new AutumnWallpaper();
      case WALLPAPER.STONE:
        return new StoneWallpaper();
      case WALLPAPER.WATER:
        return new WaterWallpaper();
      case WALLPAPER.SAND:
        return new SandWallpaper();
      case WALLPAPER.MONET:
        return new MonetWallpaper();
      case WALLPAPER.ORIOLE:
        return new OrioleWallpaper();
      // KÖVECSES
      case WALLPAPER.LEAFY:
        return new LeafyWallpaper();
      case WALLPAPER.FOG:
        return new FogWallpaper();
      case WALLPAPER.LEAVES:
        return new LeavesWallpaper();
      default:
        return new PixelWallpaper();
    }
  }

  public static BaseWallpaper getRandomWallpaper(Set<String> selection, String previous) {
    BaseWallpaper chosen;
    if (selection.isEmpty()) {
      chosen = getWallpaper(previous != null && !previous.isEmpty() ? previous : DEF.WALLPAPER);
    } else {
      String[] wallpapers = selection.toArray(new String[0]);
      if (wallpapers.length == 1) {
        chosen = getWallpaper(wallpapers[0]);
      } else {
        try {
          Random randomizer = new Random();
          do {
            chosen = getWallpaper(wallpapers[randomizer.nextInt(wallpapers.length)]);
          } while (chosen.getName().equals(previous));
        } catch (IllegalArgumentException e) {
          Log.e(TAG, "getRandomWallpaper: ", e);
          chosen = getWallpaper(DEF.WALLPAPER);
        }
      }
    }
    return chosen;
  }

  public static String[] getAllWallpapers() {
    return new String[]{
        WALLPAPER.PIXEL,
        WALLPAPER.JOHANNA,
        WALLPAPER.REIKO,
        WALLPAPER.ANTHONY,

        WALLPAPER.FLORAL,
        WALLPAPER.AUTUMN,
        WALLPAPER.STONE,
        WALLPAPER.WATER,
        WALLPAPER.SAND,
        WALLPAPER.MONET,
        WALLPAPER.ORIOLE,

        WALLPAPER.LEAFY,
        WALLPAPER.FOG,
        WALLPAPER.LEAVES
    };
  }

  public static final class DESIGN {

    public static final String DOODLE = "doodle";
    public static final String MONET = "monet";
    public static final String ANNA = "anna";
  }

  public static final class WALLPAPER {

    public static final String PIXEL = "pixel";
    public static final String JOHANNA = "johanna";
    public static final String REIKO = "reiko";
    public static final String ANTHONY = "anthony";

    public static final String STONE = "stone";
    public static final String FLORAL = "floral";
    public static final String AUTUMN = "autumn";
    public static final String WATER = "water";
    public static final String SAND = "sand";
    public static final String MONET = "monet";
    public static final String ORIOLE = "oriole";

    public static final String LEAFY = "leafy";
    public static final String FOG = "fog";
    public static final String LEAVES = "leaves";
  }

  public static final class NIGHT_MODE {

    public static final int AUTO = -1;
    public static final int ON = 1;
    public static final int OFF = 0;
  }

  public static final class RANDOM {

    public static final String OFF = "off";
    public static final String DAILY = "daily";
    public static final String SCREEN_OFF = "screen_off";
  }

  public static final class USER_PRESENCE {

    public static final String LOCKED = "locked";
    public static final String OFF = "off";
    public static final String UNLOCKED = "unlocked";
  }

  public static final class REQUEST_SOURCE {

    public static final String ZOOM_LAUNCHER = "zoom_launcher";
    public static final String ZOOM_UNLOCK = "zoom_unlock";
    public static final String TILT = "tilt";
    public static final String SWIPE = "swipe";
  }

  public static final class ACTION {

    public static final String THEME_AND_DESIGN_CHANGED = "action_theme_and_design_changed";
    public static final String THEME_CHANGED = "action_theme_changed";
    public static final String SETTINGS_CHANGED = "action_settings_changed";
    public static final String NEW_DAILY = "action_new_daily";
  }

  public static final class EXTRA {

    public static final String RUN_AS_SUPER_CLASS = "run_as_super_class";
    public static final String INSTANCE_STATE = "instance_state";
    public static final String SHOW_FORCE_STOP_REQUEST = "show_force_stop_request";
    public static final String SCROLL_POSITION = "scroll_position";
    public static final String PICKER_SHOWING = "is_picker_showing";
    public static final String COLOR = "color";
  }

  public static final class THEME {

    public static final String DYNAMIC = "dynamic";
    public static final String RED = "red";
    public static final String YELLOW = "yellow";
    public static final String LIME = "lime";
    public static final String GREEN = "green";
    public static final String TURQUOISE = "turquoise";
    public static final String TEAL = "teal";
    public static final String BLUE = "blue";
    public static final String PURPLE = "purple";
    public static final String AMOLED = "amoled";
  }
}
