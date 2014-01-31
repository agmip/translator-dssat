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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT Weather Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatWeatherOutput extends DssatCommonOutput {

    private static final Logger LOG = LoggerFactory.getLogger(DssatWeatherOutput.class);

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
        LinkedHashMap dailyVarHeaderMap = new LinkedHashMap();   // Define the mapping of daily data fields and header
        dailyVarHeaderMap.put("tdew", "DEWP");
        dailyVarHeaderMap.put("wind", "WIND");
        dailyVarHeaderMap.put("pard", "PAR");
        dailyVarHeaderMap.put("vprsd", "VPRS");
        dailyVarHeaderMap.put("rhumd", "RHUM");
        String dailyKey = "dailyWeather";  // P.S. the key name might change
        HeaderArrayList<String> dailyHeaders = new HeaderArrayList();
        StringBuilder[] dailyData;

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
                fileName = getWthFileName(result);
            }
            fileName += ".WTH";
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwW = new BufferedWriter(new FileWriter(outputFile));

            // Output Weather File
            // Titel Section
            if (getObjectOr(wthFile, "wst_notes", ".AgMIP File").equals(".AgMIP File")) {
                sbData.append(String.format("*WEATHER DATA : %1$s\r\n!Climate ID: %2$s\r\n", getObjectOr(wthFile, "wst_source", defValBlank).toString(), getValueOr(wthFile, "clim_id", "N/A")));
            } else {
                sbData.append(String.format("*WEATHER DATA : %1$s\r\n!Climate ID: %2$s\r\n", getObjectOr(wthFile, "wst_notes", defValBlank).toString(), getValueOr(wthFile, "clim_id", "N/A")));
            }

            // Weather Station Section
            String wid = getObjectOr(wthFile, "wst_id", defValC);
            if (wid.length() > 4) {
                wid = wid.substring(0, 4);
            }
            sbData.append("@ INSI      LAT     LONG  ELEV   TAV   AMP REFHT WNDHT  CCO2\r\n");
            sbData.append(String.format("  %1$-4s %2$8s %3$8s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s\r\n",
                    formatStr(4, wthFile, "dssat_insi", wid),
                    formatNumStr(8, wthFile, "wst_lat", defValR),
                    formatNumStr(8, wthFile, "wst_long", defValR),
                    formatNumStr(5, wthFile, "wst_elev", defValR),
                    formatNumStr(5, wthFile, "tav", defValR),
                    formatNumStr(5, wthFile, "tamp", defValR),
                    formatNumStr(5, wthFile, "refht", defValR),
                    formatNumStr(5, wthFile, "wndht", defValR),
                    formatNumStr(5, wthFile, "co2y", defValR)));

            // Daily weather data section
            // Title line
            sbData.append("@DATE  SRAD  TMAX  TMIN  RAIN  DEWP  WIND   PAR");
            // Register fixed headers
            dailyHeaders.add("w_date");
            dailyHeaders.add("srad");
            dailyHeaders.add("tmax");
            dailyHeaders.add("tmin");
            dailyHeaders.add("rain");
            dailyHeaders.add("tdew");
            dailyHeaders.add("wind");
            dailyHeaders.add("pard");

            // Output daily data
            dailyData = new StringBuilder[wthRecords.size()];
            for (int i = 0; i < wthRecords.size(); i++) {

                wthRecord = (HashMap) wthRecords.get(i);
                dailyData[i] = new StringBuilder();
                dailyHeaders.seCurItems(wthRecord.keySet());

                // if date is missing, jump the record
                if (!getObjectOr(wthRecord, "w_date", "").toString().equals("")) {
                    //  Format handling for daily date
                    dailyData[i].append(String.format("%1$5s", formatDateStr(getObjectOr(wthRecord, dailyHeaders.get(0), defValD))));

                    // Output the registered variables
                    for (int j = 1; j < dailyHeaders.size(); j++) {
                        dailyData[i].append(String.format(" %1$5s", formatNumStr(5, wthRecord, dailyHeaders.get(j), defValR)));
                    }

                    // Check if there is new variable not been regitered in the output list
                    while (dailyHeaders.hasMoreItem()) {
                        // Register the variable
                        String key = dailyHeaders.applyNext();

                        if (key != null) {
                            // Add title to the header line
                            sbData.append(String.format("%1$6s", getObjectOr(dailyVarHeaderMap, key, key.toUpperCase())));
                            // Add blank for previous lines (this might be optional)
                            for (int j = 0; j < i; j++) {
                                dailyData[j].append("   -99");
                            }
                            // Output new variable
                            dailyData[i].append(String.format(" %1$5s", formatNumStr(5, wthRecord, key, defValR)));
                        }
                    }
                } else {
                    sbError.append("! Warning: A daily record has the missing date in it.\r\n");
                }
            }

            // Combine the daily data
            sbData.append("\r\n");
            for (int i = 0; i < dailyData.length; i++) {
                dailyData[i].append("\r\n");
                sbData.append(dailyData[i]);
                dailyData[i] = null;
            }

            // Output finish
            bwW.write(sbError.toString());
            bwW.write(sbData.toString());
            bwW.close();
            sbError = new StringBuilder();

        } catch (IOException e) {
            LOG.error(DssatCommonOutput.getStackTrace(e));
        }
    }
}
