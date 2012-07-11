package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.agmip.util.MapUtil.*;

/**
 * DSSAT Observation Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatTFileOutput extends DssatCommonOutput {

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
    public void writeFile(String arg0, Map result) {

        // Initial variables
        LinkedHashMap<String, Object> record;     // Data holder for daily data
        BufferedWriter bwT;                         // output object
        StringBuilder sbData = new StringBuilder();         // construct the data info in the output
        HashMap altTitleList = new HashMap();               // Define alternative fields for the necessary observation data fields; key is necessary field
        // TODO Add alternative fields here
        LinkedHashMap titleOutput;                          // contain output data field id
        DssatObservedData obvDataList = new DssatObservedData();    // Varibale list definition

        try {

            // Set default value for missing data
            setDefVal();

            // Get Data from input holder
            LinkedHashMap expFile = (LinkedHashMap) getObjectOr(result, "experiment", result);
//            LinkedHashMap obvTFile = (LinkedHashMap) getObjectOr(expFile, "time_series", expFile));
//            ArrayList observeRecordsSections = ((ArrayList) obvTFile.getOr("data", new ArrayList()));
            ArrayList trArr = (ArrayList) getObjectOr(expFile, "management", new ArrayList());
            if (trArr.isEmpty()) {
                return;
            }

            // Initial BufferedWriter
            String exName = getExName(expFile);
            String fileName = exName;
            if (fileName.equals("")) {
                fileName = "a.tmp";
            } else {
                fileName = fileName.substring(0, fileName.length() - 2) + "." + fileName.substring(fileName.length() - 2) + "T";
            }
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwT = new BufferedWriter(new FileWriter(outputFile));

            // Output Observation File
            // Titel Section
            sbData.append(String.format("*EXP.DATA (T): %1$-10s %2$s\r\n\r\n", exName, getObjectOr(expFile, "local_name_t", getObjectOr(expFile, "local_name", defValC).toString())));

            // Loop Data Sections
            ArrayList observeRecords;
//            ArrayList<StringBuilder> sbArr = new ArrayList<StringBuilder>();
//            StringBuilder sbSub = new StringBuilder();
//            ArrayList<Set> titleSetArr = new ArrayList<Set>();
            for (int id = 0; id < trArr.size(); id++) {
                observeRecords = getObvData(trArr, id);
                
//                for (int i = 0; i < observeRecords.size(); i++) {
//                    ArrayList obvSubArr = (ArrayList) observeRecords.get(i);
//                    if (!obvSubArr.isEmpty()) {
//                        LinkedHashMap fstData = (LinkedHashMap) obvSubArr.get(0));
//                        if (titleSetArr.contains(fstData.keySet())) {
//                            
//                        } else {
//                            titleSetArr.add(fstData.keySet());
//                        }
//                        
//                        
//                    }
//                    
//                }
                
                titleOutput = new LinkedHashMap();

                // Get first record of observed data
                LinkedHashMap fstObvData;
                if (observeRecords.isEmpty()) {
                    fstObvData = new LinkedHashMap();
                } else {
                    return;
//                    fstObvData = (LinkedHashMap) observeRecords.get(0);
                }

                // Check if which field is available
                for (Object key : fstObvData.keySet()) {
                    // check which optional data is exist, if not, remove from map
                    if (obvDataList.isTimeSeriesData(key)) {
                        titleOutput.put(key, key);

                    } // check if the additional data is too long to output
                    else if (key.toString().length() <= 5) {
                        if (!key.equals("trno") && !key.equals("date")) {
                            titleOutput.put(key, key);
                        }

                    } // If it is too long for DSSAT, give a warning message
                    else {
                        sbError.append("! Waring: Unsuitable data for DSSAT observed data (too long): [").append(key).append("]\r\n");
                    }
                }

                // Check if all necessary field is available    // TODO conrently unuseful
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
                for (int i = 0; i < (titleOutputId.length / 39 + titleOutputId.length % 39 == 0 ? 0 : 1); i++) {

                    sbData.append("@TRNO   DATE");
                    int limit = Math.min(titleOutputId.length, (i + 1) * 39);
                    for (int j = i * 39; j < limit; j++) {
                        sbData.append(String.format("%1$6s", titleOutput.get(titleOutputId[j]).toString().toUpperCase()));
                    }
                    sbData.append("\r\n");

                    for (int j = 0; j < observeRecords.size(); j++) {

                        record = (LinkedHashMap) observeRecords.get(j);
                        sbData.append(String.format(" %1$5s", getObjectOr(record, "trno", 1).toString())); // TODO wait for confirmation that other model only have single treatment in one file
                        sbData.append(String.format(" %1$5d", Integer.parseInt(formatDateStr(getObjectOr(record, "date", defValI).toString()))));
                        for (int k = i * 39; k < limit; k++) {

                            if (obvDataList.isDapDateType(titleOutputId[k], titleOutput.get(titleOutputId[k]))) {
                                String pdate = (String) getObjectOr((LinkedHashMap) getObjectOr(result, "experiment", new LinkedHashMap()), "pdate", defValD); // TODO need be updated after ear;y version
                                sbData.append(String.format("%1$6s", formatDateStr(pdate, getObjectOr(record, titleOutput.get(titleOutputId[k]).toString(), defValI).toString())));
                            } else if (obvDataList.isDateType(titleOutputId[k])) {
                                sbData.append(String.format("%1$6s", formatDateStr(getObjectOr(record, titleOutput.get(titleOutputId[k]).toString(), defValI).toString())));
                            } else {
                                sbData.append(" ").append(formatNumStr(5, getObjectOr(record, titleOutput.get(titleOutputId[k]).toString(), defValI).toString()));
                            }

                        }
                        sbData.append("\r\n");
                    }
                }
                // Add section deviding line
                sbData.append("\r\n");
            }

            // Output finish
            bwT.write(sbError.toString());
            bwT.write(sbData.toString());
            bwT.close();
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

    /**
     * Get observed data map from treatment array
     * 
     * @param trArr treatment data array include all related info
     * @param idx   treatment index in the input array
     * 
     * @return the observed data map
     */
    private ArrayList getObvData(ArrayList trArr, int idx) {

        LinkedHashMap trData = (LinkedHashMap) trArr.get(idx);
        LinkedHashMap obvFile = (LinkedHashMap) getObjectOr(trData, "observed", new LinkedHashMap());
        ArrayList obvData = (ArrayList) getObjectOr(obvFile, "time_series", new ArrayList());

        return obvData;
    }
    
    private ArrayList getObvData(ArrayList trArr) {
        
        ArrayList obvDataArr = new ArrayList();
        ArrayList obvArrByTrnoIn;
//        ArrayList obvArrByTrnoOut;
        ArrayList obvArrByTitleIn;
        LinkedHashMap<Set, ArrayList> obvArrByTitleOut;
        
        for (int i = 0; i < trArr.size(); i++) {
            obvArrByTrnoIn = getObvData(trArr, i);
            for (int j = 0; j < obvArrByTrnoIn.size(); j++) {
                Object object = obvArrByTrnoIn.get(j);
                
            }
            
            
//            if (obvArrByTitleOut.containsKey(obvArrByTitleIn))
        }
        
        return obvDataArr;
    }
}
