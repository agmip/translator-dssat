package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import static org.agmip.util.MapUtil.*;

/**
 * DSSAT TFile Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatTFileInput extends DssatCommonInput {

    public String obvFileKey = "time_series";  // TODO the key name might change
    public String obvDataKey = "data";  // TODO the key name might change

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
    protected LinkedHashMap readFile(HashMap brMap) throws IOException {

        LinkedHashMap ret = new LinkedHashMap();
        ArrayList<LinkedHashMap> retArr = new ArrayList<LinkedHashMap>();
        LinkedHashMap file = readFileWithoutCompress(brMap);
//        compressData(file);
        ArrayList<LinkedHashMap> obvData = (ArrayList) file.get(obvDataKey);
        LinkedHashMap obv;
        LinkedHashMap expData;

        for (int i = 0; i < obvData.size(); i++) {
            expData = new LinkedHashMap();
            obv = new LinkedHashMap();
            expData.put(jsonKey, obv);
            copyItem(expData, file, "exname");
            copyItem(expData, file, "local_name");
            obv.put(obvFileKey, obvData.get(i));
            
            retArr.add(obv);
        }
        ret.put("data", retArr);

        // remove index variables
        ArrayList idNames = new ArrayList();
        idNames.add("trno_t");
        removeIndex(retArr, idNames);
        
        return ret;
    }

    /**
     * DSSAT TFile Data input method for Controller using (return map will not be compressed)
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    protected LinkedHashMap readFileWithoutCompress(HashMap brMap) throws IOException {

        LinkedHashMap file = new LinkedHashMap();
        String line;
        BufferedReader brT;
        Object buf;
        LinkedHashMap formats = new LinkedHashMap();
        ArrayList titles = new ArrayList();
        ArrayList<LinkedHashMap> obvData = new ArrayList();
        ArrayList obvDataSecByTrno = new ArrayList();
        LinkedHashMap obvDataByTrno;
        DssatObservedData obvDataList = new DssatObservedData();    // Varibale list definition
        String pdate;
        String trno = "0";

        buf = brMap.get("T");

        // If AFile File is no been found
        if (buf == null) {
            // TODO reprot file not exist error
            return file;
        } else {
            if (buf instanceof char[]) {
                brT = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brT = (BufferedReader) buf;
            }
        }

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
                    file.putAll(readLine(line, formats));

                } // Read data info 
                else {
                    // Set variables' formats
                    formats.clear();
                    for (int i = 0; i < titles.size(); i++) {
                        formats.put(titles.get(i), 6);
                    }

                    // Read line and save into return holder
                    LinkedHashMap tmp = readLine(line, formats);    // P.S. if missing data in TFile, no longer hold title name since the combination handling
                    // translate date from yyddd format to yyyymmdd format
                    tmp.put("date", translateDateStr((String) tmp.get("date")));
                    pdate = getPdate(brMap, (String) tmp.get("trno_t"));
                    for (int i = 0; i < titles.size(); i++) {
                        if (obvDataList.isDateType(titles.get(i))) {
                            translateDateStrForDOY(tmp, (String) titles.get(i), pdate);
                        }
                    }

                    // Check if the record's trno becomes the next treatment's trno
                    if (!trno.equals(tmp.get("trno_t"))) {
                        trno = getValueOr(tmp, "trno_t", "");
                        obvDataByTrno = null;

                        // Try to get the reccord which matches with the given treatment number
                        for (int i = 0; i < obvData.size(); i++) {
                            if (trno.equals(obvData.get(i).get("trno_t"))) {
                                obvDataByTrno = obvData.get(i);
                                obvDataSecByTrno = (ArrayList) obvDataByTrno.get(obvDataKey);
                                break;
                            }
                        }

                        // If not found, create a new record and add into array
                        if (obvDataByTrno == null) {
                            obvDataByTrno = new LinkedHashMap();
                            obvDataSecByTrno = new ArrayList();
                            obvDataByTrno.put("trno_t", trno);
                            obvDataByTrno.put(obvDataKey, obvDataSecByTrno);
                            obvData.add(obvDataByTrno);
                        }
                    }

                    // Add data to the array
                    String[] keys = {"trno_t", "date"};
                    addToArray(obvDataSecByTrno, tmp, keys);
                }

            } // Read Observed title
            else if (flg[2].equals("title")) {

                titles = new ArrayList();
                trno = "0";
                line = line.replaceFirst("@", " ");
                for (int i = 0; i < line.length(); i += 6) {
                    String titleStr = line.substring(i, Math.min(i + 6, line.length())).trim().toLowerCase();
                    if (titleStr.equals("")) {
                        titles.add("null" + i);
                    } else if (titleStr.equals("trno")) {
                        titles.add(titleStr + "_t");
                    } else {
                        titles.add(titleStr);
                    }
                }

            } else {
            }
        }

        file.put(obvDataKey, obvData);
        brT.close();

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
