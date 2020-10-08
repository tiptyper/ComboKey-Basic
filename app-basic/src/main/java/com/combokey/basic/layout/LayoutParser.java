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

package com.combokey.basic.layout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import static java.lang.Long.valueOf;

public class LayoutParser {

	private final static int NA = -1;

	private static final String KEY_PRIMARY = "primary";
	//private static final String KEY_PRIMARY2 = "primary2";
	private static final String KEY_PUNCTUATION = "punctuation";
	private static final String KEY_EXTRA = "extra"; // added for Combokey key M as modifier
	private static final String KEY_MAP = "map"; // added for Combokey to introduce JSON code maps


	private static final int[] INDICES_PRIMARY = {
			1, 2, 3, 4, 5, 6, 15, 19, 7,
			11, 27, 23, 16, 17, 18, 20, 21, 22, 8,
			9, 10, 12, 13, 14, 28, 41, 29, 39, 30,
			24, 58, 25, 40, 26, 64, 38, 37, 44, 45 // 40 items, ref 45 = PgDn
	};
	private static final int[] INDICES_PUNCTUATION = {
			35, 31, 33, 36, 32, 34,	38, 37 // 8 items, ref 37 = backslash
	};
	private static final int[] INDICES_EXTRA = {
			0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 // 15 items
	}; // OK now


	// to be used for loading the aux language set:

	private int langOffset = 0; // 320 or 0 depending wheter loading aux lang set or main lang set
	private int langExtraOffset = 0; // 60 or 0 depending whether loading aux lang set or main lang set

	private String lowerCaseIndicator = "abc"; // to be refactored for showing the currentlayout! not the last parsed
	private String upperCaseIndicator = "Abc";
	private String capsIndicator = "ABC";
	private String numbersIndicator = "123";
	private String symbolsIndicator = "#@";


	public String getLowerCaseIndicator() {return lowerCaseIndicator;}
	public String getUpperCaseIndicator() {return upperCaseIndicator;}
	public String getCapsIndicator() {return capsIndicator;}
	public String getNumbersIndicator() {return numbersIndicator;}
	public String getSymbolsIndicator() {return symbolsIndicator;}


	enum Section {

		METADATA("metadata", NA), LOWER_CASE("lowercase", 0), UPPER_CASE(
				"uppercase", 64), CAPS_LOCK("capslock", 128), NUMBERS(
				"numbers", 192), SYMBOLS("symbols", 256); //, EMOJIS("emojis", 794);

		// extra for numbers 513 - 527 (15 pc.) (0x201)
		// extra for symbols 528 - 542 (15 pc.) (0x210)

		/*
		 * AUXSET_LOWER_CASE( "auxset lower case", 320), AUXSET_UPPER_CASE(
		 * "auxset upper case", 384), AUXSET_CAPS_LOCK("auxset caps lock", 448),
		 * EXTRA("extra", 513);
		 *
		 */

		private final String header;
		private final int charOffset;

		Section(String header, int charOffset) {
			this.header = header;
			this.charOffset = charOffset;
		}

		public String getHeader() {
			return this.header;
		}

		public int getCharOffset() {
			return charOffset;
		}
	}

		// ============== For moving legacy JSON strings to Maps: ===================
			// ===== legacy json layout means those before format 10.0 =====

	private static final int lastMapIndex = 242+1; // +1 = abc/Abc/ABC...
	private static final int[] mapToPrimary = { 0, // index/chord of single buttons
		// This same map is for lowercase, uppercase, caps and emojis, 0 = do not load this item, leave as is
		//  a  o  u  s  c   d  BS   l SP  M    i  t  e   n  r
			1, 7, 2, 8, 3,  11, 0, 12, 0, 35,  4, 9, 5, 10, 6, // 15 // nothing pressed (1 + 15 in fact)
			// Press on left column, on rows 1 to 5:
			0, 0, 0, 0, 0,  29, 0, 0, 0, 0,   0, 19, 0, 22, 0,    // 30 // (left 27 =>31) first key on line pressed
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,   13, 38, 14, 37, 15,  // 45 // second key on line pressed
			0, 0, 0, 0, 0,  32, 0, 21, 0, 0,   0, 20, 0, 23, 0,     // 60 // third... etc.
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,   16, 36, 17, 39, 18, // 75
			0, 0, 0, 0, 0,  25, 0, 0, 0, 0,    0, 21, 0, 24, 0,  // 90
			// press on center column:
			//31, 0, 0, 0, 0,  	0, 0, 0, 0, 0,  25, 26, 27, 28, 29,  // 105 (left 27 => 31)
			29, 0, 0, 0, 25,  	0, 0, 0, 0, 0,  25, 26, 27, 28, 29,  // 105 (left 27 => 31)
			0, 0, 0, 0, 0,  	0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 120
			//0, 0, 21, 0, 0,	0, 0, 0, 0, 0,  30, 31, 32, 33, 34,  // 135 // x y z
			0, 0, 21, 0, 32,	0, 0, 0, 0, 0,  30, 31, 32, 33, 34,  // 135 // x y z
			0, 0, 0, 0, 0,  	0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 150
			0, 0, 0, 0, 0, 		0, 0, 0, 0, 0,  0, 0, 0, 0, 0,   // 165
			// Press on Right column
			0, 13, 0, 16, 0,  	25, 0, 30, 0, 0,  0, 0, 0, 0, 0,   // 180
			19, 38, 20, 36, 21, 26, 0, 31, 0, 0,  0, 0, 0, 0, 0,   // 195
			0, 14, 0, 17, 0,  	27, 0, 32, 0, 0,  0, 0, 0, 0, 0,   // 210
			22, 37, 23, 39, 24, 28, 0, 33, 0, 0,  0, 0, 0, 0, 0,   // 235
			0, 15, 0, 18, 0,  	29, 0, 34, 0, 0,  0, 0, 0, 0, 0,   // 1 + 16 x 15 = 241

			40, 0, 0, 0, 0 // abc + margin to be on safe side (at least one extra required)

	};

