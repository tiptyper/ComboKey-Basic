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

package com.combokey.basic;

import java.io.File;
import java.util.List;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.combokey.basic.layout.LayoutManager;
import com.combokey.basic.preferences.Preferences;
import com.combokey.basic.view.theme.Theme;
import com.combokey.basic.view.theme.ThemeManager;
import com.combokey.basic.view.touchevent.SimpleTouchEventProcessor;

public class CMBOKeyboardApplication extends Application {

	private static CMBOKeyboardApplication instance;

	private CMBOManager manager = new CMBOManager();

	public CMBOKeyboardApplication() {
		instance = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		registerPreferences();
		
		// TODO relocate
		List<Theme> externalThemes = getThemeManager().installAllThemes(this.getApplicationContext());
		Log.d("-THEMES", "Got " + externalThemes.size() + " external themes");}

	private void registerPreferences() {
		getPreferences().register(this);
	}

	public CMBOManager getCMBOManager() {
		return this.manager;
	}
	
	public Preferences getPreferences() {
		return this.manager.getPreferences();
	}

	public CMBOKeyboardController getController() {
		return this.manager.getController();
	}

	public CMBOWordCandidates getCandidates() {
		return this.manager.getCandidates();
	}

	public CMBOKeyboard getKeyboard() { // 2017-02-02
		return this.manager.getKeyboard();
	}

	public LayoutManager getLayoutManager() {
		return this.manager.getLayoutManager();
	}
	
	public ThemeManager getThemeManager() {
		return this.manager.getThemeManager();
	}

	public SimpleTouchEventProcessor getTouchEventProcessor(
			boolean smallLandscape) {
		return this.manager.getTouchEventProcessor(smallLandscape);
	}

	public SimpleTouchEventProcessor getTouchEventProcessor() {
		return this.getTouchEventProcessor(false);
	}

	public static CMBOKeyboardApplication getApplication() {
		return instance;
	}

	/**
	 * The output can be either the activity or the service, and we only know at
	 * runtime.
	 * 
	 * @param output
	 */
	public void setOutput(KeyboardOutput output) {
		this.manager.getKeyboard().setOutput(output);
	}

	public boolean isStrippedApp(){ // 2019-04-13 StrippedApp option draft for less settings and options in special app versions
		return false;
	}

	public File getStorageDirectory() {

		// 2019-01-24
		// Below, sometimes we cannot use:
		//   File dir = Environment.getExternalStorageDirectory();
		// because it is not always available in all devices.
		// If not available, we will instead be using:
		//   File dir = getFilesDir();
		// where files will be deleted at app uninstall.
        // We will try both, first ExternalStorageDirectory, if not successful, then FilesDir.

		String mountStatus = Environment.getExternalStorageState();
		Log.i("-MEDIA", "External media mount status: " + mountStatus);

		if (Environment.MEDIA_MOUNTED.equals(mountStatus)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(mountStatus))
		{
			File dir = Environment.getExternalStorageDirectory();
			// File dir = getFilesDir();
			File subDirectory = new File(dir, "ComboKeyBasic/");

			if (subDirectory.mkdirs() || subDirectory.isDirectory()) {
				Log.e("-MEDIA", "OK. Using External Storage directory");
				return subDirectory;
			}
			Log.e("-MEDIA", "Cannot access External Storage directory: \n" + subDirectory);
		} else {
			Log.e("-MEDIA", "External Storage media not mounted.");
		}

		// ------------------------ use internal memory instead ---------------------

		Log.e("-MEDIA", "Trying to use Internal Memory directory instead...");

		File dir = getFilesDir();
		File subDirectory = new File(dir, "ComboKeyBasic/");

		if (subDirectory.mkdirs() || subDirectory.isDirectory()) {
			Log.e("-MEDIA", "OK! Using Internal Memory directory: \n" + subDirectory);
			return subDirectory;
		}
		Log.e("-MEDIA", "Cannot even access Internal Memory directory!");

		return null;
	}


}
