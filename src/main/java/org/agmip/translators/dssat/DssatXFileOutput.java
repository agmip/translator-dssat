package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

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
        LinkedHashMap<String, Object> data;       // Data holder for section data
        BufferedWriter bwX;                          // output object
        StringBuilder sbData = new StringBuilder();     // construct the data info in the output
        StringBuilder eventPart2 = new StringBuilder();                   // output string for second part of event data
        LinkedHashMap secData;
        ArrayList subDataArr;                       // Arraylist for event data holder
        LinkedHashMap subData;
        ArrayList secDataArr;                       // Arraylist for section data holder
        LinkedHashMap trData = new LinkedHashMap();
//        int trmnNum;                            // total numbers of treatment in the data holder
        int cuNum = 0;                              // total numbers of cultivars in the data holder
        int flNum = 0;                              // total numbers of fields in the data holder
        int saNum = 0;                              // total numbers of soil analysis in the data holder
        int icNum = 0;                              // total numbers of initial conditions in the data holder
        int mpNum = 0;                              // total numbers of plaintings in the data holder
        int miNum = 0;                              // total numbers of irrigations in the data holder
        int mfNum = 0;                              // total numbers of fertilizers in the data holder
        int mrNum = 0;                              // total numbers of residues in the data holder
        int mcNum = 0;                              // total numbers of chemical in the data holder
        int mtNum = 0;                              // total numbers of tillage in the data holder
        int meNum = 0;                              // total numbers of enveronment modification in the data holder
        int mhNum = 0;                              // total numbers of harvest in the data holder
        int smNum = 0;                              // total numbers of simulation controll record
        ArrayList trArr = new ArrayList();   // array for treatment record
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
            data = (LinkedHashMap) getObjectOr(result, "experiment", result);
            if (data == null || data.isEmpty()) {
                return;
            }
            decompressData(data);
            setDefVal(data);

            // Initial BufferedWriter
            exName = getExName(result);
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
            sbData.append(String.format("*EXP.DETAILS: %1$-10s %2$s\r\n\r\n", exName, getObjectOr(data, "local_name", defValC).toString()));

            // GENERAL Section
            sbData.append("*GENERAL\r\n");
            // People
            sbData.append(String.format("@PEOPLE\r\n %1$s\r\n", getObjectOr(data, "people", defValC).toString()));
            // Address
            if (getObjectOr(data, "institutes", defValC).toString().equals("")) {
                sbData.append(String.format("@ADDRESS\r\n %3$s, %2$s, %1$s\r\n",
                        getObjectOr(data, "fl_loc_1", defValC).toString(),
                        getObjectOr(data, "fl_loc_2", defValC).toString(),
                        getObjectOr(data, "fl_loc_3", defValC).toString()));
            } else {
                sbData.append(String.format("@ADDRESS\r\n %1$s\r\n", getObjectOr(data, "institutes", defValC).toString()));
            }

            // Site
            sbData.append(String.format("@SITE\r\n %1$s\r\n", getObjectOr(data, "site", defValC).toString()));
            // Plot Info
            LinkedHashMap plotData = (LinkedHashMap) getObjectOr(data, "plot_info", new LinkedHashMap());
            if (!plotData.isEmpty()) {

                sbData.append("@ PAREA  PRNO  PLEN  PLDR  PLSP  PLAY HAREA  HRNO  HLEN  HARM.........\r\n");
                sbData.append(String.format(" %1$6s %2$5s %3$5s %4$5s %5$5s %6$-5s %7$5s %8$5s %9$5s %10$-15s\r\n",
                        formatNumStr(6, getObjectOr(plotData, "parea", defValR).toString()),
                        formatNumStr(5, getObjectOr(plotData, "prno", defValI).toString()),
                        formatNumStr(5, getObjectOr(plotData, "plen", defValR).toString()),
                        formatNumStr(5, getObjectOr(plotData, "pldr", defValI).toString()),
                        formatNumStr(5, getObjectOr(plotData, "plsp", defValI).toString()),
                        getObjectOr(plotData, "play", defValC).toString(),
                        formatNumStr(5, getObjectOr(plotData, "pltha", defValR).toString()),
                        formatNumStr(5, getObjectOr(plotData, "hrno", defValI).toString()),
                        formatNumStr(5, getObjectOr(plotData, "hlen", defValR).toString()),
                        getObjectOr(plotData, "plthm", defValC).toString()));
            }
            // Notes
            if (data.containsKey("notes")) {
                sbData.append("@NOTES\r\n");
                String notes = getObjectOr(data, "notes", defValC).toString();

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
            trArr = (ArrayList) getObjectOr(data, "management", new ArrayList());
            if (trArr.isEmpty()) {
                sbError.append("! Warning: There is no treatment data in the experiment!\r\n");
            } else {
                sbData.append("*TREATMENTS                        -------------FACTOR LEVELS------------\r\n");
                sbData.append("@N R O C TNAME.................... CU FL SA IC MP MI MF MR MC MT ME MH SM\r\n");
            }

            for (int i = 0; i < trArr.size(); i++) {
                trData = (LinkedHashMap) trArr.get(i);

                cuNum = setSecDataArr((LinkedHashMap) getObjectOr(trData, "cultivar", new LinkedHashMap()), cuArr);
                flNum = setSecDataArr((LinkedHashMap) getObjectOr(trData, "field", new LinkedHashMap()), flArr);
                saNum = setSecDataArr((LinkedHashMap) getObjectOr(trData, "soil_analysis", new LinkedHashMap()), saArr);
                icNum = setSecDataArr((LinkedHashMap) getObjectOr(trData, "initial_condition", new LinkedHashMap()), icArr);
                mpNum = setSecDataArr((LinkedHashMap) getObjectOr(trData, "plant", new LinkedHashMap()), mpArr);
                miNum = setSecDataArr((LinkedHashMap) getObjectOr(trData, "irrigation", new LinkedHashMap()), miArr);
                mfNum = setSecDataArr((ArrayList) getObjectOr(trData, "fertilizer", new ArrayList()), mfArr);
                mrNum = setSecDataArr((ArrayList) getObjectOr(trData, "residue_organic", new ArrayList()), mrArr);
                mcNum = setSecDataArr((ArrayList) getObjectOr(trData, "chemical", new ArrayList()), mcArr);
                mtNum = setSecDataArr((ArrayList) getObjectOr(trData, "tillage", new ArrayList()), mtArr);
                meNum = setSecDataArr((ArrayList) getObjectOr(trData, "emvironment", new ArrayList()), meArr);
                mhNum = setSecDataArr((ArrayList) getObjectOr(trData, "harvest", new ArrayList()), mhArr);
                smNum = setSecDataArr((LinkedHashMap) getObjectOr(trData, "simulation", new LinkedHashMap()), smArr);
                if (smNum == 0) {
                    smNum = 1;
                }

                sbData.append(String.format("%1$2s %2$1s %3$1s %4$1s %5$-25s %6$2s %7$2s %8$2s %9$2s %10$2s %11$2s %12$2s %13$2s %14$2s %15$2s %16$2s %17$2s %18$2s\r\n",
                        getObjectOr(trData, "trno", i + 1).toString(),
                        getObjectOr(trData, "sq", "1").toString(), // P.S. default value here is based on document DSSAT vol2.pdf
                        getObjectOr(trData, "op", "1").toString(),
                        getObjectOr(trData, "co", "0").toString(),
                        getObjectOr(trData, "tr_name", defValC).toString(),
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
                            getObjectOr(secData, "crid", defValC).toString(),
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
                if (getObjectOr(secData, "wsta_id", "").equals("")) {
                    sbError.append("! Warning: Incompleted record because missing data : [wsta_id]\r\n");
                } else if (getObjectOr(secData, "soil_id", "").equals("")) {
                    sbError.append("! Warning: Incompleted record because missing data : [soil_id]\r\n");
                }
                sbData.append(String.format("%1$2s %2$-8s %3$-8s %4$5s %5$5s %6$-5s %7$5s %8$5s %9$-5s %10$-5s%11$5s  %12$-10s %13$s\r\n", // P.S. change length definition to match current way
                        idx + 1, //getObjectOr(secData, "fl", defValI).toString(),
                        getObjectOr(secData, "id_field", defValC).toString(),
                        getObjectOr(secData, "wsta_id", defValC).toString(),
                        getObjectOr(secData, "flsl", defValC).toString(),
                        formatNumStr(5, getObjectOr(secData, "flob", defValR).toString()),
                        getObjectOr(secData, "fl_drntype", defValC).toString(),
                        formatNumStr(5, getObjectOr(secData, "fldrd", defValR).toString()),
                        formatNumStr(5, getObjectOr(secData, "fldrs", defValR).toString()),
                        getObjectOr(secData, "flst", defValC).toString(),
                        getObjectOr(secData, "sltx", defValC).toString(),
                        formatNumStr(5, getObjectOr(secData, "sldp", defValR).toString()),
                        getObjectOr(secData, "soil_id", defValC).toString(),
                        getObjectOr(secData, "fl_name", defValC).toString()));

                eventPart2.append(String.format("%1$2s %2$15s %3$15s %4$9s %5$17s %6$5s %7$5s %8$5s %9$5s %10$5s\r\n",
                        idx + 1, //getObjectOr(secData, "fl", defValI).toString(),
                        formatNumStr(15, getObjectOr(secData, "fl_lat", defValR).toString()),
                        formatNumStr(15, getObjectOr(secData, "fl_long", defValR).toString()),
                        formatNumStr(9, getObjectOr(secData, "flele", defValR).toString()),
                        formatNumStr(17, getObjectOr(secData, "farea", defValR).toString()),
                        formatNumStr(5, getObjectOr(data, "slen", defValR).toString()), // P.S. keep -99
                        formatNumStr(5, getObjectOr(secData, "fllwr", defValR).toString()),
                        formatNumStr(5, getObjectOr(secData, "flsla", defValR).toString()),
                        getObjectOr(secData, "flhst", defValC).toString(),
                        formatNumStr(5, getObjectOr(secData, "fhdur", defValR).toString())));
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

                    subDataArr = (ArrayList) getObjectOr(secData, "data", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        sbData.append("@A  SABL  SADM  SAOC  SANI SAPHW SAPHB  SAPX  SAKE  SASC\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = (LinkedHashMap) subDataArr.get(j);
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s\r\n",
                                idx + 1, //getObjectOr(subData, "sa", defValI).toString(),
                                formatNumStr(5, getObjectOr(subData, "sabl", defValR).toString()),
                                formatNumStr(5, getObjectOr(subData, "sabdm", defValR).toString()),
                                formatNumStr(5, getObjectOr(subData, "saoc", defValR).toString()),
                                formatNumStr(5, getObjectOr(subData, "sani", defValR).toString()),
                                formatNumStr(5, getObjectOr(subData, "saphw", defValR).toString()),
                                formatNumStr(5, getObjectOr(subData, "saphb", defValR).toString()),
                                formatNumStr(5, getObjectOr(subData, "sapx", defValR).toString()),
                                formatNumStr(5, getObjectOr(subData, "sake", defValR).toString()),
                                formatNumStr(5, getObjectOr(subData, "sasc", defValR).toString())));
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
                            formatDateStr(getObjectOr(secData, "icdat", defValD).toString()),
                            formatNumStr(5, getObjectOr(secData, "icrt", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "icnd", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "icrz#", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "icrze", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "icwt", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "icrag", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "icrn", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "icrp", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "icrip", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "icrdp", defValR).toString()),
                            getObjectOr(secData, "ic_name", defValC).toString()));

                    subDataArr = (ArrayList) getObjectOr(secData, "data", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        sbData.append("@C  ICBL  SH2O  SNH4  SNO3\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = (LinkedHashMap) subDataArr.get(j);
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s\r\n",
                                idx + 1, //getObjectOr(subData, "ic", defValI).toString(),
                                formatNumStr(5, getObjectOr(subData, "icbl", defValR).toString()),
                                formatNumStr(5, getObjectOr(subData, "ich2o", defValR).toString()),
                                formatNumStr(5, getObjectOr(subData, "icnh4", defValR).toString()),
                                formatNumStr(5, getObjectOr(subData, "icno3", defValR).toString())));
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
                    if (getObjectOr(secData, "pdate", "").equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [pdate]\r\n");
                    } else if (getObjectOr(secData, "plpop", getObjectOr(secData, "plpoe", "")).equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [plpop] and [plpoe]\r\n");
                    }
                    if (getObjectOr(secData, "plrs", "").equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [plrs]\r\n");
                    }
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s     %6$-1s     %7$-1s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s %15$5s                        %16$s\r\n",
                            idx + 1, //getObjectOr(data, "pl", defValI).toString(),
                            formatDateStr(getObjectOr(secData, "pdate", defValD).toString()),
                            formatDateStr(getObjectOr(secData, "pldae", defValD).toString()),
                            formatNumStr(5, getObjectOr(secData, "plpop", getObjectOr(secData, "plpoe", defValR)).toString()),
                            formatNumStr(5, getObjectOr(secData, "plpoe", getObjectOr(secData, "plpop", defValR)).toString()),
                            getObjectOr(secData, "plme", defValC).toString(),
                            getObjectOr(secData, "plds", defValC).toString(),
                            formatNumStr(5, getObjectOr(secData, "plrs", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "plrd", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "pldp", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "plmwt", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "page", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "penv", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "plph", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "plspl", defValR).toString()),
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

                    secData = (LinkedHashMap) miArr.get(idx);
                    subDataArr = (ArrayList) getObjectOr(secData, "data", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        subData = (LinkedHashMap) subDataArr.get(0);
                    } else {
                        subData = new LinkedHashMap();
                    }
                    sbData.append("@I  EFIR  IDEP  ITHR  IEPT  IOFF  IAME  IAMT IRNAME\r\n");
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$s\r\n",
                            idx + 1, //getObjectOr(data, "ir", defValI).toString(),
                            formatNumStr(5, getObjectOr(subData, "ireff", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "irmdp", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "irthr", defValR).toString()),
                            formatNumStr(5, getObjectOr(secData, "irept", defValR).toString()),
                            getObjectOr(secData, "irstg", defValC).toString(),
                            getObjectOr(secData, "iame", defValC).toString(),
                            formatNumStr(5, getObjectOr(secData, "iamt", defValR).toString()),
                            getObjectOr(secData, "ir_name", defValC).toString()));

                    if (!subDataArr.isEmpty()) {
                        sbData.append("@I IDATE  IROP IRVAL\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = (LinkedHashMap) subDataArr.get(j);
                        sbData.append(String.format("%1$2s %2$5s %3$-5s %4$5s\r\n",
                                idx + 1, //getObjectOr(subData, "ir", defValI).toString(),
                                formatDateStr(getObjectOr(subData, "idate", defValD).toString()),
                                getObjectOr(subData, "irop", defValC).toString(),
                                formatNumStr(5, getObjectOr(subData, "irval", defValR).toString())));
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
                        sbData.append(String.format("%1$2s %2$5s %3$-5s %4$-5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$s\r\n",
                                idx + 1, //getObjectOr(data, "fe", defValI).toString(),
                                formatDateStr(getObjectOr(secData, "fdate", defValD).toString()),
                                getObjectOr(secData, "fecd", defValC).toString(),
                                getObjectOr(secData, "feacd", defValC).toString(),
                                formatNumStr(5, getObjectOr(secData, "fedep", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "feamn", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "feamp", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "feamk", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "feamc", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "feamo", defValR).toString()),
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
                                formatDateStr(getObjectOr(secData, "omdat", defValD).toString()),
                                getObjectOr(secData, "omcd", defValC).toString(),
                                formatNumStr(5, getObjectOr(secData, "omamt", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "omn%", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "omp%", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "omk%", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "ominp", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "omdep", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "omacd", defValR).toString()),
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
                                formatDateStr(getObjectOr(secData, "cdate", defValD).toString()),
                                getObjectOr(secData, "chcd", defValC).toString(),
                                formatNumStr(5, getObjectOr(secData, "chamt", defValR).toString()),
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
                                formatDateStr(getObjectOr(secData, "tdate", defValD).toString()),
                                getObjectOr(secData, "tiimp", defValC).toString(),
                                formatNumStr(5, getObjectOr(secData, "tidep", defValR).toString()),
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
                        secData = (LinkedHashMap) secDataArr.get(i);
                        sbData.append(String.format("%1$2s %2$5s %3$-1s%4$4s %5$-1s%6$4s %7$-1s%8$4s %9$-1s%10$4s %11$-1s%12$4s %13$-1s%14$4s %15$-1s%16$4s %17$-1s%18$4s %19$s\r\n",
                                idx + 1, //getObjectOr(secData, "em", defValI).toString(),
                                formatDateStr(getObjectOr(secData, "emday", defValD).toString()),
                                getObjectOr(secData, "ecdyl", defValC).toString(),
                                formatNumStr(4, getObjectOr(secData, "emdyl", defValR).toString()),
                                getObjectOr(secData, "ecrad", defValC).toString(),
                                formatNumStr(4, getObjectOr(secData, "emrad", defValR).toString()),
                                getObjectOr(secData, "ecmax", defValC).toString(),
                                formatNumStr(4, getObjectOr(secData, "emmax", defValR).toString()),
                                getObjectOr(secData, "ecmin", defValC).toString(),
                                formatNumStr(4, getObjectOr(secData, "emmin", defValR).toString()),
                                getObjectOr(secData, "ecrai", defValC).toString(),
                                formatNumStr(4, getObjectOr(secData, "emrai", defValR).toString()),
                                getObjectOr(secData, "ecco2", defValC).toString(),
                                formatNumStr(4, getObjectOr(secData, "emco2", defValR).toString()),
                                getObjectOr(secData, "ecdew", defValC).toString(),
                                formatNumStr(4, getObjectOr(secData, "emdew", defValR).toString()),
                                getObjectOr(secData, "ecwnd", defValC).toString(),
                                formatNumStr(4, getObjectOr(secData, "emwnd", defValR).toString()),
                                getObjectOr(secData, "em_name", defValC).toString()));

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
                                formatDateStr(getObjectOr(secData, "hdate", defValD).toString()),
                                getObjectOr(secData, "hastg", defValC).toString(),
                                getObjectOr(secData, "hacom", defValC).toString(),
                                getObjectOr(secData, "hasiz", defValC).toString(),
                                formatNumStr(5, getObjectOr(secData, "hapc", defValR).toString()),
                                formatNumStr(5, getObjectOr(secData, "habpc", defValR).toString()),
                                getObjectOr(secData, "ha_name", defValC).toString()));

                    }
                }
                sbData.append("\r\n");
            }

            // SIMULATION CONTROLS and AUTOMATIC MANAGEMENT Section
            if (!smArr.isEmpty()) {

                // Set Title list
                ArrayList smTitles = new ArrayList();
                smTitles.add("@N GENERAL     NYERS NREPS START SDATE RSEED SNAME....................\r\n");
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
                    subDataArr = (ArrayList) getObjectOr(secData, "data", new ArrayList());

                    if (!subDataArr.isEmpty() && subDataArr.size() >= smTitles.size()) {
                        sbData.append("*SIMULATION CONTROLS\r\n");

                        for (int j = 0; j < subDataArr.size(); j++) {
                            sbData.append(smTitles.get(j));
                            sbData.append(String.format("%2s", idx + 1)).append(((String) subDataArr.get(j)).substring(2)).append("\r\n");
                            if (j == 4) {
                                sbData.append("\r\n");
                            }
                        }
                    } else {
                        sbData.append(createSMMAStr(idx + 1, data, trData));
                    }
                }

            } else {
                sbData.append(createSMMAStr(1, data, trData));
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
        String nitro;
        String water;
        String sdate;
        String sm = String.format("%2d", smid);
        ArrayList dataArr;
        LinkedHashMap data;
        LinkedHashMap subData;

        dataArr = (ArrayList) getObjectOr(trData, "fertilizer", new ArrayList());
        if (!dataArr.isEmpty()) {
            subData = (LinkedHashMap) dataArr.get(0);
        } else {
            subData = new LinkedHashMap();
        }
        if (!getObjectOr(subData, "fdate", "").toString().equals("")) {
            nitro = "Y";
        } else if (getObjectOr(expData, "fertilizer", "").toString().equals("N")) {
            nitro = "Y";
        } else {
            nitro = "N";
        }

        data = (LinkedHashMap) getObjectOr(trData, "irrigation", new LinkedHashMap());
        dataArr = (ArrayList) getObjectOr(data, "data", new ArrayList());
        if (!dataArr.isEmpty()) {
            subData = (LinkedHashMap) dataArr.get(0);
        } else {
            subData = new LinkedHashMap();
        }
        if (!getObjectOr(subData, "ireff", "").toString().equals("")) {
            water = "Y";
        } else if (getObjectOr(subData, "irrig", "").toString().equals("N")) {
            water = "Y";
        } else {
            water = "N";
        }

        sdate = getObjectOr(expData, "sdat", "").toString();
        if (sdate.equals("")) {
            data = (LinkedHashMap) getObjectOr(trData, "plant", new LinkedHashMap());
            sdate = getObjectOr(data, "pdate", defValD).toString();
        }
        sdate = formatDateStr(sdate);


        sb.append("*SIMULATION CONTROLS\r\n");
        sb.append("@N GENERAL     NYERS NREPS START SDATE RSEED SNAME....................\r\n");
        sb.append(sm).append(" GE              1     1     S ").append(sdate).append("  2150 DEFAULT SIMULATION CONTROL\r\n");
        sb.append("@N OPTIONS     WATER NITRO SYMBI PHOSP POTAS DISES  CHEM  TILL   CO2\r\n");
//          sb.append(" 1 OP              Y     Y     Y     N     N     N     N     Y     M\r\n");
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
     * @param result  date holder for experiment data
     */
    private void setDefVal(LinkedHashMap result) {

        ArrayList trDataArr = (ArrayList) getObjectOr(result, "management", new ArrayList());
        Map trData;
        if (!trDataArr.isEmpty()) {
            trData = (Map) trDataArr.get(0);
        } else {
            trData = new LinkedHashMap();
        }
        Map icData = (Map) trData.get("initial_condition");
        Map mpData = (Map) trData.get("plant");
        String icdat;
        String pdate;
        if (icData != null) {
            icdat = (String) icData.get("icdat");

        } else {
            icdat = null;
        }
        if (mpData != null) {
            pdate = (String) mpData.get("pdate");

        } else {
            pdate = null;
        }

        if (icdat != null && !icdat.equals("")) {
            defValD = icdat;
        } else if (!getObjectOr(result, "sdat", "").toString().equals("")) {
            defValD = getObjectOr(result, "sdat", "").toString();
        } else if (pdate != null && !pdate.equals("")) {
            defValD = pdate;
        } else {
            sbError.append("! Warning: There is noavailable date info in the experiment.\r\n");
            defValD = "20110101";
        }
        defValR = "-99";
        defValC = "-99";    // TODO wait for confirmation
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
}
