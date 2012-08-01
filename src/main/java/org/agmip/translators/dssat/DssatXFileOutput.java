package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.agmip.translators.dssat.DssatCommonInput.copyItem;

import static org.agmip.util.MapUtil.*;

/**
 * DSSAT Experiment Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatXFileOutput extends DssatCommonOutput {

    private File outputFile;

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * DSSAT Experiment Data Output method
     * 
     * @param arg0  file output path
     * @param result  data holder object
     */
    @Override
    public void writeFile(String arg0, Map result) {

        // Initial variables
        LinkedHashMap expData = (LinkedHashMap) result;
        LinkedHashMap soilData = getObjectOr(result, "soil", new LinkedHashMap());
        BufferedWriter bwX;                          // output object
        StringBuilder sbData = new StringBuilder();     // construct the data info in the output
        StringBuilder eventPart2 = new StringBuilder();                   // output string for second part of event data
        LinkedHashMap secData;
        ArrayList subDataArr;                       // Arraylist for event data holder
        LinkedHashMap subData;
        ArrayList secDataArr;                       // Arraylist for section data holder
        LinkedHashMap sqData;
        LinkedHashMap trData = new LinkedHashMap();
        ArrayList<LinkedHashMap> evtArr;            // Arraylist for section data holder
        LinkedHashMap evtData;
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
        ArrayList<LinkedHashMap> sqArr;     // array for treatment record
        ArrayList cuArr = new ArrayList();   // array for cultivars record
        ArrayList flArr = new ArrayList();   // array for fields record
        ArrayList saArr = new ArrayList();   // array for soil analysis record
        ArrayList icArr = new ArrayList();   // array for initial conditions record
        ArrayList mpArr = new ArrayList();   // array for plaintings record
        ArrayList miArr = new ArrayList();   // array for irrigations record
        ArrayList mfArr = new ArrayList();   // array for fertilizers record
        ArrayList mrArr = new ArrayList();   // array for residues record
        ArrayList mcArr = new ArrayList();   // array for chemical record
        ArrayList mtArr = new ArrayList();   // array for tillage record
        ArrayList meArr = new ArrayList();   // array for enveronment modification record
        ArrayList mhArr = new ArrayList();   // array for harvest record
        ArrayList smArr = new ArrayList();   // array for simulation control record
        String exName;

        try {

            // Set default value for missing data
//            result = (LinkedHashMap) getObjectOr(result, "experiment", result);
            if (expData == null || expData.isEmpty()) {
                return;
            }
//            decompressData((LinkedHashMap) result);
            setDefVal();

            // Initial BufferedWriter
            exName = getExName(expData);
            String fileName = exName;
            if (fileName.equals("")) {
                fileName = "a.tmp";
            } else {
                fileName = fileName.substring(0, fileName.length() - 2) + "." + fileName.substring(fileName.length() - 2) + "X";
            }
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwX = new BufferedWriter(new FileWriter(outputFile));


            // Output XFile
            // EXP.DETAILS Section
            sbData.append(String.format("*EXP.DETAILS: %1$-10s %2$s\r\n\r\n", exName, getObjectOr(expData, "local_name", defValBlank).toString()));

            // GENERAL Section
            sbData.append("*GENERAL\r\n");
            // People
            if (!getObjectOr(expData, "people", "").equals("")) {
                sbData.append(String.format("@PEOPLE\r\n %1$s\r\n", getObjectOr(expData, "people", defValBlank).toString()));
            }
            // Address
            if (getObjectOr(expData, "institution", "").equals("")) {
                if (!getObjectOr(expData, "fl_loc_1", "").equals("")
                        && getObjectOr(expData, "fl_loc_2", "").equals("")
                        && getObjectOr(expData, "fl_loc_3", "").equals("")) {
                    sbData.append(String.format("@ADDRESS\r\n %3$s, %2$s, %1$s\r\n",
                            getObjectOr(expData, "fl_loc_1", defValBlank).toString(),
                            getObjectOr(expData, "fl_loc_2", defValBlank).toString(),
                            getObjectOr(expData, "fl_loc_3", defValBlank).toString()));
                }
            } else {
                sbData.append(String.format("@ADDRESS\r\n %1$s\r\n", getObjectOr(expData, "institution", defValBlank).toString()));
            }

            // Site
            if (!getObjectOr(expData, "site", "").equals("")) {
                sbData.append(String.format("@SITE\r\n %1$s\r\n", getObjectOr(expData, "site", defValBlank).toString()));
            }
            // Plot Info
            if (isPlotInfoExist(expData)) {

                sbData.append("@ PAREA  PRNO  PLEN  PLDR  PLSP  PLAY HAREA  HRNO  HLEN  HARM.........\r\n");
                sbData.append(String.format(" %1$6s %2$5s %3$5s %4$5s %5$5s %6$-5s %7$5s %8$5s %9$5s %10$-15s\r\n",
                        formatNumStr(6, expData, "plta", defValR),
                        formatNumStr(5, expData, "pltrno", defValI),
                        formatNumStr(5, expData, "pltln", defValR),
                        formatNumStr(5, expData, "pldr", defValI),
                        formatNumStr(5, expData, "pltsp", defValI),
                        getObjectOr(expData, "plot_layout", defValC).toString(),
                        formatNumStr(5, expData, "pltha", defValR),
                        formatNumStr(5, expData, "plthno", defValI),
                        formatNumStr(5, expData, "plthl", defValR),
                        getObjectOr(expData, "plthm", defValC).toString()));
            }
            // Notes
            if (!getObjectOr(expData, "notes", "").equals("")) {
                sbData.append("@NOTES\r\n");
                String notes = getObjectOr(expData, "notes", defValC).toString();

                // If notes contain newline code, then write directly
                if (notes.indexOf("\r\n") >= 0) {
                    sbData.append(String.format(" %1$s\r\n", notes));
                } // Otherwise, add newline for every 75-bits charactors
                else {
                    while (notes.length() > 75) {
                        sbData.append(" ").append(notes.substring(0, 75)).append("\r\n");
                        notes = notes.substring(75);
                    }
                    sbData.append(" ").append(notes).append("\r\n");
                }
            }
            sbData.append("\r\n");

            // TREATMENT Section
            sqArr = getDataList(expData, "dssat_sequence", "data");
            evtArr = getDataList(expData, "management", "events");
            String seqId;
            sbData.append("*TREATMENTS                        -------------FACTOR LEVELS------------\r\n");
            sbData.append("@N R O C TNAME.................... CU FL SA IC MP MI MF MR MC MT ME MH SM\r\n");

            // if there is no sequence info, create dummy data
            if (sqArr.isEmpty()) {
                sqArr.add(new LinkedHashMap());
            }

            // Set field info
            LinkedHashMap flData = new LinkedHashMap();
            copyItem(flData, expData, "id_field");
            copyItem(flData, expData, "wst_id");
            copyItem(flData, expData, "flsl");
            copyItem(flData, expData, "flob");
            copyItem(flData, expData, "fl_drntype");
            copyItem(flData, expData, "fldrd");
            copyItem(flData, expData, "fldrs");
            copyItem(flData, expData, "flst");
            copyItem(flData, soilData, "sltx");
            copyItem(flData, soilData, "sldp");
            copyItem(flData, expData, "soil_id");
            copyItem(flData, expData, "fl_name");
            copyItem(flData, expData, "fl_lat");
            copyItem(flData, expData, "fl_long");
            copyItem(flData, expData, "flele");
            copyItem(flData, expData, "farea");
            copyItem(flData, expData, "fllwr");
            copyItem(flData, expData, "flsla");
            copyItem(flData, getObjectOr(expData, "dssat_info", new LinkedHashMap()), "flhst");
            copyItem(flData, getObjectOr(expData, "dssat_info", new LinkedHashMap()), "fhdur");
            // remove the "_trno" in the soil_id when soil analysis is available
            String soilId = getValueOr(flData, "soil_id", "");
            if (soilId.length() > 10 && soilId.matches("\\w+_\\d+")) {
                flData.put("soil_id", soilId.replaceAll("_\\d+$", ""));
            }
            flNum = setSecDataArr(flData, flArr);

            // Set initial condition info
            icNum = setSecDataArr(getObjectOr(expData, "initial_condition", new LinkedHashMap()), icArr);

            // Set soil analysis info
            ArrayList<LinkedHashMap> icSubArr = getDataList(expData, "initial_condition", "soilLayer");
            if (isSoilAnalysisExist(icSubArr)) {
                LinkedHashMap saData = new LinkedHashMap();
                ArrayList<LinkedHashMap> saSubArr = new ArrayList<LinkedHashMap>();
                LinkedHashMap saSubData;
                for (int i = 0; i < icSubArr.size(); i++) {
                    saSubData = new LinkedHashMap();
                    copyItem(saSubData, icSubArr.get(i), "sabl", "icbl", false);
                    copyItem(saSubData, icSubArr.get(i), "sasc");
                    saSubArr.add(saSubData);
                }
                copyItem(saData, soilData, "sadat");
                saData.put("soilLayer", saSubArr);
                saNum = setSecDataArr(saData, saArr);
            } else {
                saNum = 0;
            }

            // Set sequence related block info
            for (int i = 0; i < sqArr.size(); i++) {
                sqData = sqArr.get(i);
                seqId = getValueOr(sqData, "seqid", defValBlank);
                LinkedHashMap cuData = new LinkedHashMap();
                LinkedHashMap mpData = new LinkedHashMap();
                ArrayList<LinkedHashMap> miSubArr = new ArrayList<LinkedHashMap>();
                ArrayList<LinkedHashMap> mfSubArr = new ArrayList<LinkedHashMap>();
                ArrayList<LinkedHashMap> mrSubArr = new ArrayList<LinkedHashMap>();
                ArrayList<LinkedHashMap> mcSubArr = new ArrayList<LinkedHashMap>();
                ArrayList<LinkedHashMap> mtSubArr = new ArrayList<LinkedHashMap>();
                ArrayList<LinkedHashMap> meSubArr = new ArrayList<LinkedHashMap>();
                ArrayList<LinkedHashMap> mhSubArr = new ArrayList<LinkedHashMap>();
                LinkedHashMap smData = new LinkedHashMap();
                
                // Set environment modification info
                meSubArr = getObjectOr(sqData, "em_data", meSubArr);

                // Set simulation control info
                if (!getValueOr(sqData, "sm_general", "").equals("")) {
                    smData.put("sm_general", getValueOr(sqData, "sm_general", defValBlank));
                    smData.put("sm_options", getValueOr(sqData, "sm_options", defValBlank));
                    smData.put("sm_methods", getValueOr(sqData, "sm_methods", defValBlank));
                    smData.put("sm_management", getValueOr(sqData, "sm_management", defValBlank));
                    smData.put("sm_outputs", getValueOr(sqData, "sm_outputs", defValBlank));
                    smData.put("sm_planting", getValueOr(sqData, "sm_planting", defValBlank));
                    smData.put("sm_irrigation", getValueOr(sqData, "sm_irrigation", defValBlank));
                    smData.put("sm_nitrogen", getValueOr(sqData, "sm_nitrogen", defValBlank));
                    smData.put("sm_residues", getValueOr(sqData, "sm_residues", defValBlank));
                    smData.put("sm_harvests", getValueOr(sqData, "sm_harvests", defValBlank));
                } else {
                    smData.put("fertilizer", mfSubArr);
                    smData.put("irrigation", miSubArr);
                    smData.put("planting", mpData);
                }

                // Loop all event data
                for (int j = 0; j < evtArr.size(); j++) {
                    evtData = new LinkedHashMap();
                    evtData.putAll(evtArr.get(j));
                    // Check if it has same sequence number
                    if (getValueOr(evtData, "seqid", defValBlank).equals(seqId)) {
                        evtData.remove("seqid");

                        // Planting event
                        if (getValueOr(evtData, "event", defValBlank).equals("planting")) {
                            // Set cultivals info
                            copyItem(cuData, evtData, "cul_name");
                            copyItem(cuData, sqData, "crid");
                            copyItem(cuData, sqData, "cul_id");
                            // Set planting info
                            mpData.putAll(evtData);
                            mpData.remove("cul_name");
                        } // irrigation event
                        else if (getValueOr(evtData, "event", "").equals("irrigation")) {
                            miSubArr.add(evtData);
                        } // irrigation event
                        else if (getValueOr(evtData, "event", "").equals("fertilizer")) {
                            mfSubArr.add(evtData);
                        } // organic_matter event
                        else if (getValueOr(evtData, "event", "").equals("organic_matter")) {
                            mrSubArr.add(evtData);
                        } // chemical event
                        else if (getValueOr(evtData, "event", "").equals("chemical")) {
                            mcSubArr.add(evtData);
                        } // tillage event
                        else if (getValueOr(evtData, "event", "").equals("tillage")) {
                            mtSubArr.add(evtData);
//                        } // environment_modification event
//                        else if (getValueOr(evtData, "event", "").equals("environment_modification")) {
//                            meSubArr.add(evtData);
                        } // harvest event
                        else if (getValueOr(evtData, "event", "").equals("harvest")) {
                            mhSubArr.add(evtData);
                        } else {
                        }
                    } else {
                    }

                }

                cuNum = setSecDataArr(cuData, cuArr);
                mpNum = setSecDataArr(mpData, mpArr);
                miNum = setSecDataArr(miSubArr, miArr);
                mfNum = setSecDataArr(mfSubArr, mfArr);
                mrNum = setSecDataArr(mrSubArr, mrArr);
                mcNum = setSecDataArr(mcSubArr, mcArr);
                mtNum = setSecDataArr(mtSubArr, mtArr);
                meNum = setSecDataArr(meSubArr, meArr);
                mhNum = setSecDataArr(mhSubArr, mhArr);
                smNum = setSecDataArr(smData, smArr);
                if (smNum == 0) {
                    smNum = 1;
                }

                sbData.append(String.format("%1$2s %2$1s %3$1s %4$1s %5$-25s %6$2s %7$2s %8$2s %9$2s %10$2s %11$2s %12$2s %13$2s %14$2s %15$2s %16$2s %17$2s %18$2s\r\n",
                        getValueOr(sqData, "trno", "1").toString(),
                        getValueOr(sqData, "sq", "1").toString(), // P.S. default value here is based on document DSSAT vol2.pdf
                        getValueOr(sqData, "op", "1").toString(),
                        getValueOr(sqData, "co", "0").toString(),
                        getValueOr(sqData, "tr_name", getValueOr(expData, "tr_name", defValC)).toString(),
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
                        smNum)); // 1

            }
            sbData.append("\r\n");

            // CULTIVARS Section
            if (!cuArr.isEmpty()) {
                sbData.append("*CULTIVARS\r\n");
                sbData.append("@C CR INGENO CNAME\r\n");

                for (int idx = 0; idx < cuArr.size(); idx++) {
                    secData = (LinkedHashMap) cuArr.get(idx);
                    // Checl if necessary data is missing
                    if (getObjectOr(secData, "crid", "").equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [crid]\r\n");
                    } else if (getObjectOr(secData, "cul_id", "").equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [cul_id]\r\n");
                    }
                    sbData.append(String.format("%1$2s %2$-2s %3$-6s %4$s\r\n",
                            idx + 1, //getObjectOr(secData, "ge", defValI).toString(),
                            getObjectOr(secData, "crid", defValBlank).toString(),   // P.S. if missing, default value use blank string
                            getObjectOr(secData, "cul_id", defValC).toString(),
                            getObjectOr(secData, "cul_name", defValC).toString()));
                }
                sbData.append("\r\n");
            } else {
                sbError.append("! Warning: Trhee is no cultivar data in the experiment.\r\n");
            }

            // FIELDS Section
            if (!flArr.isEmpty()) {
                sbData.append("*FIELDS\r\n");
                sbData.append("@L ID_FIELD WSTA....  FLSA  FLOB  FLDT  FLDD  FLDS  FLST SLTX  SLDP  ID_SOIL    FLNAME\r\n");
                eventPart2 = new StringBuilder();
                eventPart2.append("@L ...........XCRD ...........YCRD .....ELEV .............AREA .SLEN .FLWR .SLAS FLHST FHDUR\r\n");
            } else {
                sbError.append("! Warning: Trhee is no field data in the experiment.\r\n");
            }
            for (int idx = 0; idx < flArr.size(); idx++) {
                secData = (LinkedHashMap) flArr.get(idx);
                // Check if the necessary is missing
                if (getObjectOr(secData, "wst_id", "").equals("")) {
                    sbError.append("! Warning: Incompleted record because missing data : [wst_id]\r\n");
                } else if (getObjectOr(secData, "soil_id", "").equals("")) {
                    sbError.append("! Warning: Incompleted record because missing data : [soil_id]\r\n");
                }
                sbData.append(String.format("%1$2s %2$-8s %3$-8s %4$5s %5$5s %6$-5s %7$5s %8$5s %9$-5s %10$-5s%11$5s  %12$-10s %13$s\r\n", // P.S. change length definition to match current way
                        idx + 1, //getObjectOr(secData, "fl", defValI).toString(),
                        getObjectOr(secData, "id_field", defValC).toString(),
                        getObjectOr(secData, "wst_id", defValC).toString(),
                        getObjectOr(secData, "flsl", defValC).toString(),
                        formatNumStr(5, secData, "flob", defValR),
                        getObjectOr(secData, "fl_drntype", defValC).toString(),
                        formatNumStr(5, secData, "fldrd", defValR),
                        formatNumStr(5, secData, "fldrs", defValR),
                        getObjectOr(secData, "flst", defValC).toString(),
                        getObjectOr(secData, "sltx", defValC).toString(),
                        formatNumStr(5, secData, "sldp", defValR),
                        getObjectOr(secData, "soil_id", defValC).toString(),
                        getObjectOr(secData, "fl_name", defValC).toString()));

                eventPart2.append(String.format("%1$2s %2$15s %3$15s %4$9s %5$17s %6$5s %7$5s %8$5s %9$5s %10$5s\r\n",
                        idx + 1, //getObjectOr(secData, "fl", defValI).toString(),
                        formatNumStr(15, secData, "fl_lat", defValR),
                        formatNumStr(15, secData, "fl_long", defValR),
                        formatNumStr(9, secData, "flele", defValR),
                        formatNumStr(17, secData, "farea", defValR),
                        "-99", // P.S. SLEN keeps -99
                        formatNumStr(5, secData, "fllwr", defValR),
                        formatNumStr(5, secData, "flsla", defValR),
                        getObjectOr(secData, "flhst", defValC).toString(),
                        formatNumStr(5, secData, "fhdur", defValR)));
            }
            if (!flArr.isEmpty()) {
                sbData.append(eventPart2.toString()).append("\r\n");
            }

            // SOIL ANALYSIS Section
            if (!saArr.isEmpty()) {
                sbData.append("*SOIL ANALYSIS\r\n");

                for (int idx = 0; idx < saArr.size(); idx++) {

                    secData = (LinkedHashMap) saArr.get(idx);
                    sbData.append("@A SADAT  SMHB  SMPX  SMKE  SANAME\r\n");
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s  %6$s\r\n",
                            idx + 1, //getObjectOr(secData, "sa", defValI).toString(),
                            formatDateStr(getObjectOr(secData, "sadat", defValD).toString()),
                            getObjectOr(secData, "samhb", defValC).toString(),
                            getObjectOr(secData, "sampx", defValC).toString(),
                            getObjectOr(secData, "samke", defValC).toString(),
                            getObjectOr(secData, "sa_name", defValC).toString()));

                    subDataArr = (ArrayList) getObjectOr(secData, "soilLayer", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        sbData.append("@A  SABL  SADM  SAOC  SANI SAPHW SAPHB  SAPX  SAKE  SASC\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = (LinkedHashMap) subDataArr.get(j);
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s\r\n",
                                idx + 1, //getObjectOr(subData, "sa", defValI).toString(),
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

                    secData = (LinkedHashMap) icArr.get(idx);
                    sbData.append("@C   PCR ICDAT  ICRT  ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME\r\n");
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$s\r\n",
                            idx + 1, //getObjectOr(secData, "ic", defValI).toString(),
                            getObjectOr(secData, "icpcr", defValC).toString(),
                            formatDateStr(getObjectOr(secData, "date", getPdate(result)).toString()), // P.S. icdat -> date
                            formatNumStr(5, secData, "icrt", defValR),
                            formatNumStr(5, secData, "icnd", defValR),
                            formatNumStr(5, secData, "icrzno", defValR),
                            formatNumStr(5, secData, "icrze", defValR),
                            formatNumStr(5, secData, "icwt", defValR),
                            formatNumStr(5, secData, "icrag", defValR),
                            formatNumStr(5, secData, "icrn", defValR),
                            formatNumStr(5, secData, "icrp", defValR),
                            formatNumStr(5, secData, "icrip", defValR),
                            formatNumStr(5, secData, "icrdp", defValR),
                            getObjectOr(secData, "ic_name", defValC).toString()));

                    subDataArr = (ArrayList) getObjectOr(secData, "soilLayer", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        sbData.append("@C  ICBL  SH2O  SNH4  SNO3\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = (LinkedHashMap) subDataArr.get(j);
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s\r\n",
                                idx + 1, //getObjectOr(subData, "ic", defValI).toString(),
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

                    secData = (LinkedHashMap) mpArr.get(idx);
                    // Check if necessary data is missing
                    if (getObjectOr(secData, "date", "").equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [pdate]\r\n");
                    } else if (getObjectOr(secData, "plpop", getObjectOr(secData, "plpoe", "")).equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [plpop] and [plpoe]\r\n");
                    }
                    if (getObjectOr(secData, "plrs", "").equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [plrs]\r\n");
                    }
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s %15$5s                        %16$s\r\n",
                            idx + 1, //getObjectOr(data, "pl", defValI).toString(),
                            formatDateStr(getObjectOr(secData, "date", defValD).toString()),
                            formatDateStr(getObjectOr(secData, "pldae", defValD).toString()),
                            formatNumStr(5, secData, "plpop", getObjectOr(secData, "plpoe", defValR)),
                            formatNumStr(5, secData, "plpoe", getObjectOr(secData, "plpop", defValR)),
                            getObjectOr(secData, "plma", defValC).toString(),
                            getObjectOr(secData, "plds", defValC).toString(),
                            formatNumStr(5, secData, "plrs", defValR),
                            formatNumStr(5, secData, "plrd", defValR),
                            formatNumStr(5, secData, "pldp", defValR),
                            formatNumStr(5, secData, "plmwt", defValR),
                            formatNumStr(5, secData, "page", defValR),
                            formatNumStr(5, secData, "penv", defValR),
                            formatNumStr(5, secData, "plph", defValR),
                            formatNumStr(5, secData, "plspl", defValR),
                            getObjectOr(secData, "pl_name", defValC).toString()));

                }
                sbData.append("\r\n");
            } else {
                sbError.append("! Warning: Trhee is no plainting data in the experiment.\r\n");
            }

            // IRRIGATION AND WATER MANAGEMENT Section
            if (!miArr.isEmpty()) {
                sbData.append("*IRRIGATION AND WATER MANAGEMENT\r\n");

                for (int idx = 0; idx < miArr.size(); idx++) {

//                    secData = (ArrayList) miArr.get(idx);
                    subDataArr = (ArrayList) miArr.get(idx);
                    if (!subDataArr.isEmpty()) {
                        subData = (LinkedHashMap) subDataArr.get(0);
                    } else {
                        subData = new LinkedHashMap();
                    }
                    sbData.append("@I  EFIR  IDEP  ITHR  IEPT  IOFF  IAME  IAMT IRNAME\r\n");
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$s\r\n",
                            idx + 1, //getObjectOr(data, "ir", defValI).toString(),
                            formatNumStr(5, subData, "ireff", defValR),
                            formatNumStr(5, subData, "irmdp", defValR),
                            formatNumStr(5, subData, "irthr", defValR),
                            formatNumStr(5, subData, "irept", defValR),
                            getObjectOr(subData, "irstg", defValC).toString(),
                            getObjectOr(subData, "iame", defValC).toString(),
                            formatNumStr(5, subData, "iamt", defValR),
                            getObjectOr(subData, "ir_name", defValC).toString()));

                    if (!subDataArr.isEmpty()) {
                        sbData.append("@I IDATE  IROP IRVAL\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = (LinkedHashMap) subDataArr.get(j);
                        sbData.append(String.format("%1$2s %2$5s %3$-5s %4$5s\r\n",
                                idx + 1, //getObjectOr(subData, "ir", defValI).toString(),
                                formatDateStr(getObjectOr(subData, "date", defValD).toString()), // P.S. idate -> date
                                getObjectOr(subData, "irop", defValC).toString(),
                                formatNumStr(5, subData, "irval", defValR)));
                    }
                }
                sbData.append("\r\n");
            }

            // FERTILIZERS (INORGANIC) Section
            if (!mfArr.isEmpty()) {
                sbData.append("*FERTILIZERS (INORGANIC)\r\n");
                sbData.append("@F FDATE  FMCD  FACD  FDEP  FAMN  FAMP  FAMK  FAMC  FAMO  FOCD FERNAME\r\n");

                for (int idx = 0; idx < mfArr.size(); idx++) {
                    secDataArr = (ArrayList) mfArr.get(idx);

                    for (int i = 0; i < secDataArr.size(); i++) {
                        secData = (LinkedHashMap) secDataArr.get(i);
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$s\r\n",
                                idx + 1, //getObjectOr(data, "fe", defValI).toString(),
                                formatDateStr(getObjectOr(secData, "date", defValD).toString()), // P.S. fdate -> date
                                getObjectOr(secData, "fecd", defValC).toString(),
                                getObjectOr(secData, "feacd", defValC).toString(),
                                formatNumStr(5, secData, "fedep", defValR),
                                formatNumStr(5, secData, "feamn", defValR),
                                formatNumStr(5, secData, "feamp", defValR),
                                formatNumStr(5, secData, "feamk", defValR),
                                formatNumStr(5, secData, "feamc", defValR),
                                formatNumStr(5, secData, "feamo", defValR),
                                getObjectOr(secData, "feocd", defValC).toString(),
                                getObjectOr(secData, "fe_name", defValC).toString()));

                    }
                }
                sbData.append("\r\n");
            }

            // RESIDUES AND ORGANIC FERTILIZER Section
            if (!mrArr.isEmpty()) {
                sbData.append("*RESIDUES AND ORGANIC FERTILIZER\r\n");
                sbData.append("@R RDATE  RCOD  RAMT  RESN  RESP  RESK  RINP  RDEP  RMET RENAME\r\n");

                for (int idx = 0; idx < mrArr.size(); idx++) {
                    secDataArr = (ArrayList) mrArr.get(idx);

                    for (int i = 0; i < secDataArr.size(); i++) {
                        secData = (LinkedHashMap) secDataArr.get(i);
                        sbData.append(String.format("%1$2s %2$5s %3$-5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$s\r\n",
                                idx + 1, //getObjectOr(secData, "om", defValI).toString(),
                                formatDateStr(getObjectOr(secData, "date", defValD).toString()), // P.S. omdat -> date
                                getObjectOr(secData, "omcd", defValC).toString(),
                                formatNumStr(5, secData, "omamt", defValR),
                                formatNumStr(5, secData, "omnpct", defValR),
                                formatNumStr(5, secData, "omppct", defValR),
                                formatNumStr(5, secData, "omkpct", defValR),
                                formatNumStr(5, secData, "ominp", defValR),
                                formatNumStr(5, secData, "omdep", defValR),
                                formatNumStr(5, secData, "omacd", defValR),
                                getObjectOr(secData, "om_name", defValC).toString()));
                    }
                }
                sbData.append("\r\n");
            }

            // CHEMICAL APPLICATIONS Section
            if (!mcArr.isEmpty()) {
                sbData.append("*CHEMICAL APPLICATIONS\r\n");
                sbData.append("@C CDATE CHCOD CHAMT  CHME CHDEP   CHT..CHNAME\r\n");

                for (int idx = 0; idx < mcArr.size(); idx++) {
                    secDataArr = (ArrayList) mcArr.get(idx);

                    for (int i = 0; i < secDataArr.size(); i++) {
                        secData = (LinkedHashMap) secDataArr.get(i);
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s  %8$s\r\n",
                                idx + 1, //getObjectOr(secData, "ch", defValI).toString(),
                                formatDateStr(getObjectOr(secData, "date", defValD).toString()), // P.S. cdate -> date
                                getObjectOr(secData, "chcd", defValC).toString(),
                                formatNumStr(5, secData, "chamt", defValR),
                                getObjectOr(secData, "chacd", defValC).toString(),
                                getObjectOr(secData, "chdep", defValC).toString(),
                                getObjectOr(secData, "ch_targets", defValC).toString(),
                                getObjectOr(secData, "ch_name", defValC).toString()));
                    }
                }
                sbData.append("\r\n");
            }

            // TILLAGE Section
            if (!mtArr.isEmpty()) {
                sbData.append("*TILLAGE AND ROTATIONS\r\n");
                sbData.append("@T TDATE TIMPL  TDEP TNAME\r\n");

                for (int idx = 0; idx < mtArr.size(); idx++) {
                    secDataArr = (ArrayList) mtArr.get(idx);

                    for (int i = 0; i < secDataArr.size(); i++) {
                        secData = (LinkedHashMap) secDataArr.get(i);
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$s\r\n",
                                idx + 1, //getObjectOr(secData, "ti", defValI).toString(),
                                formatDateStr(getObjectOr(secData, "date", defValD).toString()), // P.S. tdate -> date
                                getObjectOr(secData, "tiimp", defValC).toString(),
                                formatNumStr(5, secData, "tidep", defValR),
                                getObjectOr(secData, "ti_name", defValC).toString()));

                    }
                }
                sbData.append("\r\n");
            }

            // ENVIRONMENT MODIFICATIONS Section
            if (!meArr.isEmpty()) {
                sbData.append("*ENVIRONMENT MODIFICATIONS\r\n");
                sbData.append("@E ODATE EDAY  ERAD  EMAX  EMIN  ERAIN ECO2  EDEW  EWIND ENVNAME\r\n");

                for (int idx = 0; idx < meArr.size(); idx++) {
                    secDataArr = (ArrayList) meArr.get(idx);

                    for (int i = 0; i < secDataArr.size(); i++) {
                        sbData.append(String.format("%1$2s%2$s\r\n",
                                idx + 1,
                                (String) secDataArr.get(i)));
//                        sbData.append(String.format("%1$2s %2$5s %3$-1s%4$4s %5$-1s%6$4s %7$-1s%8$4s %9$-1s%10$4s %11$-1s%12$4s %13$-1s%14$4s %15$-1s%16$4s %17$-1s%18$4s %19$s\r\n",
//                                idx + 1, //getObjectOr(secData, "em", defValI).toString(),
//                                formatDateStr(getObjectOr(secData, "date", defValD).toString()), // P.S. emday -> date
//                                getObjectOr(secData, "ecdyl", defValBlank).toString(),
//                                formatNumStr(4, getObjectOr(secData, "emdyl", defValR),
//                                getObjectOr(secData, "ecrad", defValBlank).toString(),
//                                formatNumStr(4, getObjectOr(secData, "emrad", defValR),
//                                getObjectOr(secData, "ecmax", defValBlank).toString(),
//                                formatNumStr(4, getObjectOr(secData, "emmax", defValR),
//                                getObjectOr(secData, "ecmin", defValBlank).toString(),
//                                formatNumStr(4, getObjectOr(secData, "emmin", defValR),
//                                getObjectOr(secData, "ecrai", defValBlank).toString(),
//                                formatNumStr(4, getObjectOr(secData, "emrai", defValR),
//                                getObjectOr(secData, "ecco2", defValBlank).toString(),
//                                formatNumStr(4, getObjectOr(secData, "emco2", defValR),
//                                getObjectOr(secData, "ecdew", defValBlank).toString(),
//                                formatNumStr(4, getObjectOr(secData, "emdew", defValR),
//                                getObjectOr(secData, "ecwnd", defValBlank).toString(),
//                                formatNumStr(4, getObjectOr(secData, "emwnd", defValR),
//                                getObjectOr(secData, "em_name", defValC).toString()));

                    }
                }
                sbData.append("\r\n");
            }

            // HARVEST DETAILS Section
            if (!mhArr.isEmpty()) {
                sbData.append("*HARVEST DETAILS\r\n");
                sbData.append("@H HDATE  HSTG  HCOM HSIZE   HPC  HBPC HNAME\r\n");

                for (int idx = 0; idx < mhArr.size(); idx++) {
                    secDataArr = (ArrayList) mhArr.get(idx);

                    for (int i = 0; i < secDataArr.size(); i++) {
                        secData = (LinkedHashMap) secDataArr.get(i);
                        sbData.append(String.format("%1$2s %2$5s %3$-5s %4$-5s %5$-5s %6$5s %7$5s %8$s\r\n",
                                idx + 1, //getObjectOr(secData, "ha", defValI).toString(),
                                formatDateStr(getObjectOr(secData, "date", defValD).toString()), // P.S. hdate -> date
                                getObjectOr(secData, "hastg", defValC).toString(),
                                getObjectOr(secData, "hacom", defValC).toString(),
                                getObjectOr(secData, "hasiz", defValC).toString(),
                                formatNumStr(5, secData, "hapc", defValR),
                                formatNumStr(5, secData, "habpc", defValR),
                                getObjectOr(secData, "ha_name", defValC).toString()));

                    }
                }
                sbData.append("\r\n");
            }

            // SIMULATION CONTROLS and AUTOMATIC MANAGEMENT Section
            if (!smArr.isEmpty()) {

                // Set Title list
                ArrayList smTitles = new ArrayList();
                smTitles.add("@N GENERAL     NYERS NREPS START SDATE RSEED SNAME.................... SMODEL\r\n");
                smTitles.add("@N OPTIONS     WATER NITRO SYMBI PHOSP POTAS DISES  CHEM  TILL   CO2\r\n");
                smTitles.add("@N METHODS     WTHER INCON LIGHT EVAPO INFIL PHOTO HYDRO NSWIT MESOM MESEV MESOL\r\n");
                smTitles.add("@N MANAGEMENT  PLANT IRRIG FERTI RESID HARVS\r\n");
                smTitles.add("@N OUTPUTS     FNAME OVVEW SUMRY FROPT GROUT CAOUT WAOUT NIOUT MIOUT DIOUT VBOSE CHOUT OPOUT\r\n");
                smTitles.add("@  AUTOMATIC MANAGEMENT\r\n@N PLANTING    PFRST PLAST PH2OL PH2OU PH2OD PSTMX PSTMN\r\n");
                smTitles.add("@N IRRIGATION  IMDEP ITHRL ITHRU IROFF IMETH IRAMT IREFF\r\n");
                smTitles.add("@N NITROGEN    NMDEP NMTHR NAMNT NCODE NAOFF\r\n");
                smTitles.add("@N RESIDUES    RIPCN RTIME RIDEP\r\n");
                smTitles.add("@N HARVEST     HFRST HLAST HPCNP HPCNR\r\n");

                // Loop all the simulation control records
                for (int idx = 0; idx < smArr.size(); idx++) {
                    secData = (LinkedHashMap) smArr.get(idx);

                    if (secData.containsKey("sm_general")) {
                        sbData.append("*SIMULATION CONTROLS\r\n");
                        Object[] keys = secData.keySet().toArray();
                        for (int i = 0; i < keys.length; i++) {

                            sbData.append(smTitles.get(i));
                            sbData.append(String.format("%2s ", idx + 1)).append(((String) secData.get(keys[i]))).append("\r\n");
                            if (i == 4) {
                                sbData.append("\r\n");
                            }
                        }
                    } else {
                        sbData.append(createSMMAStr(idx + 1, expData, secData));
                    }
                }

            } else {
                sbData.append(createSMMAStr(1, expData, new LinkedHashMap()));
            }

            // Output finish
            bwX.write(sbError.toString());
            bwX.write(sbData.toString());
            bwX.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Create string of Simulation Control and Automatic Management Section
     * 
     * @param smid      simulation index number
     * @param expData   date holder for experiment data
     * @param trData    date holder for one treatment data
     * @return      date string with format of "yyddd" 
     */
    private String createSMMAStr(int smid, LinkedHashMap expData, LinkedHashMap trData) {

        StringBuilder sb = new StringBuilder();
        String nitro = "Y";
        String water = "Y";
        String sdate;
        String sm = String.format("%2d", smid);
        ArrayList<LinkedHashMap> dataArr;
        LinkedHashMap subData;
        
        // Check if the meta data of fertilizer is not "N" ("Y" or null)
        if (!getValueOr(expData, "fertilizer", "").equals("N")) {

            // Check if necessary data is missing in all the event records
            dataArr = (ArrayList) getObjectOr(trData, "fertilizer", new ArrayList());
            for (int i = 0; i < dataArr.size(); i++) {
                subData = dataArr.get(i);
                if (getValueOr(subData, "date", "").equals("")
                        || getValueOr(subData, "fecd", "").equals("")
                        || getValueOr(subData, "feacd", "").equals("")
                        || getValueOr(subData, "feamn", "").equals("")) {
                    nitro = "N";
                    break;
                }
            }
        }

        // Check if the meta data of irrigation is not "N" ("Y" or null)
        if (!getValueOr(expData, "irrigation", "").equals("N")) {

            // Check if necessary data is missing in all the event records
            dataArr = (ArrayList) getObjectOr(trData, "irrigation", new ArrayList());
            for (int i = 0; i < dataArr.size(); i++) {
                subData = dataArr.get(i);
                if (getValueOr(subData, "date", "").equals("")
                        || getValueOr(subData, "irval", "").equals("")) {
                    water = "N";
                    break;
                }
            }
        }

        sdate = getObjectOr(expData, "sdat", "").toString();
        if (sdate.equals("")) {
            subData = (LinkedHashMap) getObjectOr(trData, "planting", new LinkedHashMap());
            sdate = getValueOr(subData, "date", defValD);
        }
        sdate = formatDateStr(sdate);


        sb.append("*SIMULATION CONTROLS\r\n");
        sb.append("@N GENERAL     NYERS NREPS START SDATE RSEED SNAME.................... SMODEL\r\n");
        sb.append(sm).append(" GE              1     1     S ").append(sdate).append("  2150 DEFAULT SIMULATION CONTROL\r\n");
        sb.append("@N OPTIONS     WATER NITRO SYMBI PHOSP POTAS DISES  CHEM  TILL   CO2\r\n");
        sb.append(sm).append(" OP              ").append(water).append("     ").append(nitro).append("     Y     N     N     N     N     Y     M\r\n");
        sb.append("@N METHODS     WTHER INCON LIGHT EVAPO INFIL PHOTO HYDRO NSWIT MESOM MESEV MESOL\r\n");
        sb.append(sm).append(" ME              M     M     E     R     S     L     R     1     G     S     2\r\n");
        sb.append("@N MANAGEMENT  PLANT IRRIG FERTI RESID HARVS\r\n");
        sb.append(sm).append(" MA              R     R     R     R     M\r\n");
        sb.append("@N OUTPUTS     FNAME OVVEW SUMRY FROPT GROUT CAOUT WAOUT NIOUT MIOUT DIOUT VBOSE CHOUT OPOUT\r\n");
        sb.append(sm).append(" OU              N     Y     Y     1     Y     Y     Y     Y     Y     N     Y     N     Y\r\n\r\n");
        sb.append("@  AUTOMATIC MANAGEMENT\r\n");
        sb.append("@N PLANTING    PFRST PLAST PH2OL PH2OU PH2OD PSTMX PSTMN\r\n");
        sb.append(sm).append(" PL          82050 82064    40   100    30    40    10\r\n");
        sb.append("@N IRRIGATION  IMDEP ITHRL ITHRU IROFF IMETH IRAMT IREFF\r\n");
        sb.append(sm).append(" IR             30    50   100 GS000 IR001    10  1.00\r\n");
        sb.append("@N NITROGEN    NMDEP NMTHR NAMNT NCODE NAOFF\r\n");
        sb.append(sm).append(" NI             30    50    25 FE001 GS000\r\n");
        sb.append("@N RESIDUES    RIPCN RTIME RIDEP\r\n");
        sb.append(sm).append(" RE            100     1    20\r\n");
        sb.append("@N HARVEST     HFRST HLAST HPCNP HPCNR\r\n");
        sb.append(sm).append(" HA              0 83057   100     0\r\n");

        return sb.toString();
    }

    /**
     * Set default value for missing data
     * 
     */
    private void setDefVal() {

        defValR = "-99";
        defValC = "-99";
        defValI = "-99";
    }

    /**
     * Get index value of the record and set new id value in the array
     * 
     * @param m     sub data
     * @param arr   array of sub data
     * @return       current index value of the sub data
     */
    private int setSecDataArr(LinkedHashMap m, ArrayList arr) {

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
     * @param inArr     sub array of data
     * @param outArr    array of sub data
     * @return          current index value of the sub data
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
     * @param expData   experiment data holder
     * @return          the boolean value for if plot info exists
     */
    private boolean isPlotInfoExist(Map expData) {

        String[] plotIds = {"plta", "pltrno", "pltln", "pldr", "pltsp", "plot_layout", "pltha", "plthno", "plthl", "plthm"};
        for (int i = 0; i < plotIds.length; i++) {
            if (!getValueOr(expData, plotIds[i], "").equals("")) {
                return true;
            }
        }
        return false;
    }

    /**
     * To check if there is soil analysis info data existed in the experiment
     * 
     * @param expData   initial condition layer data array
     * @return          the boolean value for if soil analysis info exists
     */
    private boolean isSoilAnalysisExist(ArrayList<LinkedHashMap> icSubArr) {

        for (int i = 0; i < icSubArr.size(); i++) {
            if (!getValueOr(icSubArr.get(i), "sasc", "").equals("")) {
                return true;
            }

        }
        return false;
    }

    /**
     * Get sub data array from experiment data object
     * 
     * @param expData       experiment data holder
     * @param blockName     top level block name
     * @param dataListName  sub array name
     * @return              sub data array
     */
    private ArrayList<LinkedHashMap> getDataList(Map expData, String blockName, String dataListName) {
        LinkedHashMap dataBlock = getObjectOr(expData, blockName, new LinkedHashMap());
        return getObjectOr(dataBlock, dataListName, new ArrayList<LinkedHashMap>());
    }
}
