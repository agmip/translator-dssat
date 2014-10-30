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
     * @throws java.io.IOException
     */
    @Override
    protected HashMap readFile(HashMap brMap) throws IOException {
        HashMap ret = new HashMap();
        HashMap metaData = new HashMap();
        ArrayList<HashMap> culArr = readCultivarData(brMap, metaData);
//        compressData(sites);
        ArrayList<HashMap> expArr = new ArrayList();
        HashMap tmp = new HashMap();
        HashMap tmp2 = new HashMap();
        tmp.put(jsonKey, tmp2);
        tmp2.put(dataKey, culArr);
        expArr.add(tmp);
        ret.put("experiments", expArr);

        return ret;
    }

    /**
     * DSSAT Cultivar Data input method for Controller using (return map will
     * not be compressed)
     *
     * @param brMap The holder for BufferReader objects for all files
     * @param ret
     * @return result data holder object
     * @throws java.io.IOException
     */
    protected ArrayList<HashMap> readCultivarData(HashMap brMap, HashMap ret) throws IOException {

        ArrayList<HashMap> culArr = new ArrayList<HashMap>();
        HashMap culData = new HashMap();
        String line;
        BufferedReader brC;
        Object buf;
        HashMap mapC;
        LinkedHashMap formats = new LinkedHashMap();

        mapC = (HashMap) brMap.get("C");

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
                    culData = new HashMap();
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
                    HashMap tmp = readLine(line, formats);
                    tmp.putAll(culData);
                    tmp.put("cul_info", line);
                    culArr.add(tmp);
                }
            }
            brC.close();
        }

//        compressData(culArr);
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
