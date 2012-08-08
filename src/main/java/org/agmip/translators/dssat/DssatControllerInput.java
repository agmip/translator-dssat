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

    private DssatXFileInput mgnReader = new DssatXFileInput();
    private DssatSoilInput soilReader = new DssatSoilInput();
    private DssatWeatherInput wthReader = new DssatWeatherInput();
    private DssatAFileInput obvAReader = new DssatAFileInput();
    private DssatTFileInput obvTReader = new DssatTFileInput();

    /**
     * All DSSAT Data input method
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     */
    public ArrayList<LinkedHashMap> readFiles(String arg0) throws FileNotFoundException, IOException {

        HashMap brMap = new HashMap();
        LinkedHashMap metaData = new LinkedHashMap();
        ArrayList<LinkedHashMap> expArr = new ArrayList<LinkedHashMap>();
        LinkedHashMap expData;
        ArrayList<LinkedHashMap> mgnArr;
        ArrayList<LinkedHashMap> soilArr;
        LinkedHashMap soilData;
        ArrayList<LinkedHashMap> wthArr;
        LinkedHashMap obvAFile;
        ArrayList<LinkedHashMap> obvAArr;
        LinkedHashMap obvTFile;
        ArrayList<LinkedHashMap> obvTArr;

        // Get buffered input file holder
        try {
            brMap = getBufferReader(arg0);
        } catch (FileNotFoundException fe) {
            System.out.println("File not found under following path : [" + arg0 + "]!");
            return expArr;
        }

        // Set Data source and version info
        setDataVersionInfo(metaData);

        // Try to read XFile (treatment; management)
        mgnArr = mgnReader.readTreatments(brMap, metaData);

        // Try to read soil File
        soilArr = soilReader.readSoilSites(brMap, metaData);

        // Try to read weather File
        wthArr = wthReader.readDailyData(brMap, metaData);

        // Try to read Observed AFile (summary data)
        obvAFile = obvAReader.readObvData(brMap);
        obvAArr = getObjectOr(obvAFile, obvAReader.obvDataKey, new ArrayList<LinkedHashMap>());

        // Try to read Observed AFile (time-series data)
        obvTFile = obvTReader.readObvData(brMap);
        obvTArr = getObjectOr(obvTFile, obvTReader.obvDataKey, new ArrayList<LinkedHashMap>());

        // Combine the each part of data
        for (int i = 0; i < mgnArr.size(); i++) {

            // Set meta data block for this treatment
            expData = mgnReader.setupMetaData(metaData, i);

            // Set soil data for this treatment
            if (!getValueOr(expData, "wst_id", "0").equals("0")) {
                LinkedHashMap tmp = getSectionData(wthArr, "wst_id", expData.get("wst_id").toString());
                if (tmp != null && tmp.size() != 0) {
                    expData.put(wthReader.jsonKey, tmp);
                }
            }

            // Set weather data for this treatment
            if (!getValueOr(expData, "soil_id", "0").equals("0")) {
                soilData = getSectionData(soilArr, "soil_id", expData.get("soil_id").toString());
                // if there is soil analysis data, create new soil block by using soil analysis info
                if (expData.get("soil_analysis") != null) {
                    if (soilData == null) {
                        soilData = new LinkedHashMap();
                    } else {
                        soilData = CopyList(soilData);
                    }
                    LinkedHashMap saTmp = (LinkedHashMap) expData.remove("soil_analysis");

                    // Update soil site data
                    copyItem(soilData, saTmp, "sadat");
                    copyItem(soilData, saTmp, "smhb");
                    copyItem(soilData, saTmp, "smpx");
                    copyItem(soilData, saTmp, "smke");
                    soilData.put("soil_id", soilData.get("soil_id") + "_" + (i + 1));
                    expData.put("soil_id", expData.get("soil_id") + "_" + (i + 1));

                    // Update soil layer data
                    ArrayList<LinkedHashMap> soilLyrs = getObjectOr(soilData, soilReader.layerKey, new ArrayList());
                    ArrayList<LinkedHashMap> saLyrs = getObjectOr(saTmp, mgnReader.icEventKey, new ArrayList());
                    String[] copyKeys = {"slbdm", "sloc", "slni", "slphw", "slphb", "slpx", "slke"};
                    soilData.put(soilReader.layerKey, combinLayers(soilLyrs, saLyrs, "sllb", "slbl", copyKeys));
                }

                if (soilData != null && soilData.size() != 0) {
                    expData.put(soilReader.jsonKey, soilData);
                }
            }

            // observed data (summary)
            LinkedHashMap obv = new LinkedHashMap();
            expData.put(obvAReader.jsonKey, obv);
            if (!getValueOr(expData, "trno", "0").equals("0")) {
                LinkedHashMap tmp = getSectionData(obvAArr, "trno_a", expData.get("trno").toString());
                if (tmp != null) {
                    obv.putAll(tmp);
                }
            }

            // observed data (time-series)
            if (!getValueOr(expData, "trno", "0").equals("0")) {
                LinkedHashMap tmp = getSectionData(obvTArr, "trno_t", expData.get("trno").toString());
                if (tmp != null) {
                    obv.put("time_series", tmp.get(obvTReader.obvDataKey));
                }
            }

            // there is no observed data, remove the key from experiment object
            if (obv.isEmpty()) {
                expData.remove(obvAReader.jsonKey);
            }

            // Set experiment data include management, Initial Condition and DSSAT specific data blocks for this treatment
            mgnReader.setupTrnData(expData, mgnArr.get(i), obvAFile, obvTFile);

            // Add to output array
            expArr.add(expData);
        }

        return expArr;
    }
}
