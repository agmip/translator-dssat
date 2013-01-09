package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * DSSAT Run File I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatRunFileOutput extends DssatCommonOutput implements DssatBtachFile {

    /**
     * DSSAT Run File Output method
     *
     * @param arg0 file output path
     * @param results array of data holder object
     */
    public void writeFile(String arg0, ArrayList<HashMap> results) {
        writeFile(arg0, new HashMap());
    }

    /**
     * DSSAT Run File Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     */
    @Override
    public void writeFile(String arg0, Map result) {

        // Initial variables
        BufferedWriter bwR;                         // output object

        try {

            // Initial BufferedWriter
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + "Run.bat");
            bwR = new BufferedWriter(new FileWriter(outputFile));

            // Output Run File
            bwR.write("C:\\dssat45\\dscsm045 b dssbatch.v45\r\n");
            bwR.write("@echo off\r\n");
            bwR.write("pause\r\n");

            // Output finish
            bwR.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
