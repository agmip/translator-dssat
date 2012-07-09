package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.agmip.util.JSONAdapter;
import static org.agmip.util.MapUtil.*;

/**
 * DSSAT Weather Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatWeatherOutput extends DssatCommonOutput {

    private File[] outputFiles;

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return getOutputFile(0);
    }

    /**
     * Get output file object by array index
     */
    public File getOutputFile(int id) {

        if (outputFiles != null && id < outputFiles.length) {
            return outputFiles[id];
        } else {
            return null;
        }
    }

    /**
     * Get output files array
     */
    public File[] getOutputFiles() {
        return outputFiles;
    }

    /**
     * DSSAT Weather Data Output method
     * 
     * @param arg0   file output path
     * @param result  data holder object
     */
    @Override
    public void writeFile(String arg0, Map result) {

        // Initial variables
        JSONAdapter adapter = new JSONAdapter();    // JSON Adapter
        ArrayList wthFiles;                     // Weather files array
        LinkedHashMap wthFile;                  // Data holder for whole weather data
        ArrayList wthRecords;                   // Daily data array
        LinkedHashMap wthRecord;                // Data holder for daily data
        BufferedWriter bwW;                     // output object
        StringBuilder sbData = new StringBuilder();     // construct the data info in the output
        ArrayList minDailyData = new ArrayList();       // Define minimum necessary daily data fields
        minDailyData.add("w_date");
        minDailyData.add("srad");
        minDailyData.add("tmax");
        minDailyData.add("tmin");
        minDailyData.add("rain");
        LinkedHashMap optDailyData = new LinkedHashMap();   // Define optional daily data fields
        optDailyData.put("tdew", "DEWP");
        optDailyData.put("wind", "WIND");
        optDailyData.put("pard", "PAR");
        ArrayList adtDaily = new ArrayList();           // Record the additional field from other model for output
        String dailyKey = "data";  // P.S. the key name might change

        try {

            // Set default value for missing data
            setDefVal();

            // Get weather files
            wthFiles = (ArrayList) getValueOr(result, "weather", new ArrayList());
            if (wthFiles.isEmpty()) {
                return;
            }
            decompressData(wthFiles);
            outputFiles = new File[wthFiles.size()];

            // Output all weather files
            for (int i = 0; i < wthFiles.size(); i++) {
                wthFile = (LinkedHashMap) wthFiles.get(i);
                wthRecords = (ArrayList) getValueOr(wthFile, dailyKey, new ArrayList());

                // Initial BufferedWriter
                // Get File name
                String fileName = getValueOr(wthFile, "wst_insi", "").toString();
                if (fileName.equals("")) {
                    fileName = "a.tmp";
                } else {
                    if (wthRecords.isEmpty()) {
                        fileName += "0001.WTH";
                    } else {
                        fileName += getValueOr(((LinkedHashMap) wthRecords.get(0)), "w_date", "2000").toString().substring(2, 4) + "01.WTH";
                    }
                }
                arg0 = revisePath(arg0);
                outputFiles[i] = new File(arg0 + fileName);
                bwW = new BufferedWriter(new FileWriter(outputFiles[i]));

                // Output Weather File
                // Titel Section
                sbData.append(String.format("*WEATHER DATA : %1$s\r\n\r\n", getValueOr(wthFile, "wst_name", defValC).toString()));

                // Weather Station Section
                sbData.append("@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT\r\n");
                sbData.append(String.format("  %1$-4s %2$8s %3$8s %4$5s %5$5s %6$5s %7$5s %8$5s\r\n",
                        getValueOr(wthFile, "wst_insi", defValC).toString(),
                        formatNumStr(8, getValueOr(wthFile, "wst_lat", defValR).toString()),
                        formatNumStr(8, getValueOr(wthFile, "wst_long", defValR).toString()),
                        formatNumStr(5, getValueOr(wthFile, "elev", defValR).toString()),
                        formatNumStr(5, getValueOr(wthFile, "tav", defValR).toString()),
                        formatNumStr(5, getValueOr(wthFile, "tamp", defValR).toString()),
                        formatNumStr(5, getValueOr(wthFile, "refht", defValR).toString()),
                        formatNumStr(5, getValueOr(wthFile, "wndht", defValR).toString())));

                // Daily weather data section
                // Fixed Title
                sbData.append("@DATE  SRAD  TMAX  TMIN  RAIN");

                // Unfixed Title
                // Get First day record to find how many fields there are
                LinkedHashMap fstDayRecord = new LinkedHashMap();
                if (!wthFile.isEmpty()) {
                    fstDayRecord = (LinkedHashMap) wthRecords.get(0);
                }

                // check if there are optional fields
                for (Object title : optDailyData.keySet()) {
                    if (fstDayRecord.containsKey(title)) {
                        adtDaily.add(title);
                        sbData.append(String.format("%1$6s", optDailyData.get(title).toString()));
                    } else {
                        adtDaily.add("");
                        sbData.append("      ");
                    }
                }

                // check if there are additional fields
                for (Object title : fstDayRecord.keySet()) {
                    if (!minDailyData.contains(title) && !optDailyData.containsKey(title)) {
                        // check title length is no more than 5
                        if (title.toString().length() <= 5) {
                            adtDaily.add(title);
                            sbData.append(String.format("%1$6s", title.toString().toUpperCase()));

                        } // If it is too long for DSSAT, give a warning message
                        else {
                            sbError.append("! Waring: Unsuitable data for DSSAT weather data (too long): [").append(title).append("]\r\n");
                        }
                    }
                }

                sbData.append("\r\n");

                for (int j = 0; j < wthRecords.size(); j++) {

                    wthRecord = (LinkedHashMap) wthRecords.get(j);
                    // if date is missing, jump the record
                    if (!getValueOr(wthRecord, "w_date", "").toString().equals("")) {
                        // Fixed data part
                        sbData.append(String.format("%1$5s %2$5s %3$5s %4$5s %5$5s",
                                formatDateStr(getValueOr(wthRecord, "w_date", defValD).toString()),
                                formatNumStr(5, getValueOr(wthRecord, "srad", defValR).toString()),
                                formatNumStr(5, getValueOr(wthRecord, "tmax", defValR).toString()),
                                formatNumStr(5, getValueOr(wthRecord, "tmin", defValR).toString()),
                                formatNumStr(5, getValueOr(wthRecord, "rain", defValR).toString())));

                        // Optional data part
                        for (int k = 0; k < adtDaily.size(); k++) {
                            if (adtDaily.get(k).equals("")) {
                                sbData.append("      ");
                            } else {
                                sbData.append(String.format(" %1$5s",
                                        formatNumStr(5, getValueOr(wthRecord, adtDaily.get(k).toString(), defValR).toString())));
                            }
                        }
                        sbData.append("\r\n");
                    } else {
                        // TODO Throw exception here
                        //System.out.println("A daily record has the missing date in it.");
                        sbError.append("! Warning: A daily record has the missing date in it.\r\n");
                    }
                }

                // Output finish
                bwW.write(sbError.toString());
                bwW.write(sbData.toString());
                bwW.close();
                sbError = new StringBuilder();
                sbData = new StringBuilder();
                adtDaily = new ArrayList();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Set default value for missing data
     * 
     */
    private void setDefVal() {

        // defValD = ""; No need to set default value for Date type in weather file
        defValR = "-99";
        defValC = "-99";    // TODO wait for confirmation
        defValI = "-99";
    }
}
