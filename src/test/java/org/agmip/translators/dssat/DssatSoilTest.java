package org.agmip.translators.dssat;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import static org.agmip.util.MapUtil.getObjectOr;
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
public class DssatSoilTest {

    DssatSoilOutput obDssatSoilOutput;
    DssatSoilInput obDssatSoilInput;
    URL resource;
    
    @Before
    public void setUp() throws Exception {
        obDssatSoilOutput = new DssatSoilOutput();
        obDssatSoilInput = new DssatSoilInput();
        resource = this.getClass().getResource("/UFGA8202_MZX.zip");
    }
    

    @Test
    public void test() throws IOException, Exception {
        HashMap result;

        result = obDssatSoilInput.readFile(resource.getPath());
//        System.out.println(JSONAdapter.toJSON(result));
//        File f = new File("outputS.txt");
//        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
//        bo.write(JSONAdapter.toJSON(result).getBytes());
//        bo.close();
//        f.delete();

        ArrayList<HashMap> soilArr = getObjectOr(result, "soils", new ArrayList());
        HashMap expData = new HashMap();
        expData.put("soil", soilArr.get(0));
//        soilArr.get(0).put("exname", "UFGA8201MZ");
        obDssatSoilOutput.writeFile("output", expData);
        File file = obDssatSoilOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertEquals("IB.SOL", file.getName());
            assertTrue(file.delete());
        } else {
            assertTrue(file != null);
        }
    }
}
