package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.agmip.core.types.AdvancedHashMap;
import org.agmip.util.JSONAdapter;

/**
 * DSSAT Observation Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatAFileOutput extends DssatCommonOutput {

    private File outputFile;

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * DSSAT Observation Data Output method
     *
     * @param arg0  file output path
     * @param result data holder object
     */
    @Override
    public void writeFile(String arg0, AdvancedHashMap result) {

        // Initial variables
        JSONAdapter adapter = new JSONAdapter();    // JSON Adapter
        AdvancedHashMap<String, Object> record;     // Data holder for daily data
        BufferedWriter bwA;                         // output object
        StringBuilder sbData = new StringBuilder();         // construct the data info in the output
        HashMap altTitleList = new HashMap();               // Define alternative fields for the necessary observation data fields; key is necessary field
        altTitleList.put("hwam", "hwah");
        altTitleList.put("mdat", "mdap");
        altTitleList.put("adat", "adap");
        altTitleList.put("cwam", "");
        LinkedHashMap titleOutput = new LinkedHashMap();    // contain output data field id
        DssatObservedData obvDataList = new DssatObservedData();    // Varibale list definition

        try {

            // Set default value for missing data
            setDefVal();

            // Get Data from input holder
            AdvancedHashMap obvFile = adapter.exportRecord((Map) result.getOr("observed", result));
            AdvancedHashMap obvAFile = adapter.exportRecord((Map) obvFile.getOr("summary", obvFile));
            ArrayList observeRecords = ((ArrayList) obvAFile.getOr("data", new ArrayList()));

            // Initial BufferedWriter
            String exName = getExName(obvAFile);
            String fileName = exName;
            if (fileName.equals("")) {
                fileName = "a.tmp";
            } else {
                fileName = fileName.substring(0, fileName.length() - 2) + "." + fileName.substring(fileName.length() - 2) + "A";
            }
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwA = new BufferedWriter(new FileWriter(outputFile));

            // Output Observation File
            // Titel Section
            sbData.append(String.format("*EXP.DATA (A): %1$-10s %2$s\r\n\r\n", exName, obvAFile.getOr("local_name", defValC).toString()));

            // Get first record of observed data
            AdvancedHashMap fstObvData;
            if (observeRecords.isEmpty()) {
                fstObvData = new AdvancedHashMap();
            } else {
                fstObvData = adapter.exportRecord((Map) observeRecords.get(0));
            }

            // Check if which field is available
            for (Object key : fstObvData.keySet()) {
                // check if the data is belong to summary data
                if (obvDataList.isSummaryData(key)) {
                    titleOutput.put(key, key);

                } // check if the additional data is too long to output
                else if (key.toString().length() <= 5) {
                    if (!key.equals("trno")) {
                        titleOutput.put(key, key);
                    }

                } // If it is too long for DSSAT, give a warning message
                else {
                    sbError.append("! Waring: Unsuitable data for DSSAT observed data (too long): [").append(key).append("]\r\n");
                }
            }

            // Check if all necessary field is available
            for (Object title : altTitleList.keySet()) {

                // check which optional data is exist, if not, remove from map
                if (!fstObvData.containsKey(title)) {

                    if (fstObvData.containsKey(altTitleList.get(title))) {
                        titleOutput.put(title, altTitleList.get(title));
                    } else {
                        // TODO throw new Exception("Incompleted record because missing data : [" + title + "]");
                        sbError.append("! Waring: Incompleted record because missing data : [").append(title).append("]\r\n");
                    }

                } else {
                }
            }

            // decompress observed data
            decompressData(observeRecords);
            // Observation Data Section
            Object[] titleOutputId = titleOutput.keySet().toArray();
            for (int i = 0; i < (titleOutputId.length / 40 + titleOutputId.length % 40 == 0 ? 0 : 1); i++) {

                sbData.append("@TRNO ");
                int limit = Math.min(titleOutputId.length, (i + 1) * 40);
                for (int j = i * 40; j < limit; j++) {
                    sbData.append(String.format("%1$6s", titleOutput.get(titleOutputId[j]).toString().toUpperCase()));
                }
                sbData.append("\r\n");

                for (int j = 0; j < observeRecords.size(); j++) {

                    record = adapter.exportRecord((Map) observeRecords.get(j));
                    sbData.append(String.format(" %1$5s", record.getOr("trno", j + 1)));
                    for (int k = i * 40; k < limit; k++) {

                        if (obvDataList.isDapDateType(titleOutputId[k], titleOutput.get(titleOutputId[k]))) {
                            String pdate = (String) ((AdvancedHashMap) result.getOr("experiment", new AdvancedHashMap())).getOr("pdate", defValD); // TODO need be updated after ear;y version
                            sbData.append(String.format("%1$6s", formatDateStr(pdate, record.getOr(titleOutput.get(titleOutputId[k]).toString(), defValI).toString())));
                        } else if (obvDataList.isDateType(titleOutputId[k])) {
                            sbData.append(String.format("%1$6s", formatDateStr(record.getOr(titleOutput.get(titleOutputId[k]).toString(), defValI).toString())));
                        } else {
                            sbData.append(" ").append(formatNumStr(5, record.getOr(titleOutput.get(titleOutputId[k]).toString(), defValI).toString()));
                        }
                    }
                    sbData.append("\r\n");
                }
            }

            // Output finish
            bwA.write(sbError.toString());
            bwA.write(sbData.toString());
            bwA.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Set default value for missing data
     *
     * @version 1.0
     */
    private void setDefVal() {

        // defValD = ""; No need to set default value for Date type in Observation file
        defValR = "-99";
        defValC = "-99";    // TODO wait for confirmation
        defValI = "-99";
    }
}
