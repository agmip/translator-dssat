package org.agmip.translators.dssat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
public class DssatBatchFileTest {

    DssatControllerInput obDssatControllerInput;
    DssatBatchFileOutput obDssatBatchFileOutput;

    @Before
    public void setUp() throws Exception {
        obDssatControllerInput = new DssatControllerInput();
        obDssatBatchFileOutput = new DssatBatchFileOutput();
    }

    @Test
    public void test() throws IOException, Exception {
        ArrayList<LinkedHashMap> result;

        String filePath = "src\\test\\java\\org\\agmip\\translators\\dssat\\UFGA8201_MZX_2.ZIP";
        result = obDssatControllerInput.readFiles(filePath);

        obDssatBatchFileOutput.writeFile("", result);
        File file = obDssatBatchFileOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertTrue(file.getName().equals("DSSBatch.v45"));
//            assertTrue(file.delete());
        }
    }
}
