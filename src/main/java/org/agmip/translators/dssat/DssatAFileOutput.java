package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT Observation Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatAFileOutput extends DssatCommonOutput {

    private static final Logger LOG = LoggerFactory.getLogger(DssatAFileOutput.class);

    /**
     * DSSAT Observation Data Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     */
    @Override
    public void writeFile(String arg0, Map result) {

        // Initial variables
        ArrayList<HashMap<String, String>> records; // Array of Data holder for summary data
        BufferedWriter bwA;                         // output object
        StringBuilder sbData = new StringBuilder(); // construct the data info in the output
        HashMap<String, String> altTitleList = new HashMap();   // Define alternative fields for the necessary observation data fields; key is necessary field
        altTitleList.put("hwam", "hwah");
        altTitleList.put("mdat", "mdap");
        altTitleList.put("adat", "adap");
        altTitleList.put("cwam", "");
        HashMap titleOutput = new HashMap();    // contain output data field id
        DssatObservedData obvDataList = DssatObservedData.INSTANCE;  // Varibale list definition

        try {

            // Set default value for missing data
            setDefVal();

            // Get Data from input holder
            Object tmpData = getObjectOr(result, "observed", new Object());
            if (tmpData instanceof ArrayList) {
                records = (ArrayList) tmpData;
            } else if (tmpData instanceof HashMap) {
                records = new ArrayList();
                records.add((HashMap) tmpData);
            } else {
                return;
            }
            if (records.isEmpty()) {
                return;
            }

            // Initial BufferedWriter
            String fileName = getFileName(result, "A");
            if (fileName.endsWith(".XXA")) {
                String crid = DssatCRIDHelper.get2BitCrid(getValueOr(result, "crid", "XX"));
                fileName = fileName.replaceAll("XX", crid);
            }
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwA = new BufferedWriter(new FileWriter(outputFile));

            // Output Observation File
            // Titel Section
            sbError.append(String.format("*EXP.DATA (A): %1$-10s %2$s\r\n\r\n",
                    fileName.replaceAll("\\.", "").replaceAll("T$", ""),
                    getObjectOr(result, "local_name", defValBlank)));

            // Check if which field is available
            for (HashMap<String, String> record : records) {
                for (String key : record.keySet()) {

                    if (!(record.get(key) instanceof String)) {
                        continue;
                    } else if (titleOutput.containsKey(key)) {
                        continue;
                    } else if (key.equals("trno")) {
                        continue;
                    }

                    // check if the data is belong to summary data
                    if (obvDataList.isSummaryData(key)) {
                        titleOutput.put(key, key);

                    } // check if the additional data is too long to output
                    else if (key.length() <= 5) {
                        titleOutput.put(key, key);

                    } // If it is too long for DSSAT, give a warning message
                    else {
                        sbError.append("! Waring: Unsuitable data for DSSAT observed data (too long): [").append(key).append("]\r\n");
                    }
                }
                // Check if all necessary field is available
                for (String title : altTitleList.keySet()) {

                    // check which optional data is exist, if not, remove from map
                    if (getValueOr(record, title, "").equals("")) {

                        if (!getValueOr(record, altTitleList.get(title), "").equals("")) {
                            titleOutput.put(title, altTitleList.get(title));
                        } else {
                            sbError.append("! Waring: Incompleted record because missing data : [").append(title).append("]\r\n");
                        }

                    } else {
                    }
                }
            }

            // decompress observed data
//            decompressData(trArr);
            // Observation Data Section
            Object[] titleOutputId = titleOutput.keySet().toArray();
            Arrays.sort(titleOutputId);
            String pdate = getPdate(result);
            int loops = titleOutputId.length / 40 + titleOutputId.length % 40 == 0 ? 0 : 1;
            for (int i = 0; i < loops; i++) {

                // Write title line
                sbData.append("@TRNO ");
                int limit = Math.min(titleOutputId.length, (i + 1) * 40);
                for (int j = i * 40; j < limit; j++) {
                    sbData.append(String.format("%1$6s", titleOutputId[j].toString().toUpperCase()));
                }
                sbData.append("\r\n");

                // Write data line
                for (HashMap<String, String> record : records) {
                    sbData.append(String.format(" %1$5s", getValueOr(record, "trno", "1")));
                    for (int k = i * 40; k < limit; k++) {

                        if (obvDataList.isDapDateType(titleOutputId[k], titleOutput.get(titleOutputId[k]))) {
                            sbData.append(String.format("%1$6s", formatDateStr(pdate, getObjectOr(record, titleOutput.get(titleOutputId[k]).toString(), defValI))));
                        } else if (obvDataList.isDateType(titleOutputId[k])) {
                            sbData.append(String.format("%1$6s", formatDateStr(getObjectOr(record, titleOutput.get(titleOutputId[k]).toString(), defValI))));
                        } else {
                            sbData.append(" ").append(formatNumStr(5, record, titleOutput.get(titleOutputId[k]), defValI));
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
            LOG.error(DssatCommonOutput.getStackTrace(e));
        }
    }

    /**
     * Remove the 2-bit year number from input date string
     *
     * @param str input date string
     * @return the days of year
     */
    private String cutYear(String str) {
        if (str.length() > 3) {
            return str.substring(str.length() - 3, str.length());
        } else {
            return str;
        }
    }
}
