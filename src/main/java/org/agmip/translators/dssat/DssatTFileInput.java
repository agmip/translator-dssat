package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.agmip.core.types.AdvancedHashMap;

/**
 * DSSAT TFile Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatTFileInput extends DssatCommonInput {

    /**
     * Constructor with no parameters
     * Set jsonKey as "observed"
     * 
     */
    public DssatTFileInput() {
        super();
        jsonKey = "observed";
    }

    /**
     * DSSAT TFile Data input method for Controller using
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    @Override
    protected AdvancedHashMap readFile(HashMap brMap) throws IOException {

        AdvancedHashMap ret = new AdvancedHashMap();
        AdvancedHashMap file = new AdvancedHashMap();
        String line;
        BufferedReader brT;
        Object buf;
        LinkedHashMap formats = new LinkedHashMap();
        ArrayList titles = new ArrayList();
        ArrayList obvData = new ArrayList();
        ArrayList obvDataSection = new ArrayList();
        DssatObservedData obvDataList = new DssatObservedData();    // Varibale list definition
        String obvDataKey = "data";  // TODO the key name might change
        String obvFileKey = "time_course";  // TODO the key name might change
        String pdate;

        buf = brMap.get("T");

        // If AFile File is no been found
        if (buf == null) {
            // TODO reprot file not exist error
            return ret;
        } else {
            if (buf instanceof char[]) {
                brT = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brT = (BufferedReader) buf;
            }
        }

//        ret.put(obvFileKey, file);
//        file.put(obvDataKey, obvData);
        while ((line = brT.readLine()) != null) {

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

                    // Read line and save into return holder
                    AdvancedHashMap tmp = readLine(line, formats, "");
                    // translate date from yyddd format to yyyymmdd format
                    tmp.put("date", translateDateStr((String) tmp.get("date")));
                    pdate = getPdate(brMap, (String) tmp.get("trno"));
                    for (int i = 0; i < titles.size(); i++) {
                        if (obvDataList.isDateType(titles.get(i))) {
                            translateDateStrForDOY(tmp, (String) titles.get(i), pdate);
                        }
                    }
                    // Add data to the array
                    String[] keys = {"trno", "date"};
                    addToArray(obvDataSection, tmp, keys);
                }

            } // Read Observed title
            else if (flg[2].equals("title")) {

                titles = new ArrayList();
                obvDataSection = new ArrayList();
                obvData.add(obvDataSection);
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
        brT.close();
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
