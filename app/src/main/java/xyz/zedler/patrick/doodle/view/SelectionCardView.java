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

package xyz.zedler.patrick.doodle.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.LayerDrawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.elevation.SurfaceColors;
import xyz.zedler.patrick.doodle.R;
import xyz.zedler.patrick.doodle.util.ResUtil;
import xyz.zedler.patrick.doodle.util.SystemUiUtil;
import xyz.zedler.patrick.doodle.util.ViewUtil;

public class SelectionCardView extends MaterialCardView {

  private final MaterialCardView innerCard;

  public SelectionCardView(Context context) {
    super(context);

    final int outerRadius = SystemUiUtil.dpToPx(context, 16);
    final int outerPadding = SystemUiUtil.dpToPx(context, 16);
    final int innerSize = SystemUiUtil.dpToPx(context, 48);
    final int strokeWidth = SystemUiUtil.dpToPx(context, 1);

    // OUTER CARD (this)

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
    );
    if (ResUtil.isLayoutRtl(context)) {
      params.leftMargin = SystemUiUtil.dpToPx(context, 4);
    } else {
      params.rightMargin = SystemUiUtil.dpToPx(context, 4);
    }
    setLayoutParams(params);
    setContentPadding(outerPadding, outerPadding, outerPadding, outerPadding);
    setRadius(outerRadius);
    setCardElevation(0);
    setCardForegroundColor(null);
    super.setCardBackgroundColor(SurfaceColors.SURFACE_1.getColor(context));
    setRippleColor(ColorStateList.valueOf(ResUtil.getColorHighlight(context)));
    setStrokeWidth(0);
    setCheckable(true);
    setCheckedIconResource(R.drawable.shape_selection_check);
    setCheckedIconTint(null);
    setCheckedIconSize(innerSize - strokeWidth * 2);
    setCheckedIconMargin(outerPadding + strokeWidth);

    // INNER CARD

    ViewGroup.LayoutParams innerParams = new ViewGroup.LayoutParams(innerSize, innerSize);
    innerCard = new MaterialCardView(context);
    innerCard.setLayoutParams(innerParams);
    innerCard.setRadius(innerSize / 2f);
    innerCard.setStrokeWidth(strokeWidth);
    innerCard.setStrokeColor(ResUtil.getColorAttr(context, R.attr.colorOutline));
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

  public void setCardImageResource(@DrawableRes int resId) {
    ImageView image;
    if (innerCard.getChildCount() == 0) {
      image = new ImageView(getContext());
      image.setLayoutParams(
          new ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
          )
      );
      innerCard.addView(image);
    } else {
      image = (ImageView) innerCard.getChildAt(0);
    }
    image.setImageResource(resId);
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
}
