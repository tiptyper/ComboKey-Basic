/*
 * Copyright (c) 2020-2029 Seppo Tiainen, gkos@gkos.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.combokey.basic.view.theme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.combokey.basic.preferences.PreferencesModule;

public class ThemeManager {

	private final Map<String, Theme> themes = new HashMap<String, Theme>();

	private Theme activeTheme;

	final DrawableStrategy legacyDrawableStrategy = PreferencesModule.provideLegacyDrawableStrategy();

	final ThemeParser themeParser = new ThemeParser();

	public void setActiveTheme(Theme theme) {
		activeTheme = theme;
	}

	public Theme getActiveTheme() {
		return activeTheme;
	}


	public Theme getThemeByName(String name) {
		Theme theme = themes.get(name);

		Log.d("CMBO", "Getting theme " + name + "");

		if (theme == null) {
			Log.d("CMBO", "Getting legacy theme");
			theme = getLegacyTheme(name);
		}
		return theme;
	}

	private Theme getLegacyTheme(String name) {
		Theme theme = themes.get(name);

		if (theme == null) {
			theme = new Theme(name);

			Map<DrawableType, Drawable> drawables = new HashMap<DrawableType, Drawable>();
			for (DrawableType type : DrawableType.values()) {
				drawables.put(type,
						legacyDrawableStrategy.getDrawable(theme.getName(), type));
			}

			theme.setDrawables(drawables);
			
			// TODO
			themes.put(theme.getName(), theme);
		}

		return theme;
	}

	public List<Theme> installAllThemes(Context context) {
		Log.d("CMBO", "Initializing external themes");
		// this currently installs all themes into the manager
		List<Theme> themes = new ArrayList<Theme>();

		List<Theme> externalThemes = themeParser.getExternalThemes(context);

		if (themes != null) {
			try {
			themes.addAll(externalThemes);
			} catch(Exception e) {
				Log.d("CMBO", "Error initializing external themes");
			}

		}

		
		return themes;
	}

	public Collection<Theme> getThemes() {
		return themes.values();
	}

}
