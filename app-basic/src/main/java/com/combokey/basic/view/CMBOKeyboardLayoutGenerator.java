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

import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.Log;

import com.combokey.basic.CMBOKey;
import com.combokey.basic.CMBOKeyboardApplication;

public class CMBOKeyboardLayoutGenerator {

    public static CMBOKeyboardButtonLayout generateLayout(CMBOKeyboardView view) {

        CMBOKeyboardButtonLayout buttonLayout = null;

        boolean orientationHack = false;

        // padType
        final int PADTYPE_THREE_R0W = 10;
        final int PADTYPE_FIVE_R0W = 0;
        // padHeight
        final int PADHEIGHT_HIGHER_SMALLER = 10;
        final int PADHEIGHT_TALL_DEFAULT = 0;
        final int PADHEIGHT_LOWER_SMALLER = 20;

        boolean noPanel = CMBOKeyboardApplication.getApplication().getPreferences().isSidePanelHidden();
        boolean noTopBar = !CMBOKeyboardApplication.getApplication().getPreferences().getDisplayWordCandidates();


        int orientation = CMBOKeyboardApplication.getApplication().getResources().getConfiguration().orientation;
        int landscapeColumns = CMBOKeyboardApplication.getApplication().getPreferences().getLandscapeColumns();
        // landscapeColumns = 3, 5 or 6 (standard 5-row, special 3-row, double 5-row):
        int padPosition = CMBOKeyboardApplication.getApplication().getPreferences().getPadPosition();
        // padPosition: 0 = left, 1/3 = center wide/narrow, 2 = rightint
        int padPositionUnchanged = padPosition;
        int padHeight = CMBOKeyboardApplication.getApplication().getPreferences().getPadHeight();
        // padHeight: 10 = Higher smaller, 0 = Normal, 20 = Lower smaller:
        int padType = CMBOKeyboardApplication.getApplication().getPreferences().getPadType();
        // padType: 0 = 5-row standard, 10 = 3-row special

        //padHeight = 30; // Due to old settings in devices, could CRASH if not taken care of:
        if (padHeight == 30) { // to guarantee backwards compatibility (padHeight = 30 no more used)
            padHeight = 10; // portrait orientation 3-row special pad
            padType = PADTYPE_THREE_R0W; // 10;
        }
        int scaleAmount;
        boolean xLarge = view.isXLarge();

        //if (landDouble) padPosition = 4; // NEW value
        // * padPosition:
        boolean padLeft = (padPosition == 0);
        boolean padRight = (padPosition == 2);
        boolean padCenterWide = (padPosition == 1);
        boolean padCenterNarrow = (padPosition == 3);
        // portDouble = (padPosition == 4); // NEW
        // * padHeight
        boolean upperSmall = (padHeight == 10);
        boolean fullSize = (padHeight == 0);
        boolean lowerSmall = (padHeight == 20);
        // * padType (now a common setting for both orientations!)
        boolean port5Row = (padType == 0);
        boolean port3Row = (padType == 10);
        // * landscapeColumns
        boolean land5Row = (landscapeColumns == 3); // single landscape pad
        boolean land3Row = (landscapeColumns == 5); // not used any more, use padType for 5 or 3 rows
        boolean doublePad = (landscapeColumns == 6);  // double landscape pad // use padType for row5/row3
        // * deviceType
        boolean phone = !xLarge;
        boolean tablet = !phone;
        // orientation
        boolean land = (orientation == Configuration.ORIENTATION_LANDSCAPE);
        // * 5 Rows / 3 Rows
        boolean rows5 = port5Row; // (land && (land5Row)) || (!land && (port5Row));
        boolean rows3 = port3Row; //!rows5;



        int width = view.getWidth() - 30; // 2019-02 added 20 (= 10 + 10) margin for keypad frames
        int height = view.getHeight() - 20; // was: no scaling

        int padOption = padPosition + padHeight; // combined position left/center/right/upper/lower/

        if ((padHeight < 20) && (padType == PADTYPE_FIVE_R0W)) { // 0 = 5-row, just fill parent if small keyboard is lower
            // padPosition += padHeight;
            padOption = padPosition + padHeight;
            //     0,  1,  2, 3 - Full size: 	 left, center wide, right, center narrow
            // or 10, 11, 12, 13 - Higher small: left, center wide, right, center narrow
            // or 20, 21, 22, 23 - Lower small:	 left, center wide, right, center narrow
            // or (3-line test) 30, 31, 32, 33	 left, center wide, right, center narrow
        }


        int ofsL = 0; // offset for left part of pad
        int ofsR = 0; // offset for right part of pad
        int ofsY = 0; // offset vertical
        int ofsDelta = 0; // to fix in certain places
        int gridDelta = 0;

        CMBOButton th = null;
        CMBOButton backspace = null;
        CMBOButton w = null;
        CMBOButton space = null;
        CMBOButton m = null;
        CMBOButton none = null;

        CMBOButton a = null, b = null, c = null, d = null, e = null,
                f = null, g = null, k = null, o = null, s = null;
        Rect old;


        // padPosition:			padHeight:
        //
        // Left 		 0		Higher smaller	10
        // Wide center 	 1		Tall default	 0
        // Right 		 2		Lower smaller	20
        // Narrow center 3		3-row special	30 (test for now) NO MORE USED, see padType below
        // ...
        //
        // padType (portrait):  landscapecolumns (landscape):
        // 5-row standard 0     5-row standard  3
        // 3-row special 10     3-row special   5
        //                      double          6

        int gridX = 5;
        int gridY = noTopBar ? 5 : 6;

        double scalePercentage;

        switch (orientation) {

            default:
            case Configuration.ORIENTATION_UNDEFINED:
            case Configuration.ORIENTATION_PORTRAIT:
            case Configuration.ORIENTATION_SQUARE:

                // ========================================== PORTRAIT ========================================

                land3Row = false; land5Row = false; //landDouble = false;
                //rows5 = port5Row; rows3 = port3Row; // for selecting key matrix below

                if (tablet) { // -------------------- TABLET PORTRAIT-------------------------

                    Log.d("-PADS", "(LayoutGenerator) Tablet, landscape, padPosition = " + padPosition);

                    // Tablet Portrait Single 5-row or 3-row pad
                    if (rows5) { // 5-row
                        gridY = noTopBar ? 5 : 6;
                    }
                    else { // 3-row
                        gridY = noTopBar ? 3 : 4;
                    }
                    ofsY = noTopBar ? 0 : 1;
                    gridX = rows5 ? 5 : 7; // 5-row/3-row

                    switch (padPosition) {
                        case 0: // left (Tablet Portrait Single 5-row or 3-row pad)
                            ofsY = noTopBar ? 0 : 1;
                            if (noPanel) {
                                gridX = rows5 ? 3 : 5; // 5-row/3-row
                                ofsL =  rows5 ? 0 : 0;
                                ofsR =  rows5 ? 0 : 0;
                            } else {
                                gridX = rows5 ? 5 : 7; // 5-row/3-row
                                ofsL =  rows5 ? 0 : 0;
                                ofsR =  rows5 ? 0 : 0;
                            }
                            break;
                        case 1: // center wide (Tablet Portrait Single 5-row or 3-row pad)
                            ofsY = noTopBar ? 0 : 1;
                            if (noPanel) {
                                gridX = rows5 ? 3 : 5;
                                ofsL =  rows5 ? 0 : 0;
                                ofsR =  rows5 ? 0 : 0;
                            } else {
                                gridX = rows5 ? 5 : 7;
                                ofsL =  rows5 ? 0 : 0;
                                ofsR =  rows5 ? 0 : 0;
                            }
                            break;
                        case 2: // right (Tablet Portrait Single 5-row or 3-row pad)
                            ofsY = noTopBar ? 0 : 1;
                            if (noPanel) {
                                gridX = rows5 ? 3 : 5;
                                ofsL =  rows5 ? 0 : 0;
                                ofsR =  rows5 ? 0 : 0;
                            } else {
                                gridX = rows5 ? 5 : 7;
                                ofsL =  rows5 ? 2 : 2;
                                ofsR =  rows5 ? 2 : 2;
                            }
                            break;
                        case 3: // center narrow (Tablet Portrait Single 5-row or 3-row pad)
                            ofsY = noTopBar ? 0 : 1;
                            if (noPanel) {
                                gridX = rows5 ? 3: 5;
                                ofsL = rows5 ? 0 : 0;
                                ofsR = rows5 ? 0 : 0;
                            } else {
                                gridX = rows5 ? 5: 7;
                                ofsL = rows5 ? 2 : 0;
                                ofsR = rows5 ? 2 : 0;
                            }
                            break;
                        case 4: // center wide (Tablet Portrait Single 5-row or 3-row pad)
                            ofsY = noTopBar ? 0 : 1;
                            if (noPanel) {
                                gridX = rows5 ? 3 : 5;
                                ofsL =  rows5 ? 0 : 0;
                                ofsR =  rows5 ? 0 : 0;
                            } else {
                                gridX = rows5 ? 5 : 7;
                                ofsL =  rows5 ? 0 : 0;
                                ofsR =  rows5 ? 0 : 0;
                            }
                            break;
                        default:
                            ofsY = noTopBar ? 0 : 1;
                            gridX = rows5 ? 5 : 7;
                            gridY = noTopBar ? 5 : 7;
                            ofsL =  rows5 ? 0 : 0;
                            ofsR =  rows5 ? 0 : 0;
                    }

                    // end of Portrait Tablet 5-row or 3-row

                } else if (phone) { // --------------------- PHONE PORTRAIT ------------------

                    Log.d("-PADS", "(LayoutGenerator) Phone, Portrait, padPosition = " + padPosition);

                    // Phone Portrait Single 5-row or 3-row pad
                    if (rows5) { // 5-row
                        gridY = noTopBar ? 5 : 6;
                    }
                    else { // 3-row
                        gridY = noTopBar ? 3 : 4;
                    }
                    switch (padPosition) {
                        case 0: // left (Phone Portrait Single 5-row or 3-row pad)
                            ofsY = noTopBar ? 0 : 1;
                            if (noPanel) {
                                gridX = rows5 ? 3 : 6; // 5-row/3-row
                            } else {
                                gridX = rows5 ? 5 : 6; // 5-row/3-row
                            }
                            ofsL =  rows5 ? 0 : 0;
                            ofsR =  rows5 ? 0 : 0;
                            break;
                        case 1: // center wide (Phone Portrait Single 5-row or 3-row pad)
                            ofsY = noTopBar ? 0 : 1;
                            if (noPanel) {
                                gridX = rows5 ? 3 : 5;
                            } else {
                                gridX = rows5 ? 3 : 5;
                            }
                            ofsL =  rows5 ? 0 : 0;
                            ofsR =  rows5 ? 0 : 0;
                            break;
                        case 2: // right (Phone Portrait Single 5-row or 3-row pad)
                            ofsY = noTopBar ? 0 : 1;
                            if (noPanel) {
                                gridX = rows5 ? 3 : 6;
                                ofsL =  rows5 ? 0 : 1;
                                ofsR =  rows5 ? 0 : 1;
                            } else {
                                gridX = rows5 ? 5 : 6;
                                ofsL =  rows5 ? 2 : 1;
                                ofsR =  rows5 ? 2 : 1;
                            }
                            break;
                        case 3: // center narrow (Phone Portrait Single 5-row or 3-row pad)
                            ofsY = noTopBar ? 0 : 1;
                            if (noPanel) {
                                gridX = rows5 ? 3 : 7;
                            } else {
                                gridX = rows5 ? 4 : 7;
                            }
                            ofsL = rows5 ? 0 : 1;
                            ofsR = rows5 ? 0 : 1;
                            break;
                        case 4: // split pad (Phone Portrait Single 5-row or 3-row pad)
                            ofsY = noTopBar ? 0 : 1;
                            if (noPanel) {
                                gridX = rows5 ? 3 : 5;
                            } else {
                                gridX = rows5 ? 3 : 5;
                            }
                            ofsL =  rows5 ? 0 : 0;
                            ofsR =  rows5 ? 0 : 0;
                            break;
                        default:
                            ofsY = noTopBar ? 0 : 1;
                            gridX = 6;
                            gridY = noTopBar ? 5 : 6;
                            ofsL =  rows5 ? 0 : 0;
                            ofsR =  rows5 ? 0 : 0;
                    }

                }
                // end of Portrait Phone 5-row or 3-row

                Log.d("-PADS", "*** Arranging keys... Finished Portrait phone settings.");

                // ---------------------------------------------- END OF PORTRAIT --------------

                break; // end of portrait

            case Configuration.ORIENTATION_LANDSCAPE:

                // ======================================== LANDSCAPE =====================================

                port3Row = false; port5Row = false;
                //rows5 = land5Row; rows3 = land3Row; // for selecting key matrix below


                if (tablet) {

                    Log.d("-PADS", "(LayoutGenerator) Tablet, landscape, padPosition = " + padPosition);

                    // Tablet Landscape Single 5-row or 3-row pad
                    if (rows5) { // 5-row
                        gridY = noTopBar ? 5 : 6;
                    }
                    else { // 3-row
                        gridY = noTopBar ? 3 : 4;
                    }

                    if(doublePad) { // Master setting for doublePad landscape

                        ofsY = noTopBar ? 0 : 1;
                        gridX = rows5 ? 16 : 14;
                        ofsL = rows5 ? 0 : 0;
                        ofsR = rows5 ? 13 : 9;

                    } else { // not doublePad

                        switch (padPosition) {
                            case 0: // left (Tablet Landscape Single 5-row or 3-row pad)
                                ofsY = noTopBar ? 0 : 1;
                                gridX = rows5 ? 18 : 18; // 5-row/3-row
                                ofsL = rows5 ? 0 : 0;
                                ofsR = rows5 ? 0 : 0;
                                break;
                            case 1: // center wide (Tablet Landscape Single 5-row or 3-row pad)
                                if (doublePad) {
                                    ofsY = noTopBar ? 0 : 1; // TODO: Finally, will be two complete pads
                                    gridX = rows5 ? 16 : 12;
                                    ofsL = rows5 ? 0 : 0;
                                    ofsR = rows5 ? 13 : 7;
                                } else {
                                    ofsY = noTopBar ? 0 : 1;
                                    gridX = rows5 ? 16 : 12;
                                    ofsL = rows5 ? 0 : 0;
                                    ofsR = rows5 ? 13 : 7;
                                }
                                break;
                            case 2: // right (Tablet Landscape Single 5-row or 3-row pad)
                                ofsY = noTopBar ? 0 : 1;
                                gridX = rows5 ? 18 : 18;
                                ofsL = rows5 ? 15 : 13;
                                ofsR = rows5 ? 15 : 13;
                                break;
                            case 3: // center narrow (Tablet Landscape Single 5-row or 3-row pad)
                                ofsY = noTopBar ? 0 : 1;
                                gridX = rows5 ? 16 : 15; // 16 : 15;
                                ofsL = rows5 ? 7 : 5; // 7 : 5;
                                ofsR = rows5 ? 7 : 5; //  7 : 5;
                                break;
                            case 4: // doublePad (Tablet Landscape Single 5-row or 3-row pad)
                                ofsY = noTopBar ? 0 : 1;
                                gridX = rows5 ? 16 : 11;
                                ofsL = rows5 ? 7 : 3;
                                ofsR = rows5 ? 7 : 3;
                                break;
                            default:
                                ofsY = noTopBar ? 0 : 1;
                                gridX = 16;
                                gridY = noTopBar ? 5 : 6;
                                ofsL = rows5 ? 9 : 9;
                                ofsR = rows5 ? 9 : 9;

                        }  // end of switch

                    } // end of else (no doublePad)


                    // end of Landscape Tablet 5-row or 3-row

                } else if (phone) {

                    Log.d("-PADS", "(LayoutGenerator) Phone, landscape, padPosition = " + padPosition);

                    // Phone Landscape Single 5-row or 3-row pad
                    if (rows5) { // 5-row
                        gridY = noTopBar ? 5 : 6;
                    }
                    else { // 3-row
                        gridY = noTopBar ? 3 : 4;
                    }

                    if(doublePad) { // Master setting

                        ofsY = noTopBar ? 0 : 1;
                        gridX = rows5 ? 10 : 10;
                        ofsL =  rows5 ? 0 : 0;
                        ofsR =  rows5 ? 7 : 5;

                    } else {

                        switch (padPosition) {
                            case 0: // left (Phone Landscape Single 5-row or 3-row pad)
                                ofsY = noTopBar ? 0 : 1;
                                gridX = rows5 ? 12 : 12; // 5-row/3-row
                                ofsL = rows5 ? 0 : 0;
                                ofsR = rows5 ? 0 : 0;
                                break;
                            case 1: // center wide (Phone Landscape Single 5-row or 3-row pad)
                                ofsY = noTopBar ? 0 : 1;
                                gridX = rows5 ? 10 : 10;
                                ofsL = rows5 ? 0 : 0;
                                ofsR = rows5 ? 7 : 5;
                                break;
                            case 2: // right (Phone Landscape Single 5-row or 3-row pad)
                                ofsY = noTopBar ? 0 : 1;
                                gridX = rows5 ? 12 : 12;
                                ofsL = rows5 ? 9 : 7;
                                ofsR = rows5 ? 9 : 7;
                                break;
                            case 3: // center narrow (Phone Landscape Single 5-row or 3-row pad)
                                ofsY = noTopBar ? 0 : 1;
                                gridX = rows5 ? 10 : 10;
                                ofsL = rows5 ? 3 : 2;
                                ofsR = rows5 ? 3 : 2;
                                break;
                            case 4: // split pad (Phone Landscape Single 5-row or 3-row pad)
                                ofsY = noTopBar ? 0 : 1;
                                gridX = rows5 ? 10 : 10; // 10 : 10
                                ofsL = rows5 ? 0 : 0;
                                ofsR = rows5 ? 7 : 5; // 5
                                break;
                            default:
                                ofsY = noTopBar ? 0 : 1;
                                gridX = 10;
                                gridY = noTopBar ? 5 : 6;
                                ofsL = rows5 ? 9 : 9;
                                ofsR = rows5 ? 9 : 9;
                        }
                    }
                }
                // end of Landscape Phone 5-row or 3-row


                break;
            // -------------------------------------------------------------------

        } // end of orientation switch



        // ============================== Arrange keys ===========================


        Log.d("-PADS", "*** (GEN) Phone, Landscape, padPosition = " + padPosition
                + ", padType = " + padType + ", doublePad = " + doublePad + ", rows3 = " + rows3);

        if (rows5) {  // ------------- All 5-row keypads ----------------------

            buttonLayout = new CMBOKeyboardButtonLayout(width, height,
                    gridX, gridY);

            scalePercentage = 0; // was 0.2

            a = buttonLayout.addButton(ofsL, ofsY, 1, 1);
            old = a.getVisibleRect();

            a.modifyVisibleSize(0, scalePercentage).setId(CMBOKey.A);

            scaleAmount = a.getVisibleRect().bottom - old.bottom;

            o = buttonLayout.addButton(ofsL, 1 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.O);
            b = buttonLayout.addButton(ofsL, 2 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, scaleAmount)
                    .setId(CMBOKey.B);
            s = buttonLayout.addButton(ofsL, 3 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.S);
            c = buttonLayout.addButton(ofsL, 4 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, 0)
                    .extendHitBoxBottom(12) // in some devices (Nokia 6) required
                    .setId(CMBOKey.C);

            th = buttonLayout
                    .addButton(1 + ofsR, ofsY, 1, 1)
                    .modifyVisibleSize(0, scalePercentage)
                    .setId(CMBOKey.TH);
            th = buttonLayout
                    .addButton(1 + ofsL, ofsY, 1, 1)
                    .modifyVisibleSize(0, scalePercentage)
                    .setId(CMBOKey.TH);
            // Was:
            //.modifyVisibleSize(0, 0, -scalePercentage,
            //		-scalePercentage).setId(CMBOKey.TH);

            backspace = buttonLayout.addButton(1 + ofsR, 1 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.BACKSPACE); // was:
            backspace = buttonLayout.addButton(1 + ofsL, 1 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.BACKSPACE); // was:
            //.modifyVisibleSize(-scalePercentage/2, 0, -scalePercentage,
            //		-scalePercentage / 2).setId(CMBOKey.BACKSPACE);

            w = buttonLayout
                    .addButton(1 + ofsR, 2 + ofsY, 1, 1)
                    .modifyVisibleSize(scalePercentage, scalePercentage)
                    .setId(CMBOKey.W); // Was:
            w = buttonLayout
                    .addButton(1 + ofsL, 2 + ofsY, 1, 1)
                    .modifyVisibleSize(scalePercentage, scalePercentage)
                    .setId(CMBOKey.W); // Was:
            //.modifyVisibleSize(0, 0, -scalePercentage,
            //		-scalePercentage).setId(CMBOKey.W);

            space = buttonLayout.addButton(1 + ofsR, 3 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.SPACE);
            space = buttonLayout.addButton(1 + ofsL, 3 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.SPACE);
            //.modifyVisibleSize(0, 0, -scalePercentage / 2,
            //		-scalePercentage).setId(CMBOKey.SPACE);

            m = buttonLayout.addButton(1 + ofsR, 4 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, 0)
                    //.modifyVisibleSize(scalePercentage, scalePercentage)
                    .modifyVisibleSize(scalePercentage, 0)
                    .extendHitBoxBottom(12) // in some devices (Nokia 6) required
                    .setId(CMBOKey.M);
            m = buttonLayout.addButton(1 + ofsL, 4 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, 0)
                    //.modifyVisibleSize(scalePercentage, scalePercentage)
                    .modifyVisibleSize(scalePercentage, 0)
                    .extendHitBoxBottom(12) // in some devices (Nokia 6) required
                    .setId(CMBOKey.M);


            d = buttonLayout.addButton(2 + ofsR, ofsY, 1, 1)
                    .modifyVisibleSizePixels(0, scaleAmount)
                    .setId(CMBOKey.D);
            g = buttonLayout.addButton(2 + ofsR, 1 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.G);
            e = buttonLayout.addButton(2 + ofsR, 2 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, scaleAmount)
                    .setId(CMBOKey.E);
            k = buttonLayout.addButton(2 + ofsR, 3 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.K);
            f = buttonLayout.addButton(2 + ofsR, 4 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, 0)
                    .extendHitBoxBottom(12) // in some devices (Nokia 6) required
                    .setId(CMBOKey.F);

            o.addAdjacent(a, b);
            s.addAdjacent(b, c);
            g.addAdjacent(d, e);
            k.addAdjacent(e, f);


        } else if (rows3) {  // ------------ All 3-row keypads ---------------------
            //} else {

            buttonLayout = new CMBOKeyboardButtonLayout(width, height,
                    gridX, gridY);

            scalePercentage = 0; // was 0.2

            a = buttonLayout.addButton(ofsL, ofsY, 1, 1);
            old = a.getVisibleRect();

            a.modifyVisibleSize(0, scalePercentage).setId(CMBOKey.A);
            scaleAmount = a.getVisibleRect().bottom - old.bottom;

            o = buttonLayout.addButton(1 + ofsL, ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.O);
            b = buttonLayout.addButton(ofsL, 1 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, scaleAmount)
                    .setId(CMBOKey.B);
            s = buttonLayout.addButton(1 + ofsL, 2 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.S);
            c = buttonLayout.addButton(ofsL, 2 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, 0)
                    .extendHitBoxBottom(12)
                    .setId(CMBOKey.C);
            backspace = buttonLayout
                    .addButton(1 + ofsL, 1 + ofsY, 1, 1)
                    .modifyVisibleSize(0, 0, -scalePercentage,
                            -scalePercentage / 2).setId(CMBOKey.BACKSPACE);

            th = buttonLayout
                    .addButton(2 + ofsL, ofsY, 1, 1)
                    .modifyVisibleSize(0, 0, -scalePercentage,
                            -scalePercentage).setId(CMBOKey.TH);
            th = buttonLayout
                    .addButton(2 + ofsR, ofsY, 1, 1)
                    .modifyVisibleSize(0, 0, -scalePercentage,
                            -scalePercentage).setId(CMBOKey.TH);
            w = buttonLayout
                    .addButton(2 + ofsL, 1 + ofsY, 1, 1)
                    .modifyVisibleSize(0, 0, -scalePercentage,
                            -scalePercentage).setId(CMBOKey.W);
            w = buttonLayout
                    .addButton(2 + ofsR, 1 + ofsY, 1, 1)
                    .modifyVisibleSize(0, 0, -scalePercentage,
                            -scalePercentage).setId(CMBOKey.W);
            m = buttonLayout
                    .addButton(2 + ofsL, 2 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, 0)
                    .setId(CMBOKey.M);
            m = buttonLayout
                    .addButton(2 + ofsR, 2 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, 0)
                    .setId(CMBOKey.M);

            space = buttonLayout
                    .addButton(3 + ofsR, 1 + ofsY, 1, 1)
                    .modifyVisibleSize(0, 0, -scalePercentage / 2,
                            -scalePercentage).setId(CMBOKey.SPACE);
            d = buttonLayout.addButton(4 + ofsR, ofsY, 1, 1)
                    .modifyVisibleSizePixels(0, scaleAmount)
                    .setId(CMBOKey.D);
            g = buttonLayout.addButton(3 + ofsR, ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.G);
            e = buttonLayout.addButton(4 + ofsR, 1 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, scaleAmount)
                    .setId(CMBOKey.E);
            k = buttonLayout.addButton(3 + ofsR, 2 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(-scaleAmount, -scaleAmount)
                    .setId(CMBOKey.K);
            f = buttonLayout.addButton(4 + ofsR, 2 + ofsY, 1, 1)
                    .modifyVisibleSizePixels(scaleAmount, 0)
                    .setId(CMBOKey.F);
            o.addAdjacent(a, b);
            s.addAdjacent(b, c);
            g.addAdjacent(d, e);
            k.addAdjacent(e, f);

        }

        // -------------------------------------------------------------

        return buttonLayout;
    }



}