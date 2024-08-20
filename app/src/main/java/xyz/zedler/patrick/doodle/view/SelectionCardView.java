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
 * Copyright (c) 2019-2024 by Patrick Zedler
 */

package xyz.zedler.patrick.doodle.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.ColorRoles;
import com.google.android.material.color.MaterialColors;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.UiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class SelectionCardView extends MaterialCardView {

  private final MaterialCardView innerCard;
  private final int innerSize;

  public SelectionCardView(Context context) {
    super(context);

    final int outerRadius = UiUtil.dpToPx(context, 16);
    final int outerPadding = UiUtil.dpToPx(context, 16);
    innerSize = UiUtil.dpToPx(context, 48);

    // OUTER CARD (this)

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
    );
    if (UiUtil.isLayoutRtl(context)) {
      params.leftMargin = UiUtil.dpToPx(context, 4);
    } else {
      params.rightMargin = UiUtil.dpToPx(context, 4);
    }
    setLayoutParams(params);
    setContentPadding(outerPadding, outerPadding, outerPadding, outerPadding);
    setRadius(outerRadius);
    setCardElevation(0);
    setCardForegroundColor(null);
    super.setCardBackgroundColor(ResUtil.getColor(context, R.attr.colorSurfaceContainer));
    setRippleColor(ColorStateList.valueOf(ResUtil.getColorHighlight(context)));
    setStrokeWidth(0);
    setCheckable(true);
    setCheckedIconResource(R.drawable.shape_selection_check);
    setCheckedIconTint(null);
    setCheckedIconSize(innerSize);
    setCheckedIconMargin(outerPadding);

    // INNER CARD

    ViewGroup.LayoutParams innerParams = new ViewGroup.LayoutParams(innerSize, innerSize);
    innerCard = new MaterialCardView(context);
    innerCard.setLayoutParams(innerParams);
    innerCard.setRadius(innerSize / 2f);
    innerCard.setStrokeWidth(UiUtil.dpToPx(context, 1));
    innerCard.setStrokeColor(ResUtil.getColor(context, R.attr.colorOutline));
    innerCard.setCheckable(false);
    addView(innerCard);
  }

  @Override
  public void setCardBackgroundColor(int color) {
    if (innerCard != null) {
      innerCard.setCardBackgroundColor(color);
    }
  }

  @NonNull
  @Override
  public ColorStateList getCardBackgroundColor() {
    if (innerCard != null) {
      return innerCard.getCardBackgroundColor();
    } else {
      return super.getCardBackgroundColor();
    }
  }

  public void setCardImageResource(@DrawableRes int resId, boolean tint) {
    innerCard.removeAllViews();
    ImageView image = new ImageView(getContext());
    image.setLayoutParams(
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    );
    Drawable drawable = ResourcesCompat.getDrawable(
            getResources(), resId, null
    );
    if (tint && drawable != null) {
      drawable.setTint(ResUtil.getColor(getContext(), R.attr.colorOutline));
    }
    image.setImageDrawable(drawable);
    image.setImageResource(resId);
    innerCard.addView(image);
  }

  public void setNestedContext(Context context) {
    removeAllViews();
    ViewGroup.LayoutParams innerParams = new ViewGroup.LayoutParams(innerSize, innerSize);
    MaterialCardView innerCard = new MaterialCardView(context);
    innerCard.setLayoutParams(innerParams);
    innerCard.setRadius(innerSize / 2f);
    innerCard.setStrokeWidth(UiUtil.dpToPx(context, 1));
    innerCard.setStrokeColor(ResUtil.getColor(context, R.attr.colorOutline));
    innerCard.setCardBackgroundColor(ResUtil.getColor(context, R.attr.colorPrimaryContainer));
    innerCard.setCheckable(false);
    addView(innerCard);
    setCheckedIconTint(
        ColorStateList.valueOf(ResUtil.getColor(context, R.attr.colorOnPrimaryContainer))
    );
  }

  public void startCheckedIcon() {
    try {
      LayerDrawable layers = (LayerDrawable) getCheckedIcon();
      if (layers != null) {
        ViewUtil.startIcon(layers.findDrawableByLayerId(R.id.icon_selection_check));
      }
    } catch (ClassCastException ignored) {
      // For API 21 it will be a androidx.core.graphics.drawable.WrappedDrawableApi21
    }
  }

  public void setOuterCardBackgroundColor(int color) {
    super.setCardBackgroundColor(color);
  }

  public void setScrimEnabled(boolean enabled, boolean ensureContrast) {
    setCheckedIconResource(
        enabled ? R.drawable.shape_selection_check_scrim : R.drawable.shape_selection_check
    );
    if (!enabled && ensureContrast) {
      int bg = getCardBackgroundColor().getDefaultColor();
      ColorRoles roles = MaterialColors.getColorRoles(bg, MaterialColors.isColorLight(bg));
      setCheckedIconTint(ColorStateList.valueOf(roles.getOnAccentContainer()));
    } else if (!enabled) {
      setCheckedIconTint(
          ColorStateList.valueOf(ResUtil.getColor(getContext(), R.attr.colorOnPrimaryContainer))
      );
    }
  }
}
