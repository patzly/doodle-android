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

package xyz.zedler.patrick.doodle.util;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SheetUtil {

  private final FragmentManager fragmentManager;

  public SheetUtil(@NonNull FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
  }

  public void show(BottomSheetDialogFragment fragment) {
    show(fragment, null);
  }

  public void show(BottomSheetDialogFragment fragment, Bundle bundle) {
    if (bundle != null) {
      fragment.setArguments(bundle);
    }
    fragment.show(fragmentManager, fragment.toString());
  }
}
