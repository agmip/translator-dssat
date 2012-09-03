package org.agmip.translators.dssat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.agmip.core.types.TranslatorInput;
import static org.agmip.translators.dssat.DssatCommonInput.*;
import static org.agmip.util.MapUtil.*;

/**
 *
 * @author Meng Zhang
 */
public class DssatControllerInput implements TranslatorInput {

    private DssatXFileInput mgnReader = new DssatXFileInput();
    private DssatSoilInput soilReader = new DssatSoilInput();
    private DssatWeatherInput wthReader = new DssatWeatherInput();
    private DssatAFileInput obvAReader = new DssatAFileInput();
    private DssatTFileInput obvTReader = new DssatTFileInput();
    private DssatCulFileInput culReader = new DssatCulFileInput();

    /**
     * All DSSAT Data input method
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     */
    public LinkedHashMap readFile(String arg0) {

        HashMap brMap;
        LinkedHashMap ret = new LinkedHashMap();
        LinkedHashMap metaData = new LinkedHashMap();
        ArrayList<LinkedHashMap> expArr = new ArrayList<LinkedHashMap>();
        LinkedHashMap expData;
        ArrayList<LinkedHashMap> mgnArr;
        ArrayList<LinkedHashMap> soilArr;
        LinkedHashMap soilData;
        LinkedHashMap soilTmpMap = new LinkedHashMap();
        String soilId;
        ArrayList<LinkedHashMap> wthArr;
        LinkedHashMap wthData;
        LinkedHashMap wthTmpMap = new LinkedHashMap();
        String wthId;
        LinkedHashMap obvAFiles;
        LinkedHashMap obvAFile;
        ArrayList<LinkedHashMap> obvAArr;
        LinkedHashMap obvTFiles;
        LinkedHashMap obvTFile;
        ArrayList<LinkedHashMap> obvTArr;
        ArrayList<LinkedHashMap> culArr;
        LinkedHashMap culData;

        try {
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
            obvAFiles = obvAReader.readObvData(brMap);

            // Try to read Observed AFile (time-series data)
            obvTFiles = obvTReader.readObvData(brMap);

            // Try to read cultivar File
            culArr = culReader.readCultivarData(brMap, metaData);

        } catch (FileNotFoundException fe) {
            System.out.println("File not found under following path : [" + arg0 + "]!");
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return ret;
        }

        // Combine the each part of data
        for (int i = 0; i < mgnArr.size(); i++) {

            // Set meta data block for this treatment
            expData = mgnReader.setupMetaData(metaData, i);

            // Set soil data for this treatment
            wthId = getValueOr(expData, "wst_id", "0");
            if (!wthId.equals("0")) {
                wthData = getSectionData(wthArr, "wst_id", wthId);
                if (wthData != null && wthData.size() != 0) {
//                    expData.put(wthReader.jsonKey, wthData);
                    wthTmpMap.put(wthId, wthData);
                }
            }

            // Set weather data for this treatment
            soilId = getValueOr(expData, "soil_id", "0");
            if (!soilId.equals("0") && !soilTmpMap.containsKey(soilId)) {
                soilData = getSectionData(soilArr, "soil_id", soilId);
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
                    soilId += "_" + (i + 1);
                    soilData.put("soil_id", soilId);
                    expData.put("soil_id", soilId);

                    // Update soil layer data
                    ArrayList<LinkedHashMap> soilLyrs = getObjectOr(soilData, soilReader.layerKey, new ArrayList());
                    ArrayList<LinkedHashMap> saLyrs = getObjectOr(saTmp, mgnReader.icEventKey, new ArrayList());
                    String[] copyKeys = {"sllb", "slbdm", "sloc", "slni", "slphw", "slphb", "slpx", "slke", "slsc"};
                    soilData.put(soilReader.layerKey, combinLayers(soilLyrs, saLyrs, "sllb", "sllb", copyKeys));
                }

                if (soilData != null && soilData.size() != 0) {
//                    expData.put(soilReader.jsonKey, soilData);
                    soilTmpMap.put(soilId, soilData);
                }
            }

            // Get exname
            String exname = (String) expData.remove("exname_o");
            if (exname == null) {
                exname = "";
            }
            // observed data (summary)
            obvAFile = getObjectOr(obvAFiles, exname, new LinkedHashMap());
            obvAArr = getObjectOr(obvAFile, obvAReader.obvDataKey, new ArrayList<LinkedHashMap>());
            LinkedHashMap obv = new LinkedHashMap();
            expData.put(obvAReader.jsonKey, obv);
            if (!getValueOr(expData, "trno", "0").equals("0")) {
                LinkedHashMap tmp = getSectionData(obvAArr, "trno_a", expData.get("trno").toString());
                if (tmp != null) {
                    obv.putAll(tmp);
                }
            }

            // observed data (time-series)
            obvTFile = getObjectOr(obvTFiles, exname, new LinkedHashMap());
            obvTArr = getObjectOr(obvTFile, obvTReader.obvDataKey, new ArrayList<LinkedHashMap>());
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
            mgnReader.setupTrnData(expData, mgnArr.get(i), obvAFiles, obvTFiles);

            // Set dssat cultivar info block
            if (!culArr.isEmpty()) {
                LinkedHashMap mgnData = getObjectOr(expData, mgnReader.jsonKey, new LinkedHashMap());
                ArrayList<LinkedHashMap> eventArr = getObjectOr(mgnData, "events", new ArrayList());
                ArrayList<LinkedHashMap> culTmpArr = new ArrayList<LinkedHashMap>();
                for (int j = 0; j < eventArr.size(); j++) {
                    if (getObjectOr(eventArr.get(j), "event", "").equals("planting")) {
                        culData = getSectionData(culArr, "cul_id", (String) eventArr.get(j).get("cul_id"));
                        if (culData != null) {
                            culTmpArr.add(culData);
                        }
                    }
                }

                if (!culTmpArr.isEmpty()) {
                    LinkedHashMap tmp = new LinkedHashMap();
                    tmp.put(culReader.dataKey, culTmpArr);
                    expData.put(culReader.jsonKey, tmp);
                }
            }

            // Add to output array
            expArr.add(expData);
        }

        ret.put("experiments", expArr);
        if (!soilTmpMap.isEmpty()) {
            ret.put("soils", new ArrayList(soilTmpMap.values()));
        }
        if (!wthTmpMap.isEmpty()) {
            ret.put("weathers", new ArrayList(wthTmpMap.values()));
        }

        return ret;
    }
}
