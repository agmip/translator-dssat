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
        new DssatXFileInput()};
    
    public AdvancedHashMap readFiles(String arg0) throws FileNotFoundException, IOException {
        
        HashMap brMap = new HashMap();
        AdvancedHashMap ret = new AdvancedHashMap();
        if (inputs.length > 0) {
            brMap = inputs[0].getBufferReader(arg0);
        }
        
        for (int i = 0; i < inputs.length; i++) {
            // If return map contains multiple file, like soil or weather, get ArrayList from the return map
            if (inputs[i].getClass().equals(DssatSoilInput.class) || inputs[i].getClass().equals(DssatWeatherInput.class)) {
                ret.put(inputs[i].jsonKey, inputs[i].readFile(brMap).get(inputs[i].jsonKey));
                
            } // if read Observed data file, need to save them under the same key (currently is "observed")
            else if (inputs[i].getClass().equals(DssatAFileInput.class) || inputs[i].getClass().equals(DssatTFileInput.class)) {
                if (ret.containsKey(inputs[i].jsonKey)) {
                    ((AdvancedHashMap)ret.get(inputs[i].jsonKey)).put(inputs[i].readFile(brMap));
                } else {
                    ret.put(inputs[i].jsonKey, inputs[i].readFile(brMap));
                }
            } else {
                ret.put(inputs[i].jsonKey, inputs[i].readFile(brMap));
            }
            
        }
        
        return ret;
    }
}
