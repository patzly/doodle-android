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

  public void show(BottomSheetDialogFragment bottomSheetDialog) {
    show(bottomSheetDialog, null);
  }

  public void show(BottomSheetDialogFragment bottomSheetDialog, Bundle bundle) {
    if (bundle != null) {
      bottomSheetDialog.setArguments(bundle);
    }
    bottomSheetDialog.show(fragmentManager, bottomSheetDialog.toString());
  }
}
