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

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.Toast;

import com.combokey.basic.CMBOKeyboardApplication;

public class Theme {

	private Map<DrawableType, Drawable> drawables = new HashMap<DrawableType, Drawable>();

	private String name;

	public Theme(String name) {
		this.setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Drawable getDrawable(DrawableType drawableType, Context context) {
		Drawable drawable = drawables.get(drawableType);

		// Log.d("CMBO", "Getting drawable of type " + drawableType.name()
		// +" for theme " + name);

		// TODO need to add pressed key to theme configuration json instead of using temporary ugly hack
		if (drawable == null && (drawableType == DrawableType.PRESSED_KEY || drawableType == DrawableType.TOPBAR_IMAGE)) {
			drawable = CMBOKeyboardApplication.getApplication()
					.getThemeManager().getThemeByName("Cool").getDrawable(drawableType, context);

			this.drawables.put(drawableType, drawable);
		}

		// In addition to the above: Any other drawable wrong or missing?
		// Take it from theme Cool, or else the app will crash!
		if (drawable == null) {
			drawable = CMBOKeyboardApplication.getApplication()
					.getThemeManager().getThemeByName("Cool").getDrawable(drawableType, context);

			this.drawables.put(drawableType, drawable);

			String toastMessage = "Error in keyboard Theme files.\n May not show correctly.\n Use some other theme.";
			Toast toast;
			toast = Toast.makeText(CMBOKeyboardApplication.getApplication().getApplicationContext(), toastMessage, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		}

		return drawable;

	}

	public void setDrawables(Map<DrawableType, Drawable> drawables) {
		this.drawables = drawables;
	}

}
