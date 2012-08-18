package org.agmip.translators.dssat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.agmip.util.JSONAdapter;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
/**
 *
 * @author Meng Zhang
 */
public class DssatCulFileTest {

    DssatCulFileOutput obOutput;
    DssatCulFileInput obInput;

    @Before
    public void setUp() throws Exception {
        obOutput = new DssatCulFileOutput();
        obInput = new DssatCulFileInput();
    }

    @Test
    public void test() throws IOException, Exception {

        ArrayList<LinkedHashMap> result;

        String filePath = "src\\test\\java\\org\\agmip\\translators\\dssat\\APAN9304_PNX.zip";
        result = obInput.readFileAll(filePath);
//        System.out.println(JSONAdapter.toJSON(result));
        File f = new File("outputS.txt");
        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
        bo.write(JSONAdapter.toJSON(result).getBytes());
        bo.close();
        f.delete();

        result.get(0).put("exname", "APAN9304PN");
        obOutput.writeFile("", result.get(0));
        File file = obOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertTrue(file.getName().equals("APAN9304_PNX.CUL"));
            assertTrue(file.delete());
        }
    }
}
