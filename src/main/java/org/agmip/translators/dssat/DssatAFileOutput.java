package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.agmip.core.types.AdvancedHashMap;
import org.agmip.util.JSONAdapter;

/**
 * DSSAT Observation Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatAFileOutput extends DssatCommonOutput {

    private File outputFile;

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * DSSAT Observation Data Output method
     *
     * @param arg0  file output path
     * @param result data holder object
     */
    @Override
    public void writeFile(String arg0, AdvancedHashMap result) {

        // Initial variables
        JSONAdapter adapter = new JSONAdapter();    // JSON Adapter
        AdvancedHashMap<String, Object> record;     // Data holder for daily data
        BufferedWriter bwA;                         // output object
        StringBuilder sbData = new StringBuilder();         // construct the data info in the output
        HashMap altTitleList = new HashMap();               // Define alternative fields for the necessary observation data fields; key is necessary field
        altTitleList.put("hwam", "hwah");
        altTitleList.put("mdat", "mdap");
        altTitleList.put("adat", "adap");
        altTitleList.put("cwam", "");
        ArrayList optTitleList = getAllTitleList();         // Define optional observation data fields
        LinkedHashMap titleOutput = new LinkedHashMap();    // contain output data field id
        int trno;

        try {

            // Set default value for missing data
            setDefVal();

            // Get Data from input holder
            AdvancedHashMap obvFile = adapter.exportRecord((Map) result.getOr("observed", result));
            AdvancedHashMap obvAFile = adapter.exportRecord((Map) obvFile.getOr("summary", obvFile));
            ArrayList observeRecords = ((ArrayList) obvAFile.getOr("data", new ArrayList()));

            // Initial BufferedWriter
            String exName = getExName(obvAFile);
            String fileName = exName;
            if (fileName.equals("")) {
                fileName = "a.tmp";
            } else {
                fileName = fileName.substring(0, fileName.length() - 2) + "." + fileName.substring(fileName.length() - 2) + "A";
            }
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwA = new BufferedWriter(new FileWriter(outputFile));

            // Output Observation File
            // Titel Section
            sbData.append(String.format("*EXP.DATA (A): %1$-10s %2$s\r\n\r\n", exName, obvAFile.getOr("local_name", defValC).toString()));

            // Get first record of observed data
            AdvancedHashMap fstObvData;
            if (observeRecords.isEmpty()) {
                fstObvData = new AdvancedHashMap();
            } else {
                fstObvData = adapter.exportRecord((Map) observeRecords.get(0));
            }

            // Check if which field is available
            for (Object key : fstObvData.keySet()) {
                // check which optional data is exist, if not, remove from map
                if (optTitleList.contains(key)) {
                    titleOutput.put(key, key);
                }
            }

            // Check if all necessary field is available
            for (Object title : altTitleList.keySet()) {

                // check which optional data is exist, if not, remove from map
                if (!fstObvData.containsKey(title)) {

                    if (fstObvData.containsKey(altTitleList.get(title))) {
                        titleOutput.put(title, altTitleList.get(title));
                    } else {
                        // TODO throw new Exception("Incompleted record because missing data : [" + title + "]");
                        sbError.append("! Waring: Incompleted record because missing data : [").append(title).append("]\r\n");
                    }

                } else {
                }
            }

            // decompress observed data
            decompressData(observeRecords);
            // Observation Data Section
            Object[] titleOutputId = titleOutput.keySet().toArray();
            for (int i = 0; i < (titleOutputId.length / 40 + titleOutputId.length % 40 == 0 ? 0 : 1); i++) {

                sbData.append("@TRNO ");
                int limit = Math.min(titleOutputId.length, (i + 1) * 40);
                for (int j = i * 40; j < limit; j++) {
                    sbData.append(String.format("%1$6s", titleOutput.get(titleOutputId[j]).toString().toUpperCase()));
                }
                sbData.append("\r\n");
                trno = 1;

                for (int j = 0; j < observeRecords.size(); j++) {

                    record = adapter.exportRecord((Map) observeRecords.get(j));
                    sbData.append(String.format(" %1$5d", trno));
                    trno++;
                    for (int k = i * 40; k < limit; k++) {
                        if ((titleOutputId[k].toString().equals("adat") && titleOutput.get(titleOutputId[k]).toString().trim().equals("adap"))
                                || (titleOutputId[k].toString().equals("mdat") && titleOutput.get(titleOutputId[k]).toString().trim().equals("mdap"))) {
                            String pdate = (String) ((AdvancedHashMap) result.getOr("experiment", new AdvancedHashMap())).getOr("pdate", defValD); // TODO need be updated after ear;y version
                            sbData.append(String.format("%1$6s", formatDateStr(pdate, record.getOr(titleOutput.get(titleOutputId[k]).toString(), defValI).toString())));
                        } else {
                            sbData.append(" ").append(formatNumStr(5, record.getOr(titleOutput.get(titleOutputId[k]).toString(), defValI).toString()));
                        }
                    }
                    sbData.append("\r\n");
                }
            }

            // Output finish
            bwA.write(sbError.toString());
            bwA.write(sbData.toString());
            bwA.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Set default value for missing data
     *
     * @version 1.0
     */
    private void setDefVal() {

        // defValD = ""; No need to set default value for Date type in Observation file
        defValR = "-99";
        defValC = "";
        defValI = "-99";
    }

    /**
     * Create the list of titles for all observed data
     *
     */
    private ArrayList getAllTitleList() {

        ArrayList ret = new ArrayList();
        ret.add("etcm");
        ret.add("prcm");
        ret.add("hdate");
        ret.add("adap");
        ret.add("cwam");
        ret.add("cwams");
        ret.add("hmah");
        ret.add("hwah");
        ret.add("hwahs");
        ret.add("hyah");
        ret.add("hyahs");
        ret.add("mdap");
        ret.add("r8aps");
        ret.add("adaps");
        ret.add("chtds");
        ret.add("hmahs");
        ret.add("adat");
        ret.add("adoy");
        ret.add("ap1d");
        ret.add("br1d");
        ret.add("br2d");
        ret.add("br3d");
        ret.add("br4d");
        ret.add("bwah");
        ret.add("bwam");
        ret.add("cdwa");
        ret.add("cfah");
        ret.add("chta");
        ret.add("chwa");
        ret.add("cnaa");
        ret.add("cnam");
        ret.add("cpaa");
        ret.add("cpam");
        ret.add("cwaa");
        ret.add("drcm");
        ret.add("drid");
        ret.add("dwap");
        ret.add("e#am");
        ret.add("e#um");
        ret.add("eemd");
        ret.add("eoaa");
        ret.add("epac");
        ret.add("epcm");
        ret.add("escm");
        ret.add("ewum");
        ret.add("fdat");
        ret.add("fwah");
        ret.add("gl%m");
        ret.add("gn%m");
        ret.add("gnam");
        ret.add("gp%m");
        ret.add("gpam");
        ret.add("gw%m");
        ret.add("gwam");
        ret.add("gwgm");
        ret.add("gwpm");
        ret.add("gyam");
        ret.add("gypm");
        ret.add("gyvm");
        ret.add("h#am");
        ret.add("h#gm");
        ret.add("h#um");
        ret.add("hastg");
        ret.add("hdap");
        ret.add("hiam");
        ret.add("hipm");
        ret.add("hprh");
        ret.add("hwac");
        ret.add("hwam");
        ret.add("hwum");
        ret.add("hyam");
        ret.add("icsw");
        ret.add("idap");
        ret.add("idat");
        ret.add("ir#m");
        ret.add("ircm");
        ret.add("l#sm");
        ret.add("l#sx");
        ret.add("laix");
        ret.add("lf3d");
        ret.add("lf5d");
        ret.add("liwam");
        ret.add("llfd");
        ret.add("lwam");
        ret.add("mdat");
        ret.add("mdat2");
        ret.add("mdoy");
        ret.add("niam");
        ret.add("nlcm");
        ret.add("nucm");
        ret.add("ocam");
        ret.add("oid");
        ret.add("oid");
        ret.add("oid");
        ret.add("onam");
        ret.add("p#am");
        ret.add("pd1p");
        ret.add("pd1t");
        ret.add("pdfp");
        ret.add("pdft");
        ret.add("pwam");
        ret.add("r1at");
        ret.add("r2at");
        ret.add("r3at");
        ret.add("r4at");
        ret.add("r5at");
        ret.add("r6at");
        ret.add("r7at");
        ret.add("r8ap");
        ret.add("r8at");
        ret.add("r9at");
        ret.add("rnah");
        ret.add("rocm");
        ret.add("rpah");
        ret.add("snam");
        ret.add("spam");
        ret.add("sqdat");
        ret.add("sraa");
        ret.add("swxm");
        ret.add("t#am");
        ret.add("tdap");
        ret.add("tdat");
        ret.add("tham");
        ret.add("tl#c");
        ret.add("tnah");
        ret.add("tnim");
        ret.add("tspd");
        ret.add("twah");
        ret.add("twam");
        ret.add("un%h");
        ret.add("unam");
        ret.add("upam");
        ret.add("uwah");
        ret.add("uyah");
        ret.add("vwam");
        ret.add("z21d");
        ret.add("z30d");
        ret.add("z31d");
        ret.add("z37d");
        ret.add("z39d");

        return ret;
    }
}
