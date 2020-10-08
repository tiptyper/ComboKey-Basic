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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.combokey.basic.layout.Layout;
import com.combokey.basic.view.CMBOKeyboardView;

import java.util.List;
import java.util.Locale;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;
import static android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
import static android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT;


public class CMBOKeyboardActivity extends Activity {

	public static String TAG = "CMBOKeyboard";

	public static final String EDITABLE_TEXT_KEY = "EDITABLE_TEXT";
	public static final String EDIT_POSITION_KEY = "EDIT_POSITION";

	private String editableText = "";
	private int position = 0; // ( = text edit position!)
	private String currentLanguage = "en"; // default lang for editText
	private String jsonLanguage = "--";

	private int layoutResource = R.layout.activity;

	private boolean xLarge = false;
	private int tabletDevice = -1; // will be either 0 (phone) or 1 (tablet)
	private boolean debugInUse = false; // just to debug with toasts

	/** Called when the activity is **first** created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d("-ACTIVITY", "onCreate()");
		Log.d("-VIEW", "ACTIVITY onCreate()");

		getWindow().setSoftInputMode(  // no effect on Galaxy A3
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // (clearFlags/setFlags/addFlags)
				//	requestWindowFeature(Window.FEATURE_NO_TITLE);

		//setContentView(selectLayoutResource()); // set view before accessing entry resource
		setContentView(R.layout.activity);

		// -------------Set Dynamic Locale---------------------
		//checkActivityLanguage(); // To be used in future when this will have an impact on spellchecking.
		//setActivityLocaleDynamically("hi"); // Test Ok. Works here.
		// ----------------------------------

		final EditText textView = (EditText) findViewById(R.id.entry);

		//if (Build.VERSION.SDK_INT >= 11)... is no more needed here
		textView.setRawInputType(TYPE_CLASS_TEXT);
		textView.setTextIsSelectable(true);

		textView.setInputType(InputType.TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_MULTI_LINE | TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		//textView.setSingleLine(false); // deprecated, caused crashes on Android 9, use line above instead

		restoreUiState();

		// The method above does not detect e.g. Huawei tablet, so we use this:
		xLarge = isXLarge();

		Log.d("-SCREEN", "(ACTIVITY) xLarge screen: " + xLarge);
		//Toast.makeText(this, "Large screen is " + xLarge, Toast.LENGTH_SHORT).show();

		// Adjust the text area bottom margin:
		int bottomMarginInDp = 100;

		if (isOrientationLandscape()) { // Landscape margin
			bottomMarginInDp = xLarge ? (getDisplayHeightInDp(this) / 2)
					: (8 + 2 * getDisplayHeightInDp(this) / 3);
		} else { // Portrait margin
			bottomMarginInDp = xLarge ? (40 + 2 * getDisplayHeightInDp(this) / 5)
					: (20 + getDisplayHeightInDp(this) / 2); // was / 2
		}

		setMarginsInDp(textView, this, 8, 8,
				8, bottomMarginInDp);

		if (Build.VERSION.SDK_INT >= 26) {
			textView.setFocusedByDefault(true); // only works on new phones
		}
		Log.d("-KBD", "onCreate()");
		// moved here 2019-01-31:
		showKeyboard();

	}

	public String getCurrentLanguageISO() { // e.g. "en" from JSON extra
		// JSON Metadata: extra = ["-","dev","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]
		// JSON Metadata: extra = ["-","-","en","US","-","-","-","-","-","-","-","-","-","-","-","-","-"]
		// languageTag = en-US
		String result = "";
		String extra = CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.LAYOUT_EXTRA);
		String[] ary = extra.split("\",\""); // Note: Not allowed to have any space between items? Just a comma?
		result = ary[2]; // "en"
		Log.i("-LOCALE", "(Activity) (getCurrentLanguageISO()) - extra = " + extra + ", result = " + result);
		return result;
	}

	private void checkActivityLanguage(){ // Momentarily not used. No spellchecking in operation.
		jsonLanguage = getCurrentLanguageISO();
		Log.i("-LOCALE", "(Activity) checkActivityLanguage() json: " + jsonLanguage + ", current: " + currentLanguage);
		if (!jsonLanguage.equals(currentLanguage)) {
			setActivityLocaleDynamically(jsonLanguage);
			currentLanguage = jsonLanguage;
		}
	}

	private void setActivityLocaleDynamically(String languageOfLocale) {
		Log.i("-LOCALE", "(Activity) setActivityLocaleDynamically("+ languageOfLocale + ")");
		// -------------Set Dynamic Locale---------------------
		//String languageOfLocale = "hi"; // language e.g. "es" "hu" "fi" "hi" "en"
		Locale locale = new Locale(languageOfLocale);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
		this.setContentView(R.layout.activity);
		// ----------------------------------
	}

	private boolean isComboKeyEnabled() {
		String packageComboKey = getPackageName();
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		try {
			List<InputMethodInfo> list = inputMethodManager.getEnabledInputMethodList();
			// Check if ComboKey is enabled as input method
			for (InputMethodInfo inputMethod : list) {
				String packageName = inputMethod.getPackageName();
				if (packageName.equals(packageComboKey)) {
					//Toast.makeText(getApplicationContext(),"ComboKey is enabled.",Toast.LENGTH_SHORT).show();
					return true; // ComboKey is enabled
				}
			}
		}
		catch (Exception e) {
			Toast.makeText(getApplicationContext(),"Input method check error: " + e,Toast.LENGTH_SHORT).show();
			return false;
		}
		return false; // default, ComboKey not enabled
	}



	private int getDisplayHeightInDp(Context context) {

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int heightPixels = metrics.heightPixels;
		int widthPixels = metrics.widthPixels;
		int densityDpi = metrics.densityDpi;
		float xdpi = metrics.xdpi;
		float ydpi = metrics.ydpi;

		Log.d("-SCREEN", "(ACTIVITY) Display size xdpi/ydpi: " + xdpi + "/" + ydpi);
		Log.d("-SCREEN", "(ACTIVITY) Display size heightPixels/widthPixels: " + heightPixels + "/" + widthPixels);
			//return (int) heightPixels;
			return (int) convertPxToDp((float) heightPixels, context);
	}


	public static void setMarginsInDp (View v, Context context,
				   int leftInDp, int topInDp, int rightInDp, int bottomInDp)
	{
		// Sets the margins, in pixels (conversion dp to pixels needed)
		int l = (int) convertDpToPixel((float) leftInDp, context);
		int t = (int) convertDpToPixel((float) topInDp, context);
		int r = (int) convertDpToPixel((float) rightInDp, context);
		int b = (int) convertDpToPixel((float) bottomInDp, context);
		Log.d("-SCREEN", "(ACTIVITY) setMargins(): " + l + ", " + t + ", " + r + ", " + b);
		if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
			ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
			p.setMargins(l, t, r, b);
			v.setLayoutParams(p); // added
			v.requestLayout();
			Log.d("-SCREEN", "(ACTIVITY) setMarginsInDp(): done. ");
		} else {
			Log.d("-SCREEN", "(ACTIVITY) setMarginsInDp(): NOT done! ");
		}
	}

	public static void setMarginsInPx (View v, Context context,
									   int leftInPx, int topInPx, int rightInPx, int bottomInPx) {

		Log.d("-SCREEN", "(ACTIVITY) setMargins(): " +
				leftInPx + ", " + topInPx + ", " + rightInPx + ", " + bottomInPx);

		if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
			ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
			p.setMargins(leftInPx, topInPx, rightInPx, bottomInPx);
			v.requestLayout();
			Log.d("-SCREEN", "(ACTIVITY) setMarginsInPx(): done.");
		} else {
			Log.d("-SCREEN", "(ACTIVITY) setMarginsInPx(): NOT done!");
		}
	}


	public static float convertDpToPixel(float dp, Context context){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		return px;
	}

	public static float convertPxToDp (float px, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		return dp;
	}

	public boolean isXLarge() { // resource 'isTablet' in attr.xml in folder sw600dp.xml
		return getResources().getBoolean(R.bool.isTablet); // 2019-11-28
	}


	private boolean isOrientationLandscape() {
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
			Log.v("-SCREEN","showOrientation() - Landscape");
			return true;
		}
		else {
			Log.v("-SCREEN","showOrientation() - Portrait");
			return false;
		}
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v("-ACTIVITY","onDestroy()");
		hideKeyboard();
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d("-SCREEN", "ACTIVITY onConfigurationChanged()");
		// probably will only be called if android:configChanges="orientation|screenSize"
		// is added to Manifest <activity...

		// final EditText textView = (EditText) findViewById(R.id.entry);
		Log.d("-VIEW", "ACTIVITY onConfigurationChanged()");
		showKeyboard();
	}


	public void showSoftKeyboard() {

		Log.d("-KBD", "showSoftKeyboard()");
		EditText textView = findViewById(R.id.entry);
		if (Build.VERSION.SDK_INT >= 26) {
			textView.setFocusedByDefault(true); // only works on new phones
		}
		if (textView.requestFocus()) {
			InputMethodManager imm = (InputMethodManager)
					getSystemService(Context.INPUT_METHOD_SERVICE);
			if (isOrientationLandscape()) {
				textView.requestFocus(); // these two lines are critical in landscape orientation
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
			} else {
				imm.showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
			}
		}
	}

	private void showKbd() {
		Log.d("-KBD", "showKbd()");

		EditText textView = findViewById(R.id.entry);
		if (Build.VERSION.SDK_INT >= 26) {
			textView.setFocusedByDefault(true); // only works on new phones
		}
		try {
			textView.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(textView, SHOW_IMPLICIT);
		} catch (Exception e) {
			Toast toast = Toast.makeText(this,
					"Keypad: " + e,
					Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private void showKeyboard() { // 2017-11-27 and 2018-03-01 and 2019-11-14
		Log.d("-KBD", "showKeyboard()");
		// Use delay before showing keyboard
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				//showKbd();
				showSoftKeyboard();
			}
		}, 1000);
	}

	private void hideKeyboard() {
		Log.d("-KBD", "hideKeyboard()");
		EditText textView = findViewById(R.id.entry);
		try {
			InputMethodManager imm = (InputMethodManager)getSystemService(
					Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
		} catch (Exception e) {
			Log.d("-ACTIVITY", "hideKeyboard() error: " + e);
		}
	}


	private Dialog d = null;
	private boolean kbdAdded = false;

	private void showDialogOnFirstStart(boolean force) {
		Log.d("-ACTIVITY", "showDialogOnFirstStart()");
		Log.d("-FIRST_START", "Show dialog on first start? Just asking...");

		if (d != null)
			d.dismiss();

		if ((CMBOKeyboardApplication.getApplication().getPreferences().isThisFirstStart()) || (force) ) {

			if (kbdAdded) return;

			Log.d("-FIRST_START", "YES! Show dialog on first start.");


			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			builder.setCancelable(true);
			//builder.setTitle(" Add keyboard");
			builder.setTitle(R.string.header_add_kbd);
			//builder.setMessage("Please turn on ComboKey on the list of optional keyboards.");
			builder.setMessage(R.string.pleaseaddcombokeyboard);
			builder.setPositiveButton("OK", new
					DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							addInputMethod();
						}
					});
			builder.show();
			kbdAdded = true;
		}

	}

	private void showInfoDialog() {
		Log.d("-ACTIVITY", "showInfoDialog()");

		if (d != null)
			d.dismiss();

			d = new Dialog(this);
			Window window = d.getWindow();
		try {
			//assert window != null;
			window.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, 0);
		} catch (Exception e) {
			Toast toast = Toast.makeText(this,
					"Info dialog: " + e,
					Toast.LENGTH_SHORT);
			toast.show();
		}
			d.setTitle(R.string.header_info);// + android.os.Build.MODEL);
			d.setContentView(R.layout.tips_dialog);
			d.show();
	}

	private void showHelpDialog() {
		Log.d("-ACTIVITY", "showHelpDialog()");


		if (d != null)
			d.dismiss();

		d = new Dialog(this);
		Window window = d.getWindow();
		try {
			//assert window != null;
			window.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, 0);
		} catch (Exception e) {
			Toast toast = Toast.makeText(this,
					"Info dialog: " + e,
					Toast.LENGTH_SHORT);
			toast.show();
		}
		d.setTitle(R.string.header_help);// + android.os.Build.MODEL);
		d.setContentView(R.layout.help_dialog);
		d.show();
	}


	private void restoreUiState() {
		Log.d("-ACTIVITY", "restoreUiState()");

		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		editableText = settings.getString(EDITABLE_TEXT_KEY, "");
		Log.d("-KBD", "restoreUiState()");
		showKeyboard();
	}


	final float dispDensity = CMBOKeyboardApplication.getApplication().getResources().getDisplayMetrics().density;
	final float scale = (float) (0.42 * dispDensity + 0.44/dispDensity);
	// ...gives equal amount of text on e.g. Samsung G A3 and Nokia 6 (edited 2017-10)

	private void setFontSize() {
		Log.d("-ACTIVITY", "setFontSize()");

		EditText textView = (EditText) findViewById(R.id.entry);
		textView.setTextSize(CMBOKeyboardApplication.getApplication().getPreferences().getFontSize() * scale);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d("-ACTIVITY", "onSaveInstanceState()");

		super.onSaveInstanceState(outState);
		EditText textView = (EditText) findViewById(R.id.entry);

		outState.putString(EDITABLE_TEXT_KEY, textView.getText().toString());
		outState.putInt(EDIT_POSITION_KEY, textView.getSelectionStart());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d("-ACTIVITY", "onRestoreInstanceState()");

		super.onRestoreInstanceState(savedInstanceState);

		editableText = savedInstanceState.getString(EDITABLE_TEXT_KEY);
		position = savedInstanceState.getInt(EDIT_POSITION_KEY);
	}

	@Override
	public void onPause() {
		Log.d("-ACTIVITY", "onPause()");

		super.onPause();
		if (d != null)
			d.dismiss();

		SharedPreferences uiState = getPreferences(0);
		SharedPreferences.Editor editor = uiState.edit();

		EditText textView = (EditText) findViewById(R.id.entry);
		editableText = textView.getText().toString();

		position = textView.getSelectionStart();
		editor.putInt(EDIT_POSITION_KEY, position);
		editor.putString(EDITABLE_TEXT_KEY, editableText);
		//editor.commit(); // write into persistent memory immediately
		editor.apply(); // write handled in the background (2017-10)

		hideKeyboard(); // 2019-03-07

	}

	private Locale getEditTextLocale() {
		// If API level 17 (JELLY_BEAN_MR1) or higher, the text field locale can be set:
		try {
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				// API level 21 (LOLLIPOP) and higher can resolve locale from language tag
				String tag = getJsonLanguageTag();
				Locale loc = Locale.forLanguageTag(tag);
				Log.d("-LOCALE", "--- (Activity) editText Locale should be " + loc);
				return loc;
			}
		} catch (Exception e) { // on error
			Log.d("-LOCALE", "--- (Activity) editText locale error: " + e);
			return Locale.getDefault();
		}
		// No way to resolve and set
		Log.d("-LOCALE", "--- (Activity) locale of editText set to Default = " + Locale.getDefault());
		return Locale.getDefault();
	}

	private String getJsonLanguageTag(){ // e.g."en-US" from JSON extra
		// JSON Metadata: extra = ["-","dev","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]
		// JSON Metadata: extra = ["-","-","en","US","-","-","-","-","-","-","-","-","-","-","-","-","-"]
		// languageTag = en-US
		String result = "";
		String extra = CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.LAYOUT_EXTRA);
		String[] ary = extra.split("\",\""); // Note: Not allowed to have any space between items? Just a comma?
		result = ary[2] + "-" + ary[3];
		return result;
	}


	@Override
	public void onResume() {
		Log.d("-ACTIVITY", "ACTIVITY onResume()");
		Log.d("-VIEW", "ACTIVITY onResume()");
		super.onResume();

		final EditText textView = (EditText) findViewById(R.id.entry);

		//if (Build.VERSION.SDK_INT >= 11)... is no more needed here
		textView.setRawInputType(TYPE_CLASS_TEXT);
		textView.setTextIsSelectable(true);

		textView.setInputType(InputType.TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_MULTI_LINE | TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		//textView.setSingleLine(false); // deprecated, caused crashes on Android 9, use line above instead

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // locale
			textView.setTextLocale(getEditTextLocale()); // only for API 17 (JELLY_BEAN_MR1) and higher
			Log.d("-LOCALE", "--- (Activity) editText Locale is now set to " + textView.getTextLocale());
		}

		restoreUiState(); // includes showKeyboard()

		showToast("**** - on Resume (Activity) -", CMBOKeyboardView.debugInUse);

		setFontSize();

		Log.d("-SCREEN", "Setting text background...");
		
		if (CMBOKeyboardApplication.getApplication().getPreferences().isTransparentBackground()) {
			//textView.setBackgroundResource(R.xml.transp_rect); // must be drawable:
			//textView.setBackgroundResource(R.drawable.screen_transp_dark);
			textView.setBackgroundResource(R.drawable.screen_transp_darker);
			Log.d("-SCREEN", "...transparent dark");
			textView.setTextColor(Color.WHITE); // 2019-0307 defined in xml
			//textView.setBackgroundColor(LTGRAY);
			textView.setCursorVisible(true);
		} else {
			//textView.setBackgroundResource(R.xml.black_rect); // deleted, must be drawable:
			// TODO: crash, check reason for crash:
			textView.setBackgroundResource(R.drawable.screen_black);
			Log.d("-SCREEN", "...black");
			// The line above: Crash, resume activity, 2018-03-23, at android.widget.EditText.setSelection,
			// out of bounds exception
			textView.setTextColor(Color.WHITE); // 2019-0307 defined in xml
			//textView.setBackgroundColor(BLACK);
			textView.setCursorVisible(true);
		}

		try {
			textView.setText(editableText);
			textView.setSelection(position);
		} catch (Exception e) { // prevent a rare crash
			showToast("Failed to fully resume text area content.", true);
		}

	}

	private int selectLayoutResource() {

		//int padPosition = CMBOKeyboardApplication.getApplication().getPreferences().getPadPosition();
		int padHeight = CMBOKeyboardApplication.getApplication().getPreferences().getPadHeight();

		Log.d("-SCREEN", "ACTIVITY selectLayoutResource() - padHeight = " + padHeight);

		/*
		switch (padHeight) {

			case 0: // Tall default pad
				layoutResource =  R.layout.activity;
				Log.d("-SCREEN", "Layout: R.layout.activity");
				break;
			case 10: // Higher smaller pad
				layoutResource =  R.layout.activity_smaller_higher;
				Log.d("-SCREEN", "Layout: activity_smaller_higher");
				break;
			case 20: // Lower smaller pad
				layoutResource =  R.layout.activity_smaller_lower;
				Log.d("-SCREEN", "Layout: activity_smaller_lower");
				break;
			default: // default pad
				layoutResource =  R.layout.activity;
				break;
		}
		*/
		layoutResource =  R.layout.activity;
		//Toast toast = Toast.makeText(this, "layoutResource case " + padHeight,	Toast.LENGTH_SHORT);
		//toast.show();

