package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import org.agmip.core.types.AdvancedHashMap;
import org.agmip.util.JSONAdapter;

/**
 * DSSAT Experiment Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatXFileOutput extends DssatCommonOutput {

    private File outputFile;
    // Define necessary id for the experiment data
    private static String[] necessaryData = {"pdate", "plpop,plpoe", "plrs", "crid", "cul_id", "wst_insi", "soil_id"};

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
    public void writeFile(String arg0, AdvancedHashMap result) {

        // Initial variables
        JSONAdapter adapter = new JSONAdapter();    // JSON Adapter
        AdvancedHashMap<String, Object> data;       // Data holder for section data
        BufferedWriter bwX;                          // output object
        StringBuilder sbData = new StringBuilder();     // construct the data info in the output
        StringBuilder eventPart2 = new StringBuilder();                   // output string for second part of event data
        AdvancedHashMap secData;
        ArrayList subDataArr;                       // Arraylist for event data holder
        AdvancedHashMap subData;
        ArrayList secDataArr;                       // Arraylist for section data holder
        AdvancedHashMap trData = new AdvancedHashMap();
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
//        File file;
//        FileWriter output;

        try {

            // Initial missing data check for necessary fields
            // TODO will be revised
            for (int i = 0; i < necessaryData.length; i++) {
                String[] strs = necessaryData[i].split(",");
                for (int j = 0; j < strs.length; j++) {
                    if (!result.getOr(strs[j], "").toString().equals("")) {
                        strs = null;
                        break;
                    }
                }
                if (strs != null) {
                    //throw new Exception("Incompleted record because missing data : [" + necessaryData[i] + "]");
                    //System.out.println("Incompleted record because missing data : [" + necessaryData[i] + "]");
                    sbError.append("! Warning: Incompleted record because missing data : [").append(necessaryData[i]).append("]\r\n");
                    //return;
                }
            }

            // Set default value for missing data
            data = adapter.exportRecord((Map) result.getOr("experiment", result));
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
            sbData.append(String.format("*EXP.DETAILS: %1$-10s %2$s\r\n\r\n", exName, data.getOr("local_name", defValC).toString()));

            // GENERAL Section
            sbData.append("*GENERAL\r\n");
            // People
            sbData.append(String.format("@PEOPLE\r\n %1$s\r\n", data.getOr("people", defValC).toString()));
            // Address
            if (data.getOr("institutes", defValC).toString().equals("")) {
                sbData.append(String.format("@ADDRESS\r\n %3$s, %2$s, %1$s\r\n",
                        data.getOr("fl_loc_1", defValC).toString(),
                        data.getOr("fl_loc_2", defValC).toString(),
                        data.getOr("fl_loc_3", defValC).toString()));
            } else {
                sbData.append(String.format("@ADDRESS\r\n %1$s\r\n", data.getOr("institutes", defValC).toString()));
            }

            // Site
            sbData.append(String.format("@SITE\r\n %1$s\r\n", data.getOr("site", defValC).toString()));
            // Plot Info
            AdvancedHashMap plotData = (AdvancedHashMap) data.getOr("plot_info", new AdvancedHashMap());
            if (!plotData.isEmpty()) {

                sbData.append("@ PAREA  PRNO  PLEN  PLDR  PLSP  PLAY HAREA  HRNO  HLEN  HARM.........\r\n");
                sbData.append(String.format(" %1$6s %2$5s %3$5s %4$5s %5$5s %6$-5s %7$5s %8$5s %9$5s %10$-15s\r\n",
                        formatNumStr(6, plotData.getOr("parea", defValR).toString()),
                        formatNumStr(5, plotData.getOr("prno", defValI).toString()),
                        formatNumStr(5, plotData.getOr("plen", defValR).toString()),
                        formatNumStr(5, plotData.getOr("pldr", defValI).toString()),
                        formatNumStr(5, plotData.getOr("plsp", defValI).toString()),
                        plotData.getOr("play", defValC).toString(),
                        formatNumStr(5, plotData.getOr("pltha", defValR).toString()),
                        formatNumStr(5, plotData.getOr("hrno", defValI).toString()),
                        formatNumStr(5, plotData.getOr("hlen", defValR).toString()),
                        plotData.getOr("plthm", defValC).toString()));
            }
            // Notes
            if (data.containsKey("notes")) {
                sbData.append("@NOTES\r\n");
                String notes = data.getOr("notes", defValC).toString();

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
            trArr = (ArrayList) data.getOr("treatment", new ArrayList());
            if (trArr.isEmpty()) {
                sbError.append("! Warning: There is no treatment data in the experiment!/r/n");
            } else {
                sbData.append("*TREATMENTS                        -------------FACTOR LEVELS------------\r\n");
                sbData.append("@N R O C TNAME.................... CU FL SA IC MP MI MF MR MC MT ME MH SM\r\n");
            }

            for (int i = 0; i < trArr.size(); i++) {
                trData = adapter.exportRecord((Map) trArr.get(i));

                cuNum = setSecDataArr(adapter.exportRecord((Map) trData.getOr("cultivar", new AdvancedHashMap())), cuArr);
                flNum = setSecDataArr(adapter.exportRecord((Map) trData.getOr("field", new AdvancedHashMap())), flArr);
                saNum = setSecDataArr(adapter.exportRecord((Map) trData.getOr("soil_analysis", new AdvancedHashMap())), saArr);
                icNum = setSecDataArr(adapter.exportRecord((Map) trData.getOr("initial_condition", new AdvancedHashMap())), icArr);
                mpNum = setSecDataArr(adapter.exportRecord((Map) trData.getOr("plant", new AdvancedHashMap())), mpArr);
                miNum = setSecDataArr(adapter.exportRecord((Map) trData.getOr("irrigation", new AdvancedHashMap())), miArr);
                mfNum = setSecDataArr((ArrayList) trData.getOr("fertilizer", new ArrayList()), mfArr);
                mrNum = setSecDataArr((ArrayList) trData.getOr("residue_organic", new ArrayList()), mrArr);
                mcNum = setSecDataArr((ArrayList) trData.getOr("chemical", new ArrayList()), mcArr);
                mtNum = setSecDataArr((ArrayList) trData.getOr("tillage", new ArrayList()), mtArr);
                meNum = setSecDataArr((ArrayList) trData.getOr("emvironment", new ArrayList()), meArr);
                mhNum = setSecDataArr((ArrayList) trData.getOr("harvest", new ArrayList()), mhArr);
                smNum = setSecDataArr(adapter.exportRecord((Map) trData.getOr("simulation", new AdvancedHashMap())), smArr);
                if (smNum == 0) {
                    smNum = 1;
                }

                sbData.append(String.format("%1$2s %2$1s %3$1s %4$1s %5$-25s %6$2s %7$2s %8$2s %9$2s %10$2s %11$2s %12$2s %13$2s %14$2s %15$2s %16$2s %17$2s %18$2s\r\n",
                        i + 1,
                        trData.getOr("sq", "1").toString(), // P.S. default value here is based on document DSSAT vol2.pdf
                        trData.getOr("op", "1").toString(),
                        trData.getOr("co", "0").toString(),
                        trData.getOr("tr_name", defValC).toString(),
                        cuNum, //data.getOr("ge", defValI).toString(), 
                        flNum, //data.getOr("fl", defValI).toString(), 
                        saNum, //data.getOr("sa", defValI).toString(),
                        icNum, //data.getOr("ic", defValI).toString(),
                        mpNum, //data.getOr("pl", defValI).toString(),
                        miNum, //data.getOr("ir", defValI).toString(),
                        mfNum, //data.getOr("fe", defValI).toString(),
                        mrNum, //data.getOr("om", defValI).toString(),
                        mcNum, //data.getOr("ch", defValI).toString(),
                        mtNum, //data.getOr("ti", defValI).toString(),
                        meNum, //data.getOr("em", defValI).toString(),
                        mhNum, //data.getOr("ha", defValI).toString(),
                        smNum)); // 1

            }
            sbData.append("\r\n");

            // CULTIVARS Section
            if (!cuArr.isEmpty()) {
                sbData.append("*CULTIVARS\r\n");
                sbData.append("@C CR INGENO CNAME\r\n");

                for (int idx = 0; idx < cuArr.size(); idx++) {
                    secData = adapter.exportRecord((Map) cuArr.get(idx));
                    sbData.append(String.format("%1$2s %2$-2s %3$-6s %4$s\r\n",
                            idx + 1, //secData.getOr("ge", defValI).toString(),
                            secData.getOr("crid", defValC).toString(),
                            secData.getOr("cul_id", defValC).toString(),
                            secData.getOr("cul_name", defValC).toString()));
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
                secData = adapter.exportRecord((Map) flArr.get(idx));
                sbData.append(String.format("%1$2s %2$-8s %3$-8s %4$5s %5$5s %6$-5s %7$5s %8$5s %9$-5s %10$-5s%11$5s  %12$-10s %13$s\r\n", // P.S. change length definition to match current way
                        idx + 1, //secData.getOr("fl", defValI).toString(),
                        secData.getOr("id_field", defValC).toString(),
                        secData.getOr("wsta_id", defValC).toString(),
                        secData.getOr("flsl", defValC).toString(),
                        formatNumStr(5, secData.getOr("flob", defValR).toString()),
                        secData.getOr("fl_drntype", defValC).toString(),
                        formatNumStr(5, secData.getOr("fldrd", defValR).toString()),
                        formatNumStr(5, secData.getOr("fldrs", defValR).toString()),
                        secData.getOr("flst", defValC).toString(),
                        secData.getOr("sltx", defValC).toString(),
                        formatNumStr(5, secData.getOr("sldp", defValR).toString()),
                        secData.getOr("soil_id", defValC).toString(),
                        secData.getOr("fl_name", defValC).toString()));

                eventPart2.append(String.format("%1$2s %2$15s %3$15s %4$9s %5$17s %6$5s %7$5s %8$5s %9$-5s %10$5s\r\n",
                        idx + 1, //secData.getOr("fl", defValI).toString(),
                        formatNumStr(15, secData.getOr("fl_lat", defValR).toString()),
                        formatNumStr(15, secData.getOr("fl_long", defValR).toString()),
                        formatNumStr(9, secData.getOr("flele", defValR).toString()),
                        formatNumStr(17, secData.getOr("farea", defValR).toString()),
                        formatNumStr(5, data.getOr("slen", defValR).toString()), // P.S. keep -99
                        formatNumStr(5, secData.getOr("fllwr", defValR).toString()),
                        formatNumStr(5, secData.getOr("flsla", defValR).toString()),
                        secData.getOr("flhst", defValC).toString(),
                        formatNumStr(5, secData.getOr("fhdur", defValR).toString())));
            }
            if (!flArr.isEmpty()) {
                sbData.append(eventPart2.toString()).append("\r\n");
            }

            // SOIL ANALYSIS Section
            if (!saArr.isEmpty()) {
                sbData.append("*SOIL ANALYSIS\r\n");

                for (int idx = 0; idx < saArr.size(); idx++) {

                    secData = adapter.exportRecord((Map) saArr.get(idx));
                    sbData.append("@A SADAT  SMHB  SMPX  SMKE  SANAME\r\n");
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s  %6$s\r\n",
                            idx + 1, //secData.getOr("sa", defValI).toString(),
                            formatDateStr(secData.getOr("sadat", defValD).toString()),
                            secData.getOr("samhb", defValC).toString(),
                            secData.getOr("sampx", defValC).toString(),
                            secData.getOr("samke", defValC).toString(),
                            secData.getOr("sa_name", defValC).toString()));

                    subDataArr = (ArrayList) secData.getOr("data", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        sbData.append("@A  SABL  SADM  SAOC  SANI SAPHW SAPHB  SAPX  SAKE  SASC\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = adapter.exportRecord((Map) subDataArr.get(j));
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s\r\n",
                                idx + 1, //subData.getOr("sa", defValI).toString(),
                                formatNumStr(5, subData.getOr("sabl", defValR).toString()),
                                formatNumStr(5, subData.getOr("sabdm", defValR).toString()),
                                formatNumStr(5, subData.getOr("saoc", defValR).toString()),
                                formatNumStr(5, subData.getOr("sani", defValR).toString()),
                                formatNumStr(5, subData.getOr("saphw", defValR).toString()),
                                formatNumStr(5, subData.getOr("saphb", defValR).toString()),
                                formatNumStr(5, subData.getOr("sapx", defValR).toString()),
                                formatNumStr(5, subData.getOr("sake", defValR).toString()),
                                formatNumStr(5, subData.getOr("sasc", defValR).toString())));
                    }


                }
                sbData.append("\r\n");
            }

            // INITIAL CONDITIONS Section
            if (!icArr.isEmpty()) {
                sbData.append("*INITIAL CONDITIONS\r\n");

                for (int idx = 0; idx < icArr.size(); idx++) {

                    secData = adapter.exportRecord((Map) icArr.get(idx));
                    sbData.append("@C   PCR ICDAT  ICRT  ICND  ICRN  ICRE  ICWD ICRES ICREN ICREP ICRIP ICRID ICNAME\r\n");
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$s\r\n",
                            idx + 1, //secData.getOr("ic", defValI).toString(),
                            secData.getOr("icpcr", defValC).toString(),
                            formatDateStr(secData.getOr("icdat", defValD).toString()),
                            formatNumStr(5, secData.getOr("icrt", defValR).toString()),
                            formatNumStr(5, secData.getOr("icnd", defValR).toString()),
                            formatNumStr(5, secData.getOr("icrz#", defValR).toString()),
                            formatNumStr(5, secData.getOr("icrze", defValR).toString()),
                            formatNumStr(5, secData.getOr("icwt", defValR).toString()),
                            formatNumStr(5, secData.getOr("icrag", defValR).toString()),
                            formatNumStr(5, secData.getOr("icrn", defValR).toString()),
                            formatNumStr(5, secData.getOr("icrp", defValR).toString()),
                            formatNumStr(5, secData.getOr("icrip", defValR).toString()),
                            formatNumStr(5, secData.getOr("icrdp", defValR).toString()),
                            secData.getOr("ic_name", defValC).toString()));

                    subDataArr = (ArrayList) secData.getOr("data", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        sbData.append("@C  ICBL  SH2O  SNH4  SNO3\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = adapter.exportRecord((Map) subDataArr.get(j));
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s\r\n",
                                idx + 1, //subData.getOr("ic", defValI).toString(),
                                formatNumStr(5, subData.getOr("icbl", defValR).toString()),
                                formatNumStr(5, subData.getOr("ich2o", defValR).toString()),
                                formatNumStr(5, subData.getOr("icnh4", defValR).toString()),
                                formatNumStr(5, subData.getOr("icno3", defValR).toString())));
                    }
                }
                sbData.append("\r\n");
            }

            // PLANTING DETAILS Section
            if (!mpArr.isEmpty()) {
                sbData.append("*PLANTING DETAILS\r\n");
                sbData.append("@P PDATE EDATE  PPOP  PPOE  PLME  PLDS  PLRS  PLRD  PLDP  PLWT  PAGE  PENV  PLPH  SPRL                        PLNAME\r\n");

                for (int idx = 0; idx < mpArr.size(); idx++) {

                    secData = adapter.exportRecord((Map) mpArr.get(idx));
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s     %6$-1s     %7$-1s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s %15$5s                        %16$s\r\n",
                            idx + 1, //data.getOr("pl", defValI).toString(),
                            formatDateStr(secData.getOr("pdate", defValD).toString()),
                            formatDateStr(secData.getOr("pldae", defValD).toString()),
                            formatNumStr(5, secData.getOr("plpop", secData.getOr("plpoe", defValR)).toString()),
                            formatNumStr(5, secData.getOr("plpoe", secData.getOr("plpop", defValR)).toString()),
                            secData.getOr("plme", defValC).toString(),
                            secData.getOr("plds", defValC).toString(),
                            formatNumStr(5, secData.getOr("plrs", defValR).toString()),
                            formatNumStr(5, secData.getOr("plrd", defValR).toString()),
                            formatNumStr(5, secData.getOr("pldp", defValR).toString()),
                            formatNumStr(5, secData.getOr("plmwt", defValR).toString()),
                            formatNumStr(5, secData.getOr("page", defValR).toString()),
                            formatNumStr(5, secData.getOr("penv", defValR).toString()),
                            formatNumStr(5, secData.getOr("plph", defValR).toString()),
                            formatNumStr(5, secData.getOr("plspl", defValR).toString()),
                            secData.getOr("pl_name", defValC).toString()));

                }
                sbData.append("\r\n");
            } else {
                sbError.append("! Warning: Trhee is no plainting data in the experiment.\r\n");
            }

            // IRRIGATION AND WATER MANAGEMENT Section
            if (!miArr.isEmpty()) {
                sbData.append("*IRRIGATION AND WATER MANAGEMENT\r\n");

                for (int idx = 0; idx < miArr.size(); idx++) {

                    secData = adapter.exportRecord((Map) miArr.get(idx));
                    sbData.append("@I  EFIR  IDEP  ITHR  IEPT  IOFF  IAME  IAMT IRNAME\r\n");
                    sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$s\r\n",
                            idx + 1, //data.getOr("ir", defValI).toString(),
                            formatNumStr(5, secData.getOr("ireff", defValR).toString()),
                            formatNumStr(5, secData.getOr("irmdp", defValR).toString()),
                            formatNumStr(5, secData.getOr("irthr", defValR).toString()),
                            formatNumStr(5, secData.getOr("irept", defValR).toString()),
                            secData.getOr("irstg", defValC).toString(),
                            secData.getOr("iame", defValC).toString(),
                            formatNumStr(5, secData.getOr("iamt", defValR).toString()),
                            secData.getOr("ir_name", defValC).toString()));

                    subDataArr = (ArrayList) secData.getOr("data", new ArrayList());
                    if (!subDataArr.isEmpty()) {
                        sbData.append("@I IDATE  IROP IRVAL\r\n");
                    }
                    for (int j = 0; j < subDataArr.size(); j++) {
                        subData = adapter.exportRecord((Map) subDataArr.get(j));
                        sbData.append(String.format("%1$2s %2$5s %3$-5s %4$5s\r\n",
                                idx + 1, //subData.getOr("ir", defValI).toString(),
                                formatDateStr(subData.getOr("idate", defValD).toString()),
                                subData.getOr("irop", defValC).toString(),
                                formatNumStr(5, subData.getOr("irval", defValR).toString())));
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
                        secData = adapter.exportRecord((Map) secDataArr.get(i));
                        sbData.append(String.format("%1$2s %2$5s %3$-5s %4$-5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$s\r\n",
                                idx + 1, //data.getOr("fe", defValI).toString(),
                                formatDateStr(secData.getOr("fdate", defValD).toString()),
                                secData.getOr("fecd", defValC).toString(),
                                secData.getOr("feacd", defValC).toString(),
                                formatNumStr(5, secData.getOr("fedep", defValR).toString()),
                                formatNumStr(5, secData.getOr("feamn", defValR).toString()),
                                formatNumStr(5, secData.getOr("feamp", defValR).toString()),
                                formatNumStr(5, secData.getOr("feamk", defValR).toString()),
                                formatNumStr(5, secData.getOr("feamc", defValR).toString()),
                                formatNumStr(5, secData.getOr("feamo", defValR).toString()),
                                secData.getOr("feocd", defValC).toString(),
                                secData.getOr("fe_name", defValC).toString()));

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
                        secData = adapter.exportRecord((Map) secDataArr.get(i));
                        sbData.append(String.format("%1$2s %2$5s %3$-5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$s\r\n",
                                idx + 1, //secData.getOr("om", defValI).toString(),
                                formatDateStr(secData.getOr("omdat", defValD).toString()),
                                secData.getOr("omcd", defValC).toString(),
                                formatNumStr(5, secData.getOr("omamt", defValR).toString()),
                                formatNumStr(5, secData.getOr("omn%", defValR).toString()),
                                formatNumStr(5, secData.getOr("omp%", defValR).toString()),
                                formatNumStr(5, secData.getOr("omk%", defValR).toString()),
                                formatNumStr(5, secData.getOr("ominp", defValR).toString()),
                                formatNumStr(5, secData.getOr("omdep", defValR).toString()),
                                formatNumStr(5, secData.getOr("omacd", defValR).toString()),
                                secData.getOr("om_name", defValC).toString()));
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
                        secData = adapter.exportRecord((Map) secDataArr.get(i));
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s  %8$s\r\n",
                                idx + 1, //secData.getOr("ch", defValI).toString(),
                                formatDateStr(secData.getOr("cdate", defValD).toString()),
                                secData.getOr("chcd", defValC).toString(),
                                formatNumStr(5, secData.getOr("chamt", defValR).toString()),
                                secData.getOr("chacd", defValC).toString(),
                                secData.getOr("chdep", defValC).toString(),
                                secData.getOr("ch_targets", defValC).toString(),
                                secData.getOr("ch_name", defValC).toString()));
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
                        secData = adapter.exportRecord((Map) secDataArr.get(i));
                        sbData.append(String.format("%1$2s %2$5s %3$5s %4$5s %5$s\r\n",
                                idx + 1, //secData.getOr("ti", defValI).toString(),
                                formatDateStr(secData.getOr("tdate", defValD).toString()),
                                secData.getOr("tiimp", defValC).toString(),
                                formatNumStr(5, secData.getOr("tidep", defValR).toString()),
                                secData.getOr("ti_name", defValC).toString()));

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
                        secData = adapter.exportRecord((Map) secDataArr.get(i));
                        sbData.append(String.format("%1$2s %2$5s %3$-1s%4$4s %5$-1s%6$4s %7$-1s%8$4s %9$-1s%10$4s %11$-1s%12$4s %13$-1s%14$4s %15$-1s%16$4s %17$-1s%18$4s %19$s\r\n",
                                idx + 1, //secData.getOr("em", defValI).toString(),
                                formatDateStr(secData.getOr("emday", defValD).toString()),
                                secData.getOr("ecdyl", defValC).toString(),
                                formatNumStr(4, secData.getOr("emdyl", defValR).toString()),
                                secData.getOr("ecrad", defValC).toString(),
                                formatNumStr(4, secData.getOr("emrad", defValR).toString()),
                                secData.getOr("ecmax", defValC).toString(),
                                formatNumStr(4, secData.getOr("emmax", defValR).toString()),
                                secData.getOr("ecmin", defValC).toString(),
                                formatNumStr(4, secData.getOr("emmin", defValR).toString()),
                                secData.getOr("ecrai", defValC).toString(),
                                formatNumStr(4, secData.getOr("emrai", defValR).toString()),
                                secData.getOr("ecco2", defValC).toString(),
                                formatNumStr(4, secData.getOr("emco2", defValR).toString()),
                                secData.getOr("ecdew", defValC).toString(),
                                formatNumStr(4, secData.getOr("emdew", defValR).toString()),
                                secData.getOr("ecwnd", defValC).toString(),
                                formatNumStr(4, secData.getOr("emwnd", defValR).toString()),
                                secData.getOr("em_name", defValC).toString()));

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
                        secData = adapter.exportRecord((Map) secDataArr.get(i));
                        sbData.append(String.format("%1$2s %2$5s %3$-5s %4$-5s %5$-5s %6$5s %7$5s %8$s\r\n",
                                idx + 1, //secData.getOr("ha", defValI).toString(),
                                formatDateStr(secData.getOr("hdate", defValD).toString()),
                                secData.getOr("hastg", defValC).toString(),
                                secData.getOr("hacom", defValC).toString(),
                                secData.getOr("hasiz", defValC).toString(),
                                formatNumStr(5, secData.getOr("hapc", defValR).toString()),
                                formatNumStr(5, secData.getOr("habpc", defValR).toString()),
                                secData.getOr("ha_name", defValC).toString()));

                    }
                }
                sbData.append("\r\n");
            }

            // SIMULATION CONTROLS and AUTOMATIC MANAGEMENT Section
            if (!smArr.isEmpty()) {
                
                for (int idx = 0; idx < smArr.size(); idx++) {
                    secData = adapter.exportRecord((Map) smArr.get(idx));
                    
                    sbData.append("*SIMULATION CONTROLS\r\n");
                    sbData.append("@N GENERAL     NYERS NREPS START SDATE RSEED SNAME....................\r\n");
                    sbData.append(secData.getOr("general", defValC)).append("\r\n");
                    sbData.append("@N OPTIONS     WATER NITRO SYMBI PHOSP POTAS DISES  CHEM  TILL   CO2\r\n");
                    sbData.append(secData.getOr("options", defValC)).append("\r\n");
                    sbData.append("@N METHODS     WTHER INCON LIGHT EVAPO INFIL PHOTO HYDRO NSWIT MESOM MESEV MESOL\r\n");
                    sbData.append(secData.getOr("methods", defValC)).append("\r\n");
                    sbData.append("@N MANAGEMENT  PLANT IRRIG FERTI RESID HARVS\r\n");
                    sbData.append(secData.getOr("management", defValC)).append("\r\n");
                    sbData.append("@N OUTPUTS     FNAME OVVEW SUMRY FROPT GROUT CAOUT WAOUT NIOUT MIOUT DIOUT VBOSE CHOUT OPOUT\r\n");
                    sbData.append(secData.getOr("outputs", defValC)).append("\r\n\r\n");
                    sbData.append("@  AUTOMATIC MANAGEMENT\r\n");
                    sbData.append("@N PLANTING    PFRST PLAST PH2OL PH2OU PH2OD PSTMX PSTMN\r\n");
                    sbData.append(secData.getOr("planting", defValC)).append("\r\n");
                    sbData.append("@N IRRIGATION  IMDEP ITHRL ITHRU IROFF IMETH IRAMT IREFF\r\n");
                    sbData.append(secData.getOr("irrigation", defValC)).append("\r\n");
                    sbData.append("@N NITROGEN    NMDEP NMTHR NAMNT NCODE NAOFF\r\n");
                    sbData.append(secData.getOr("nitrogen", defValC)).append("\r\n");
                    sbData.append("@N RESIDUES    RIPCN RTIME RIDEP\r\n");
                    sbData.append(secData.getOr("residues", defValC)).append("\r\n");
                    sbData.append("@N HARVEST     HFRST HLAST HPCNP HPCNR\r\n");
                    sbData.append(secData.getOr("harvest", defValC)).append("\r\n");
                }
                
            } else {
                sbData.append(createSMMAStr(0, trData));
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
     * @param type  The return type of string
     *                  (0:default; 1: undefined)
     * @param result  date holder for experiment data
     * @return      date string with format of "yyddd" 
     */
    private String createSMMAStr(int type, AdvancedHashMap result) {

        StringBuilder sb = new StringBuilder();
        String nitro;
        String water;
        String sdate;

        if (!result.getOr("fdate", "").toString().equals("")) {
            nitro = "Y";
        } else if (result.getOr("fertilizer", "").toString().equals("N")) {
            nitro = "Y";
        } else {
            nitro = "N";
        }

        if (!result.getOr("ireff", "").toString().equals("")) {
            water = "Y";
        } else if (result.getOr("irrig", "").toString().equals("N")) {
            water = "Y";
        } else {
            water = "N";
        }

        sdate = result.getOr("sdat", "").toString();
        if (sdate.equals("")) {
            sdate = result.getOr("pdate", defValD).toString();
        }
        sdate = formatDateStr(sdate);

        // Check return type
        if (type == 1) {
            // Undefined, will be used for future
        } // default
        else {
            sb.append("*SIMULATION CONTROLS\r\n");
            sb.append("@N GENERAL     NYERS NREPS START SDATE RSEED SNAME....................\r\n");
            sb.append(" 1 GE              1     1     S ").append(sdate).append("  2150 DEFAULT SIMULATION CONTROL\r\n");
            sb.append("@N OPTIONS     WATER NITRO SYMBI PHOSP POTAS DISES  CHEM  TILL   CO2\r\n");
//          sb.append(" 1 OP              Y     Y     Y     N     N     N     N     Y     M\r\n");
            sb.append(" 1 OP              ").append(water).append("     ").append(nitro).append("     Y     N     N     N     N     Y     M\r\n");
            sb.append("@N METHODS     WTHER INCON LIGHT EVAPO INFIL PHOTO HYDRO NSWIT MESOM MESEV MESOL\r\n");
            sb.append(" 1 ME              M     M     E     R     S     L     R     1     G     S     2\r\n");
            sb.append("@N MANAGEMENT  PLANT IRRIG FERTI RESID HARVS\r\n");
            sb.append(" 1 MA              R     R     R     R     M\r\n");
            sb.append("@N OUTPUTS     FNAME OVVEW SUMRY FROPT GROUT CAOUT WAOUT NIOUT MIOUT DIOUT VBOSE CHOUT OPOUT\r\n");
            sb.append(" 1 OU              N     Y     Y     1     Y     Y     Y     Y     Y     N     Y     N     Y\r\n\r\n");
            sb.append("@  AUTOMATIC MANAGEMENT\r\n");
            sb.append("@N PLANTING    PFRST PLAST PH2OL PH2OU PH2OD PSTMX PSTMN\r\n");
            sb.append(" 1 PL          82050 82064    40   100    30    40    10\r\n");
            sb.append("@N IRRIGATION  IMDEP ITHRL ITHRU IROFF IMETH IRAMT IREFF\r\n");
            sb.append(" 1 IR             30    50   100 GS000 IR001    10  1.00\r\n");
            sb.append("@N NITROGEN    NMDEP NMTHR NAMNT NCODE NAOFF\r\n");
            sb.append(" 1 NI             30    50    25 FE001 GS000\r\n");
            sb.append("@N RESIDUES    RIPCN RTIME RIDEP\r\n");
            sb.append(" 1 RE            100     1    20\r\n");
            sb.append("@N HARVEST     HFRST HLAST HPCNP HPCNR\r\n");
            sb.append(" 1 HA              0 83057   100     0\r\n");
        }

        return sb.toString();
    }

    /**
     * Set default value for missing data
     * 
     * @param result  date holder for experiment data
     */
    private void setDefVal(AdvancedHashMap result) {

        // TODO need to be revised
        if (!result.getOr("icdat", "").toString().equals("")) {
            defValD = result.getOr("icdat", "").toString();
        } else if (!result.getOr("sdat", "").toString().equals("")) {
            defValD = result.getOr("sdat", "").toString();
        } else if (!result.getOr("pdate", "").toString().equals("")) {
            defValD = result.getOr("pdate", "").toString();
        } else {
            //throw new Exception("Experiment can't be output due to unavailable date info.");
            defValD = "20110101";
        }
        defValR = "-99";
        defValC = "-99";    // TODO wait for confirmation
        defValI = "-99";
    }

    /**
     * Get index value of the record and set new id value in the array
     * 
     * @param idVal  id string for the record
     * @param arr    array of the id
     * @return       current index value of the id
     */
    private int getIdxVal(String idStr, ArrayList arr) {

        int ret = 0;
        if (!idStr.equals("")) {
            ret = arr.indexOf(idStr);
            if (ret == -1) {
                arr.add(idStr);
                ret = arr.size();
            }
        }

        return ret;
    }

    /**
     * Get index value of the record and set new id value in the array
     * 
     * @param m     sub data
     * @param arr   array of sub data
     * @return       current index value of the sub data
     */
    private int setSecDataArr(AdvancedHashMap m, ArrayList arr) {

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
