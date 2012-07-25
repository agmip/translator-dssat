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
 * DSSAT Soil Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatSoilOutput extends DssatCommonOutput {

    private File outputFile;

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * DSSAT Soil Data Output method
     * 
     * @param arg0   file output path
     * @param result  data holder object
     */
    @Override
    public void writeFile(String arg0, Map result) {

        // Initial variables
//        ArrayList soilSites;                            // Soil site data array
        LinkedHashMap soilSite;                       // Data holder for one site of soil data
        ArrayList soilRecords;                          // Soil layer data array
        LinkedHashMap soilRecord;                     // Data holder for one layer data
        BufferedWriter bwS;                             // output object
        StringBuilder sbData = new StringBuilder();     // construct the data info in the output
        StringBuilder sbLyrP2 = new StringBuilder();    // output string for second part of layer data
        boolean p2Flg;
        String[] p2Ids = {"slpx", "slpt", "slpo", "caco3", "slal", "slfe", "slmn", "slbs", "slpa", "slpb", "slke", "slmg", "slna", "slsu", "slec", "slca"};
        String layerKey = "soilLayer";  // P.S. the key name might change

        try {

            // Set default value for missing data
            setDefVal();

//            soilSites = (ArrayList) getObjectOr(result, "soil", new ArrayList());
            soilSite = (LinkedHashMap) getObjectOr(result, "soil", new LinkedHashMap());
            if (soilSite.isEmpty()) {
                return;
            }
//            decompressData(soilSites);

            // Initial BufferedWriter
            // Get File name
            String exName = getExName(result);
            String fileName;
            if (exName.equals("")) {
                fileName = "soil.SOL";
            } else {
                try {
                    exName = exName.replace("\\.", "");
                    fileName = exName.substring(0, 8) + "_" + exName.substring(8) + "X.SOL";
                } catch (Exception e) {
                    fileName = "soil.SOL";
                }

            }
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwS = new BufferedWriter(new FileWriter(outputFile));

            // Output Soil File
            // Titel Section
            bwS.write("*SOILS: " + getObjectOr((LinkedHashMap) soilSite, "sl_notes", defValBlank) + "\r\n\r\n");

            // Loop sites of data
//            for (int i = 0; i < soilSites.size(); i++) {
//                soilSite = (LinkedHashMap) soilSites.get(i);

            // Site Info Section
            sbData.append(String.format("*%1$-10s  %2$-11s %3$-5s %4$5s %5$s\r\n",
                    getObjectOr(soilSite, "soil_id", defValC).toString(),
                    getObjectOr(soilSite, "sl_source", defValC).toString(),
                    getObjectOr(soilSite, "sltx", defValC).toString(),
                    formatNumStr(5, getObjectOr(soilSite, "sldp", defValR).toString()),
                    getObjectOr(soilSite, "soil_name", defValC).toString()));
            sbData.append("@SITE        COUNTRY          LAT     LONG SCS FAMILY\r\n");
            sbData.append(String.format(" %1$-11s %2$-11s %3$9s%4$8s %5$s\r\n",
                    getObjectOr(soilSite, "sl_loc_3", defValC).toString(),
                    getObjectOr(soilSite, "sl_loc_1", defValC).toString(),
                    formatNumStr(8, getObjectOr(soilSite, "soil_lat", defValR).toString()), // P.S. Definition changed 9 -> 10 (06/24)
                    formatNumStr(8, getObjectOr(soilSite, "soil_long", defValR).toString()), // P.S. Definition changed 9 -> 8  (06/24)
                    getObjectOr(soilSite, "classification", defValC).toString()));
            sbData.append("@ SCOM  SALB  SLU1  SLDR  SLRO  SLNF  SLPF  SMHB  SMPX  SMKE\r\n");
            sbData.append(String.format(" %1$5s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$-5s %9$-5s %10$-5s\r\n",
                    getObjectOr(soilSite, "scom", defValC).toString(),
                    formatNumStr(5, getObjectOr(soilSite, "salb", defValR).toString()),
                    formatNumStr(5, getObjectOr(soilSite, "slu1", defValR).toString()),
                    formatNumStr(5, getObjectOr(soilSite, "sldr", defValR).toString()),
                    formatNumStr(5, getObjectOr(soilSite, "slro", defValR).toString()),
                    formatNumStr(5, getObjectOr(soilSite, "slnf", defValR).toString()),
                    formatNumStr(5, getObjectOr(soilSite, "slpf", defValR).toString()),
                    getObjectOr(soilSite, "smhb", defValC).toString(),
                    getObjectOr(soilSite, "smpx", defValC).toString(),
                    getObjectOr(soilSite, "smke", defValC).toString()));

            // Soil Layer data section
            soilRecords = (ArrayList) getObjectOr(soilSite, layerKey, new ArrayList());

            // part one
            sbData.append("@  SLB  SLMH  SLLL  SDUL  SSAT  SRGF  SSKS  SBDM  SLOC  SLCL  SLSI  SLCF  SLNI  SLHW  SLHB  SCEC  SADC\r\n");
            // part two
            // Get first site record
            LinkedHashMap fstRecord = new LinkedHashMap();
            if (!soilRecords.isEmpty()) {
                fstRecord = (LinkedHashMap) soilRecords.get(0);
            }

            // Check if there is 2nd part of layer data for output
            p2Flg = false;
            for (int j = 0; j < p2Ids.length; j++) {
                if (!getObjectOr(fstRecord, p2Ids[j], "").toString().equals("")) {
                    p2Flg = true;
                    break;
                }
            }
            if (p2Flg) {
                sbLyrP2.append("@  SLB  SLPX  SLPT  SLPO CACO3  SLAL  SLFE  SLMN  SLBS  SLPA  SLPB  SLKE  SLMG  SLNA  SLSU  SLEC  SLCA\r\n");
            }

            // Loop for laryer data
            for (int j = 0; j < soilRecords.size(); j++) {

                soilRecord = (LinkedHashMap) soilRecords.get(j);
                // part one
                sbData.append(String.format(" %1$5s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s %15$5s %16$5s %17$5s\r\n",
                        formatNumStr(5, getObjectOr(soilRecord, "sllb", defValR).toString()),
                        getObjectOr(soilRecord, "slmh", defValC).toString(),
                        formatNumStr(5, getObjectOr(soilRecord, "slll", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "sldul", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "slsat", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "slrgf", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "sksat", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "slbdm", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "sloc", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "slcly", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "slsil", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "slcf", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "slni", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "slphw", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "slphb", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "slcec", defValR).toString()),
                        formatNumStr(5, getObjectOr(soilRecord, "sadc", defValR).toString())));

                // part two
                if (p2Flg) {
                    sbLyrP2.append(String.format(" %1$5s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s %15$5s %16$5s %17$5s\r\n",
                            formatNumStr(5, getObjectOr(soilRecord, "sllb", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slpx", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slpt", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slpo", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "caco3", defValR).toString()), // P.S. Different with document (DSSAT vol2.pdf)
                            formatNumStr(5, getObjectOr(soilRecord, "slal", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slfe", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slmn", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slbs", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slpa", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slpb", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slke", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slmg", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slna", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slsu", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slec", defValR).toString()),
                            formatNumStr(5, getObjectOr(soilRecord, "slca", defValR).toString())));
                }
            }

            // Add part two
            if (p2Flg) {
                sbData.append(sbLyrP2.toString()).append("\r\n");
                sbLyrP2 = new StringBuilder();
            } else {
                sbData.append("\r\n");
            }
//            }

            // Output finish
            bwS.write(sbError.toString());
            bwS.write(sbData.toString());
            bwS.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Set default value for missing data
     *
     */
    private void setDefVal() {

        // defValD = ""; No need to set default value for Date type in soil file
        defValR = "-99";
        defValC = "-99";
        defValI = "-99";
    }
}
