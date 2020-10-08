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

import java.util.HashMap;
import java.util.Map;

public class Layout {

	private String[] layout; // main language character set
	private String[] layout2; // aux language character set

	private String fileName;
	private String fileName2;

	private String[] mapLowerCase;
	private String[] mapUpperCase;
	private String[] mapCapsLock;
	private String[] mapNumbers;
	private String[] mapSymbols;
	private String[] mapEmojis;
	private String[] mapFn;

	private String[] mapLowerCase2; // These may not be needed finally, aux can be loaded to same string array
	private String[] mapUpperCase2;
	private String[] mapCapsLock2;
	private String[] mapNumbers2;
	private String[] mapSymbols2;
	private String[] mapEmojis2;
	private String[] mapFn2;

	private String abcIndicator;
	private String AbcIndicator;
	private String ABCIndicator;
	private String numIndicator;
	private String symbIndicator;



	private final Map<String, String> metadata = new HashMap<String, String>();
	private final Map<String, String> metadata2 = new HashMap<String, String>();

	public enum Metadata {
		LAYOUT_FORMAT("format"), ISO_639_1("iso6391"), NAME("name"), DESCRIPTION(
				"description"), LAYOUT_VERSION("version"), AUTHOR("author"), FILENAME(
				"file name"), GROUP("group"), LAYOUT_EXTRA("extra");
		private final String key;

		Metadata(String key) {
			this.key = key;
		}

		public String getKey() {
			return this.key;
		}
	}


	public enum Metadata2 {
		LAYOUT_FORMAT("format"), ISO_639_1("iso6391"), NAME("name"), DESCRIPTION(
				"description"), LAYOUT_VERSION("version"), AUTHOR("author"), FILENAME(
				"file name"), GROUP("group"), LAYOUT_EXTRA("extra");
		private final String key;

		Metadata2(String key) {
			this.key = key;
		}

		public String getKey() {
			return this.key;
		}
	}

	//  key + " = " + metadata.getString(key):

	// extra = ["-","dev","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]
	// format = 4.0

	//  - 'format' needs to be 4.0 or higher for 'extra' to exist
	// format 10 up:
	// extra = ["-","-","en","US","-","-","-","-","-","-","-","-","-","-","-","-","-"]


	public String toString() { return this.getValue(Metadata.NAME)+"_"+this.getValue(Metadata.GROUP); }
	public String toString2() { return this.getValue2(Metadata2.NAME)+"_"+this.getValue2(Metadata2.GROUP); }

	public void setValue(Metadata key, String value) {
		this.metadata.put(key.getKey(), value);
	}
	public void setValue2(Metadata key, String value) {
		this.metadata2.put(key.getKey(), value);
	}

	public void setValue(String key, String value) {
		this.metadata.put(key, value);
	}
	public void setValue2(String key, String value) {
		this.metadata2.put(key, value);
	}

	public String getValue(Metadata key) {
		return this.metadata.get(key.getKey());
	}
	public String getValue2(Metadata2 key) {
		return this.metadata2.get(key.getKey());
	}

	public String[] getLayout() {
		return layout;
	}
	public String[] getLayout2() {
		return layout2;
	}

	public void setLayout(String[] layout) {
		this.layout = layout;
	}
	public void setLayout2(String[] layout) {
		this.layout2 = layout;
	}

	public String getFileName() {
		return fileName;
	}
	public String getFileName2() {
		return fileName2;
	}

	public String[] getMapLowerCase() {
		return mapLowerCase;
	}
	public String[] getMapUpperCase() {
		return mapUpperCase;
	}
	public String[] getMapCapsLock() {
		return mapCapsLock;
	}
	public String[] getMapNumbers() {
		return mapNumbers;
	}
	public String[] getMapSymbols() {
		return mapSymbols;
	}
	public String[] getMapEmojis() {
		return mapEmojis;
	}
	public String[] getMapMore() {
		return mapFn;
	}

	public String[] getMapLowerCase2() {
		return mapLowerCase2;
	}
	public String[] getMapUpperCase2() {
		return mapUpperCase2;
	}
	public String[] getMapCapsLock2() {
		return mapCapsLock2;
	}
	public String[] getMapNumbers2() {
		return mapNumbers2;
	}
	public String[] getMapSymbols2() {
		return mapSymbols2;
	}
	public String[] getMapEmojis2() {
		return mapEmojis2;
	}
	public String[] getMapMore2() {
		return mapFn2;
	}


	public void setMapLowerCase(String[] mapLowerCase) {
		this.mapLowerCase = mapLowerCase;
	}
	public void setMapUpperCase(String[] mapUpperCase) {
		this.mapUpperCase = mapUpperCase;
	}
	public void setMapCapsLock(String[] mapCapsLock) {
		this.mapCapsLock = mapCapsLock;
	}
	public void setMapNumbers(String[] mapNumbers) {
		this.mapNumbers = mapNumbers;
	}
	public void setMapSymbols(String[] mapSymbols) {
		this.mapSymbols = mapSymbols;
	}
	public void setMapEmojis(String[] mapEmojis) {
		this.mapEmojis = mapEmojis;
	}
	public void setMapFn(String[] mapFn) {
		this.mapFn = mapFn;
	}

	public void changeMapItem(int item, String string) { // e.g. to swap "_Play" and "_Stop"
		this.mapLowerCase[item] = string;
		this.mapUpperCase[item] = string;
		this.mapCapsLock[item] = string;
		this.mapNumbers[item] = string;
		this.mapSymbols[item] = string;
		this.mapFn[item] = string;
	}

	public void setMapLowerCase2(String[] mapLowerCase2) {
		this.mapLowerCase2 = mapLowerCase2;
	}
	public void setMapUpperCase2(String[] mapUpperCase2) {
		this.mapUpperCase2 = mapUpperCase2;
	}
	public void setMapCapsLock2(String[] mapCapsLock2) {
		this.mapCapsLock2 = mapCapsLock2;
	}
	public void setMapNumbers2(String[] mapNumbers2) {
		this.mapNumbers2 = mapNumbers2;
	}
	public void setMapSymbols2(String[] mapSymbols2) {
		this.mapSymbols2 = mapSymbols2;
	}
	public void setMapEmojis2(String[] mapEmojis2) {
		this.mapEmojis2 = mapEmojis2;
	}
	public void setMapFn2(String[] mapFn2) {
		this.mapFn2 = mapFn2;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void setFileName2(String fileName) {
		this.fileName2 = fileName;
	}

}
