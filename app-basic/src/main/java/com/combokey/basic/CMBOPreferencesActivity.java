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

import java.util.Collection;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.combokey.basic.layout.Layout;
import com.combokey.basic.layout.Layout.Metadata;
import com.combokey.basic.view.theme.Theme;

public class CMBOPreferencesActivity extends PreferenceActivity  {

	public static final String TAG = "CMBOKeyboard";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		addLanguagePreferences();
		addLanguage2Preferences();
		addThemePreferences();
		setTitle(R.string.settings_header);

		if (CMBOKeyboardApplication.getApplication().isStrippedApp()) setStrippedList(); //
		setStrippedListTemp(); // Always hide items not finished yet

	}

	@Override
	public void onDestroy() { // 2017
		super.onDestroy();
		this.finish();
	}

	@Override
	public void onStop() { // 2017
		super.onStop();
		this.finish();
	}

	// ----------------------------------------

	public void setStrippedList() { // 2019-04-13 strippedApp, remove (= hide) part of preferences
		// First set certain preferences somewhere else:
		/*
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = preferences.edit();
		CMBOKeyboardApplication.getApplication().getPreferences().register(this);
		editor.putBoolean("PREFERENCES_TRANSPARENT_BACKGROUND", false); // Not usable here, only called when settings are visible
		editor.apply();
		*/
		// -----------------------------------------------------------------------------------
		// Then hide some of these settings:

		String[] prefsToHide = {"PREFERENCES_DISPLAY_WORD_CANDIDATES",
				"PREFERENCES_DISPLAY_MIRROR_CHARACTERS", "PREFERENCES_SOUND_ON",
				"PREFERENCES_VIBRATOR_ON", "PREFERENCES_COMBO_FLASH_ON", "PREFERENCES_SYMBOL_FLASH_ON", "PREFERENCES_FONT_SIZE",
				"PREFERENCES_PADPOSITION","PREFERENCES_PADHEIGHT", "PREFERENCES_PADTYPE", "PREFERENCES_SPEAK",
				"PREFERENCES_LANDSCAPECOLUMNS","PREFERENCES_AUTOCAPS", "PREFERENCES_SINGLE_HAND",
				"PREFERENCES_HYST_AMOUNT", "PREFERENCES_DISPLAY_NO_CHARACTERS",
				"PREFERENCES_TERMINAL_MODE","PREFERENCES_LAUNCH", "PREFERENCES_REMOVE_TRAILING_SPACE",
				"PREFERENCES_DISABLE_LANGUAGE_COMBO", "PREFERENCES_DISPLAY_HELP_CHARACTERS",
				"PREFERENCES_TIPTYPE", "PREFERENCES_TYPE_METHOD", "PREFERENCES_THEME", "PREFERENCES_FONT_TYPE",
				"PREFERENCES_SOUND_VOLUME", "PREFERENCES_SIDE_PANEL_HIDDEN", "PREFERENCES_ONEHAND_COMBO", "PREFERENCES_HIGHLIGHT_OPTION"};

		for (int i = 0; i < prefsToHide.length; i++) {
			Log.d("-PREFS", "Hide preference " + i + ", = " + prefsToHide[i]);
					hidePreference(prefsToHide[i]);
		}

	}

	public void setStrippedListTemp() { // Hide unfinished or removed items quickly in settings

		if (android.os.Build.VERSION.SDK_INT < 26) { // less than Oreo

			String[] prefsToHide1 = {"PREFERENCES_LAUNCH",
					"PREFERENCES_DISPLAY_MIRROR_CHARACTERS", "PREFERENCES_SINGLE_HAND", "PREFERENCES_COMBO_FLASH_ON",
					"PREFERENCES_FONT_TYPE"};
			for (int i = 0; i < prefsToHide1.length; i++) {
				Log.d("-PREFS", "Hide preference " + i + ", = " + prefsToHide1[i]);
				hidePreference(prefsToHide1[i]);
			}
		} else { // Oreo and up

			String[] prefsToHide2 = {"PREFERENCES_LAUNCH",
					"PREFERENCES_DISPLAY_MIRROR_CHARACTERS", "PREFERENCES_SINGLE_HAND", "PREFERENCES_COMBO_FLASH_ON"};
			for (int i = 0; i < prefsToHide2.length; i++) {
				Log.d("-PREFS", "Hide preference " + i + ", = " + prefsToHide2[i]);
				hidePreference(prefsToHide2[i]);
			}
		}

	}



	private void hidePreference(String key) { // 2019-04-13
		PreferenceScreen screen = getPreferenceScreen();
		Preference pref = getPreferenceManager().findPreference(key);
		screen.removePreference(pref);
	}

	// ----------------------------------------------------------


	private void addThemePreferences() {
		ListPreference themeListPreference = (ListPreference) getPreferenceScreen()
				.findPreference("PREFERENCES_THEME");

		Collection<Theme> themes = CMBOKeyboardApplication.getApplication()
				.getThemeManager().getThemes();

		int oldSize = themeListPreference.getEntries().length;
		
		CharSequence[] entries = new CharSequence[oldSize + themes.size()];
		CharSequence[] entryValues = new CharSequence[oldSize + themes.size()];

		int entryIndex = 0;

		for (CharSequence entry : themeListPreference.getEntries()) {
			entries[entryIndex] = entry;
			entryValues[entryIndex] = entry;
			entryIndex++;
		}
		
		for (Theme theme : themes) {
			entries[entryIndex] = theme.getName();
			entryValues[entryIndex] = theme.getName();
			entryIndex++;
		}
	
		themeListPreference.setEntries(entries);
		themeListPreference.setEntryValues(entryValues);	
	}

	private void addLanguagePreferences() {
		ListPreference languageListPreference = (ListPreference) getPreferenceScreen()
				.findPreference("PREFERENCES_LANGUAGE");

		Collection<Layout> layouts = CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getLayouts();

		String[] entries = new String[layouts.size()];
		String[] entryValues = new String[layouts.size()];

		int entryIndex = 0;

		for (Layout layout : layouts) {

			if (layout.getValue(Metadata.DESCRIPTION).equals(layout.getValue(Metadata.NAME))){
				entries[entryIndex] = layout.getValue(Metadata.DESCRIPTION) + " (v" // ComboKey Plus GROUP => LAYOUT_VERSION
						+ layout.getValue(Metadata.LAYOUT_VERSION) + ")";
			} else {
				entries[entryIndex] = layout.getValue(Metadata.DESCRIPTION) + " (" + layout.getValue(Metadata.NAME) + " v" // ComboKey Plus GROUP => LAYOUT_VERSION
						+ layout.getValue(Metadata.LAYOUT_VERSION) + ")";
			}

			entryValues[entryIndex] = layout.toString();
			entryIndex++;
		}

		languageListPreference.setEntries(entries);
		languageListPreference.setEntryValues(entryValues);

	}

	// ==========================

	private void addLanguage2Preferences() {
		ListPreference languageListPreference = (ListPreference) getPreferenceScreen()
				.findPreference("PREFERENCES_LANGUAGE2");

		Collection<Layout> layouts = CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getLayouts();

		String[] entries = new String[layouts.size()];
		String[] entryValues = new String[layouts.size()];

		int entryIndex = 0;

		for (Layout layout : layouts) {

			if (layout.getValue(Metadata.DESCRIPTION).equals(layout.getValue(Metadata.NAME))){
				entries[entryIndex] = layout.getValue(Metadata.DESCRIPTION) + " (v" // ComboKey Plus GROUP => LAYOUT_VERSION
						+ layout.getValue(Metadata.LAYOUT_VERSION) + ")";
			} else {
				entries[entryIndex] = layout.getValue(Metadata.DESCRIPTION) + " (" + layout.getValue(Metadata.NAME) + " v" // ComboKey Plus GROUP => LAYOUT_VERSION
						+ layout.getValue(Metadata.LAYOUT_VERSION) + ")";
			}


			entryValues[entryIndex] = layout.toString();
			entryIndex++;
		}

		languageListPreference.setEntries(entries);
		languageListPreference.setEntryValues(entryValues);

	}



}
