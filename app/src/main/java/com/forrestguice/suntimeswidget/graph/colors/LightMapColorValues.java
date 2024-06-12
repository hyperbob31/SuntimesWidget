// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2024 Forrest Guice
    This file is part of SuntimesWidget.

    SuntimesWidget is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SuntimesWidget is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimeswidget.graph.colors;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.forrestguice.suntimeswidget.R;
import com.forrestguice.suntimeswidget.colors.ColorValues;

/**
 * ColorValues
 */
public class LightMapColorValues extends GraphColorValues implements Parcelable
{
    @Override
    public String[] getColorKeys() {
        return new String[] {
                COLOR_DAY, COLOR_CIVIL, COLOR_NAUTICAL, COLOR_ASTRONOMICAL, COLOR_NIGHT,
                COLOR_POINT_FILL, COLOR_POINT_STROKE
        };
    }
    @Override
    public int[] getColorAttrs() {
        return new int[] {
                R.attr.graphColor_day, R.attr.graphColor_civil, R.attr.graphColor_nautical, R.attr.graphColor_astronomical, R.attr.graphColor_night,
                R.attr.graphColor_pointFill, R.attr.graphColor_pointStroke,
        };
    }
    @Override
    public int[] getColorLabelsRes() {
        return new int[] {
                R.string.timeMode_day, R.string.timeMode_civil, R.string.timeMode_nautical, R.string.timeMode_astronomical, R.string.timeMode_night,
                R.string.graph_option_points, R.string.graph_option_points,
        };
    };
    @Override
    public int[] getColorsResDark() {
        return new int[] {
                R.color.graphColor_day_dark, R.color.graphColor_civil_dark, R.color.graphColor_nautical_dark, R.color.graphColor_astronomical_dark, R.color.graphColor_night_dark,
                R.color.graphColor_pointFill_dark, R.color.graphColor_pointStroke_dark,
        };
    }
    @Override
    public int[] getColorsResLight() {
        return new int[] {
                R.color.graphColor_day_light, R.color.graphColor_civil_light, R.color.graphColor_nautical_light, R.color.graphColor_astronomical_light, R.color.graphColor_night_light,
                R.color.graphColor_pointFill_light, R.color.graphColor_pointStroke_light,
        };
    }
    @Override
    public int[] getColorsFallback() {
        return new int[] {
                Color.YELLOW, Color.CYAN, Color.BLUE, Color.DKGRAY, Color.BLACK,
                Color.DKGRAY, Color.DKGRAY,
        };
    }

    public LightMapColorValues(ColorValues other) {
        super(other);
    }
    public LightMapColorValues(SharedPreferences prefs, String prefix) {
        super(prefs, prefix);
    }
    private LightMapColorValues(Parcel in) {
        super(in);
    }
    public LightMapColorValues() {
        super();
    }
    public LightMapColorValues(Context context) {
        this(context, true);
    }
    public LightMapColorValues(Context context, boolean darkTheme) {
        super(context, darkTheme);
    }
    public LightMapColorValues(String jsonString) {
        super(jsonString);
    }

    public static final Creator<LightMapColorValues> CREATOR = new Creator<LightMapColorValues>()
    {
        public LightMapColorValues createFromParcel(Parcel in) {
            return new LightMapColorValues(in);
        }
        public LightMapColorValues[] newArray(int size) {
            return new LightMapColorValues[size];
        }
    };

    public static LightMapColorValues getColorDefaults(Context context, boolean darkTheme) {
        return new LightMapColorValues(new LightMapColorValues().getDefaultValues(context, darkTheme));
    }
}
