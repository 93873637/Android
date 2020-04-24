/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:
 * @author: fxw@rock-chips.com
 * Create at:   2014年5月12日 下午6:17:42
 *
 * Modification History:
 * Date         Author      Version     Description
 * ------------------------------------------------------------------
 * 2014年5月12日      fxw         1.0         create
 *******************************************************************/

package com.liz.androidutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class DiskTestUtils {

    private static final String DISK_TEST_FILENAME = "disktest.txt";
    private static final byte[] DISK_TEST_DATA = new byte[1024*1024];

    public static boolean testReadAndWrite(String testDir) {
        // check test dir
        boolean testDirCreated = false;
        File dir = new File(testDir);
        if (!dir.exists()) {
            JLog.ti("test dir \"" + testDir + "\" not exists, create...");
            if (!dir.mkdirs()) {
                JLog.te("create test dir \"" + testDir + "\" failed.");
                return false;
            }
            else {
                testDirCreated = true;
            }
        }
        else {
            // already exist, check if directory
            if (!dir.isDirectory()) {
                JLog.ti("test dir \"" + testDir + "\" exists but not direct, delete it first...");
                if (!dir.delete()) {
                    JLog.te("delete old test dir \"" + testDir + "\" failed");
                    return false;
                }
                JLog.ti("create new test dir \"" + testDir + "\"...");
                if (!dir.mkdirs()) {
                    JLog.te("create new test dir \"" + testDir + "\" failed.");
                    return false;
                }
                else {
                    testDirCreated = true;
                }
            }
        }

        File f = new File(testDir, DISK_TEST_FILENAME);
        try {
            // prepare test file
            if (f.exists()) {
                JLog.ti("test file \"" + f.getAbsolutePath() + "\" already exists, delete it first...");
                if (f.delete()) {
                    JLog.te("delete old test file \"" + f.getAbsolutePath() + "\" failed.");
                    return false;
                }
            }
            if (!f.createNewFile()) {
                JLog.te("create test file \"" + f.getAbsolutePath() + "\" failed.");
                return false;
            }

            // test write data to file
            testWriteFile(f.getAbsolutePath());
            //###@:testReadFile(f.getAbsolutePath());
            return true;
        } catch (Exception ex) {
            JLog.te("test failed, ex = " + ex.toString());
            ex.printStackTrace();
            return false;
        } finally {
            if (f.exists()) {
                if (f.delete()) {
                    JLog.te("delete test file \"" + f.getAbsolutePath() + "\" failed.");
                }
            }
            if (testDirCreated && dir.exists()) {
                if (!dir.delete()) {
                    JLog.te("delete test dir \"" + testDir + "\" failed");
                }
            }
        }
    }

   public static void testWriteFile(String filename) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename));
            //####@: osw.write(DISK_TEST_DATA, 0, DISK_TEST_DATA.length);
            osw.flush();
            osw.close();
        } catch (Exception ex) {
            JLog.te("test failed, ex = " + ex.toString());
            ex.printStackTrace();
        }
    }

    public String doReadFile(String filename) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filename)));
            String data = null;
            StringBuilder temp = new StringBuilder();
            while ((data = br.readLine()) != null) {
                temp.append(data);
            }
            br.close();
            return temp.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
