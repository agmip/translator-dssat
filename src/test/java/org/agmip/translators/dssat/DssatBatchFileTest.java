package org.agmip.translators.dssat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import static org.agmip.util.MapUtil.*;
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
    URL resource;

    @Before
    public void setUp() throws Exception {
        obDssatControllerInput = new DssatControllerInput();
        obDssatBatchFileOutput = new DssatBatchFileOutput();
        resource = this.getClass().getResource("/UFGA8201_MZX.zip");
    }

    @Test
    public void test() throws IOException, Exception {
        HashMap result;

        result = obDssatControllerInput.readFile(resource.getPath());
        ArrayList<HashMap> expArr = getObjectOr(result, "experiments", new ArrayList());

        obDssatBatchFileOutput.writeFile("", expArr , new HashMap());
        File file = obDssatBatchFileOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertTrue(file.getName().equals("DSSBatch.v45"));
            assertTrue(file.delete());
        }
    }
}
