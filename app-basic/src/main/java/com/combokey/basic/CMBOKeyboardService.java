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

//import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.combokey.basic.layout.Layout;
import com.combokey.basic.view.CMBOKeyboardButtonLayout;
import com.combokey.basic.view.CMBOKeyboardView;
import com.combokey.basic.view.touchevent.SimpleTouchEventProcessor;
import com.combokey.basic.view.touchevent.filter.CoordinateStretchFilter;
import com.combokey.basic.view.touchevent.filter.CoordinateThresholdFilter;
import com.combokey.basic.view.touchevent.filter.FilteringTouchEventProcessor;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import static android.speech.tts.TextToSpeech.LANG_AVAILABLE;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class CMBOKeyboardService extends InputMethodService implements
		KeyboardOutput {

	private final static String TAG = "CMBOKeyboard";

	private CMBOKeyboardButtonLayout buttonLayout;
	private CMBOKeyboardController controller;
	private CMBOKeyboardView view;

	private CMBOWordCandidates candidates = CMBOKeyboardApplication.getApplication().getCandidates();

	private String savedNotes = CMBOKeyboardApplication.getApplication().getPreferences().getNotes();
	private String notesToSave = "";
	private String savedText = CMBOKeyboardApplication.getApplication().getPreferences().getTempString();
	private String textToSave = "*empty*";
	private boolean notesVisible = false;

    private WindowManager candWindowManager;
    private ImageView candWindow;
    private boolean isTopBar = false;

    private TextToSpeech textToSpeech1;
	public boolean textToSpeechAvailable = false;
	private String voiceInputText = "";
	public int prevOrientation = Configuration.ORIENTATION_UNDEFINED; // used as the previous orientation to detect change
	public static boolean readyToClearText = false; // double Clear required to delete all test
	public static boolean readyToPlayText = false; // double Play required first to start text-to-speech (to avoid getting embarrassed)
	public static boolean readyToSaveText = false;
	private String ctrlKey = "";


	CountDownTimer topBarTimer = new CountDownTimer(200, 100) { // 1000, 500
		// (time in ms to onFinish(), Tick interval while counting)
		@Override
		public void onTick(long l) {
			Log.d("-TIMER", "- (init) topBar timer onTick");
		}
		@Override
		public void onFinish() {
			Log.d("-TIMER", "- (init) TTS timer done, time to set speech language = current language.");
			if (isTopBar) {
				updateTopBarText();
			}
			view.redraw();
			topBarTimer.cancel();
		}
	};

	// ---------------------

	CountDownTimer initTtsTimer = new CountDownTimer(2000, 600) { // 1000, 500
		// (time in ms to onFinish(), Tick interval while counting)
		@Override
		public void onTick(long l) {
			Log.d("-TIMER", "- (init) TTS timer onTick");

		}
		@Override
		public void onFinish() {
			Log.d("-TIMER", "- (init) TTS timer done, time to set speech language = current language.");
			if (textToSpeechAvailable) {
				Log.d("-SPEECH", "- (init) textToSpeechAvailable = true");
				initSpeechLanguage(); // 2019-09-19 to avoid nullPointerException
			}
			initVoiceInputLang();
			initTtsTimer.cancel();
		}
	};

	CountDownTimer playTimer = new CountDownTimer(120000, 1000) {
		// (time in ms to onFinish(), Tick interval while counting)
		@Override
		public void onTick(long l) { // to show Play button symbol again when play stops
			Log.d("-TIMER", "- Play timer onTick");
			if (!textToSpeech1.isSpeaking()){
				CMBOKeyboardApplication.getApplication().getLayoutManager()
						.changeCharacterMapItem(116, "_Play");
				playTimer.cancel();
			}
		}
		@Override
		public void onFinish() {
			Log.d("-TIMER", "- (init) Play timer done, time to monitor if Play will stop.");
				CMBOKeyboardApplication.getApplication().getLayoutManager()
						.changeCharacterMapItem(116, "_Play");
			readyToPlayText = false;
			playTimer.cancel();
		}
	};

	@Override
	public void onWindowHidden() {
		super.onWindowHidden();
		if (isTopBar) candidates.setKeypadVisible(false);
		Log.d("-VIEW", "*** Service: onWindowHidden() KEYPAD HIDDEN ****");
	}
	@Override
	public void onWindowShown() {
		super.onWindowShown();
		if (isTopBar) candidates.setKeypadVisible(true);
		Log.d("-VIEW", "*** Service: onWindowShown() KEYPAD SHOWN ****");
	}



	BroadcastReceiver broadcastReceiver; // get data from voiceTyping activity

	@Override
	public void onCreate() {
		Log.d("-VIEW", "SERVICE onCreate()");
		super.onCreate();
		showToast("onCreate (Service)", CMBOKeyboardView.debugInUse);
		initTtsEngine(); // Start TTS engine
		initTtsTimer.start(); // When TTS running, set TTS language according to current language
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            	String action = intent.getAction();

            	if (action != null) {

					if (action.equalsIgnoreCase("getting_speech_data")) {
						//intent.getStringExtra("value");
						String spoken = intent.getStringExtra("value");
						//showToast("onBrReceiver (Service): " + spoken, true);
						voiceInputText = spoken;
						spoken = "Spoken: " + spoken; // TEST
						//onText(spoken); // Does not work here (no focus on text area)
					} else if (action.equalsIgnoreCase("getting_notes_data")) {
						//intent.getStringExtra("value");
						String notes = intent.getStringExtra("value");
						String status = intent.getStringExtra("status"); // is data really new data ("valid"/"invalid")
						//showToast("onBrReceiver (Service): " + spoken, true);
						//voiceInputText = notes; // TEST
						//notes = "Notes: " + notes; // TEST
						if (!status.equals("invalid")) savedNotes = notes; // only save valid data
						notesVisible = false;
						//onText(spoken); // Does not work here (no focus on text area)
						Log.d("-NOTES", "*** Notes text received in Service");

					}

				}
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        // set the custom action
        // - Action is just a string used to identify the receiver as there can be many
        //   in your app so it helps deciding which receiver should receive the intent.
        intentFilter.addAction("getting_speech_data");
		intentFilter.addAction("getting_notes_data");
        // register the receiver
        registerReceiver(broadcastReceiver, intentFilter);


		// =========================
		//CMBOKeyboardApplication.getApplication().getCandidates().setWordHint("Service"); // TEST
		//CMBOKeyboardApplication.getApplication().getCandidates().initializeWords(this); // ok
		//candidates.initializeWords(this); // ok also
		// ==========================

		//savedNotes = CMBOKeyboardApplication.getApplication().getPreferences().getNotes();
		//savedText = CMBOKeyboardApplication.getApplication().getPreferences().getTempString();

	}

	private void sendTextToNotesActivity(String txt) {
		brText = txt;
		final Handler handler = new Handler(); // Delay required: Activity needs to start before receiving
		handler.postDelayed(new Runnable() {
			//@Override
			public void run() {
				// Do this after delay -------------------------------
				broadcastDelayed();
				Log.d("-NOTES", "Notes broadcasted (delayed) from Service to Notes activity");
				// --------------------------------------------------------
			}
		}, 2000); // 2 s delay
	}

	private String brText = "";
	private void broadcastDelayed() {
		Intent intent = new Intent("sending_notes_data");
		intent.putExtra("value", brText);
		int fontsize = CMBOKeyboardApplication.getApplication().getPreferences().getFontSize();
		String fontSize = Integer.toString(fontsize);
		intent.putExtra("fontsize", fontSize);

		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}


	@Override
	public void onDestroy() {
		Log.d("-VIEW", "onDestroy()");
		super.onDestroy();
		showToast("onDestroy (Service)", CMBOKeyboardView.debugInUse);
		handleKeypadHide(); // 2019-03-19
		textToSpeech1.shutdown(); // 2019-10-14
		unregisterReceiver(broadcastReceiver);

		CMBOKeyboardApplication.getApplication().getPreferences().saveNotes(savedNotes);
		CMBOKeyboardApplication.getApplication().getPreferences().saveTempString(savedText);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d("-SCREEN", "onConfigurationChanged()");
		// probably will only be called if android:configChanges="orientation|screenSize"
		// is added to Manifest <activity...
		Log.d("-VIEW", "SERVICE onConfigurationChanged()");
	}

	/**
	 * Called by the framework when view for showing candidates needs to
	 * be generated, like {@link #onCreateInputView}.
	 */

	@Override
	public View onCreateCandidatesView() { // only used for timing initializing of words list
		Log.d("-VIEW", "onCreateCandidatesView()");
		CMBOKeyboardApplication.getApplication().getCandidates().initializeWords(this);
		return null; // No android candidates view in use;
	}


	@Override
	public void onInitializeInterface() {
		Log.d("-VIEW", "SERVICE onInitializeInterface()");
		super.onInitializeInterface();
		CMBOKeyboardApplication.getApplication().setOutput(this); // One crash report here on v3.7
		this.controller = CMBOKeyboardApplication.getApplication()
				.getController();

	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) { // kbd shows up
		//Log.d("-VIEW", "onStartInputView(). isNumberInputMode() = " + isNumberInputMode());
		Log.d("-VIEW", "SERVICE onStartInputView(). restarting = " + restarting + ", attribute = " + attribute + ", textToSpeech1 = " + textToSpeech1);
		super.onStartInputView(attribute, restarting);
		//CMBOKeyboardView.updateServiceViewKludge = true;
		showToast("*** - onStartInputView - (Service)", CMBOKeyboardView.debugInUse);
		// Start keypad in correct mode (numbers or uppercase/lowercase letters):
		isTopBar = CMBOKeyboardApplication.getApplication().getPreferences().getDisplayWordCandidates();

		checkInputType();
		candidates.clearHint();
	   Log.d("-REDRAW", "Serv: redraw() due to updating input type: 123, Abc, abc(=email/UI)...");
       view.redraw(); // update keypad view

		// Returning from voice input?
		if(!voiceInputText.equals("")) {
			onText(voiceInputText);
			voiceInputText = "";
		}

	}

	@Override
	public View onCreateExtractTextView() {
		return super.onCreateExtractTextView();
	}

	@Override 	// prevent ExtractedView (extra text window) from opening for txt
				// entry in landscape orientation of the device
	public void onUpdateExtractingVisibility(EditorInfo ei) { // 2019-04-09
		ei.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
		super.onUpdateExtractingVisibility(ei);
	}


    @Override
	public void onBindInput() { // after prefs
		Log.d("-VIEW", "SERVICE onBindInput()");

		super.onBindInput();

		Log.d("onBindInput()", "PadHeight = ???");

		showToast("**** - onBindInput - (Service)", CMBOKeyboardView.debugInUse);
		readyToClearText = false; readyToSaveText = false; readyToPlayText = false;
		if (buttonLayout != null) buttonLayout.listButtons();

	}

	public void updateWordAtCursor(){ // Should be called from Keyboard
		if (!isTopBar) return;
		String word = "";
		// Get word left from cursor (word max length can be 40 chars)
		try{
			CharSequence charsBefore = getCurrentInputConnection().getTextBeforeCursor(40, 0);
			int start = charsBefore.length();
			while (start > 0 && !Character.isWhitespace(charsBefore.charAt(start - 1))) start--;
			word = charsBefore.toString().substring(start);
			// Optional:
			//String textBefore = getCurrentInputConnection().getTextBeforeCursor(80, 0).toString();
			//int start = textBefore.length();
			//while (start > 0 && !(textBefore.substring(start - 1)).equals(" ")) start--;
			//word = textBefore.substring(start);
			//
			candidates.updateWordAtCursor(word);
		}
		catch (Exception e) {
			candidates.updateWordAtCursor("");
		}
	}

	private boolean isXLarge() { // resource 'isTablet' in attr.xml in folder sw600dp.xml
		return getResources().getBoolean(R.bool.isTablet); // 2019-11-28
	}

	@Override
	public View onCreateInputView() {

		Log.d("-VIEW", "SERVICE onCreateInputView()");
		showToast("**** - onCreateInputView - (Service)", CMBOKeyboardView.debugInUse);

		int o = getResources().getConfiguration().orientation;

		SimpleTouchEventProcessor processor = null;

		LinearLayout layout = null;

		int layoutResource = R.layout.input; // defaults
		int landscapeColumns = 3; // 3 = 5-row, 5 = 3-row, 6 = double
		int padHeight = 0;
		int padType = 0; // 0 = 5-row, 10 = 3-row special

		try { // padHeight 0, 10, 20. 0 = normal, 10 = small higher up, 20 = smaller lower down
			padHeight = CMBOKeyboardApplication.getApplication().getPreferences().getPadHeight();
			Log.d("-PADTYPE", "padHeight = " + padHeight + " (10/0/20: upper small / normal / lower small))");
		} catch (Exception e) {
			Log.d("-PADTYPE", "Could not get padHeight.");
			padHeight = 0; // normal height
		}

		try { //  // 0 = 5-row, 10 = 3-row special
			padType = CMBOKeyboardApplication.getApplication().getPreferences().getPadType();
			landscapeColumns = CMBOKeyboardApplication.getApplication().getPreferences().getLandscapeColumns();
			Log.d("-PADTYPE", "Pad Type (Portrait)  = " + padType + " (10 = 3-row, 0 = 5-row)");
			Log.d("-PADTYPE", "Pad Type (Landscape) = " + landscapeColumns + " (5 = 3-row, 3 = 5-row, 6 = double 5-row)");
			Log.d("-PADTYPE", "Tablet:  " + isXLarge());
		} catch (Exception e) {
			Log.d("-PADTYPE", "Could not get padType.");
			padType = 0; // normal
		}

		//boolean showWords = CMBOKeyboardApplication.getApplication().getPreferences().getDisplayWordCandidates();

		// ================= Select resource =============================

		switch (padHeight) { // it is not enough to set the input xml file only here, must be finalized later
			case 10: // smaller higher up
				if (padType == 0) { // 5-row pad
					Log.d("-PADTYPE", " Smaller pad higher up, 5-row Portrait pad.");
					//layoutResource = showWords ? R.layout.input_words : R.layout.input_smaller_higher;
					layoutResource = R.layout.input_smaller_higher;
				} else { // 3-row pad (padType = 10)
					Log.d("-PADTYPE", " Smaller pad higher up, 3-row Portrait pad.");
					//layoutResource = showWords ? R.layout.input_words : R.layout.input_smaller_higher_3row;
					layoutResource = R.layout.input_smaller_higher_3row;
				}
				break;
			case 0: // normal height
				if (padType == 0) { // 5-row pad
					Log.d("-PADTYPE", " Full size pad, 5-row Portrait pad.");
					//layoutResource = showWords ? R.layout.input_words : R.layout.input;
					layoutResource = R.layout.input;
				} else { // 3-row pad
					Log.d("-PADTYPE", " Full size pad, 3-row Portrait pad.");
					//layoutResource = showWords ? R.layout.input_words : R.layout.input_3row;
					layoutResource = R.layout.input_3row;
				}
				break;
			case 20: // smaller lower down
				if (padType == 0) { // 5-row pad
					Log.d("-PADTYPE", " Smaller pad lower down, 5-row Portrait pad.");
					//layoutResource = showWords ? R.layout.input_words : R.layout.input_smaller_lower;
					layoutResource = R.layout.input_smaller_lower;
				} else { // 3-row pad
					Log.d("-PADTYPE", " Smaller pad lower down, 3-row Portrait pad.");
					//layoutResource = showWords ? R.layout.input_words : R.layout.input_smaller_lower_3row;
					layoutResource = R.layout.input_smaller_lower_3row;
				}
				break;
			default:
				break;
		}

		Log.d("onCreateInputView()", "PadHeight = " + padHeight);

		switch (o) {
			case Configuration.ORIENTATION_LANDSCAPE:

				layout = (LinearLayout) getLayoutInflater().inflate(layoutResource, // inflate(int resource, ViewGroup root)
						null); // Inflate a new view hierarchy from the specified xml resource.
				view = (CMBOKeyboardView) layout.findViewById(R.id.keyboard);
				view.setService(true);

				if (CMBOKeyboardApplication.getApplication().getPreferences().isKeysDisabled()) { // Prevent slippage
					processor = new FilteringTouchEventProcessor().addFilter( // (poor multitouch screen response)
							new CoordinateThresholdFilter(10f, 8, 10)).addFilter( // 10f, 8, 10
							new CoordinateStretchFilter(1f, 5f, 15)); // 1f, 5f, 15
				} else {

					processor = new FilteringTouchEventProcessor().addFilter( // Standard case (proper multitouch screen)
							new CoordinateThresholdFilter(10f, 64, 10)).addFilter( // 10f, 64, 10
							new CoordinateStretchFilter(1f, 10f, 30)); // 1f, 10f, 30
				}
				view.setTouchEventProcessor(processor);
				break;

			case Configuration.ORIENTATION_PORTRAIT:
			default:
				layout = (LinearLayout) getLayoutInflater().inflate(layoutResource,
						null);
				view = (CMBOKeyboardView) layout.findViewById(R.id.keyboard);

				view.setService(true);

				processor = new FilteringTouchEventProcessor().addFilter(
						new CoordinateThresholdFilter(10f, 64, 10)).addFilter( // 10f, 64, 10
						new CoordinateStretchFilter(1f, 10f, 30)); // 1f, 10f, 30

				view.setTouchEventProcessor(processor);
				break;

		}

		view.setController(controller);
		int orientation = getResources().getConfiguration().orientation;

		int leftMargin = 0;
		int topMargin = 0;
		int rightMargin = 0;
		int bottomMargin = 0;
		int heightDecrease = 0;

		int kbdScale = 30; // float, default master scale setting

		boolean noPanel = CMBOKeyboardApplication.getApplication().getPreferences().isSidePanelHidden();

		// ======================= Set Keyboard size and position =====================

		int screenWidth = 0;
		int screenHeight = 0;
		int screenMarginAbove = 0;
		int keypadHeight = 0;
		int keypadWidth = 0;
		boolean rows3 = false;

		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			//      ============== portrait =============
			screenWidth = (int) convertPixelToDp(screenWidth(), getApplicationContext()); // smaller value
			screenHeight = (int) convertPixelToDp(screenHeight(), getApplicationContext());// larger value
			screenMarginAbove = isXLarge() ? 3 * screenHeight / 4 : 20 * screenHeight / 32;
		} else { // =============== landscape ============
			screenWidth = (int) convertPixelToDp(screenHeight(), getApplicationContext()); // larger value
			screenHeight = (int) convertPixelToDp(screenWidth(), getApplicationContext());// smaller value
			//screenMarginAbove = isXLarge() ? 2 * screenHeight / 3 : 14 * screenHeight / 32;
			screenMarginAbove = isXLarge() ? (int) (0.64 * screenHeight) : 14 * screenHeight / 32;
		}

		// --------------- These are valid for portrait: ---------------------

		int padPosition = CMBOKeyboardApplication.getApplication().getPreferences().getPadPosition();

		int extraWidth3Row = 0;
		int lessHeight3Row = 0;
		if (padType == 10) { // 3-row extra width and less height in some cases
			extraWidth3Row = screenWidth/6;
			lessHeight3Row = 40;
		}

		switch (padHeight + padPosition) { // 0/10/20/30 + 0/1/2/3  // ===== using dp ======
		//switch (padHeight) { // ===== using dp ======
			case 0: // Tall default // Left, portrait
					bottomMargin = 0; topMargin = 0;
					leftMargin = 0;
				if (noPanel) {
					rightMargin = isXLarge() ? 3 *screenWidth/5 - extraWidth3Row : 2*screenWidth/5;
				} else if (padType == 10) { // 3-row?
					rightMargin = isXLarge() ? screenWidth/4 : 0;
					rows3 = true;
				} else { // 5-row
					rightMargin = isXLarge() ? screenWidth/2 : 0;
				}
				break;
			case 1: // Tall default // Center wide, portrait
					bottomMargin = 0; topMargin = 0;
				if (noPanel) {
					leftMargin = isXLarge() ? screenWidth / 4 - extraWidth3Row : 0;
					rightMargin = isXLarge() ? screenWidth / 4 - extraWidth3Row : 0;
				} else  if (padType == 10) { // 3-row?
					leftMargin = isXLarge() ? screenWidth / 8 : 0;
					rightMargin = isXLarge() ? screenWidth / 8 : 0;
					rows3 = true;
				} else { // 5-row
					leftMargin = isXLarge() ? screenWidth / 5 : 0;
					rightMargin = isXLarge() ? screenWidth / 5 : 0;
				}
				break;
			case 2: // Tall default // Right, portrait
					bottomMargin = 0; topMargin = 0;
					rightMargin = 0;
				if (noPanel) {
					leftMargin = isXLarge() ? 3*screenWidth/5 - extraWidth3Row : 2*screenWidth/5;
				} else if (padType == 10) { // 3-row?
					leftMargin = isXLarge() ? screenWidth/4 : 0;
					rows3 = true;
				} else {
					leftMargin = isXLarge() ? screenWidth/2 : 0;
				}
				break;
			case 3: // Tall default // Center narrow, portrait
					bottomMargin = 0; topMargin = 0;
				if (noPanel) {
					leftMargin = isXLarge() ? 3*screenWidth/10 - extraWidth3Row : screenWidth/5;
					rightMargin = isXLarge() ? 3*screenWidth/10 - extraWidth3Row : screenWidth/5;
				} else if (padType == 10) { // 3-row? {
					leftMargin = isXLarge() ? screenWidth/6 : screenWidth/5;
					rightMargin = isXLarge() ? screenWidth/6 : 0;
					rows3 = true;
				} else { // 5-row
					leftMargin = isXLarge() ? screenWidth/4 : screenWidth/5;
					rightMargin = isXLarge() ? screenWidth/4 : 0;
				}
				break;
			case 10: // Higher smaller // Left, portrait
					bottomMargin = 40; topMargin = 0;
					leftMargin = 0;
				if (noPanel) {
					rightMargin = isXLarge() ? 2*screenWidth/3 - extraWidth3Row : screenWidth/2;
				} else if (padType == 10) { // 3-row? {
					rightMargin = isXLarge() ? screenWidth/3 : screenWidth/6;
					rows3 = true;
				} else { // 5-row
					rightMargin = isXLarge() ? screenWidth/2 : screenWidth/6;
				}
				break;
			case 11: // Higher smaller // Center wide, portrait
				bottomMargin = 40; topMargin = 0;
				if (noPanel) {
					leftMargin = isXLarge() ? screenWidth / 4  - extraWidth3Row/2 : 0;
					rightMargin = isXLarge() ? screenWidth / 4 - extraWidth3Row/2 : 0;
				} else if (padType == 10) { // 3-row? {
					leftMargin = isXLarge() ? screenWidth / 8 : 0;
					rightMargin = isXLarge() ? screenWidth / 8 : 0;
					rows3 = true;
				} else { // 5-row
					leftMargin = isXLarge() ? screenWidth / 4 : 0;
					rightMargin = isXLarge() ? screenWidth / 4 : 0;
				}
				break;
			case 12: // Higher smaller // Right, portrait
				bottomMargin = 40; topMargin = 0;
				rightMargin = 0;
				if (noPanel) {
					leftMargin = isXLarge() ? 2*screenWidth/3 - extraWidth3Row : screenWidth/2;
				} else if (padType == 10) { // 3-row? {
					leftMargin = isXLarge() ? screenWidth/3 : screenWidth/6;
					rows3 = true;
				} else { // 5-row
					leftMargin = isXLarge() ? screenWidth/2 : screenWidth/6;
				}
				break;
			case 13: // Higher smaller // Center narrow, portrait
				bottomMargin = 40; topMargin = 0;
				if (noPanel) {
					leftMargin = isXLarge() ? 3*screenWidth/10 - extraWidth3Row/2 : screenWidth/4;
					rightMargin = isXLarge() ? 3*screenWidth/10 - extraWidth3Row/2 : screenWidth/4;
				} else if (padType == 10) { // 3-row? {
					leftMargin = isXLarge() ? screenWidth/5 : screenWidth/5;
					rightMargin = isXLarge() ? screenWidth/5 : 0;
					rows3 = true;
				} else { // 5-row
					leftMargin = isXLarge() ? screenWidth/4 : screenWidth/5;
					rightMargin = isXLarge() ? screenWidth/4 : 0;
				}
				break;
			case 20: // Lower smaller // Left, portrait
					bottomMargin = 0;  heightDecrease = 40; //  topMargin = 0; //40;
					leftMargin = 0;
				if (noPanel) {
					rightMargin = isXLarge() ? 2*screenWidth/3 - extraWidth3Row : screenWidth/2;
				} else if (padType == 10) { // 3-row? {
					rightMargin = isXLarge() ? screenWidth/3 : screenWidth/6; // 420
					rows3 = true;
				} else { // 5-row
					rightMargin = isXLarge() ? screenWidth/2 : screenWidth/6; // 420
				}
				break;
			case 21: // Lower smaller // Center wide, portrait
				bottomMargin = 0; heightDecrease = 40; // topMargin = 0; //40;
				if (noPanel) {
					leftMargin = isXLarge() ? 3 * screenWidth / 10 - extraWidth3Row/2 : 0;
					rightMargin = isXLarge() ? 3 * screenWidth / 10 - extraWidth3Row/2 : 0;
				} else if (padType == 10) { // 3-row? {
					leftMargin = isXLarge() ? screenWidth / 8 : 0;
					rightMargin = isXLarge() ? screenWidth / 8 : 0;
					rows3 = true;
				} else { // 5-row
					leftMargin = isXLarge() ? screenWidth / 4 : 0;
					rightMargin = isXLarge() ? screenWidth / 4 : 0;
				}
				break;
			case 22: // Lower smaller // Right, portrait
				bottomMargin = 0; heightDecrease = 40; // topMargin = 0; //40;
				rightMargin = 0;
				if (noPanel) {
					leftMargin = isXLarge() ? 2*screenWidth/3 - extraWidth3Row : screenWidth/2;
				} else if (padType == 10) { // 3-row? {
					leftMargin = isXLarge() ? screenWidth/3 : screenWidth/6;
					rows3 = true;
				} else { // 5-row
					leftMargin = isXLarge() ? screenWidth/2 : screenWidth/6;
				}
				break;
			case 23: // Lower smaller // Center narrow, portrait
				bottomMargin = 0; heightDecrease = 40; // topMargin = 0; //40;
				if (noPanel) {
					leftMargin = isXLarge() ? 3*screenWidth/10 - extraWidth3Row/2 : screenWidth/4;
					rightMargin = isXLarge() ? 3*screenWidth/10 - extraWidth3Row/2 : screenWidth/4;
				} else if (padType == 10) { // 3-row? {
					leftMargin = isXLarge() ? screenWidth/6 : screenWidth/5;
					rightMargin = isXLarge() ? screenWidth/6 : 0;
					rows3 = true;
				} else { // 5-row
					leftMargin = isXLarge() ? screenWidth/4 : screenWidth/5;
					rightMargin = isXLarge() ? screenWidth/4 : 0;
				}
				break;
			case 30: // 3-row, not in use any more, use landscapeColumns instead: 3, 5, or 6
				break;
			default:
					bottomMargin = 0; topMargin = 0; // portrait
				if (noPanel) {
					leftMargin = 0; rightMargin = 0;
				} else  if (padType == 10) { // 3-row?{
					leftMargin = 0; rightMargin = 0;
					rows3 = true;
				} else { // 5-row
					leftMargin = 0; rightMargin = 0;
				}
				break;
		}

		// padType: 0 = Normal, 10 = 3-row special (portrait for now)
		// landscapeColumns: padType in landscape orientation: 5 = 3-row, 3 = 5-row, 6 = double 5-row
		// padPosition: 0 = left, 1/3 = middle (wide/narrow), 2 = right
		// padHeight: 10 = Higher small, 0 = Normal, 20 = Lower small, 30 = 3-row special (portrait for now)

		// ------------- These are valid for landscape: ------------------------

		if ((landscapeColumns == 5) || (padType == 10)) { // landscape 3-row OR portrait 3-row

			// ======== 3-row pads (landscape and portrait) ========

			if (orientation == Configuration.ORIENTATION_PORTRAIT) { // Portrait
				if (!isXLarge()) { // phone portrait
					leftMargin = 0;
					rightMargin = 0;
				}
				topMargin = 2 * topMargin;
				//bottomMargin = 2 * bottomMargin;
			} else { // Landscape
				leftMargin = 0; rightMargin = 0;
			}

		} else { // =========== 5-row pads (landcape and portrait) ================

			if (orientation == Configuration.ORIENTATION_LANDSCAPE) { // landscape

				if (!isXLarge()) { // Phone landscape
					bottomMargin = 0; // keep bottom at screen bottom because there's
					//topMargin = isTopBar ? 40 : 0;  // 40 : 0 not much room for height, gets too small
					lessHeight3Row = 0; // else becomes too small in height
					if (isTopBar) heightDecrease = 40; // but never full height
				} // Tablet landscape
				// TODO: Later, fix LayoutGenerator for these so that full screen width will not be used
				leftMargin = 0;
				rightMargin = 0;// <= to use the old LayoutGeneration definitions for landscape (screen-wide)
			} else {					 // portrait:
				if ((padHeight == 0) && (!isXLarge())) {
					bottomMargin = 0; topMargin = 0;
				}
			}
		}

		isTopBar = CMBOKeyboardApplication.getApplication().getPreferences().getDisplayWordCandidates();
		int topBarHeight = 0;
		if (isTopBar) {
			topBarHeight = 40;
		}
//		if (rows3) {
//			heightDecrease = 20; //
//		}

		//bottomMargin = 0;

		// heightDecrease is for smaller keypads, lessHeight3Row is less height when 3 rows instead of 5
		keypadHeight = screenHeight - screenMarginAbove - topMargin
				- bottomMargin + topBarHeight - heightDecrease - lessHeight3Row;
		keypadWidth = screenWidth - leftMargin - rightMargin;

		view.setKeyboardSizeAndPositionInDp(keypadWidth, keypadHeight,
				leftMargin, topMargin, rightMargin, bottomMargin,
				getApplicationContext());

		Log.d("-PADTYPE", "Settings: screenHeight = " + screenHeight + ", screenMarginAbove = " + screenMarginAbove);
		Log.d("-PADTYPE", "Settings: topMargin = " + topMargin + "" +
				", bottomMargin = " + bottomMargin + ", topBarHeight = " + topBarHeight);
		Log.d("-PADTYPE", "Settings: leftMargin = " + leftMargin + "" +
				", rightMargin = " + rightMargin);
		Log.d("-PADTYPE", "Setting keypad size. keypadHeight = " + keypadHeight
				+ ", kbdScale = " + kbdScale + ", screenHeight = " + screenHeight()
		 		+ ", keypadWidth = " + keypadWidth);

		return layout;

	}

	private static float convertPixelToDp(float px, Context context){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		return dp;
	}


	public static int screenHeight() {
		int measure1 = Resources.getSystem().getDisplayMetrics().heightPixels;
		int measure2 = Resources.getSystem().getDisplayMetrics().widthPixels;
		return max(measure1, measure2);
	}

	public static int screenWidth() {
		int measure1 = Resources.getSystem().getDisplayMetrics().heightPixels;
		int measure2 = Resources.getSystem().getDisplayMetrics().widthPixels;
		return min(measure1, measure2);
	}


	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
	}


	public void keyUpDown(int keyEventCode) {
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
	}

	public void keyDown(int keyEventCode) {
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
	}
	public void keyUp(int keyEventCode) {
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
	}

	public void sendKeyString(String keyString) { // 2019-03-08
		// This is only used when Ctrl is on.
		// ADD: string to keyChar

		int keyEventCode = KeyEvent.KEYCODE_E;
		keyUpDown(keyEventCode);

		//int charCode = 27;
		//sendKeyChar((char) 27);
	}

	public String getCtrlKey() {
		return this.ctrlKey;
	}

	//private String savedText = "";

	private void updateTopBarText() {
		updateWordAtCursor(); //
	}

	public void onKey(int primaryCode, int[] keyCodes) {
		Log.d("-KEY", "-- onKey -- " + primaryCode);
		//if Ctrl is down, this method is not reached!!!

		ctrlKey = ""; // default
		boolean keepHint = true; // Candidate hint (start of word)

		switch (primaryCode) {
			case KeyboardOutput.KEYCODE_ESCAPE:
				if (isTerminalMode()) {
					sendKeyChar((char) 27);
					//sendKeyChar((char) 65); // 65 = A, to test, keyCode = 229 (NOT A)
					break; // deal with SSH and terminal apps
				}
				keyUpDown(primaryCode); keepHint = false;
				break;
			case KeyEvent.KEYCODE_DEL:
				keyUpDown(primaryCode); //keepHint = false;
				//candidates.addLetter("_BS");
				break;
			case KeyEvent.KEYCODE_ENTER:
				keyUpDown(primaryCode); keepHint = false;
				break;
			case KeyboardOutput.KEYCODE_INSERT:
				keyUpDown(primaryCode); keepHint = false;
				break;
			// -------------
			case KeyEvent.KEYCODE_DPAD_UP:
				ctrlKey = "P"; // previous command
				if (sendKeyWithCtrl("P")) break;
				handleArrow(primaryCode); //keepHint = false;
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				ctrlKey = "N"; // next command if Ctrl+P used before
				if (sendKeyWithCtrl("N")) break;
				handleArrow(primaryCode); //keepHint = false;
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				ctrlKey = "B";
				if (sendKeyWithCtrl("B")) break;
				handleArrow(primaryCode); //keepHint = false;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				ctrlKey = "F";
				if (sendKeyWithCtrl("F")) break;
				handleArrow(primaryCode); //keepHint = false;
				break;
		// ----------------
			case CMBOKeyboard.CUSTOM_KEYCODE_TAB:
				ctrlKey = "I"; // Was "L" in v4.0 which was a bug! Fixed in 4.1
				if (sendKeyWithCtrl("I")) {
					Log.d("-Fn", "(service) CUSTOM_KEYCODE_TAB (terminal key sequence Ctrl+" + ctrlKey + ")");
					break;
				}
				handleTab(); // Use system's KEYCODE_TAB
				keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_END:
				ctrlKey = "E";
				if (sendKeyWithCtrl("E")) break;
				handleEnd(); //keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_HOME:
				ctrlKey = "A";
				if (sendKeyWithCtrl("A")) break;
				handleHome(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_WLEFT:
				ctrlKey = "EscB";
				if (sendKeyWithCtrl("EscB")) break;
				handleWleft(); //keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_WRIGHT:
				ctrlKey = "EscF";
				if (sendKeyWithCtrl("EscF")) break;
				handleWright(); //keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PGDN:
				ctrlKey = "PgDn";
				if (sendKeyWithCtrl("PgDn")) break;
				handlePgDn(); //keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PGUP:
				ctrlKey = "PgUp";
				if (sendKeyWithCtrl("PgUp")) break;
				handlePgUp(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_DEL:
				ctrlKey = "D"; // Delete at cursor (not before cursor)
				if (sendKeyWithCtrl("D")) break;
				handleDel(); //keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_SELECT_ALL:
				handleSelectAll(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_COPY:
				handleCopy(true); // true = show toast of copying
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PASTE:
				ctrlKey ="Paste";
				if (sendKeyWithCtrl("Paste")) break;
				handlePaste(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_CLEAR:
				ctrlKey = "U"; // Clear the entry line
				if (sendKeyWithCtrl("U")) break;
				handleClear(); //controller.setKeyboardFnMode(false);
				keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_LANG:
				handleLang(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_SEARCH:
				ctrlKey = "R"; // Search Bash history
				if (sendKeyWithCtrl("R")) break;
				handleSearch(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_TRANSLATE:
				handleTranslate(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_RECEIVED:
				//handleMicInputMethod(); // TEST!!!!
				handleReceived(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_CALLBACK:
				handleCallback(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_CALL:
				handleCall(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_SEND:
				handleSend(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_SPEAK_PLAY:
				handleSpeakPlay();  keepHint = false;// handlePlay
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_SPEAK_STOP:
				handleSpeakStop();  keepHint = false;// handleStop
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_VOICE_REC:
				handleVoiceRecord();  keepHint = false;// handleMic
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_CTRL:
				handleCtrl(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_ALT:
				handleAlt(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_MENU:
				handleMenu(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_INPUT:
				handleInput(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_EMOJI:
				handleEmoji(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_USERSTRINGS:
				handleUserStrings(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_123:
				handle123();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_ABC:
				handleABC();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_SHIFTDOWN:
				handleShiftDown();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_LAUNCH:
				handleLaunch(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_REPEAT:
				handleRepeat(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_SETTINGS:
				handleSettings();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_KEYPAD_RIGHT:
				handleKeypadRight();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_KEYPAD_LEFT:
				handleKeypadLeft();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_KEYPAD_CENTER:
				handleKeypadCenter();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_KEYPAD_WIDE:
				handleKeypadWide();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_KEYPAD_HIDE:
				handleKeypadHide();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_FN:
				handleFn(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F1:
				handleF1(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F2:
				handleF2(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F3:
				handleF3(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F4:
				handleF4(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F5:
				handleF5(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F6:
				handleF6(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F7:
				handleF7(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F8:
				handleF8(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F9:
				handleF9(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F10:
				handleF10(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F11:
				handleF11();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_F12:
				handleF12(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_META:
				handleMeta(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_FILE:
				handleFile(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_NOTES:
				handleNotes(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_SAVE:
				handleSave(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_APPEND:
				handleAppend(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_OPEN:
				handleOpen(); keepHint = false;
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_GET:
				handleGet(); keepHint = false;
				break;

			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_UL:
				handlePadUL();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_CW:
				handlePadCW();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_UR:
				handlePadUR();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_B:
				handlePadB();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_U:
				handlePadU();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_S:
				handlePadS();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_L:
				handlePadL();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_R:
				handlePadR();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_5R:
				handlePad5R();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_3R:
				handlePad3R();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_LL:
				handlePadLL();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_CN:
				handlePadCN();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_D:
				handlePadD();
				break;
			case CMBOKeyboard.CUSTOM_KEYCODE_PAD_LR:
				handlePadLR();
				break;

		}

		if (isTopBar) {
			if (!keepHint) { // Clear text on topBar
				candidates.addLetter(" "); // Clear candidate hint
			} else { //
				// updateTopBarText(); // Opt 1
				topBarTimer.start(); // Opt 2: allow time for display to settle
			}
		}

	}

	private void handlePadUL() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("0");
		CMBOKeyboardApplication.getApplication().getPreferences().setPadHeight("10");
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			CMBOKeyboardApplication.getApplication().getPreferences().setLandscapeColumns("3"); // single landscape pad
		}
		Log.d("-PadXX", "*** Service: Handle PadUL");
	}
	private void handlePadCW() {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			CMBOKeyboardApplication.getApplication().getPreferences().setLandscapeColumns("6"); // Landscape double pad
		} else {
			CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("1");
		}
		Log.d("-PadXX", "*** Service: Handle PadCW");
	}
	private void handlePadUR() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("2");
		CMBOKeyboardApplication.getApplication().getPreferences().setPadHeight("10");
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			CMBOKeyboardApplication.getApplication().getPreferences().setLandscapeColumns("3"); // single landscape pad
		}
		Log.d("-PadXX", "*** Service: Handle PadUR");
	}
	private void handlePadB() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadHeight("0");
		Log.d("-PadXX", "*** Service: Handle PadB");
	}
	private void handlePadU() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadHeight("10");
		Log.d("-PadXX", "*** Service: Handle PadU");
	}
	private void handlePadS() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadHeight("10");
		Log.d("-PadXX", "*** Service: Handle PadS");
	}
	private void handlePadL() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("0");
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			CMBOKeyboardApplication.getApplication().getPreferences().setLandscapeColumns("3"); // single landscape pad
		}
		Log.d("-PadXX", "*** Service: Handle PadL");
	}
	private void handlePadR() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("2");
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			CMBOKeyboardApplication.getApplication().getPreferences().setLandscapeColumns("3"); // single landscape pad
		}
		Log.d("-PadXX", "*** Service: Handle PadR");
	}
	private void handlePad5R() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadType("0");
		Log.d("-PadXX", "*** Service: Handle Pad5R");
	}
	private void handlePad3R() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadType("10");
		Log.d("-PadXX", "*** Service: Handle Pad3R");
	}
	private void handlePadLL() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadHeight("20");
		CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("0");
		Log.d("-PadXX", "*** Service: Handle PadLL");
	}
	private void handlePadCN() {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			CMBOKeyboardApplication.getApplication().getPreferences().setLandscapeColumns("3"); // single landscape pad
		}
			CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("3");
		Log.d("-PadXX", "*** Service: Handle PadCN");
	}
	private void handlePadD() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadHeight("20");
		Log.d("-PadXX", "*** Service: Handle PadD");
	}
	private void handlePadLR() {
		CMBOKeyboardApplication.getApplication().getPreferences().setPadHeight("20");
		CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("2");
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			CMBOKeyboardApplication.getApplication().getPreferences().setLandscapeColumns("3"); // single landscape pad
		}
		Log.d("-PadXX", "*** Service: Handle PadLR");
	}

	private void handleMore() {
		Log.d("-Fn", "*** Service: Handle More");
		//showToast("Not implemented yet", true);
	}

	private void handleMeta() {
		Log.d("-Fn", "*** Service: Handle Meta");
		showToast("Meta not implemented yet", true);
	}


	private void handleNotes() { // Show activity_notes on a 'yellow sticker'

		//String text = "Notes:\nThis is notes from Service.";
        if (notesVisible) return;

		//LayoutInflater inflater = (LayoutInflater)
		//		getSystemService(LAYOUT_INFLATER_SERVICE);
		//View notesView = inflater.inflate(R.layout.activity_notes, null);

		Intent notesIntent = new Intent(this, CMBONotes.class);
		notesIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(notesIntent);
		notesVisible = true;

		sendTextToNotesActivity(savedNotes); // Use broadcast

	}

	/*
	private void showNotes2() {
		LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				//WindowManager.LayoutParams.TYPE_INPUT_METHOD
				//		|
				//WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT //,
				//WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				//PixelFormat.TRANSLUCENT
		);

		params.gravity = Gravity.END | Gravity.TOP;
		View myview = li.inflate(R.layout.activity_notes, null);

		wm.addView(myview, params);
	}
	*/
	private void handleFile() {
		Log.d("-Fn", "*** Service: Handle File");
		showToast("File not implemented yet", true);
	}

	private void handleSave() { // Save activity_notes replacing old ones

		String notesHeader = getString(R.string.flashtext_notes);

		if (readyToSaveText) {
			try {
				//notesToSave = notesHeader + ":\n" + currentText();
				notesToSave = "\n" + currentText();
			} catch (Exception e) {
				Log.d("-NOTES", "*** SERVICE: Save Notes error: " + e);
			}
			CMBOKeyboardApplication.getApplication().getPreferences().saveNotes(notesToSave);
			savedNotes = notesToSave;
			showCustomToast(getString(R.string.save_text_done), "save", false);

			Log.d("-NOTES", "*** SERVICE: Handle Save, String = " + notesToSave);
		} else {
			showCustomToast(getString(R.string.save_text_confirm), "save", false);
			readyToSaveText = true; // this is set to false in CMBOKeyboard.java
		}
	}

	private void handleAppend() {

			try {
				textToSave = currentText();
				//stringSaved = CMBOKeyboardApplication.getApplication().getPreferences().getNotes();
			} catch (Exception e) {
				Log.d("-NOTES", "*** SERVICE: Save Notes error: " + e);
			}
			if (!textToSave.equals("")){
				notesToSave = savedNotes + "\n" + textToSave;
				CMBOKeyboardApplication.getApplication().getPreferences().saveNotes(notesToSave);
				savedNotes = notesToSave;
				showCustomToast(getString(R.string.append_text_done), "save", false);
			} else {
				showCustomToast(getString(R.string.save_text_empty), "save", false);
			}
			Log.d("-NOTES", "*** SERVICE: Handle Append, String = " + textToSave);

	}

	private void handleGet() {
		handleOpen();
	}

	private void handleOpen() {
		String txt = "";
		if (savedNotes.length() > 8) {
			txt = "\n" + savedNotes;
			onText(txt);
		} else {
			showToast(getString(R.string.toast_notes_blank), true);
		}
		Log.d("-NOTES", "*** SERVICE: Handle Open, String = " + savedNotes);
	}


	// These are now handled in Keyboard.java: anyFnPressed()
	private void handleFn() {
		Log.d("-Fn", "*** Service: Handle Fn");
	}
	private void handleF1() {
		Log.d("-Fn", "*** Service: Handle F1");
	}
	private void handleF2() {
		Log.d("-Fn", "*** Service: Handle F2");
	}
	private void handleF3() {
		Log.d("-Fn", "*** Service: Handle F3");
	}
	private void handleF4() {
		Log.d("-Fn", "*** Service: Handle F4");
	}
	private void handleF5() {
		Log.d("-Fn", "*** Service: Handle F5");
	}
	private void handleF6() {
		Log.d("-Fn", "*** Service: Handle F6");
	}
	private void handleF7() {
		Log.d("-Fn", "*** Service: Handle F7");
	}
	private void handleF8() {
		Log.d("-Fn", "*** Service: Handle F8");
	}
	private void handleF9() {
		Log.d("-Fn", "*** Service: Handle F8");
	}
	private void handleF10() {
		Log.d("-Fn", "*** Service: Handle F10");
	}
	private void handleF11() {
		Log.d("-Fn", "*** Service: Handle F11");
	}
	private void handleF12() {
		Log.d("-Fn", "*** Service: Handle F12");
	}

	private void handleKeypadHide() { // Hide keyboard, hideKeyboard
		this.hideWindow(); // for non static method only
	}
	private void handleKeypadHideDelayed() { // Hide keyboard, hideKeyboard
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			//@Override
			public void run() {
				handleKeypadHide(); // for non static method only
			}
		}, 1500); // 1.5 s delay
	}

	// 0 = left, 1/3 = center (wide/narrow), 2 = right
	private void handleKeypadRight() {
		showToast("Move keypad right", true);
		CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("2");
	}
	private void handleKeypadLeft() {
		showToast("Move keypad left", true);
		CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("0");
	}
	private void handleKeypadCenter() {
		showToast("Move keypad to center", true);
		CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("3");
	}
	private void handleKeypadWide() {
		showToast("Keypad wide in center", true);
		CMBOKeyboardApplication.getApplication().getPreferences().setPadPosition("1");
	}

	public void handleSettings() {
		String toastMessage = "Settings";
		Intent i = new Intent(view.getContext(), CMBOPreferencesActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 2019-10-14 put back

		controller.setCtrl(false);
		Log.d("-SETTINGS", "*** Service: Settings called from KeyboardService (Ctrl + Lang)");

		try {
			startActivity(i);
			//view.getContext().startActivity(i);
			showToast(toastMessage, true); // ok
		} catch (Exception e) {
			toastMessage = e.getMessage();
			showToast(toastMessage, true); // error
		}
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			handleKeypadHideDelayed(); // hideKeyboard is necessary for Landscape (kbd SHOW_FORCED has been used)
		}
	}

	private void handleRepeat() {
		// just a dummy, needed though because Repeat combo is really pressed
	}

	private void handleLaunch() {

		String appString = CMBOKeyboardApplication.getApplication().getPreferences().getAppToLaunch(); // One way of doing this
		String appPackage = "";

		if (appString.equals("") || (appString.length() < 3)) {
			appString = getString(R.string.settings_launch_default); // defined in strings.xml
		}

		showToast(getString(R.string.toast_launch_string), true);

		// First example is TextMAXER
		String appName = appString; //"TextMAXER"; // "NotePad" (AK NotePad), "Calcul" (Calculator)...

		if (launchApplication(appName, appPackage)) {
			//return;  // may be needed later
		} else {
			showToast("Application " + appName + " not found.\nYou might want to install it.", true);
		}

		// Second options...
	}

	private void handleArrow(int primCode) {

		// Take care of not getting lost outside text!
		InputConnection ic = getCurrentInputConnection();

		String leftChar = ""; String rightChar = "";

		Log.d("-ARROWS", "*** HandleArrow(" + primCode + ")");

		try {
			leftChar = ic.getTextBeforeCursor(1, 0).toString(); // 1 character, flags = 0 (or GET_TEXT_WITH_STYLES)
			rightChar = ic.getTextAfterCursor(1, 0).toString();
		} catch (Exception e) {
			return; // to avoid NullPointerException (in autorepeat?) Fixed 2018-04-29 for version 3.4
		}

		int startOfSelection = selStart;
		int endOfSelection = selEnd;

		boolean shiftMode = false;
		int offs = CMBOKeyboardApplication.getApplication()
				.getKeyboard().getOffset();
		if (offs == CMBOKey.AUXSET_SHIFT_MODIFIER || offs == CMBOKey.SHIFT_MODIFIER) {
			shiftMode = true;
		}

		int amount = 1;

		switch (primCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (!leftChar.equals("")) { // on edge left?
					if (shiftMode) {
						try {
							ic.setSelection(startOfSelection - amount, endOfSelection);
						} catch (Exception e) {
							showToast("Error selecting text, sorry: " + e, true);
						}
					} else {
						try {
						keyUpDown(primCode);
						} catch (Exception e) {
							showToast("Error, sorry: " + e, true);
						}
					}
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (!rightChar.equals("")) { // on edge right
					if (shiftMode) {
						try {
							ic.setSelection(startOfSelection + amount, endOfSelection);
						} catch (Exception e) {
							showToast("Error selecting text, sorry: " + e, true);
						}
					} else {
						try {
						keyUpDown(primCode);
						} catch (Exception e) {
							showToast("Error, sorry: " + e, true);
						}
					}

				}
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				if (!leftChar.equals("")) {
					keyUpDown(primCode); // no selection of text possible
				}
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (!rightChar.equals("")) {
					keyUpDown(primCode); // no selection of text possible
				}
				break;
			default:
				break;
		}
		updateTopBarText();

	}

	private void handle123() {

		//showToast("handle123() not ready", true);
	}

	private void handleABC() {

		//showToast("handleABC() not ready", true);
	}

	private void handleShiftDown() {

		//showToast("handleShiftDown() not ready", true);
	}


	//private KeyboardOutput output;

	private void handleUserStrings() {

		String userString = CMBOKeyboardApplication.getApplication().getPreferences().getUserStrings(); // One way of doing this

		if (userString.equals(""))
			userString = getString(R.string.settings_usertext_default); // defined in strings.xml
		onText(userString);
		showToast(getString(R.string.toast_user_string), true);

	}

	private void handleEmoji() {
		// handled by CMBOKeyboard.java based on chord (= 119)

	}


	private void selectInputMethod() { // for activating voice input or other keyboard
		InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showInputMethodPicker();
		} else {
			Toast.makeText(this, getString(R.string.not_possible_im_picker), Toast.LENGTH_LONG).show();
		}
	}


	private void handleInput() {

		selectInputMethod(); // just this for now, to get the keyboard list and choose voice input from there

	}


	private void handleMenu() {

	}

	private void handleCtrl() {
		if (controller.isCtrlDown()) {
			controller.setCtrl(false);
		} else {
			controller.setCtrl(true);
		}

	}

	private void handleAlt() {
		if (controller.isAltDown()) {
			controller.setAlt(false);
		} else {
			controller.setAlt(true);
		}

	}


	private void handleSearch() {

		searchCurrentText(); // clipboard involved no more
	}


	private void searchCurrentText() {

		String searchText = "";
		String query = "";

		String currentLangISO = CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.ISO_639_1); // ISO code of lang

		try {
			searchText = currentText(); // might throw an error?
			query = URLEncoder.encode(searchText, "utf-8");
		} catch (Exception e) {
			query = "";
			showToast(e.getMessage(), true);
		}

		Intent browserIntent = null;
		String toastMessage = "";

		if (!controller.isAltMode()) {
			toastMessage = getString(R.string.google_search); // Searching+these+words
			browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com/search?q=" + query));
			browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		} else {
			controller.setAltMode(false);
			toastMessage = getString(R.string.flashtext_search); // Search Wikipedia
			String queryWiki = query.replace("+", "_"); // Searching_these_words
			browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + currentLangISO + ".m.wikipedia.org/wiki/" + queryWiki));
			browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}

		try {
			startActivity(browserIntent);
			showToast(toastMessage, true); // ok
		} catch (Exception e) {
			toastMessage = e.getMessage();
			showToast(toastMessage, true); // error
		}

	}


	private void handleTranslate() { // Translate to language *not* active, since 2020-04 (v5.3 up)

		String transLangISO = "";
		String transLang = "";
		String transLangNative = "";

		boolean mainLangActive = CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getMainLanguageActive();
		if (mainLangActive) {
			transLangISO = CMBOKeyboardApplication.getApplication()
					.getLayoutManager().getCurrentLayout2().getValue(Layout.Metadata.ISO_639_1); // ISO code of lang
			transLang = CMBOKeyboardApplication.getApplication()
					.getLayoutManager().getCurrentLayout2().getValue(Layout.Metadata.NAME); //  Lang real name (in English)
			transLangNative = CMBOKeyboardApplication.getApplication()
					.getLayoutManager().getCurrentLayout2().getValue(Layout.Metadata.DESCRIPTION); //  Lang real name in native lang
		} else {
			transLangISO = CMBOKeyboardApplication.getApplication()
					.getLayoutManager().getCurrentLayout1().getValue(Layout.Metadata.ISO_639_1); // ISO code of lang
			transLang = CMBOKeyboardApplication.getApplication()
					.getLayoutManager().getCurrentLayout1().getValue(Layout.Metadata.NAME); //  Lang real name (in English)
			transLangNative = CMBOKeyboardApplication.getApplication()
					.getLayoutManager().getCurrentLayout1().getValue(Layout.Metadata.DESCRIPTION); //  Lang real name in native lang
		}
		String fullText = getCurrentInputConnection().getExtractedText((new ExtractedTextRequest()),0).text.toString();

		if (fullText.equals("")) { // if no text, translate from clipboard
			showToast(getString(R.string.toast_translate_from_clipboard), true);
			fullText = clipboardText();
			if (fullText.length() > 3800) { // too long
				fullText = fullText.substring(0, 3800);
				showToast(getString(R.string.toast_translate_text_too_long), true);
			}
		}

		String toastMessage = getString(R.string.translate_to) + " " + transLangNative + "/" + transLang;

		String query = "";

		try {
			query = URLEncoder.encode(fullText, "utf-8");
		} catch(Exception e) {
			showToast(e.getMessage(), true);
			return;
		}

		String urlString = "https://translate.google.com/#auto/" + transLangISO + "/" + query;
		if (controller.isAltMode()) {
			controller.setAltMode(false);
			String queryWiki = query.replace("+", "_");
			urlString = "https://" + transLangISO + ".m." + "wiktionary.org/wiki/" + queryWiki;
		}

		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
		browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		try {
			startActivity(browserIntent);
			showToast(toastMessage, true); // ok
		} catch (Exception e) {
			toastMessage = e.getMessage();
			showToast(toastMessage, true); // error
		}
	}


	private void handleCall() { // Move to phone with the digits dialled

		String phone = "";
		String dispText = "";

		try {
			dispText = currentText(); // copy current text but restore clipboard to original
		} catch(Exception e) {
			dispText = "";
			showToast(e.getMessage(), true);
		}

		if ((dispText.length() < 25) && (dispText.matches("[0-9+*# ]+") && dispText.length() > 2)) {
			// Check phone number validity
			phone = dispText;
		} else {
			phone = "";
		}

		if (this.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_TELEPHONY)) {
			try {
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 2019-10-14
				this.startActivity(intent); // added "this.", too
			} catch (Exception e) {
				showToast(e.getMessage(), true);
			}

		}  else{
			showToast(getString(R.string.no_sms_capability), true);
		}
	}


	private void checkSpeakWord(CharSequence txt) { // typed letter or shortcut
		// 0 = no speaking while typing, 1 = say each letter, 2 = say each word
		int spType = CMBOKeyboardApplication.getApplication().getPreferences().getSpeakType();
		if ((spType == 0) || (spType == 2)) return;
		if (view.isAutorepeat()) return;
	    speakText(txt.toString());
    }

	private void speakText(String textToSpeak) {
		if (!textToSpeechAvailable) { // Speech not available
			showToast(getString(R.string.toast_speech_not_available), true);
			return;
		}
		if (!isTTSLanguageAvailable) {
			showToast(getString(R.string.toast_speech_data_not_available), true);
		 return;
		}
		if (textToSpeak.length() > 3800) { // cut if too long
			showToast(getString(R.string.toast_speech_text_too_long), true);
			textToSpeak = textToSpeak.substring(0, 3800);
		}
		if (!textToSpeak.equals("")) {
			textToSpeech1.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	private void handleSpeakStop() {
		Log.i("-SPEECH", "Handle Speech Stop");
		// item 116 = _Stop or Play
		CMBOKeyboardApplication.getApplication().getLayoutManager().changeCharacterMapItem(116, "_Play");

		if (CMBOKeyboardApplication.getApplication().getPreferences().getSpeakType() == 0) return;
		//speakText("");
		textToSpeech1.stop();
	}

	private void handleVoiceRecord() {

		if (controller.isAltMode()) {
			handleCamTextRead();
			controller.setAltMode(false);
			return;
		}

		// TODO:
		Log.i("-SPEECH", "Handle Speech Recording ENTER");
		Intent i = new Intent(this, CMBOSpeechRecognizer.class);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // tested 2019-03-13
		//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // tested 2019-05-24, not good! No return to WhatsApp etc.

		this.startActivity(i);
		Log.i("-SPEECH", "Handle Speech Recording EXIT");
	}

	private void handleCamTextRead() {
		showToast("CamTextRead not ready yet.", true);
	}


	private void handleSpeakPlay() { // Speech is OFF
		if (CMBOKeyboardApplication.getApplication().getPreferences().getSpeakType() == 0) { // speech off
			showToast(getString(R.string.toast_select_speaktype), true);
			return;
		}
		if (!textToSpeechAvailable) { // Speech not available
			showToast(getString(R.string.toast_speech_not_available), true);
			return;
		}
		if (!isTTSLanguageAvailable) {
			showToast(getString(R.string.toast_speech_data_not_available), true);
			return;
		}
		if (CMBOKeyboardApplication.getApplication().getPreferences().getSpeakType() == 1) return; // single letters only



		// showCustomToast (String toastText, String iconType)
		if (!readyToPlayText) {
			showCustomToast(getString(R.string.play_text_confirm), "play", false);
			readyToPlayText = true; // this is set to false in CMBOKeyboard
			return;
		}

		Log.i("-SPEECH", "Starting Text-To-Speech...");

		// item 116 = _Stop or Play
		playTimer.start(); // Change Play button into Stop button:
		CMBOKeyboardApplication.getApplication().getLayoutManager().changeCharacterMapItem(116, "_Stop");

		// Speak starting from cursor position except when at end (then start from beginning)
		String fullText = getCurrentInputConnection().getExtractedText((new ExtractedTextRequest()),0).text.toString();

		if (fullText.equals("")) { // if no text, play from clipboard
			showToast(getString(R.string.toast_speech_from_clipboard), true);
			fullText = clipboardText();
			if (fullText.length() > 3800) { // too long
				fullText = fullText.substring(0, 3800);
				showToast(getString(R.string.toast_speech_text_too_long), true);
			}
			Log.i("-SPEECH", "Speaking clipboard text...");
			textToSpeech1.speak(fullText, TextToSpeech.QUEUE_FLUSH, null);
			return;
		}
		// play from input connection:
		String textBasedOnCursor = getCurrentInputConnection().getTextAfterCursor(fullText.length(), 0).toString();
		if(textBasedOnCursor.equals("")) { // if cursor at end, use full text
			textBasedOnCursor = getCurrentInputConnection().getTextBeforeCursor(fullText.length(), 0).toString();
		}
		// finally, is text too long (> 3800 characters)?
		if (textBasedOnCursor.length() > 3800) { // to long
			textBasedOnCursor = textBasedOnCursor.substring(0, 3800);
			showToast(getString(R.string.toast_speech_text_too_long), true);
		}

		Log.i("-SPEECH", "Speaking text based on text shown and the cursor: \n" + textBasedOnCursor);
		textToSpeech1.speak(textBasedOnCursor, TextToSpeech.QUEUE_FLUSH, null);
	}

	private void initTtsEngine() {

		Log.i("-SPEECH", "(init) Initialize TTS engine and check if available.");

			textToSpeech1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
			//@Override
			public void onInit(int status) {
				if(status != TextToSpeech.ERROR) {
					textToSpeechAvailable = true;
					Log.i("-SPEECH", "(init) ...TTS engine is available.");
				} else {
					Log.i("-SPEECH", "(init) ...TTS engine is NOT available!!!");
				}
			}
		});
	}


	private String getCurrentLanguageTag(){ // e.g."en-US" from JSON extra
		// JSON Metadata: extra = ["-","dev","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]
		// JSON Metadata: extra = ["-","-","en","US","-","-","-","-","-","-","-","-","-","-","-","-","-"]
		// languageTag = en-US
		String result = "";
		String extra = CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.LAYOUT_EXTRA);
		String[] ary = extra.split("\",\""); // Note: Not allowed to have any space between items? Just a comma?
		result = ary[2] + "-" + ary[3];

		//Log.i("-SPEECH", "(getCurrentLanguageTag()) - extra = " + extra + ", result = " + result);
		return result;
	}

	public String getCurrentLanguageISO() { // e.g. "en" from JSON extra
		// JSON Metadata: extra = ["-","dev","-","-","-","-","-","-","-","-","-","-","-","-","-","-","-"]
		// JSON Metadata: extra = ["-","-","en","US","-","-","-","-","-","-","-","-","-","-","-","-","-"]
		// languageTag = en-US
		String result = "";
		String extra = CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.LAYOUT_EXTRA);
		String[] ary = extra.split("\",\""); // Note: Not allowed to have any space between items? Just a comma?
		result = ary[2];
		//Log.i("-SPEECH", "(getCurrentLanguageISO()) - extra = " + extra + ", result = " + result);
		return result;
	}

	public String getCurrentCountryISO() { // e.g. "US" from JSON extra
		String result = "";
		String extra = CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.LAYOUT_EXTRA);
		String[] ary = extra.split("\",\""); // Note: Not allowed to have any space between items? Just a comma?
		result = ary[3];
		//Log.i("-SPEECH", "(getCurrentCountryISO()) - extra = " + extra + ", result = " + result);
		return result;
	}


	private void initSpeechLanguage() {

		//Log.i("-SPEECH", "Start initSpeechLanguage()");
		// Some timing problem may exist here. Too soon to start browsing locales?

		Log.i("-SPEECH", "(initSpeechLang) Initialize Speech Language (layout JSONs loaded by now). TTS available now = "
				+ textToSpeechAvailable);

		// This action is too early after app is started. getCurrentLayout() cannot
		// come before loadAvailableLayouts() which is happening at the same time.
		// There will be a crash on some devices due to this here.
		// That is why a timer has been added before entering this init method.

		//String extra = CMBOKeyboardApplication.getApplication()
		//		.getLayoutManager().getCurrentLayout().getValue(Layout.Metadata.LAYOUT_EXTRA);
		//String[] ary = extra.split("\",\""); // Note: Not allowed to have any space between items? Just a comma?
		//languageTag = " - " + ary[2] + "-" + ary[3];
		String currentLangISO = getCurrentLanguageISO(); 	// e.g. "en"
		String currentCountryISO = getCurrentCountryISO(); 	// e.g. "US"

		Log.i("-SPEECH", "(initSpeechLang) Searching Locale for: " + currentLangISO + "_" + currentCountryISO);
		//showToast("Searching Locale for: " + currentLangISO, true);

		Locale localeFound = Locale.ENGLISH; // Default

		//Locale localeFound;
		Locale[] locales = Locale.getAvailableLocales();
		try {
			for (Locale locale : locales) {
				//Log.i("-SPEECH", "Existing Locale: " + locale.getLanguage() + " / " + locale.getCountry() + " / " + locale.getDisplayLanguage());
				// Crash "Couldn't find 3-letter country code for IC" if using line:
				//Log.i("-SPEECH", "Existing Locale: " + locale.getISO3Language() + " / " + locale.getISO3Country());
				if (locale.getLanguage().equals(new Locale(currentLangISO).getLanguage())
						&& locale.getCountry().equals(currentCountryISO)) {
					Log.i("-SPEECH", "(initSpeechLang) *** Found Locale " + locale.toString() + " = " + locale.getLanguage() + "_" + locale.getCountry());
					Log.i("-SPEECH", " Locale found (ISO3): " + locale.getISO3Language() + " / " + locale.getISO3Country());
					//showToast("Found Locale: " + locale.toString() + " = " + locale.getDisplayLanguage(), true);
					localeString = locale.toString();
					if (textToSpeech1.isLanguageAvailable(locale) >= LANG_AVAILABLE) {
						Log.i("-SPEECH", "(initSpeechLang) TTS data available for " + locale.toString());
						isTTSLanguageAvailable = true;
						//Locale localeFound = new Locale(locale.getISO3Language(), locale.getISO3Country()); // ("spa", "ESP"); // TEST: THIS WORKS !!!!!! = es_ES
						localeFound = locale;
					} else {
						isTTSLanguageAvailable = false;
						Log.i("-SPEECH", "(initSpeechLang) Missing data or TTS Language not supported");
						showToast("Voice data not accessible or Language not supported. TTS: " + textToSpeech1, true);
					}
				}
			} // for-next loop

			} catch (Exception e) {
				Log.i("-SPEECH-TIMER", "**** (initSpeechLang) Error initializing Locales.");
			}

		// Only after browsing all locales, set the intended locale
		if (isTTSLanguageAvailable) textToSpeech1.setLanguage(localeFound);

	}


	public String languageTag = "en-US";
	public String localeString = "en_US";
	private String previousLocaleString = "";

	private void initVoiceInputLang() {
		this.languageTag = getCurrentLanguageTag();
	}

	public void setVoiceInputLang() { // when changed
		this.languageTag = getCurrentLanguageTag();
		//if (textToSpeechAvailable) {
		//	Log.d("-SPEECH", "- (init) textToSpeechAvailable = true");
		//	initSpeechLanguage();
		//}
	}

	public String getLanguageTag() {
		return this.languageTag;
	}

	private boolean isTTSLanguageAvailable = false;

	public void setSpeechLang(String lang, String country) { // e.g. ("en", "US")

		isTTSLanguageAvailable = false;


		if (!textToSpeechAvailable) {
			Log.i("-SPEECH", "(setSpeechLang) TTS capability not available.");
			return;
		}


		Log.i("-SPEECH", "(setSpeechLang) ...");

		//showToast("Try finding locale: lang = " + lang + ", country = " + country, true);
		Locale[] locales = Locale.getAvailableLocales();

		Locale localeFound = Locale.ENGLISH; // Default
		// country = ""; // TEST!!!!
		try {
			for (Locale locale : locales) {
				Log.i("-SPEECH", "Existing Locale: " + locale.getLanguage() + " / " + locale.getCountry());
				//if (locale.getDisplayLanguage().equalsIgnoreCase(displayLang)  // e.g. English
				//if (locale.getLanguage().equalsIgnoreCase(lang)  // e.g. en
				if (locale.getLanguage().equals(new Locale(lang).getLanguage())
						&& locale.getCountry().equalsIgnoreCase(country)) {   // e.g. US
					//showToast("Found Locale: " + locale.toString(), true);
					Log.i("-SPEECH", "(setSpeechLang) *** Found Locale " + locale.toString() + " = " + locale.getLanguage() + "_" + locale.getCountry());
					// LANG_AVAILABLE = 0 // LANG_COUNTRY_AVAILABLE = 1	// LANG_COUNTRY_VAR_AVAILABLE = 2
					// LANG_MISSING_DATA = -1 // LANG_NOT_SUPPORTED = -2
					Log.i("-SPEECH", " Locale found (ISO3): " + locale.getISO3Language() + " / " + locale.getISO3Country());
					Log.i("-SPEECH", "(setSpeechLang) isLanguageAvailable for " + locale.toString()
							+ " = " + textToSpeech1.isLanguageAvailable(locale) + "  (OK values are: 0...3)");
					localeString = locale.toString();
					if (textToSpeech1.isLanguageAvailable(locale) >= LANG_AVAILABLE) {
						Log.i("-SPEECH", "(setSpeechLang) TTS data available for " + locale.toString());
						isTTSLanguageAvailable = true;
						localeFound = locale;
						//localeFound = new Locale("swe", "SWE", ""); // TEST!!!!!
					} else {
						isTTSLanguageAvailable = false;
						Log.i("-SPEECH", "(setSpeechLang) Missing data or Language not supported");
						//showToast("Missing Speech data or Language not supported/available", true);
					}
				}
			} // for-next loop

			} catch (Exception e) {
			Log.i("-SPEECH", "**** (setSpeechLang) Error browsing Locales.");
			}

		// Only after browsing all locales, set the intended locale
		if (isTTSLanguageAvailable)  {
			textToSpeech1.setLanguage(localeFound);
		} else {
			Log.i("-SPEECH", "(setSpeechLang) Locale not found on list.");
		}

		// consisits of  (ISO 639-2 language code, ISO 3166 country code, ...):
		//textToSpeech1.setLanguage(new Locale("hin", "IND", "variant"));
		//textToSpeech1.setLanguage(new Locale("eng", "USA", "variant"));
		//textToSpeech1.setLanguage(Locale.ENGLISH);
		// getISO3Language, getISO3Country give 3-letter codes
		// getLanguage gives a 2-letter ISO 639 code which is not a stable standard
		// Guide, use this form:
		// if (locale.getLanguage().equals(new Locale("he").getLanguage()))
		// ... because can be "he" (ISO) or "iw" (Android) for Hebrew

	}


	private void handleMicInputMethod() {
		try {
			InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
			final IBinder token = this.getWindow().getWindow().getAttributes().token;
			//imm.setInputMethod(token, LATIN);
			//imm.getEnabledInputMethodList();
			imm.switchToLastInputMethod(token);
		} catch (Throwable t) { // java.lang.NoSuchMethodError if API_level<11
			Log.e(TAG,"cannot set the previous input method:");
			t.printStackTrace();
		}
	}

	private void handleReceived() { // Not used any more. Should not be used (SMS).


		showToast(getString(R.string.checking_received_smss), true);

		if (this.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_TELEPHONY)) {
			try {
			// Option 1:
				String appName = "Messages"; //"TextMAXER"; // "NotePad" (AK NotePad), "Calcul" (Calculator)...
				String appPackage = "";
				// SMSs = "Messages", FB Messenger = "Messenger"

				if (launchApplication(appName, appPackage)) {
					//return;  // may be needed later
				} else {
					// showToast("Application " + appName + " not found.\nYou might want to install it.", true);
					handleReceivedOption2();
				}


			} catch (Exception e) { // Added June 2017 because Android 7.1 throws error here
				Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
				toast.show();
				Log.d("CMBO", "ERROR: handleReceived() " + e.getMessage() + " - (Service)");
			}

			// ----------------

		} else {
			showToast(getString(R.string.no_sms_capability), true);
		}


	}

	private void handleReceivedOption2() {

		if (this.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_TELEPHONY)) {
			try {
			// ------- Option 2:
				Intent receiveIntent = new Intent(Intent.ACTION_VIEW); // ACTION_VIEW
				receiveIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				receiveIntent.setData(Uri.parse("sms:"));
				startActivity(receiveIntent); // this line throws error in Android 7.1, ComboKey Plus app v.1.3

			} catch (Exception e) { // Added June 2017 because Android 7.1 throws error here
				Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
				toast.show();
				Log.d("CMBO", "ERROR: handleReceived() " + e.getMessage() + " - (Service)");
			}

		} else {
			showToast(getString(R.string.no_sms_capability), true);
		}


	}



	private void handleCallback() {
		showToast("No longer supported in this app", true);
	}

	private void handleSend() { // Not used any more. Should not be used (SMS).

		showToast(getString(R.string.send_as_sms), true);

		if (this.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_TELEPHONY)) {
			try {
				String sendText = currentText(); // clipboardText(); // clipboard.getText().toString(); // moved here from above
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);
				sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				sendIntent.setData(Uri.parse("sms:")); // optional to the line above (the line above was used in v1.3)
				// sendIntent.putExtra("address", "1234567890"); // not used in this app
				// sendIntent.setData(Uri.parse("sms:" + phoneNumber)); // optional to the line above
				sendIntent.putExtra("sms_body", sendText);
				startActivity(sendIntent); // this line throws an error in Android 7.1, app v.1.3
				} catch (Exception e) {// Added June 2017 because Android 7.1 throws error here
					Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
					toast.show();
				}
		} else {
			showToast(getString(R.string.no_sms_capability), true);
		}

	}

	private String clipboardText() {

		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		String text = "";
		try {
			ClipData abc = clipboard.getPrimaryClip();
			ClipData.Item item = abc.getItemAt(0);
			text = item.getText().toString();
		} catch (Exception e) {
			Log.d("-ComboKey", "Clipboard text retrieve error.");
			//text = "";
		}
		return text;
	}

	private void clearClipboard() {
		try {
			ClipboardManager clipService = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clipData = ClipData.newPlainText("", "");
			clipService.setPrimaryClip(clipData);
		} catch (Exception e) {
			Log.d("-ComboKey", "Clipboard clear error.");
		}
	}

	private void restoreClipboard(String prevClipText) {
		try {
			ClipboardManager clipService = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clipData = ClipData.newPlainText("", prevClipText);
			clipService.setPrimaryClip(clipData);
		} catch (Exception e) {
			Log.d("-ComboKey", "Clipboard restore error.");
		}
	}

	private String currentText() { 	// get the text being edited

		return getCurrentInputConnection().getExtractedText((new ExtractedTextRequest()),0).text.toString();
	}

	private void handleClear() {

		if (readyToClearText) {
			getCurrentInputConnection().performContextMenuAction(android.R.id.selectAll);

			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
			readyToClearText = false;

			controller.checkDevOffsetMode(); // checkDevanagariEtc
			if (!controller.statusDevanagariOn()) {
				controller.setKeyboardShift(); // Set Upper case only if not devanagari
			}

			controller.setKeyboardFnMode(false); // stop showing main key buttons on center column
			Log.d("-Fn", "(service) setKeyboardFnMode(false)");

		} else {
			showCustomToast(getString(R.string.delete_all_text_confirm), "clear", false);
			readyToClearText = true; // this is set to false in CMBOKeyboard
		}

	}




	private void handleLang() { // 2017-02-02

		boolean aMode = !CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getMainLanguageActive();

		String msgStart = ""; String msg = "";

		if (aMode) {
			aMode = false;
			// Translated note:
			msgStart = this.getResources().getString(R.string.main_language_selected) + ": ";
			// Translated language name:
			msg = view.translatedLanguageName(CMBOKeyboardApplication.getApplication()
					.getLayoutManager().getMainLanguageName());

			CMBOKeyboardApplication.getApplication()
					.getLayoutManager().setMainLanguageActive(true);
		} else {
			aMode = true;
			// Translated note:
			msgStart = this.getResources().getString(R.string.aux_language_selected) + ": ";
			// Translated language name:
			msg = view.translatedLanguageName(CMBOKeyboardApplication.getApplication()
					.getLayoutManager().getAuxLanguageName());

			CMBOKeyboardApplication.getApplication()
					.getLayoutManager().setMainLanguageActive(false);
		}

		controller.checkDevOffsetMode(); // checkDevanagariEtc

		showToast(msgStart + msg, true);

		Log.d("-DEV", "(service) DevanagariOn Status: " + controller.statusDevanagariOn());

		// ---- old:

		int offs = CMBOKeyboardApplication.getApplication()
				.getKeyboard().getOffset(); //for current offset in keyboard

		if (offs == CMBOKey.EMOJI_MODIFIER) {
			// come back from emojis area to start with
			if (aMode) {
				offs = CMBOKey.AUXSET_MODIFIER;
			} else {
				offs = CMBOKey.NONE;
			}
		}
		if (offs == CMBOKey.FN_MODIFIER) {
			// also come back from Fn area to start with
			if (aMode) {
				offs = CMBOKey.AUXSET_MODIFIER;
			} else {
				offs = CMBOKey.NONE;
			}
		}


		if (offs < CMBOKey.AUXSET_MODIFIER ) {
			offs = offs + CMBOKey.AUXSET_MODIFIER;

		} else {
			offs = offs - CMBOKey.AUXSET_MODIFIER;

		}
		// switch between main and aux language sets

		CMBOKeyboardApplication.getApplication()
				.getKeyboard().setLangOffset(offs, aMode);

		String speechLang = aMode ? CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getAuxLanguageName() : CMBOKeyboardApplication.getApplication()
				.getLayoutManager().getMainLanguageName();

		String currentLangISO = getCurrentLanguageISO();
		String currentCountryISO = getCurrentCountryISO();

		setSpeechLang(currentLangISO, currentCountryISO); // e.g. ("en", "US") or ("en", "")
	}

	private void handleCopy(boolean toastIt) {

		int selectionStart = selStart; // save for a while

		if (selStart == selEnd) {
			getCurrentInputConnection().performContextMenuAction(android.R.id.selectAll);
			showToast(getString(R.string.all_text_copied), toastIt); // Copy all
		} else {
			showToast(getString(R.string.copy), toastIt); // Copy selection
		}
		getCurrentInputConnection().performContextMenuAction(android.R.id.copy);

		// select none:
		getCurrentInputConnection().setSelection(selectionStart, selectionStart);
	}


	private void handlePaste() {
		getCurrentInputConnection()
				.performContextMenuAction(android.R.id.paste);
		showToast(getString(R.string.pasted_from_clipboard), true);
	}

	private void handleTab() {
		Log.d("-Fn", "(service) KEYCODE_TAB (system key code)");
			keyUpDown(KeyEvent.KEYCODE_TAB);
	}

	private void handlePgUp() {
		// move to first line
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PAGE_UP));
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_PAGE_UP));
		// move to beginning of line
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_HOME));

	}

	private void handlePgDn() {
		// move to (the beginning of) last line
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_PAGE_DOWN));
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_PAGE_DOWN));
		// move to end of (last) line
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END));
		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_END));

	}


	private void handleHome() { // start of text

		boolean devOn = CMBOKeyboardApplication.getApplication()
				.getKeyboard().statusDevanagariOn();

		if (devOn) {
			handlePgUp();
		} else {
			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME));
			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_HOME));
		}

	}

	private void handleEnd() { // end of text

		boolean devOn = CMBOKeyboardApplication.getApplication()
				.getKeyboard().statusDevanagariOn();

		if (devOn) {
			handlePgDn();
		} else {
			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END));
			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_END));
		}
	}

	private void handleSelectAll() {
		getCurrentInputConnection().performContextMenuAction(
				android.R.id.selectAll); // switchInputMethod or selectAll); // was: selectAll
	}

	private void handleWleft() { // Move cursor to beginning of previous word:

		// Option 2 - Find next space and stop there:
		String xChar = ""; // getCurrentInputConnection().getTextBeforeCursor(1, 0).toString(); // 1 charactes, flags = 0 (or GET_TEXT_WITH_STYLES)
		String prevXChar = "";
		//int startWL = selStart;
		int endWL = selEnd;
		int steps = 0;

		for (int i=1; i<32; i++) { // search step by step back until space found

			getCurrentInputConnection().setSelection(endWL - i, endWL - i);

			// The above method is slow (IPC round-trip), spend some time here
			try {
				Thread.sleep(10); // ms per step
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}

			try {
				//if (readyToClearText) { onKey("KEYCODE_DEL"); } // TODO: Send backSpace here somehow to clear word left
				xChar = getCurrentInputConnection().getTextBeforeCursor(1, 0).toString(); // Crash in v3.2
				prevXChar = getCurrentInputConnection().getTextAfterCursor(1, 0).toString();
			} catch (Exception e) {
				xChar = ""; // to avoid crash, 2018-04-29 for version 3.4
			}

			Log.d("-CURSOR", "xChar before cursor = |" + xChar + "|, prevChar = |" + prevXChar + "|, selection end = " + selEnd);

			if ((xChar.equals(" ") && !prevXChar.equals(" "))
                    || (xChar.equals(""))
                    || (endWL - i == 0) // selEnd might not be updated before next move
                    || (selEnd == 0)) {
				steps = i;
				break;
			}

		}

		// Space or start of text found, now move cursor to that position:
		Log.d("-CURSOR", "Moved cursor " + steps + " steps.");
	}



	private void handleWright() { // Move cursor to beginning of next word:

		// Option 2 - Find next space and stop there:
		String xChar = ""; // getCurrentInputConnection().getTextBeforeCursor(1, 0).toString(); // 1 charactes, flags = 0 (or GET_TEXT_WITH_STYLES)
		String prevXChar = "";
		//int startWL = selStart;
		int endWR = selEnd;
		int steps = 0;

		for (int i=1; i<32; i++) { // search step by step forward until space found

			getCurrentInputConnection().setSelection(endWR + i, endWR + i);

			// The above method is slow (IPC round-trip), spend some time here
			try {
				Thread.sleep(20); // ms per step
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}

			try {
				prevXChar = getCurrentInputConnection().getTextBeforeCursor(1, 0).toString();
				xChar = getCurrentInputConnection().getTextAfterCursor(1, 0).toString();
			} catch (Exception e) { // to eliminate crash here, 2018-04-29 for version 3.4
				xChar = "";
			}
			Log.d("-CURSOR", "xChar after cursor = |" + xChar + "|, prevXChar = |" + prevXChar + "|, selection end = " + selEnd);

			if ((!xChar.equals(" ") && prevXChar.equals(" "))
					|| (xChar.equals("")) // selEnd might not be updated before next move, so sleep 30 ms
				) {
				steps = i;
				break;
			}

		}

		// Space or end of text found, now move cursor to that position:
		Log.d("-CURSOR", "Moved cursor " + steps + " steps.");

	}

	private int selStart, selEnd;
	private int wordCandidateStart, wordCandidateEnd;

	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd, int candidatesStart,
			int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
				candidatesStart, candidatesEnd);

		selStart = newSelStart;
		selEnd = newSelEnd;
		Log.d("-CANDIDATES", "selStart/selEnd/ = "	+ selStart + "/" + selEnd);
	}

	private void handleDel() {

		if (selStart != selEnd) {
			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));

			return;
		}

		getCurrentInputConnection().deleteSurroundingText(0, 1);

	}

	public void onText(CharSequence text) {
		InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;
		ic.beginBatchEdit();
		ic.commitText(text, 1);
		ic.endBatchEdit();
		checkSpeakWord(text);
	}


	public void checkInputType() { // Check and set applicable text entry mode (web/email address, letters, numbers...)

		Log.d("-INPUT_TYPE", "INPUT TYPE - Checking input type ");

		if (isNumberInputMode()) { // automatically set number mode for entering numbers only
			controller.setKeyboardNumberMode();
			Log.d("-INPUT_TYPE", "Set Number mode.");
		} else { // All other cases, except devanagari, start in upper case letter mode?
			controller.checkDevOffsetMode(); // checkDevanagariEtc,  // Lower case. devanagari, email(web address:

			if (controller.statusDevanagariOn() || isEmailInputMode() || isUriInputMode() || isTerminalMode()) { // Lower case
				controller.resetKeyboardShift();
				Log.d("-INPUT_TYPE", "Set lower case (Devanagari, email, Uri or Terminal.");

			} else { // Set Upper case only if not devanagari, email, terminal or web address
				Log.d("-INPUT_TYPE", "Set Upper case if empty text entry field.");
				CharSequence currentText;
				try { // avoid Null object reference:
					if (getCurrentInputConnection() != null) {
						currentText = getCurrentInputConnection().getExtractedText(new ExtractedTextRequest(), 0).text;
					} else {
						controller.resetKeyboardShift();
						Log.d("-INPUT_TYPE", "...Set lower case (current input connection is null!).");
						return;
					}
				} catch (Exception e) { // avoid Null object reference:
					Log.d("-INPUT_TYPE", "INPUT TYPE - **** CharSequence extract error: " + e);
					controller.resetKeyboardShift();
					return;
				}
				if (currentText.equals("")) { // Upper case only if empty text area
					controller.setKeyboardShift();
					Log.d("-INPUT_TYPE", "...Set Upper case.");
				} else {
					controller.resetKeyboardShift();
					Log.d("-INPUT_TYPE", "...Set lower case.");
				}
			}
		}
	}

	public boolean isPasswordInputMode() { // password input
		int iT = getInputType() & 255;
		//Log.d("-INPUT_TYPE", "isPassword (18, 129, 225, 145)? inputType = " + getInputType() + ", inputType & 255 = " + iT);
		boolean result = ((iT == 18) || (iT == 129) || (iT == 225) || (iT == 145));
		candidates.setPasswordMode(result);
		return result;
		//  16, 128, 224  passwords (number, text, web text)
		// 18 = 12h: 10h(= TYPE_NUMBER_VARIATION_PASSWORD) + 2(= TYPE_CLASS_NUMBER)
		// 129 = 81h: 80h(= TYPE_TEXT_VARIATION_PASSWORD) + 1(= TYPE_CLASS_TEXT)
		// 225 = e1h: e0h(= TYPE_TEXT_VARIATION_WEB_PASSWORD) + 1(= TYPE_CLASS_TEXT)
		// 145 = 91h: 90h(= TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) + 1(= TYPE_CLASS_TEXT)
	}
	public boolean isNumberInputMode() { // use numbers
		int iT = getInputType() & 15;
		return ((iT == 2) || (iT == 3) || (iT == 4));
		// 2, 3, 4 = numbers (number, phone, date)
	}

	public boolean isEmailInputMode() { // use lower case
		int iT = getInputType() & 255;
		return ((iT == 33) || (iT == 225) || (iT == 161));
		// 33 = 21h: 20h = TYPE_TEXT_VARIATION_EMAIL_ADDRESS + 1h = TYPE_CLASS_TEXT
		// 225 = e1h: e0 =  TYPE_TEXT_VARIATION_WEB_PASSWORD + 1 = TYPE_CLASS_TEXT
		// 161 = a1h: a0 = TYPE_TEXT_VARIATION_WEB_EDIT_TEXT + 1 = TYPE_CLASS_TEXT
	}

	public boolean isUriInputMode() { // use lower case
		int iT = getInputType() & 255;
		return (iT == 17);
		//  17 = 11h: 10 = TYPE_TEXT_VARIATION_URI + 1 = TYPE_CLASS_TEXT
	}

	public int getInputType() {

		EditorInfo ei = getCurrentInputEditorInfo();
		String pkg = ei.packageName;
		if (ei == null || pkg == null)
			return 0; // <- This value also means terminal input mode for this app!
		Log.d("-KEY", "Input type = " + ei.inputType);
		return ei.inputType; // <- Usable for all other modes but terminal input mode
	}



	public boolean isTerminalMode() {

		int termMode = CMBOKeyboardApplication.getApplication()
				.getPreferences().getTerminalModeSetting();

		if (termMode == 1) {
			return true; // Forced ON
		}
		if (termMode == 2) {
			return false; // Forced OFF
		}

		// Mode is AUTO (== 0)
		EditorInfo ei = getCurrentInputEditorInfo();
		String pkg = ei.packageName;
		if (ei == null || pkg == null) {
			return false;
		}
		// Cannot use getInputType() here because 0 could also mean null pkg or null inputType
		// Log.d("-KEY", "Esc, Package = " + pkg + ", input type = " + ei.inputType);
		// showToast("Package = " + pkg + ", input type = " + ei.inputType, true); // for debug
		return (ei.inputType == 0); // Simply check if input type is 0 (hope this is enough)
		//return true; // TEST!!!
	}


	private boolean sendKeyWithCtrl(String keyString) { // A => send Ctrl+A etc.

		// keyString comes either as 'keyString' above or as 'ctrlKey' if keyString is empty

		if (!isTerminalMode()) return false;

		char c = 0;
		String ctrlKey = "";

		// try: char to ascii, minus 64, ascii to char
		if (keyString.equals("")) {
			ctrlKey = getCtrlKey(); // e.g. "A", "B", ... send this as Ctrl+A, Ctrl+B...
		} else {
			ctrlKey = keyString;
		}

		if (ctrlKey.equals("")) return false; // nothing to send

		// Functions converted to Ctrl+char, e.g. "A" = 'move to start of line' (= Ctrl+A)
		if (ctrlKey.length() == 1){ // only single characters are really sent with Ctrl

			c = ctrlKey.toUpperCase().charAt(0);

		} else if (ctrlKey.length() > 1) { // Others use Esc sequences etc.

			if (ctrlKey.equals("EscB")) { // Word left
				sendKeyChar((char) 27); // Esc
				sendKeyChar((char) "b".charAt(0));
				return true;
			} else if (ctrlKey.equals("EscF")) { // word right
				sendKeyChar((char) 27); // Esc
				sendKeyChar((char) "f".charAt(0));
				return true;
			} else if (ctrlKey.equals("CtrlIns")) { // copy
				sendKeyChar((char) 146); // not ready
				return true;
			} else if (ctrlKey.equals("ShiftIns")) { // paste
				sendKeyChar((char) 147); // not ready
				return true;
			} else if (ctrlKey.equals("Paste")) { // paste
				pasteToTerminal(); // working ok
				return true;
			} else if (ctrlKey.equals("PgUp")) { // Scroll up
				sendKeyChar((char) 27); sendKeyChar((char) 91); // ESC [
				onText("9s");
				return true;
			} else if (ctrlKey.equals("PgDn")) { // Scroll down
				sendKeyChar((char) 27); sendKeyChar((char) 91);// ESC [
				onText("9t");
				return true;
			} else
				// Add code here !!!!!
			// Deal with Esc + char and Alt+char...
			return false;
		} else {
			return false;
		}

		// Upper case minus 64 makes Ctrl version, any letter key (limit!)
		int asciiChar = CharToASCII(c);
		if (asciiChar == 32) asciiChar = 64; // Ctrl+space can replace Ctrl+@ to obtain null
		if (asciiChar == 63) asciiChar = 127 + 64; // DEL (127) can be obtained by Ctrl+? (? = 63)
		if ((asciiChar > 63) && (asciiChar < 128)) asciiChar += -64;
		int code = ASCIIToChar(asciiChar);
		Log.d("-KEY", "Terminal: character code = " + code + ", key = Ctrl+(int)" + (int) c);
		sendKeyChar((char) code);

		return true;
	}

	private void pasteToTerminal() {
		String pasteString = getClipData();
		Log.d("-KEY", "Terminal paste string = " + pasteString);
		onText(pasteString);
	}

	private String getClipData() {
		ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
		try {
			clipboardManager.getPrimaryClip();
			ClipData pasteData = clipboardManager.getPrimaryClip();
			ClipData.Item item = pasteData.getItemAt(0);
			return item.getText().toString();
		} catch (Exception e) {
			return "";
		}
	}

	private static int CharToASCII(final char character){
		return (int) character;
	}

	private static char ASCIIToChar(final int ascii){
		return (char) ascii;
	}

	// ------------------

	private boolean launchApplication(String appString, String packageString) {
		// One of the two app definition strings must fully correct
		Log.d("-PACKAGE", "Looking for Application name " + appString + ", package name " + packageString);

		if (appString.equals("")) {
			appString = "xxxxxxxxxzy";
		}
		//packageString = "com.google.android.apps.messaging"; // TEST !!!!

		String appStringLC = appString.toLowerCase();
		String packageStringLC = packageString.toLowerCase();

		ArrayList<HashMap<String,Object>> items = new ArrayList<HashMap<String,Object>>();

		final PackageManager pm = getPackageManager();
		List<PackageInfo> packs = pm.getInstalledPackages(0); // get all apps

		for (PackageInfo pi : packs) { // find what we want, with limits to real apps
			// Give either the app name fully correct plus part of app name
			// of just the complete package name
			if ((pi.applicationInfo.toString().toLowerCase().contains(appStringLC)
				&& pi.packageName.toLowerCase().equals(packageStringLC))
				|| (pi.applicationInfo.loadLabel(pm).toString().toLowerCase().equals(appStringLC)))
			{	Log.d("-PACKAGE", "Build App list - put on list.");
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("appName", pi.applicationInfo.loadLabel(pm));
				map.put("packageName", pi.packageName);
				items.add(map);
			}
			Log.d("-PACKAGE", "App list - packageName: " + pi.packageName + ", <|> appName: " + pi.applicationInfo.loadLabel(pm));

		}

		if(items.size()>=1){
			String packageNameFound = (String) items.get(0).get("packageName");
			String appNameFound = (String) items.get(0).get("appName");
			// onText(CharSequence text) // to screen (editText area)
			Log.d("-PACKAGE", "App FOUND - packageName: " + packageNameFound + ", appName: " + appNameFound);
			Intent i = pm.getLaunchIntentForPackage(packageNameFound);
			if (i != null)
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			return true;
		}
		else{
			// Application not found
			return false;
		}


	}

	// ------------------------

	private void showToast(String toastMessage, boolean inUse){

		//String toastMessage = "Not implemented yet";
		if (!inUse) return;

		boolean lengthLong = toastMessage.length() > 60;

		Toast toast;

		if (lengthLong) {
			toast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		} else {
			toast = Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		}


	}

	private void showCustomToast(String toastText, String iconType, boolean lengthLong) {
		// iconName: clear, play, ....

		LinearLayout layout = null;

		int layoutResource = R.layout.input;

		int padHeight = CMBOKeyboardApplication.getApplication().getPreferences().getPadHeight();

		switch (padHeight) { // it is not enough to set the input xml file only here, must be finalized later, now always same xml
			case 10:
				layoutResource = isTopBar ? R.layout.input_words : R.layout.input_smaller_higher;
				//layoutResource = R.layout.input_smaller_higher;
				break;
			case 0:
				layoutResource = isTopBar ? R.layout.input_words : R.layout.input;
				//layoutResource = R.layout.input;
				break;
			case 20:
				layoutResource = isTopBar ? R.layout.input_words : R.layout.input_smaller_lower;
				//layoutResource = R.layout.input_smaller_lower;
				break;
			default:
				layoutResource = isTopBar ? R.layout.input_words : R.layout.input;
				//layoutResource = R.layout.input;
				break;
		}


		layout = (LinearLayout) getLayoutInflater().inflate(layoutResource,	null);
		view = (CMBOKeyboardView) layout.findViewById(R.id.keyboard);

		LayoutInflater inflater = getLayoutInflater();
		View toastLayout = inflater.inflate(R.layout.custom_toast,
				(ViewGroup) layout.findViewById(R.id.custom_toast_container));

		TextView textView = (TextView) toastLayout.findViewById(R.id.toast_text);
		ImageView iconView = (ImageView) toastLayout.findViewById(R.id.toast_icon);

		if (iconType.equalsIgnoreCase("clear")){ // iconType = "clear"
				iconView.setImageResource(R.drawable.clear2ws);
		} else if (iconType.equalsIgnoreCase("play")){ //  iconType = "play"
			iconView.setImageResource(R.drawable.speak_play);
		} else {
			iconView.setImageResource(R.drawable.notes); // iconType = "save" ("activity_notes" for now)
		}

		textView.setText(toastText);

		Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.TOP, 0, 0);
		if (lengthLong) {
			toast.setDuration(Toast.LENGTH_LONG); // works opposite! Strange...
		} else {
			toast.setDuration(Toast.LENGTH_SHORT);
		}
		toast.setView(toastLayout);
		toast.show();

	}

}
