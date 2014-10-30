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
 * DSSAT Cultivar Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatCulFileOutput extends DssatCommonOutput {

    private static final Logger LOG = LoggerFactory.getLogger(DssatCulFileOutput.class);

    /**
     * DSSAT Cultivar Data Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     */
    @Override
    public void writeFile(String arg0, Map result) {

        // Initial variables
        HashMap culData;              // Data holder for one site of cultivar data
        ArrayList<HashMap> culArr;    // Data holder for one site of cultivar data
        BufferedWriter bwC;                             // output object
        StringBuilder sbData = new StringBuilder();     // construct the data info in the output

        try {

            // Set default value for missing data
            setDefVal();

            culData = getObjectOr(result, "dssat_cultivar_info", new HashMap());
            culArr = getObjectOr(culData, "data", new ArrayList());
            if (culArr.isEmpty()) {
                return;
            }
//            decompressData(culArr);

            // Initial BufferedWriter
            // Get File name
            String fileName = getFileName(result, "X");
            if (fileName.matches("TEMP\\d{4}\\.\\w{2}X")) {
                fileName = "Cultivar.CUL";
            } else {
                try {
                    fileName = fileName.replaceAll("\\.", "_") + ".CUL";
                } catch (Exception e) {
                    fileName = "Cultivar.CUL";
                }
            }
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwC = new BufferedWriter(new FileWriter(outputFile, outputFile.exists()));

            // Output Cultivar File
            String lastHeaderInfo = "";
            String lastTitles = "";
            for (HashMap culSubData : culArr) {
                // If come to new header, add header line and title line
                if (!getObjectOr(culSubData, "header_info", "").equals(lastHeaderInfo)) {
                    lastHeaderInfo = getObjectOr(culSubData, "header_info", "");
                    sbData.append(lastHeaderInfo).append("\r\n");
                    lastTitles = getObjectOr(culSubData, "cul_titles", "");
                    sbData.append(lastTitles).append("\r\n");
                }
                // If come to new title line, add title line
                if (!getObjectOr(culSubData, "cul_titles", "").equals(lastTitles)) {
                    lastTitles = getObjectOr(culSubData, "cul_titles", "");
                    sbData.append(lastTitles).append("\r\n");
                }
                // Write data line
                sbData.append(getObjectOr(culSubData, "cul_info", "")).append("\r\n");
            }

            // Output finish
            bwC.write(sbError.toString());
            bwC.write(sbData.toString());
            bwC.close();

        } catch (IOException e) {
            LOG.error(DssatCommonOutput.getStackTrace(e));
        }
    }
}