		return layoutResource;
	}


	/**
	 * Create all the needed classes
	 */
	public void initialize() {
		Log.d("-ACTIVITY", "initialize()");

		setContentView(selectLayoutResource());
		// selectLayoutResource();
	}

	public void onButtonClickAddKbd(View view) { // Not really used any more, see showDialogOnFirstStart();
		Log.d("-ACTIVITY", "onButtonClick()");

		addInputMethod();
	}

	public void onClickText(View view) {
		Log.d("-ACTIVITY", "onClickText()");
		//showToast("Text Clicked", true);
		Log.d("-KBD", "onClickText()");
		//showKeyboard();
	}

	public void onButtonClickInfo(View view) {
		Log.d("-ACTIVITY", "onButtonClickInfo()");

		// ----Animation ---
		Button button = (Button)findViewById(R.id.infoButton);
		final Animation buttonAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

		// Simple XML animation
		button.startAnimation(buttonAnim);

		//Toast.makeText(this, "Info pressed", Toast.LENGTH_LONG).show();
		showInfoDialog();
	}


	public void onButtonClickHelp(View view) {
		Log.d("-ACTIVITY", "onButtonClickHelp()");

		// ----Animation ---

		Button button = (Button)findViewById(R.id.helpButton);
		final Animation buttonAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

		// Simple XML animation
		button.startAnimation(buttonAnim);

		//Toast.makeText(this, "Help pressed", Toast.LENGTH_LONG).show();
		showHelpDialog();
	}


	public void onButtonClickSwitchKbd(View view) {
		Log.d("-ACTIVITY", "onButtonClickKeyboard()");

		Log.d("-FIRST_START", "Keyboard button pressed (Activity).");

		// ----Animation ---

		Button button = (Button)findViewById(R.id.switchKbdButton);
		final Animation buttonAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

		// Simple XML animation
		button.startAnimation(buttonAnim);

		Log.d("-KBD", "onButtonClickSwitchKbd()");
		showKeyboard();

		if (isComboKeyEnabled()) {
			selectInputMethod();
		} else {
			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			builder.setCancelable(true);
			// Please add keyboard::
			builder.setTitle(R.string.header_add_kbd);
			// Please turn on ComboKey on the list of optional keyboards:
			builder.setMessage(R.string.pleaseaddcombokeyboard);
			builder.setPositiveButton("OK", new
					DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							addInputMethod();
						}
					});
			builder.show();
		}

	}

	private void addInputMethod() {
		hideKeyboard(); // 2019-11-18
		Log.d("-ACTIVITY", "addInputMethod()");
		startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), 0);
	}

	private void selectInputMethod() {
		Log.d("-ACTIVITY", "selectInputMethod()");

		InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showInputMethodPicker();
		} else {
			Toast.makeText(this, R.string.not_possible_im_picker, Toast.LENGTH_LONG).show();
		}
	}


	public void onButtonClickSettings(View view) {
		Log.d("-SETTINGS", "Activity: onButtonClickSettings()");

		// ----Animation ---
		Button button = (Button)findViewById(R.id.settingsButton);
		final Animation buttonAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);

		// Simple XML animation
		button.startAnimation(buttonAnim);

		//Toast.makeText(this, "Settings pressed", Toast.LENGTH_LONG).show();
		handleSettings();
	}


	private void handleSettings() { // Tools/Settings  menu selected via buttons
		Log.d("-ACTIVITY", "handleSettings()");
		hideKeyboard(); // may not be necessary here
		//Intent i = new Intent(getContext(),
		Intent i = new Intent(this, CMBOPreferencesActivity.class);
		//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // earlier versions up to 3.9
		//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // tested 2019-03-13

		this.startActivity(i);
	}


	private void showToast(String toastMessage, boolean inUse){
		Log.d("-ACTIVITY", "showToast(): " + toastMessage);

		if (!inUse) return;

		Toast toast = Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();

	}


}
