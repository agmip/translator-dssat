package org.agmip.translators.dssat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import static org.agmip.util.MapUtil.*;
import static org.agmip.translators.dssat.DssatCommonInput.*;

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
        brMap = getBufferReader(arg0);

        // Set Data source and version info
        setDataVersionInfo(metaData);

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
        trMetaArr = CopyList((ArrayList<LinkedHashMap>) metaData.get("tr_meta"));
        metaData.remove("tr_meta");
        // Combine the each part of data
        for (int i = 0; i < mgnArr.size(); i++) {
            // Set meta data for all treatment
            expData = CopyList(metaData);
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
                soilData = getSectionData(soilArr, "soil_id", trMetaData.get("soil_id").toString());
                // if there is soil analysis data, create new soil block by using soil analysis info
                if (expData.containsKey("soil_analysis")) {
                    soilData = CopyList(soilData);
                    LinkedHashMap saTmp = (LinkedHashMap) expData.remove("soil_analysis");

                    // Update soil site data
                    copyItem(soilData, saTmp, "sadat");
                    copyItem(soilData, saTmp, "smhb");
                    copyItem(soilData, saTmp, "smpx");
                    copyItem(soilData, saTmp, "smke");
                    soilData.put("soil_id", "AG" + getValueOr(soilData, "soil_id", "AG").substring(2));    // TODO create new soil_id

                    // Update soil layer data
                    ArrayList<LinkedHashMap> soilLyrs = (ArrayList) soilData.get(soilReader.layerKey);
                    ArrayList<LinkedHashMap> saLyrs = (ArrayList) saTmp.get("data");
                    String[] copyKeys = {"slbdm", "sloc", "slni", "slphw", "slphb", "slpx", "slke"};
                    soilData.put(soilReader.layerKey, combinLayers(soilLyrs, saLyrs, "sllb", "sabl", copyKeys));
                }

                expData.put(soilReader.jsonKey, soilData);
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

    private void cutDataBlock(LinkedHashMap from, LinkedHashMap to, String key) {
        if (from.containsKey(key)) {
            to.put(key, CopyList((LinkedHashMap) from.get(key)));
            from.remove(key);
        }
    }

    private void copyItem(LinkedHashMap to, LinkedHashMap from, String key) {
        if (from.get(key) != null) {
            to.put(key, from.get(key));
        }
    }
}
