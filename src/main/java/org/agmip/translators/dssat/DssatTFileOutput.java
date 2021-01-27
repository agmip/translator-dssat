package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT Observation Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatTFileOutput extends DssatCommonOutput {

    private static final Logger LOG = LoggerFactory.getLogger(DssatTFileOutput.class);

    /**
     * DSSAT Observation Data Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     */
    @Override
    public void writeFile(String arg0, Map result) {

        // Initial variables
        ArrayList<HashMap> records;
        ArrayList<HashMap> recordT;
        ArrayList<HashMap> observeRecords;    // Array of data holder for time series data
        BufferedWriter bwT;                         // output object
        StringBuilder sbData = new StringBuilder();             // construct the data info in the output
        HashMap<String, String> altTitleList = new HashMap();   // Define alternative fields for the necessary observation data fields; key is necessary field
        // P.S. Add alternative fields here
        HashMap titleOutput;                              // contain output data field id
        DssatObservedData obvDataList = DssatObservedData.INSTANCE;    // Varibale list definition

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

            observeRecords = new ArrayList();
            for (HashMap record : records) {
                recordT = getObjectOr(record, "timeSeries", new ArrayList());
                String trno = getValueOr(record, "trno", "1");
                if (!recordT.isEmpty()) {
                    String[] sortIds = {"date"};
                    Collections.sort(recordT, new DssatSortHelper(sortIds));
                    for (HashMap recordT1 : recordT) {
                        recordT1.put("trno", trno);
                    }
                    observeRecords.addAll(recordT);
                }
            }

            // Initial BufferedWriter
            String fileName = getFileName(result, "T");
            if (fileName.endsWith(".XXT")) {
                String crid = DssatCRIDHelper.get2BitCrid(getValueOr(result, "crid", "XX"));
                fileName = fileName.replaceAll("XX", crid);
            }
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwT = new BufferedWriter(new FileWriter(outputFile));

            // Output Observation File
            // Titel Section
            sbError.append(String.format("*EXP.DATA (T): %1$-10s %2$s\r\n\r\n",
                    fileName.replaceAll("\\.", "").replaceAll("T$", ""),
                    getObjectOr(result, "local_name", defValBlank)));

            titleOutput = new HashMap();
            // TODO get title for output
            // Loop all records to find out all the titles
            for (HashMap record : observeRecords) {
                // Check if which field is available
                for (Object key : record.keySet()) {
                    // check which optional data is exist, if not, remove from map
                    if (obvDataList.isTimeSeriesData(key)) {
                        titleOutput.put(key, key);

                    } // check if the additional data is too long to output
                    else if (key.toString().length() <= 5) {
                        if (!key.equals("date") && !key.equals("trno")) {
                            titleOutput.put(key, key);
                        }

                    } // If it is too long for DSSAT, give a warning message
                    else {
                        sbError.append("! Waring: Unsuitable data for DSSAT observed data (too long): [").append(key).append("]\r\n");
                    }
                }
                // Check if all necessary field is available    // P.S. conrently unuseful
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
//            decompressData(observeRecords);
            // Observation Data Section
            Object[] titleOutputId = titleOutput.keySet().toArray();
            Arrays.sort(titleOutputId);
            String pdate = getPdate(result);
            for (int i = 0; i < (titleOutputId.length / 39 + titleOutputId.length % 39 == 0 ? 0 : 1); i++) {

                sbData.append("@TRNO   DATE");
                int limit = Math.min(titleOutputId.length, (i + 1) * 39);
                for (int j = i * 39; j < limit; j++) {
                    sbData.append(String.format("%1$6s", titleOutput.get(titleOutputId[j]).toString().toUpperCase()));
                }
                sbData.append("\r\n");

                for (HashMap record : observeRecords) {
                    
                    if (record.keySet().size() <= 2 && record.containsKey("trno") && record.containsKey("date")) {
                        continue;
                    }
                    sbData.append(String.format(" %1$5s", getValueOr(record, "trno", "1")));
                    sbData.append(String.format(" %1$5s", formatDateStr(getObjectOr(record, "date", defValI))));
                    for (int k = i * 39; k < limit; k++) {

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
            // Add section deviding line
            sbData.append("\r\n");

            // Output finish
            bwT.write(sbError.toString());
            bwT.write(sbData.toString());
            bwT.close();
        } catch (IOException e) {
            LOG.error(DssatCommonOutput.getStackTrace(e));
        }
    }
}
