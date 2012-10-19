package org.agmip.translators.dssat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
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
public class DssatAcmoCsvTranslatorTest {

    DssatOutputFileInput obDssatOutputFileInput;
    DssatAcmoCsvTranslator obDssatAcmoCsvTanslator;
    URL resource;

    @Before
    public void setUp() throws Exception {
        obDssatOutputFileInput = new DssatOutputFileInput();
        obDssatAcmoCsvTanslator = new DssatAcmoCsvTranslator();
        resource = this.getClass().getClassLoader().getResource("testCsv.zip");
    }

    @Test
    public void test() throws IOException, Exception {
        LinkedHashMap result = obDssatOutputFileInput.readFile(resource.getPath());
//        System.out.println(JSONAdapter.toJSON(result));
        File f = new File("outputOut.txt");
        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
        bo.write(JSONAdapter.toJSON(result).getBytes());
        bo.close();
        f.delete();

        obDssatAcmoCsvTanslator.writeCsvFile("", resource.getPath());
        File file = obDssatAcmoCsvTanslator.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertTrue(file.getName().equals("ACMO.csv"));
            assertTrue(file.delete());
        }
    }
}
