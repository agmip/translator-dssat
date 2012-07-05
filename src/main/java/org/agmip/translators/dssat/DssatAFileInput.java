package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.agmip.core.types.AdvancedHashMap;

/**
 * DSSAT AFile Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatAFileInput extends DssatCommonInput {

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
    protected AdvancedHashMap readFile(HashMap brMap) throws IOException {

        AdvancedHashMap ret = new AdvancedHashMap();
        AdvancedHashMap file = new AdvancedHashMap();
        String line;
        BufferedReader brA;
        Object buf;
        LinkedHashMap formats = new LinkedHashMap();
        ArrayList titles = new ArrayList();
        ArrayList obvData = new ArrayList();
        DssatObservedData obvDataList = new DssatObservedData();    // Varibale list definition
        String obvDataKey = "data";     // P.S. the key name might change
        String obvFileKey = "summary";  // P.S. the key name might change
        String pdate;

        buf = brMap.get("A");

        // If AFile File is no been found
        if (buf == null) {
            // TODO reprot file not exist error
            return ret;
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
                    file.put(readLine(line, formats, ""));

                } // Read data info 
                else {
                    // Set variables' formats
                    formats.clear();
                    for (int i = 0; i < titles.size(); i++) {
                        formats.put(titles.get(i), 6);
                    }
                    // Read line and save into return holder
                    AdvancedHashMap tmp = readLine(line, formats, "");
                    pdate = getPdate(brMap, (String) tmp.get("trno"));
                    for (int i = 0; i < titles.size(); i++) {
                        if (obvDataList.isDateType(titles.get(i))) {
                            translateDateStrForDOY(tmp, (String) titles.get(i), pdate);
                        }
                    }
                    addToArray(obvData, tmp, "trno");
                }

            } // Read Observed title
            else if (flg[2].equals("title")) {

                titles = new ArrayList();
                line = line.replaceFirst("@", " ");
                for (int i = 0; i < line.length(); i += 6) {
                    String titleStr = line.substring(i, Math.min(i + 6, line.length())).trim().toLowerCase();
                    if (titleStr.equals("")) {
                        titles.add("null" + i);
                    } else {
                        titles.add(titleStr);
                    }
                }

            } else {
            }
        }

        ret.put(obvFileKey, file);
        file.put(obvDataKey, obvData);
        brA.close();
        compressData(ret);

        return ret;
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
