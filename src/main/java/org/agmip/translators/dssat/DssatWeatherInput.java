package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.agmip.core.types.AdvancedHashMap;

/**
 * DSSAT Weather Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatWeatherInput extends DssatCommonInput {

    /**
     * Constructor with no parameters
     * Set jsonKey as "weather"
     * 
     */
    public DssatWeatherInput() {
        super();
        jsonKey = "weather";
    }

    /**
     * DSSAT Weather Data input method for Controller using
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    @Override
    protected AdvancedHashMap readFile(HashMap brMap) throws IOException {

        AdvancedHashMap ret = new AdvancedHashMap();
        String line;
        BufferedReader brW;
        LinkedHashMap formats = new LinkedHashMap();

        brW = (BufferedReader) brMap.get("W");

        // If Weather File is no been found
        if (brW == null) {
            // TODO reprot file not exist error
            return ret;
        }

        while ((line = brW.readLine()) != null) {
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

        brW.close();

        return ret;
    }

    /**
     * Set reading flgs for title lines (marked with *)
     * 
     * @param line  the string of reading line
     */
    @Override
    protected void setTitleFlgs(String line) {
        flg[0] = "weather";
        flg[1] = "";
        flg[2] = "data";
    }
}
