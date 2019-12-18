package com.liz.androidutils;

@SuppressWarnings("unused, WeakerAccess")
public class StringBufferQueue {

    private final static int DEFAULT_MAX_COUNT = 10;  // at least one
    private final static int INVALID_INDEX = -1;
    private final static int DEFAULT_MAX_ITEM_LEN = 512;  // longer then trimmed string
    private final static String TRIMMED_STR = "...";

    private String[] mArray;
    private int mMaxCount;
    private int mEndPos;
    private boolean mQueueFull;
    private int mMaxItemLen;

    public StringBufferQueue() {
        init(DEFAULT_MAX_COUNT, DEFAULT_MAX_ITEM_LEN);
    }

    public StringBufferQueue(int maxCount) {
        init(maxCount, DEFAULT_MAX_ITEM_LEN);
    }

    public StringBufferQueue(int maxCount, int maxItemLen) {
        init(maxCount, maxItemLen);
    }

    private void init(int maxCount, int maxItemLen) {
        mMaxCount = maxCount;
        mEndPos = INVALID_INDEX;
        mQueueFull = false;
        mArray = new String[mMaxCount];
        mMaxItemLen = maxItemLen;
    }

    public boolean isEmpty() {
        return mEndPos == INVALID_INDEX;
    }

    public boolean isFull() {
        return mQueueFull;
    }

    public void append(String item) {
        if (item == null) {
            return;
        }

        //trim item if exceed max length
        if (item.length() > mMaxItemLen) {
            item = item.substring(0, mMaxItemLen - TRIMMED_STR.length()) + TRIMMED_STR;
        }

        if (isEmpty()) {
            mEndPos = 0;
            mArray[mEndPos] = item;
        }
        else {
            mEndPos ++;
            if (mEndPos >= mMaxCount - 1) {
                mQueueFull = true;
                mEndPos %= mMaxCount;
            }
            mArray[mEndPos] = item;
        }
    }

    public String getBuffer() {
        StringBuilder sb = new StringBuilder();
        if (!mQueueFull) {
            for (int i = 0; i<= mEndPos; i++) {
                String itemString = mArray[i];
                if (i != mEndPos)
                    itemString += "\n";
                sb.append(itemString);
            }
        }
        else {
            for (int i=0; i<mMaxCount; i++) {
                int index = (mEndPos + i + 1) % mMaxCount;
                String itemString = mArray[index];
                if (i != mMaxCount - 1)
                    itemString += "\n";
                sb.append(itemString);
            }
        }
        return sb.toString();
    }

    public String debugInfo() {
        return "mEndPos = " + mEndPos + ", IsFull=" + isFull();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test Functions

    public static void main(String[] args) {
        {
            StringBufferQueue sbq = new StringBufferQueue();
            System.out.println("----------------------------------------");
            System.out.println(sbq.debugInfo());
            System.out.println(sbq.getBuffer());
            System.out.println("----------------------------------------");
        }
        {
            StringBufferQueue sbq = new StringBufferQueue();
            for (int i = 0; i < 3; i++)
                sbq.append("aaa" + i);
            System.out.println("----------------------------------------");
            System.out.println(sbq.debugInfo());
            System.out.println(sbq.getBuffer());
            System.out.println("----------------------------------------");
        }
        {
            StringBufferQueue sbq = new StringBufferQueue();
            for (int i = 0; i < 10; i++)
                sbq.append("aaa" + i);
            System.out.println("----------------------------------------");
            System.out.println(sbq.debugInfo());
            System.out.println(sbq.getBuffer());
            System.out.println("----------------------------------------");
        }
        {
            StringBufferQueue sbq = new StringBufferQueue();
            for (int i = 0; i < 11; i++)
                sbq.append("aaa" + i);
            System.out.println("----------------------------------------");
            System.out.println(sbq.debugInfo());
            System.out.println(sbq.getBuffer());
            System.out.println("----------------------------------------");
        }
        {
            StringBufferQueue sbq = new StringBufferQueue(10, 10);
            for (int i = 0; i < 11; i++)
                sbq.append("aaa" + i + "bbbbbbbbb");
            System.out.println("----------------------------------------");
            System.out.println(sbq.debugInfo());
            System.out.println(sbq.getBuffer());
            System.out.println("----------------------------------------");
        }
        {
            StringBufferQueue sbq = new StringBufferQueue();
            for (int i = 0; i < 100000000; i++)
                sbq.append("num #" + i);
            System.out.println("----------------------------------------");
            System.out.println(sbq.debugInfo());
            System.out.println(sbq.getBuffer());
            System.out.println("----------------------------------------");
        }
    }
}
