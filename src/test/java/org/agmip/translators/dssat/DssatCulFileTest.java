package org.agmip.translators.dssat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import org.agmip.util.JSONAdapter;
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
public class DssatCulFileTest {

    DssatCulFileOutput obOutput;
    DssatCulFileInput obInput;
    URL resource;

    @Before
    public void setUp() throws Exception {
        obOutput = new DssatCulFileOutput();
        obInput = new DssatCulFileInput();
        resource = this.getClass().getResource("/APAN9304_PNX.zip");
        
    }

    @Test
    public void test() throws IOException, Exception {

        HashMap result;

        result = obInput.readFile(resource.getPath());
//        System.out.println(JSONAdapter.toJSON(result));
//        File f = new File("outputCul.txt");
//        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
//        bo.write(JSONAdapter.toJSON(result).getBytes());
//        bo.close();
//        f.delete();

        ArrayList<HashMap> expArr = getObjectOr(result, "experiments", new ArrayList());
        expArr.get(0).put("exname", "APAN9304PN");
        obOutput.writeFile("", expArr.get(0));
        File file = obOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertEquals("APAN9304_PNX.CUL", file.getName());
            assertTrue(file.delete());
        }
    }
}
