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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
/**
 *
 * @author Meng Zhang
 */
public class DssatXFileTest {

    private static final Logger log = LoggerFactory.getLogger(DssatXFileTest.class);
    DssatXFileOutput obDssatXFileOutput;
    DssatXFileInput obDssatXFileInput;
    URL resource;

    @Before
    public void setUp() throws Exception {
        obDssatXFileOutput = new DssatXFileOutput();
        obDssatXFileInput = new DssatXFileInput();
        resource = this.getClass().getResource("/UFGA8201_MZX.zip");
    }

    @Test
    public void test() throws IOException, Exception {
        HashMap result;

        log.debug(resource.toString());

        result = obDssatXFileInput.readFile(resource.getPath());

        // Output Json text for debugging
//        File f = new File("outputX.txt");
//        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
//        bo.write(JSONAdapter.toJSON(result).getBytes());
//        bo.close();
//        f.delete();

        ArrayList<HashMap> expArr = getObjectOr(result, "experiments", new ArrayList());
        obDssatXFileOutput.writeFile("output", expArr.get(0));
        File file = obDssatXFileOutput.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertEquals("UFGA8201.MZX", file.getName());
            assertTrue(file.delete());
        } else {
            assertTrue(file != null);
        }
    }
}
