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

public class CMBOCombiner {
	
	public static final String ACCENTS = "`´~¨ˆˇ";

	public static String getCombinedCharacter(String c, String accent) {
		boolean isUpperCase = !c.equals(c.toLowerCase());

		// two accents in a row or '_Del' or alike followed by accent
		if (isAccent(c) || (c.length() != 1)) return accent;

		String combined = c;

		c = c.toLowerCase();

		if ("ˆ".equals(accent)) {
			if ("a".equals(c))
				combined = "â";
			else if ("e".equals(c))
				combined = "ê";
			else if ("i".equals(c))
				combined = "î";
			else if ("o".equals(c))
				combined = "ô";
			else if ("u".equals(c))
				combined = "û";
			else if (" ".equals(c))
				combined = "ˆ";
		}

		else if ("ˇ".equals(accent)) {
			if ("a".equals(c))
				combined = "ǎ";
			else if ("c".equals(c))
				combined = "č";
			else if ("d".equals(c))
				combined = "ď";
			else if ("e".equals(c))
				combined = "ě";
			else if ("i".equals(c))
				combined = "ǐ";
			else if ("o".equals(c))
				combined = "ǒ";
			else if ("n".equals(c))
				combined = "ň";
			else if ("r".equals(c))
				combined = "ř";
			else if ("u".equals(c))
				combined = "ǔ";
			else if ("s".equals(c))
				combined = "š";
			else if ("t".equals(c))
				combined = "ť";
			else if ("z".equals(c))
				combined = "ž";
			else if (" ".equals(c))
				combined = "ˇ";

		}

		else if ("`".equals(accent)) {
			if ("a".equals(c))
				combined = "à";
			else if ("e".equals(c))
				combined = "è";
			else if ("i".equals(c))
				combined = "ì";
			else if ("o".equals(c))
				combined = "ò";
			else if ("u".equals(c))
				combined = "ù";
			else if (" ".equals(c))
				combined = "`";
		}

		else if ("´".equals(accent)) {
			if ("a".equals(c))
				combined = "á";
			else if ("e".equals(c))
				combined = "é";
			else if ("i".equals(c))
				combined = "í";
			else if ("o".equals(c))
				combined = "ó";
			else if ("u".equals(c))
				combined = "ú";
			else if ("r".equals(c))
				combined = "ŕ";
			else if ("y".equals(c))
				combined = "ý";
			else if (" ".equals(c))
				combined = "´";
		}
		else if ("~".equals(accent)) {
			if ("a".equals(c))
				combined = "ã";
			else if ("e".equals(c))
				combined = "ẽ";
			else if ("i".equals(c))
				combined = "ĩ";
			else if ("o".equals(c))
				combined = "õ";
			else if ("u".equals(c))
				combined = "ũ";
			else if ("n".equals(c))
				combined = "ñ";
			else if (" ".equals(c))
				combined = "~";
		}

		else if ("¨".equals(accent)) {
			if ("a".equals(c))
				combined = "ä";
			else if ("e".equals(c))
				combined = "ë";
			else if ("е".equals(c))
				combined = "ë";// Russian е != e								
			else if ("i".equals(c))
				combined = "ï";
			else if ("o".equals(c))
				combined = "ö";
			else if ("u".equals(c))
				combined = "ü";
			else if (" ".equals(c))
				combined = "¨";
		}

		return isUpperCase ? combined.toUpperCase() : combined;
	}

	public static boolean isAccent(String c) {
		return ACCENTS.contains(c);
	}

}
