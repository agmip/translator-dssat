package org.agmip.translators.dssat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
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
public class DssatControllerTest {

    DssatControllerOutput obDssatControllerOutput;
    DssatControllerInput obDssatControllerInput;
    URL resource;
    String fileName =
            "UFGA8401_CPX.ZIP";
//            "UFGA8201_MZX.ZIP";
//            "UFGA8202_MZX.ZIP";
//            "UFGA8201_MZX_dummy.ZIP";

    @Before
    public void setUp() throws Exception {
        obDssatControllerOutput = new DssatControllerOutput();
        obDssatControllerInput = new DssatControllerInput();
        resource = this.getClass().getResource("/" + fileName);
    }

    @Test
    public void test() throws IOException, Exception {
        HashMap result;
        File f;
        BufferedOutputStream bo;
        result = obDssatControllerInput.readFile(resource.getPath());
        f = new File(fileName.replaceAll("[Xx]*\\.\\w+$", ".json"));
        bo = new BufferedOutputStream(new FileOutputStream(f));

        // Output json for reading
        bo.write(JSONAdapter.toJSON(result).getBytes());
        bo.close();
        f.delete();

        obDssatControllerOutput = new DssatControllerOutput();
        obDssatControllerOutput.writeFile("", result);
        File file = obDssatControllerOutput.getOutputZipFile();
        if (file != null) {
            assertTrue(file.exists());
            assertTrue(file.getName().toUpperCase().matches("^AGMIP_DSSAT_\\d+\\.ZIP$"));
            assertTrue(file.delete());
        }

        String jsonStr;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("Machakos_1Exp-1Yr.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        jsonStr = br.readLine();
        obDssatControllerOutput = new DssatControllerOutput();
        obDssatControllerOutput.writeFile("", JSONAdapter.fromJSON(jsonStr));
        file = obDssatControllerOutput.getOutputZipFile();
        if (file != null) {
            assertTrue(file.exists());
            assertTrue(file.getName().toUpperCase().matches("^AGMIP_DSSAT_\\d+\\.ZIP$"));
            assertTrue(file.delete());
        }
    }
}
