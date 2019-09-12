package com.liz.androidutilstest;

import com.liz.androidutils.LJson;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test_LJson_formatJson() {
        String jstr = "{\"TYPE\": 1, \"N[AM]E\": \"wh[atsa]i\", \"LIST\": [{\"T{YP}E\": 0, \"NAME\": \"Task NAME0\"},{\"TYPE\": 0, \"NAME\": \"Task NAME1\"}]};";
        String formatStr = LJson.formatJson(jstr);
        //System.out.println(formatStr);
        String expectStr = "{\n" +
                "  \"TYPE\":1,\n" +
                "  \"N[AM]E\":\"wh[atsa]i\",\n" +
                "  \"LIST\":[\n" +
                "    {\n" +
                "      \"T{YP}E\":0,\n" +
                "      \"NAME\":\"Task NAME0\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"TYPE\":0,\n" +
                "      \"NAME\":\"Task NAME1\"\n" +
                "    }\n" +
                "  ]\n" +
                "};";
        assertEquals(formatStr, expectStr);
    }
}
