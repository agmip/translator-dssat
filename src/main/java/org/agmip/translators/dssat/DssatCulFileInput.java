package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * DSSAT Cultivar Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatCulFileInput extends DssatCommonInput {

    public String dataKey = "data";  // P.S. the key name might change

    /**
     * Constructor with no parameters Set jsonKey as "dssat_cultivar_info"
     *
     */
    public DssatCulFileInput() {
        super();
        jsonKey = "dssat_cultivar_info";
    }

    /**
     * DSSAT Cultivar Data input method for only inputing Cultivar file
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     */
    @Override
    protected ArrayList<LinkedHashMap> readFile(HashMap brMap) throws IOException {
        LinkedHashMap metaData = new LinkedHashMap();
        ArrayList<LinkedHashMap> culArr = readCultivarData(brMap, metaData);
//        compressData(sites);
        ArrayList<LinkedHashMap> ret = new ArrayList();
        LinkedHashMap tmp = new LinkedHashMap();
        LinkedHashMap tmp2 = new LinkedHashMap();
        tmp.put(jsonKey, tmp2);
        tmp2.put(dataKey, culArr);
        ret.add(tmp);

        return ret;
    }

    /**
     * DSSAT Cultivar Data input method for Controller using (return map will
     * not be compressed)
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     */
    protected ArrayList<LinkedHashMap> readCultivarData(HashMap brMap, LinkedHashMap ret) throws IOException {

        String slNotes = null;
        ArrayList<LinkedHashMap> culArr = new ArrayList<LinkedHashMap>();
        LinkedHashMap culData = new LinkedHashMap();
        String line;
        BufferedReader brC = null;
        Object buf;
        LinkedHashMap mapC;
        LinkedHashMap formats = new LinkedHashMap();

        mapC = (LinkedHashMap) brMap.get("C");

        // If Cultivar File is no been found
        if (mapC.isEmpty()) {
            return culArr;
        }

        culArr = new ArrayList();

        for (Object key : mapC.keySet()) {

            buf = mapC.get(key);
            if (buf instanceof char[]) {
                brC = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brC = (BufferedReader) buf;
            }

            while ((line = brC.readLine()) != null) {

                // Get content type of line
                judgeContentType(line);

                // Read Header Info
                if (flg[2].equals("meta")) {
                    culData = new LinkedHashMap();
                    culData.put("header_info", line.trim());
                } // Cultivar data title
                else if (flg[2].equals("title")) {
                    culData.put("cul_titles", line);
                }// Cultivar data
                else if (flg[2].equals("data")) {
                    // Set variables' formats
                    line = line.replaceAll(".*:", "").trim();
                    formats.clear();
                    formats.put("cul_id", 6);
                    // Read line and save into return holder
                    LinkedHashMap tmp = readLine(line, formats);
                    tmp.putAll(culData);
                    tmp.put("cul_info", line);
                    culArr.add(tmp);
                }
            }
        }

//        compressData(culArr);
        brC.close();

        return culArr;
    }

    /**
     * Set reading flgs for title lines (marked with *)
     *
     * @param line the string of reading line
     */
    @Override
    protected void setTitleFlgs(String line) {
        flg[0] = "cultivar";
        flg[1] = "";
        flg[2] = "meta";
    }
}
