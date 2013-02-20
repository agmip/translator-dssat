package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.agmip.util.MapUtil.*;

/**
 * DSSAT Weather Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatWeatherOutput extends DssatCommonOutput {

    /**
     * DSSAT Weather Data Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     */
    @Override
    public void writeFile(String arg0, Map result) {

        // Initial variables
        HashMap wthFile;                  // Data holder for whole weather data
        ArrayList wthRecords;                   // Daily data array
        HashMap wthRecord;                // Data holder for daily data
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
        String dailyKey = "dailyWeather";  // P.S. the key name might change

        try {

            // Set default value for missing data
            setDefVal();

            // Get weather files
            wthFile = (HashMap) getObjectOr(result, "weather", new HashMap());
            if (wthFile.isEmpty()) {
                return;
            }
//            decompressData(wthFiles);

            // Output all weather files
            wthRecords = (ArrayList) getObjectOr(wthFile, dailyKey, new ArrayList());

            // Initial BufferedWriter
            // Get File name
            String fileName = getValueOr(result, "wst_id", "");
            if (fileName.equals("")) {
                fileName = getWthFileName(wthFile);
            }
            fileName += ".WTH";
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwW = new BufferedWriter(new FileWriter(outputFile));

            // Output Weather File
            // Titel Section
            sbData.append(String.format("*WEATHER DATA : %1$s\r\n\r\n", getObjectOr(wthFile, "wst_name", defValBlank).toString()));

            // Weather Station Section
            String wid = getObjectOr(wthFile, "wst_id", defValC);
            if (wid.length() > 4) {
                wid = wid.substring(0, 4);
            }
            sbData.append("@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT   CO2\r\n");
            sbData.append(String.format("  %1$-4s %2$8s %3$8s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s\r\n",
                    formatStr(4, wthFile, "dssat_wst_id", wid),
                    formatNumStr(8, wthFile, "wst_lat", defValR),
                    formatNumStr(8, wthFile, "wst_long", defValR),
                    formatNumStr(5, wthFile, "elev", defValR),
                    formatNumStr(5, wthFile, "tav", defValR),
                    formatNumStr(5, wthFile, "tamp", defValR),
                    formatNumStr(5, wthFile, "refht", defValR),
                    formatNumStr(5, wthFile, "wndht", defValR),
                    formatNumStr(5, wthFile, "co2y", defValR)));

            // Daily weather data section
            // Fixed Title
            sbData.append("@DATE  SRAD  TMAX  TMIN  RAIN");

            // Unfixed Title
            // Get First day record to find how many fields there are
            HashMap fstDayRecord = new HashMap();
            if (!wthFile.isEmpty()) {
                fstDayRecord = (HashMap) wthRecords.get(0);
            }

            // check if there are optional fields
            for (Object title : optDailyData.keySet()) {
                if (!getObjectOr(fstDayRecord, title, "").equals("")) {
                    adtDaily.add(title);
                    sbData.append(String.format("%1$6s", optDailyData.get(title).toString()));
                } else {
                    adtDaily.add("");
                    sbData.append(String.format("%1$6s", optDailyData.get(title).toString()));
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

                wthRecord = (HashMap) wthRecords.get(j);
                // if date is missing, jump the record
                if (!getObjectOr(wthRecord, "w_date", "").toString().equals("")) {
                    // Fixed data part
                    sbData.append(String.format("%1$5s %2$5s %3$5s %4$5s %5$5s",
                            formatDateStr(getObjectOr(wthRecord, "w_date", defValD).toString()),
                            formatNumStr(5, wthRecord, "srad", defValR),
                            formatNumStr(5, wthRecord, "tmax", defValR),
                            formatNumStr(5, wthRecord, "tmin", defValR),
                            formatNumStr(5, wthRecord, "rain", defValR)));

                    // Optional data part
                    for (int k = 0; k < adtDaily.size(); k++) {
                        if (adtDaily.get(k).equals("")) {
                            sbData.append("      ");
                        } else {
                            sbData.append(String.format(" %1$5s",
                                    formatNumStr(5, wthRecord, adtDaily.get(k).toString(), defValR)));
                        }
                    }
                    sbData.append("\r\n");
                } else {
                    sbError.append("! Warning: A daily record has the missing date in it.\r\n");
                }
            }

            // Output finish
            bwW.write(sbError.toString());
            bwW.write(sbData.toString());
            bwW.close();
            sbError = new StringBuilder();
//            sbData = new StringBuilder();
//            adtDaily = new ArrayList();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
