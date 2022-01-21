package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT Run File I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatRunFileOutput extends DssatCommonOutput implements DssatBtachFile {

    private static final Logger LOG = LoggerFactory.getLogger(DssatRunFileOutput.class);
    private String dssatVerStr;

    public DssatRunFileOutput() {
        dssatVerStr = null;
    }

    public DssatRunFileOutput(DssatBatchFileOutput.DssatVersion version) {
        dssatVerStr = version.toString();
    }

    /**
     * DSSAT Run File Output method
     *
     * @param arg0 file output path
     * @param results array of data holder object
     */
    @Override
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

            // Get version number
            if (dssatVerStr == null) {
                dssatVerStr = getObjectOr(result, "crop_model_version", "").replaceAll("\\D", "");
                if (!dssatVerStr.matches("\\d+")) {
                    dssatVerStr = DssatBatchFileOutput.DssatVersion.DSSAT47.toString();
                }
            }

            // Initial BufferedWriter
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + "Run" + dssatVerStr + ".bat");
            bwR = new BufferedWriter(new FileWriter(outputFile));

            // Output Run File
            bwR.write("C:\\dssat" + dssatVerStr + "\\dscsm0" + dssatVerStr + " b dssbatch.v" + dssatVerStr + "\r\n");
            bwR.write("@echo off\r\n");
            bwR.write("pause\r\n");
            bwR.write("exit\r\n");

            // Output finish
            bwR.close();
            
//            // Output Run File for 2D
//            File outputFile2D = new File(arg0 + "Run" + dssatVerStr + "_2D.bat");
//            bwR = new BufferedWriter(new FileWriter(outputFile2D));
//            bwR.write("C:\\dssat" + dssatVerStr + "\\dscsm0" + dssatVerStr + "_2D b dssbatch.v" + dssatVerStr + "\r\n");
//            bwR.write("@echo off\r\n");
//            bwR.write("pause\r\n");
//            bwR.write("exit\r\n");
//            bwR.close();
            
        } catch (IOException e) {
            LOG.error(DssatCommonOutput.getStackTrace(e));
        }
    }
}
