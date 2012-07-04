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
 * DSSAT Soil Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatSoilOutput extends DssatCommonOutput {

    private File[] outputFiles;

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return getOutputFile(0);
    }

    /**
     * Get output file object by array index
     */
    public File getOutputFile(int id) {

        if (outputFiles != null && id < outputFiles.length) {
            return outputFiles[id];
        } else {
            return null;
        }
    }

    /**
     * Get output files array
     */
    public File[] getOutputFiles() {
        return outputFiles;
    }

    /**
     * DSSAT Soil Data Output method
     * 
     * @param arg0   file output path
     * @param result  data holder object
     */
    @Override
    public void writeFile(String arg0, AdvancedHashMap result) {

        // Initial variables
        JSONAdapter adapter = new JSONAdapter();        // JSON Adapter
        ArrayList soilFiles;                            // Soil file array
        AdvancedHashMap soilFile;                       // Data holder for one file of soil data
        ArrayList soilSites;                            // Soil site data array
        AdvancedHashMap soilSite;                       // Data holder for one site of soil data
        ArrayList soilRecords;                          // Soil layer data array
        AdvancedHashMap soilRecord;                     // Data holder for one layer data
        BufferedWriter bwS;                             // output object
        StringBuilder sbData = new StringBuilder();     // construct the data info in the output
        StringBuilder sbLyrP2 = new StringBuilder();    // output string for second part of layer data
        boolean p2Flg;
        String[] p2Ids = {"slpx", "slpt", "slpo", "caco3", "slal", "slfe", "slmn", "slbs", "slpa", "slpb", "slke", "slmg", "slna", "slsu", "slec", "slca"};
        String layerKey = "data";  // P.S. the key name might change

        try {

            // Set default value for missing data
            setDefVal();

            soilFiles = (ArrayList) result.getOr("soil", new ArrayList());
            decompressData(soilFiles);
            outputFiles = new File[soilFiles.size()];

            for (int i = 0; i < soilFiles.size(); i++) {
                soilFile = adapter.exportRecord((Map) soilFiles.get(i));
                soilSites = (ArrayList) soilFile.getOr("soil_sites", new ArrayList());
                

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
                outputFiles[i] = new File(arg0 + fileName);
                bwS = new BufferedWriter(new FileWriter(outputFiles[i]));

                // Output Soil File
                // Titel Section
                bwS.write("*SOILS: " + soilFile.getOr("address", defValC) +"\r\n\r\n");

                // Loop sites of data
                for (int j = 0; j < soilSites.size(); j++) {
                    soilSite = adapter.exportRecord((Map) soilSites.get(j));

                    // Site Info Section
                    sbData.append(String.format("*%1$-10s  %2$-11s %3$-5s %4$5s %5$s\r\n",
                            soilSite.getOr("soil_id", defValC).toString(),
                            soilSite.getOr("sl_source", defValC).toString(),
                            soilSite.getOr("sltx", defValC).toString(),
                            formatNumStr(5, soilSite.getOr("sldp", defValR).toString()),
                            soilSite.getOr("soil_name", defValC).toString()));
                    sbData.append("@SITE        COUNTRY          LAT     LONG SCS FAMILY\r\n");
                    sbData.append(String.format(" %1$-11s %2$-11s %3$9s%4$8s %5$s\r\n",
                            soilSite.getOr("sl_loc_3", defValC).toString(),
                            soilSite.getOr("sl_loc_1", defValC).toString(),
                            formatNumStr(8, soilSite.getOr("soil_lat", defValR).toString()),     // P.S. Definition changed 9 -> 10 (06/24)
                            formatNumStr(8, soilSite.getOr("soil_long", defValR).toString()),    // P.S. Definition changed 9 -> 8  (06/24)
                            soilSite.getOr("classification", defValC).toString()));
                    sbData.append("@ SCOM  SALB  SLU1  SLDR  SLRO  SLNF  SLPF  SMHB  SMPX  SMKE\r\n");
                    sbData.append(String.format(" %1$5s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$-5s %9$-5s %10$-5s\r\n",
                            soilSite.getOr("scom", defValC).toString(),
                            formatNumStr(5, soilSite.getOr("salb", defValR).toString()),
                            formatNumStr(5, soilSite.getOr("slu1", defValR).toString()),
                            formatNumStr(5, soilSite.getOr("sldr", defValR).toString()),
                            formatNumStr(5, soilSite.getOr("slro", defValR).toString()),
                            formatNumStr(5, soilSite.getOr("slnf", defValR).toString()),
                            formatNumStr(5, soilSite.getOr("slpf", defValR).toString()),
                            soilSite.getOr("smhb", defValC).toString(),
                            soilSite.getOr("smpx", defValC).toString(),
                            soilSite.getOr("smke", defValC).toString()));

                    // Soil Layer data section
                    soilRecords = (ArrayList) soilSite.getOr(layerKey, new ArrayList());

                    // part one
                    sbData.append("@  SLB  SLMH  SLLL  SDUL  SSAT  SRGF  SSKS  SBDM  SLOC  SLCL  SLSI  SLCF  SLNI  SLHW  SLHB  SCEC  SADC\r\n");
                    // part two
                    // Get first site record
                    AdvancedHashMap fstRecord = new AdvancedHashMap();
                    if (!soilRecords.isEmpty()) {
                        fstRecord = adapter.exportRecord((Map) soilRecords.get(0));
                    }
                    
                    // Check if there is 2nd part of layer data for output
                    p2Flg = false;
                    for (int k = 0; k < p2Ids.length; k++) {
                        if (!fstRecord.getOr(p2Ids[k], "").toString().equals("")) {
                            p2Flg = true;
                            break;
                        }
                    }
                    if (p2Flg) {
                        sbLyrP2.append("@  SLB  SLPX  SLPT  SLPO CACO3  SLAL  SLFE  SLMN  SLBS  SLPA  SLPB  SLKE  SLMG  SLNA  SLSU  SLEC  SLCA\r\n");
                    }

                    // Loop for laryer data
                    for (int k = 0; k < soilRecords.size(); k++) {

                        soilRecord = adapter.exportRecord((Map) soilRecords.get(k));
                        // part one
                        sbData.append(String.format(" %1$5s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s %15$5s %16$5s %17$5s\r\n",
                                formatNumStr(5, soilRecord.getOr("sllb", defValR).toString()), //TODO Do I need to check if sllb is a valid value
                                soilRecord.getOr("slmh", defValC).toString(),
                                formatNumStr(5, soilRecord.getOr("slll", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("sldul", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("slsat", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("slrgf", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("sksat", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("slbdm", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("sloc", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("slcly", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("slsil", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("slcf", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("slni", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("slphw", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("slphb", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("slcec", defValR).toString()),
                                formatNumStr(5, soilRecord.getOr("sadc", defValR).toString())));

                        // part two
                        if (p2Flg) {
                            sbLyrP2.append(String.format(" %1$5s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s %15$5s %16$5s %17$5s\r\n",
                                    formatNumStr(5, soilRecord.getOr("sllb", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slpx", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slpt", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slpo", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("caco3", defValR).toString()), // P.S. Different with document (DSSAT vol2.pdf)
                                    formatNumStr(5, soilRecord.getOr("slal", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slfe", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slmn", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slbs", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slpa", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slpb", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slke", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slmg", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slna", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slsu", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slec", defValR).toString()),
                                    formatNumStr(5, soilRecord.getOr("slca", defValR).toString())));
                        }
                    }

                    // Add part two
                    if (p2Flg) {
                        sbData.append(sbLyrP2.toString()).append("\r\n");
                        sbLyrP2 = new StringBuilder();
                    } else {
                        sbData.append("\r\n");
                    }
                }

                // Output finish
                bwS.write(sbError.toString());
                bwS.write(sbData.toString());
                bwS.close();
                sbError = new StringBuilder();
                sbData = new StringBuilder();
            }


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
        defValC = "-99";    // TODO wait for confirmation
        defValI = "-99";
    }
}