	private static final int[] mapToPrimNum = { 0, // index/chord of single buttons
			// This is for Numbers and Symbols, 0 = do not load this
			1, 7, 2, 8, 3,  11, 0, 12, 0, 35,  4, 9, 5, 22, 6, // 15 a o u s c   d BS l SP M

			0, 0, 0, 0, 0,  0, 0, 19, 0, 0,  0, 19, 0, 0, 0, // 30
			0, 0, 0, 0, 0,  0, 0, 31, 0, 0,  13, 38, 14, 0, 15,  // 45
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 20, 0, 0, 0,  // 60
			0, 0, 0, 0, 0,  0, 0, 33, 0, 0,  16, 36, 17, 39, 18, // 75
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 21, 0, 0, 0,  // 90

			//30, 0, 0, 0, 0,    	0, 0, 0, 0, 0,  25, 26, 0, 28, 0,  // 105
			30, 0, 0, 0, 0,    	0, 0, 0, 0, 0,  25, 0, 0, 28, 0,  // 105
			0, 0, 0, 0, 0,    	0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 120
			19, 31, 34, 33, 32, 0, 0, 0, 0, 0,  13, 0, 29, 0, 27,  // 135 // x y z
			0, 0, 0, 0, 0,    	0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 150
			0, 0, 0, 0, 0,    	0, 0, 0, 0, 0,  0, 0, 0, 0, 0,   // 165

			0, 13, 0, 16, 0,  	 0, 0, 13, 0, 0,  0, 0, 0, 0, 0,   // 180
			19, 38, 20, 36, 21,  0, 0, 0, 0, 0,   0, 0, 0, 0, 0,// 195
			0, 14, 0, 17, 0,     27, 0, 29, 0, 0,   0, 0, 0, 0, 0,   // 210
			23, 37, 10, 39, 24,   28, 0, 0, 0, 0,   0, 0, 0, 0, 0,   // 235
			0, 15, 0, 18, 0,  	 0, 26, 0, 0, 0,   0, 0, 0, 0, 0,   // 240 + 1

			40, 0, 0, 0, 0 // abc + margin

	};

	private static final int[] mapToPunctuation = {0, // index/chord of single buttons
			// This is for punctuation of all sets, 0 = do not load this
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0, // 15 a o u s c   d BS l SP M

			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 1, 0, 6, // 30
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 45
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  4, 0, 0, 0, 2,  // 60
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0, // 75
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  3, 0, 5, 0, 0,  // 6 x 15 = 90

			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 105
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 120
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 135
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  // 150
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0,   // 11 x 15 = 65

			0, 0, 4, 0, 3,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0,   // 180
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0,   // 195
			1, 0, 0, 0, 5,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0,   // 210
			0, 0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0,   // 235
			6, 0, 2, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0, 0,   // 1 + 16 x 15 = 241

			0, 0, 0, 0, 0 // margin
	};

	private static final int[] mapLegacyExtra = { // KeyM, map rows 1, 3, 5, 2, 4  to columns 1, 2, 3
			3, 6, 11,   1, 8, 14,   5, 10, 15,   2, 7, 12,   4, 9, 13 // modified # @ & / locations
			//1, 6, 11,   3, 8, 13,   5, 10, 15,   2, 7, 12,   4, 9, 14 // one-to-one original
	};
	// For Devanagari, keep the original
	private static final int[] mapLegacyExtraDev = { // KeyM, map rows 1, 3, 5, 2, 4  to columns 1, 2, 3
			1, 6, 11,   3, 8, 13,   5, 10, 15,   2, 7, 12,   4, 9, 14 // one-to-one original
	};
	// ==================== end of legacy json mappings ======================================



