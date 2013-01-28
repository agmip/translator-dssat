package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.agmip.core.types.TranslatorInput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT Experiment Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public abstract class DssatCommonInput implements TranslatorInput {

    private static final Logger log = LoggerFactory.getLogger(DssatCommonInput.class);
    protected String[] flg = {"", "", ""};
    protected int flg4 = 0;
    protected String defValR = "-99.0";
    protected String defValC = "";
    protected String defValI = "-99";
    protected String defValD = "20110101";
    protected String jsonKey = "unknown";

    /**
     * DSSAT Data Output method for Controller using
     *
     * @param m The holder for BufferReader objects for all files
     * @return result data holder object
     */
    protected abstract HashMap readFile(HashMap m) throws IOException;

    /**
     * DSSAT XFile Data input method, always return the first data object
     *
     * @param arg0 file name
     * @return result data holder object
     */
    @Override
    public HashMap readFile(String arg0) {

        HashMap ret = new HashMap();
        String filePath = arg0;

        try {
            // read file by file
            ret = readFile(getBufferReader(filePath));

        } catch (FileNotFoundException fe) {
            log.warn("File not found under following path : [" + filePath + "]!");
            return ret;
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return ret;
//        return readFileById(arg0, 0);
    }

    /**
     * Set reading flgs for reading lines
     *
     * @param line the string of reading line
     */
    protected void judgeContentType(String line) {
        // Section Title line
        if (line.startsWith("*")) {

            setTitleFlgs(line);
            flg4 = 0;

        } // Data title line
        else if (line.startsWith("@")) {

            flg[1] = line.substring(1).trim().toLowerCase();
            flg[2] = "title";
            flg4++;

        } // Comment line
        else if (line.startsWith("!")) {

            flg[2] = "comment";

        } // Data line
        else if (!line.trim().equals("")) {

            flg[2] = "data";

        } // Continued blank line
        else if (flg[2].equals("blank")) {

            flg[0] = "";
            flg[1] = "";
            flg[2] = "blank";
            flg4 = 0;

        } else {

//            flg[0] = "";
            flg[1] = "";
            flg[2] = "blank";
        }
    }

    /**
     * Set reading flgs for title lines (the line marked with *)
     *
     * @param line the string of reading line
     */
    protected abstract void setTitleFlgs(String line);

    /**
     * Take the data str from input map and translate it from "yyddd" to
     * "yyyymmdd"
     *
     * @param m input map which might contain date value in it
     * @param id date string with format of "yyddd"
     */
    protected void translateDateStr(HashMap m, String id) {

        if (m.get(id) != null) {
            m.put(id, translateDateStr((String) m.get(id)));
        }
    }

    /**
     * Translate data str from "yyddd" to "yyyymmdd"
     *
     * @param str date string with format of "yyddd"
     * @return result date string with format of "yyyymmdd"
     */
    protected String translateDateStr(String str) {

        return translateDateStr(str, "0");
    }

    /**
     * Translate data str from "yyddd" or "doy" to "yyyymmdd"
     *
     * @param m the experiment data holder
     * @param id the key name of date field in the map
     * @param pdate the related planting date
     */
    protected void translateDateStrForDOY(HashMap m, String id, String pdate) {

        if (m.get(id) != null) {
            m.put(id, translateDateStrForDOY((String) m.get(id), pdate));
        }
    }

    /**
     * Translate data str from "yyddd" or "doy" to "yyyymmdd"
     *
     * @param str date string with format of "yyddd"
     * @param pdate the related planting date
     * @return result date string with format of "yyyymmdd"
     */
    protected String translateDateStrForDOY(String str, String pdate) {

        if (str != null && str.length() <= 3) {
            if (!pdate.equals("") && pdate.length() >= 2) {
                try {
                    str = String.format("%1$2s%2$03d", pdate.substring(0, 2), Integer.parseInt(str));
                } catch (NumberFormatException e) {
                    return "";
                }
            }
        }

        return translateDateStr(str, "0");
    }

    /**
     * Translate data str from "yyddd" to "yyyymmdd" plus days you want
     *
     * @param startDate date string with format of "yyydd"
     * @param strDays the number of days need to be added on
     * @return result date string with format of "yyyymmdd"
     */
    protected String translateDateStr(String startDate, String strDays) {

        // Initial Calendar object
        Calendar cal = Calendar.getInstance();
        int days;
        int year;
        if (startDate == null || startDate.length() > 5 || startDate.length() < 4) {
            //throw new Exception("");
            return ""; //defValD; // P.S. use blank string instead of -99
        }
        try {
            startDate = String.format("%05d", Integer.parseInt(startDate));
            days = Double.valueOf(strDays).intValue();
            // Set date with input value
            year = Integer.parseInt(startDate.substring(0, 2));
            year += year <= 15 ? 2000 : 1900; // P.S. 2015 is the cross year for the current version 
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(startDate.substring(2)));
            cal.add(Calendar.DATE, days);
            // translatet to yyddd format
            return String.format("%1$04d%2$02d%3$02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
            // if tranlate failed, then use default value for date
            // sbError.append("! Waring: There is a invalid date [").append(startDate).append("]");
            return ""; //formatDateStr(defValD); // P.S. use blank string instead of -99
        }

    }

    /**
     * Divide the data in the line into a map (Default invalid value is null,
     * which means not to be sore in the json)
     *
     * @param line The string of line read from data file
     * @param formats The definition of length for each data field (String
     * itemName : Integer length)
     * @return the map contains divided data with keys from original string
     */
    protected HashMap readLine(String line, LinkedHashMap<String, Integer> formats) {

        return readLine(line, formats, null);
    }

    /**
     * Divide the data in the line into a map
     *
     * @param line The string of line read from data file
     * @param formats The definition of length for each data field (String
     * itemName : Integer length)
     * @param invalidValue The text will replace the original reading when its
     * value is invalid
     * @return the map contains divided data with keys from original string
     */
    protected HashMap readLine(String line, LinkedHashMap<String, Integer> formats, String invalidValue) {

        HashMap ret = new HashMap();
        int length;
        String tmp;

        for (String key : formats.keySet()) {
            // To avoid to be over limit of string lenght
            length = Math.min((Integer) formats.get(key), line.length());
            if (!((String) key).equals("") && !((String) key).startsWith("null")) {
                tmp = line.substring(0, length).trim();
                // if the value is in valid keep blank string in it
                if (checkValidValue(tmp)) {
                    ret.put(key, tmp);
                } else {
                    if (invalidValue != null) {
                        ret.put(key, invalidValue);   // P.S. "" means missing or invalid value
                    }
                }
            }
            line = line.substring(length);
        }

        return ret;
    }

    /**
     * Check if input is a valid value
     *
     * @return check result
     */
    protected boolean checkValidValue(String value) {
        if (value == null || value.trim().equals(defValC) || value.equals(defValI) || value.equals(defValR)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get BufferReader for each type of file
     *
     * @param filePath the full path of the input file
     * @return result the holder of BufferReader for different type of files
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected static HashMap getBufferReader(String filePath) throws FileNotFoundException, IOException {

        HashMap result = new HashMap();
        InputStream in;
        HashMap mapW = new HashMap();
        HashMap mapS = new HashMap();
        HashMap mapC = new HashMap();
        HashMap mapX = new HashMap();
        HashMap mapA = new HashMap();
        HashMap mapT = new HashMap();
        String[] tmp = filePath.split("[\\/]");

        // If input File is ZIP file
        if (filePath.toUpperCase().endsWith(".ZIP")) {

            // Get experiment name
            ZipEntry entry;
            ArrayList<String> exnames = new ArrayList();
            String exname = "";
            in = new ZipInputStream(new FileInputStream(filePath));
            while ((entry = ((ZipInputStream) in).getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    if (entry.getName().matches(".+\\.\\w{2}[Xx]")) {
                        exname = entry.getName().replaceAll("[Xx]$", "");
                        exnames.add(exname);
//                        break;
                    }
                }
            }

            // Read Files
            in = new ZipInputStream(new FileInputStream(filePath));

            while ((entry = ((ZipInputStream) in).getNextEntry()) != null) {
                if (!entry.isDirectory()) {

                    if (exnames.contains(entry.getName().replaceAll("[Xx]$", ""))) {
//                        result.put("X", getBuf(in, (int) entry.getSize()));
                        mapX.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (entry.getName().toUpperCase().endsWith(".WTH")) {
                        mapW.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (entry.getName().toUpperCase().endsWith(".SOL")) {
                        mapS.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (exnames.contains(entry.getName().replaceAll("[Aa]$", ""))) {
//                        result.put("A", getBuf(in, (int) entry.getSize()));
                        mapA.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (exnames.contains(entry.getName().replaceAll("[Tt]$", ""))) {
//                        result.put("T", getBuf(in, (int) entry.getSize()));
                        mapT.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (entry.getName().toUpperCase().endsWith(".OUT")) {
                        result.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (entry.getName().toUpperCase().endsWith(".CUL")) {
                        mapC.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    } else if (entry.getName().toUpperCase().endsWith(".JSON")) {
                        result.put(entry.getName().toUpperCase(), getBuf(in, entry));
                    }
                }
            }
        } // If input File is not ZIP file
        else {
            in = new FileInputStream(filePath);
            File f = new File(filePath);
            if (filePath.matches(".+\\.\\w{2}[Xx]")) {
                mapX.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
//                result.put("X", new BufferedReader(new InputStreamReader(in)));
            } else if (filePath.toUpperCase().endsWith(".WTH")) {
                mapW.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            } else if (filePath.toUpperCase().endsWith(".SOL")) {
                mapS.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            } else if (filePath.matches(".+\\.\\w{2}[Aa]")) {
                mapA.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
//                result.put("A", new BufferedReader(new InputStreamReader(in)));
            } else if (filePath.matches(".+\\.\\w{2}[Tt]")) {
                mapT.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
//                result.put("T", new BufferedReader(new InputStreamReader(in)));
            } else if (f.getName().toUpperCase().endsWith(".OUT")) {
                result.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            } else if (f.getName().toUpperCase().endsWith(".CUL")) {
                mapC.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            } else if (filePath.toUpperCase().endsWith(".JSON")) {
                result.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            }
        }

        result.put("W", mapW);
        result.put("S", mapS);
        result.put("X", mapX);
        result.put("A", mapA);
        result.put("T", mapT);
        result.put("C", mapC);
        result.put("Z", tmp[tmp.length - 1]);

        return result;
    }

    /**
     * Get BufferReader object from Zip entry
     *
     * @param in The input stream of zip file
     * @param entry The current entry of zip input stream
     * @return result The char array for current entry
     * @throws IOException
     */
    private static char[] getBuf(InputStream in, ZipEntry entry) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        char[] buf;
        long size = entry.getSize();;

        if (size > 0 && size <= Integer.MAX_VALUE) {
            buf = new char[(int) size];
            br.read(buf);
        } else {
            char[] b = new char[1024];
            CharArrayWriter cw = new CharArrayWriter();
            int chunk;
            while ((chunk = br.read(b)) > 0) {
                cw.write(b, 0, chunk);
            }
            buf = cw.toCharArray();
        }

        return buf;
    }

    /**
     * compress the data in a map object
     *
     * @param m input map
     */
    protected void compressData(HashMap m) {

        Object[] keys = m.keySet().toArray();
        Object key;
        for (int i = 0; i < keys.length; i++) {
            key = keys[i];

            if (m.get(key) instanceof ArrayList) {
                if (((ArrayList) m.get(key)).isEmpty()) {
                    // Delete the empty list
                    m.remove(key);
                } else {
                    // iterate sub array nodes
                    compressData((ArrayList) m.get(key));
                }
            } else if (m.get(key) instanceof HashMap) {
                if (((HashMap) m.get(key)).isEmpty()) {
                    // Delete the empty list
                    m.remove(key);
                } else {
                    // iterate sub data nodes
                    compressData((HashMap) m.get(key));
                }

            } else {
                // ignore other type nodes
            }
        }

    }

    /**
     * compress the data in an ArrayList object
     *
     * @param arr input ArrayList
     *
     */
    protected void compressData(ArrayList arr) {

        HashMap fstData = null; // The first data record (Map type)
        HashMap cprData = null; // The following data record which will be compressed

        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i) instanceof ArrayList) {
                // iterate sub array nodes
                compressData((ArrayList) arr.get(i));

            } else if (arr.get(i) instanceof HashMap) {
                // iterate sub data nodes
                compressData((HashMap) arr.get(i));

                // Compress data for current array
                if (fstData == null) {
                    // Get first data node
                    fstData = (HashMap) arr.get(i);
                } else {
                    cprData = (HashMap) arr.get(i);
                    Object[] keys = cprData.keySet().toArray();
                    // The missing data will be given a "" value; Only data item (String type) will be processed
                    for (Object key : fstData.keySet()) {
                        if (!cprData.containsKey(key)) {
                            if (fstData.get(key) instanceof String) {
                                cprData.put(key, "");
                            } else if (fstData.get(key) instanceof HashMap) {
                                cprData.put(key, new HashMap());
                            } else if (fstData.get(key) instanceof ArrayList) {
                                cprData.put(key, new ArrayList());
                            }
                        }

                    }
                    // The repeated data will be deleted; Only data item (String type) will be processed
                    for (Object key : keys) {
                        if (cprData.get(key).equals(fstData.get(key))) {
                            cprData.remove(key);
                        }
                    }
                }
            } else {
            }
        }
    }

    /**
     * Get a copy of input map
     *
     * @param m input map
     * @return the copy of whole input map
     */
    protected static HashMap CopyList(HashMap m) {
        HashMap ret = new HashMap();

        for (Object key : m.keySet()) {
            if (m.get(key) instanceof String) {
                ret.put(key, m.get(key));
            } else if (m.get(key) instanceof HashMap) {
                ret.put(key, CopyList((HashMap) m.get(key)));
            } else if (m.get(key) instanceof ArrayList) {
                ret.put(key, CopyList((ArrayList) m.get(key)));
            }
        }

        return ret;
    }

    /**
     * Get a copy of input array
     *
     * @param arr input ArrayList
     * @return the copy of whole input array
     */
    protected static ArrayList CopyList(ArrayList arr) {
        ArrayList ret = new ArrayList();
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i) instanceof String) {
                ret.add(arr.get(i));
            } else if (arr.get(i) instanceof HashMap) {
                ret.add(CopyList((HashMap) arr.get(i)));
            } else if (arr.get(i) instanceof ArrayList) {
                ret.add(CopyList((ArrayList) arr.get(i)));
            }
        }

        return ret;
    }

    /**
     * Add the new item into array by having same key value
     *
     * @param arr the target array
     * @param item the input item which will be added into array
     * @param key the primary key item's name
     */
    protected void addToArray(ArrayList arr, HashMap item, Object key) {
        HashMap elem;
        boolean unmatchFlg = true;

        // Added logging (cv)
        log.debug("Array: {}", arr.toString());
        log.debug("Item: {}", item.toString());
        log.debug("Key: {}", key);


        for (int i = 0; i < arr.size(); i++) {
            elem = (HashMap) arr.get(i);
            if (!key.getClass().isArray()) {
                if (elem.get(key).equals(item.get(key))) {
                    elem.putAll(item);
                    unmatchFlg = false;
                    break;
                }
            } else {
                Object[] keys = (Object[]) key;
                boolean equalFlg = true;
                for (int j = 0; j < keys.length; j++) {
                    if (!elem.get(keys[j]).equals(item.get(keys[j]))) {
                        equalFlg = false;
                        break;
                    }
                }
                if (equalFlg) {
                    elem.putAll(item);
                    unmatchFlg = false;
                    break;
                }
            }
        }
        if (unmatchFlg) {
            arr.add(item);
        }
    }

    /**
     * Get planting date value from XFile with related treatment number
     *
     * @param m the files content holder
     */
    protected String getPdate(HashMap m, String trno, String fileName) {

        BufferedReader br;
        Object buf = null;
        String line;
        LinkedHashMap formats = new LinkedHashMap();
        String pl = null;
        String[] flgP = new String[3];
        DssatXFileInput xfile = new DssatXFileInput();

//        buf = (char[]) m.get("X");
        HashMap mapX = (HashMap) m.get("X");

        buf = mapX.get(fileName);
        if (buf == null) {
            return "";
        } else if (buf instanceof char[]) {
            br = new BufferedReader(new CharArrayReader((char[]) buf));
        } else {
            br = (BufferedReader) buf;
        }

//        if (buf == null) {
//            return "";
//        } else {
//            br = new BufferedReader(new CharArrayReader(buf));
//        }

        try {
            while ((line = br.readLine()) != null) {

                // Get content type of line
                xfile.judgeContentType(line);
                flgP = xfile.flg;

                // Read TREATMENTS Section
                if (flgP[0].startsWith("treatments")) {

                    if (pl != null) {
                        continue;
                    }

                    // Read TREATMENTS data
                    if (flgP[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("trno", 2);
                        formats.put("", 44);
                        formats.put("pl", 3);
                        // Read line and get related planting info number
                        HashMap tmp = readLine(line, formats);
                        if (tmp.get("trno").equals(trno)) {
                            pl = (String) tmp.get("pl");
                        }
                    } else {
                    }


                } // Read CULTIVARS Section
                else if (flgP[0].startsWith("planting")) {

                    if (pl == null) {
                        return "";
                    }

                    // Read PLANTING data
                    if (flgP[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("pl", 2);
                        formats.put("pdate", 6);
                        // Read line and save into defValD
                        HashMap tmp = readLine(line, formats);
                        if (tmp.get("pl").equals(pl)) {
                            return (String) tmp.get("pdate");
                        }

                    } else {
                    }

                } else {
                }
            }

            br.close();
        } catch (IOException ex) {
            //            Logger.getLogger(DssatCommonInput.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

        return "";
    }

    public static void setDataVersionInfo(HashMap m) {
        m.put("data_source", "DSSAT");
        m.put("crop_model_version", "v4.5");
    }

    /**
     * Get the section data by given index value and key
     *
     * @param secArr Section data array
     * @param key index variable name
     * @param value index variable value
     */
    public static HashMap getSectionData(ArrayList secArr, Object key, String value) {

        HashMap ret = null;
        // Get First data node
        if (secArr.isEmpty() || value == null) {
            return ret;
        }
        for (int i = 0; i < secArr.size(); i++) {
            if (value.equals(((HashMap) secArr.get(i)).get(key))) {
                return DssatCommonInput.CopyList((HashMap) secArr.get(i));
            }
        }

        return ret;
    }

    /**
     * Get the section data by given index value and key, without getting a copy
     * of original data
     *
     * @param secArr Section data array
     * @param key index variable name
     * @param value index variable value
     */
    public static HashMap getSectionDataWithNocopy(ArrayList secArr, Object key, String value) {

        HashMap ret = null;
        // Get First data node
        if (secArr.isEmpty() || value == null) {
            return ret;
        }
        for (int i = 0; i < secArr.size(); i++) {
            if (value.equals(((HashMap) secArr.get(i)).get(key))) {
                return (HashMap) secArr.get(i);
            }
        }

        return ret;
    }

    /**
     * Combine two layer data array into a new array by matching the pointed id,
     * and only combine the pointed variables
     *
     * @param toArr the array which the data will be combined to
     * @param fromArr the array which the data will be combined from
     * @param toKey the index variable name of to array
     * @param fromKey the index variable name of from array
     * @param copyKeys the array of variable names which will be handled
     *
     * @return the combined new array
     */
    public static ArrayList<HashMap> combinLayers(ArrayList<HashMap> toArr, ArrayList<HashMap> fromArr, String toKey, String fromKey, String[] copyKeys) {

        ArrayList<HashMap> ret = new ArrayList<HashMap>();
        ArrayList<String> fromKeyArr = getLayerNumArray(fromArr, fromKey);
        ArrayList<String> toKeyArr = getLayerNumArray(toArr, toKey);

        int cnt = 0;
        for (int j = 0; j < fromKeyArr.size(); j++) {
            HashMap toTmp = new HashMap();
            HashMap fromTmp = (HashMap) fromArr.get(j);
            String fromKeyVal = fromKeyArr.get(j);
            String toKeyVal = "";

            for (int k = cnt; k < toKeyArr.size(); k++, cnt++) {
                toKeyVal = toKeyArr.get(k);
                if (Double.parseDouble(toKeyVal) == Double.parseDouble(fromKeyVal)) {
                    toTmp = (HashMap) getSectionData(toArr, toKey, toKeyVal);
                    ret.add(toTmp);
                    copyItems(toTmp, fromTmp, copyKeys);
                    break;
                } else if (Double.parseDouble(toKeyVal) > Double.parseDouble(fromKeyVal)) {
                    toTmp = CopyList((HashMap) getSectionData(toArr, toKey, toKeyVal));
                    toTmp.put(toKey, fromKeyVal);
                    ret.add(toTmp);
                    copyItems(toTmp, fromTmp, copyKeys);
                    break;
                } else if (!fromKeyArr.contains(toKeyVal)) {
                    toTmp = (HashMap) getSectionData(toArr, toKey, toKeyVal);
                    ret.add(toTmp);
                    copyItems(toTmp, fromTmp, copyKeys);
                }
            }

            if (toKeyArr.isEmpty()) {
                ret.add(toTmp);
                copyItems(toTmp, fromTmp, copyKeys);
            } else if (toKeyArr.size() <= cnt) {
                toTmp = CopyList(getSectionData(toArr, toKey, toKeyArr.get(toKeyArr.size() - 1)));
                toTmp.put(toKey, fromKeyVal);
                ret.add(toTmp);
                copyItems(toTmp, fromTmp, copyKeys);
            }
        }

        for (int j = cnt; j < toKeyArr.size(); j++) {
            ret.add((HashMap) getSectionData(toArr, toKey, toKeyArr.get(j)));
        }

        // Auto-fill the copy data in the missing layer with nearby layer data
        String copyItem;
        for (int i = 0; i < copyKeys.length; i++) {
            copyItem = null;
            for (int j = ret.size() - 1; j > 0; j--) {
                if (ret.get(j).containsKey(copyKeys[i])) {
                    copyItem = (String) ret.get(j).get(copyKeys[i]);
                } else {
                    if (copyItem == null) {
                        for (int k = ret.size() - 1; k > 0; k--) {
                            if (ret.get(k).get(copyKeys[i]) != null) {
                                copyItem = (String) ret.get(k).get(copyKeys[i]);
                                break;
                            }
                        }
                    }
                    if (copyItem != null) {
                        ret.get(j).put(copyKeys[i], copyItem);
                    }
                }
            }
        }


        return ret;
    }

    /**
     * Get the array of key variables from original array
     *
     * @param arr the input array
     * @param key the key name
     *
     * @return the array of key variable value
     */
    private static ArrayList<String> getLayerNumArray(ArrayList<HashMap> arr, String key) {

        ArrayList<String> ret = new ArrayList();
        for (int i = 0; i < arr.size(); i++) {
            ret.add((String) (arr.get(i).get(key)));
        }
        return ret;
    }

    /**
     * copy items from one map to another map
     *
     * @param to the map which data will be copied to
     * @param from the map which data will be copied from
     * @param copyKeys the array of key name which will be copied
     *
     */
    private static void copyItems(HashMap to, HashMap from, String[] copyKeys) {
        for (int i = 0; i < copyKeys.length; i++) {
            copyItem(to, from, copyKeys[i]);
        }
    }

    /**
     * remove all the relation index for the input array
     *
     * @param arr The array of treatment data
     * @param idNames The array of id want be removed
     */
    public static void removeIndex(ArrayList arr, ArrayList idNames) {

        for (int i = 0; i < arr.size(); i++) {
            Object item = arr.get(i);
            if (item instanceof ArrayList) {
                removeIndex((ArrayList) item, idNames);
            } else if (item instanceof HashMap) {
                removeIndex((HashMap) item, idNames);
            }

        }

    }

    /**
     * remove all the relation index for the input map
     *
     * @param m the array of treatment data
     */
    public static void removeIndex(HashMap m, ArrayList idNames) {

        Object[] keys = m.keySet().toArray();
        for (Object key : keys) {
            Object item = m.get(key);
            if (item instanceof ArrayList) {
                removeIndex((ArrayList) item, idNames);
            } else if (item instanceof HashMap) {
                removeIndex((HashMap) item, idNames);
            } else if (item instanceof String && idNames.contains(key)) {
                m.remove(key);
            }
        }
    }

    /**
     * copy item from one map to another map
     *
     * @param to the map which data will be copied to
     * @param from the map which data will be copied from
     * @param key the key name which will be copied
     *
     */
    public static void copyItem(HashMap to, HashMap from, String key) {
        copyItem(to, from, key, key, false);
    }

    /**
     * copy item from one map to another map original data might be delete based
     * on last boolean value
     *
     * @param to the map which data will be copied to
     * @param from the map which data will be copied from
     * @param key the key name which will be copied
     * @param deleteFlg decide if delete the original data(true for delete)
     *
     */
    public static void copyItem(HashMap to, HashMap from, String key, boolean deleteFlg) {
        copyItem(to, from, key, key, deleteFlg);
    }

    /**
     * copy item from one map to another map by using different key original
     * data might be delete based on last boolean value
     *
     * @param to the map which data will be copied to
     * @param from the map which data will be copied from
     * @param toKey the key name used in target holder
     * @param fromKey the key name used in original holder
     * @param deleteFlg decide if delete the original data(true for delete)
     *
     */
    public static void copyItem(HashMap to, HashMap from, String toKey, String fromKey, boolean deleteFlg) {
        if (from.get(fromKey) != null) {
            if (deleteFlg && from.get(fromKey) != null) {
                to.put(toKey, from.remove(fromKey));
            } else {
                to.put(toKey, from.get(fromKey));
            }
        }
    }
}
