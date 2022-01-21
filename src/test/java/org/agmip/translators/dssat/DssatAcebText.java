package org.agmip.translators.dssat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Meng Zhang
 */
public class DssatAcebText {
    DssatAcebOutput translator;
    URL resource;
    String fileName =
        "Survey_data-Machakos-MAZ.aceb";

    @Before
    public void setUp() throws Exception {
        translator = new DssatAcebOutput();
        resource = this.getClass().getResource("/" + fileName);
    }

    @Test
    @Ignore
    public void test() throws IOException, Exception {
        Calendar cal = Calendar.getInstance();
        String outPath = "output\\AGMIP_DSSAT_ACEB_" + cal.getTimeInMillis();
        File outDir = new File(outPath);
        
        ArrayList<File> files = DssatAcebOutput.writeFile(outPath, new FileInputStream(resource.getFile()));
        for (File file : files) {
            System.out.println("Generated: " + file.getName());
        }
        outDir.delete();
        
        File zipFile = DssatAcebOutput.writeZipFile(outPath, new FileInputStream(resource.getFile()));
        System.out.println("Generated: " + zipFile.getName());
        zipFile.delete();
        outDir.delete();
    }
}
