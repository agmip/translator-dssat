package org.agmip.translators.dssat;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
    protected StringBuilder sbError;
    protected File outputFile;

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
     * @param m the experiment data holder
     * @param key the key of field in the map
     * @param defVal the default return value when error happens
     * @return formated string of number
     */
    protected String formatNumStr(int bits, HashMap m, Object key, String defVal) {

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
            sbError.append("! Waring: There is a variable [").append(key).append("] with oversized number [").append(ret).append("] (Limitation is ").append(bits).append(" bits)\r\n");
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
     * Format the output string with maximum length
     *
     * @param bits Maximum length of the output string
     * @param m the experiment data holder
     * @param key the key of field in the map
     * @param defVal the default return value when error happens
     * @return formated string of number
     */
    protected String formatStr(int bits, HashMap m, Object key, String defVal) {

        String ret = getObjectOr(m, key, defVal).trim();
        if (ret.equals("")) {
            return ret;
        } else if (ret.length() > bits) {
            //throw new Exception();
            sbError.append("! Waring: There is a variable [").append(key).append("] with oversized content [").append(ret).append("], only first ").append(bits).append(" bits will be applied.\r\n");
            return ret.substring(0, bits);
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
        if (ret.matches("\\w+_+\\d+")) {
            ret = ret.replaceAll("_+\\d+$", "");
        }

        // If length more than 10-bit, only remain first 10-bit
        if (ret.length() > 10) {
            ret = ret.substring(0, 10);
        }

        return ret;
    }

    /**
     * Get crop id with 2-bit format
     *
     * @param result date holder for experiment data
     * @return crop id
     */
    protected String getCrid(Map result) {

        HashMap mgnData = getObjectOr(result, "management", new HashMap());
        ArrayList<HashMap> events = getObjectOr(mgnData, "events", new ArrayList());
        String crid = null;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).get("event").equals("planting")) {
                if (crid == null) {
                    crid = (String) events.get(i).get("crid");
                } else if (!crid.equals(events.get(i).get("crid"))) {
                    crid = "SQ";
                    break;
                }
            }
        }
        DssatCRIDHelper crids = new DssatCRIDHelper();
        return crids.get2BitCrid(crid);
    }

    /**
     * Generate output file name
     *
     * @param result date holder for experiment data
     * @param fileType the last letter from file extend name
     * @return file name
     */
    protected String getFileName(Map result, String fileType) {
        String ret = getExName(result);
        String crid = getCrid(result);
        if (ret == null || ret.equals("")) {
            ret = "TEMP.XX" + fileType;
        } else {
            try {
                if (ret.endsWith(crid)) {
                    ret = ret.substring(0, ret.length() - crid.length());
                } else {
                    if (ret.length() == 10) {
                        if (crid.equals("XX")) {
                            crid = ret.substring(ret.length() - 2, ret.length());
                        }
                        ret = ret.substring(0, ret.length() - 2);
                    }
                }

                ret += "." + crid + fileType;
            } catch (Exception e) {
                ret = "TEMP.XX" + fileType;
            }
        }
        return ret;
    }

    /**
     * Revise output path
     *
     * @param path the output path
     * @return revised path
     */
    public static String revisePath(String path) {
        if (!path.trim().equals("")) {
//            path = path.replaceAll("/", File.separator);
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            File f = new File(path);
            if (f.isFile()) {
                f = f.getParentFile();
            }
            if (f != null && !f.exists()) {
                f.mkdirs();
            }
        }
        return path;
    }

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * decompress the data in a map object
     *
     * @param m input map
     */
    protected void decompressData(HashMap m) {

        for (Object key : m.keySet()) {
            if (m.get(key) instanceof ArrayList) {
                // iterate sub array nodes
                decompressData((ArrayList) m.get(key));
            } else if (m.get(key) instanceof HashMap) {
                // iterate sub data nodes
                decompressData((HashMap) m.get(key));
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

        HashMap fstData = null; // The first data record (Map type)
        HashMap cprData = null; // The following data record which will be compressed

        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i) instanceof ArrayList) {
                // iterate sub array nodes
                decompressData((ArrayList) arr.get(i));

            } else if (arr.get(i) instanceof HashMap) {
                // iterate sub data nodes
                decompressData((HashMap) arr.get(i));

                // Compress data for current array
                if (fstData == null) {
                    // Get first data node
                    fstData = (HashMap) arr.get(i);
                } else {
                    cprData = (HashMap) arr.get(i);
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
     * @param result the experiment data object
     * @return plating date
     */
    protected String getPdate(Map result) {

        HashMap management = getObjectOr(result, "management", new HashMap());
        ArrayList<HashMap> events = getObjectOr(management, "events", new ArrayList<HashMap>());
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
     * @param data experiment data holder or weather data holder
     * @return the weather file name
     */
    protected String getWthFileName(HashMap data) {

//        String agmipFileHack = getValueOr(wthFile, "wst_name", "");
//        if (agmipFileHack.length() == 8) {
//            return agmipFileHack;
//        }
        String ret = getObjectOr(data, "wst_id", "").toString();
        if (ret.equals("") || ret.length() > 8) {
            DssatWthFileHelper wthHelper = new DssatWthFileHelper();
            ret = wthHelper.createWthFileName(getObjectOr(data, "weather", data));
            if (ret.equals("")) {
                ret = "AGMP";
            }
        }

        return ret;
    }

    /**
     * Get the soil_id with legal length (8~10 bits), filled with "_"
     *
     * @param data experiment data holder or weather data holder
     * @return the weather file name
     */
    protected String getSoilID(HashMap data) {
        String ret = getObjectOr(data, "soil_id", "");
        ret = ret.trim();
        if (ret.equals("")) {
            return ret;
        }
        while (ret.length() < 8) {
            ret += "_";
        }
        return ret;
    }

    /**
     * Set default value for missing data
     *
     */
    protected void setDefVal() {

        // defValD = ""; No need to set default value for Date type in weather file
        defValR = "-99";
        defValC = "-99";
        defValI = "-99";
        sbError = new StringBuilder();
        outputFile = null;
    }
}
