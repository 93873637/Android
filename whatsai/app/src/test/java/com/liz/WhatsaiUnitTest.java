package com.liz;

import com.liz.whatsai.logic.Task;
import com.liz.whatsai.logic.TaskGroup;
import com.liz.whatsai.logic.WhatsaiDir;
import com.liz.whatsai.logic.WhatsaiFile;
import com.liz.whatsai.logic.WhatsaiText;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class WhatsaiUnitTest {
    @Test
    public void test_isDir() {
        assertFalse(new WhatsaiFile().isDir());
        assertFalse(new Task().isDir());
        assertFalse(new WhatsaiText().isDir());
        assertTrue(new WhatsaiDir().isDir());
        assertTrue(new TaskGroup().isDir());
    }
}
