package org.agmip.translators.dssat;

import java.io.File;
import org.agmip.core.types.AdvancedHashMap;

/**
 *
 * @author Meng Zhang
 */
public class DssatControllerOutput {

    private DssatCommonOutput[] outputs = {
//        new DssatXFileOutput(),
//        new DssatSoilOutput(),
//        new DssatWeatherOutput(),
        new DssatAFileOutput(),
        new DssatTFileOutput()};
    
    /**
     * ALL DSSAT Data Output method
     * 
     * @param arg0   file output path
     * @param result  data holder object
     */
    public void writeFiles(String arg0, AdvancedHashMap result) {
        
        for (int i = 0; i < outputs.length; i++) {
            outputs[i].writeFile(arg0, result);
        }
    }
    
    /**
     * Get all output files
     */
    public File[] getOutputFiles() {
        File[] files = new File[outputs.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = outputs[i].getOutputFile();
        }
        return files;
    }
}
