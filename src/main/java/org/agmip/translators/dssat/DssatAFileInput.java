package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.IOException;
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
        String line;
        BufferedReader brA;
        LinkedHashMap formats = new LinkedHashMap();

        brA = (BufferedReader) brMap.get("A");

        // If AFile File is no been found
        if (brA == null) {
            // TODO reprot file not exist error
            return ret;
        }

        while ((line = brA.readLine()) != null) {
            // TODO Create holders for each section and build final result holder in the last
            // Get content type of line
            judgeContentType(line);

            // Read Exp Detail
            if (flg[0].startsWith("exp.details:") && flg[2].equals("")) {

                // Set variables' formats
                formats.clear();
                formats.put("exname", 11);
                formats.put("local_name", 61);
                // Read line and save into return holder
                ret.put(readLine(line.substring(13), formats));
                ret.put("institutes", line.substring(14, 16).trim());
            } // Read General Section
            else {
            }


        }

        brA.close();

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