	public Layout getLayout(Context context, String asset) { // main
		InputStream inputStream;
		try {
			inputStream = context.getAssets().open(asset);
			return getLayout(context, loadFileContents(inputStream), asset);
		} catch (IOException e) {
			Log.e("CMBO", "Unable to open asset (main) " + asset, e);
			//return null;
		}
		return null;
	}
	public Layout getLayout2(Context context, String asset) { // aux
		InputStream inputStream;
		try {
			inputStream = context.getAssets().open(asset);
			return getLayout2(context, loadFileContents(inputStream), asset);
		} catch (IOException e) {
			Log.e("CMBO", "Unable to open asset (aux) " + asset, e);
			//return null;
		}
		return null;
	}





	public Layout getLayout(Context context, File externalFile) { // main
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(externalFile);
			return getLayout(context, loadFileContents(inputStream),
					externalFile.getName());

		} catch (IOException e) {
			Log.e("CMBO",
					"Unable to open file (main) " + externalFile.getAbsolutePath(), e);
		}
		return null;
	}
	public Layout getLayout2(Context context, File externalFile) { // aux
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(externalFile);
			return getLayout2(context, loadFileContents(inputStream),
					externalFile.getName());
		} catch (IOException e) {
			Log.e("CMBO",
					"Unable to open file (aux) " + externalFile.getAbsolutePath(), e);
		}
		return null;
	}


	public Layout getLayout(Context context, String contents, String fileName) { // main

		Log.e("CMBO", "Start parsing main layout file " + fileName);

		try {
			JSONObject jsonLayout = new JSONObject(contents);
			if (validate(jsonLayout)) {
				Layout layout = parse(jsonLayout, fileName);
				// TODO more validation?
				return layout;
			}
		} catch (JSONException e) {
			Log.e("CMBO", "Error while parsing layout file (main) " + fileName + " - " + e);
		}

		return null;
	}

	public Layout getLayout2(Context context, String contents, String fileName) { // aux

		Log.e("CMBO", "Start parsing aux layout file " + fileName);

		try {
			JSONObject jsonLayout = new JSONObject(contents);
			if (validate(jsonLayout)) {
				Layout layout2 = parse2(jsonLayout, fileName);
				// TODO more validation?
				return layout2;
			}
		} catch (JSONException e) {
			Log.e("CMBO", "Error while parsing layout file (aux) " + fileName + " - " + e);
		}

		return null;
	}


	private Layout parse(JSONObject jsonLayout, String fileName) // main language set
			throws JSONException {

		Layout layout = new Layout();

				langOffset = 0; // 320 or 0 depending wheter loading aux lang set or main lang set
				langExtraOffset = 0; // 60 or 0 depending whether loading aux lang set or main lang set

				//String[] keyLayout = LayoutFactory.getBase();

				String[] mapLowerCase = LayoutFactory.getMapLowerCase();
				String[] mapUpperCase = LayoutFactory.getMapUpperCase();
				String[] mapCapsLock = LayoutFactory.getMapCapsLock();
				String[] mapNumbers = LayoutFactory.getMapNumbers();
				String[] mapSymbols = LayoutFactory.getMapSymbols();
				String[] mapEmojis = LayoutFactory.getMapEmojis();
				String[] mapFn = LayoutFactory.getMapMore();
				// for getting some items from LayoutFactory:
				String[] mapLowerCasePart = LayoutFactory.getMapLowerCase();
				String[] mapNumbersPart = LayoutFactory.getMapNumbers();

				String layoutFormat = ""; double layoutFormatValue = 0;

		//layout.setLayout(keyLayout);
		layout.setFileName(fileName);

		layout.setMapLowerCase(mapLowerCase);
		layout.setMapUpperCase(mapUpperCase);
		layout.setMapCapsLock(mapCapsLock);
		layout.setMapNumbers(mapNumbers);
		layout.setMapSymbols(mapSymbols);
		layout.setMapEmojis(mapEmojis);
		layout.setMapFn(mapFn);


		for (Section section : Section.values()) {
			Log.d("CMBO", "Parsing section " + section.getHeader() + ". JSON File = " + fileName);

			if (section == Section.METADATA) {
				JSONObject metadata = jsonLayout.getJSONObject(section
						.getHeader());
				// TODO proper error handling for invalid data
				for (Iterator<String> keys = (Iterator<String>) metadata.keys(); keys
						.hasNext();) {
					String key = keys.next();
					layout.setValue(key, metadata.getString(key));
					// Log.d("CMBO", "Parsing section Metadata: " + key + " = " + metadata.getString(key));

					if (key.equals("format")) {

						layoutFormat = metadata.getString(key);
						//layoutFormatValue = Double.valueOf(layoutFormat);
						layoutFormatValue = valueOf(layoutFormat);

						Log.d("CMBO", "**** (main) - Parsing section Metadata Format (layoutFormat) = "
								+ layoutFormat + ", value  " + layoutFormatValue + ". JSON File = " + fileName);
					}
				}
				continue;
			}

			/*
			if format = 4.0 or higher:
			 extra = ["-","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"] or
			 extra = ["-","dev","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]

			 ----
			 Parsing section Metadata:
			    Metadata: iso6391 = uk
                Metadata: author = Seppo Tiainen
                Metadata: extra = ["-","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]
                Metadata: description = Ukrainian
                Metadata: name = Ukrainian
                Metadata: format = 5.0
                Metadata: group = Optimized
                Metadata: info = The recommended optimized CMBO keyboard layout for Ukrainian - 2015-04-27
                Metadata:  version = 1.2

			*/

			JSONObject set = jsonLayout.getJSONObject(section.getHeader());

			// ----
			int lastPrimaryIndex = 0; // depending of format, there are more characters in the json file
			int lastPunctuationIndex = 0; // depending of format, there are more characters in the json file
			//int lastMapIndex = 241; // mapToPrimary.length // is always 256!; // 241; // 16 x 15 + 1 = 241


			if (layoutFormatValue >= 5.0) {
				lastPrimaryIndex = INDICES_PRIMARY.length;  // abc indicator text still left out (last item)
				lastPunctuationIndex = INDICES_PUNCTUATION.length - 2; // forget / and \, they already come from primary
			} else {
				lastPrimaryIndex = INDICES_PRIMARY.length - 4; //  /, \, PgUp, PgDn not present in json
				lastPunctuationIndex = INDICES_PUNCTUATION.length;
			}

			Log.d("-MAP", "*** lastPrimaryIndex     = " + lastPrimaryIndex);
			Log.d("-MAP", "*** lastPunctuationIndex = " + lastPunctuationIndex);
			Log.d("-MAP", "*** lastMapIndex         = " + lastMapIndex);
			// -----

			//if ((section != Section.NUMBERS) && (section != Section.SYMBOLS) && (section != Section.EMOJIS)) {
			if ((section != Section.NUMBERS) && (section != Section.SYMBOLS)) {

				//JSONObject set = jsonLayout.getJSONObject(section.getHeader());
				JSONArray primary = new JSONArray("[{}]");
				JSONArray punctuation = new JSONArray("[{}]");
				JSONArray map = new JSONArray("[{}]");

				if (layoutFormatValue < 10.0) { // Use code map instead of primary and punctuation

					primary = set.getJSONArray(KEY_PRIMARY);
					punctuation = set.getJSONArray(KEY_PUNCTUATION);

				}

				// ----------------------------------------

				if (section == Section.LOWER_CASE) {


					if (layoutFormatValue >= 10.0) { // Use code map instead of primary and punctuation
						map = set.getJSONArray(KEY_MAP);
						for (int index = 0; index < lastMapIndex; index++) {
							mapLowerCase[index] = map.getString(index);
						}

					} else { // legacy

						lowerCaseIndicator = primary.getString(lastPrimaryIndex);
						//Log.d("-MAP", "*** lowerCaseIndicator = " + lowerCaseIndicator);

						// copy from primary and punctuation sets to indexed map:
						for (int i = 1; i < lastMapIndex; i++) { //zxcxvbn
							//Log.d("-MAP", " i = " + i + ", pointer = " + mapToPrimary[i] + ", pointed item = " + primary.getString(mapToPrimary[i] - 1));
							if (mapToPrimary[i] > 0) { // Note: 0 value in mapToPrimary[i] means 'not defined in JSON'
								Log.d("-MAP", "*** Parsing LowerCase Map from JSON primary set. String " + i + " = " + primary.getString(mapToPrimary[i] - 1));
								mapLowerCase[i] = primary.getString(mapToPrimary[i] - 1); // first item = 0, i = 1
							} else if ((mapToPunctuation[i] > 0) && (mapToPunctuation[i] - 1 < lastPunctuationIndex)) { // part comes from punctuation
								Log.d("-MAP", "*** Parsing LowerCase Map from JSON Punctuation set. String " + i + " = " + punctuation.getString(mapToPunctuation[i] - 1));
								mapLowerCase[i] = punctuation.getString(mapToPunctuation[i] - 1);
								// first item = 0, i2 = 1
							}
						}
					}
					// above, the items are taken from from JSON keymaps
					// 106-120 = BS pressed, get these from LayoutFactory (functions)
					// 136-150 = SP pressed, get these from LayoutFactory (navigation)
					// String[] mapLowerCase = LayoutFactory.getMapLowerCase();
					for (int kp = 106; kp < 121; kp++) {
						mapLowerCase[kp] = mapLowerCasePart[kp];
						mapLowerCase[kp + 30] = mapLowerCasePart[kp + 30];
					}

					layout.setMapLowerCase(mapLowerCase); // save map layout


				}



				// ---------------------------------------

				if (section == Section.UPPER_CASE) {


					if (layoutFormatValue >= 10.0) { // Use code map instead of primary and punctuation
						map = set.getJSONArray(KEY_MAP);
						for (int index = 0; index < lastMapIndex; index++) {
							mapUpperCase[index] = map.getString(index);
						}

					} else { // legacy

						upperCaseIndicator = primary.getString(lastPrimaryIndex);

						// copy from primary and punctuation sets to indexed map:
						for (int n = 1; n < lastMapIndex - 1; n++) { //zxcxvbn
							if (mapToPrimary[n] > 0) { // Note: 0 value in mapToPrimary[i] means 'not defined in JSON'
								Log.d("-MAP", "*** Parsing UpperCase Map from JSON primary set. String " + n + " = " + primary.getString(mapToPrimary[n] - 1));
								mapUpperCase[n] = primary.getString(mapToPrimary[n] - 1); // first item = 0, i = 1
							} else if ((mapToPunctuation[n] > 0) && (mapToPunctuation[n] - 1 < lastPunctuationIndex)) { // part comes from punctuation
								Log.d("-MAP", "*** Parsing UpperCase Map from JSON Punctuation set. String " + n + " = " + punctuation.getString(mapToPunctuation[n] - 1));
								mapUpperCase[n] = punctuation.getString(mapToPunctuation[n] - 1); // first item = 0, i2 = 1
							}
						}
					}
					// above, the items are taken from from JSON keymaps
					// 106-120 = BS pressed, get these from LayoutFactory (functions)
					// 136-150 = SP pressed, get these from LayoutFactory (navigation)
					// String[] mapLowerCase = LayoutFactory.getMapLowerCase();
					for (int kp = 106; kp < 121; kp++) {
						mapUpperCase[kp] = mapLowerCasePart[kp];
						mapUpperCase[kp + 30] = mapLowerCasePart[kp + 30];
					}

					layout.setMapUpperCase(mapUpperCase); // save map layout

				}

				// ---------------------

				if (section == Section.CAPS_LOCK) {

					if (layoutFormatValue >= 10.0) { // Use code map instead of primary and punctuation
						map = set.getJSONArray(KEY_MAP);
						for (int index = 0; index < lastMapIndex; index++) {
							mapCapsLock[index] = map.getString(index);
						}

					} else { // legacy

						capsIndicator = primary.getString(lastPrimaryIndex);

						// copy from primary and punctuation sets to indexed map:
						for (int m = 1; m < lastMapIndex - 1; m++) { //zxcxvbn
							if (mapToPrimary[m] > 0) { // Note: 0 value in mapToPrimary[i] means 'not defined in JSON'
								Log.d("-MAP", "*** Parsing CapsLock Map from JSON primary set. String " + m + " = " + primary.getString(mapToPrimary[m] - 1));
								mapCapsLock[m] = primary.getString(mapToPrimary[m] - 1); // first item = 0, i = 1
							} else if ((mapToPunctuation[m] > 0) && (mapToPunctuation[m] - 1 < lastPunctuationIndex)) { // part comes from punctuation
								Log.d("-MAP", "*** Parsing CapsLock Map from JSON Punctuation set. String " + m + " = " + punctuation.getString(mapToPunctuation[m] - 1));
								mapCapsLock[m] = punctuation.getString(mapToPunctuation[m] - 1); // first item = 0, i2 = 1
							}
						}

					}
					// above, the items are taken from from JSON keymaps
					// 106-120 = BS pressed, get these from LayoutFactory (functions)
					// 136-150 = SP pressed, get these from LayoutFactory (navigation)
					// String[] mapLowerCase = LayoutFactory.getMapLowerCase();
					for (int kp = 106; kp < 121; kp++) {
						mapCapsLock[kp] = mapLowerCasePart[kp];
						mapCapsLock[kp + 30] = mapLowerCasePart[kp + 30];
					}

					layout.setMapCapsLock(mapCapsLock); // save map layout
				}

				//if (section == Section.SYMBOLS) symbolsIndicator = primary.getString(lastPrimaryIndex);
				//if (section == Section.NUMBERS) numbersIndicator = primary.getString(lastPrimaryIndex);



			}

			// ----------------------------

			if (section == Section.NUMBERS) { // Only numbers and symbols have extra! (Key M as state)


				JSONArray primary = new JSONArray("[{}]");
				JSONArray punctuation = new JSONArray("[{}]");
				JSONArray map = new JSONArray("[{}]");

				if (layoutFormatValue >= 10.0) { // Use map instaad of primary and punctuation
					map = set.getJSONArray(KEY_MAP);
					for (int index = 0; index < lastMapIndex; index++) {
						mapNumbers[index] = map.getString(index);
					}



				} else { // legacy:

					primary = set.getJSONArray(KEY_PRIMARY);
					punctuation = set.getJSONArray(KEY_PUNCTUATION);


					if ((layoutFormatValue >= 3.0) && (layoutFormatValue < 10)) {
					//if (layoutFormatValue >= 3.0) { // use native emojis for map also!
						JSONArray extra = set.getJSONArray(KEY_EXTRA);
						Log.d("CMBO", "**** - Parsing NUMBERS EXTRA."); // native JSON emojis
					}


					numbersIndicator = primary.getString(lastPrimaryIndex);

					// copy from primary and punctuation sets to indexed map:
					for (int k = 1; k < lastMapIndex - 1; k++) { //zxcxvbn
						if (mapToPrimNum[k] > 0) { // Note: 0 value in mapToPrimary[i] means 'not defined in JSON'
							Log.d("-MAP", "*** Parsing Numbers Map from JSON primary set. String " + k + " = " + primary.getString(mapToPrimNum[k] - 1) + ". Map from " + (mapToPrimNum[k] - 1));
							mapNumbers[k] = primary.getString(mapToPrimNum[k] - 1); // first item = 0, i = 1
						} else if ((mapToPunctuation[k] > 0) && (mapToPunctuation[k] - 1 < lastPunctuationIndex)) { // part comes from punctuation
							Log.d("-MAP", "*** Parsing Numbers Map from JSON Punctuation set. String " + k + " = " + punctuation.getString(mapToPunctuation[k] - 1));
							mapNumbers[k] = punctuation.getString(mapToPunctuation[k] - 1); // first item = 0, i2 = 1
						}
					}

				}
				// above, the items are taken from from JSON keymaps
				// 106-120 = BS pressed, get these from LayoutFactory (functions)
				// 136-150 = SP pressed, get these from LayoutFactory (navigation)
				// String[] mapNumbers = LayoutFactory.getMapNumbers();
				for (int kn = 106; kn < 121; kn++) {
					mapNumbers[kn] = mapNumbersPart[kn];
					mapNumbers[kn + 30] = mapNumbersPart[kn + 30];
				}

				layout.setMapNumbers(mapNumbers); // save map layout
			}

			// ------------------------

			if (section == Section.SYMBOLS) { // Only numbers and symbols have extra! (Key M as state)

				JSONArray primary = new JSONArray("[{}]");
				JSONArray punctuation = new JSONArray("[{}]");
				JSONArray map = new JSONArray("[{}]");

				if (layoutFormatValue >= 10.0) { // Use code map instead of primary and punctuation
					map = set.getJSONArray(KEY_MAP);
					for (int index = 0; index < lastMapIndex; index++) {
						mapSymbols[index] = map.getString(index);
					}

				} else { // legacy

					primary = set.getJSONArray(KEY_PRIMARY);
					punctuation = set.getJSONArray(KEY_PUNCTUATION);


					if ((layoutFormatValue >= 3.0) && (layoutFormatValue < 10)) {
						JSONArray extra = set.getJSONArray(KEY_EXTRA);

						Log.d("CMBO", "**** - Parsing SYMBOLS EXTRA.");


					}


					symbolsIndicator = primary.getString(lastPrimaryIndex);

					// copy from primary and punctuation sets to indexed map:
					for (int j = 1; j < lastMapIndex - 1; j++) { //zxcxvbn
						if (mapToPrimNum[j] > 0) { // Note: 0 value in mapToPrimary[i] means 'not defined in JSON'
							Log.d("-MAP", "*** Parsing Symbols Map from JSON primary set. String " + j + " = " + primary.getString(mapToPrimNum[j] - 1));
							mapSymbols[j] = primary.getString(mapToPrimNum[j] - 1); // first item = 0, i = 1
						} else if ((mapToPunctuation[j] > 0) && (mapToPunctuation[j] - 1 < lastPunctuationIndex)) { // part comes from punctuation
							Log.d("-MAP", "*** Parsing Symbols Map from JSON Punctuation set. String " + j + " = " + punctuation.getString(mapToPunctuation[j] - 1));
							mapSymbols[j] = punctuation.getString(mapToPunctuation[j] - 1); // first item = 0, i2 = 1
						}
					}

					if (layoutFormatValue >= 3.0) { // SYMBOLS EXTRA = legacy KeyM symbols
						//if (layoutFormatValue >= 3.0) { // use native KeyM symbols for map also!
						JSONObject symbolsLegacy = jsonLayout.getJSONObject("symbols");
						JSONArray symbolsextra = symbolsLegacy.getJSONArray(KEY_EXTRA);
						Log.d("CMBO", "**** - Parsing SYMBOLS EXTRA for map KeyM (Symbols/Extra)."); // native JSON emojis

						JSONObject numbersLegacy = jsonLayout.getJSONObject("numbers");
						JSONArray numbersextra = numbersLegacy.getJSONArray(KEY_EXTRA);
						Log.d("CMBO", "**** - Parsing NUMBERS EXTRA for map emojis (Numberss/Extra)."); // native JSON emojis

						JSONObject metadata = jsonLayout.getJSONObject("metadata");
						JSONArray metadataextra = metadata.getJSONArray("extra");


						if (metadataextra.getString(1).equals("dev") ) { // devanagari

							// Devanagari, keep order of Key.M symbols unchanged
							for (int index3 = 0; index3 < INDICES_EXTRA.length; index3++) {
								// Map same KeyM symbols to all sets except Emojis (the Legacy way)
								mapLowerCase[150 + mapLegacyExtraDev[index3]] = symbolsextra.getString(index3);
								mapUpperCase[150 + mapLegacyExtraDev[index3]] = symbolsextra.getString(index3);
								mapCapsLock[150 + mapLegacyExtraDev[index3]] = symbolsextra.getString(index3);
								mapNumbers[150 + mapLegacyExtraDev[index3]] = symbolsextra.getString(index3);
								// For symbols set, trust LayoutFactory
								// mapSymbols[150 + mapLegacyExtra[index3]] = symbolsextra.getString(index3);
								// The lines commented use default symbols from LayoutFactory.java
							}

						} else { // non-devanagari

							// For non-devanagari, change the old order of Key.M symbols: & / # @
							for (int index3 = 0; index3 < INDICES_EXTRA.length; index3++) {
								// Map same KeyM symbols to all sets except Emojis (the Legacy way)
								mapLowerCase[150 + mapLegacyExtra[index3]] = symbolsextra.getString(index3);
								mapUpperCase[150 + mapLegacyExtra[index3]] = symbolsextra.getString(index3);
								mapCapsLock[150 + mapLegacyExtra[index3]] = symbolsextra.getString(index3);
								mapNumbers[150 + mapLegacyExtra[index3]] = symbolsextra.getString(index3);
								// For symbols set, trust LayoutFactory
								// mapSymbols[150 + mapLegacyExtra[index3]] = symbolsextra.getString(index3);
								// The lines commented use default symbols from LayoutFactory.java
							}

							//for (int index4 = 0; index4 < INDICES_EXTRA.length; index4++) {
							//	Log.d("-LAYOUT", symbolsextra.getString(index4));
							//}

						}
					}


				} //end of legacy

				// above, the items are taken from from JSON keymaps
				// 106-120 = BS pressed, get these from LayoutFactory (functions)
				// 136-150 = SP pressed, get these from LayoutFactory (navigation)
				// String[] mapLowerCase = LayoutFactory.getMapLowerCase();
				for (int kp = 106; kp < 121; kp++) {
					mapSymbols[kp] = mapLowerCasePart[kp];
					mapSymbols[kp + 30] = mapLowerCasePart[kp + 30];
				}

				layout.setMapSymbols(mapSymbols); // save map layout

			}



		} // section by section end, main language


				// ------------------------ EMOJIS --------------------------

				if (jsonLayout.has("emojis")) { // EMOJIS set map found in JSON?
				//if (layoutFormatValue >= 10.0) { // EMOJIS from JSON key map

					Log.d("CMBO", "**** - Parsing EMOJIS map (format >= 10).");

					JSONObject emojisMain = jsonLayout.getJSONObject("emojis");
					JSONArray map = emojisMain.getJSONArray("map");

					for (int index = 0; index < lastMapIndex; index++) { // 0...241
						mapEmojis[index] = map.getString(index);
						Log.d("-MAP", "Emoji map item " + index + " = " + mapEmojis[index]);
					}

					layout.setMapEmojis(mapEmojis); // save map layout

				}  else { // emojis from Layoutfactory

					if (layoutFormatValue >= 3.0) { // EMOJIS EXTRA (native emojis) from NUMBERS SET
					//if (layoutFormatValue >= 3.0 && layoutFormatValue < 10.0) { // use native emojis for map also!
						JSONObject emojisLegacy = jsonLayout.getJSONObject("numbers");
						JSONArray extra = emojisLegacy.getJSONArray(KEY_EXTRA);
						Log.d("CMBO", "**** - Parsing NUMBERS EXTRA for map (Numbers/Emojis)."); // native JSON emojis

						for (int index2 = 0; index2 < INDICES_EXTRA.length; index2++) {
							mapEmojis[150 + mapLegacyExtraDev[index2]] = extra.getString(index2);
						}


						layout.setMapEmojis(mapEmojis); // save map layout
					}
				}



				// ---------------------------------------------------------------------------------

				return layout;

	}

	//private static String[] mapLegacyExtra = {151, };

	private Layout parse2(JSONObject jsonLayout, String fileName) // aux language set
			throws JSONException {

		Layout layout2 = new Layout();

				langOffset = 320; // 320 or 0 depending wheter loading aux lang set or main lang set


		String layoutFormat = ""; double layoutFormatValue = 0;

		//layout2.setLayout(keyLayout);
		layout2.setFileName(fileName);

		for (Section section : Section.values()) {
			Log.d("CMBO", "Parsing section " + section.getHeader());

			if (section == Section.METADATA) {
				JSONObject metadata = jsonLayout.getJSONObject(section
						.getHeader());
				// TODO proper error handling for invalid data
				for (Iterator<String> keys = (Iterator<String>) metadata.keys(); keys
						.hasNext();) {
					String key = keys.next();
					layout2.setValue(key, metadata.getString(key));
					// Log.d("CMBO", "Parsing section Metadata: " + key + " = " + metadata.getString(key));

					if (key.equals("format")) {

						layoutFormat = metadata.getString(key);
						//layoutFormatValue = Double.valueOf(layoutFormat);
						layoutFormatValue = valueOf(layoutFormat);

						Log.d("CMBO", "**** (aux) - Parsing section Metadata2 Format (layoutFormat) = "
								+ layoutFormat + " => " + layoutFormatValue);
					}
				}
				continue;
			}

			/*
			if format = 4.0 or higher:
			 extra = ["-","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"] or
			 extra = ["-","dev","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]

			 ----
			 Parsing section Metadata:
			    Metadata: iso6391 = uk
                Metadata: author = Jack Titmouse
                Metadata: extra = ["-","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]
                Metadata: description = Ukrainian
                Metadata: name = Ukrainian
                Metadata: format = 5.0
                Metadata: group = Optimized
                Metadata: info = The recommended optimized ComboKey keyboard layout for Ukrainian - 2015-04-27
                Metadata:  version = 1.2

			*/

			JSONObject set = jsonLayout.getJSONObject(section.getHeader());


			//if ((section != Section.NUMBERS) && (section != Section.SYMBOLS) && (section != Section.EMOJIS)) {
			if ((section != Section.NUMBERS) && (section != Section.SYMBOLS)) {

				//JSONObject set = jsonLayout.getJSONObject(section.getHeader());
				JSONArray primary = set.getJSONArray(KEY_PRIMARY);
				JSONArray punctuation = set.getJSONArray(KEY_PUNCTUATION);

			}


			if (section == Section.NUMBERS) { // Only numbers and symbols have extra! (Key M as state)

				JSONArray primary = set.getJSONArray(KEY_PRIMARY);
				JSONArray punctuation = set.getJSONArray(KEY_PUNCTUATION);
				if ((layoutFormatValue >= 3.0) && (layoutFormatValue < 10)) {
					JSONArray extra = set.getJSONArray(KEY_EXTRA);
					Log.d("CMBO", "Parsing NUMBERS EXTRA.");

				}

			}

			if (section == Section.SYMBOLS) { // Only numbers and symbols have extra! (Key M as state)

				JSONArray primary = set.getJSONArray(KEY_PRIMARY);
				JSONArray punctuation = set.getJSONArray(KEY_PUNCTUATION);

				if ((layoutFormatValue >= 3.0) && (layoutFormatValue < 10)) {
					JSONArray extra = set.getJSONArray(KEY_EXTRA);
					Log.d("CMBO", "**** - Parsing SYMBOLS EXTRA.");

				}


			}


		} // section for section end, aux language


		return layout2;
	}



		private boolean validate(JSONObject layout) throws JSONException {

		boolean valid = true;

		return valid;
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

}
