package org.agmip.translators.dssat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import static org.agmip.translators.dssat.DssatCommonInput.getSectionData;
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

    @Before
    public void setUp() throws Exception {
        obDssatControllerInput = new DssatControllerInput();
        obDssatBatchFileOutput = new DssatBatchFileOutput();
    }

    @Test
    public void test() throws IOException, Exception {
        ArrayList<LinkedHashMap> result;

        String filePath = "src\\test\\java\\org\\agmip\\translators\\dssat\\UFGA8201_MZX_2.ZIP";
        LinkedHashMap tmp = obDssatControllerInput.readFiles(filePath);
        result = new ArrayList();
        ArrayList<LinkedHashMap> expArr = getObjectOr(tmp, "experiment", new ArrayList());
        LinkedHashMap expData;
        for (int j = 0; j < expArr.size(); j++) {
            expData = expArr.get(j);
            expData.put("soil", getSectionData(getObjectOr(tmp, "soil", new ArrayList()), "soil_id", getObjectOr(expData, "soil_id", "")));
            expData.put("weather", getSectionData(getObjectOr(tmp, "weather", new ArrayList()), "wst_id", getObjectOr(expData, "wst_id", "")));
            result.add(expData);
        }

        obDssatBatchFileOutput.writeFile("", result);
        File file = obDssatBatchFileOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertTrue(file.getName().equals("DSSBatch.v45"));
            assertTrue(file.delete());
        }
    }
}
