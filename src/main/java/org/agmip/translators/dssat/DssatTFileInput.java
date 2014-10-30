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

    public String obvFileKey = "timeSeries";  // P.S. the key name might change
    public String obvDataKey = "data";  // P.S. the key name might change

    /**
     * Constructor with no parameters Set jsonKey as "observed"
     *
     */
    public DssatTFileInput() {
        super();
        jsonKey = "observed";
    }

    /**
     * DSSAT TFile Data input method for Controller using
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     * @throws java.io.IOException
     */
    @Override
    protected HashMap readFile(HashMap brMap) throws IOException {

        HashMap ret = new HashMap();
        ArrayList<HashMap> expArr = new ArrayList<HashMap>();
        HashMap<String, HashMap> files = readObvData(brMap);
//        compressData(file);
        ArrayList<HashMap> obvData;
        HashMap obv;
        HashMap expData;

        for (String exname : files.keySet()) {
            obvData = (ArrayList) files.get(exname).get(obvDataKey);
            for (HashMap obvSub : obvData) {
                expData = new HashMap();
                obv = new HashMap();
                copyItem(expData, files.get(exname), "exname");
                copyItem(expData, files.get(exname), "crid");
                copyItem(expData, files.get(exname), "local_name");
                expData.put(jsonKey, obv);
                obv.put(obvFileKey, obvSub.get(obvDataKey));
                expArr.add(expData);
            }
        }

        // remove index variables
        ArrayList idNames = new ArrayList();
        idNames.add("trno_t");
        removeIndex(expArr, idNames);
        ret.put("experiments", expArr);

        return ret;
    }

    /**
     * DSSAT TFile Data input method for Controller using (return map will not
     * be compressed)
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     * @throws java.io.IOException
     */
    protected HashMap readObvData(HashMap brMap) throws IOException {

        HashMap files = new HashMap();
        HashMap file = new HashMap();
        String line;
        BufferedReader brT;
        Object buf;
        HashMap mapT;
        LinkedHashMap formats = new LinkedHashMap();
        ArrayList titles = new ArrayList();
        ArrayList<HashMap> obvData;
        ArrayList obvDataSecByTrno = new ArrayList();
        HashMap obvDataByTrno;
        DssatObservedData obvDataList = DssatObservedData.INSTANCE;    // Varibale list definition
        String pdate;
        String trno = "0";

        mapT = (HashMap) brMap.get("T");

        // If AFile File is no been found
        if (mapT.isEmpty()) {
            return file;
        }

        for (Object keyT : mapT.keySet()) {

            buf = mapT.get(keyT);
            String fileName = (String) keyT;
//            String exname = fileName.replaceAll("\\.", "").replaceAll("T$", "");
            String exname = fileName.replaceAll("\\.\\w\\wT$", "");
            String crid = fileName.replaceAll("\\w+\\.", "").replaceAll("T$", "");
            if (buf instanceof char[]) {
                brT = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brT = (BufferedReader) buf;
            }

            file = new HashMap();
            obvData = new ArrayList();

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
                        formats.put("null", 10);  // P.S. Since exname in top line is not reliable, read from file name
                        formats.put("local_name", line.length());
                        // Read line and save into return holder
                        file.putAll(readLine(line, formats));
                        file.put("exname", exname);
                        file.put("crid", DssatCRIDHelper.get3BitCrid(crid));
                        flg[0] = "data";

                    } // Read data info 
                    else {
                        // Set variables' formats
                        formats.clear();
                        for (Object title : titles) {
                            formats.put(title, 6);
                        }

                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);    // P.S. if missing data in TFile, no longer hold title name since the combination handling
                        // translate date from yyddd format to yyyymmdd format
                        tmp.put("date", translateDateStr((String) tmp.get("date")));
                        pdate = getPdate(brMap, (String) tmp.get("trno_t"), fileName.replaceAll("T$", "X"));
                        for (Object title : titles) {
                            if (obvDataList.isDateType(title)) {
                                translateDateStrForDOY(tmp, (String) title, pdate, "");
                            }
                        }

                        // Check if the record's trno becomes the next treatment's trno
                        if (!trno.equals(tmp.get("trno_t"))) {
                            trno = getValueOr(tmp, "trno_t", "");
                            obvDataByTrno = null;

                            // Try to get the reccord which matches with the given treatment number
                            for (HashMap obvSub : obvData) {
                                if (trno.equals(obvSub.get("trno_t"))) {
                                    obvDataByTrno = obvSub;
                                    obvDataSecByTrno = (ArrayList) obvDataByTrno.get(obvDataKey);
                                    break;
                                }
                            }

                            // If not found, create a new record and add into array
                            if (obvDataByTrno == null) {
                                obvDataByTrno = new HashMap();
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
            files.put(exname, file);
            brT.close();
        }

        return files;
    }

    /**
     * Set reading flags for title lines (marked with *)
     *
     * @param line the string of reading line
     */
    @Override
    protected void setTitleFlgs(String line) {
        flg[0] = "meta";
        flg[1] = "";
        flg[2] = "data";
    }
}
