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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.combokey.basic.CMBOKeyboardApplication;

public class ThemeParser {

	private static final String THEME_BUTTON_IMAGE_JSON_KEY = "buttonimage";
	private static final String THEME_METADATA_JSON_KEY = "metadata";
	public static final String THEME_NAME_JSON_KEY = "name";
	public static final String THEME_TEXTAREA_JSON_KEY = "textarea";
	public static final String THEME_BACKGROUNDIMAGE_JSON_KEY = "backgroundimage";

	/*
	private static final String THEME_PADAREA_JSON_KEY = "padarea";
	private static final String THEME_PADBACKGROUND_JSON_KEY = "padbackground";

	private static final String THEME_TEXTAREA_JSON_KEY = "textarea";
	private static final String THEME_TEXTBACKGROUND_JSON_KEY = "textbackground";
	*/

	public List<Theme> getExternalThemes(Context context) {

		List<Theme> themes = new ArrayList<Theme>();

		Log.d("-MEDIA", "Trying to load themes from external directory.");

		File storageDirectory = CMBOKeyboardApplication.getApplication()
				.getStorageDirectory();

		if (storageDirectory == null)
			return null;

		if (storageDirectory != null) {
			themes.addAll(loadThemes(storageDirectory));
		}

		String[] themeAssets;
		try {
			themeAssets = context.getAssets().list("themes");
			Log.d("CMBO", "Got " + themeAssets.length + " assets");
			for (String themeAsset : themeAssets) {
				unpackTheme(context, themeAsset);
			}
		} catch (IOException e) {
			Log.e("CMBO",
					"Directory 'themes' not found under application assets.");
		}

		return themes;
	}

	private List<Theme> loadThemes(File storageDirectory) {
		List<Theme> themes = new ArrayList<Theme>();
		File layoutDir = new File(storageDirectory, "themes/");

		if (layoutDir.mkdirs() || layoutDir.isDirectory()) {
			Log.d("CMBO",
					"Looking for external theme files in "
							+ layoutDir.getAbsolutePath());
			String[] files = layoutDir.list(new FilenameFilter() {
				public boolean accept(File file, String name) {
					return name.matches(".*\\.zip");
				}
			});
			if (files == null) return themes; // added to avoid NullPointerException, 2018-04-29 v3.4

			for (String fileName : files) { // v3.3 could crash here, caused by: java.lang.NullPointerException
				File themeFile = new File(layoutDir, fileName);

				Log.d("CMBO",
						"Attempting to load themes from "
								+ themeFile.getAbsolutePath());

				Theme theme = unpackTheme(themeFile);

				if (theme != null) {
					Log.d("CMBO", "Storing theme " + theme);
					themes.add(theme);
				}
			}

		}

		return themes;
	}

	private Theme unpackTheme(Context context, String asset) {
		InputStream inputStream;
		try {
			inputStream = context.getAssets().open("themes/" + asset);
			return unpackTheme(inputStream);
		} catch (IOException e) {
			Log.e("CMBO", "Unable to open asset " + asset, e);
		}
		return null;
	}

	private Theme unpackTheme(File themeFile) {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(themeFile);
			return unpackTheme(inputStream);
		} catch (FileNotFoundException e) {
			Log.e("CMBO", "Theme file " + themeFile.getName() + " not found.");
		}

		return null;
	}

	private Theme unpackTheme(InputStream themeFileStream) {

		ZipInputStream zipInputStream;
		Theme theme = null;

		Log.d("CMBO", "--- Unpacking theme file ");
		try {

			Map<DrawableType, String> typeToFileMapping = new HashMap<DrawableType, String>();
			Map<String, Drawable> fileToDrawableMapping = new HashMap<String, Drawable>();
			Map<DrawableType, Drawable> typeToDrawableMapping = new HashMap<DrawableType, Drawable>();

			zipInputStream = new ZipInputStream(themeFileStream);
			ZipEntry zipEntry;

			while ((zipEntry = zipInputStream.getNextEntry()) != null) {

				String fileName = zipEntry.getName();

				Log.d("CMBO", "Processing entry (file name): " + fileName);

				if (fileName.endsWith(".json")) {
					Log.d("CMBO", "Parsing theme JSON metadata");
					theme = parseThemeMetadata(zipInputStream,
							typeToFileMapping);
					if (theme == null) {
						Log.e("CMBO", "Was unable to parse theme JSON metadata");
						return null;
					}

				} else if (fileName.endsWith(".png")) {
					Log.d("CMBO", "Processing IMAGE file");
					Drawable drawable = Drawable.createFromStream(
							zipInputStream, zipEntry.getName());
					fileToDrawableMapping.put(fileName, drawable);
				}

				Log.d("CMBO", "Done with entry " + zipEntry.getName());
				zipInputStream.closeEntry();
			}

			Log.d("CMBO", "Done with zip.");
			zipInputStream.close();

			for (Entry<DrawableType, String> entry : typeToFileMapping
					.entrySet()) {
				DrawableType type = entry.getKey();
				String file = entry.getValue();
				Drawable drawable = fileToDrawableMapping.get(file);
				if (drawable != null) {
					typeToDrawableMapping.put(type, drawable);
					Log.d("CMBO", "Drawable type: " + type + ", Drawable: " + drawable);
				}
			}

			//try {
				theme.setDrawables(typeToDrawableMapping);

			//} catch (Exception e) { // was added and then commented 2/2018

			//	Log.d("CMBO", "Problem setting Drawables.", e);
			//}

			return theme;

		} catch (IOException e) {

			Log.d("CMBO", "IO Exception. ", e);
		}


		return null;
	}

	private static String loadFileContents(InputStream inputStream) {

		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					inputStream));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
				total.append("\n");
			}
			return total.toString();

		} catch (IOException e) {
			Log.e("CMBO", "Unable to read in given file");
			// just return null
		}
		return null;
	}

	private Theme parseThemeMetadata(InputStream inputStream,
			Map<DrawableType, String> typeToFileMapping) {

		try {
			String contents = loadFileContents(inputStream);

			JSONObject json = new JSONObject(contents);

			String name = json.getJSONObject(THEME_METADATA_JSON_KEY)
					.getString(THEME_NAME_JSON_KEY);

			String backgroundimage = json.getJSONObject(THEME_TEXTAREA_JSON_KEY)
					.getString(THEME_BACKGROUNDIMAGE_JSON_KEY);

			Theme theme = CMBOKeyboardApplication.getApplication()
					.getThemeManager().getThemeByName(name);

			for (DrawableType type : DrawableType.values()) {
				JSONObject buttonImages = json
						.getJSONObject(THEME_BUTTON_IMAGE_JSON_KEY);
				if (buttonImages.has(type.getKey())) {
					String fileForType = buttonImages.getString(type.getKey());
					if (fileForType != null) {
						typeToFileMapping.put(type, fileForType);
					}
				}
			}

			for (DrawableType type : DrawableType.values()) {
				JSONObject buttonImages = json
						.getJSONObject(THEME_TEXTAREA_JSON_KEY);
				if (buttonImages.has(type.getKey())) {
					String fileForType = buttonImages.getString(type.getKey());
					if (fileForType != null) {
						typeToFileMapping.put(type, fileForType);
					}
				}
			}



			return theme;

		} catch (JSONException e) {
			Log.e("CMBO", "Unable to parse theme description", e);
		}
		return null;
	}
}