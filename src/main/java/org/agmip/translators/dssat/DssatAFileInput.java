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
     * Constructor with no parameters Set jsonKey as "observed"
     *
     */
    public DssatAFileInput() {
        super();
        jsonKey = "observed";
    }

    /**
     * DSSAT AFile Data input method for Controller using
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
        HashMap expData;

        for (String exname : files.keySet()) {
            obvData = (ArrayList) files.get(exname).get(obvDataKey);
            for (HashMap obvSub : obvData) {
                expData = new HashMap();
                copyItem(expData, files.get(exname), "exname");
                copyItem(expData, files.get(exname), "crid");
                copyItem(expData, files.get(exname), "local_name");
                expData.put(jsonKey, obvSub);
                expArr.add(expData);
            }
        }

        // remove index variables
        ArrayList idNames = new ArrayList();
        idNames.add("trno_a");
        removeIndex(expArr, idNames);
        ret.put("experiments", expArr);

        return ret;
    }

    /**
     * DSSAT AFile Data input method for Controller using (return map will not
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
        HashMap mapA;
        BufferedReader brA;
        Object buf;
        LinkedHashMap formats = new LinkedHashMap();
        ArrayList titles = new ArrayList();
        ArrayList obvData;
        DssatObservedData obvDataList = DssatObservedData.INSTANCE;    // Varibale list definition
        String pdate;

        mapA = (HashMap) brMap.get("A");

        // If AFile File is no been found
        if (mapA.isEmpty()) {
            return file;
        }

        for (Object keyA : mapA.keySet()) {

            buf = mapA.get(keyA);
            String fileName = (String) keyA;
//            String exname = fileName.replaceAll("\\.", "").replaceAll("A$", "");
            String exname = fileName.replaceAll("\\.\\w\\wA$", "");
            String crid = fileName.replaceAll("\\w+\\.", "").replaceAll("A$", "");
            if (buf instanceof char[]) {
                brA = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brA = (BufferedReader) buf;
            }

            file = new HashMap();
            obvData = new ArrayList();

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
                        formats.put("null", 10);  // P.S. Since exname in top line is not reliable, read from file name
                        formats.put("local_name", line.length());
                        // Read line and save into return holder
                        file.putAll(readLine(line, formats));
                        file.put("exname", exname);
                        file.put("crid", DssatCRIDHelper.get3BitCrid(crid));

                    } // Read data info 
                    else {
                        // Set variables' formats
                        formats.clear();
                        for (Object title : titles) {
                            formats.put(title, 6);
                        }
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        pdate = getPdate(brMap, (String) tmp.get("trno_a"), fileName.replaceAll("A$", "X"));
                        for (Object title : titles) {
                            if (obvDataList.isDateType(title)) {
                                translateDateStrForDOY(tmp, (String) title, pdate, "");
//                            String val = (String) tmp.get(title);
//                            if (val != null && val.length() > 3) {
//                                tmp.put(title, val.substring(val.length() - 3, val.length()));
//                            }
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
            files.put(exname, file);
            brA.close();
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
