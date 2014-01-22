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
public class DssatTFileTest {

    DssatTFileOutput obDssatTFileOutput;
    DssatTFileInput obDssatTFileInput;
    URL resource;

    @Before
    public void setUp() throws Exception {
        obDssatTFileOutput = new DssatTFileOutput();
        obDssatTFileInput = new DssatTFileInput();
        resource = this.getClass().getResource("/UFGA8201_MZX.zip");
    }

    @Test
    public void test() throws IOException, Exception {
        HashMap result;

        result = obDssatTFileInput.readFile(resource.getPath());
//        System.out.println(JSONAdapter.toJSON(result));
//        File f = new File("outputT.txt");
//        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
//        bo.write(JSONAdapter.toJSON(result).getBytes());
//        bo.close();
//        f.delete();

        ArrayList<HashMap> expArr = getObjectOr(result, "experiments", new ArrayList());
        obDssatTFileOutput.writeFile("output", expArr.get(0));
        File file = obDssatTFileOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertEquals("UFGA8201.MZT", file.getName());
            assertTrue(file.delete());
        } else {
            assertTrue(file != null);
        }
    }
}
