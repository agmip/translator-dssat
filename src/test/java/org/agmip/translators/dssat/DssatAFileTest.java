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
public class DssatAFileTest {

    DssatAFileOutput obDssatAFileOutput;
    DssatAFileInput obDssatAFileInput;
    URL resource;

    @Before
    public void setUp() throws Exception {
        obDssatAFileOutput = new DssatAFileOutput();
        obDssatAFileInput = new DssatAFileInput();
        resource = this.getClass().getResource("/UFGA8202_MZX.ZIP");
    }

    @Test
    public void test() throws IOException, Exception {
        HashMap result;

        result = obDssatAFileInput.readFile(resource.getPath());
//        System.out.println(JSONAdapter.toJSON(result));
//        File f = new File("outputA.txt");
//        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
//        bo.write(JSONAdapter.toJSON(result).getBytes());
//        bo.close();
//        f.delete();

        ArrayList<HashMap> expArr = getObjectOr(result, "experiments", new ArrayList());
        obDssatAFileOutput.writeFile("output", expArr.get(0));
        File file = obDssatAFileOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertEquals("UFGA8202.MZA", file.getName());
            assertTrue(file.delete());
        }
    }
}
