package org.agmip.translators.dssat;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import org.agmip.core.types.TranslatorOutput;
import static org.agmip.util.MapUtil.*;

/**
 * DSSAT Experiment Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public abstract class DssatCommonOutput implements TranslatorOutput {

    // Default value for each type of value (R: real number; C: String; I: Integer; D: Date)
    protected String defValR = "0.00";
    protected String defValC = "";
    protected String defValI = "0";
    protected String defValD = "-99";
    protected String defValBlank = "";
    // construct the error message in the output
    protected StringBuilder sbError = new StringBuilder();

    /**
     * Translate data str from "yyyymmdd" to "yyddd"
     *
     * 2012/3/19 change input format from "yy/mm/dd" to "yyyymmdd"
     *
     * @param str date string with format of "yyyymmdd"
     * @return result date string with format of "yyddd"
     */
    protected String formatDateStr2(String str) {

        // Initial Calendar object
        Calendar cal = Calendar.getInstance();
        str = str.replaceAll("/", "");
        try {
            // Set date with input value
            cal.set(Integer.parseInt(str.substring(0, 4)), Integer.parseInt(str.substring(4, 6)) - 1, Integer.parseInt(str.substring(6)));
            // translatet to yyddd format
            return String.format("%1$02d%2$03d", cal.get(Calendar.YEAR) % 100, cal.get(Calendar.DAY_OF_YEAR));
        } catch (Exception e) {
            // if tranlate failed, then use default value for date
            //sbError.append("! Waring: There is a invalid date [").append(str).append("]\r\n");
            return formatDateStr2(defValD);
        }
    }

    /**
     * Format the number with maximum length and type
     *
     * @param bits Maximum length of the output string
     * @param str Input string of number
     * @return formated string of number
     */
    protected String formatNumStr(int bits, LinkedHashMap m, Object key, String defVal) {

        String ret = "";
        String str = getObjectOr(m, key, defVal);
        double decimalPower;
        long decimalPart;
        double input;
        String[] inputStr = str.split("\\.");
        if (str.trim().equals("")) {
            return String.format("%" + bits + "s", defVal);
        } else if (inputStr[0].length() > bits) {
            //throw new Exception();
            sbError.append("! Waring: There is a variable [").append(key).append("] with oversized number [").append(str).append("] (Limitation is ").append(bits).append("bits)\r\n");
            return String.format("%" + bits + "s", defVal);
        } else {
            ret = inputStr[0];

            if (inputStr.length > 1 && inputStr[0].length() < bits) {

                if (inputStr[1].length() <= bits - inputStr[0].length() - 1) {
                    ret = ret + "." + inputStr[1];
                } else {
                    try {
                        input = Math.abs(Double.valueOf(str));
                    } catch (Exception e) {
                        // TODO throw exception
                        return str;
                    }
                    //decimalPower = Math.pow(10, Math.min(bits - inputStr[0].length(), inputStr[1].length()) - 1);
                    decimalPower = Math.pow(10, bits - inputStr[0].length() - 1);
                    decimalPart = Double.valueOf(Math.round(input * decimalPower) % decimalPower).longValue();
                    ret = ret + "." + (decimalPart == 0 && (bits - inputStr[0].length() < 2) ? "" : decimalPart);
                }
            }
            if (ret.length() < bits) {
                ret = String.format("%1$" + bits + "s", ret);
            }
        }

        return ret;
    }

    /**
     * Translate data str from "yyyymmdd" to "yyddd"
     *
     * 2012/3/19 change input format from "yy/mm/dd" to "yyyymmdd"
     *
     * @param str date string with format of "yyyymmdd"
     * @return result date string with format of "yyddd"
     */
    protected String formatDateStr(String str) {

        return formatDateStr(str, "0");
    }

    /**
     * Translate data str from "yyyymmdd" to "yyddd" plus days you want
     *
     * @param startDate date string with format of "yyyymmdd"
     * @param strDays the number of days need to be added on
     * @return result date string with format of "yyddd"
     */
    protected String formatDateStr(String startDate, String strDays) {

        // Initial Calendar object
        Calendar cal = Calendar.getInstance();
        int days;
        startDate = startDate.replaceAll("/", "");
        try {
            days = Double.valueOf(strDays).intValue();
            // Set date with input value
            cal.set(Integer.parseInt(startDate.substring(0, 4)), Integer.parseInt(startDate.substring(4, 6)) - 1, Integer.parseInt(startDate.substring(6)));
            cal.add(Calendar.DATE, days);
            // translatet to yyddd format
            return String.format("%1$02d%2$03d", cal.get(Calendar.YEAR) % 100, cal.get(Calendar.DAY_OF_YEAR));
        } catch (Exception e) {
            // if tranlate failed, then use default value for date
            // sbError.append("! Waring: There is a invalid date [").append(startDate).append("]\r\n");
            return "-99"; //formatDateStr(defValD);
        }

    }

    /**
     * Get exname with normal format
     *
     * @param result date holder for experiment data
     * @return exname
     */
    protected String getExName(Map result) {

        String ret = getValueOr(result, "exname", "");
        if (ret.contains(".")) {
            ret = ret.substring(0, ret.length() - 1).replace(".", "");
        }
        // TODO need to be updated with a translate rule for other models' exname
        if (ret.length() > 10 && ret.matches("\\w+_\\d+")) {
            ret = ret.replaceAll("_\\d+$", "");
        }

        return ret;
    }

    /**
     * Revise output path
     *
     * @param path the output path
     * @return revised path
     */
    protected String revisePath(String path) {
        if (!path.trim().equals("")) {
            path = path.replaceAll("/", "\\");
            if (path.endsWith("\\")) {
                path += "\\";
            }
        }
        return path;
    }

    /**
     * Get output file object
     */
    public abstract File getOutputFile();

    /**
     * decompress the data in a map object
     *
     * @param m input map
     */
    protected void decompressData(LinkedHashMap m) {

        for (Object key : m.keySet()) {
            if (m.get(key) instanceof ArrayList) {
                // iterate sub array nodes
                decompressData((ArrayList) m.get(key));
            } else if (m.get(key) instanceof LinkedHashMap) {
                // iterate sub data nodes
                decompressData((LinkedHashMap) m.get(key));
            } else {
                // ignore other type nodes
            }
        }

    }

    /**
     * decompress the data in an ArrayList object
     *
     * @param arr input ArrayList
     *
     */
    protected void decompressData(ArrayList arr) {

        LinkedHashMap fstData = null; // The first data record (Map type)
        LinkedHashMap cprData = null; // The following data record which will be compressed

        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i) instanceof ArrayList) {
                // iterate sub array nodes
                decompressData((ArrayList) arr.get(i));

            } else if (arr.get(i) instanceof LinkedHashMap) {
                // iterate sub data nodes
                decompressData((LinkedHashMap) arr.get(i));

                // Compress data for current array
                if (fstData == null) {
                    // Get first data node
                    fstData = (LinkedHashMap) arr.get(i);
                } else {
                    cprData = (LinkedHashMap) arr.get(i);
                    // The omitted data will be recovered to the following map; Only data item (String type) will be processed
                    for (Object key : fstData.keySet()) {
                        if (!cprData.containsKey(key)) {
                            cprData.put(key, fstData.get(key));
                        }
                    }
                }
            } else {
            }
        }
    }

    /**
     * Get plating date from experiment management event
     * 
     * @param result    the experiment data object
     * @return plating date
     */
    protected String getPdate(Map result) {

        LinkedHashMap management = getObjectOr(result, "management", new LinkedHashMap());
        ArrayList<LinkedHashMap> events = getObjectOr(management, "events", new ArrayList<LinkedHashMap>());
        for (int i = 0; i < events.size(); i++) {
            if (getValueOr(events.get(i), "event", "").equals("planting")) {
                return getValueOr(events.get(i), "date", "");
            }
        }

        return "";
    }
    
    /**
     * Get the weather file name for auto-generating (extend name not included)
     * 
     * @param wthFile   weather data holder
     * @return 
     */
    protected String getWthFileName(LinkedHashMap wthFile) {
        ArrayList wthRecords = (ArrayList) getObjectOr(wthFile, "dailyWeather", new ArrayList());
        String ret = getObjectOr(wthFile, "wst_id", "").toString();
            if (ret.equals("")) {
                ret = "AGMP";
            } else {
                if (!wthRecords.isEmpty()) {
                    // Get the year of starting date and end date
                    String startYear = getValueOr(((LinkedHashMap) wthRecords.get(0)), "w_date", "    ").substring(2, 4).trim();
                    String endYear = getValueOr(((LinkedHashMap) wthRecords.get(wthRecords.size() - 1)), "w_date", "    ").substring(2, 4).trim();
                    // If not available, do not show year and duration in the file name
                    if (!startYear.equals("") && !endYear.equals("")) {
                        ret += startYear;
                        try {
                            int duration = Integer.parseInt(endYear) - Integer.parseInt(startYear) + 1;
                            // P.S. Currently the system only support the maximum of 99 years for duration
                            duration = duration > 99 ? 99 : duration;
                            ret += String.format("%02d", duration);
                        } catch (Exception e) {
                            ret += "01";    // Default duration uses 01 (minimum value)
                        }
                    }
                }
            }
        
        return ret;
    }
}
