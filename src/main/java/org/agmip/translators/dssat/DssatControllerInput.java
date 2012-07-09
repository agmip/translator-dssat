package org.agmip.translators.dssat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import org.agmip.core.types.AdvancedHashMap;

/**
 *
 * @author Meng Zhang
 */
public class DssatControllerInput {

    private DssatCommonInput[] inputs = {
        new DssatSoilInput(),
        new DssatWeatherInput(),
        new DssatXFileInput()};
//        new DssatAFileInput(),
//        new DssatTFileInput()};

    /**
     * All DSSAT Data input method
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    public AdvancedHashMap readFiles(String arg0) throws FileNotFoundException, IOException {

        HashMap brMap = new HashMap();
        AdvancedHashMap ret = new AdvancedHashMap();
        AdvancedHashMap tmp;
        if (inputs.length > 0) {
            brMap = inputs[0].getBufferReader(arg0);
        }

        for (int i = 0; i < inputs.length; i++) {

            tmp = inputs[i].readFile(brMap);

            // Check if reading result is empty. If so, ignore it.
            if (!tmp.isEmpty()) {
                // If return map contains multiple file, like soil or weather, get ArrayList from the return map
                if (inputs[i] instanceof DssatSoilInput || inputs[i] instanceof DssatWeatherInput) {
                    ret.put(inputs[i].jsonKey, tmp.get(inputs[i].jsonKey));

                } // if read Observed data file, need to save them under the same key (currently is "observed")
                else if (inputs[i] instanceof DssatAFileInput || inputs[i] instanceof DssatTFileInput) {
                    if (ret.containsKey(inputs[i].jsonKey)) {
                        ((AdvancedHashMap) ret.get(inputs[i].jsonKey)).put(tmp);
                    } else {
                        ret.put(inputs[i].jsonKey, tmp);
                    }
                } else {
                    ret.put(inputs[i].jsonKey, tmp);
                }

            } else {
            }
        }

        return ret;
    }
}
