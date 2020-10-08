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

package com.combokey.basic.view;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.combokey.basic.CMBOKey;
import com.combokey.basic.CMBOKeyboard;
import com.combokey.basic.CMBOKeyboardActivity;
import com.combokey.basic.CMBOKeyboardController;
import com.combokey.basic.CMBOKeyboardService;
import com.combokey.basic.CMBOWordCandidates;
import com.combokey.basic.R;
import com.combokey.basic.view.theme.DrawableType;
import com.combokey.basic.view.theme.Theme;
import com.combokey.basic.view.theme.ThemeManager;
import com.combokey.basic.view.touchevent.SimpleTouchEvent;
import com.combokey.basic.view.touchevent.SimpleTouchEventListener;
import com.combokey.basic.view.touchevent.SimpleTouchEventProcessor;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.graphics.Color.BLACK;
import static com.combokey.basic.CMBOKeyboardApplication.getApplication;
import static java.lang.Boolean.TRUE;


public class CMBOKeyboardView extends ImageView implements
		SimpleTouchEventListener, OnLongClickListener {

	public static final int PADTYPE_THREE_R0W = 10;
	public static final int PADTYPE_FIVE_R0W = 0;
	// padHeight
	public static final int PADHEIGHT_HIGHER_SMALLER = 10;
	public static final int PADHEIGHT_TALL_DEFAULT = 0;
	public static final int PADHEIGHT_LOWER_SMALLER = 20;
	// padPosition
	public static final int PADPOSITION_LEFT = 0;
	public static final int PADPOSITION_WIDE_CENTER = 1;
	public static final int PADPOSITION_RIGHT = 2;
	public static final int PADPOSITION_NARROW_CENTER = 3;
	// landscapeColumns
	public static final int LANDSCAPECOLUMNS_STANDARD = 3;
	public static final int LANDSCAPECOLUMNS_THREE_ROW = 5; // not used any more
	public static final int LANDSCAPECOLUMNS_DOUBLE = 6;
	// tipType
	public static final int TIPTYPE_NONE = 0;
	public static final int TIPTYPE_MIDDLE_KEYS = 1;
	public static final int TIPTYPE_MAIN_KEYS = 2;
	public static final int TIPTYPE_MIXED_KEYS = 3;



	private static int tabletDevice = -1;
	private static int frSz = 0; // px // convertDpToPixel(float dp, Context context) => 10px for normal screen

	public static String TAG = "CMBOKeyboard";

	//private Canvas canvasInUse = null;
	private int previousOrientation = Configuration.ORIENTATION_PORTRAIT; // getResources().getConfiguration().orientation;; // Configuration.ORIENTATION_PORTRAIT; // ORIENTATION_UNDEFINED;

	private boolean service = false;
	private CMBOButton pressedKeyPrevious = null;

	private CMBOKeyboardController controller;
	private final CMBOKeyboard keyboard = getApplication().getCMBOManager().getKeyboard(); // 2018-01
	private final CMBOWordCandidates candidates = getApplication().getCMBOManager().getCandidates();
	private String abcIndicator = ""; //CMBOKeyboardApplication.getApplication()
			//.getLayoutManager().getAbcIndicator();
	public String modeState = "";
	private int slim = 0; // Slim sidePanel, Abc => Ab, English => Eng
	public static int soundIndex = 1; // do not put 0 here! sounds start at 1

	// To have black background behind all keys
	private int extendLeftPad = 0; // 0 = covers just the left column
	private int extendLeftPadLeft = 0; // 0 = covers just the left column
	private int extendRightPad = 2; // 0 = covers two columns on the right // 2 = covers also the sidePanel
	private int extendRightPadRight = 0;



	public CMBOKeyboardButtonLayout buttonLayout;
	private SimpleTouchEventProcessor simpleTouchEventProcessor;

	private Drawable statusButton; // really used for Abc button
	private Drawable languagesButton; //
	private Rect statusArea, flashArea, languageArea;
	private Rect backgroundArea = new Rect(0, 0, 0, 0);;
	private Rect topBarArea = new Rect(0, 0, 0, 0);;
	private Rect topBarAreaAdd = new Rect(0, 0, 0, 0);;
	private Rect topBarAreaMinus = new Rect(0, 0, 0, 0);;
	private boolean isTopBar = false; //
	//private boolean isSidePanel = true;
	boolean noPanel = getApplication().getPreferences().isSidePanelHidden();


	private Drawable backgroundImage;// = getResources().getDrawable(R.drawable.backgroundimage); // 2020-03
	private Drawable topBarImage; // 2020-02
	private Drawable topBarImageAdd; // 2020-03
	private Drawable topBarImageMinus; // 2020-03
	private String previousThemeName = ""; // 2017-09

	private boolean noSymbolsUpdate = false; // symbols on keys shall be updated?
	private String flashSymbol = "";
	//private String wordCandidate = "[word]";
	private boolean terminalMode = false; // follows the state of service.isTerminalMode()
	private boolean passwordMode = false; // follows the state of service.isPasswordInputMode()
	private boolean threeRowPad = false; // follows rows3


	private Paint
			black, mediumBlack, smallBlack,
			gray, mediumGray, smallGray,
			blue, mediumBlue, smallBlue,
			white, mediumWhite, smallWhite, smallWhitePlus, smallBlackPlus,
			modeText, modeTextYellow, modeTextMellowYellow, modeTextGreen, modeTextSmall, modeTextSmallGray,
			flashText, flashTextMedium, flashTextSmall,
			helpText, helpTextWhite, helpTextWhiteBig, helpTextMainButton,
			mirrorTextYellow, frame,
			blackNoShadow;

	public static boolean regenerateViewKludge = false; //true; // 2017: to regenerate Service view
	public static boolean debugInUse = false; // (set to false when app is ready) Alt+d turns debug on/off
	public static final boolean debug2InUse = false; //true; // (set to false when app is ready) Alt+d turns debug on/off
	public static boolean runningService = false;

	public static int previousPadPosition = 50;
	public static int previousPadHeight = 0;
	public static int previousPadType = -1;


	private boolean aMode = getApplication()
			.getKeyboard().getAuxMode();


	final float scale = getContext().getResources().getDisplayMetrics().density;
	private int swipeSpanX = 0;
	private int swipeSpanY = 0;


	public static int getScreenWidth() {
		return Resources.getSystem().getDisplayMetrics().widthPixels;
	}

	public static int getScreenHeight() {
		return Resources.getSystem().getDisplayMetrics().heightPixels;
	}


	public CMBOKeyboardView(Context context) {
		super(context);
	}


	private boolean hideSymbol = false; // some symbols can be dynamically hidden but are functional

	private boolean isHidden() {
		return hideSymbol;
	}
	public void setHidden(boolean hide) {
		hideSymbol = hide;
	}

	public CMBOKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);

		showToast("View started (View)", debugInUse);

		if (buttonLayout != null) { // Clear old buttons and touch areas
			buttonLayout.clearButtons();
		}

		invalidate();

		// ---- 2017 ---
		if (this.getContext().getClass() == CMBOKeyboardActivity.class) {
			showToast("Running Activity class (View)", debugInUse);
			runningService = false;
			Log.d("CMBO", "*** Running Activity class (View)");
		}

		else if (this.getContext().getClass() == CMBOKeyboardService.class) {
			showToast("Running Service class (View)", debugInUse);
			CMBOKeyboardService service = (CMBOKeyboardService) this
					.getContext();
			runningService = true;
			Log.d("CMBO", "*** Running Service now. (View)");
		}


		initButtons(context);

		mpCleanUp(); // avoid multiple pools in case of re-establishing view

		mpInitSounds();
		mpLoadSounds();

		repeatTimer.cancel(); // prevent autorepeat from running at resume or start

		setUpPaint();

		mapButtonStringsToImages();

		setWillNotDraw(false);

		this.isTopBar = getApplication().getPreferences().getDisplayWordCandidates();
		candidates.setIsTopBar(isTopBar);

	}

	// ============== sound ==================

	private boolean readyToPlaySound = false;
	private int soundQue = 0; // <== To fix missing sounds while typing fast.
	private boolean newKeyDown = false;

	// If you want to try mp and mp6 sound options, remove the comments far left (// and /*)
	// and set soundOption = 2
	// https://stackoverflow.com/questions/18459122/play-sound-on-button-click-android
	// https://developer.android.com/reference/android/media/MediaPlayer.html


	private final int soundOption = 1; // 1 = pool, 2 = mp

	// ------------------- sound pool option --------------------- TODO: Simplify playing sounds!!!

	private static SoundPool soundPool;
	private int sound1, sound2, sound3, sound4, sound5, sound6;
	private final int[] sIndex = {sound1, sound2, sound3, sound4, sound5, sound6};

	private final AudioManager audioManager = (AudioManager) this.getContext().getSystemService(AUDIO_SERVICE);

	public void cleanUp() {
		try {
			soundPool.release();
			soundPool = null;
			audioManager.unloadSoundEffects();
		} catch (Exception e) {
			// Throws an error in some cases (if sound has not been used before closing the app?)
		}
	}

	public void mpLoadSounds() {
		// Without poolMap: (the shorter the sound, the better behaviour when repeated fast)
		sound1 = soundPool.load(getApplication(), R.raw.keyw_low2_vol, 1); // space
		sound2 = soundPool.load(getApplication(), R.raw.rubber_medium_vol, 1); // vsssh
		sound3 = soundPool.load(getApplication(), R.raw.keyw_low_vol, 1);   // kh-ksh, Clear
		sound4 = soundPool.load(getApplication(), R.raw.hit_short_low_vol, 1); // hit, snap, letters etc
		sound5 = soundPool.load(getApplication(), R.raw.hit_short_low_vol, 1); //  tap
		sound6 = soundPool.load(getApplication(), R.raw.blob_low_vol2, 1); // blob, functions

		sIndex[0] = sound1; sIndex[1] = sound2; sIndex[2] = sound3;
		sIndex[3] = sound4; sIndex[4] = sound5; sIndex[5] = sound6;
	}

	public void initSounds() {


		if (Build.VERSION.SDK_INT >= 21) {
			AudioAttributes audioAttributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
					.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					.build();

			soundPool = new SoundPool.Builder()
					.setMaxStreams(6)
					.setAudioAttributes(audioAttributes)
					.build();
		} else {
			soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
		}

	}

	// --------------------------------------------------------------


	public void mpInitSounds() {
		if (soundOption == 1) initSounds();
	}

	public void mpCleanUp() { // clean mp or both
		if (soundOption == 1) { // 1 = soundPool, 2 = mp
			cleanUp();
		}

	}

	final int soundVolume = getApplication().getPreferences().getSoundVolume(); // 0 to 100
	private final float leftVolume = ((float) soundVolume)/100; 	// 100 -> 1, 50 -> 0.5
	private final float rightVolume = ((float) soundVolume)/100; 	// 100 -> 1, 50 -> 0.5

	private void playSound(int index, int speed) { // almost real time, less lag

		if (!readyToPlaySound || !newKeyDown || controller.showTempHelp) return; // showTempHelp = jus a peek

		if (soundQue < 2) readyToPlaySound = false;
		soundQue -= 1; //
		if (soundQue < 0) soundQue = 0;

		newKeyDown = false;

		// -------------------

		// Validity check added in v3.1: *** index values: 1 to 6 (same as soundN numbers) ****
		if (!((index > 0) && (index < 7))) index = 4; // v3.0: This line was commented/missing, crash! Out of bounds.

		// Left and right volume setting in float 0.0 to 1.0 (e.g. "1f, 1f" or "0.3f, 0.3f")
		soundPool.play(sIndex[index - 1], leftVolume, rightVolume, 0, 0, 1);

		//soundPool.play params:
		//	soundID		int:	a soundID returned by the load() function
		//	leftVolume	float: 	left volume value (range = 0.0f to 1.0f)
		//	rightVolume	float: 	right volume value (range = 0.0f to 1.0f)
		//	priority	int: 	stream priority (0 = lowest priority)
		//	loop		int: 	loop mode (0 = no loop, -1 = loop forever)
		//	rate		float: 	playback rate (1.0 = normal playback, range 0.5 to 2.0)
	}


	public void mpPlaySound(int index, int speed) { // not real time, more lag

		if (soundOption == 1) { // 1 = use soundPool instead of media player.
			soundQue += 1; // <== To fix missing sounds while typing fast.
			playSound(index, speed);
		}
	}
	// After release(), the object is no longer available.
	// After reset(), the object is like being just created.

	// ====================================

	public void setKeyboardHeight(String heightDp, Context context) // heightDp = e.g. "280dp"
	{
		int heightInDp = Integer.parseInt(heightDp.substring(0, heightDp.length()-2)); // omit "dp"

		float height = convertDpToPixel((float) heightInDp, context);
		if (height > 100)
		{
			int keyboardHeight = (int) height;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
			this.setLayoutParams(params);
		}
	}

	public void setKeyboardSizeAndPositionInDp(int kbdWidth, int kbdHeight,
				int leftMargin, int topMargin, int rightMargin, int bottomMargin,
				Context context){

		int height = (int) convertDpToPixel((float) kbdHeight, context);
		int width = (int) convertDpToPixel((float) kbdWidth, context);
		int left = (int) convertDpToPixel((float) leftMargin, context);
		int top = (int) convertDpToPixel((float) topMargin, context);
		int right = (int) convertDpToPixel((float) rightMargin, context);
		int bottom = (int) convertDpToPixel((float) bottomMargin, context);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);// TEST:
		params.setMargins(left, top, right, bottom);
		this.setLayoutParams(params);
	}

	public void setKeyboardHeightInDp(int heightInDp, Context context) // heightDp = e.g. 280
	{
		float height = convertDpToPixel((float) heightInDp, context);
		if (height > 100)
		{
			int keyboardHeight = (int) height;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
			//LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(800, keyboardHeight);// TEST:
			//params.setMargins(50, 50, 50, 50);
			//LinearLayout inputPad = (LinearLayout) findViewById(R.id.keyboard);
			//inputPad.setGravity(Gravity.CENTER);
			//LinearLayout.LayoutParams lparams = inputPad.getLayoutParams();
			//layoutParams.height =
			//LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1000, keyboardHeight, 200);
			//LinearLayout.MarginLayoutParams lparams = new LinearLayout.MarginLayoutParams(10, 10);
			//this.setParams(lparams);
			this.setLayoutParams(params);
		}
	}

	public void setKbdHeightAndWidthInDp(int heightInDp, int widthInDp, Context context) // heightDp = e.g. "280dp"
	{
		float height = convertDpToPixel((float) heightInDp, context);
		float width = convertDpToPixel((float) widthInDp, context);
		if ((height > 100) && (width > 100))
		{
			int keyboardHeight = (int) height;
			int keyboardWidth = (int) width;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(keyboardWidth, keyboardHeight);
			this.setLayoutParams(params);
		}
	}

	public void setKeyboardHeightInPx(int heightInPx, Context context) //
	{
		if (heightInPx > 100)
		{
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightInPx);
			this.setLayoutParams(params);
		}
	}

	public void setKeyboardHeightAndWidth(String heightDp, String widthDp, Context context) // heightDp = e.g. "280dp"
	{
		int heightInDp = Integer.parseInt(heightDp.substring(0, heightDp.length()-2)); // omit "dp"
		int widthInDp = Integer.parseInt(widthDp.substring(0, widthDp.length()-2)); // omit "dp"

		float height = convertDpToPixel((float) heightInDp, context);
		float width = convertDpToPixel((float) widthInDp, context);
		if ((height > 100) && (width > 100))
		{
			int keyboardHeight = (int) height;
			int keyboardWidth = (int) width;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(keyboardWidth, keyboardHeight);
			this.setLayoutParams(params);
		}
	}

	public static float convertDpToPixel(float dp, Context context){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		return px;
	}
	public static float convertPixelToDp(float px, Context context){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
		return dp;
	}

	private void setUpPaint() {

		int padHeight = getApplication().getPreferences().getPadHeight();

		int basicSize = (padHeight == 0) ? 38 : 36;  // basic text of single characters on keys
		int mediumSize = (padHeight == 0) ? 26 : 24; // Medium size text on buttons (white, gray, blue, black)
		int smallSizePlus = (padHeight == 0) ? 22 : 20;  // Text of several characters on highlighted and non-highlighted keys
		int smallSize = (padHeight == 0) ? 20 : 18;  // Text of several characters on highlighted and non-highlighted keys

		int flashBasicSize = 100; // Flash text sizes depending on length
		int flashMediumSize = 36; //
		int flashSmallSize = 26;  //

		int helpBasicSize = (padHeight == 0) ? 20 : 16; // help text size on middle keys
		int helpBigSize = (padHeight == 0) ? 26 : 22; // punctuation help text size on middle keys
		int helpMainButtonSize = (padHeight == 0) ? 22 : 18; // help text size on main keys

		// Normal size black text on buttons
		black = new Paint();
		black.setStyle(Paint.Style.FILL);
		black.setColor(BLACK); // was black
		black.setTextAlign(Align.CENTER);
		black.setTextSize(basicSize * scale); // was 40

		black.setFakeBoldText(true);
		black.setAntiAlias(true);

		if (android.os.Build.VERSION.SDK_INT >= 26) { // Oreo and later only
            //Typeface typeface = getResources().getFont(R.font.opendyslexic);

			int fontType = getApplication()
					.getPreferences().getFontType();
			if (fontType == 1) {
				Typeface typeface = getResources().getFont(R.font.opendyslexic);
				black.setTypeface(typeface);
				black.setFakeBoldText(false);
			}
        }

		blackNoShadow = new Paint(black);
		blackNoShadow.setShadowLayer(0, 0, 0, 0);

		// Normal size gray text on buttons
		gray = new Paint(black); // ComboKey, symbols on dark area
		gray.setColor(Color.rgb(210, 210, 210));

		frame = new Paint();
		frame.setColor(Color.rgb(120, 120, 120));

		gray.setShadowLayer(5, 0, 0, BLACK);
		// Normal size white text on buttons
		white = new Paint(black);
		white.setColor(Color.WHITE); // Symbols on buttonsfl

		blue = new Paint(black);
		blue.setColor(Color.rgb(170, 200, 255)); // Longer text buttons

		// Small size white text on buttons
		// White
		smallWhite = new Paint(white);  // text of several characters on non-highlighted keys
		smallWhite.setTextSize(smallSize * scale);
		smallWhitePlus = new Paint(white);  // text of several characters on non-highlighted keys
		smallWhitePlus.setTextSize(smallSizePlus * scale);
		// Gray
		smallGray = new Paint(smallWhite); // text of several characters on highlighted keys
		smallGray.setColor(Color.rgb(210, 210, 210));
		// Black
		smallBlack = new Paint(smallWhite); // text of several characters on highlighted keys
		smallBlack.setColor(BLACK);
		smallBlackPlus = new Paint(smallWhitePlus); // text of several characters on highlighted keys
		smallBlackPlus.setColor(BLACK);

		smallBlue = new Paint(smallWhite); // text of several characters on highlighted keys
		smallBlue.setColor(Color.rgb(180, 220, 255));

		// Medium size text on buttons
		// White
		mediumWhite = new Paint(white); //
		mediumWhite.setTextSize(mediumSize * scale); // was 30
		// Gray
		mediumGray = new Paint(gray); // was white
		mediumGray.setTextSize(mediumSize * scale); // was 30
		// Black
		mediumBlack = new Paint(black); // new
		mediumBlack.setColor(BLACK);
		mediumBlack.setTextSize(mediumSize * scale); // was 30
		// Blue
		mediumBlue = new Paint(mediumGray);
		mediumBlue.setColor(Color.rgb(180, 220, 255));



		modeText = new Paint(white); // Abc indicator text
		modeText.setColor(Color.rgb(210, 210, 210)); // was black text for wooden background, now light gray for dark stone background
		modeText.setTextSize((int) (0.7 * black.getTextSize())); // was: / 2
		modeText.setShadowLayer(0, 0, 0, 0);

		modeTextYellow = new Paint(modeText);
		modeTextYellow.setColor(Color.YELLOW);

		modeTextMellowYellow = new Paint(modeText);
		modeTextMellowYellow.setColor(Color.rgb(255, 255, 130 ));

		modeTextGreen = new Paint(modeText);
		modeTextGreen.setColor(Color.rgb(120, 250, 120 ));

		modeTextSmall = new Paint(modeText);
		modeTextSmall.setTextSize(modeText.getTextSize() * 0.8f);

		modeTextSmallGray = new Paint(modeTextSmall);
		modeTextSmallGray.setColor(Color.DKGRAY);;

		helpText = new Paint(modeText);
		helpText.setTextSize((int) (helpBasicSize * scale)); // added, modeText was too big ( / 1.5)

		helpTextWhite = new Paint(helpText);
		helpTextWhite.setColor(Color.GRAY); // was WHITE, then LTGRAY
		helpTextWhite.setShadowLayer(2, 4, 4, BLACK);

		mirrorTextYellow = new Paint(white); // not used yet
		mirrorTextYellow.setColor(Color.YELLOW); // Yellow mirrored characters on buttons

		helpTextWhiteBig = new Paint(helpText);
		helpTextWhiteBig.setColor(Color.WHITE); // was WHITE
		helpTextWhiteBig.setTextSize((int) (helpBigSize * scale));
		helpTextWhiteBig.setShadowLayer(2, 4, 4, BLACK);
		helpTextWhiteBig.setFakeBoldText(TRUE);

		helpTextMainButton = new Paint(helpText);
		helpTextMainButton.setColor(Color.LTGRAY); //
		helpTextMainButton.setTextSize((int) (helpMainButtonSize * scale));
		helpTextMainButton.setShadowLayer(2, 4, 4, BLACK);
		helpTextMainButton.setFakeBoldText(TRUE);

		flashText = new Paint(modeText);
		flashText.setTextSize(flashBasicSize * scale);
		flashText.setColor(Color.WHITE); // was BLACK

		flashTextMedium = new Paint(modeText);
		flashTextMedium.setTextSize(flashMediumSize * scale);
		flashTextMedium.setColor(mediumBlue.getColor());

		flashTextSmall = new Paint(modeText);
		flashTextSmall.setTextSize(flashSmallSize * scale);
		flashTextSmall.setColor(mediumBlue.getColor()); // was BLACK

	}

	private void initButtons(Context context) {

		ThemeManager themeManager = getApplication()
				.getThemeManager();
		themeManager.setActiveTheme(themeManager
				.getThemeByName(getApplication()
						.getPreferences().getTheme()));

		this.statusButton = getResources().getDrawable( // abc button
				R.drawable.blank);

		this.languagesButton = getResources().getDrawable(
				R.drawable.blank);
	}


	public void setController(CMBOKeyboardController controller) {
		this.controller = controller;
	}

	public boolean isXLarge() { // resource 'isTablet' in attr.xml in folder sw600dp.xml
		return getResources().getBoolean(R.bool.isTablet); // 2019-11-28
	}

	public void setTouchEventProcessor(SimpleTouchEventProcessor processor) {
		this.simpleTouchEventProcessor = processor;
		this.setOnTouchListener(this.simpleTouchEventProcessor);
		this.setOnLongClickListener(this);
		this.simpleTouchEventProcessor.addSimpleTouchEventListener(this);
	}

	@Override
	protected void dispatchDraw(Canvas canvas){

		Log.i("CMBO","Dispatch Draw - drawing");
		super.dispatchDraw(canvas);


		if (regenerateViewKludge) {

			Log.d("-PADTYPE", "drawItems() run on service due to change in preferences.");
			CMBOKeyboardService service = (CMBOKeyboardService) this.getContext();
			service.setInputView(service.onCreateInputView());
			regenerateViewKludge = false;
		}

	}


	@Override
	public void onDraw(Canvas canvas) {

		Log.d("-VIEW", "onDraw(), kludge = " + regenerateViewKludge);
		Log.d("onDraw()", "On Draw - drawing");
		super.onDraw(canvas);

		if (regenerateViewKludge) {
			Log.d("-PADTYPE", "*** service.onCreateInputView() run due to pad change in preferences.");
			CMBOKeyboardService service = (CMBOKeyboardService) this.getContext();
			service.setInputView(service.onCreateInputView());
			regenerateViewKludge = false;
		}


		if (this.getContext().getClass() != CMBOKeyboardService.class) {
			//regenerateViewKludge = true;
			return; // 2019
		}
		int padHeight = getApplication().getPreferences().getPadHeight();

		drawItems(canvas);

	}


	private void drawSidePanel(int padPosition, Canvas canvas) {

		this.isTopBar = getApplication().getPreferences().getDisplayWordCandidates();
		boolean sidePanelHidden = getApplication().getPreferences().isSidePanelHidden();
		noPanel = sidePanelHidden; // was isSidePanel
		int orientation = getResources().getConfiguration().orientation;
		boolean land = (orientation != Configuration.ORIENTATION_PORTRAIT);
		boolean port = !land;
		slim = 0;

		CMBOButton a = buttonLayout.getButton(CMBOKey.A);
		CMBOButton c = buttonLayout.getButton(CMBOKey.C);
		CMBOButton d = buttonLayout.getButton(CMBOKey.D);



		int landscapeColumns = getApplication().getPreferences().getLandscapeColumns();
		int padType = getApplication().getPreferences().getPadType();
		int padHeight = getApplication().getPreferences().getPadHeight();
		// 3/5-row for Landscape (landscapeColumns): 3 = 5-row, 4 = split (NEW), 5 = 3-row, 6 = double (5-row):
		// 3/5-row for portrait (padType): 0 = Normal 5-row, 10 = 3-row, (20 = Lower small???), 30 = 3-row special (portrait for now)
		// padType: 0 = 5-row, 10 = 3-row
		// padHeight: 0 = Full size, 10 = Upper small, 20 = Lower small, [30 = 3-row special (portrait for now)]
		// padPosition: 0 = left, 1/3 = center wide/narrow, 2 = right


		// * padPosition
		// boolean padLeft = (padPosition == 0);
		// boolean padRight = (padPosition == 2);
		// boolean padCenterWide = (padPosition == 1);
		// boolean padCenterNarrow = (padPosition == 3);
		// boolean portDouble = (padPosition == 4); // NEW
		// * padHeight
		// boolean upperSmall = (padHeight == 10);
		boolean fullSize = (padHeight == 0);
		// boolean lowerSmall = (padHeight == 20);
		// * padType (now a common setting for both orientations!)
		boolean port5Row = (padType == 0);
		boolean port3Row = (padType == 10);
		// * landscapeColumns
		// boolean land5Row = (landscapeColumns == 3); // single landscape pad
		// boolean land3Row = (landscapeColumns == 5); // *** not used any more, use padType for 5 or 3 rows
		boolean doublePad = (landscapeColumns == 6); // double landscape pad
		// * deviceType
		boolean phone = !isXLarge();
		boolean tablet = !phone;
		// * 5 Rows / 3 Rows
		// boolean rows5 = port5Row; // Common settings for portrait and landscape
		boolean rows3 = port3Row; //

		threeRowPad = rows3; // for button layout button colors

		// Frame size
		frSz = (int) convertDpToPixel(3, this.getContext()); // Small disp 240 x 432: 3dp

		int btnWidth = a.getHitBox().width();
		int btnHeight = a.getHitBox().height();

		int sidePanelLeft = a.getHitBox().left - 2 * btnWidth;
		int sidePanelTop = a.getHitBox().top;
		int sidePanelRight = a.getHitBox().left;
		//int sidePanelBottom = c.getHitBox().bottom; // Commented 2020-07-28
		int sidePanelBottom = c.getVisibleRect().bottom; // Use visible button, hitBox bottom was extended

		int topBarLeft = a.getHitBox().left;
		int topBarTop = a.getHitBox().top - btnHeight;
		int topBarRight = d.getHitBox().right;
		int topBarBottom = a.getHitBox().top - 2 * frSz;

		int leftPadLeft = a.getHitBox().left;
		int leftPadTop = a.getHitBox().top;
		int leftPadRight = a.getHitBox().left + 2*btnWidth;
		//int leftPadBottom = c.getHitBox().bottom;
		int leftPadBottom = c.getVisibleRect().bottom; // Use visible button, hitBox bottom was extended

		// Center column is common to both pads (often overlapping area)
		int rightPadLeft = d.getHitBox().right - 2*btnWidth;
		int rightPadTop = a.getHitBox().top;
		int rightPadRight = d.getHitBox().right;
		//int rightPadBottom = c.getHitBox().bottom;
		int rightPadBottom = c.getVisibleRect().bottom; // Use visible button, hitBox bottom was extended

		int leftFrameLeft = a.getHitBox().left;
		int leftFrameRight = a.getHitBox().left + 2*btnWidth;

		int rightFrameLeft = d.getHitBox().right - 2*btnWidth;
		int rightFrameRight = d.getHitBox().right;

		// ----------------- Adjust defaults to all keypad options -------------------------

		if (port) { // ================ Portrait Phones and Tablets =========================


			if (port3Row) { // more columns, center column is always common
				leftPadRight = a.getHitBox().left + 3*btnWidth;
				leftFrameRight = a.getHitBox().left + 3*btnWidth;
				rightPadLeft = d.getHitBox().right - 3*btnWidth;
				rightFrameLeft = d.getHitBox().right - 3*btnWidth;
			}

			if (phone) { // ++++++++++++++ Portrait Phones ++++++++++++++++

				Log.d("-PADS", "(VIEW) Phone, Portrait, padPosition = " + padPosition);

				switch (padPosition) { // 0 = left, 1 = center wide, 2 = right, 3 = center narrow

					case 0: // left, portrait, phone
						if (port3Row) { // 3-row
							sidePanelLeft = rightPadRight;
							sidePanelRight = rightPadRight + btnWidth;
							rightFrameRight = sidePanelRight;
							slim = 1;
						} else { // or 5-row big, phone
							sidePanelLeft = rightPadRight;
							sidePanelRight = rightPadRight + 2 * btnWidth;
							rightFrameRight = sidePanelRight;
						}
						topBarRight = sidePanelRight;
						break;
					case 4: // split
					case 1: // center wide, portrait, phone
						noPanel = true;
						topBarRight = rightPadRight;
						break;
					case 2: // right, portrait, phone
						if (port3Row) { // 3-row
							sidePanelLeft = leftPadLeft - btnWidth;
							slim = 1;
						} else { // or 5-row big, portrait, phone
							sidePanelLeft = leftPadLeft - 2 * btnWidth;
						}
						leftFrameLeft = sidePanelLeft;
						topBarLeft = sidePanelLeft;
						topBarRight = rightPadRight;
						break;
					case 3: // center narrow, portrait, phone
						//leftFrameLeft = leftPadLeft;
						rightFrameLeft = leftPadLeft;
						rightFrameRight += btnWidth;
						sidePanelLeft = rightPadRight;
						sidePanelRight = sidePanelLeft + btnWidth;
						topBarLeft = leftPadLeft;
						topBarRight = sidePanelRight;
						slim = 1; // for shorter text on side panel
						break;
					default:
						break;

				}

			} else if (tablet) { // ++++++++++++++ Portrait Tablets ++++++++++++++++

				Log.d("-PADS", "(VIEW) Tablet, Portrait, padPosition = " + padPosition);

				switch (padPosition) { // 0 = left, 1 = center wide, 2 = right, 3 = center narrow

					case 0: // left, portrait, tablet
						sidePanelLeft = rightPadRight;
						rightFrameRight = rightPadRight + 2 * btnWidth;
						if (port3Row && fullSize) { // 3-row big
							sidePanelRight = rightPadRight + 2 * btnWidth;
							//slim = 1;
						} else { // or 5-row big, phone
							sidePanelRight = rightPadRight + 2 * btnWidth;
						}
						topBarLeft = leftPadLeft;
						topBarRight = sidePanelRight;
						break;
					case 4: // split
					case 1: // center wide, portrait, tablet
						//sidePanelHidden = true;
						if (port3Row) {
							leftFrameLeft = leftPadLeft;
							leftFrameRight =  leftPadLeft + 7*btnWidth;
							rightFrameLeft = rightPadRight;
							rightFrameRight = rightPadRight + 2*btnWidth;
							sidePanelLeft = rightPadRight ;
							sidePanelRight = rightPadRight + 2*btnWidth;
							topBarLeft = leftPadLeft;
							topBarRight = sidePanelRight;
						} else { // port5Row
							leftFrameLeft = leftPadLeft;
							leftFrameRight = leftPadLeft + 3*btnWidth;
							rightFrameLeft = rightPadRight;
							rightFrameRight = rightPadRight + 3*btnWidth;
							sidePanelLeft = rightPadRight;
							sidePanelRight = rightPadRight + 2*btnWidth;
							topBarLeft = leftPadLeft;
							topBarRight = rightPadRight + 2*btnWidth;
						}
						break;
					case 2: // right, portrait, tablet
						leftFrameRight = leftPadRight - 2* btnWidth;
						if (port3Row) { // 3-row, portrait, tablet
							rightFrameLeft = rightPadRight - 7*btnWidth;
							sidePanelLeft = leftPadLeft - 2*btnWidth;
							sidePanelRight = leftPadLeft;
							slim = 1;
						} else { // or 5-row big, portrait, tablet
							rightFrameLeft = rightPadRight - 5*btnWidth;
							sidePanelLeft = leftPadLeft - 2*btnWidth;
							sidePanelRight = leftPadLeft;
						}
						topBarLeft = sidePanelLeft;
						topBarRight = rightPadRight;
						break;
					case 3: // center narrow, portrait, tablet
						if (port3Row) {
							leftFrameLeft = leftPadLeft;
							leftFrameRight =  leftPadLeft + 7*btnWidth;
							rightFrameLeft = rightPadRight;
							rightFrameRight = rightPadRight + 2*btnWidth;
							sidePanelLeft = rightPadRight ;
							sidePanelRight = rightPadRight + 2*btnWidth;
							topBarLeft = leftPadLeft;
							topBarRight = sidePanelRight;
						} else { // port5Row
							leftFrameLeft = leftPadLeft - 2*btnWidth;
							rightFrameLeft = rightPadRight - 2*btnWidth;
							sidePanelLeft = leftPadLeft - 2*btnWidth;
							sidePanelRight = leftPadLeft;
							topBarLeft = sidePanelLeft;
							topBarRight = rightPadRight;
						}
						//slim = 1; // for shorter text on side panel
						break;
					default:
						break;

				}



			}

		} else { // ====================== Landscape Phones and Tablets =======================

			if (rows3) { // more columns, center column is always common
				//leftPadLeft = a.getHitBox().left;
				leftPadRight = leftPadLeft + 3*btnWidth;
				leftFrameRight = leftPadLeft + 3*btnWidth;
				//rightPadRight = d.getHitBox().right;
				rightPadLeft =  rightPadRight - 3*btnWidth;
				rightFrameLeft = rightPadRight - 3*btnWidth;
			} else { // rows5
				//leftPadLeft = a.getHitBox().left;
				leftFrameRight = leftPadLeft + 2*btnWidth;
				//rightPadRight = d.getHitBox().right;
				rightFrameLeft = rightPadRight - 2*btnWidth;
			}
			topBarLeft = leftPadLeft;
			topBarRight = rightPadRight;


			if (phone) { // ++++++++++++++ Landscape Phones ++++++++++++++++

			Log.d("-PADS", "***(VIEW) Phone, Landscape, padPosition = " + padPosition
					+ ", padType = " + padType + ", doublePad = " + doublePad + ", rows3 = " + rows3);

			if(doublePad){ // Master setting for landscape (phone)

				if (rows3) { //  3-row Note, using portrait rows setting here!
					sidePanelLeft = rightPadRight - 5 * btnWidth;
					sidePanelRight = rightPadRight - 3 * btnWidth;
					sidePanelLeft = rightPadRight - 5 * btnWidth;
					rightFrameLeft = sidePanelLeft;
					leftFrameRight = leftPadLeft + 3 * btnWidth;
				} else { // 5-row
					leftPadRight = leftPadLeft + 2 * btnWidth;
					rightPadLeft = rightPadRight - 2 * btnWidth;
					sidePanelLeft = rightPadRight - 4 * btnWidth;
					sidePanelRight = rightPadLeft;
					rightFrameLeft = sidePanelLeft;
					leftFrameRight = leftPadLeft + 2 * btnWidth;
				}

			} else {

				switch (padPosition) {  // 0 = left, 1 = center wide, 2 = right, 3 = center narrow. 4 = split

					case 0: // left, phone, landscape, OK
						rightFrameRight += 2 * btnWidth;
						sidePanelLeft = rightPadRight;
						sidePanelRight = rightPadRight + 2 * btnWidth;
						topBarRight = sidePanelRight;
						break;
					case 1: //center wide, phone, landscape
						sidePanelLeft = rightPadLeft - 2 * btnWidth;
						sidePanelRight = rightPadLeft;
						rightFrameLeft = sidePanelLeft;
						//topBarLeft = leftFrameLeft;
						break;
					case 2: // right, phone, landscape
						leftFrameLeft -= 2 * btnWidth;
						sidePanelLeft = leftPadLeft - 2 * btnWidth;
						sidePanelRight = leftPadLeft;
						topBarLeft = sidePanelLeft;
						break;
					case 3: // center narrow, phone, landscape
						rightFrameRight += 2 * btnWidth;
						sidePanelLeft = rightPadRight;
						sidePanelRight = rightPadRight + 2 * btnWidth;
						topBarRight = sidePanelRight;
						break;
					case 4: // Split on sides, phone, landscape
						if (padType == 10) { //  3-rowNote, using portrait rows setting here!
							sidePanelLeft = rightPadRight - 4 * btnWidth;
							sidePanelRight = rightPadRight - 5 * btnWidth;
							leftFrameRight = leftPadLeft + 3 * btnWidth;
							rightFrameLeft = sidePanelLeft;
						} else { // 5-row
							leftPadRight = leftPadLeft + 3 * btnWidth;
							rightPadLeft = rightPadRight - 3 * btnWidth;
							sidePanelLeft = rightPadLeft - 2 * btnWidth;
							sidePanelRight = rightPadLeft;
							rightFrameLeft = sidePanelLeft;
							leftFrameRight = leftPadLeft + 3 * btnWidth;
						}
						break;
					default:
						break;

				} // end of padPosition switch

			} // end of not doublePad

		} else { // +++++++++++++++ Landscape Tablets +++++++++++++++++++++++

				topBarLeft = leftPadLeft;
				topBarRight = rightPadRight;

				Log.d("-PADS", "(VIEW) Tablet, Landscape, padPosition = " + padPosition);

				if(doublePad){ // Master setting for landscape (phone)

					if (rows3) { //  3-row Note, using portrait rows setting here!
						sidePanelLeft = rightPadRight - 5 * btnWidth;
						sidePanelRight = rightPadRight - 3 * btnWidth;
						sidePanelLeft = rightPadRight - 5 * btnWidth;
						rightFrameLeft = sidePanelLeft;
						leftFrameRight = leftPadLeft + 3 * btnWidth;
					} else { // 5-row
						leftPadRight = leftPadLeft + 2 * btnWidth;
						rightPadLeft = rightPadRight - 2 * btnWidth;
						sidePanelLeft = rightPadRight - 4 * btnWidth;
						sidePanelRight = rightPadLeft;
						rightFrameLeft = sidePanelLeft;
						leftFrameRight = leftPadLeft + 2 * btnWidth;
					}

					topBarLeft = sidePanelLeft;

				} else {


					switch (padPosition) {  // 0 = left, 1 = center wide, 2 = right, 3 = center narrow, 4 = split

						case 0: // left, tablet, landscape
							if (rows3) {
								leftFrameLeft = leftPadLeft;
								leftFrameRight = leftPadLeft + 5 * btnWidth;
								rightFrameLeft = rightPadRight - 2 * btnWidth;
								rightFrameRight = rightPadRight + 2 * btnWidth;
								sidePanelLeft = leftPadLeft + 5 * btnWidth;
								sidePanelRight = leftPadLeft + 7 * btnWidth;
								topBarRight = sidePanelRight;
							} else { // land5row
								leftFrameLeft = leftPadLeft;
								leftFrameRight = leftPadLeft + 5 * btnWidth;
								rightFrameLeft = rightPadRight - 2 * btnWidth;
								rightFrameRight = rightPadRight + 2 * btnWidth;
								sidePanelLeft = leftPadLeft + 3 * btnWidth;
								sidePanelRight = leftPadLeft + 5 * btnWidth;
								topBarRight = sidePanelRight;
							}
							break;
						case 1: //center wide, tablet, landscape
							//if (doublePad) { // TODO: move here those below with row options

							//} else {

							//}
							if (rows3) { // TODO: Finally, will include (be fit for) double options for both cases (doublePad = true)
								leftPadRight = leftPadLeft + 3 * btnWidth; // to get more black background
								rightPadLeft = rightPadRight - 3 * btnWidth;
								leftFrameLeft = leftPadLeft;
								leftFrameRight = leftPadLeft + 3 * btnWidth;
								rightFrameLeft = rightPadRight - 5 * btnWidth; // -5
								rightFrameRight = rightPadRight;
								sidePanelLeft = rightPadRight - 5 * btnWidth;
								sidePanelRight = rightPadRight - 3 * btnWidth;
								topBarLeft = rightFrameLeft;
								topBarRight = rightPadRight;
							} else { // rows5
								leftFrameLeft = leftPadLeft;
								leftFrameRight = leftPadLeft + 2 * btnWidth;
								rightFrameLeft = rightPadRight - 4 * btnWidth; // -4
								rightFrameRight = rightPadRight;
								sidePanelLeft = rightPadRight - 4 * btnWidth;
								sidePanelRight = rightPadRight - 2 * btnWidth;
								topBarLeft = sidePanelLeft;
							}
							break;
						case 2: // right, tablet, landscape
							if (rows3) {
								leftFrameLeft = leftPadLeft - 2 * btnWidth;
								leftFrameRight = leftPadLeft;
								rightFrameLeft = rightPadRight - 5 * btnWidth;
								rightFrameRight = rightPadRight;
								sidePanelLeft = rightPadRight - 7 * btnWidth;
								sidePanelRight = rightPadRight - 5 * btnWidth;
								topBarLeft = leftFrameLeft;
							} else { // land5Row
								leftFrameLeft = leftPadLeft - 2 * btnWidth;
								leftFrameRight = leftPadLeft;
								rightFrameLeft = rightPadRight - 5 * btnWidth;
								rightFrameRight = rightPadRight;
								sidePanelLeft = rightPadRight - 5 * btnWidth;
								sidePanelRight = rightPadRight - 3 * btnWidth;
								topBarLeft = leftFrameLeft;
							}
							break;
						case 3: // center narrow, tablet, landscape
							if (rows3) {
								leftFrameLeft = leftPadLeft;
								leftFrameRight = leftPadLeft + 5 * btnWidth;
								rightFrameLeft = rightPadRight - 3 * btnWidth;
								rightFrameRight = rightPadRight;
								sidePanelLeft = leftPadLeft + 3 * btnWidth;
								sidePanelRight = rightPadRight - 3 * btnWidth;
								topBarLeft = leftPadLeft;
								topBarRight = rightPadRight;
							} else { // land5Row
								leftFrameLeft = leftPadLeft;
								leftFrameRight = leftPadLeft + 2 * btnWidth;
								rightFrameLeft = rightPadRight - 5 * btnWidth;
								rightFrameRight = rightPadRight;
								sidePanelLeft = rightPadRight - 5 * btnWidth;
								sidePanelRight = rightPadRight - 3 * btnWidth;
								topBarLeft = sidePanelLeft;
							}
							break;
						case 4: // Split on sides, tablet, landscape
							if (rows3) {
								sidePanelLeft = rightPadRight - 5 * btnWidth;
								sidePanelRight = rightPadRight - 4 * btnWidth;
								rightFrameLeft = sidePanelLeft;
							} else { // rows5
								sidePanelLeft = rightPadLeft - 2 * btnWidth;
								sidePanelRight = rightPadRight - 3 * btnWidth;
								rightFrameLeft = sidePanelLeft;
							}
							break;

						default:
							break;

					} // switch

				} // else, not doublePad

			} // tablets

	} // landscape


		if (noPanel) {
			leftFrameLeft = leftPadLeft; leftFrameRight = leftPadRight;
			rightFrameLeft = rightPadLeft; rightFrameRight = rightPadRight;
		}

		// ---------------------- Finally, draw the keyboard --------------------------------

		// Frame for left and right part of keypad
		RectF rectFrameL = new RectF(leftFrameLeft - frSz, leftPadTop - frSz, leftFrameRight + frSz, leftPadBottom + frSz);
		RectF rectFrameR = new RectF(rightFrameLeft - frSz, rightPadTop - frSz, rightFrameRight + frSz, rightPadBottom + frSz);
		// Black background for keypads, left and right part
		RectF rectPadL = new RectF(leftPadLeft, leftPadTop, leftPadRight, leftPadBottom);
		RectF rectPadR = new RectF(rightPadLeft , rightPadTop, rightPadRight, rightPadBottom);

		// check frame color
		CMBOKeyboardService service = (CMBOKeyboardService) this.getContext();

		if (service.isTerminalMode()) { // (was: frameBlue)
			if (!terminalMode) { // if not already set, do it
				passwordMode = false;
				terminalMode = true;
				frame.setColor(Color.rgb(30, 50, 140)); // blue
			}
		} else if (service.isPasswordInputMode()) {
			if (!passwordMode) {
				terminalMode = false;
				passwordMode = true;
				frame.setColor(Color.rgb(180, 50, 30)); // red
			}
		} else {
			if (passwordMode || terminalMode) {
				terminalMode = false;
				passwordMode = false;
				frame.setColor(Color.rgb(120, 120, 120)); // gray
			}
		}

		// -----------

		// frames
		canvas.drawRoundRect(rectFrameL, frSz * 2, frSz * 2, frame); // rounded rect, radius, color
		canvas.drawRoundRect(rectFrameR, frSz * 2, frSz * 2, frame); // rounded rect, radius, color
		// pad black backgrounds
		canvas.drawRoundRect(rectPadL, frSz * 2, frSz * 2, black); // rounded rect
		canvas.drawRoundRect(rectPadR, frSz, frSz, black); // rounded rect

		Theme theme = getApplication().getThemeManager().getActiveTheme();
		String themeName = theme.getName();
		//backgroundImage = theme.getDrawable(DrawableType.BACKGROUND_IMAGE, this.getContext());
		if (!themeName.equals(previousThemeName)) { //
			backgroundImage = theme.getDrawable(DrawableType.BACKGROUND_IMAGE, this.getContext());
			//topBarImage = theme.getDrawable(DrawableType.TOPBAR_IMAGE, this.getContext()); // use when themes will include topbar image
			topBarImage = getResources().getDrawable(R.drawable.topbarimage); // TEMP
			topBarImageAdd = getResources().getDrawable(R.drawable.topbarplus);
			topBarImageMinus = getResources().getDrawable(R.drawable.topbarminus);
			Log.i("CMBO", "New Theme. Changing backgroundImage to: " + backgroundImage);
			previousThemeName = themeName;
		}
		if (noPanel) {
			backgroundArea = new Rect(0, 0, 0, 0);
			statusArea = new Rect(0, 0, 0, 0);
			languageArea = new Rect(0, 0, 0, 0);
			flashArea = new Rect(0, 0, 0, 0);
			topBarLeft = a.getHitBox().left; // TODO: These might be better somewhere above!!!
			topBarRight = d.getHitBox().right;
		} else {
			backgroundArea = new Rect(sidePanelLeft, sidePanelTop, sidePanelRight, sidePanelBottom);
			backgroundImage.setBounds(backgroundArea);
			backgroundImage.draw(canvas);
			statusArea = new Rect(sidePanelLeft + btnWidth / 4, sidePanelTop,
					sidePanelRight - btnWidth / 4,	sidePanelTop + btnHeight);
			languageArea = new Rect(sidePanelLeft + btnWidth / 4, backgroundArea.centerY() - btnHeight / 2,
					backgroundArea.right - btnWidth / 4,
					backgroundArea.centerY() + btnHeight / 2 );
			flashArea = new Rect(sidePanelLeft + btnWidth / 4, backgroundArea.centerY() - btnHeight,
					backgroundArea.right - btnWidth / 4,
					backgroundArea.centerY() - btnHeight / 2);
		}
		if (isTopBar) {
			topBarArea = new Rect(topBarLeft, topBarTop, topBarRight, topBarBottom);
			topBarAreaAdd = new Rect(topBarLeft, topBarTop, topBarLeft + btnHeight, topBarBottom);
			topBarAreaMinus = new Rect(topBarRight - btnHeight, topBarTop, topBarRight, topBarBottom);
			topBarImage.setBounds(topBarArea); topBarImage.draw(canvas);
			topBarImageAdd.setBounds(topBarAreaAdd); topBarImageAdd.draw(canvas);
			topBarImageMinus.setBounds(topBarAreaMinus); topBarImageMinus.draw(canvas);
			Log.i("-TOPBAR", "Draw TopBar (hidden side panel)");
		} else {
			topBarArea = new Rect(0, 0, 0, 0);
			topBarAreaAdd = new Rect(0, 0, 0, 0);
			topBarAreaMinus = new Rect(0, 0, 0, 0);
		}

	}

	private void drawItems(Canvas canvas) {

		//if ((canvas == null) || (controller == null)) return;

		int padPosition = getApplication().getPreferences().getPadPosition();
		// padPosition: 0/1/2 = left/wide_center/right,  3 = narrow in center
		int padHeight = getApplication().getPreferences().getPadHeight();
		// padHeight: 10 = Higher small, 0 = Normal, 20 = Lower small, 30 = 3-row special (portrait for now)
		int padType = getApplication().getPreferences().getPadType();
		// padType: 0 = 5-row standard, 10 = 3-row special
		int landcapeColumns = getApplication().getPreferences().getLandscapeColumns();
		// 3 = standard kbd, 5 = special 3-row kbd // 3, 5 or 6 (standard, special, double)
		int orientation = getResources().getConfiguration().orientation;

		if (orientation != Configuration.ORIENTATION_PORTRAIT)  {
			Log.d("-ORIENT", "Orientation NOT Portrait - drawItems()");
			// padPosition = 1; // Landscape kbd always full width in middle
		} else {
			Log.d("-ORIENT", "Orientation IS Portrait - drawItems()");
		}

		canvas.drawColor(Color.TRANSPARENT);

		Log.d("CMBO", "Draw Items");
		if (buttonLayout != null) {
			Log.d("CMBO", "*** ButtonLayout is NOT null. Listing buttons: (commented out)");
			//buttonLayout.listButtons();
		}


		//if ((buttonLayout == null) || (regenerateViewKludge) || isNewOrient){ // <= Google version 1.3
		if ( (buttonLayout == null) || regenerateViewKludge ){ // null or preferences changed

			if (buttonLayout != null) {
				showToast("*** ButtonLayout is NOT null. Cleared now.", debug2InUse);
				Log.d("-PADTYPE", "*** ButtonLayout is NOT null. Clearing areas and buttons now:");
				buttonLayout.clearButtons();
				//regenerateViewKludge = false;
			} else {
				showToast("*** ButtonLayout is NULL.", debug2InUse);
				Log.d("-PADTYPE", "*** ButtonLayout is null.");
				//regenerateViewKludge = true;
				//return;
			}

			showToast("**** (Re)generateLayout (View). PadPosition = " +  padPosition, debugInUse);

		}

		// the background image is defined in activity.xml files for each case of orientation and size

		addSideInfo(padPosition, padHeight, orientation, padType, landcapeColumns, isXLarge(), canvas); // Add abc, language, (flash symbol)

		if (!isSymbolFlashOn()) { // no symbol flash in use
			drawState(canvas, orientation, padPosition); // added 11/2017 to keep side info stable
		}

		CMBOButton pressedState = null;
		CMBOButton pressedKey = null;

		CMBOKeyboardService service = (CMBOKeyboardService) this
				.getContext();

		Log.d("CMBO", "Controller state: " + controller.getState());
		Log.d("CMBO", "Controller key: " + controller.getKey());

		// Finally, draw image on side panel (the black area within frames):
		checkBackgroundImage(canvas, padPosition); // check and change in case of new theme, then draw


		// Draw all buttons:

		for (CMBOButton b : buttonLayout.getButtons()) {

			int pressedStateValue = 0;

			if (controller.getState() == b.getId()) { // Tap
				pressedState = b;


				// TEST to flash single keys (not perfect yet)

				if (controller.getKey() == 0) flashSymbol = controller.getButtonStringUnpressed(b);

				//toastCandidateWords(flashSymbol);

				if (!passwordMode && !flashSymbol.equals("") && !flashSymbol.equals("...") && isSymbolFlashOn()) {
					Log.d("-FLASH", "pressed state ID = " + b.getId() + ", pressed string = " + flashSymbol);
					//drawFlashSymbol(canvas, flashSymbol, orientation, padPosition, padType, padHeight);
					keepFlashVisible = true; // this is enough, no need to draw again
					flashTimer.start();
				} else {
					flashTimer.start(); // needed to stop holdFirstKey by timer
				}

				// end of TEST

				Log.d("-PRESS", "pressed state ID = " + b.getId() + ", pressed string = " + flashSymbol);
				Log.d("CMBO", "pressed state = " + b.getId());

			}


			if (controller.getKey() == b.getId()) { // Combo
				pressedKey = b;
				// Flash:
				//flashSymbol = controller.getButtonString(b); // class global variable
				// option2: take combo symbol into account, not just key label
				flashSymbol = getApplication().getLayoutManager()
						.getStringRepresentation(controller.getKeyboardOffset(), controller.getState(), controller.getKey());

				//toastCandidateWords(flashSymbol);

				Log.d("-PRESS", "pressed key ID = " + b.getId() + ", pressed string =  " + flashSymbol + ", pressedKey = " + pressedKey);

				//if (isComboFlashOn() && controller.oneDown()) { // No flash on two-hand Combos
				if (isSymbolFlashOn()) { // Flash on?
					Log.d("-FLASH", "pressed key ID = " + b.getId() + ", pressed string =  " + flashSymbol + ", pressedKey = " + pressedKey);
					//if (!keepFlashVisible)
					//if ((flashSymbol != "") && (flashSymbol != "...")) {
					if (!passwordMode && !flashSymbol.equals("") && !flashSymbol.equals("...")) {
						drawFlashSymbol(canvas, flashSymbol, orientation, padPosition, padType, padHeight);
						keepFlashVisible = true;
						flashTimer.start();
					}
				}

				// Autorepeat (combos only):

				if (!autoRepeat && controller.isAutoRepeatCombo()){
					//controller.sendAutoRepeatCombo();
					autoRepeat = true; candidates.autorepeatOn = false;
					repeatTimer.start(); // 2018-04-14
					Log.d("-REPEAT", "Repeat timer start.");

				} else if (!autoRepeat && (flashSymbol.equals("_Repeat"))) {
					autoRepeat = true; candidates.autorepeatOn = false;
					repeatTimer.start(); // 2018-04-14
					Log.d("-REPEAT", "Repeat timer start.");

				} else if (!(flashSymbol.equals("_Repeat") || controller.isAutoRepeatCombo())) {
						repeatTimer.cancel(); // 2018-04-14
						autoRepeat = false; candidates.autorepeatOn = false;
					Log.d("-REPEAT", "Repeat timer STOP.");

				}
			}


			drawButton(b, false, b.getVisibleRect(), canvas);


			// --------------------------


			if (!isDisplayNoCharacters()) {
				// TODO
				// Seppo edited 26.1.2011, but is
				// this
				// the right place for this condition?
				drawButtonSymbol(canvas, b, noSymbolsUpdate);
			}

		}

		// reason to vibrate?

		if (pressedState != null) { // (1) first key pressed **************
			readyToPlaySound = true; // In addition to this, newKeyDown is required elsewhere
			// vibrate
			// if (pressedKey == null) pressedKeyPrevious = null;
			// draw
			drawButton(pressedState, true, pressedState.getVisibleRect(), canvas);

			drawButtonSymbol(canvas, pressedState, noSymbolsUpdate); // skip showing updated symbols for a short while
			for (CMBOButton b : pressedState.getAdjacentButtons()) {
			//for (CMBOButton b : buttonLayout.getButtons()) { // TEST !!!!!!
				drawButton(b, true, b.getVisibleRect(), canvas);
				drawButtonSymbol(canvas, b, noSymbolsUpdate);
			}

            if (( //isShowMirror() // no more in preferences
					// && isShowHelp() // show yellow mirror characters now even if no other help is on
                    !isDisplayNoCharacters()
                    && !isKeysDisabled())
					&& !(typeMethod() == 3) // Combos only = 3
					// || (controller.showTempHelp) // not usable here
					) {
				//Log.d("-TYPEMETHOD", "typeMethod() = " + typeMethod());
                showMirrorOnSameSide(canvas, padType, noSymbolsUpdate);
            }


		}

		if (pressedKey != null) { // (2) second key pressed checked here *********
			readyToPlaySound = true; // In addition to this, newKeyDown is required elsewhere
			// vibrate for a combo
			if (pressedKeyPrevious == null) {
				vibrate();
				Log.d("-VIBRATOR", "Vibrate! (second key pressed:" + pressedKey.getId() + ")");
			} else if (pressedKey.getId() != pressedKeyPrevious.getId()) { // compare integer values!
				vibrate();
				Log.d("-VIBRATOR", "Vibrate! (combo with new key: " + pressedKey.getId() + ", not same as previous: " + pressedKeyPrevious.getId() + ")");
			}
			// draw
			drawButton(pressedKey, true, pressedKey.getVisibleRect(), canvas);
			drawButtonSymbol(canvas, pressedKey, noSymbolsUpdate); // show symbol on pressed/mirrored key
			for (CMBOButton b : pressedKey.getAdjacentButtons()) {
				drawButton(b, true, b.getVisibleRect(), canvas);
				drawButtonSymbol(canvas, b, noSymbolsUpdate);
			}

        } else {
			Log.d("-VIBRATOR", "No vibrate (no combo)");
		}

		pressedKeyPrevious = pressedKey;

		for (CMBOButton b : buttonLayout.getButtons()) { // Copied here from above
							// in order to paint HelpCharacters on top of ALL keys
			if ((controller.getState() != 0) && (!noSymbolsUpdate)) {
				return; // Show Tips only while no press
			}
			if ((isShowHelp()
				&& !isDisplayNoCharacters()
				&& !(isKeysDisabled() && isService())
				)
				|| (controller.showTempHelp) // Show Tips/help after peekPress
				|| (controller.getKeyboardOffset() == CMBOKey.SYMBOL_MODIFIER) // ...or always for symbols set
				|| (controller.getKeyboardOffset() == CMBOKey.AUXSET_SYMBOL_MODIFIER)
				)
			{
					drawHelpCharacters(canvas, b, landcapeColumns, padHeight, orientation, padType, padPosition);
			}


		}


		if (keepFlashVisible && !controller.peekPress) {
			drawFlashSymbol(canvas, flashSymbol, orientation, padPosition, padType, padHeight);
		} else {
			drawState(canvas, orientation, padPosition);
		}

		currentLangISO = service.getCurrentLanguageISO(); // e.g. "en"
		currentCountryISO = service.getCurrentCountryISO(); // e.g. "US"
		// ISO code of lang, e.g. "en"
		if (currentLangChanged(currentLangISO))	{
			Log.d("-SPEECH", "Current lang changed");
			service.setSpeechLang(currentLangISO, currentCountryISO);
			service.setVoiceInputLang();
		}

		localeString = currentLangISO + "_" + currentCountryISO;
		if (!previousLocaleString.equals(localeString)) {
			candidates.setLocaleString(localeString);
			previousLocaleString = localeString;
		}

	}

	private String currentLangISO = "en"; // defaults
	private String currentCountryISO = "US";

	private String previousLocaleString = "";
	private String previousCurrentLang = "";

	private boolean currentLangChanged(String currentLang) {
		Log.d("-CURLANG", "*** Previous/current language: " + previousCurrentLang + "/" + currentLang);
		if (previousCurrentLang.equals(currentLang) || previousCurrentLang.equals("")) {
			previousCurrentLang = currentLang;
			return false;
		} else {
			previousCurrentLang = currentLang;
			return true;
		}
	}


	private void showMirrorOnSameSide(Canvas canvas, int padType, boolean noSymbolsUpdate) {

		int offs = getApplication()
				.getKeyboard().getOffset(); //for current offset in keyboard

		if ((noSymbolsUpdate) || (offs == CMBOKey.EMOJI_MODIFIER) || (offs == CMBOKey.FN_MODIFIER)) return;

		String testString = "X"; // yellow text

		boolean leftColumn = false; //(CMBOKey.areOnSameColumn(CMBOKey.I1, keyIndex)
		boolean rightColumn = false;

		CMBOButton o = buttonLayout.getButton(CMBOKey.O); // left
		CMBOButton s = buttonLayout.getButton(CMBOKey.S);
		CMBOButton g = buttonLayout.getButton(CMBOKey.G); // right
		CMBOButton k = buttonLayout.getButton(CMBOKey.K);

		int dX = o.getHitBox().width() / 2;
		int dY = o.getHitBox().height() / 2 + (int) (white.getTextSize() / 2.8);

		int xO = o.getVisibleRect().left + dX;
		int yO = o.getVisibleRect().top + dY;
		int xS = s.getVisibleRect().left + dX;
		int yS = s.getVisibleRect().top + dY;
		int xG = g.getVisibleRect().left + dX;;
		int yG = g.getVisibleRect().top + dY;
		int xK = k.getVisibleRect().left + dX;;
		int yK = k.getVisibleRect().top + dY;


		//CMBOButton pressedState = controller.getState();
		int pressedState = controller.getState(); // chord value
		int pressedStateIndex = CMBOKey.getIndexForChord(pressedState);

		leftColumn = (CMBOKey.areOnSameColumn(CMBOKey.I1, pressedStateIndex));
		rightColumn = (CMBOKey.areOnSameColumn(CMBOKey.I12, pressedStateIndex));

		if (leftColumn) {
			//testString = controller.getButtonString(g);
			if ((pressedState != 3) && (controller.getKey() != 24)) canvas.drawText(controller.getButtonString(g).trim(), xO, yO, mirrorTextYellow);
			if ((pressedState != 6) && (controller.getKey() != 48)) canvas.drawText(controller.getButtonString(k).trim(), xS, yS, mirrorTextYellow);
		} else if (rightColumn) {
			if ((pressedState != 24) && (controller.getKey() != 3)) canvas.drawText(controller.getButtonString(o).trim(), xG, yG, mirrorTextYellow);
			if ((pressedState != 48) && (controller.getKey() != 6)) canvas.drawText(controller.getButtonString(s).trim(), xK, yK, mirrorTextYellow);
		}

	}


	private void addSideInfo(int padPosition, int padHeight,
							 int orientation, int padType,
							 int landscapeColumns, boolean xLarge,
							 Canvas canvas) { // abc, language, flash

		Log.d("addSideInfo", "Add Side info: Abc, Language, Flash.");
		buttonLayout = CMBOKeyboardLayoutGenerator.generateLayout(this);
		if (buttonLayout == null) return;

		// Get the native text of abc/Abc/ABC/123 from JSON
		abcIndicator = getApplication()
				.getLayoutManager().getAbcIndicator(keyboard.getOffset());

		CMBOButton th = buttonLayout.getButton(CMBOKey.TH); // old CMBO layout
		CMBOButton w = buttonLayout.getButton(CMBOKey.W);   // old CMBO layout

		CMBOButton a = buttonLayout.getButton(CMBOKey.A);
		CMBOButton b = buttonLayout.getButton(CMBOKey.B);
		CMBOButton c = buttonLayout.getButton(CMBOKey.C);
		CMBOButton d = buttonLayout.getButton(CMBOKey.D);

		CMBOButton e = buttonLayout.getButton(CMBOKey.E);
		CMBOButton f = buttonLayout.getButton(CMBOKey.F);

		CMBOButton s = buttonLayout.getButton(CMBOKey.S);
		CMBOButton k = buttonLayout.getButton(CMBOKey.K);

		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// NOTE: Definitions have been removed from here because
		// drawSidePanel(padPosition, canvas) is used now (v3.8 up)
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

		Log.d("CMBO", "*** Present and previous pad positions/heights: " + padPosition + "/" + previousPadPosition + ", " + padHeight + "/" + previousPadHeight);

		if ((statusArea == null) || (padPosition != previousPadPosition)
				|| (padHeight != previousPadHeight)
				|| (padType != previousPadType)) { //  original

			Log.d("CMBO", "*** statusArea is null or padPosition changed. Creating statusArea, languageArea and flashArea.");


			// =================================================


			drawSidePanel(padPosition, canvas);

			previousPadPosition = padPosition;
			previousPadHeight = padHeight;
			previousPadType = padType;
		}



	}



	boolean isService() {
		return this.service;
	} // seems to be always 'false'

	private boolean isShowHelp() {
		// Option (not used):
		// return (tipType() != 0);
		return getApplication().getPreferences().get()
				.getBoolean("PREFERENCES_DISPLAY_HELP_CHARACTERS", true);
	}

	private int tipType() { // 0 = no tips, 1 = tips on middle keys, 2 = tips on main keys
		return Integer.parseInt(getApplication().getPreferences().get()
				.getString("PREFERENCES_TIPTYPE", "2"));
	}

	private int typeMethod() { // 1 = Swipes+Combos, 2 = Swipes only, 3 = Combos only
		return Integer.parseInt(getApplication().getPreferences().get()
				.getString("PREFERENCES_TYPE_METHOD", "1"));
	}

	private boolean isShowMirror() { // not used any more
		return getApplication().getPreferences().get()
				.getBoolean("PREFERENCES_DISPLAY_MIRROR_CHARACTERS", true);
	}

	private boolean isShowWords() {
		return getApplication().getPreferences().get()
				.getBoolean("PREFERENCES_DISPLAY_WORD_CANDIDATES", false);
	}

	private boolean isDisplayNoCharacters() {
		return getApplication().getPreferences().get()
				.getBoolean("PREFERENCES_DISPLAY_NO_CHARACTERS", false);
	}

	private void drawHelpCharacters(Canvas canvas, CMBOButton b, int landscapeColumns,
									int padHeight, int orientation, int padType, int padPosition) {

		Paint helpText = this.helpTextWhiteBig;
		Paint helpText2 = this.helpTextMainButton;
		Paint helpText3 = this.mirrorTextYellow;
		Paint helpText2gray = this.helpTextWhite;

		int key = b.getId();
		int keyIndex = CMBOKey.getIndexForChord(key);

		// boolean leftColumn = CMBOKey.isOnTheSameSide(CMBOKey.A, key);
		// Put tips on left or right on the button depending if the key is considered to be on...
		// Side of the keypad:
		boolean leftColumn = (CMBOKey.areOnSameColumn(CMBOKey.I1, keyIndex) || (keyIndex == 6) || (keyIndex == 8) || (keyIndex == 10));
		// Default edge of the button
		boolean leftEdge = !leftColumn; // towards which edge of the button to show the tip
		boolean leftEdge2 = leftColumn; // towards which edge of the button to show the tip
		boolean rightEdge = !leftEdge;
		boolean rightEdge2 = leftEdge;
		// tips to show (one out of three characters):
		boolean tip10 = false; // basic tips (tips1) on middle buttons
		boolean tip11 = false;
		boolean tip12 = false;
		boolean tip20 = false; // optional tips (tips2) on main buttons
		boolean tip21 = false;
		boolean tip22 = false;

		int tipOffset = 0; // move tip character up or down on the key in steps
		int tipOffset2 = 0;

		//boolean shiftUp =  (key == CMBOKey.B) || (key == CMBOKey.E); // move up signs and text on keys B and E
		boolean shiftUp = false;

		//int includedChars = 0; // include one character out of three (0...2; do not exclude: -1))
		int includeTips1 = 1; // -1; // 1 = include all legacy tips
		int includeTips2 = -1; // 1 = include all optional tips on main buttons
		int includeTips3 = -1; // 1 = include tips mixed on main and middle buttons
		//int includeTips4 = -1; // 1 = include mirrored yellow tips on buttons G, K, O, S

		Log.i("-DRAWHELP",
				"--- drawHelpCharacters --- landscapeColumns/padHeight/orientation/padType/padPosition = " + landscapeColumns
		+ "/" + padHeight + "/" + orientation + "/" + padType + "/" + padPosition);


		if (controller.statusDevanagariOn()){ // If devanagari, use always tips on middle buttons
			includeTips1 = 1;  // middle
			includeTips2 = -1; // main
			includeTips3 = -1; // mixed
		} else if (tipType() == 1) { // tips on middle buttons
			includeTips1 = 1;  // middle
			includeTips2 = -1; // main
			includeTips3 = -1; // mixed
		} else if (tipType() == 2){ // tips on main buttons
			includeTips1 = -1;
			includeTips2 = 1;
			includeTips3 = -1;
		} else { // == 3 mixed tips, depend on side (left/right) and key type (main/middle)
			//includeTips1 = 1; // defaults: all shown
			includeTips1 = 1; // (orientation == Configuration.ORIENTATION_PORTRAIT) ? 1 : -1;
			includeTips2 = 1; // (orientation == Configuration.ORIENTATION_PORTRAIT) ? 1 : 1;
			includeTips3 = 1; // (orientation == Configuration.ORIENTATION_PORTRAIT) ? 1 : -1;
		}


		// key = chord value

		switch (key) { // === what to show ===

			// A and D Keys
			case CMBOKey.A:
				includeTips1 = 1; // show punctuation on main keys anyway
				if ((includeTips3 == 1) && (padPosition == 2)) { // Left main key, Pad right
					includeTips2 = -1;
				}
				tip10 = false; tip11 = true; tip12 = false;
				tip20 = true; tip21 = true; tip22 = false;
				break;
			case CMBOKey.D:
				includeTips1 = 1; // show punctuation on main keys anyway
				if ((includeTips3 == 1) && (padPosition == 0)) { // Right main key, Pad Left
					includeTips2 = -1;
				}
				tip10 = false; tip11 = true; tip12 = false;
				tip20 = true; tip21 = true; tip22 = false;
				break;

			// B and E Keys
			case CMBOKey.B:
				includeTips1 = 1; // show punctuation on main keys anyway
				if ((includeTips3 == 1) && (padPosition == 2)) { // Left main key, Pad right
					includeTips2 = -1;
				}
				tip10 = false; tip11 = false; tip12 = true;
				tip20 = true; tip21 = true; tip22 = false;
				break;
			case CMBOKey.E:
				includeTips1 = 1; // show punctuation on main keys anyway
				if ((includeTips3 == 1) && (padPosition == 0)) { // Right main key, Pad left
					includeTips2 = -1;
				}
				tip10 = false; tip11 = false; tip12 = true;
				tip20 = true; tip21 = true; tip22 = false;
				break;

			// C and F Keys
			case CMBOKey.C:
				includeTips1 = 1;
				if ((includeTips3 == 1) && (padPosition == 2)) { // Left main key, Pad right
					includeTips2 = -1;
				}
				tip10 = true; tip11 = false; tip12 = false;
				tip20 = true; tip21 = true; tip22 = false;
				break;
			case CMBOKey.F:
				includeTips1 = 1;
				if ((includeTips3 == 1) && (padPosition == 0)) { // Right main key, Pad left
					includeTips2 = -1;
				}
				tip10 = true; tip11 = false; tip12 = false;
				tip20 = true; tip21 = true; tip22 = false;
				break;

			// G and K Keys
			case CMBOKey.G:
				if ((includeTips3 == 1) && (padPosition == 0)) { // Right middle key, Pad left
					includeTips1 = -1;
				}
				tip10 = true; tip11 = true; tip12 = true;
				tip20 = false; tip21 = false; tip22 = false;
				helpText = this.helpTextWhite;
				break;
			case CMBOKey.K:
				if ((includeTips3 == 1) && (padPosition == 0)) { // Right middle key, Pad left
					includeTips1 = -1;
				}
				tip10 = true; tip11 = true; tip12 = true;
				tip20 = false; tip21 = false; tip22 = false;
				helpText = this.helpTextWhite;
				break;

			// O and S Keys
			case CMBOKey.O:
				if ((includeTips3 == 1) && (padPosition == 2)) { // Left middle key, Pad right
					includeTips1 = -1;
				}
				tip10 = true; tip11 = true; tip12 = true;
				tip20 = false; tip21 = false; tip22 = false;
				helpText = this.helpTextWhite;
				break;
			case CMBOKey.S:
				if ((includeTips3 == 1) && (padPosition == 2)) { // Left middle key, Pad right
					includeTips1 = -1;
				}
				tip10 = true; tip11 = true; tip12 = true;
				tip20 = false; tip21 = false; tip22 = false;
				helpText = this.helpTextWhite;
				break;

			// Center column keys
			case CMBOKey.TH: //  D/Ä
				includeTips1 = 1;
				tip10 = true; tip11 = false; tip12 = false;
				tip20 = false; tip21 = false; tip22 = false;
				helpText = this.helpTextWhite;
				break;
			case CMBOKey.W: //  L
				includeTips1 = 1;
				tip10 = true; tip11 = true; tip12 = true;
				tip20 = false; tip21 = false; tip22 = false;
				helpText = this.helpTextWhite;
				break;
			case CMBOKey.M: //  M
				includeTips1 = 1;
				tip10 = false; tip11 = true; tip12 = false;
				tip20 = false; tip21 = false; tip22 = false;
				helpText = this.helpTextWhite;
				break;
			case CMBOKey.BACKSPACE:
			case CMBOKey.SPACE:
				tip10 = false; tip11 = true; tip12 = false;
				tip20 = false; tip21 = false; tip22 = false;
				break;

		}


		switch (key) { // === where to show ===   (key = chord value)
			case CMBOKey.A:
				rightEdge2 = true; leftEdge2 = false;
				rightEdge = false; leftEdge = true;
				tipOffset = 1;
				shiftUp = true;
				break;
			case CMBOKey.B:
				rightEdge2 = true; leftEdge2 = false;
				rightEdge = false; leftEdge = true;
				tipOffset = 0;
				shiftUp = true;
				break;
			case CMBOKey.C:
				rightEdge2 = true; leftEdge2 = false;
				rightEdge = false; leftEdge = true;
				tipOffset = 2;
				shiftUp = true;
				break;

			case CMBOKey.D:
				rightEdge2 = false; leftEdge2 = true;
				rightEdge = true; leftEdge = false;
				tipOffset = 1;
				shiftUp = true;
				break;
			case CMBOKey.E:
				rightEdge2 = false; leftEdge2 = true;
				rightEdge = true; leftEdge = false;
				tipOffset = 0;
				shiftUp = true;
				break;
			case CMBOKey.F:
				rightEdge2 = false; leftEdge2 = true;
				rightEdge = true; leftEdge = false;
				tipOffset = 2;
				shiftUp = true;
				break;

			case CMBOKey.G:
			case CMBOKey.K:
				rightEdge2 = false; leftEdge2 = true;
				rightEdge = false; leftEdge = true;
				break;

			case CMBOKey.O:
			case CMBOKey.S:
				rightEdge2 = true; leftEdge2 = false;
				rightEdge = true; leftEdge = false;
				break;

			case CMBOKey.W: // D/Ä
				rightEdge = true; leftEdge = false;
				rightEdge2 = false; leftEdge2 = false;
				break;
			case CMBOKey.TH: // L
			case CMBOKey.M:
				rightEdge = true; leftEdge = false;
				rightEdge2 = true; leftEdge2 = false;
				shiftUp = true;
				tipOffset = 1;
				break;

			case CMBOKey.BACKSPACE:
			case CMBOKey.SPACE:
				break;

		}



		String[] buttons = new String[3];
		String[] buttons2 = new String[3]; // optional letter tips on main buttons
		String[] buttons3 = new String[3]; // yellow mirrored tips on keys G, K, O, S

		// original tips on middle buttons (plus .,!?- on main buttons)
		buttons[0] = this.controller.getButtonString(key, leftColumn ? CMBOKey.D
				: CMBOKey.A);
		buttons[1] = this.controller.getButtonString(key, leftColumn ? CMBOKey.E
				: CMBOKey.B);
		buttons[2] = this.controller.getButtonString(key, leftColumn ? CMBOKey.F
				: CMBOKey.C);

		// optional letter tips on main buttons
		buttons2[0] = this.controller.getButtonString(key, leftColumn ? CMBOKey.G
				: CMBOKey.O);
		buttons2[1] = this.controller.getButtonString(key, leftColumn ? CMBOKey.K
				: CMBOKey.S);
		buttons2[2] = this.controller.getButtonString(key, CMBOKey.E); // for key M only, sign '@'



		Rect hitBox = b.getHitBox();
		Rect visBox = b.getVisibleRect();


		for (int i = 0; i < buttons.length; i++) {


			String bString = buttons[i];
			String bString2 = buttons2[i];

			// includedChars: single: 1, 2, 4; multiple: 3, 5, 6, 7
			// includedChar =


			if (includeTips1 != 1) {
				bString = "";
				buttons[i] = "";
			}

			if (includeTips2 != 1) { // exclude all optional tips on this button)
				bString2 = "";
				buttons2[i] = "";
			}


			switch (i) { // which out of three characters should be shown

				case 0:
					if (!tip10) { // top char
						bString = "";
						buttons[i] = "";
					}
					if (!tip20) { // top char
						bString2 = "";
						buttons2[i] = "";
					}
					break;
				case 1:
					if (!tip11) { // middle char
						bString = "";
						buttons[i] = "";
					}
					if (!tip21) { // middle char
						bString2 = "";
						buttons2[i] = "";
					}
					break;
				case 2:
					if (!tip12) { // bottom char
						bString = "";
						buttons[i] = "";
					}
					if (!tip22) { // bottom char
						bString2 = "";
						buttons2[i] = "";
					}
					break;
				default:
					bString = "";
					buttons[i] = "";
					bString2 = "";
					buttons2[i] = "";
					break;

			}


			boolean symbol = "_Up".equals(bString) || "_Down".equals(bString)
					|| "_Shift".equals(bString);

			if (bString.length() > 1 && bString.startsWith("_") && !symbol) {
				buttons[i] = "";
			}

			if (bString2.length() > 1) { // only single letters shown as optional tips on main buttons
				buttons2[i] = "";
			}



			if (symbol) { // Symbol images on keys A, B, C, D, E, F
				Bitmap bitmap = getImage(buttons[i] + "Help");

				Rect dest = new Rect(hitBox);
				Rect dest2 = new Rect(hitBox); // Additional letter help om main buttons (optional tips)

				canvas.drawBitmap(bitmap, null, dest, helpText); // clear old?
			}

			// && buttons[i].startsWith("_")
			if (buttons[i].length() > 1)
				buttons[i] = "";

			canvas.drawText( // Basic tips: Letters/numbers on middle buttons, punctuation on main buttons
					buttons[i],
					rightEdge ? (visBox.right - helpText.getTextSize() / 2)
							: (visBox.left + helpText.getTextSize() / 2),
					(shiftUp) ? (hitBox.top + hitBox.height() / (float) 3 * (i + 1 + tipOffset) - 4 * scale) // was: -30
					: (hitBox.top + hitBox.height() / (float) 3 * (i + 1 + tipOffset) - 2 * scale)
					, helpText);


			canvas.drawText( // Optional letter tips on Main buttons
					buttons2[i],
					leftEdge2 ? (visBox.left + helpText2.getTextSize() / 2)
							: (visBox.right - helpText2.getTextSize() / 2),
					hitBox.top + hitBox.height() / (float) 2 * (i + 1 + tipOffset2) - hitBox.height() / (float) 8
					, helpText2);

		}

	}


	private void drawButton(CMBOButton button, boolean pressed, Rect rect,
							Canvas canvas) {

		int highlightOption = getApplication().getPreferences().getHighlightOption();
		Drawable buttonDrawable = null;
		Drawable circleDrawable = getResources().getDrawable(
				R.drawable.button_pressed_circle_white_yellow); // 5/2020
		Theme theme = getApplication()
				.getThemeManager().getActiveTheme();

		boolean biggerRect = false;

		int add = (int) (scale * 38); // was: 18, gives about 60 for a 'normal' device screen

		switch (button.getId()) {
			case CMBOKey.O:
			case CMBOKey.S:
				if (pressed) {
					buttonDrawable = theme.getDrawable(DrawableType.PRESSED_KEY,
							this.getContext());
					biggerRect = true;
				} else {
					buttonDrawable = theme.getDrawable(DrawableType.MIDDLE_KEY,
							this.getContext());
				}
				break;

			case CMBOKey.A:
			case CMBOKey.B:
			case CMBOKey.C:
				if (pressed) {
					buttonDrawable = theme.getDrawable(DrawableType.PRESSED_KEY,
							this.getContext());
					biggerRect = true;
				} else {
					buttonDrawable = theme.getDrawable(DrawableType.MAIN_KEY,
							this.getContext());
				}
				break;
			case CMBOKey.G:
			case CMBOKey.K:
				if (pressed) {
					buttonDrawable = theme.getDrawable(DrawableType.PRESSED_KEY,
							this.getContext());
					biggerRect = true;
				} else {
					buttonDrawable = theme.getDrawable(DrawableType.MIDDLE_KEY,
							this.getContext());
				}
				break;
			case CMBOKey.D:
			case CMBOKey.E:
			case CMBOKey.F:
				if (pressed) {
					buttonDrawable = theme.getDrawable(DrawableType.PRESSED_KEY,
							this.getContext());
							biggerRect = true;
				} else {
					buttonDrawable = theme.getDrawable(DrawableType.MAIN_KEY,
							this.getContext());
				}
				break;
			case CMBOKey.TH:
			case CMBOKey.W:
			case CMBOKey.M:
				if (pressed) {
					buttonDrawable = theme.getDrawable(DrawableType.PRESSED_KEY,
							this.getContext());
							biggerRect = true;
				} else

				if (controller.numberMode() || controller.getKeyboardFnMode() || (threeRowPad && !controller.statusDevanagariOn())) {
					// 2017-12 (now always in number mode (including shift/symb/caps), 2020-03 threeRowPad
					// in number mode, 3 rows only have main keys for digits 1 to 9 on them
					// center column uses main button color in number mode and in fn mode
					buttonDrawable = theme.getDrawable(DrawableType.MAIN_KEY,
							this.getContext());

				} else {
					buttonDrawable = theme.getDrawable(DrawableType.TOP_KEY,
							this.getContext());

				}
				break;
			case CMBOKey.BACKSPACE:
				if (pressed) {
					//backspacePressed = true;
					buttonDrawable = theme.getDrawable(DrawableType.PRESSED_KEY,
							this.getContext());
							biggerRect = true;
				} else {
					buttonDrawable = theme.getDrawable(DrawableType.LEFT_BAR_KEY,
							this.getContext());
				}
				break;
			case CMBOKey.SPACE:
				//spacePressed = true;
				if (pressed) {
					buttonDrawable = theme.getDrawable(DrawableType.PRESSED_KEY,
							this.getContext());
							biggerRect = true;
				} else {
					buttonDrawable = theme.getDrawable(DrawableType.RIGHT_BAR_KEY,
							this.getContext());
				}
				break;

			default:
				if (pressed) {
					buttonDrawable = theme.getDrawable(DrawableType.PRESSED_KEY,
							this.getContext());
							biggerRect = true;
				} else {
					buttonDrawable = theme.getDrawable(DrawableType.MAIN_KEY,
							this.getContext());
				}
				break;
		}


		// Animate e.g. by first drawing a larger rect (pic of a circle)

		if (biggerRect) { // pressed/swiped-to buttons
			Rect rectBigger = new Rect(rect.left - add, rect.top - add,
					rect.right + add, rect.bottom + add); //
			if (highlightOption == 1) { // Stronger highlight: A bigger pressed area as long as pressed
				circleDrawable.setBounds(rectBigger);
				circleDrawable.draw(canvas);
			} else if (noSymbolsUpdate) { // Legacy highlight: flash a bigger pressed area first
				circleDrawable.setBounds(rectBigger);
				circleDrawable.draw(canvas);
			} else {
				buttonDrawable.setBounds(rect);
				buttonDrawable.draw(canvas);
			}

		} else { // other buttons

			buttonDrawable.setBounds(rect);
			buttonDrawable.draw(canvas);
		}

	}


		public void checkBackgroundImage(Canvas canvas, int padPosition) {

			Theme theme = getApplication() // 2017-09
					.getThemeManager().getActiveTheme();

			String themeName = theme.getName();

			Log.i("CMBO", "Theme " + themeName + " has been selected in Settings (preferences).");

			if (!themeName.equals(previousThemeName)) { //
				backgroundImage = theme.getDrawable(DrawableType.BACKGROUND_IMAGE, this.getContext());
				//topBarImage = theme.getDrawable(DrawableType.TOPBAR_IMAGE, this.getContext()); // add when themes will include topbar image
				topBarImage = getResources().getDrawable(R.drawable.topbarimage);
				topBarImageAdd = getResources().getDrawable(R.drawable.topbarplus);
				topBarImageMinus = getResources().getDrawable(R.drawable.topbarminus);
				Log.i("CMBO", "New Theme. Changing backgroundImage to: " + backgroundImage);
				previousThemeName = themeName;
			}

			drawSidePanel(padPosition, canvas);
			if (isTopBar) showWordCandidates(canvas);

		}


		public void showWordCandidates(Canvas canvas) {

			CMBOButton a = buttonLayout.getButton(CMBOKey.A);

			String wordCandidate = candidates.getWordCandidate();
			int itemsFound = candidates.itemsFound();
			int itemSelected = candidates.itemSelected();
			// inputConnection must be handled in Service
			if (isTopBar) {

				Log.i("-TOPBAR", "Draw Text on TopBar (showWordCandidates)");
				float x = topBarArea.exactCenterX();
				float y = topBarArea.exactCenterY() + (float) a.getHitBox().height()/4;
				Log.i("-TOPBAR", "Draw Text on TopBar (showWordCandidates) X/Y = " + x + " / " + y);
				Log.i("-TOPBAR", "itemsFound = " + itemsFound + ", itemSelected = " + itemSelected);

				if (itemsFound > 1) {
					if (itemSelected < itemsFound - 1) {
						canvas.drawText(wordCandidate + "\u2026", x, y , modeTextMellowYellow); // several words found, there's more
					} else {
						canvas.drawText(wordCandidate, x, y , modeTextMellowYellow); // the last word found
					}
					canvas.drawText("...", backgroundArea.exactCenterX() // ellipsis "\u2026" is too small
							, backgroundArea.bottom - (float) a.getHitBox().height()/2, modeTextMellowYellow);
				} else if (candidates.itemsFound() > 0) {
					canvas.drawText(wordCandidate, x, y , modeTextGreen); // one word found
					canvas.drawText("   ", backgroundArea.exactCenterX()
							, backgroundArea.bottom - (float) a.getHitBox().height()/2, modeText);
				} else {
					canvas.drawText(wordCandidate, x, y , modeText);
					canvas.drawText("   ", backgroundArea.exactCenterX()
							, backgroundArea.bottom - (float) a.getHitBox().height()/2, modeText);
				}
			}
		}

		private void acceptWordCandidate(int swipeSpanX) {

			String word = candidates.acceptWordCandidate(swipeSpanX);
			if (!word.equals("")) {
				keyboard.outputCharacter(word);
				candidates.clearHint();
			}
			// inputConnection must be handled in Service
		}

		private String localeString = "en_US"; // default

		public void setLocaleString(String locString) {
				this.localeString = locString;
			}

		private void addWordToDictionary() {
			String locString = currentLangISO + "_" + currentCountryISO;
			candidates.addWordToDictionary(locString);

		}

		private void removeWordFromDictionary() {
			String locString = currentLangISO + "_" + currentCountryISO;
			candidates.removeWordFromDictionary(locString);
		}

		public String getModeString() {
			return modeState;
		}

		int toastCounter = 0;
		Toast toastC = Toast.makeText(this.getContext(), "-", Toast.LENGTH_LONG);

		private void toastCandidateWords (String cand) {

			if(!isShowWords()) return;
			toastC.cancel(); //toastC.setDuration(1);
			toastCounter += 1;
			toastC = Toast.makeText(this.getContext(), toastCounter + " " + cand, Toast.LENGTH_LONG);
			toastC.setGravity(Gravity.BOTTOM| Gravity.START, 250, 250);
			toastC.show(); // gravity
		}

		private void drawState(Canvas canvas, int orientation, int padPosition) { // statusArea: abc/Abc/ABC/123/SYMB

		if (noPanel) return;
		if (keepFlashVisible) return;

			// abc button

		float x = statusArea.exactCenterX();
		float y = statusArea.exactCenterY();

		Rect dest = new Rect(statusArea);
		dest.top = statusArea.top + dest.height() / 16;
		dest.bottom = statusArea.bottom + dest.height() / 16;
		dest.left = statusArea.left - dest.width() / 8;
		dest.right = statusArea.right + dest.width() / 8;
		// note: this dest is also used for languageArea's Settings icon size


		statusButton.setBounds(dest); // really abc button
		statusButton.draw(canvas);

		// ---- lang button

			Rect destLang = new Rect(languageArea);

			int centerLangY = destLang.centerY();

			languagesButton.setBounds(destLang);
			languagesButton.draw(canvas);



		int padHeight = getApplication().getPreferences().getPadHeight();

		String modeString = "";

			if (controller.isCtrlDown()) {
				modeString = "Ctrl";
			} else if (controller.isAltDown()) {
				modeString = "Alt";
			} else {
				modeString = abcIndicator; // abc
			}

			modeState = modeString; // remains even if modeString is modified or cleared

			if ((slim == 1) && modeString.length() > 2) {
				modeString = modeString.substring(0,2); // Narrow side panel => shorter text
			} else if (slim == 2) {
				modeString = ""; // No side panel => no text
			}

		// ...Abc and Language indicator

		canvas.drawText(modeString, x,
				y - modeText.getFontMetrics().ascent / 2, modeText); // abc/Abc/ABC...


		// ----- Next... lang

			// NAME: to be translated
			String lang = ""; // Show language only in portrait orientation (no more like this!)
			String lang2 = "";

			// DESCRIPTION: Can be in native language in the JSON MetaData
			String langDescription = getApplication()
					.getLayoutManager().getMainLanguageDescription();
			String lang2Description = getApplication()
					.getLayoutManager().getAuxLanguageDescription();

			String unTrLang = getApplication()
					.getLayoutManager().getMainLanguageName();

			String unTrLang2 = getApplication()
					.getLayoutManager().getAuxLanguageName();




			Log.d("-TRANSLATE", "View: Languages untranslated: " + unTrLang + " and " + unTrLang2);


			if (lang.equals(langDescription)) {
				lang = translatedLanguageName(unTrLang); // Translate if equal 'description' and 'name' in JSON
			} else {
				lang = langDescription;
			}

			if (lang.equals(lang2Description)) {
				lang2 = translatedLanguageName(unTrLang2); //
			} else {
				lang2 = lang2Description;
			}

				Log.d("-TRANSLATE", "View: Languages translated: " + lang + " and " + lang2);

				// more than one aux language is possible now
				//lang2 = this.getContext().getResources().getString(R.string.English); // Always English for now

			if (slim == 1) {
				lang = lang.substring(0,3);
				lang2 = lang2.substring(0,3);
			} else if (slim == 2){
				lang = "";
				lang2 = "";
			}

		int offs = keyboard.getOffset();

		x = languageArea.exactCenterX();
		y = languageArea.exactCenterY();


		//int bottom = languageArea.top;

	Log.d("-Fn", "(VIEW) Language indicator: offset = " + offs + ", aux mode (aMode) = " + aMode);

		if (!controller.isCtrlDown()) { // Show Language indicator
			if (offs < CMBOKey.AUXSET_MODIFIER) {
				canvas.drawText(lang, x, y + modeText.getFontMetrics().ascent / 2, modeTextSmall);
				canvas.drawText(lang2, x, y - modeText.getFontMetrics().ascent / 2, modeTextSmallGray);

			} else if ((offs != CMBOKey.EMOJI_MODIFIER) && (offs != CMBOKey.FN_MODIFIER)) {
				canvas.drawText(lang, x, y + modeText.getFontMetrics().ascent / 2, modeTextSmallGray);
				canvas.drawText(lang2, x, y - modeText.getFontMetrics().ascent / 2, modeTextSmall);
			}
		} else { // in case Ctrl is down, show Settings instead of Language indicator

			if (Build.VERSION.SDK_INT >= 21) { // getDrawable() requires API 21 up

				Drawable d = this.getContext().getResources().getDrawable(R.drawable.settings_shadow, null);
				//d.setBounds(left, top, right, bottom);
				d.setBounds((int) x - dest.height() / 3, (int) y - dest.height() / 3,
						(int) x + dest.height() / 3, (int) y + dest.height() / 3);
				d.draw(canvas);

			} else { // lower API will only show text "Set.."
				canvas.drawText(this.getContext().getResources().getString(R.string.flashtext_settings), x, y, helpText);
			}
			// open Settings...
		}

	}


	private void updateLanguageIndicator(Canvas canvas, int offs, float x, float y, String lang, String lang2) {

		if (!controller.isCtrlDown()) { // Show Language indicator
			if (offs < CMBOKey.AUXSET_MODIFIER) {
				canvas.drawText(lang, x, y + modeText.getFontMetrics().ascent / 2, modeTextSmall);
				canvas.drawText(lang2, x, y - modeText.getFontMetrics().ascent / 2, modeTextSmallGray);

			} else if (offs != CMBOKey.EMOJI_MODIFIER) {
				canvas.drawText(lang, x, y + modeText.getFontMetrics().ascent / 2, modeTextSmallGray);
				canvas.drawText(lang2, x, y - modeText.getFontMetrics().ascent / 2, modeTextSmall);
			}
		} else { // in case Ctrl is down, show Settings instead of Language indicator
			canvas.drawText(this.getContext().getResources().getString(R.string.settingsbutton), x, y, helpText);
			// open Settings...
		}
	}


	private String previousLang = ""; // translated language names previously valid
	private String previousLang2 = ""; // (aux can be other than English now)
	private String previousUnTrLang = ""; // untranslated language names previously valid
	private String previousUnTrLang2 = ""; // for aux lang

	public String translatedLanguageName(String lang) {

		String trLang = lang;

		try { // depends on whether language name/translations are listed or not in strings.xml
			trLang = this.getContext().getResources().getString(getStringIdentifier(this.getContext(), lang));
		} catch (Exception e) {
			trLang = lang; // not listed, use non-translated name
		}

		Log.d("-TRANSLATE", "View: Language name " + lang + " re-translated to " + trLang);
		return trLang;
	}

	// This will convert a variable string to corresponding int identifier of the same string
	// to be used in (R.string...). However this uses reflection and is not so fast!
	// TODO: consider using array.xml instead of strings .xml
	private static int getStringIdentifier(Context context, String name) {
		return context.getResources().getIdentifier(name, "string", context.getPackageName());
	}


	private void drawFlashSymbol(Canvas canvas, String flashString,
								 int orientation, int padPosition,
								 int padType, int padHeight)
	{ // Tools button for Settings

		if (noPanel) return;

		if (passwordMode || !isSymbolFlashOn() || (slim == 2)) return; // no flash in use or no room for flash (no side panel)
		// no flash of combo symbols other than in portrait orientation

			float x = flashArea.exactCenterX();
			float y = flashArea.exactCenterY();

		Log.d("-FLASH", "Flash on keypad side panel area. Center at x/y = " + x + "/" + y + " of canvas.");

		String bString = flashString; // controller.getButtonString(b);

		if (bString.length() > 11) {return;} // Skip long text

		if (bString.length() < 2) { // Always show single signs

			canvas.drawText(bString, x, y - flashText.getFontMetrics().ascent / 2, flashText);

		} else { // Show strings of reasonable length

			// Remove _ at the beginning or space at the end
			if (bString.startsWith("_")) {
				bString = bString.substring(1);
				// Here we can translate the flashed text when appropriate:
				bString = translatedFlashText(bString);
				// ----
			} else if (bString.endsWith(" ")) {
				bString = bString.substring(0, bString.length() - 1);
			}

			if ((padPosition == 3) && (bString.length() > 4)) { // narrow pad at center, not much room
				bString = bString.substring(0, 4) + ".." ; //bString.substring(0, 2) + "...";
				canvas.drawText(bString, x, y - white.getFontMetrics().ascent / 2, flashTextSmall);
			} else if (bString.length() < 7) { // Shorter string
				canvas.drawText(bString, x, y - white.getFontMetrics().ascent / 2, flashTextMedium);
			} else { // longer string
				canvas.drawText(bString, x, y - white.getFontMetrics().ascent / 2, flashTextSmall);
			}
		}
	}


	private String translatedFlashText(String flashText) {
		// Only translate: Received, Send, Lang, Translate, Search, Copy, Paste,

		if (flashText.length() < 4) { // No change or get native text from JSON

			// Get native JSON text for 123/abc/Abc/ABC/(not SYMB)
			if ("123".equals(flashText)) {
				flashText = getApplication()
						.getLayoutManager().getAbcIndicator(CMBOKey.NUMBER_MODIFIER);
			} else if ("abc".equals(flashText)) {
				flashText = getApplication()
						.getLayoutManager().getAbcIndicator(CMBOKey.NONE);
			} else if ("Abc".equals(flashText)) {
			flashText = getApplication()
					.getLayoutManager().getAbcIndicator(CMBOKey.SHIFT_MODIFIER);
			} else if ("ABC".equals(flashText)) {
				flashText = getApplication()
						.getLayoutManager().getAbcIndicator(CMBOKey.CAPS_MODIFIER);
			}

				return flashText; // if no deal above, keep as is
			}

		// length >=
		if ("Received".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_received);
		} else	if ("Call".equals(flashText)) {
				flashText = this.getContext().getResources().getString(R.string.flashtext_call);
		} else if ("Send".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_send);
		} else if ("Play".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_speak);
		} else if ("Record".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_record);
		} else if ("Lang".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_lang);
		} else if ("Translate".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_translate);
		} else if ("Search".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_search);
		} else if ("Copy".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_copy);
		} else if ("Paste".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_paste);
		} else if ("Repeat".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_repeat);
		} else if ("Notes".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_notes);
		} else if ("Launch".equals(flashText)) {
			//flashText = this.getContext().getResources().getString(R.string.flashtext_launch);
			flashText = getApplication().getPreferences().getAppToLaunch();
			if (flashText.equals("")) flashText = this.getContext().getResources().getString(R.string.settings_launch_default);
		} else if ("SYMB".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_symbols); // = "#@"
		} else if ("Messages".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_received);
		} else if ("Settings".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_settings);
		} else if ("ShiftDown".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_shiftdown);
		} else if ("Emoji".equals(flashText)) {
			flashText = this.getContext().getResources().getString(R.string.flashtext_emoiji); // = :)
	}

		return flashText;
	}


	private boolean omitDrawButtonSymbol(CMBOButton button, boolean skipSymbols) {

		boolean hide = false; // default = show
		// If Magic Other Hand is on, hide symbols on same column even if they function
		if (skipSymbols || noSymbolsUpdate) hide = true; // orig
		return hide; // show the rest
	}



	private void drawButtonSymbol(Canvas canvas, CMBOButton button, boolean skipSymbols) {

		int secondKey = controller.getKey(); // added to switch text color of swiped-to key

		float x = button.getVisibleRect().exactCenterX();
		float y = button.getVisibleRect().exactCenterY();
		// float y = button.getVisibleRect().bottom;

		// TODO just an approximation for vertical font alignment on buttons
		int amount = (int) (black.getTextSize() / 2.8); // 2018-01, was: (int) black.getTextSize() / 4;

		Paint paint = white;

		String bString = controller.getButtonString(button);
		// controller decides which symbols are shown and which hidden (same side etc.)

		if (omitDrawButtonSymbol(button, skipSymbols)) { // Do not yet update symbols on keys
			if (noSymbolsUpdate) bString = controller.getButtonStringUnpressed(button);
		}

		switch (button.getId()) {
			case CMBOKey.A:
			case CMBOKey.B:
			case CMBOKey.C:
			case CMBOKey.D:
			case CMBOKey.E:
			case CMBOKey.F:
				paint = white;
				break;
			case CMBOKey.G:
			case CMBOKey.K:
			case CMBOKey.O:
			case CMBOKey.S:
			case CMBOKey.BACKSPACE:
			case CMBOKey.SPACE:
				paint = gray;
				break;
			case CMBOKey.TH:
			case CMBOKey.W:
			case CMBOKey.M:
				if (controller.numberMode() || (threeRowPad && !controller.statusDevanagariOn())) {
					paint = white;
				} else {
					paint = gray;
				}
				break;
		}

		// Switch symbol color when second key is highlighted
		if (secondKey == button.getId()) {
			paint = black;
		}

		boolean useBlue = true;

		// Show as text (_Xxx):
		if (bString.startsWith("_")) { // to speed up string comparision
			if ("_Esc".equals(bString)
							|| "_Ins".equals(bString) || "_Ctrl".equals(bString)
							|| "_Alt".equals(bString) || "_Meta".equals(bString))
			{
				bString = bString.substring(1); // Remove '_'
				useBlue = false;
			}
		}

		// Show as image (_Xxx):
		if ((bString.startsWith("_") && bString.length() > 1) // small font size if longer text
				|| " ".equals(bString)) {
			//paint = smallWhite;
			paint = (secondKey == button.getId()) ? smallBlack : smallWhite; // a highlighted second key?
			Bitmap bitmap = getImage(bString);
			Rect dest = getFittingRect(button);
			canvas.drawBitmap(bitmap, null, dest, paint);

		} else { // Show as text:

			if (bString.length() != 1 ) {

				if (bString.startsWith("F")) useBlue = false; // for F1...F12 (just a quick hack)

				if (bString.length() > 4) { // was > 4
					amount = (int) amount / 2; // vertical alignment higher up for smaller text
					paint = (secondKey == button.getId()) ? smallBlack : smallBlue;// a highlighted second key?
				} else if (bString.length() > 3) {
					amount = (int) amount / 2; // vertical alignment higher up for smaller text
					paint = (secondKey == button.getId()) ? smallBlackPlus : smallWhitePlus; // not blue: Ctrl, Open, Same, Meta
				} else if (bString.length() > 2) {
					amount = (int) (black.getTextSize() / 3.6);
					//amount = (int) amount / 2; // vertical alignment higher up for smaller text
					if (useBlue) {
						paint = (secondKey == button.getId()) ? mediumBlack : mediumBlue;// a highlighted second key?
					} else {
						paint = (secondKey == button.getId()) ? mediumBlack : mediumWhite; // not blue: Esc, Ctrl, Alt
					}
				} else if (bString.length() > 1) {
					if (useBlue) {
						paint = (secondKey == button.getId()) ? black : blue;
					} else {
						paint = (secondKey == button.getId()) ? black : mediumWhite;
					}
				}
			}

			canvas.drawText(bString.trim(), x, y + amount, paint);

		}
	}

	private Rect getFittingRect(CMBOButton button) {
		Rect dest = new Rect(button.getVisibleRect());

		int centerY = dest.centerY();
		dest.top = centerY - dest.height() / 2;
		dest.bottom = centerY + dest.height() / 2;

		int margin = dest.height() / 8; // was / 4
		dest.top += margin;
		dest.bottom -= margin;
		int width = dest.width();
		dest.left += (width - dest.height()) / 2;
		dest.right -= (width - dest.height()) / 2;

		return dest;
	}

	public void redraw() {
		Log.d("-REDRAW", "View: redraw = invalidate(). noSymbolsUpdate = " + noSymbolsUpdate);
		Log.d("-VIEW", "(view) redraw()");
		invalidate(); // for UI Thread
		// postInvalidate(); // for non-UI thread
		if (isTopBar) candidates.checkDictionary(); // 2020-03-31
	}

	private boolean down(SimpleTouchEvent event) {
		// get which key location was pressed (disregard the state, just get the
		// location)
		//int state = controller.getState(); // needed for hysteresis, do not use hysteresis if two keys down
		int key = getPressedVirtualKey(event);
		Log.d("CMBO", "View: checking, pressed key: " + key + " -> state");
		if (key != CMBOKey.NONE) {
			// forward to controller
			Log.d("CMBO", "View: forward to controller, pressed key != NONE");
			return controller.processKeyPress(key, event.getPointerIndex());
		}

		return false;
	}

	/**
	 * Check which key position was pushed
	 *
	 * @param event
	 * @return
	 */

	private int getPressedVirtualKey(SimpleTouchEvent event) {

		int x = (int) event.getX();
		int y = (int) event.getY();

		int deltaX = event.getDeltaX();
		int deltaY = event.getDeltaY();

		// When going to preferences and changing to or from tablet
		// layout, the buttonLayout is set to null. Then if there is an
		// unprocessed touch event when returning to the activity, we get a
		// NullPointerException. The below is a workaround.
		if (buttonLayout == null)
			return CMBOKey.NONE;

		boolean oneDown = controller.oneDown();
		boolean twoDown = controller.bothDown();
		// boolean oneDown2 = controller.oneDown2();

		CMBOButton button = buttonLayout.getHitKey(x, y, oneDown, twoDown, deltaX, deltaY);

		int l = buttonLayout.getButtons().lastIndexOf(button);
		Log.d("CMBO", "View: button = " + button + ". button index = " + l + ". Id = " + (button != null ? button.getId() : CMBOKey.NONE));

		if (button != null) {
			return button.getId();
		} else {
			//
			return CMBOKey.NONE;
		}

	}

	public boolean isKeysDisabled() {
		return getApplication().getPreferences()
				.isKeysDisabled();
	}

	public boolean isLangComboDisabled() { // no switch between main and aux languages by key pad combo
		return getApplication().getPreferences()
				.isLangComboDisabled();
	}

	public boolean isTrailingSpaceRemoved() { // no switch between main and aux languages by key pad combo
		return getApplication().getPreferences()
				.isTrailingSpaceRemoved();
	}
	public boolean isEasyModeChangeDisabled() { // no switch between main and aux languages by key pad combo
		return getApplication().getPreferences()
				.isEasyModeChangeDisabled();
	}

	public boolean isSymbolFlashOn() { // combo symbol flash next to the keypad
		return getApplication().getPreferences()
				.isSymbolFlashOn();
	}

	public void setService(boolean service) {
		this.service = service;
	}

	private void vibrate() {
		if (getApplication().getPreferences().get()
				.getBoolean("PREFERENCES_VIBRATOR_ON", false)) {
			Vibrator vibrator = (Vibrator) getContext().getSystemService(
					Context.VIBRATOR_SERVICE);
			try {
				vibrator.cancel();
				vibrator.vibrate(20);
				Log.d("-VIBRATOR", "vibrate()");
			} catch (Exception e) {
				Log.d("-VIBRATOR", "vibrate() error: " + e);
			}
		}
	}


	private boolean abcDown = false;
	int downX, downY;

	private int startX = 0;
	private int startY = 0;
	private boolean topBarDown = false;

	public void touchEvent(SimpleTouchEvent event) {

		try { // added for v.1.5 to avoid NullPointerException
			downX = (int) event.getX(); // float to integer
			downY = (int) event.getY();
		} catch (Exception e) {
			downX = 0;
			downY = 0;
			Log.d("-TOUCHEVENT", "touchEvent() in View. Error = " + e);
			return;
		}

		Log.d("-TOUCHEVENT", "touchEvent() in View. downX = " + downX + ", downY = " + downY);
		//passThrough = (downX < 220); 	// Test value only, pass touch event through under the
										// transparent keyboard horizontal area

		soundIndex = 4; // default

		switch (event.getType()) {
			case POINTER_DOWN:

				soundQue += 1;
				newKeyDown = true;      // on keypad or on any other place,
				// readyToPlaySound is required as well before sound

				if ((statusArea == null) || (languageArea == null)) { // prevent a rare crash 2018-09-02
					//firstRedrawDone = false;
					//pressTimer.start(); // redraw with delay
					break;
				}

			if (controller.getState() == CMBOKey.NONE) {
					Log.d("-TOUCH-POINTER DOWN", "state = NONE");


				// Abc pressed
				// ===========
				if (statusArea.contains(downX,  // Several crash reports here on v3.7.
						downY)) {                // Fix: statusArea now has no negative x y values on v3.8


					Log.d("-TOUCH-POINTER DOWN", "Abc (status area) pressed (tbd: Shift)."); // TODO: ***

					abcDown = true; // pressing abc indicator button, long or short, POINTER_UP handles this
					readyToPlaySound = true;
					soundIndex = 4;
					// Delay Shift until indicator button release
					//getApplication()
					//		.getKeyboard().shiftPressed();

					// Language pressed
					// ================
				} else if (languageArea.contains(downX,
						downY)) {

					Log.d("-TOUCH-POINTER DOWN", "Language indicator area pressed (tbd: switch between main and aux languages).");

					readyToPlaySound = true;
					soundIndex = 4;

					// Way to set Preferences from keyboard:
					if (controller.isCtrlDown()) { // Settings: Ctrl + press Language indicator

						this.invalidate();
						((CMBOKeyboardService) this.getContext()).handleSettings();

						controller.setCtrl(false);
						Log.d("-TOUCH-POINTER DOWN", "*** Calling Settings from KeyboardView (Ctrl + Language indicator)");
						Log.d("-SETTINGS", "*** Calling Settings from KeyboardView (Ctrl + Language indicator)");
						break;
					}

					handleLang();

				// Word Candidates
				} else if (isTopBar && backgroundArea.contains(downX, downY)) {
					candidates.browseCandidates(1); // Browse (1 = forward) word candidates by tapping on '...' on side panel
				} else if (isTopBar && topBarAreaAdd.contains(downX, downY)) { // left part of topBar (green '+')
					addWordToDictionary();
				} else if (isTopBar && topBarAreaMinus.contains(downX, downY)) { // right part of topBar (red 'X')
					removeWordFromDictionary();
				} else if (isTopBar && topBarArea.contains(downX, downY)) {
					topBarDown = true;
					//if (isTopBar) acceptWordCandidate();

					// ===========================================
					// Key pressed but state not registered yet (?):
					// ===========================================
				} else if (down(event)) {
					Log.d("-TOUCH-POINTER DOWN", "pointer down redraw1 (state = NONE still). pressTimer.start");
				}

				// =================================================
				// Key pressed (does not occur, however. See above.):
				// =================================================

			} else if (down(event)) {
				Log.d("-TOUCH-POINTER DOWN", "pointer down redraw2 (state != NONE). pressTimer.start");
			}

		int deltaX = event.getDeltaX();
		int deltaY = event.getDeltaY();

		Log.d("-TOUCH-POINTER DOWN", "pointer down exit (all cases), deltaX and Y: " + deltaX + ", " + deltaY);
				firstRedrawDone = false;
				pressTimer.start(); // redraw with delay (Redraw only here now, 2019-08-06)
				break;

			case POINTER_UP:


				autoRepeat = false; candidates.autorepeatOn = false;
				repeatTimer.cancel();
				controller.release(event.getPointerIndex());

				if (abcDown) { // Shift (pressing abc indicator) is delayed until this point due to autorepeat
					abcDown = false;
					getApplication()
							.getKeyboard().abcPressed();
				}
				//vibrate();
				swipeSpanX = event.getDeltaX();// swipeSpanX = startX - endX;
				swipeSpanY = event.getDeltaY();// swipeSpanY = startY - endY;

				buttonLayout.clearPreviousListButton(); // forget previously hit key, hysteresis

				int maxSpan = getSwipeSpan(); // TEST for debug
				Log.d("-TOUCH-POINTER UP", "pointer up redraw. Swipe span X = " + swipeSpanX + ", Swipe span Y = " + swipeSpanY + ", Max = " + maxSpan);

				//isSwipeSpanReal(); // TEST
				if (isTopBar && this.topBarDown) {
					acceptWordCandidate(swipeSpanX);
					this.topBarDown = false;
				}

				redraw();
				if (getApplication().getPreferences().isSoundEnabled()) {
					mpPlaySound(soundIndex, 1); // Test
				}

				break;
			case POINTER_MOVE:
				Log.d("-TOUCH-POINTER MOVE", "pointer move...");
				//redraw();
				if (down(event)) {
					deltaX = event.getDeltaX();
					deltaY = event.getDeltaY();
					Log.d("-TOUCH-POINTER MOVE", "pointer move redraw, deltaX and Y: " + deltaX + ", " + deltaY);
					redraw();
				}
				break;
		}

		//redraw();

	}



	public boolean isSwipeSpanReal() { // a short swipe at button border will not be a real swipe (info not used yet)
		int span = (int) (40 * scale);
		boolean result = getSwipeSpan() > span;
		Log.d("-TOUCH-POINTER MOVE", "Swipe span is real = " + result);
		return result;
	}

	private int getSwipeSpan() {
		int spanX = swipeSpanX;
		int spanY = swipeSpanY;
		if (spanX < 0) spanX = -spanX;
		if (spanY < 0) spanY = -spanY;
		if (spanX > spanY) {
			Log.d("-TOUCH-POINTER MOVE", "Final swipe span (X) = " + spanX);
			return  spanX;
		} else {
			Log.d("-TOUCH-POINTER MOVE", "Final swipe span (Y) = " + spanY);
			return spanY;
		}
	}


	private boolean keepFlashVisible = false;

	// 750 250
	final CountDownTimer flashTimer = new CountDownTimer(600, 384) { // 500, 256 (time in ms to onFinish(), Tick interval while counting)
		@Override
		public void onTick(long l) {
			Log.d("-TIMER", "- Flash timer onTick");

		}
		@Override
		public void onFinish() {
			Log.d("-TIMER", "- Flash timer done, redraw also symbols after delay");
			keepFlashVisible = false;
			redraw();
		}
	}.start();

	// Second autorepeat method (combo pressed, not Abc button)
	final CountDownTimer repeatTimer = new CountDownTimer(1000, 400) { // (time in ms to onFinish(), Tick interval while counting)
		@Override
		public void onTick(long l) {
			Log.d("-TIMER", "- Repeat timer onTick");
		}
		@Override
		public void onFinish() {
			Log.d("-TIMER", "- Repeat timer done, start autoRepeatPreviousCharacter()");
			//if (controller != null) controller.autoRepeat("");
			if (controller != null) autoRepeatPreviousCharacter();
		}
	}.start();


	private boolean firstRedrawDone = false;

	final CountDownTimer pressTimer = new CountDownTimer(100, 10) { // (time in ms to onFinish(), Tick interval while counting)
		@Override
		public void onTick(long l) {
			Log.d("-TIMER", "- Symbols redraw timer: onTick (redraw only on first tick)");
			if (!firstRedrawDone) {
				Log.d("-TIMER", "- Symbols redraw timer: First redraw after press, highlights only (first tick)");
				noSymbolsUpdate = true;
				redraw(); // update button highlights
			}
			firstRedrawDone = true;
		}
		@Override
		public void onFinish() {
			Log.d("-TIMER", "- Symbols redraw timer: timer done, redraw also symbols after delay");
			// Problem: onDraw only follows this in Portrait orientation
			noSymbolsUpdate = false; // now also update symbols on keys
			//keepFlashVisible = false; // commented 2019-09-19 for single tap flash operation
			redraw(); // update symbols
			//cancel();
		}
	}.start();

	private void mapButtonStringsToImages() {
		// map string representations to icons

		generateImage("_Received", R.drawable.arrow_received_edges);
		generateImage("_Menu", R.drawable.shifts_edges); // Changed use of this button: Menu => Shift
		generateImage("_Send", R.drawable.arrow_send_edges);
		generateImage("_Play", R.drawable.speak_play);
		generateImage("_Record", R.drawable.speak_rec);
		generateImage("_Play!", R.drawable.speak_play_red);
		generateImage("_Record!", R.drawable.speak_rec_red);
		generateImage("_Stop", R.drawable.speak_stop);
		generateImage("_Copy", R.drawable.copy_edges);
		generateImage("_Paste", R.drawable.paste_edges);
		generateImage("_Translate", R.drawable.flag_s);
		generateImage("_Lang", R.drawable.lang);
		generateImage("_Callback", R.drawable.outgoing_call_back_edges);
		generateImage("_Call", R.drawable.outgoing_call_start_edges);
		generateImage("_Search", R.drawable.search_edges);

		generateImage("_Clear", R.drawable.clear2ws);
		//generateImage("_Ctrl", R.drawable.ctrl);
		generateImage("_BS", R.drawable.backspaces_edges);
		generateImage("_Del", R.drawable.deletes_white);
		generateImage("_Up", R.drawable.ups_edges);
		generateImage("_Down", R.drawable.downs_edges);

		generateImage("_Left", R.drawable.lefts_edges);
		generateImage("_Right", R.drawable.rights_edges);
		generateImage("_abc123", R.drawable.abcnums_edgesw);
		generateImage("_End", R.drawable.ends_edges);
		generateImage("_Enter", R.drawable.enter_edges_text);
		generateImage("_Home", R.drawable.homes_edges);
		generateImage("_Tab", R.drawable.tabs_edges);
		generateImage("_WLeft", R.drawable.wlefts_edges);
		generateImage("_WRight", R.drawable.wrights_edges);
		generateImage("_PgUp", R.drawable.pgup_edges);
		generateImage("_PgDn", R.drawable.pgdn_edges);
		generateImage(" ", R.drawable.spaces_edges);
		generateImage("_Shift", R.drawable.shift_w); // abc => Abc = ABC
		generateImage("_SYMB", R.drawable.symby);

		generateImage("_UpHelp", R.drawable.blank);  // was uphelp // not shown as help to avoid messy impact
		generateImage("_DownHelp", R.drawable.blank); // was downhelp
		generateImage("_ShiftHelp", R.drawable.blank); // was shift_w

		generateImage("_Tools", R.drawable.tools);
		generateImage("_Emoji", R.drawable.emoj);
		generateImage("_User", R.drawable.userstrings);
		generateImage("_Keyboard", R.drawable.kbd_icon_150);
		generateImage("_Hide", R.drawable.kbd_hide_150g2);
		generateImage("_Fn", R.drawable.fn);
		generateImage("_Settings", R.drawable.settings_shadow);

		generateImage("_123", R.drawable.num123y); // 'ABC' switch to numbers
		generateImage("_abc", R.drawable.abc2y); // '123' switch to letters
		generateImage("_ShiftDown", R.drawable.shift_w_down); // (shift pointing down) Abc => abc => ABC
		generateImage("_Notes", R.drawable.show_notes); // take activity_notes, save text
		generateImage("_Add", R.drawable.append_note); // take activity_notes, save text
		generateImage("_Save", R.drawable.save_notes); // take activity_notes, save text
		generateImage("_Get", R.drawable.get_notes); // take activity_notes, save text
		generateImage("_Launch", R.drawable.launch_app); // launch user-defined application
		generateImage("_Repeat", R.drawable.repeat); // launch user-defined application
		generateImage("_More", R.drawable.more); // Switch to more functions

		generateImage("_PadUL", R.drawable.pad_ul);
		generateImage("_PadCW", R.drawable.pad_cw);
		generateImage("_PadUR", R.drawable.pad_ur);
		generateImage("_PadB", R.drawable.pad_b);
		generateImage("_PadU", R.drawable.pad_u);
		generateImage("_PadS", R.drawable.pad_s);
		generateImage("_PadL", R.drawable.pad_l);
		generateImage("_PadR", R.drawable.pad_r);
		generateImage("_Pad5R", R.drawable.pad_5r);
		generateImage("_Pad3R", R.drawable.pad_3r);
		generateImage("_PadLL", R.drawable.pad_ll);
		generateImage("_PadCN", R.drawable.pad_cn);
		generateImage("_PadD", R.drawable.pad_d);
		generateImage("_PadLR", R.drawable.pad_lr);

		generateImage("_File", R.drawable.file);
	}

	private Bitmap getImage(String buttonString) {
		Bitmap bitmap = imageMap.get(buttonString);

		if (bitmap == null) {
			bitmap = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.icon);
		}

		return bitmap;
	}

	private final Map<String, Bitmap> imageMap = new HashMap<String, Bitmap>();

	private void generateImage(String key, int res) {
		Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), res);
		imageMap.put(key, bitmap);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
										  String key) {
	}

	// float downX, downY; // moved up
	public static boolean autoRepeat = false; // static: common to all instances of this class

	public boolean onLongClick(View v) {
		Log.d("CMBO", "Long click at " + downX + ", " + downY + " -- "
				+ statusArea.contains(downX, downY));

		if (statusArea.contains(downX, downY)) {

			abcDown = false; // forget Shift because we are now using autorepeat instead
			autoRepeatPreviousCharacter(); // "" = repeat previous char

		}

		return true;
	}

	public boolean isAutorepeat() {
		return autoRepeat;
	}

	private void autoRepeatPreviousCharacter() {
				autoRepeat = true; candidates.autorepeatOn = true;
				int extraSpeed = 0;
		Runnable r = new Runnable() {
			public void run() {
				int repeatCounter = 0;
				int repeatMillis = 250; // slow speed 250, mid speed 125, fast speed 64, very fast 32
				while (autoRepeat) {
					CMBOKeyboardView.this.post(new Runnable() {
						public void run() {
							controller.autoRepeat(""); // "" means previous character (needs to be a static value here)
						}
					});
					repeatCounter += 1;

					if (repeatCounter > 80) {
						repeatMillis = 32; // move very fast now
					} else if (repeatCounter > 32) {
						repeatMillis = 64; // move fast now
					} else if (repeatCounter > 16) {
							repeatMillis = 125; // move faster now
					}

					try {
						Thread.sleep(repeatMillis);
					} catch (InterruptedException e) {
						//controller.stopAutorepeat(); // added 2017
					}
				}
				controller.stopAutorepeat();
			}
		};
		Thread t = new Thread(r);
		t.start();
	}


	private void showToast(String toastMessage, boolean inUse){

		if (!inUse) return;

		Toast toast;
		toast = Toast.makeText(this.getContext(), toastMessage, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();

	}


	private void handleLang() { // 2017-02-02

		// this handles the tap on language indicator area only (not Lang selected on the keypad)

		int offs = keyboard.getOffset();

		// get present values:
		aMode = !getApplication()
				.getLayoutManager().getMainLanguageActive();


		// ---------- switch languages: -----------------

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

		if (offs < CMBOKey.AUXSET_MODIFIER) {
			//offs = offs + CMBOKey.AUXSET_MODIFIER;
			offs = CMBOKey.AUXSET_MODIFIER;
			aMode = true; // 2019-03-20
		} else {
			//offs = offs - CMBOKey.AUXSET_MODIFIER;
			offs = CMBOKey.NONE;
			aMode = false; // 2019-03-20
		}

		// save value:
		getApplication()
				.getLayoutManager().setMainLanguageActive(!aMode);



		// ---------------- Display new status: -------------------------------

		abcIndicator = getApplication()
				.getLayoutManager().getAbcIndicator(offs);


		String msgStart = ""; String msg = "";

		if (aMode) {
			msgStart = this.getContext().getResources().getString(R.string.aux_language_selected) + ": ";

			msg = translatedLanguageName(getApplication()
					.getLayoutManager().getAuxLanguageName()) + " - " + getApplication()
					.getLayoutManager().getAuxLanguageDescription();

		} else {
			msgStart = this.getContext().getResources().getString(R.string.main_language_selected) + ": ";

			msg = translatedLanguageName(getApplication()
					.getLayoutManager().getMainLanguageName()) + " - " + getApplication()
					.getLayoutManager().getMainLanguageDescription();

		}

		controller.checkDevOffsetMode(); // checkDevanagariEtc

		showToast(msgStart + msg, true);

		Log.d("-DEV", "(view) DevanagariOn status = " + controller.statusDevanagariOn());


		// --------------------------------------------------


		Log.d("-Fn", "(VIEW) Switched main/aux languages: offset = " + offs + ", aux mode (aMode) = " + aMode);
		// switch between main and aux language sets


		getApplication()
				.getKeyboard().setLangOffset(offs, aMode);

		CMBOKeyboardService service = (CMBOKeyboardService) this
				.getContext();

		String speechLang = aMode ? getApplication()
				.getLayoutManager().getAuxLanguageName() : getApplication()
				.getLayoutManager().getMainLanguageName();

		String currentLangISO = service.getCurrentLanguageISO();
		String currentCountryISO = service.getCurrentCountryISO();

		if (currentLangChanged(currentLangISO))	{
			Log.d("-SPEECH", "(VIEW) service.setSpeechLang(" + currentLangISO + ")");
			service.setSpeechLang(currentLangISO, currentCountryISO);
			service.setVoiceInputLang();
		}

	}

}
