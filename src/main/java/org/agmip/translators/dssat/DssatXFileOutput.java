package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.agmip.translators.dssat.DssatCommonInput.copyItem;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT Experiment Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatXFileOutput extends DssatCommonOutput {

    private static final Logger LOG = LoggerFactory.getLogger(DssatXFileOutput.class);
//    public static final DssatCRIDHelper crHelper = new DssatCRIDHelper();

    /**
     * DSSAT Experiment Data Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     */
    @Override
    public void writeFile(String arg0, Map result) {

        // Initial variables
        HashMap expData = (HashMap) result;
        ArrayList<HashMap> soilArr = readSWData(expData, "soil");
        ArrayList<HashMap> wthArr = readSWData(expData, "weather");
        HashMap soilData;
        HashMap wthData;
        BufferedWriter bwX;                          // output object
        StringBuilder sbGenData = new StringBuilder();      // construct the data info in the output
        StringBuilder sbDomeData = new StringBuilder();     // construct the dome info in the output
        StringBuilder sbNotesData = new StringBuilder();      // construct the data info in the output
        StringBuilder sbData = new StringBuilder();         // construct the data info in the output
        StringBuilder eventPart2;                           // output string for second part of event data
        HashMap sqData;
        ArrayList<HashMap> evtArr;            // Arraylist for section data holder
        ArrayList<HashMap> adjArr;
        HashMap evtData;
//        int trmnNum;                            // total numbers of treatment in the data holder
        int cuNum;                              // total numbers of cultivars in the data holder
        int flNum;                              // total numbers of fields in the data holder
        int saNum;                              // total numbers of soil analysis in the data holder
        int icNum;                              // total numbers of initial conditions in the data holder
        int mpNum;                              // total numbers of plaintings in the data holder
        int miNum;                              // total numbers of irrigations in the data holder
        int mfNum;                              // total numbers of fertilizers in the data holder
        int mrNum;                              // total numbers of residues in the data holder
        int mcNum;                              // total numbers of chemical in the data holder
        int mtNum;                              // total numbers of tillage in the data holder
        int meNum;                              // total numbers of enveronment modification in the data holder
        int mhNum;                              // total numbers of harvest in the data holder
        int smNum;                              // total numbers of simulation controll record
        ArrayList<HashMap> sqArr;     // array for treatment record
        ArrayList<HashMap> cuArr = new ArrayList();   // array for cultivars record
        ArrayList<HashMap> flArr = new ArrayList();   // array for fields record
        ArrayList<HashMap> saArr = new ArrayList();   // array for soil analysis record
        ArrayList<HashMap> icArr = new ArrayList();   // array for initial conditions record
        ArrayList<HashMap> mpArr = new ArrayList();   // array for plaintings record
        ArrayList<ArrayList<HashMap>> miArr = new ArrayList();   // array for irrigations record
        ArrayList<ArrayList<HashMap>> mfArr = new ArrayList();   // array for fertilizers record
        ArrayList<ArrayList<HashMap>> mrArr = new ArrayList();   // array for residues record
        ArrayList<ArrayList<HashMap>> mcArr = new ArrayList();   // array for chemical record
        ArrayList<ArrayList<HashMap>> mtArr = new ArrayList();   // array for tillage record
        ArrayList<ArrayList<HashMap>> meArr = new ArrayList();     // array for enveronment modification record
        ArrayList<ArrayList<HashMap>> mhArr = new ArrayList();   // array for harvest record
        ArrayList<HashMap> smArr = new ArrayList();     // array for simulation control record
//        String exName;
        boolean isFallow = false;

        try {

            // Set default value for missing data
            if (expData == null || expData.isEmpty()) {
                return;
            }
//            decompressData((HashMap) result);
            setDefVal();

            // Initial BufferedWriter
            String fileName = getFileName(result, "X");
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwX = new BufferedWriter(new FileWriter(outputFile));

            // Output XFile
            // EXP.DETAILS Section
            sbError.append(String.format("*EXP.DETAILS: %1$-10s %2$s\r\n\r\n",
                    getFileName(result, "").replaceAll("\\.", ""),
                    getObjectOr(expData, "local_name", defValBlank)));

            // GENERAL Section
            sbGenData.append("*GENERAL\r\n");
            // People
            if (!getObjectOr(expData, "person_notes", "").isEmpty()) {
                sbGenData.append(String.format("@PEOPLE\r\n %1$s\r\n", getObjectOr(expData, "person_notes", defValBlank)));
            }
            // Address
            if (getObjectOr(expData, "institution", "").isEmpty()) {
//                if (!getObjectOr(expData, "fl_loc_1", "").equals("")
//                        && getObjectOr(expData, "fl_loc_2", "").equals("")
//                        && getObjectOr(expData, "fl_loc_3", "").equals("")) {
//                    sbGenData.append(String.format("@ADDRESS\r\n %3$s, %2$s, %1$s\r\n",
//                            getObjectOr(expData, "fl_loc_1", defValBlank).toString(),
//                            getObjectOr(expData, "fl_loc_2", defValBlank).toString(),
//                            getObjectOr(expData, "fl_loc_3", defValBlank).toString()));
//                }
            } else {
                sbGenData.append(String.format("@ADDRESS\r\n %1$s\r\n", getObjectOr(expData, "institution", defValBlank)));
            }

            // Site
            if (!getObjectOr(expData, "site_name", "").isEmpty()) {
                sbGenData.append(String.format("@SITE\r\n %1$s\r\n", getObjectOr(expData, "site_name", defValBlank)));
            }
            // Plot Info
            if (isPlotInfoExist(expData)) {

                sbGenData.append("@ PAREA  PRNO  PLEN  PLDR  PLSP  PLAY HAREA  HRNO  HLEN  HARM.........\r\n");
                sbGenData.append(String.format(" %1$6s %2$5s %3$5s %4$5s %5$5s %6$-5s %7$5s %8$5s %9$5s %10$-15s\r\n",
                        formatNumStr(6, expData, "plta", defValR),
                        formatNumStr(5, expData, "pltr#", defValI),
                        formatNumStr(5, expData, "pltln", defValR),
                        formatNumStr(5, expData, "pldr", defValI),
                        formatNumStr(5, expData, "pltsp", defValI),
                        getObjectOr(expData, "pllay", defValC),
                        formatNumStr(5, expData, "pltha", defValR),
                        formatNumStr(5, expData, "plth#", defValI),
                        formatNumStr(5, expData, "plthl", defValR),
                        getObjectOr(expData, "plthm", defValC)));
            }
            // Notes
            if (!getObjectOr(expData, "tr_notes", "").isEmpty()) {
                sbNotesData.append("@NOTES\r\n");
                String notes = getObjectOr(expData, "tr_notes", defValC);
                notes = notes.replaceAll("\\\\r\\\\n", "\r\n");

                // If notes contain newline code, then write directly
                if (notes.contains("\r\n")) {
//                    sbData.append(String.format(" %1$s\r\n", notes));
                    sbNotesData.append(notes);
                } // Otherwise, add newline for every 75-bits charactors
                else {
                    while (notes.length() > 75) {
                        sbNotesData.append(" ").append(notes.substring(0, 75)).append("\r\n");
                        notes = notes.substring(75);
                    }
                    sbNotesData.append(" ").append(notes).append("\r\n");
                }
            }
            sbData.append("\r\n");

            // TREATMENT Section
            sqArr = getDataList(expData, "dssat_sequence", "data");
            evtArr = getDataList(expData, "management", "events");
            adjArr = getObjectOr(expData, "adjustments", new ArrayList());
            ArrayList<HashMap> rootArr = getObjectOr(expData, "dssat_root", new ArrayList());
            ArrayList<HashMap> meOrgArr = getDataList(expData, "dssat_environment_modification", "data");
            ArrayList<HashMap> smOrgArr = getDataList(expData, "dssat_simulation_control", "data");
            String seqId;
            String em;
            String sm;
            boolean isAnyDomeApplied = false;
            LinkedHashMap<String, String> appliedDomes = new LinkedHashMap<String, String>();
            sbData.append("*TREATMENTS                        -------------FACTOR LEVELS------------\r\n");
            sbData.append("@N R O C TNAME.................... CU FL SA IC MP MI MF MR MC MT ME MH SM\r\n");

            // if there is no sequence info, create dummy data
            if (sqArr.isEmpty()) {
                sqArr.add(new HashMap());
            }

            // Set sequence related block info
            for (int i = 0; i < sqArr.size(); i++) {
                sqData = sqArr.get(i);
                seqId = getValueOr(sqData, "seqid", defValBlank);
                em = getValueOr(sqData, "em", defValBlank);
                sm = getValueOr(sqData, "sm", defValBlank);
                if (i < soilArr.size()) {
                    soilData = soilArr.get(i);
                } else if (soilArr.isEmpty()) {
                    soilData = new HashMap();
                } else {
                    soilData = soilArr.get(0);
                }
                if (soilData == null) {
                    soilData = new HashMap();
                }
                if (i < wthArr.size()) {
                    wthData = wthArr.get(i);
                } else if (wthArr.isEmpty()) {
                    wthData = new HashMap();
                } else {
                    wthData = wthArr.get(0);
                }
                if (wthData == null) {
                    wthData = new HashMap();
                }
                HashMap cuData = new HashMap();
                HashMap flData = new HashMap();
                HashMap mpData = new HashMap();
                ArrayList<HashMap> miSubArr = new ArrayList<HashMap>();
                ArrayList<HashMap> mfSubArr = new ArrayList<HashMap>();
                ArrayList<HashMap> mrSubArr = new ArrayList<HashMap>();
                ArrayList<HashMap> mcSubArr = new ArrayList<HashMap>();
                ArrayList<HashMap> mtSubArr = new ArrayList<HashMap>();
                ArrayList<HashMap> meSubArr = new ArrayList<HashMap>();
                ArrayList<HashMap> mhSubArr = new ArrayList<HashMap>();
                HashMap smData = new HashMap();
                boolean is2D = false;
                HashMap rootData;
                // Set exp root info
                if (i < rootArr.size()) {
                    rootData = rootArr.get(i);
                } else {
                    rootData = expData;
                }

                // Applied DOME Info
                String trt_name = getValueOr(sqData, "trt_name", getValueOr(rootData, "trt_name", getValueOr(rootData, "exname", defValC)));
                if (getValueOr(rootData, "dome_applied", "").equals("Y")) {
                    // If it comes with seasonal exname style
                    if (trt_name.matches(".+[^_]__\\d+$")) {
                        trt_name = trt_name.replaceAll("__\\d+$", "_*");
                        if (appliedDomes.get(trt_name + " Field    ") == null) {
                            appliedDomes.put(trt_name + " Field    ", getAppliedDomes(rootData, "field"));
                        }
                        if (appliedDomes.get(trt_name + " Seasonal ") == null) {
                            appliedDomes.put(trt_name + " Seasonal ", getAppliedDomes(rootData, "seasonal"));
                        }
                    } else {
                        appliedDomes.put(trt_name + " Field    ", getAppliedDomes(rootData, "field"));
                        appliedDomes.put(trt_name + " Seasonal ", getAppliedDomes(rootData, "seasonal"));
                    }
                    isAnyDomeApplied = true;
                    copyItem(smData, rootData, "seasonal_dome_applied");
                } else {
                    appliedDomes.put(trt_name, "");
                }

                // Set field info
                copyItem(flData, rootData, "id_field");
                String dssat_wst_id = getValueOr(rootData, "dssat_wst_id", "");
                // Weather data is missing plus dssat_wst_id is available
                if (!dssat_wst_id.isEmpty() && wthData.isEmpty()) {
                    flData.put("wst_id", dssat_wst_id);
                } else {
                    flData.put("wst_id", getWthFileName(rootData));
                }
                copyItem(flData, rootData, "flsl");
                copyItem(flData, rootData, "flob");
                copyItem(flData, rootData, "fl_drntype");
                copyItem(flData, rootData, "fldrd");
                copyItem(flData, rootData, "fldrs");
                copyItem(flData, rootData, "flst");
                if (soilData.get("sltx") != null) {
                    copyItem(flData, soilData, "sltx");
                } else {
                    copyItem(flData, rootData, "sltx");
                }
                copyItem(flData, soilData, "sldp");
                copyItem(flData, rootData, "soil_id");
                copyItem(flData, rootData, "fl_name");
                copyItem(flData, rootData, "fl_lat");
                copyItem(flData, rootData, "fl_long");
                copyItem(flData, rootData, "flele");
                copyItem(flData, rootData, "farea");
                copyItem(flData, rootData, "fllwr");
                copyItem(flData, rootData, "flsla");
                copyItem(flData, rootData, "bdwd");
                copyItem(flData, rootData, "bdht");
                copyItem(flData, rootData, "pmalb");
                copyItem(flData, getObjectOr(rootData, "dssat_info", new HashMap()), "flhst");
                copyItem(flData, getObjectOr(rootData, "dssat_info", new HashMap()), "fhdur");
                // remove the "_trno" in the soil_id when soil analysis is available
                String soilId = getValueOr(flData, "soil_id", "");
                if (soilId.length() > 10 && soilId.matches("\\w+_\\d+") || soilId.length() < 8) {
                    flData.put("soil_id", getSoilID(flData));
                }
                flNum = setSecDataArr(flData, flArr);

                // Set initial condition info
                icNum = setSecDataArr(getObjectOr(rootData, "initial_conditions", new HashMap()), icArr);

                // Set environment modification info
                for (HashMap meOrgArr1 : meOrgArr) {
                    if (em.equals(meOrgArr1.get("em"))) {
                        HashMap tmp = new HashMap();
                        tmp.putAll(meOrgArr1);
                        tmp.remove("em");
                        meSubArr.add(tmp);
                    }
                }
                if (!adjArr.isEmpty()) {
                    ArrayList<HashMap<String, String>> startArr = new ArrayList();
                    ArrayList<HashMap<String, String>> endArr = new ArrayList();
                    String sdat = getValueOr(rootData, "sdat", "");
                    if (sdat.isEmpty()) {
                        sdat = getPdate(result);
                    }
                    final List<String> vars = Arrays.asList(new String[]{"tmax", "tmin", "srad", "wind", "rain", "co2y", "tdew"});
                    final List<String> emVars = Arrays.asList(new String[]{"emmax", "emmin", "emrad", "emwnd", "emrai", "emco2", "emdew"});
                    final List<String> emcVars = Arrays.asList(new String[]{"ecmax", "ecmin", "ecrad", "ecwnd", "ecrai", "ecco2", "ecdew"});

                    for (HashMap adjData : adjArr) {
                        if (getValueOr(adjData, "seqid", defValBlank).equals(seqId)) {
                            String var = getValueOr(adjData, "variable", "");
                            String ecVar;
                            String val = getValueOr(adjData, "value", "");
                            String method = getValueOr(adjData, "method", "");
                            String startDate = getValueOr(adjData, "startdate", sdat);
                            String endDate = getValueOr(adjData, "enddate", "");
                            int idx = vars.indexOf(var);
                            if (idx < 0) {
                                LOG.warn("Found unsupported adjusment variable [" + var + "], will be ignored.");
                                sbError.append("! Warning: Found unsupported adjusment variable [").append(var).append("], will be ignored.");
                                continue;
                            } else {
                                var = emVars.get(idx);
                                ecVar = emcVars.get(idx);
                            }
                            if (method.equals("substitute")) {
                                method = "R";
                            } else if (method.equals("delta")) {
                                if (val.startsWith("-")) {
                                    val = val.substring(1);
                                    method = "S";
                                } else {
                                    method = "A";
                                }
                            } else if (method.equals("multiply")) {
                                method = "M";
                            } else {
                                LOG.warn("Found unsupported adjusment method [" + method + "] for [" + var + "], will be ignored.");
                                sbError.append("! Warning: Found unsupported adjusment method [").append(method).append("] for [").append(var).append("], will be ignored.");
                                continue;
                            }

                            HashMap tmp = DssatCommonInput.getSectionDataWithNocopy(startArr, "date", startDate);
                            if (tmp == null) {
                                tmp = new HashMap();
                                startArr.add(tmp);
                            }
                            tmp.put(var, val);
                            tmp.put(ecVar, method);
                            tmp.put("date", startDate);

                            if (!endDate.isEmpty()) {
                                tmp = DssatCommonInput.getSectionDataWithNocopy(endArr, "date", endDate);
                                if (tmp == null) {
                                    tmp = new HashMap();
                                    endArr.add(tmp);
                                }
                                tmp.put(var, "0");
                                tmp.put(ecVar, "A");
                                tmp.put("date", endDate);
                            }
                        }
                    }

                    meSubArr.addAll(startArr);
                    meSubArr.addAll(endArr);
                }

                // Set soil analysis info
//                ArrayList<HashMap> icSubArr = getDataList(expData, "initial_condition", "soilLayer");
                ArrayList<HashMap> soilLarys = getObjectOr(soilData, "soilLayer", new ArrayList());
//                // If it is stored in the initial condition block
//                if (isSoilAnalysisExist(icSubArr)) {
//                    HashMap saData = new HashMap();
//                    ArrayList<HashMap> saSubArr = new ArrayList<HashMap>();
//                    HashMap saSubData;
//                    for (int i = 0; i < icSubArr.size(); i++) {
//                        saSubData = new HashMap();
//                        copyItem(saSubData, icSubArr.get(i), "sabl", "icbl", false);
//                        copyItem(saSubData, icSubArr.get(i), "sasc", "slsc", false);
//                        saSubArr.add(saSubData);
//                    }
//                    copyItem(saData, soilData, "sadat");
//                    saData.put("soilLayer", saSubArr);
//                    saNum = setSecDataArr(saData, saArr);
//                } else
                // If it is stored in the soil block
                if (isSoilAnalysisExist(soilLarys)) {
                    HashMap saData = new HashMap();
                    ArrayList<HashMap> saSubArr = new ArrayList<HashMap>();
                    HashMap saSubData;
                    for (HashMap soilLary : soilLarys) {
                        saSubData = new HashMap();
                        copyItem(saSubData, soilLary, "sabl", "sllb", false);
                        copyItem(saSubData, soilLary, "saoc", "sloc", false);
                        copyItem(saSubData, soilLary, "sasc", "slsc", false);
                        saSubArr.add(saSubData);
                    }
                    copyItem(saData, soilData, "sadat");
                    saData.put("soilLayer", saSubArr);
                    saNum = setSecDataArr(saData, saArr);
                } else {
                    saNum = 0;
                }

                // Set simulation control info
                for (HashMap smOrgArr1 : smOrgArr) {
                    if (sm.equals(smOrgArr1.get("sm"))) {
                        smData.putAll(smOrgArr1);
                        smData.remove("sm");
                        break;
                    }
                }
//                if (smData.isEmpty()) {
//                    smData.put("fertilizer", mfSubArr);
//                    smData.put("irrigation", miSubArr);
//                    smData.put("planting", mpData);
//                }
                copyItem(smData, rootData, "sdat");
                copyItem(smData, rootData, "dssat_model");
                copyItem(smData, rootData, "water");
                copyItem(smData, rootData, "nitro");
                copyItem(smData, rootData, "mesev");
                copyItem(smData, getObjectOr(wthData, "weather", new HashMap()), "co2y");
                if (rootData.containsKey("bdwd")
                        || rootData.containsKey("bdht")
                        || rootData.containsKey("pmalb")) {
                    smData.put("2d_valid", "Y");
                    is2D = true;
                }

                // Loop all event data
                for (HashMap evtArr1 : evtArr) {
                    evtData = new HashMap();
                    evtData.putAll(evtArr1);
                    // Check if it has same sequence number
                    if (getValueOr(evtData, "seqid", defValBlank).equals(seqId)) {
                        evtData.remove("seqid");

                        // Planting event
                        String eventType = getValueOr(evtData, "event", defValBlank);
                        if (eventType.equals("planting")) {
                            // Set cultivals info
                            copyItem(cuData, evtData, "cul_name");
                            copyItem(cuData, evtData, "crid");
                            copyItem(cuData, evtData, "cul_id");
                            copyItem(cuData, evtData, "dssat_cul_id");
                            copyItem(cuData, evtData, "rm");
                            copyItem(cuData, evtData, "cul_notes");
                            translateTo2BitCrid(cuData);
                            // Set planting info
                            // To make comparision only on the planting information (without crop data), use HashMap to rebuild pure planting map
                            copyItem(mpData, evtData, "date");
                            copyItem(mpData, evtData, "edate");
                            copyItem(mpData, evtData, "plpop");
                            copyItem(mpData, evtData, "plpoe");
                            copyItem(mpData, evtData, "plma");
                            copyItem(mpData, evtData, "plds");
                            copyItem(mpData, evtData, "plrs");
                            copyItem(mpData, evtData, "plrd");
                            copyItem(mpData, evtData, "pldp");
                            copyItem(mpData, evtData, "plmwt");
                            copyItem(mpData, evtData, "page");
                            copyItem(mpData, evtData, "plenv");
                            copyItem(mpData, evtData, "plph");
                            copyItem(mpData, evtData, "plspl");
                            copyItem(mpData, evtData, "pl_name");
                        } // irrigation event
                        else if (eventType.equals("irrigation")) {
                            miSubArr.add(evtData);
                            if (is2D) {
                                evtData.put("2d_valid", "Y");
                            }
                        } // auto irrigation event
                        else if (eventType.equals("auto_irrig")) {
                            smData.put("auto_irrig", evtData);
//                            if (getValueOr(evtData, "iamt", "").trim().equals("")) {
//                                smData.put("auto_irrig_code", "A");
//                            } else {
//                                smData.put("auto_irrig_code", "F");
//                            }
//                            copyItem(smData, evtData, "irmdp");
//                            copyItem(smData, evtData, "irthr");
                        } // fertilizer event
                        else if (eventType.equals("fertilizer")) {
                            mfSubArr.add(evtData);
                        } // organic_matter event
                        else if (eventType.equals("organic_matter")) {   // P.S. change event name to organic-materials; Back to organic_matter again.
                            mrSubArr.add(evtData);
                        } // chemical event
                        else if (eventType.equals("chemical")) {
                            mcSubArr.add(evtData);
                        } // tillage event
                        else if (eventType.equals("tillage")) {
                            mtSubArr.add(evtData);
//                        } // environment_modification event
//                        else if (getValueOr(evtData, "event", "").equals("environment_modification")) {
//                            meSubArr.add(evtData);
                        } // harvest event
                        else if (eventType.equals("harvest")) {
                            mhSubArr.add(evtData);
                            if (!getValueOr(evtData, "date", "").trim().isEmpty()) {
                                smData.put("hadat_valid", "Y");
                            }
//                            copyItem(smData, evtData, "hadat", "date", false);
                        } else {
                        }
                    } else {
                    }
                }

                // Cancel for assume default value handling
//                // If alternative fields are avaiable for fertilizer data
//                if (mfSubArr.isEmpty()) {
//                    if (!getObjectOr(result, "fen_tot", "").equals("")
//                            || !getObjectOr(result, "fep_tot", "").equals("")
//                            || !getObjectOr(result, "fek_tot", "").equals("")) {
//                        mfSubArr.add(new HashMap());
//                    }
//                }
                // Specila handling for fallow experiment
                if (mpData.isEmpty()) {
                    isFallow = true;
                    cuData.put("crid", "FA");
                    cuData.put("dssat_cul_id", "IB0001");
                    cuData.put("cul_name", "Fallow");
                    if (mhSubArr.isEmpty()) {
                        HashMap mhData = new HashMap();
                        copyItem(mhData, rootData, "date", "endat", false);
                        //                    mhData.put("hastg", "GS000");
                        mhSubArr.add(mhData);
                        smData.put("hadat_valid", "Y");
                        //
                    }
                }

                cuNum = setSecDataArr(cuData, cuArr);
                mpNum = setSecDataArr(mpData, mpArr, true);
                if (smData.containsKey("auto_irrig_code")) {
                    miNum = setSecDataArr(miSubArr, miArr); // skip date check for auto_irrig event
                } else {
                    miNum = setSecDataArr(miSubArr, miArr, true);
                }
                mfNum = setSecDataArr(mfSubArr, mfArr, true);
                mrNum = setSecDataArr(mrSubArr, mrArr, true);
                mcNum = setSecDataArr(mcSubArr, mcArr, true);
                mtNum = setSecDataArr(mtSubArr, mtArr, true);
                meNum = setSecDataArr(meSubArr, meArr); // Since old format of EM might exist, skip the check for EM
                mhNum = setSecDataArr(mhSubArr, mhArr, true);
                smNum = setSecSMDataArr(smData, smArr);
//                if (smNum == 0) {
//                    smNum = 1;
//                }
                StringBuilder sbBadEventRrrMsg = new StringBuilder();
                boolean badEventFlg = false;
                if (mpNum < 0) {
                    mpNum = 0;
                    badEventFlg = true;
                    sbBadEventRrrMsg.append("MP ");
                }
                if (miNum < 0) {
                    miNum = 0;
                    badEventFlg = true;
                    sbBadEventRrrMsg.append("MI ");
                }
                if (mfNum < 0) {
                    mfNum = 0;
                    badEventFlg = true;
                    sbBadEventRrrMsg.append("MF ");
                }
                if (mrNum < 0) {
                    mrNum = 0;
                    badEventFlg = true;
                    sbBadEventRrrMsg.append("MR ");
                }
                if (mcNum < 0) {
                    mcNum = 0;
                    badEventFlg = true;
                    sbBadEventRrrMsg.append("MC ");
                }
                if (mtNum < 0) {
                    mtNum = 0;
                    badEventFlg = true;
                    sbBadEventRrrMsg.append("MT ");
                }
                if (meNum < 0) {
                    meNum = 0;
                    badEventFlg = true;
                    sbBadEventRrrMsg.append("ME ");
                }
                if (mhNum < 0) {
                    mhNum = 0;
                    badEventFlg = true;
                    sbBadEventRrrMsg.append("MH ");
                }
                if (badEventFlg) {
                    String tmp = sbBadEventRrrMsg.toString();
                    sbBadEventRrrMsg = new StringBuilder();
                    sbBadEventRrrMsg.append(" ! Bad Event detected for ").append(tmp);
                }

                sbData.append(String.format("%1$-3s%2$1s %3$1s %4$1s %5$-25s %6$2s %7$2s %8$2s %9$2s %10$2s %11$2s %12$2s %13$2s %14$2s %15$2s %16$2s %17$2s %18$2s%19$s\r\n",
                        String.format("%2s", getValueOr(sqData, "trno", "1")), // For 3-bit treatment number
                        getValueOr(sqData, "sq", "1"), // P.S. default value here is based on document DSSAT vol2.pdf
                        getValueOr(sqData, "op", "1"),
                        getValueOr(sqData, "co", "0"),
                        formatStr(25, sqData, "trt_name", getValueOr(rootData, "trt_name", getValueOr(rootData, "exname", defValC))),
                        cuNum, //getObjectOr(data, "ge", defValI).toString(), 
                        flNum, //getObjectOr(data, "fl", defValI).toString(), 
                        saNum, //getObjectOr(data, "sa", defValI).toString(),
                        icNum, //getObjectOr(data, "ic", defValI).toString(),
                        mpNum, //getObjectOr(data, "pl", defValI).toString(),
                        miNum, //getObjectOr(data, "ir", defValI).toString(),
                        mfNum, //getObjectOr(data, "fe", defValI).toString(),
                        mrNum, //getObjectOr(data, "om", defValI).toString(),
                        mcNum, //getObjectOr(data, "ch", defValI).toString(),
                        mtNum, //getObjectOr(data, "ti", defValI).toString(),
                        meNum, //getObjectOr(data, "em", defValI).toString(),
                        mhNum, //getObjectOr(data, "ha", defValI).toString(),
                        smNum, // 1
                        sbBadEventRrrMsg.toString()));

            }
            sbData.append("\r\n");

            // CULTIVARS Section
            if (!cuArr.isEmpty()) {
                sbData.append("*CULTIVARS\r\n");
                sbData.append("@C CR INGENO CNAME\r\n");

                for (int idx = 0; idx < cuArr.size(); idx++) {
                    HashMap secData = cuArr.get(idx);
//                    String cul_id = defValC;
                    String crid = getValueOr(secData, "crid", "");
                    // Checl if necessary data is missing
                    if (crid.isEmpty()) {
                        sbError.append("! Warning: Incompleted record because missing data : [crid]\r\n");
//                    } else {
//                        // Set cultivar id a default value deponds on the crop id
//                        if (crid.equals("MZ")) {
//                            cul_id = "990002";
//                        } else {
//                            cul_id = "999999";
//                        }
//                    }
//                    if (getObjectOr(secData, "cul_id", "").equals("")) {
//                        sbError.append("! Warning: Incompleted record because missing data : [cul_id], and will use default value '").append(cul_id).append("'\r\n");
                    }
                    sbData.append(String.format("%1$-3s%2$-2s %3$-6s %4$s\r\n",
                            idx + 1,
                            formatStr(2, secData, "crid", defValBlank), // P.S. if missing, default value use blank string
                            formatStr(6, secData, "dssat_cul_id", getValueOr(secData, "cul_id", defValC)), // P.S. Set default value which is deponds on crid(Cancelled)
                            getValueOr(secData, "cul_name", defValC)));

                    if (!getValueOr(secData, "rm", "").isEmpty() || !getValueOr(secData, "cul_notes", "").isEmpty()) {
                        if (sbNotesData.toString().isEmpty()) {
                            sbNotesData.append("@NOTES\r\n");
                        }
                        sbNotesData.append(" Cultivar Additional Info\r\n");
                        sbNotesData.append(" C   RM CNAME            CUL_NOTES\r\n");
                        sbNotesData.append(String.format("%1$-3s%2$4s %3$s\r\n",
                                idx + 1,
                                getValueOr(secData, "rm", defValC),
                                getValueOr(secData, "cul_notes", defValC)));
                    }

                }
                sbData.append("\r\n");
            } else if (!isFallow) {
                sbError.append("! Warning: There is no cultivar data in the experiment.\r\n");
            }

            // FIELDS Section
            if (!flArr.isEmpty()) {

                boolean is2D = false;
                for (HashMap secData : flArr) {
                    if (secData.containsKey("bdwd")
                            || secData.containsKey("bdht")
                            || secData.containsKey("pmalb")) {
                        is2D = true;
                        break;
                    }
                }

                sbData.append("*FIELDS\r\n");
                if (is2D) {
                    sbData.append("@L ID_FIELD WSTA....  FLSA  FLOB  FLDT  FLDD  FLDS  FLST SLTX  SLDP  ID_SOIL     BDWD  BDHT PMALB FLNAME\r\n");
                } else {
                    sbData.append("@L ID_FIELD WSTA....  FLSA  FLOB  FLDT  FLDD  FLDS  FLST SLTX  SLDP  ID_SOIL    FLNAME\r\n");
                }
                eventPart2 = new StringBuilder();
                eventPart2.append("@L ...........XCRD ...........YCRD .....ELEV .............AREA .SLEN .FLWR .SLAS FLHST FHDUR\r\n");

                for (int idx = 0; idx < flArr.size(); idx++) {
                    HashMap secData = flArr.get(idx);
                    // Check if the necessary is missing
                    if (getObjectOr(secData, "wst_id", "").isEmpty()) {
                        sbError.append("! Warning: Incompleted record because missing data : [wst_id]\r\n");
                    }
                    String soil_id = getValueOr(secData, "soil_id", defValC);
                    if (soil_id.isEmpty()) {
                        sbError.append("! Warning: Incompleted record because missing data : [soil_id]\r\n");
                    } else if (soil_id.length() > 10) {
                        sbError.append("! Warning: Oversized data : [soil_id] ").append(soil_id).append("\r\n");
                    }
                    if (is2D) {
                        sbData.append(String.format("%1$-3s%2$-8s %3$-8s %4$5s %5$5s %6$-5s %7$5s %8$5s %9$-5s %10$-5s%11$5s  %12$-10s %13$5s %14$5s %15$5s %16$s\r\n", // P.S. change length definition to match current way
                                idx + 1,
                                formatStr(8, secData, "id_field", defValC),
                                formatStr(8, secData, "wst_id", defValC),
                                formatStr(4, secData, "flsl", defValC),
                                formatNumStr(5, secData, "flob", defValR),
                                formatStr(5, secData, "fl_drntype", defValC),
                                formatNumStr(5, secData, "fldrd", defValR),
                                formatNumStr(5, secData, "fldrs", defValR),
                                formatStr(5, secData, "flst", defValC),
                                formatStr(5, transSltx(getValueOr(secData, "sltx", defValC)), "sltx"),
                                formatNumStr(5, secData, "sldp", defValR),
                                soil_id,
                                formatNumStr(5, secData, "bdwd", defValR),
                                formatNumStr(5, secData, "bdht", defValR),
                                formatNumStr(5, secData, "pmalb", defValR),
                                getValueOr(secData, "fl_name", defValC)));
                    } else {
                        sbData.append(String.format("%1$-3s%2$-8s %3$-8s %4$5s %5$5s %6$-5s %7$5s %8$5s %9$-5s %10$-5s%11$5s  %12$-10s %13$s\r\n", // P.S. change length definition to match current way
                                idx + 1,
                                formatStr(8, secData, "id_field", defValC),
                                formatStr(8, secData, "wst_id", defValC),
                                formatStr(4, secData, "flsl", defValC),
                                formatNumStr(5, secData, "flob", defValR),
                                formatStr(5, secData, "fl_drntype", defValC),
                                formatNumStr(5, secData, "fldrd", defValR),
                                formatNumStr(5, secData, "fldrs", defValR),
                                formatStr(5, secData, "flst", defValC),
                                formatStr(5, transSltx(getValueOr(secData, "sltx", defValC)), "sltx"),
                                formatNumStr(5, secData, "sldp", defValR),
                                soil_id,
                                getValueOr(secData, "fl_name", defValC)));
                    }

                    eventPart2.append(String.format("%1$-3s%2$15s %3$15s %4$9s %5$17s %6$5s %7$5s %8$5s %9$5s %10$5s\r\n",
                            idx + 1,
                            formatNumStr(15, secData, "fl_long", defValR),
                            formatNumStr(15, secData, "fl_lat", defValR),
                            formatNumStr(9, secData, "flele", defValR),
                            formatNumStr(17, secData, "farea", defValR),
                            "-99", // P.S. SLEN keeps -99
                            formatNumStr(5, secData, "fllwr", defValR),
                            formatNumStr(5, secData, "flsla", defValR),
                            formatStr(5, secData, "flhst", defValC),
                            formatNumStr(5, secData, "fhdur", defValR)));
                }
                sbData.append(eventPart2.toString()).append("\r\n");

            } else {
                sbError.append("! Warning: There is no field data in the experiment.\r\n");
            }

            // SOIL ANALYSIS Section
            if (!saArr.isEmpty()) {
                sbData.append("*SOIL ANALYSIS\r\n");

                for (int idx = 0; idx < saArr.size(); idx++) {

                    HashMap secData = (HashMap) saArr.get(idx);
                    sbData.append("@A SADAT  SMHB  SMPX  SMKE  SANAME\r\n");
                    sbData.append(String.format("%1$-3s%2$5s %3$5s %4$5s %5$5s  %6$s\r\n",
                            idx + 1,
                            formatDateStr(getValueOr(secData, "sadat", defValD)),
                            getValueOr(secData, "samhb", defValC),
                            getValueOr(secData, "sampx", defValC),
                            getValueOr(secData, "samke", defValC),
                            getValueOr(secData, "sa_name", defValC)));

                    ArrayList<HashMap> subDataArr = getObjectOr(secData, "soilLayer", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        sbData.append("@A  SABL  SADM  SAOC  SANI SAPHW SAPHB  SAPX  SAKE  SASC\r\n");
                    }
                    for (HashMap subData : subDataArr) {
                        sbData.append(String.format("%1$-3s%2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s\r\n",
                                idx + 1,
                                formatNumStr(5, subData, "sabl", defValR),
                                formatNumStr(5, subData, "sabdm", defValR),
                                formatNumStr(5, subData, "saoc", defValR),
                                formatNumStr(5, subData, "sani", defValR),
                                formatNumStr(5, subData, "saphw", defValR),
                                formatNumStr(5, subData, "saphb", defValR),
                                formatNumStr(5, subData, "sapx", defValR),
                                formatNumStr(5, subData, "sake", defValR),
                                formatNumStr(5, subData, "sasc", defValR)));
                    }

                }
                sbData.append("\r\n");
            }

            // INITIAL CONDITIONS Section
            if (!icArr.isEmpty()) {
                sbData.append("*INITIAL CONDITIONS\r\n");

                for (int idx = 0; idx < icArr.size(); idx++) {

                    HashMap secData = icArr.get(idx);
                    String brokenMark = "";
                    if (getValueOr(secData, "icdat", defValD).equals(defValD)) {
                        brokenMark = "!";
                    }
                    sbData.append(brokenMark).append("@C   PCR ICDAT  ICRT  ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME\r\n");
                    sbData.append(brokenMark).append(String.format("%1$-3s%2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$s\r\n",
                            idx + 1,
                            translateTo2BitCrid(secData, "icpcr", defValC),
                            formatDateStr(getValueOr(secData, "icdat", getPdate(result))),
                            formatNumStr(5, secData, "icrt", defValR),
                            formatNumStr(5, secData, "icnd", defValR),
                            formatNumStr(5, secData, "icrz#", defValR),
                            formatNumStr(5, secData, "icrze", defValR),
                            formatNumStr(5, secData, "icwt", defValR),
                            formatNumStr(5, secData, "icrag", defValR),
                            formatNumStr(5, secData, "icrn", defValR),
                            formatNumStr(5, secData, "icrp", defValR),
                            formatNumStr(5, secData, "icrip", defValR),
                            formatNumStr(5, secData, "icrdp", defValR),
                            getValueOr(secData, "ic_name", defValC)));

                    ArrayList<HashMap> subDataArr = getObjectOr(secData, "soilLayer", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        sbData.append(brokenMark).append("@C  ICBL  SH2O  SNH4  SNO3\r\n");
                    }
                    for (HashMap subData : subDataArr) {
                        sbData.append(brokenMark).append(String.format("%1$-3s%2$5s %3$5s %4$5s %5$5s\r\n",
                                idx + 1,
                                formatNumStr(5, subData, "icbl", defValR),
                                formatNumStr(5, subData, "ich2o", defValR),
                                formatNumStr(5, subData, "icnh4", defValR),
                                formatNumStr(5, subData, "icno3", defValR)));
                    }
                }
                sbData.append("\r\n");
            }

            // PLANTING DETAILS Section
            if (!mpArr.isEmpty()) {
                sbData.append("*PLANTING DETAILS\r\n");
                sbData.append("@P PDATE EDATE  PPOP  PPOE  PLME  PLDS  PLRS  PLRD  PLDP  PLWT  PAGE  PENV  PLPH  SPRL                        PLNAME\r\n");

                for (int idx = 0; idx < mpArr.size(); idx++) {

                    HashMap secData = mpArr.get(idx);
                    // Check if necessary data is missing
                    String pdate = getValueOr(secData, "date", "");
                    if (pdate.isEmpty()) {
                        sbError.append("! Warning: Incompleted record because missing data : [pdate]\r\n");
                    } else if (formatDateStr(pdate).equals(defValD)) {
                        sbError.append("! Warning: Incompleted record because variable [pdate] with invalid value [").append(pdate).append("]\r\n");
                    }
                    if (getValueOr(secData, "plpop", getValueOr(secData, "plpoe", "")).isEmpty()) {
                        sbError.append("! Warning: Incompleted record because missing data : [plpop] and [plpoe]\r\n");
                    }
                    if (getValueOr(secData, "plrs", "").isEmpty()) {
                        sbError.append("! Warning: Incompleted record because missing data : [plrs]\r\n");
                    }
//                    if (getValueOr(secData, "plma", "").equals("")) {
//                        sbError.append("! Warning: missing data : [plma], and will automatically use default value 'S'\r\n");
//                    }
//                    if (getValueOr(secData, "plds", "").equals("")) {
//                        sbError.append("! Warning: missing data : [plds], and will automatically use default value 'R'\r\n");
//                    }
//                    if (getValueOr(secData, "pldp", "").equals("")) {
//                        sbError.append("! Warning: missing data : [pldp], and will automatically use default value '7'\r\n");
//                    }

                    // mm -> cm
                    String pldp = getValueOr(secData, "pldp", "");
                    if (!pldp.isEmpty()) {
                        try {
                            BigDecimal pldpBD = new BigDecimal(pldp);
                            pldpBD = pldpBD.divide(new BigDecimal("10"));
                            secData.put("pldp", pldpBD.toString());
                        } catch (NumberFormatException e) {
                        }
                    }

                    sbData.append(String.format("%1$-3s%2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s %15$5s                        %16$s\r\n",
                            idx + 1,
                            formatDateStr(getValueOr(secData, "date", defValD)),
                            formatDateStr(getValueOr(secData, "edate", defValD)),
                            formatNumStr(5, secData, "plpop", getValueOr(secData, "plpoe", defValR)),
                            formatNumStr(5, secData, "plpoe", getValueOr(secData, "plpop", defValR)),
                            getValueOr(secData, "plma", defValC), // P.S. Set default value as "S"(Cancelled)
                            getValueOr(secData, "plds", defValC), // P.S. Set default value as "R"(Cancelled)
                            formatNumStr(5, secData, "plrs", defValR),
                            formatNumStr(5, secData, "plrd", defValR),
                            formatNumStr(5, secData, "pldp", defValR), // P.S. Set default value as "7"(Cancelled)
                            formatNumStr(5, secData, "plmwt", defValR),
                            formatNumStr(5, secData, "page", defValR),
                            formatNumStr(5, secData, "plenv", defValR),
                            formatNumStr(5, secData, "plph", defValR),
                            formatNumStr(5, secData, "plspl", defValR),
                            getValueOr(secData, "pl_name", defValC)));

                }
                sbData.append("\r\n");
            } else if (!isFallow) {
                sbError.append("! Warning: There is no plainting data in the experiment.\r\n");
            }

            // IRRIGATION AND WATER MANAGEMENT Section
            if (!miArr.isEmpty()) {
                sbData.append("*IRRIGATION AND WATER MANAGEMENT\r\n");

                for (int idx = 0; idx < miArr.size(); idx++) {

//                    secData = (ArrayList) miArr.get(idx);
                    ArrayList<HashMap> subDataArr = miArr.get(idx);
                    boolean isDrip = false;
                    ArrayList<HashMap> dripLines = new ArrayList();
                    ArrayList<Integer> dripLineNos = new ArrayList();
                    
                    for (HashMap subData : subDataArr) {
                        if (getValueOr(subData, "irop", defValC).equals("IR005") &&
                                getValueOr(subData, "2d_valid", defValC).equals("Y")) {
                            isDrip = true;
                            HashMap dripLine = new HashMap();
                            copyItem(dripLine, subData, "irspc");
                            copyItem(dripLine, subData, "irofs");
                            copyItem(dripLine, subData, "irdep");
                            if (dripLines.contains(dripLine)) {
                                dripLineNos.add(dripLines.indexOf(dripLine) + 1);
                            } else {
                                dripLines.add(dripLine);
                                dripLineNos.add(dripLines.size());
                            }
                        } else {
                            dripLineNos.add(-99);
                        }
                    }
                    HashMap subData;
                    if (!subDataArr.isEmpty()) {
                        subData = subDataArr.get(0);
                    } else {
                        subData = new HashMap();
                    }
                    if (isDrip) {
                        sbData.append("@I  EFIR  IDEP  ITHR  IEPT  IOFF  IAME  IAMT IRNAME\r\n");
                        sbData.append(String.format("%1$-3s%2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$s\r\n",
                                idx + 1,
                                formatNumStr(5, subData, "ireff", defValR),
                                formatNumStr(5, subData, "irmdp", defValR),
                                formatNumStr(5, subData, "irthr", defValR),
                                formatNumStr(5, subData, "irept", defValR),
                                getValueOr(subData, "irstg", defValC),
                                getValueOr(subData, "iame", defValC),
                                formatNumStr(5, subData, "iamt", defValR),
                                getValueOr(subData, "ir_name", defValC)));
                        sbData.append("@I  IRLN IRSPC IROFS IRDEP\r\n");
                        int drpLnIdx = 1;
                        for (HashMap dripLine : dripLines) {
                            sbData.append(String.format("%1$-3s%2$5s %3$5s %4$5s %5$5s\r\n",
                                    idx + 1,
                                    drpLnIdx,
                                    formatNumStr(5, dripLine, "irspc", defValR),
                                    formatNumStr(5, dripLine, "irofs", defValR),
                                    formatNumStr(5, dripLine, "irdep", defValR)));
                            drpLnIdx++;
                        }
                    } else {
                        sbData.append("@I  EFIR  IDEP  ITHR  IEPT  IOFF  IAME  IAMT IRNAME\r\n");
                        sbData.append(String.format("%1$-3s%2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$s\r\n",
                                idx + 1,
                                formatNumStr(5, subData, "ireff", defValR),
                                formatNumStr(5, subData, "irmdp", defValR),
                                formatNumStr(5, subData, "irthr", defValR),
                                formatNumStr(5, subData, "irept", defValR),
                                getValueOr(subData, "irstg", defValC),
                                getValueOr(subData, "iame", defValC),
                                formatNumStr(5, subData, "iamt", defValR),
                                getValueOr(subData, "ir_name", defValC)));
                    }

                    if (!subDataArr.isEmpty()) {
                        if (isDrip) {
                            sbData.append("@I IDATE  IROP IRVAL IRSTR IRDUR  IRLN\r\n");
                        } else {
                            sbData.append("@I IDATE  IROP IRVAL\r\n");
                        }
                    }
                    int drpLnIdx = 0;
                    for (HashMap subDataArr1 : subDataArr) {
                        subData = subDataArr1;
                        String brokenMark = "";
                        if (getValueOr(subData, "date", defValD).equals(defValD)) {
                            brokenMark = "!";
                        }
                        if (isDrip) {
                            sbData.append(brokenMark)
                                    .append(String.format("%1$-3s%2$5s %3$-5s %4$5s %5$5s %6$5s %7$5s\r\n",
                                                    idx + 1,
                                                    formatDateStr(getValueOr(subData, "date", defValD)), // P.S. idate -> date
                                                    getValueOr(subData, "irop", defValC),
                                                    formatNumStr(5, subData, "irval", defValR),
                                                    getValueOr(subData, "irstr", defValC),
                                                    formatNumStr(5, subData, "irdur", defValR),
                                                    dripLineNos.get(drpLnIdx)));
                            drpLnIdx++;
                        } else {
                            sbData.append(brokenMark).append(String.format("%1$-3s%2$5s %3$-5s %4$5s\r\n",
                                    idx + 1,
                                    formatDateStr(getValueOr(subData, "date", defValD)), // P.S. idate -> date
                                    getValueOr(subData, "irop", defValC),
                                    formatNumStr(5, subData, "irval", defValR)));
                        }

                    }
                }
                sbData.append("\r\n");
            }

            // FERTILIZERS (INORGANIC) Section
            if (!mfArr.isEmpty()) {
                sbData.append("*FERTILIZERS (INORGANIC)\r\n");
                sbData.append("@F FDATE  FMCD  FACD  FDEP  FAMN  FAMP  FAMK  FAMC  FAMO  FOCD FERNAME\r\n");
//                String fen_tot = getValueOr(result, "fen_tot", defValR);
//                String fep_tot = getValueOr(result, "fep_tot", defValR);
//                String fek_tot = getValueOr(result, "fek_tot", defValR);
//                String pdate = getPdate(result);
//                if (pdate.equals("")) {
//                    pdate = defValD;
//                }

                for (int idx = 0; idx < mfArr.size(); idx++) {
                    ArrayList<HashMap> secDataArr = mfArr.get(idx);

                    for (HashMap secData : secDataArr) {
//                        if (getValueOr(secData, "fdate", "").equals("")) {
//                            sbError.append("! Warning: missing data : [fdate], and will automatically use planting value '").append(pdate).append("'\r\n");
//                        }
//                        if (getValueOr(secData, "fecd", "").equals("")) {
//                            sbError.append("! Warning: missing data : [fecd], and will automatically use default value 'FE001'\r\n");
//                        }
//                        if (getValueOr(secData, "feacd", "").equals("")) {
//                            sbError.append("! Warning: missing data : [feacd], and will automatically use default value 'AP002'\r\n");
//                        }
//                        if (getValueOr(secData, "fedep", "").equals("")) {
//                            sbError.append("! Warning: missing data : [fedep], and will automatically use default value '10'\r\n");
//                        }
//                        if (getValueOr(secData, "feamn", "").equals("")) {
//                            sbError.append("! Warning: missing data : [feamn], and will automatically use the value of FEN_TOT, '").append(fen_tot).append("'\r\n");
//                        }
//                        if (getValueOr(secData, "feamp", "").equals("")) {
//                            sbError.append("! Warning: missing data : [feamp], and will automatically use the value of FEP_TOT, '").append(fep_tot).append("'\r\n");
//                        }
//                        if (getValueOr(secData, "feamk", "").equals("")) {
//                            sbError.append("! Warning: missing data : [feamk], and will automatically use the value of FEK_TOT, '").append(fek_tot).append("'\r\n");
//                        }
                        String brokenMark = "";
                        if (getValueOr(secData, "date", defValD).equals(defValD)) {
                            brokenMark = "!";
                        }
                        sbData.append(brokenMark).append(String.format("%1$-3s%2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$s\r\n",
                                idx + 1,
                                formatDateStr(getValueOr(secData, "date", defValD)), // P.S. fdate -> date
                                getValueOr(secData, "fecd", defValC), // P.S. Set default value as "FE005"(Cancelled)
                                getValueOr(secData, "feacd", defValC), // P.S. Set default value as "AP002"(Cancelled)
                                formatNumStr(5, secData, "fedep", defValR), // P.S. Set default value as "10"(Cancelled)
                                formatNumStr(5, secData, "feamn", "0"), // P.S. Set default value to use 0 instead of -99
                                formatNumStr(5, secData, "feamp", defValR), // P.S. Set default value to use the value of FEP_TOT in meta data(Cancelled)
                                formatNumStr(5, secData, "feamk", defValR), // P.S. Set default value to use the value of FEK_TOT in meta data(Cancelled)
                                formatNumStr(5, secData, "feamc", defValR),
                                formatNumStr(5, secData, "feamo", defValR),
                                getValueOr(secData, "feocd", defValC),
                                getValueOr(secData, "fe_name", defValC)));
                    }
                }
                sbData.append("\r\n");
            }

            // RESIDUES AND ORGANIC FERTILIZER Section
            if (!mrArr.isEmpty()) {
                sbData.append("*RESIDUES AND ORGANIC FERTILIZER\r\n");
                sbData.append("@R RDATE  RCOD  RAMT  RESN  RESP  RESK  RINP  RDEP  RMET RENAME\r\n");

                for (int idx = 0; idx < mrArr.size(); idx++) {
                    ArrayList<HashMap> secDataArr = mrArr.get(idx);

                    for (HashMap secData : secDataArr) {
                        String brokenMark = "";
                        if (getValueOr(secData, "date", defValD).equals(defValD)) {
                            brokenMark = "!";
                        }
                        sbData.append(brokenMark).append(String.format("%1$-3s%2$5s %3$-5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$s\r\n",
                                idx + 1,
                                formatDateStr(getValueOr(secData, "date", defValD)), // P.S. omdat -> date
                                getValueOr(secData, "omcd", defValC),
                                formatNumStr(5, secData, "omamt", defValR),
                                formatNumStr(5, secData, "omn%", defValR),
                                formatNumStr(5, secData, "omp%", defValR),
                                formatNumStr(5, secData, "omk%", defValR),
                                formatNumStr(5, secData, "ominp", defValR),
                                formatNumStr(5, secData, "omdep", defValR),
                                formatNumStr(5, secData, "omacd", defValR),
                                getValueOr(secData, "om_name", defValC)));
                    }
                }
                sbData.append("\r\n");
            }

            // CHEMICAL APPLICATIONS Section
            if (!mcArr.isEmpty()) {
                sbData.append("*CHEMICAL APPLICATIONS\r\n");
                sbData.append("@C CDATE CHCOD CHAMT  CHME CHDEP   CHT..CHNAME\r\n");

                for (int idx = 0; idx < mcArr.size(); idx++) {
                    ArrayList<HashMap> secDataArr = mcArr.get(idx);

                    for (HashMap secData : secDataArr) {
                        String brokenMark = "";
                        if (getValueOr(secData, "date", defValD).equals(defValD)) {
                            brokenMark = "!";
                        }
                        sbData.append(brokenMark).append(String.format("%1$-3s%2$5s %3$5s %4$5s %5$5s %6$5s %7$5s  %8$s\r\n",
                                idx + 1,
                                formatDateStr(getValueOr(secData, "date", defValD)), // P.S. cdate -> date
                                getValueOr(secData, "chcd", defValC),
                                formatNumStr(5, secData, "chamt", defValR),
                                getValueOr(secData, "chacd", defValC),
                                getValueOr(secData, "chdep", defValC),
                                getValueOr(secData, "ch_targets", defValC),
                                getValueOr(secData, "ch_name", defValC)));
                    }
                }
                sbData.append("\r\n");
            }

            // TILLAGE Section
            if (!mtArr.isEmpty()) {
                sbData.append("*TILLAGE AND ROTATIONS\r\n");
                sbData.append("@T TDATE TIMPL  TDEP TNAME\r\n");

                for (int idx = 0; idx < mtArr.size(); idx++) {
                    ArrayList<HashMap> secDataArr = mtArr.get(idx);

                    for (HashMap secData : secDataArr) {
                        String brokenMark = "";
                        if (getValueOr(secData, "date", defValD).equals(defValD)) {
                            brokenMark = "!";
                        }
                        sbData.append(brokenMark).append(String.format("%1$-3s%2$5s %3$5s %4$5s %5$s\r\n",
                                idx + 1,
                                formatDateStr(getValueOr(secData, "date", defValD)), // P.S. tdate -> date
                                getValueOr(secData, "tiimp", defValC),
                                formatNumStr(5, secData, "tidep", defValR),
                                getValueOr(secData, "ti_name", defValC)));
                    }
                }
                sbData.append("\r\n");
            }

            // ENVIRONMENT MODIFICATIONS Section
            if (!meArr.isEmpty()) {
                sbData.append("*ENVIRONMENT MODIFICATIONS\r\n");
                sbData.append("@E ODATE EDAY  ERAD  EMAX  EMIN  ERAIN ECO2  EDEW  EWIND ENVNAME\r\n");

                for (int idx = 0, cnt = 1; idx < meArr.size(); idx++) {
                    ArrayList<HashMap> secDataArr = meArr.get(idx);
                    for (HashMap secData : secDataArr) {
                        if (secData.containsKey("em_data")) {
                            sbData.append(String.format("%1$-3s%2$s\r\n",
                                    cnt,
                                    getValueOr(secData, "em_data", "").trim()));
                        } else {
                            String brokenMark = "";
                            if (getValueOr(secData, "date", defValD).equals(defValD)) {
                                brokenMark = "!";
                            }
                            sbData.append(brokenMark).append(String.format("%1$-3s%2$5s %3$-1s%4$4s %5$-1s%6$4s %7$-1s%8$4s %9$-1s%10$4s %11$-1s%12$4s %13$-1s%14$4s %15$-1s%16$4s %17$-1s%18$4s %19$s\r\n",
                                    idx + 1,
                                    formatDateStr(getValueOr(secData, "date", defValD)), // P.S. emday -> date
                                    getValueOr(secData, "ecdyl", "A"),
                                    formatNumStr(4, secData, "emdyl", "0"),
                                    getValueOr(secData, "ecrad", "A"),
                                    formatNumStr(4, secData, "emrad", "0"),
                                    getValueOr(secData, "ecmax", "A"),
                                    formatNumStr(4, secData, "emmax", "0"),
                                    getValueOr(secData, "ecmin", "A"),
                                    formatNumStr(4, secData, "emmin", "0"),
                                    getValueOr(secData, "ecrai", "A"),
                                    formatNumStr(4, secData, "emrai", "0"),
                                    getValueOr(secData, "ecco2", "A"),
                                    formatNumStr(4, secData, "emco2", "0"),
                                    getValueOr(secData, "ecdew", "A"),
                                    formatNumStr(4, secData, "emdew", "0"),
                                    getValueOr(secData, "ecwnd", "A"),
                                    formatNumStr(4, secData, "emwnd", "0"),
                                    getValueOr(secData, "em_name", defValC)));
                        }

                    }
                }
                sbData.append("\r\n");
            }

            // HARVEST DETAILS Section
            if (!mhArr.isEmpty()) {
                sbData.append("*HARVEST DETAILS\r\n");
                sbData.append("@H HDATE  HSTG  HCOM HSIZE   HPC  HBPC HNAME\r\n");

                for (int idx = 0; idx < mhArr.size(); idx++) {
                    ArrayList<HashMap> secDataArr = mhArr.get(idx);

                    for (HashMap secData : secDataArr) {
                        String brokenMark = "";
                        if (getValueOr(secData, "date", defValD).equals(defValD)) {
                            brokenMark = "!";
                        }
                        sbData.append(brokenMark).append(String.format("%1$-3s%2$5s %3$-5s %4$-5s %5$-5s %6$5s %7$5s %8$s\r\n",
                                idx + 1,
                                formatDateStr(getValueOr(secData, "date", defValD)), // P.S. hdate -> date
                                getValueOr(secData, "hastg", defValC),
                                getValueOr(secData, "hacom", defValC),
                                getValueOr(secData, "hasiz", defValC),
                                formatNumStr(5, secData, "hap%", defValR),
                                formatNumStr(5, secData, "hab%", defValR),
                                getValueOr(secData, "ha_name", defValC)));
                    }
                }
                sbData.append("\r\n");
            }

            // SIMULATION CONTROLS and AUTOMATIC MANAGEMENT Section
            if (!smArr.isEmpty()) {

                // Loop all the simulation control records
                sbData.append("*SIMULATION CONTROLS\r\n");
                for (int idx = 0; idx < smArr.size(); idx++) {
                    HashMap secData = smArr.get(idx);
                    sbData.append(createSMMAStr(idx + 1, secData));
                }

            } else {
                sbData.append("*SIMULATION CONTROLS\r\n");
                sbData.append(createSMMAStr(1, new HashMap()));
            }

            // DOME Info Section
            if (isAnyDomeApplied) {
                sbDomeData.append("! APPLIED DOME INFO\r\n");
                for (String exname : appliedDomes.keySet()) {
                    if (!getValueOr(appliedDomes, exname, "").isEmpty()) {
                        sbDomeData.append("! ").append(exname).append("\t");
                        sbDomeData.append(appliedDomes.get(exname));
                        sbDomeData.append("\r\n");
                    }
                }
                sbDomeData.append("\r\n");
            }

            // Output finish
            bwX.write(sbError.toString());
            bwX.write(sbDomeData.toString());
            bwX.write(sbGenData.toString());
            bwX.write(sbNotesData.toString());
            bwX.write(sbData.toString());
            bwX.close();
        } catch (IOException e) {
            LOG.error(DssatCommonOutput.getStackTrace(e));
        }
    }

    /**
     * Create string of Simulation Control and Automatic Management Section
     *
     * @param smid simulation index number
     * @param trData date holder for one treatment data
     * @return date string with format of "yyddd"
     */
    private String createSMMAStr(int smid, HashMap trData) {

        StringBuilder sb = new StringBuilder();
        String nitro = getValueOr(trData, "nitro", "Y").trim();
        String water = getValueOr(trData, "water", "Y").trim();
        String mesev = getValueOr(trData, "mesev", "S").trim();
        String co2 = "M";
        String harOpt = "M";
        String hydro = "R";
        boolean is2D = false;
        String sdate;
        String irrigMgn;
        HashMap<String, String> autoIrrEvt = getObjectOr(trData, "auto_irrig", new HashMap());
        String sm = String.format("%2d", smid);
//        ArrayList<HashMap> dataArr;
        HashMap subData;
        String vbose = "N";

        // Detect the irrigation control code
        String iamt = getValueOr(autoIrrEvt, "iamt", "").trim();
        String irmdp = getValueOr(autoIrrEvt, "irmdp", "").trim();
        String irthr = getValueOr(autoIrrEvt, "irthr", "").trim();
        if (irmdp.isEmpty() || irthr.isEmpty()) {
            irrigMgn = "R";
            if (!irmdp.isEmpty() || !irthr.isEmpty()) {
                LOG.warn("Detect incompleted auto-irrigation event. Please provide IRMDP and IRTHR at least.");
                sbError.append("! Warning: Detect incompleted auto-irrigation event. Please provide IRMDP and IRTHR at least.");
            }
        } else if (!iamt.isEmpty()) {
            irrigMgn = "F";
        } else {
            irrigMgn = "A";
        }

//        // Check if the meta data of fertilizer is not "N" ("Y" or null)
//        if (!getValueOr(expData, "fertilizer", "").equals("N")) {
//
//            // Check if necessary data is missing in all the event records
//            // P.S. rule changed since all the necessary data has a default value for it
//            dataArr = getObjectOr(trData, "fertilizer", new ArrayList());
//            if (dataArr.isEmpty()) {
//                nitro = "N";
//            }
////            for (int i = 0; i < dataArr.size(); i++) {
////                subData = dataArr.get(i);
////                if (getValueOr(subData, "date", "").equals("")
////                        || getValueOr(subData, "fecd", "").equals("")
////                        || getValueOr(subData, "feacd", "").equals("")
////                        || getValueOr(subData, "feamn", "").equals("")) {
////                    nitro = "N";
////                    break;
////                }
////            }
//        }
//        // Check if the meta data of irrigation is not "N" ("Y" or null)
//        if (!getValueOr(expData, "irrigation", "").equals("N")) {
//
//            // Check if necessary data is missing in all the event records
//            dataArr = getObjectOr(trData, "irrigation", new ArrayList());
//            for (int i = 0; i < dataArr.size(); i++) {
//                subData = dataArr.get(i);
//                if (getValueOr(subData, "date", "").equals("")
//                        || getValueOr(subData, "irval", "").equals("")) {
//                    water = "N";
//                    break;
//                }
//            }
//        }
        // Check if CO2Y value is provided and the value is positive, then set CO2 switch to W
        String co2y = getValueOr(trData, "co2y", "").trim();
        if (!co2y.isEmpty() && !co2y.startsWith("-")) {
            co2 = "W";
        }

        sdate = getValueOr(trData, "sdat", "");
        if (sdate.isEmpty()) {
            subData = getObjectOr(trData, "planting", new HashMap());
            sdate = getValueOr(subData, "date", defValD);
        }
        sdate = formatDateStr(sdate);
        sdate = String.format("%5s", sdate);

        if (!getValueOr(trData, "hadat_valid", "").trim().isEmpty()) {
            harOpt = "R";
        }

        if (!getValueOr(trData, "2d_valid", "").trim().isEmpty()) {
            hydro = "G";
            is2D = true;
        }

        if (getValueOr(trData, "seasonal_dome_applied", "").trim().equals("Y")) {
            vbose = "0";
        }

        String smStr;
        HashMap smData;
        // GENERAL
        sb.append("@N GENERAL     NYERS NREPS START SDATE RSEED SNAME.................... SMODEL\r\n");
        if (!(smStr = getValueOr(trData, "sm_general", "")).isEmpty()) {
            if (!sdate.trim().equals("-99") && !sdate.trim().isEmpty()) {
                smStr = replaceSMStr(smStr, sdate, 30);
            }
            smStr = replaceSMStr(smStr, getValueOr(trData, "dssat_model", ""), 68);
            sb.append(sm).append(" ").append(smStr).append("\r\n");
        } else if (!(smData = getObjectOr(trData, "general", new HashMap())).isEmpty()) {
            if (sdate.trim().equals("-99") || sdate.trim().isEmpty()) {
                sdate = String.format("%1$02d%2$03d", Integer.parseInt(formatNumStr(2, smData, "sdyer", "0").trim()), Integer.parseInt(formatNumStr(3, smData, "sdday", "0").trim()));
            }
            sb.append(String.format("%1$-3s%2$-11s %3$5s %4$5s     %5$-1s %6$5s %7$5s %8$-25s %9$s\r\n",
                    sm,
                    "GE",
                    getValueOr(smData, "nyers", "1"),
                    getValueOr(smData, "nreps", "1"),
                    getValueOr(smData, "start", "S"),
                    sdate,
                    getValueOr(smData, "rseed", "250"),
                    getValueOr(smData, "sname", defValC),
                    getValueOr(trData, "dssat_model", getValueOr(smData, "model", ""))));
        } else {
            sb.append(sm).append(" GE              1     1     S ").append(sdate).append("  2150 DEFAULT SIMULATION CONTRL ").append(getValueOr(trData, "dssat_model", "")).append("\r\n");
        }
        // OPTIONS
        sb.append("@N OPTIONS     WATER NITRO SYMBI PHOSP POTAS DISES  CHEM  TILL   CO2\r\n");
        if (!(smStr = getValueOr(trData, "sm_options", "")).isEmpty()) {
//            smStr = replaceSMStr(smStr, co2, 64);
            sb.append(sm).append(" ").append(smStr).append("\r\n");
        } else if (!(smData = getObjectOr(trData, "options", new HashMap())).isEmpty()) {
            sb.append(String.format("%1$-3s%2$-11s     %3$-1s     %4$-1s     %5$-1s     %6$-1s     %7$-1s     %8$-1s     %9$-1s     %10$-1s     %11$-1s\r\n",
                    sm,
                    "OP",
                    getValueOr(smData, "water", water),
                    getValueOr(smData, "nitro", nitro),
                    getValueOr(smData, "symbi", "Y"),
                    getValueOr(smData, "phosp", "N"),
                    getValueOr(smData, "potas", "N"),
                    getValueOr(smData, "dises", "N"),
                    getValueOr(smData, "chem", "N"),
                    getValueOr(smData, "till", "Y"),
                    getValueOr(smData, "co2", co2)));
        } else if (is2D) {
            sb.append(sm).append(" OP              ").append(water).append("     ").append(nitro).append("     Y     N     N     N     N     N     ").append(co2).append("\r\n");
        } else {
            sb.append(sm).append(" OP              ").append(water).append("     ").append(nitro).append("     Y     N     N     N     N     Y     ").append(co2).append("\r\n");
        }
        // METHODS
        sb.append("@N METHODS     WTHER INCON LIGHT EVAPO INFIL PHOTO HYDRO NSWIT MESOM MESEV MESOL\r\n");
        if (!(smStr = getValueOr(trData, "sm_methods", "")).isEmpty()) {
            sb.append(sm).append(" ").append(smStr).append("\r\n");
        } else if (!(smData = getObjectOr(trData, "methods", new HashMap())).isEmpty()) {
            sb.append(String.format("%1$-3s%2$-11s     %3$-1s     %4$-1s     %5$-1s     %6$-1s     %7$-1s     %8$-1s     %9$-1s     %10$-1s     %11$-1s     %12$-1s     %13$-1s\r\n",
                    sm,
                    "ME",
                    getValueOr(smData, "wther", "M"),
                    getValueOr(smData, "incon", "M"),
                    getValueOr(smData, "light", "E"),
                    getValueOr(smData, "evapo", "R"),
                    getValueOr(smData, "infil", "S"),
                    getValueOr(smData, "photo", "L"),
                    getValueOr(smData, "hydro", "R"),
                    getValueOr(smData, "nswit", "1"),
                    getValueOr(smData, "mesom", "P"),
                    getValueOr(smData, "mesev", mesev),
                    getValueOr(smData, "mesol", "2")));
        } else if (is2D) {
            sb.append(sm).append(" ME              M     M     E     R     N     C     ").append(hydro).append("     1     G     ").append(mesev).append("     2\r\n"); // P.S. 2012/09/02 MESOM "G" -> "P"
        } else {
            sb.append(sm).append(" ME              M     M     E     R     S     L     ").append(hydro).append("     1     P     ").append(mesev).append("     2\r\n"); // P.S. 2012/09/02 MESOM "G" -> "P"
        }
        // MANAGEMENT
        sb.append("@N MANAGEMENT  PLANT IRRIG FERTI RESID HARVS\r\n");
        if (!(smStr = getValueOr(trData, "sm_management", "")).isEmpty()) {
//            smStr = smStr.replaceAll("     D", "     R");
//            smStr = replaceSMStr(smStr, harOpt, 40);
            sb.append(sm).append(" ").append(smStr).append("\r\n");
        } else if (!(smData = getObjectOr(trData, "management", new HashMap())).isEmpty()) {
            sb.append(String.format("%1$-3s%2$-11s     %3$-1s     %4$-1s     %5$-1s     %6$-1s     %7$-1s\r\n",
                    sm,
                    "MA",
                    getValueOr(smData, "plant", "R"),
                    getValueOr(smData, "irrig", "R"),
                    getValueOr(smData, "ferti", "R"),
                    getValueOr(smData, "resid", "R"),
                    getValueOr(smData, "harvs", harOpt)));
        } else {
            sb.append(sm).append(" MA              R     ").append(irrigMgn).append("     R     R     ").append(harOpt).append("\r\n");
        }
        // OUTPUTS
        sb.append("@N OUTPUTS     FNAME OVVEW SUMRY FROPT GROUT CAOUT WAOUT NIOUT MIOUT DIOUT VBOSE CHOUT OPOUT\r\n");
        if (!(smStr = getValueOr(trData, "sm_outputs", "")).isEmpty()) {
            sb.append(sm).append(" ").append(smStr).append("\r\n\r\n");
        } else if (!(smData = getObjectOr(trData, "outputs", new HashMap())).isEmpty()) {
            sb.append(String.format("%1$-3s%2$-11s     %3$-1s     %4$-1s     %5$-1s %6$5s     %7$-1s     %8$-1s     %9$-1s     %10$-1s     %11$-1s     %12$-1s     %13$-1s     %14$-1s     %15$-1s\r\n\r\n",
                    sm,
                    "OU",
                    getValueOr(smData, "fname", "N"),
                    getValueOr(smData, "ovvew", "Y"),
                    getValueOr(smData, "sumry", "Y"),
                    getValueOr(smData, "fropt", "1"),
                    getValueOr(smData, "grout", "Y"),
                    getValueOr(smData, "caout", "Y"),
                    getValueOr(smData, "waout", "N"),
                    getValueOr(smData, "niout", "N"),
                    getValueOr(smData, "miout", "N"),
                    getValueOr(smData, "diout", "N"),
                    getValueOr(smData, "vbose", vbose),
                    getValueOr(smData, "chout", "N"),
                    getValueOr(smData, "opout", "N")));
        } else if (is2D) {
            vbose = "D";
            sb.append(sm).append(" OU              N     Y     Y     1     Y     Y     Y     Y     N     N     ").append(vbose).append("     N     N\r\n\r\n");
        } else {
            sb.append(sm).append(" OU              N     Y     Y     1     Y     Y     N     N     N     N     ").append(vbose).append("     N     N\r\n\r\n");
        }
        // PLANTING
        sb.append("@  AUTOMATIC MANAGEMENT\r\n");
        sb.append("@N PLANTING    PFRST PLAST PH2OL PH2OU PH2OD PSTMX PSTMN\r\n");
        if (!(smStr = getValueOr(trData, "sm_planting", "")).isEmpty()) {
            sb.append(sm).append(" ").append(smStr).append("\r\n");
        } else if (!(smData = getObjectOr(trData, "planting", new HashMap())).isEmpty()) {
            sb.append(String.format("%1$-3s%2$-11s %3$02d%4$03d %5$02d%6$03d %7$5s %8$5s %9$5s %10$5s %11$5s\r\n",
                    sm,
                    "PL",
                    Integer.parseInt(formatNumStr(2, smData, "pfyer", "82")),
                    Integer.parseInt(formatNumStr(3, smData, "pfday", "050")),
                    Integer.parseInt(formatNumStr(2, smData, "plyer", "82")),
                    Integer.parseInt(formatNumStr(3, smData, "plday", "064")),
                    getValueOr(smData, "ph2ol", "40"),
                    getValueOr(smData, "ph2ou", "100"),
                    getValueOr(smData, "ph2od", "30"),
                    getValueOr(smData, "pstmx", "40"),
                    getValueOr(smData, "pstmn", "10")));
        } else {
            sb.append(sm).append(" PL          82050 82064    40   100    30    40    10\r\n");
        }
        // IRRIGATION
        sb.append("@N IRRIGATION  IMDEP ITHRL ITHRU IROFF IMETH IRAMT IREFF\r\n");
        if (!(smStr = getValueOr(trData, "sm_irrigation", "")).isEmpty()) {
            sb.append(sm).append(" ").append(smStr).append("\r\n");
        } else if (!(smData = getObjectOr(trData, "irrigation", new HashMap())).isEmpty()) {
            sb.append(String.format("%1$-3s%2$-11s %3$5s %4$5s %5$5s %6$-5s %7$-5s %8$5s %9$5s\r\n",
                    sm,
                    "IR",
                    getValueOr(smData, "imdep", "30"),
                    getValueOr(smData, "ithrl", "50"),
                    getValueOr(smData, "ithru", "100"),
                    getValueOr(smData, "iroff", "GS000"),
                    getValueOr(smData, "imeth", "IR001"),
                    getValueOr(smData, "iramt", "10"),
                    getValueOr(smData, "ireff", "1.00")));
        } else if (!"R".equals(irrigMgn)) {
            sb.append(String.format("%1$-3s%2$-11s %3$5s %4$5s %5$5s %6$-5s %7$-5s %8$5s %9$5s\r\n",
                    sm,
                    "IR",
                    getValueOr(autoIrrEvt, "irmdp", ""),
                    getValueOr(autoIrrEvt, "irthr", ""),
                    getValueOr(autoIrrEvt, "irept", ""),
                    getValueOr(autoIrrEvt, "irstg", ""),
                    getValueOr(autoIrrEvt, "iame", ""),
                    getValueOr(autoIrrEvt, "iamt", ""),
                    getValueOr(autoIrrEvt, "ireff", "")));
        } else {
            sb.append(sm).append(" IR             30    50   100 GS000 IR001    10  1.00\r\n");
        }
        // NITROGEN
        sb.append("@N NITROGEN    NMDEP NMTHR NAMNT NCODE NAOFF\r\n");
        if (!(smStr = getValueOr(trData, "sm_nitrogen", "")).isEmpty()) {
            sb.append(sm).append(" ").append(smStr).append("\r\n");
        } else if (!(smData = getObjectOr(trData, "nitrogen", new HashMap())).isEmpty()) {
            sb.append(String.format("%1$-3s%2$-11s %3$5s %4$5s %5$5s %6$-5s %7$-5s\r\n",
                    sm,
                    "NI",
                    getValueOr(smData, "nmdep", "30"),
                    getValueOr(smData, "nmthr", "50"),
                    getValueOr(smData, "namnt", "25"),
                    getValueOr(smData, "ncode", "FE001"),
                    getValueOr(smData, "naoff", "GS000")));
        } else {
            sb.append(sm).append(" NI             30    50    25 FE001 GS000\r\n");
        }
        // RESIDUES
        sb.append("@N RESIDUES    RIPCN RTIME RIDEP\r\n");
        if (!(smStr = getValueOr(trData, "sm_residues", "")).isEmpty()) {
            sb.append(sm).append(" ").append(smStr).append("\r\n");
        } else if (!(smData = getObjectOr(trData, "residues", new HashMap())).isEmpty()) {
            sb.append(String.format("%1$-3s%2$-11s %3$5s %4$5s %5$5s\r\n",
                    sm,
                    "RE",
                    getValueOr(smData, "ripcn", "100"),
                    getValueOr(smData, "rtime", "1"),
                    getValueOr(smData, "ridep", "20")));
        } else {
            sb.append(sm).append(" RE            100     1    20\r\n");
        }
        // HARVEST
        sb.append("@N HARVEST     HFRST HLAST HPCNP HPCNR\r\n");
        if (!(smStr = getValueOr(trData, "sm_harvests", "")).isEmpty()) {
            sb.append(sm).append(" ").append(smStr).append("\r\n\r\n");
        } else if (!(smData = getObjectOr(trData, "harvests", new HashMap())).isEmpty()) {
            sb.append(String.format("%1$-3s%2$-11s %3$5s %4$02d%5$03d %6$5s %7$5s\r\n\r\n",
                    sm,
                    "HA",
                    getValueOr(smData, "hfrst", "0"),
                    Integer.parseInt(formatNumStr(2, smData, "hlyer", "83")),
                    Integer.parseInt(formatNumStr(3, smData, "hlday", "057")),
                    getValueOr(smData, "hpcnp", "100"),
                    getValueOr(smData, "hpcnr", "0")));
        } else {
            sb.append(sm).append(" HA              0 83057   100     0\r\n\r\n");
        }

        return sb.toString();
    }

    /**
     * Get index value of the record and set new id value in the array. In
     * addition, it will check if the event date is available. If not, then
     * return 0.
     *
     * @param m sub data
     * @param arr array of sub data
     * @return current index value of the sub data
     */
    private int setSecDataArr(HashMap m, ArrayList arr, boolean isEvent) {

        int idx = setSecDataArr(m, arr);

        if (idx != 0 && isEvent && getValueOr(m, "date", "").isEmpty()) {
            return -1;
        } else {
            return idx;
        }
    }

    /**
     * Get index value of the record and set new id value in the array
     *
     * @param m sub data
     * @param arr array of sub data
     * @return current index value of the sub data
     */
    private int setSecDataArr(HashMap m, ArrayList arr) {

        if (!m.isEmpty()) {
            for (int j = 0; j < arr.size(); j++) {
                if (arr.get(j).equals(m)) {
                    return j + 1;
                }
            }
            arr.add(m);
            return arr.size();
        } else {
            return 0;
        }
    }

    /**
     * Get index value of the record and set new id value in the array
     *
     * @param m sub data
     * @param arr array of sub data
     * @return current index value of the sub data
     */
    private int setSecSMDataArr(HashMap m, ArrayList arr) {

        for (int j = 0; j < arr.size(); j++) {
            if (arr.get(j).equals(m)) {
                return j + 1;
            }
        }
        arr.add(m);
        return arr.size();
    }

    private int setSecDataArr(ArrayList inArr, ArrayList outArr, boolean isEvent) {

        int idx = setSecDataArr(inArr, outArr);

        if (idx != 0 && isEvent && !checkEventDate(inArr)) {
            return -1;
        } else {
            return idx;
        }
    }

    private boolean checkEventDate(ArrayList arr) {
        for (Object o : arr) {
            if (getValueOr((HashMap) o, "date", "").isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get index value of the record and set new id value in the array
     *
     * @param inArr sub array of data
     * @param outArr array of sub data
     * @return current index value of the sub data
     */
    private int setSecDataArr(ArrayList inArr, ArrayList outArr) {

        if (!inArr.isEmpty()) {
            for (int j = 0; j < outArr.size(); j++) {
                if (outArr.get(j).equals(inArr)) {
                    return j + 1;
                }
            }
            outArr.add(inArr);
            return outArr.size();
        } else {
            return 0;
        }
    }

    /**
     * To check if there is plot info data existed in the experiment
     *
     * @param expData experiment data holder
     * @return the boolean value for if plot info exists
     */
    private boolean isPlotInfoExist(Map expData) {

        String[] plotIds = {"plta", "pltr#", "pltln", "pldr", "pltsp", "pllay", "pltha", "plth#", "plthl", "plthm"};
        for (String plotId : plotIds) {
            if (!getValueOr(expData, plotId, "").isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * To check if there is soil analysis info data existed in the experiment
     *
     * @param expData initial condition layer data array
     * @return the boolean value for if soil analysis info exists
     */
    private boolean isSoilAnalysisExist(ArrayList<HashMap> icSubArr) {

        for (HashMap icSubData : icSubArr) {
            if (!getValueOr(icSubData, "slsc", "").isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get sub data array from experiment data object
     *
     * @param expData experiment data holder
     * @param blockName top level block name
     * @param dataListName sub array name
     * @return sub data array
     */
    private ArrayList<HashMap> getDataList(Map expData, String blockName, String dataListName) {
        HashMap dataBlock = getObjectOr(expData, blockName, new HashMap());
        return getObjectOr(dataBlock, dataListName, new ArrayList<HashMap>());
    }

    /**
     * Try to translate 3-bit CRID to 2-bit version stored in the map
     *
     * @param cuData the cultivar data record
     * @param id the field id for contain crop id info
     * @param defVal the default value when id is not available
     * @return 2-bit crop ID
     */
    private String translateTo2BitCrid(Map cuData, String id, String defVal) {
        String crid = getValueOr(cuData, id, "");
        if (!crid.isEmpty()) {
            return DssatCRIDHelper.get2BitCrid(crid);
        } else {
            return defVal;
        }
    }

    /**
     * Try to translate 3-bit CRID to 2-bit version stored in the map
     *
     * @param cuData the cultivar data record
     */
    private void translateTo2BitCrid(Map cuData) {
        String crid = getValueOr(cuData, "crid", "");
        if (!crid.isEmpty()) {
            cuData.put("crid", DssatCRIDHelper.get2BitCrid(crid));
        }
    }

    /**
     * Get soil/weather data from data holder
     *
     * @param expData The experiment data holder
     * @param key The key name for soil/weather section
     * @return the list of data holder
     */
    private ArrayList readSWData(HashMap expData, String key) {
        ArrayList ret;
        Object soil = expData.get(key);
        if (soil != null) {
            if (soil instanceof ArrayList) {
                ret = (ArrayList) soil;
            } else {
                ret = new ArrayList();
                ret.add(soil);
            }
        } else {
            ret = new ArrayList();
        }

        return ret;
    }

    private String replaceSMStr(String smStr, String val, int start) {
        if (smStr.length() < start) {
            smStr = String.format("%-" + start + "s", smStr);
        }
        smStr = smStr.substring(0, start)
                + val
                + smStr.substring(start + val.length());
        return smStr;
    }

    private String getAppliedDomes(HashMap data, String domeType) {
        if (getValueOr(data, domeType + "_dome_applied", "").equals("Y")) {
            // Get dome ids
            String domeKey = "";
            if (domeType.equals("field")) {
                domeKey = "field_overlay";
            } else if (domeType.equals("seasonal")) {
                domeKey = "seasonal_strategy";
            }
            String[] domeIds = getValueOr(data, domeKey, "").split("[|]");
            String[] failedDomeIds = getValueOr(data, domeType + "_dome_failed", "").split("[|]");
            HashSet<String> domes = new HashSet();
            // Add all dome ids
            for (String domeId : domeIds) {
                if (!domeId.isEmpty()) {
                    domes.add(domeId);
                }
            }
            // Remove failed dome id
            for (String failedDomeId : failedDomeIds) {
                if (!failedDomeId.isEmpty()) {
                    domes.remove(failedDomeId);
                }
            }
            // Convert to string format, divide by "|"
            StringBuilder ret = new StringBuilder();
            String[] domeArr = domes.toArray(new String[0]);
            if (domeArr.length > 0) {
                ret.append(domeArr[0]);
            }
            for (int i = 1; i < domeArr.length; i++) {
                ret.append("|").append(domeArr[i]);
            }
            return ret.toString();
        } else {
            return "";
        }
    }
}
