package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.agmip.util.JSONAdapter;
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
        JSONAdapter adapter = new JSONAdapter();    // JSON Adapter
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
            data = (LinkedHashMap) getValueOr(result, "experiment", result);
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
            sbData.append(String.format("*EXP.DETAILS: %1$-10s %2$s\r\n\r\n", exName, getValueOr(data, "local_name", defValC).toString()));

            // GENERAL Section
            sbData.append("*GENERAL\r\n");
            // People
            sbData.append(String.format("@PEOPLE\r\n %1$s\r\n", getValueOr(data, "people", defValC).toString()));
            // Address
            if (getValueOr(data, "institutes", defValC).toString().equals("")) {
                sbData.append(String.format("@ADDRESS\r\n %3$s, %2$s, %1$s\r\n",
                        getValueOr(data, "fl_loc_1", defValC).toString(),
                        getValueOr(data, "fl_loc_2", defValC).toString(),
                        getValueOr(data, "fl_loc_3", defValC).toString()));
            } else {
                sbData.append(String.format("@ADDRESS\r\n %1$s\r\n", getValueOr(data, "institutes", defValC).toString()));
            }

            // Site
            sbData.append(String.format("@SITE\r\n %1$s\r\n", getValueOr(data, "site", defValC).toString()));
            // Plot Info
            LinkedHashMap plotData = (LinkedHashMap) getValueOr(data, "plot_info", new LinkedHashMap());
            if (!plotData.isEmpty()) {

                sbData.append("@ PAREA  PRNO  PLEN  PLDR  PLSP  PLAY HAREA  HRNO  HLEN  HARM.........\r\n");
                sbData.append(String.format(" %1$6s %2$5s %3$5s %4$5s %5$5s %6$-5s %7$5s %8$5s %9$5s %10$-15s\r\n",
                        formatNumStr(6, getValueOr(plotData, "parea", defValR).toString()),
                        formatNumStr(5, getValueOr(plotData, "prno", defValI).toString()),
                        formatNumStr(5, getValueOr(plotData, "plen", defValR).toString()),
                        formatNumStr(5, getValueOr(plotData, "pldr", defValI).toString()),
                        formatNumStr(5, getValueOr(plotData, "plsp", defValI).toString()),
                        getValueOr(plotData, "play", defValC).toString(),
                        formatNumStr(5, getValueOr(plotData, "pltha", defValR).toString()),
                        formatNumStr(5, getValueOr(plotData, "hrno", defValI).toString()),
                        formatNumStr(5, getValueOr(plotData, "hlen", defValR).toString()),
                        getValueOr(plotData, "plthm", defValC).toString()));
            }
            // Notes
            if (data.containsKey("notes")) {
                sbData.append("@NOTES\r\n");
                String notes = getValueOr(data, "notes", defValC).toString();

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
            trArr = (ArrayList) getValueOr(data, "treatment", new ArrayList());
            if (trArr.isEmpty()) {
                sbError.append("! Warning: There is no treatment data in the experiment!\r\n");
            } else {
                sbData.append("*TREATMENTS                        -------------FACTOR LEVELS------------\r\n");
                sbData.append("@N R O C TNAME.................... CU FL SA IC MP MI MF MR MC MT ME MH SM\r\n");
            }

            for (int i = 0; i < trArr.size(); i++) {
                trData = (LinkedHashMap) trArr.get(i);

                cuNum = setSecDataArr((LinkedHashMap) getValueOr(trData, "cultivar", new LinkedHashMap()), cuArr);
                flNum = setSecDataArr((LinkedHashMap) getValueOr(trData, "field", new LinkedHashMap()), flArr);
                saNum = setSecDataArr((LinkedHashMap) getValueOr(trData, "soil_analysis", new LinkedHashMap()), saArr);
                icNum = setSecDataArr((LinkedHashMap) getValueOr(trData, "initial_condition", new LinkedHashMap()), icArr);
                mpNum = setSecDataArr((LinkedHashMap) getValueOr(trData, "plant", new LinkedHashMap()), mpArr);
                miNum = setSecDataArr((LinkedHashMap) getValueOr(trData, "irrigation", new LinkedHashMap()), miArr);
                mfNum = setSecDataArr((ArrayList) getValueOr(trData, "fertilizer", new ArrayList()), mfArr);
                mrNum = setSecDataArr((ArrayList) getValueOr(trData, "residue_organic", new ArrayList()), mrArr);
                mcNum = setSecDataArr((ArrayList) getValueOr(trData, "chemical", new ArrayList()), mcArr);
                mtNum = setSecDataArr((ArrayList) getValueOr(trData, "tillage", new ArrayList()), mtArr);
                meNum = setSecDataArr((ArrayList) getValueOr(trData, "emvironment", new ArrayList()), meArr);
                mhNum = setSecDataArr((ArrayList) getValueOr(trData, "harvest", new ArrayList()), mhArr);
                smNum = setSecDataArr((LinkedHashMap) getValueOr(trData, "simulation", new LinkedHashMap()), smArr);
                if (smNum == 0) {
                    smNum = 1;
                }

                sbData.append(String.format("%1$2s %2$1s %3$1s %4$1s %5$-25s %6$2s %7$2s %8$2s %9$2s %10$2s %11$2s %12$2s %13$2s %14$2s %15$2s %16$2s %17$2s %18$2s\r\n",
                        getValueOr(trData, "trno", i + 1).toString(),
                        getValueOr(trData, "sq", "1").toString(), // P.S. default value here is based on document DSSAT vol2.pdf
                        getValueOr(trData, "op", "1").toString(),
                        getValueOr(trData, "co", "0").toString(),
                        getValueOr(trData, "tr_name", defValC).toString(),
                        cuNum, //getValueOr(data, "ge", defValI).toString(), 
                        flNum, //getValueOr(data, "fl", defValI).toString(), 
                        saNum, //getValueOr(data, "sa", defValI).toString(),
                        icNum, //getValueOr(data, "ic", defValI).toString(),
                        mpNum, //getValueOr(data, "pl", defValI).toString(),
                        miNum, //getValueOr(data, "ir", defValI).toString(),
                        mfNum, //getValueOr(data, "fe", defValI).toString(),
                        mrNum, //getValueOr(data, "om", defValI).toString(),
                        mcNum, //getValueOr(data, "ch", defValI).toString(),
                        mtNum, //getValueOr(data, "ti", defValI).toString(),
                        meNum, //getValueOr(data, "em", defValI).toString(),
                        mhNum, //getValueOr(data, "ha", defValI).toString(),
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
                    if (getValueOr(secData, "crid", "").equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [crid]\r\n");
                    } else if (getValueOr(secData, "cul_id", "").equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [cul_id]\r\n");
                    }
                    sbData.append(String.format("%1$2s %2$-2s %3$-6s %4$s\r\n",
                            idx + 1, //getValueOr(secData, "ge", defValI).toString(),
                            getValueOr(secData, "crid", defValC).toString(),
                            getValueOr(secData, "cul_id", defValC).toString(),
                            getValueOr(secData, "cul_name", defValC).toString()));
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
                if (getValueOr(secData, "wsta_id", "").equals("")) {
                    sbError.append("! Warning: Incompleted record because missing data : [wsta_id]\r\n");
                } else if (getValueOr(secData, "soil_id", "").equals("")) {
                    sbError.append("! Warning: Incompleted record because missing data : [soil_id]\r\n");
                }
                sbData.append(String.format("%1$2s %2$-8s %3$-8s %4$5s %5$5s %6$-5s %7$5s %8$5s %9$-5s %10$-5s%11$5s  %12$-10s %13$s\r\n", // P.S. change length definition to match current way
                        idx + 1, //getValueOr(secData, "fl", defValI).toString(),
                        getValueOr(secData, "id_field", defValC).toString(),
                        getValueOr(secData, "wsta_id", defValC).toString(),
                        getValueOr(secData, "flsl", defValC).toString(),
                        formatNumStr(5, getValueOr(secData, "flob", defValR).toString()),
                        getValueOr(secData, "fl_drntype", defValC).toString(),
                        formatNumStr(5, getValueOr(secData, "fldrd", defValR).toString()),
                        formatNumStr(5, getValueOr(secData, "fldrs", defValR).toString()),
                        getValueOr(secData, "flst", defValC).toString(),
                        getValueOr(secData, "sltx", defValC).toString(),
                        formatNumStr(5, getValueOr(secData, "sldp", defValR).toString()),
                        getValueOr(secData, "soil_id", defValC).toString(),
                        getValueOr(secData, "fl_name", defValC).toString()));

                eventPart2.append(String.format("%1$2s %2$15s %3$15s %4$9s %5$17s %6$5s %7$5s %8$5s %9$5s %10$5s\r\n",
                        idx + 1, //getValueOr(secData, "fl", defValI).toString(),
                        formatNumStr(15, getValueOr(secData, "fl_lat", defValR).toString()),
                        formatNumStr(15, getValueOr(secData, "fl_long", defValR).toString()),
                        formatNumStr(9, getValueOr(secData, "flele", defValR).toString()),
                        formatNumStr(17, getValueOr(secData, "farea", defValR).toString()),
                        formatNumStr(5, getValueOr(data, "slen", defValR).toString()), // P.S. keep -99
                        formatNumStr(5, getValueOr(secData, "fllwr", defValR).toString()),
                        formatNumStr(5, getValueOr(secData, "flsla", defValR).toString()),
                        getValueOr(secData, "flhst", defValC).toString(),
                        formatNumStr(5, getValueOr(secData, "fhdur", defValR).toString())));
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
                            idx + 1, //getValueOr(secData, "sa", defValI).toString(),
                            formatDateStr(getValueOr(secData, "sadat", defValD).toString()),
                            getValueOr(secData, "samhb", defValC).toString(),
                            getValueOr(secData, "sampx", defValC).toString(),
                            getValueOr(secData, "samke", defValC).toString(),
                            getValueOr(secData, "sa_name", defValC).toString()));

                    subDataArr = (ArrayList) getValueOr(secData, "data", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        sbData.append("@A  SABL  SADM  SAOC  SANI SAPHW SAPHB  SAPX  SAKE  SASC\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = (LinkedHashMap) subDataArr.get(j);
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s\r\n",
                                idx + 1, //getValueOr(subData, "sa", defValI).toString(),
                                formatNumStr(5, getValueOr(subData, "sabl", defValR).toString()),
                                formatNumStr(5, getValueOr(subData, "sabdm", defValR).toString()),
                                formatNumStr(5, getValueOr(subData, "saoc", defValR).toString()),
                                formatNumStr(5, getValueOr(subData, "sani", defValR).toString()),
                                formatNumStr(5, getValueOr(subData, "saphw", defValR).toString()),
                                formatNumStr(5, getValueOr(subData, "saphb", defValR).toString()),
                                formatNumStr(5, getValueOr(subData, "sapx", defValR).toString()),
                                formatNumStr(5, getValueOr(subData, "sake", defValR).toString()),
                                formatNumStr(5, getValueOr(subData, "sasc", defValR).toString())));
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
                            idx + 1, //getValueOr(secData, "ic", defValI).toString(),
                            getValueOr(secData, "icpcr", defValC).toString(),
                            formatDateStr(getValueOr(secData, "icdat", defValD).toString()),
                            formatNumStr(5, getValueOr(secData, "icrt", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "icnd", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "icrz#", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "icrze", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "icwt", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "icrag", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "icrn", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "icrp", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "icrip", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "icrdp", defValR).toString()),
                            getValueOr(secData, "ic_name", defValC).toString()));

                    subDataArr = (ArrayList) getValueOr(secData, "data", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        sbData.append("@C  ICBL  SH2O  SNH4  SNO3\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = (LinkedHashMap) subDataArr.get(j);
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s\r\n",
                                idx + 1, //getValueOr(subData, "ic", defValI).toString(),
                                formatNumStr(5, getValueOr(subData, "icbl", defValR).toString()),
                                formatNumStr(5, getValueOr(subData, "ich2o", defValR).toString()),
                                formatNumStr(5, getValueOr(subData, "icnh4", defValR).toString()),
                                formatNumStr(5, getValueOr(subData, "icno3", defValR).toString())));
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
                    if (getValueOr(secData, "pdate", "").equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [pdate]\r\n");
                    } else if (getValueOr(secData, "plpop", getValueOr(secData, "plpoe", "")).equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [plpop] and [plpoe]\r\n");
                    }
                    if (getValueOr(secData, "plrs", "").equals("")) {
                        sbError.append("! Warning: Incompleted record because missing data : [plrs]\r\n");
                    }
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s     %6$-1s     %7$-1s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s %15$5s                        %16$s\r\n",
                            idx + 1, //getValueOr(data, "pl", defValI).toString(),
                            formatDateStr(getValueOr(secData, "pdate", defValD).toString()),
                            formatDateStr(getValueOr(secData, "pldae", defValD).toString()),
                            formatNumStr(5, getValueOr(secData, "plpop", getValueOr(secData, "plpoe", defValR)).toString()),
                            formatNumStr(5, getValueOr(secData, "plpoe", getValueOr(secData, "plpop", defValR)).toString()),
                            getValueOr(secData, "plme", defValC).toString(),
                            getValueOr(secData, "plds", defValC).toString(),
                            formatNumStr(5, getValueOr(secData, "plrs", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "plrd", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "pldp", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "plmwt", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "page", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "penv", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "plph", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "plspl", defValR).toString()),
                            getValueOr(secData, "pl_name", defValC).toString()));

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
                    subDataArr = (ArrayList) getValueOr(secData, "data", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        subData = (LinkedHashMap) subDataArr.get(0);
                    } else {
                        subData = new LinkedHashMap();
                    }
                    sbData.append("@I  EFIR  IDEP  ITHR  IEPT  IOFF  IAME  IAMT IRNAME\r\n");
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$s\r\n",
                            idx + 1, //getValueOr(data, "ir", defValI).toString(),
                            formatNumStr(5, getValueOr(subData, "ireff", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "irmdp", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "irthr", defValR).toString()),
                            formatNumStr(5, getValueOr(secData, "irept", defValR).toString()),
                            getValueOr(secData, "irstg", defValC).toString(),
                            getValueOr(secData, "iame", defValC).toString(),
                            formatNumStr(5, getValueOr(secData, "iamt", defValR).toString()),
                            getValueOr(secData, "ir_name", defValC).toString()));

                    if (!subDataArr.isEmpty()) {
                        sbData.append("@I IDATE  IROP IRVAL\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = (LinkedHashMap) subDataArr.get(j);
                        sbData.append(String.format("%1$2s %2$5s %3$-5s %4$5s\r\n",
                                idx + 1, //getValueOr(subData, "ir", defValI).toString(),
                                formatDateStr(getValueOr(subData, "idate", defValD).toString()),
                                getValueOr(subData, "irop", defValC).toString(),
                                formatNumStr(5, getValueOr(subData, "irval", defValR).toString())));
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
                                idx + 1, //getValueOr(data, "fe", defValI).toString(),
                                formatDateStr(getValueOr(secData, "fdate", defValD).toString()),
                                getValueOr(secData, "fecd", defValC).toString(),
                                getValueOr(secData, "feacd", defValC).toString(),
                                formatNumStr(5, getValueOr(secData, "fedep", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "feamn", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "feamp", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "feamk", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "feamc", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "feamo", defValR).toString()),
                                getValueOr(secData, "feocd", defValC).toString(),
                                getValueOr(secData, "fe_name", defValC).toString()));

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
                                idx + 1, //getValueOr(secData, "om", defValI).toString(),
                                formatDateStr(getValueOr(secData, "omdat", defValD).toString()),
                                getValueOr(secData, "omcd", defValC).toString(),
                                formatNumStr(5, getValueOr(secData, "omamt", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "omn%", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "omp%", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "omk%", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "ominp", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "omdep", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "omacd", defValR).toString()),
                                getValueOr(secData, "om_name", defValC).toString()));
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
                                idx + 1, //getValueOr(secData, "ch", defValI).toString(),
                                formatDateStr(getValueOr(secData, "cdate", defValD).toString()),
                                getValueOr(secData, "chcd", defValC).toString(),
                                formatNumStr(5, getValueOr(secData, "chamt", defValR).toString()),
                                getValueOr(secData, "chacd", defValC).toString(),
                                getValueOr(secData, "chdep", defValC).toString(),
                                getValueOr(secData, "ch_targets", defValC).toString(),
                                getValueOr(secData, "ch_name", defValC).toString()));
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
                                idx + 1, //getValueOr(secData, "ti", defValI).toString(),
                                formatDateStr(getValueOr(secData, "tdate", defValD).toString()),
                                getValueOr(secData, "tiimp", defValC).toString(),
                                formatNumStr(5, getValueOr(secData, "tidep", defValR).toString()),
                                getValueOr(secData, "ti_name", defValC).toString()));

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
                                idx + 1, //getValueOr(secData, "em", defValI).toString(),
                                formatDateStr(getValueOr(secData, "emday", defValD).toString()),
                                getValueOr(secData, "ecdyl", defValC).toString(),
                                formatNumStr(4, getValueOr(secData, "emdyl", defValR).toString()),
                                getValueOr(secData, "ecrad", defValC).toString(),
                                formatNumStr(4, getValueOr(secData, "emrad", defValR).toString()),
                                getValueOr(secData, "ecmax", defValC).toString(),
                                formatNumStr(4, getValueOr(secData, "emmax", defValR).toString()),
                                getValueOr(secData, "ecmin", defValC).toString(),
                                formatNumStr(4, getValueOr(secData, "emmin", defValR).toString()),
                                getValueOr(secData, "ecrai", defValC).toString(),
                                formatNumStr(4, getValueOr(secData, "emrai", defValR).toString()),
                                getValueOr(secData, "ecco2", defValC).toString(),
                                formatNumStr(4, getValueOr(secData, "emco2", defValR).toString()),
                                getValueOr(secData, "ecdew", defValC).toString(),
                                formatNumStr(4, getValueOr(secData, "emdew", defValR).toString()),
                                getValueOr(secData, "ecwnd", defValC).toString(),
                                formatNumStr(4, getValueOr(secData, "emwnd", defValR).toString()),
                                getValueOr(secData, "em_name", defValC).toString()));

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
                                idx + 1, //getValueOr(secData, "ha", defValI).toString(),
                                formatDateStr(getValueOr(secData, "hdate", defValD).toString()),
                                getValueOr(secData, "hastg", defValC).toString(),
                                getValueOr(secData, "hacom", defValC).toString(),
                                getValueOr(secData, "hasiz", defValC).toString(),
                                formatNumStr(5, getValueOr(secData, "hapc", defValR).toString()),
                                formatNumStr(5, getValueOr(secData, "habpc", defValR).toString()),
                                getValueOr(secData, "ha_name", defValC).toString()));

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
                    subDataArr = (ArrayList) getValueOr(secData, "data", new ArrayList());

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
        JSONAdapter adapter = new JSONAdapter();
        String nitro;
        String water;
        String sdate;
        String sm = String.format("%2d", smid);
        ArrayList dataArr;
        LinkedHashMap data;
        LinkedHashMap subData;

        dataArr = (ArrayList) getValueOr(trData, "fertilizer", new ArrayList());
        if (!dataArr.isEmpty()) {
            subData = (LinkedHashMap) dataArr.get(0);
        } else {
            subData = new LinkedHashMap();
        }
        if (!getValueOr(subData, "fdate", "").toString().equals("")) {
            nitro = "Y";
        } else if (getValueOr(expData, "fertilizer", "").toString().equals("N")) {
            nitro = "Y";
        } else {
            nitro = "N";
        }

        data = (LinkedHashMap) getValueOr(trData, "irrigation", new LinkedHashMap());
        dataArr = (ArrayList) getValueOr(data, "data", new ArrayList());
        if (!dataArr.isEmpty()) {
            subData = (LinkedHashMap) dataArr.get(0);
        } else {
            subData = new LinkedHashMap();
        }
        if (!getValueOr(subData, "ireff", "").toString().equals("")) {
            water = "Y";
        } else if (getValueOr(subData, "irrig", "").toString().equals("N")) {
            water = "Y";
        } else {
            water = "N";
        }

        sdate = getValueOr(expData, "sdat", "").toString();
        if (sdate.equals("")) {
            data = (LinkedHashMap) getValueOr(trData, "plant", new LinkedHashMap());
            sdate = getValueOr(data, "pdate", defValD).toString();
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

        ArrayList trDataArr = (ArrayList) getValueOr(result, "treatment", new ArrayList());
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
        } else if (!getValueOr(result, "sdat", "").toString().equals("")) {
            defValD = getValueOr(result, "sdat", "").toString();
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
