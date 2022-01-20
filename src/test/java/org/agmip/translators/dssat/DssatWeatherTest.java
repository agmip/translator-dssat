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

public class DssatWeatherTest {

    DssatWeatherOutput obDssatWeatherOutput;
    DssatWeatherInput obDssatWeatherInput;
    URL resource;
    URL resource4Y;

    @Before
    public void setUp() throws Exception {
        obDssatWeatherOutput = new DssatWeatherOutput();
        obDssatWeatherInput = new DssatWeatherInput();
        resource = this.getClass().getResource("/UFGA8202_MZX.ZIP");
        resource4Y = this.getClass().getResource("/UHIH1201.WTH");
    }

//    @Test
    public void test() throws IOException, Exception {
        HashMap result;

        result = obDssatWeatherInput.readFile(resource.getPath());
//        System.out.println(JSONAdapter.toJSON(result));
//        File f = new File("outputW.txt");
//        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
//        bo.write(JSONAdapter.toJSON(result).getBytes());
//        bo.close();
//        f.delete();

        ArrayList<HashMap> wthArr = getObjectOr(result, "weathers", new ArrayList());
        HashMap expData = new HashMap();
        expData.put("weather", wthArr.get(0));
        obDssatWeatherOutput.writeFile("output", expData);
        File file = obDssatWeatherOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertEquals("UFGA8201.WTH", file.getName());
            assertTrue(file.delete());
        } else {
            assertTrue(file != null);
        }
        
        expData = new HashMap();
        expData.put("weather", wthArr.get(0));
        wthArr.get(0).put("clim_id", "5FPA");
        obDssatWeatherOutput.writeFile("output", expData);
        file = obDssatWeatherOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertEquals("UFGA5FPA.WTH", file.getName());
            assertTrue(file.delete());
        } else {
            assertTrue(file != null);
        }
    }
    
    @Test
    public void test4Y() throws IOException, Exception {
        HashMap result;

        result = obDssatWeatherInput.readFile(resource4Y.getPath());

        ArrayList<HashMap> wthArr = getObjectOr(result, "weathers", new ArrayList());
        HashMap expData = new HashMap();
        expData.put("weather", wthArr.get(0));
        obDssatWeatherOutput.writeFile("output", expData);
        File file = obDssatWeatherOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertEquals("UHIH1201.WTH", file.getName());
//            assertTrue(file.delete());
        } else {
            assertTrue(file != null);
        }
        
        ArrayList<HashMap> wthRecords = (ArrayList<HashMap>) getObjectOr(wthArr.get(0), DssatWeatherInput.dailyKey, new ArrayList());
        assertTrue("20120101".equals(wthRecords.get(0).get("w_date")));
        
    }
}
