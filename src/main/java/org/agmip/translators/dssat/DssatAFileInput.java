package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * DSSAT AFile Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatAFileInput extends DssatCommonInput {

    public String obvFileKey = "summary";  // P.S. the key name might change
    public String obvDataKey = "data";     // P.S. the key name might change

    /**
     * Constructor with no parameters
     * Set jsonKey as "observed"
     * 
     */
    public DssatAFileInput() {
        super();
        jsonKey = "observed";
    }

    /**
     * DSSAT AFile Data input method for Controller using
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    @Override
    protected ArrayList<LinkedHashMap> readFile(HashMap brMap) throws IOException {

        ArrayList<LinkedHashMap> ret = new ArrayList<LinkedHashMap>();
        LinkedHashMap file = readObvData(brMap);
//        compressData(file);
        ArrayList<LinkedHashMap> obvData = (ArrayList) file.get(obvDataKey);
        LinkedHashMap obv;
        LinkedHashMap expData;

        for (int i = 0; i < obvData.size(); i++) {
            expData = new LinkedHashMap();
            obv = new LinkedHashMap();
            copyItem(expData, file, "exname");
            copyItem(expData, file, "local_name");
            expData.put(jsonKey, obv);
            obv.put(obvFileKey, obvData.get(i));

            ret.add(expData);
        }

        // remove index variables
        ArrayList idNames = new ArrayList();
        idNames.add("trno_a");
        removeIndex(ret, idNames);

        return ret;
    }

    /**
     * DSSAT AFile Data input method for Controller using (return map will not be compressed)
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    protected LinkedHashMap readObvData(HashMap brMap) throws IOException {

        LinkedHashMap file = new LinkedHashMap();
        String line;
        BufferedReader brA;
        Object buf;
        LinkedHashMap formats = new LinkedHashMap();
        ArrayList titles = new ArrayList();
        ArrayList obvData = new ArrayList();
        DssatObservedData obvDataList = new DssatObservedData();    // Varibale list definition
        String pdate;

        buf = brMap.get("A");

        // If AFile File is no been found
        if (buf == null) {
            return file;
        } else {
            if (buf instanceof char[]) {
                brA = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brA = (BufferedReader) buf;
            }
        }

//        ret.put(obvFileKey, file);
//        file.put(obvDataKey, obvData);
        while ((line = brA.readLine()) != null) {

            // Get content type of line
            judgeContentType(line);

            // Read Observed data
            if (flg[2].equals("data")) {

                // Read meta info
                if (flg[0].equals("meta") && flg[1].equals("")) {

                    // Set variables' formats
                    line = line.replaceAll(".*:", "").trim();
                    formats.clear();
                    formats.put("exname", 10);
                    formats.put("local_name", line.length());
                    // Read line and save into return holder
                    file.putAll(readLine(line, formats));

                } // Read data info 
                else {
                    // Set variables' formats
                    formats.clear();
                    for (int i = 0; i < titles.size(); i++) {
                        formats.put(titles.get(i), 6);
                    }
                    // Read line and save into return holder
                    LinkedHashMap tmp = readLine(line, formats, "");
                    pdate = getPdate(brMap, (String) tmp.get("trno_a"));
                    for (int i = 0; i < titles.size(); i++) {
                        if (obvDataList.isDateType(titles.get(i))) {
                            translateDateStrForDOY(tmp, (String) titles.get(i), pdate);
                        }
                    }
                    addToArray(obvData, tmp, "trno_a");
                }

            } // Read Observed title
            else if (flg[2].equals("title")) {

                titles = new ArrayList();
                line = line.replaceFirst("@", " ");
                for (int i = 0; i < line.length(); i += 6) {
                    String titleStr = line.substring(i, Math.min(i + 6, line.length())).trim().toLowerCase();
                    if (titleStr.equals("")) {
                        titles.add("null" + i);
                    } else if (titleStr.equals("trno")) {
                        titles.add(titleStr + "_a");
                    } else {
                        titles.add(titleStr);
                    }
                }

            } else {
            }
        }

        file.put(obvDataKey, obvData);
        brA.close();

        return file;
    }

    /**
     * Set reading flags for title lines (marked with *)
     * 
     * @param line  the string of reading line
     */
    @Override
    protected void setTitleFlgs(String line) {
        flg[0] = "meta";
        flg[1] = "";
        flg[2] = "data";
    }
}
