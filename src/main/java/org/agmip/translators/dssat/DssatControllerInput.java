package org.agmip.translators.dssat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import static org.agmip.util.MapUtil.*;

/**
 *
 * @author Meng Zhang
 */
public class DssatControllerInput {

    private DssatCommonInput[] inputs = {
        //        new DssatSoilInput(),
        new DssatWeatherInput(),
        new DssatXFileInput()};
//        new DssatAFileInput(),
//        new DssatTFileInput()};
    private DssatXFileInput mgnReader = new DssatXFileInput();
    private DssatSoilInput soilReader = new DssatSoilInput();
    private DssatWeatherInput wthReader = new DssatWeatherInput();

    /**
     * All DSSAT Data input method
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    public ArrayList<LinkedHashMap> readFiles(String arg0) throws FileNotFoundException, IOException {

        HashMap brMap = new HashMap();
        LinkedHashMap metaData = new LinkedHashMap();
        ArrayList<LinkedHashMap> expArr = new ArrayList<LinkedHashMap>();
        LinkedHashMap expData;
        ArrayList<LinkedHashMap> trMetaArr;
        LinkedHashMap trMetaData;
        ArrayList<LinkedHashMap> mgnArr;
        LinkedHashMap mgnData;
        ArrayList<LinkedHashMap> soilArr;
        LinkedHashMap soilData;
        ArrayList<LinkedHashMap> wthArr;
        LinkedHashMap wthData;

        // Get buffered input file holder
        brMap = DssatCommonInput.getBufferReader(arg0);

        // Set Data source and version info
        DssatCommonInput.setDataVersionInfo(metaData);

        // Try to read XFile (treatment; management)
        mgnArr = mgnReader.readTreatments(brMap, metaData);

        // Try to read soil File
        soilArr = soilReader.readSoilSites(brMap, metaData);

        // Try to read weather File
        wthArr = wthReader.readDailyData(brMap, metaData);

        // Try to read Observed AFile (summary data)
        // TODO
        // Try to read Observed AFile (time-series data)
        // TODO

        // Get meta data for per treatment
        trMetaArr = DssatCommonInput.CopyList((ArrayList<LinkedHashMap>) metaData.get("tr_meta"));
        metaData.remove("tr_meta");
        // Combine the each part of data
        for (int i = 0; i < mgnArr.size(); i++) {
            // Set meta data for all treatment
            expData = DssatCommonInput.CopyList(metaData);
            // Set meta data per treatment
            trMetaData = trMetaArr.get(i);
            expData.putAll(trMetaData);
            expData.remove("wst_id");

            // Set soil data for this treatment
            if (!getObjectOr(trMetaData, "wst_id", "0").equals("0")) {
                expData.put(wthReader.jsonKey, getSectionData(wthArr, "wst_id", trMetaData.get("wst_id").toString()));
            }

            // Set weather data for this treatment
            if (!getObjectOr(trMetaData, "soil_id", "0").equals("0")) {
                expData.put(soilReader.jsonKey, getSectionData(soilArr, "soil_id", trMetaData.get("soil_id").toString()));
            }

            // Set management data for this treatment
            mgnData = mgnArr.get(i);
            // TODO some handling here
            expData.put(mgnReader.jsonKey, mgnData);

            // Set Initial Condition data for this treatment
            cutDataBlock(mgnData, expData, "initial_condition");

            // Set DSSAT specific data blocks
            cutDataBlock(mgnData, expData, "dssat_info");
            cutDataBlock(mgnData, expData, "dssat_sequence");

            // Add to output array
            expArr.add(expData);
        }

//        for (int i = 0; i < inputs.length; i++) {
//
//            tmp = inputs[i].readFile(brMap);
//
//            // Check if reading result is empty. If so, ignore it.
//            if (!tmp.isEmpty()) {
//                // If return map contains multiple file, like soil or weather, get ArrayList from the return map
//                if (inputs[i] instanceof DssatSoilInput || inputs[i] instanceof DssatWeatherInput) {
//                    ret.put(inputs[i].jsonKey, tmp.get(inputs[i].jsonKey));
//
//                } // if read Observed data file, need to save them under the same key (currently is "observed")
//                else if (inputs[i] instanceof DssatAFileInput || inputs[i] instanceof DssatTFileInput) {
//                    if (ret.containsKey(inputs[i].jsonKey)) {
//                        ((LinkedHashMap) ret.get(inputs[i].jsonKey)).putAll(tmp);
//                    } else {
//                        ret.put(inputs[i].jsonKey, tmp);
//                    }
//                } else {
//                    ret.put(inputs[i].jsonKey, tmp);
//                }
//
//            } else {
//            }
//        }

        return expArr;
    }

    /**
     * Get the section data by given index value and key
     * 
     * @param secArr    Section data array
     * @param key       index variable name
     * @param value     index variable value
     */
    private LinkedHashMap getSectionData(ArrayList secArr, Object key, String value) {

        LinkedHashMap ret = null;
        // Get First data node
        if (secArr.isEmpty() || value == null) {
            return ret;
        }
        for (int i = 0; i < secArr.size(); i++) {
            if (value.equals(((LinkedHashMap) secArr.get(i)).get(key))) {
                return DssatCommonInput.CopyList((LinkedHashMap) secArr.get(i));
            }
        }

        return ret;
    }

    private void cutDataBlock(LinkedHashMap from, LinkedHashMap to, String key) {
        if (from.containsKey(key)) {
            to.put(key, DssatCommonInput.CopyList((LinkedHashMap) from.get(key)));
            from.remove(key);
        }
    }
}
