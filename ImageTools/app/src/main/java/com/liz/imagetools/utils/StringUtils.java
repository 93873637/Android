package com.liz.imagetools.utils;

import android.util.Size;

public class StringUtils {

    /**
     * parse width and height from string, such as:
     * out_1920x1440.nv21 -> size=1920x1440
     * @param strToParse
     */
    public static Size parseSizeFromString(String strToParse) {
        int xCharPos = strToParse.lastIndexOf('x');
        if (xCharPos < 0) {
            LogUtils.e("ERROR: No 'x' found in str: " + strToParse);
            return null;
        }

        int rightDotCharPos = strToParse.indexOf('.', xCharPos);
        if (rightDotCharPos < 0) {
            LogUtils.e( "ERROR: No '.' found after 'x' in str: " + strToParse);
            return null;
        }

        int rightCharPos = rightDotCharPos;
        int rightUnderLineCharPos = strToParse.indexOf('_', xCharPos);
        if (rightUnderLineCharPos != -1) {
            LogUtils.d("get rightUnderLineCharPos: " + rightUnderLineCharPos);
            rightCharPos = Math.min(rightDotCharPos, rightUnderLineCharPos);
        }

        if (rightCharPos <= 0) {
            LogUtils.e("ERROR: invalid rightCharPos: " + rightCharPos + " in str: " + strToParse);
            return null;
        }

        String heightString = strToParse.substring(xCharPos + 1, rightCharPos);
        int height = Integer.parseInt(heightString);
        if (height <= 0) {
            LogUtils.e("ERROR: invalid height " + height + " in str: " + strToParse);
            return null;
        }

        int leftCharPos = strToParse.lastIndexOf('_', xCharPos);
        if (leftCharPos <= 0) {
            LogUtils.e("ERROR: No '_' before 'x' found in str: " + strToParse);
            return null;
        }
        String widthString = strToParse.substring(leftCharPos + 1, xCharPos);
        int width = Integer.parseInt(widthString);
        if (width <= 0) {
            LogUtils.e("ERROR: invalid height " + width + " in str: " + strToParse);
            return null;
        }

        Size size = new Size(width, height);
        LogUtils.d("parseSizeFromString: str=" + strToParse + ", size=" + size.getWidth() + "x" + size.getHeight());
        return size;
    }
}
