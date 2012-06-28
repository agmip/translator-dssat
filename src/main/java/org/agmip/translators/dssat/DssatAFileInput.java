package org.agmip.translators.dssat;

import java.io.BufferedReader;
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
        LinkedHashMap formats = new LinkedHashMap();
        ArrayList titles = new ArrayList();
        ArrayList obvData = new ArrayList();
        String obvDataKey = "data";  // TODO the key name might change
        String obvFileKey = "average";  // TODO the key name might change

        brA = (BufferedReader) brMap.get("A");

        // If AFile File is no been found
        if (brA == null) {
            // TODO reprot file not exist error
            return ret;
        }

        ret.put(obvFileKey, file);
        file.put(obvDataKey, obvData);
        while ((line = brA.readLine()) != null) {

            // Get content type of line
            judgeContentType(line);

            // Read Observed data
            if (flg[2].equals("data")) {

                // Read meta info
                if (flg[0].equals("meta")) {

                    file.put("meta", line.replaceAll(".*:", "").trim());

                } // Read data info 
                else {
                    // Set variables' formats
                    formats.clear();
                    for (int i = 0; i < titles.size(); i++) {
                        formats.put(titles.get(i), 6);
                    }
                    // Read line and save into return holder
                    addToArray(obvData, readLine(line, formats), "trno");
                }

            } // Read Observed title
            else if (flg[2].equals("title")) {

                titles = new ArrayList();
                line = line.replaceFirst("@", " ");
                for (int i = 0; i < line.length(); i += 6) {
                    titles.add(line.substring(i, Math.min(i + 6, line.length())).trim().toLowerCase());
                }

            } else {
            }
        }

//        brA.close();

        return ret;
    }

    /**
     * Set reading flgs for title lines (marked with *)
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
