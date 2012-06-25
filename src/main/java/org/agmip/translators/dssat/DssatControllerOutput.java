package org.agmip.translators.dssat;

import org.agmip.core.types.AdvancedHashMap;

/**
 *
 * @author Meng Zhang
 */
public class DssatControllerOutput {

    private DssatCommonOutput[] outputs = {
        new DssatXFileOutput(),
        new DssatSoilOutput(),
        new DssatWeatherOutput(),
        new DssatAFileOutput(),
        new DssatTFileOutput()};
    
    public void writeFiles(String arg0, AdvancedHashMap result) {
        
        for (int i = 0; i < outputs.length; i++) {
            outputs[i].writeFile(arg0, result);
        }
    }
}
