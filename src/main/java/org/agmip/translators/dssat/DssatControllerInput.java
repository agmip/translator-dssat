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
            ret.put(inputs[i].jsonKey, inputs[i].readFile(brMap));
        }
        
        return ret;
    }
}
