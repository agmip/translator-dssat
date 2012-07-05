package org.agmip.translators.dssat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
    public ArrayList getOutputFiles() {
        ArrayList<File> files = new ArrayList();
        for (int i = 0; i < files.size(); i++) {
            if (outputs[i] instanceof DssatWeatherOutput) {
                files.addAll(Arrays.asList(((DssatWeatherOutput) outputs[i]).getOutputFiles()));
            } else {
                files.add(outputs[i].getOutputFile());
            }
        }
        return files;
    }
}
