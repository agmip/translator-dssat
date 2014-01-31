package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import org.agmip.util.JSONAdapter;
import org.junit.Before;
import org.junit.Ignore;
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
    URL resource2;
    String fileName =
//                        "UFGA8401_CPX.ZIP";
                        "GHWA0401_MZX.ZIP";
//                        "UFGA8201_MZX.ZIP";
//                        "UFGA8202_MZX.ZIP";
//                        "UFGA8201_MZX_dummy.ZIP";
//                        "SWData.zip";
//                        "AGMIP_DSSAT_1359154224136.zip";
//                        "HSC_wth_bak.zip";

    @Before
    public void setUp() throws Exception {
        obDssatControllerOutput = new DssatControllerOutput();
        obDssatControllerInput = new DssatControllerInput();
        resource = this.getClass().getResource("/" + fileName);
        resource2 = this.getClass().getResource("/CRAFT/Sample_Run");
    }

    @Test
    @Ignore
    public void test() throws IOException, Exception {
        HashMap result;
        Calendar cal;
        String outPath;
        File outDir;
        File[] files;
        result = obDssatControllerInput.readFile(resource.getPath());
//        BufferedOutputStream bo;
//        File f = new File("output\\" + fileName.replaceAll("[Xx]*\\.\\w+$", ".json"));
//        bo = new BufferedOutputStream(new FileOutputStream(f));
//
//        // Output json for reading
//        bo.write(JSONAdapter.toJSON(result).getBytes());
//        bo.close();
//        f.delete();

        cal = Calendar.getInstance();
        outPath = "output\\AGMIP_DSSAT_" + cal.getTimeInMillis();
        obDssatControllerOutput = new DssatControllerOutput();
        obDssatControllerOutput.writeFile(outPath, result);
        outDir = new File(outPath);
        files = outDir.listFiles();
        for (File file : files) {
            System.out.println("Generated: " + file.getName());
            file.delete();
        }
        outDir.delete();

        String jsonStr;
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("Machakos_1Exp-1Yr.json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        jsonStr = br.readLine();
        cal = Calendar.getInstance();
        outPath = "output\\AGMIP_DSSAT_" + cal.getTimeInMillis();
        obDssatControllerOutput = new DssatControllerOutput();
        obDssatControllerOutput.writeFile(outPath, JSONAdapter.fromJSON(jsonStr));
        outDir = new File(outPath);
        files = outDir.listFiles();
        for (File file : files) {
            System.out.println("Generated: " + file.getName());
            file.delete();
        }
        outDir.delete();
        
        // Test for CRAFT
        result = obDssatControllerInput.readFileFromCRAFT(resource2.getPath());
//        f = new File("output/CRAFT.json");
//        bo = new BufferedOutputStream(new FileOutputStream(f));
//
//        // Output json for reading
//        bo.write(JSONAdapter.toJSON(result).getBytes());
//        bo.close();
//        f.delete();
        
        cal = Calendar.getInstance();
        outPath = "output\\AGMIP_DSSAT_" + cal.getTimeInMillis();
        obDssatControllerOutput = new DssatControllerOutput();
        obDssatControllerOutput.writeFile(outPath, result);
        outDir = new File(outPath);
        files = outDir.listFiles();
        for (File file : files) {
            System.out.println("Generated: " + file.getName());
            file.delete();
        }
        outDir.delete();
    }
}
