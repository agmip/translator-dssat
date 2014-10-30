package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT Soil Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatSoilOutput extends DssatCommonOutput {

    private static final Logger LOG = LoggerFactory.getLogger(DssatSoilOutput.class);

    /**
     * DSSAT Soil Data Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     */
    @Override
    public void writeFile(String arg0, Map result) {

        // Initial variables
//        ArrayList soilSites;                            // Soil site data array
        HashMap soilSite;                       // Data holder for one site of soil data
        ArrayList<Map> soilSistes;
        ArrayList<HashMap> soilRecords;                          // Soil layer data array
        BufferedWriter bwS;                             // output object
        StringBuilder sbTitle = new StringBuilder();
        StringBuilder sbSites = new StringBuilder();
        StringBuilder sbData;                           // construct the data info in the output
        StringBuilder sbLyrP2 = new StringBuilder();    // output string for second part of layer data
        boolean p2Flg;
        String[] p2Ids = {"slpx", "slpt", "slpo", "caco3", "slal", "slfe", "slmn", "slbs", "slpa", "slpb", "slke", "slmg", "slna", "slsu", "slec", "slca"};
        String layerKey = "soilLayer";  // P.S. the key name might change

        try {

            // Set default value for missing data
            setDefVal();

            soilSistes = getObjectOr(result, "soils", new ArrayList());
            if (soilSistes.isEmpty()) {
                soilSistes.add(result);
            }

            soilSite = (HashMap) getObjectOr(soilSistes.get(0), "soil", new HashMap());
            if (soilSite.isEmpty()) {
                return;
            }
//            decompressData(soilSites);

            // Initial BufferedWriter
            // Get File name
            String soilId = getObjectOr(soilSite, "soil_id", "");
            String fileName;
            if (soilId.equals("")) {
                fileName = "soil.SOL";
            } else {
                try {
                    fileName = soilId.substring(0, 2) + ".SOL";
                } catch (Exception e) {
                    fileName = "soil.SOL";
                }

            }
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
//                boolean existFlg = outputFile.exists();
            bwS = new BufferedWriter(new FileWriter(outputFile));

            // Description info for output by translator
            sbTitle.append("!This soil file is created by DSSAT translator tool on ").append(Calendar.getInstance().getTime()).append(".\r\n");
            sbTitle.append("*SOILS: ");

            for (Map expData : soilSistes) {

                soilSite = (HashMap) getObjectOr(expData, "soil", new HashMap());
                sbError = new StringBuilder();
                sbData = new StringBuilder();

                // Output Soil File
                // Titel Section
                String sl_notes = getObjectOr((HashMap) soilSite, "sl_notes", defValBlank);
                if (!sl_notes.equals(defValBlank)) {
                    sbTitle.append(sl_notes).append("; ");
                }
                sbData.append("!The ACE ID is ").append(getValueOr(expData, "id", "N/A")).append(".\r\n");
                sbData.append("!This soil data is used for the experiment of ").append(getValueOr(expData, "exname", "N/A")).append(".\r\n!\r\n");

                // Site Info Section
                String soil_id = getSoilID(soilSite);
                if (soil_id.equals("")) {
                    sbError.append("! Warning: Incompleted record because missing data : [soil_id]\r\n");
                } else if (soil_id.length() > 10) {
                    sbError.append("! Warning: Oversized data : [soil_id] ").append(soil_id).append("\r\n");
                }
                sbData.append(String.format("*%1$-10s  %2$-11s %3$-5s %4$5s %5$s\r\n",
                        soil_id,
                        formatStr(11, soilSite, "sl_source", defValC),
                        formatStr(5, transSltx(getValueOr(soilSite, "sltx", defValC)), "sltx"),
                        formatNumStr(5, soilSite, "sldp", defValR),
                        getObjectOr(soilSite, "soil_name", defValC)));
                sbData.append("@SITE        COUNTRY          LAT     LONG SCS FAMILY\r\n");
                sbData.append(String.format(" %1$-11s %2$-11s %3$9s%4$8s %5$s\r\n",
                        formatStr(11, soilSite, "sl_loc_3", defValC),
                        formatStr(11, soilSite, "sl_loc_1", defValC),
                        formatNumStr(8, soilSite, "soil_lat", defValR), // P.S. Definition changed 9 -> 10 (06/24)
                        formatNumStr(8, soilSite, "soil_long", defValR), // P.S. Definition changed 9 -> 8  (06/24)
                        getObjectOr(soilSite, "classification", defValC)));
                sbData.append("@ SCOM  SALB  SLU1  SLDR  SLRO  SLNF  SLPF  SMHB  SMPX  SMKE\r\n");
//                if (getObjectOr(soilSite, "slnf", "").equals("")) {
//                    sbError.append("! Warning: missing data : [slnf], and will automatically use default value '1'\r\n");
//                }
//                if (getObjectOr(soilSite, "slpf", "").equals("")) {
//                    sbError.append("! Warning: missing data : [slpf], and will automatically use default value '0.92'\r\n");
//                }
                sbData.append(String.format(" %1$5s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s\r\n",
                        getObjectOr(soilSite, "sscol", defValC),
                        formatNumStr(5, soilSite, "salb", defValR),
                        formatNumStr(5, soilSite, "slu1", defValR),
                        formatNumStr(5, soilSite, "sldr", defValR),
                        formatNumStr(5, soilSite, "slro", defValR),
                        formatNumStr(5, soilSite, "slnf", defValR), // P.S. Remove default value as '1'
                        formatNumStr(5, soilSite, "slpf", defValR), // P.S. Remove default value as '0.92'
                        getObjectOr(soilSite, "smhb", defValC),
                        getObjectOr(soilSite, "smpx", defValC),
                        getObjectOr(soilSite, "smke", defValC)));

                // Soil Layer data section
                soilRecords = (ArrayList) getObjectOr(soilSite, layerKey, new ArrayList());

                // part one
                sbData.append("@  SLB  SLMH  SLLL  SDUL  SSAT  SRGF  SSKS  SBDM  SLOC  SLCL  SLSI  SLCF  SLNI  SLHW  SLHB  SCEC  SADC\r\n");
                // part two
                sbLyrP2.append("@  SLB  SLPX  SLPT  SLPO CACO3  SLAL  SLFE  SLMN  SLBS  SLPA  SLPB  SLKE  SLMG  SLNA  SLSU  SLEC  SLCA\r\n");
                p2Flg = false;

                // Loop for laryer data
                for (HashMap soilRecord : soilRecords) {

                    // part one
                    sbData.append(String.format(" %1$5s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s %15$5s %16$5s %17$5s\r\n",
                            formatNumStr(5, soilRecord, "sllb", defValR),
                            getObjectOr(soilRecord, "slmh", defValC),
                            formatNumStr(5, soilRecord, "slll", defValR),
                            formatNumStr(5, soilRecord, "sldul", defValR),
                            formatNumStr(5, soilRecord, "slsat", defValR),
                            formatNumStr(5, soilRecord, "slrgf", defValR),
                            formatNumStr(5, soilRecord, "sksat", defValR),
                            formatNumStr(5, soilRecord, "slbdm", defValR),
                            formatNumStr(5, soilRecord, "sloc", defValR),
                            formatNumStr(5, soilRecord, "slcly", defValR),
                            formatNumStr(5, soilRecord, "slsil", defValR),
                            formatNumStr(5, soilRecord, "slcf", defValR),
                            formatNumStr(5, soilRecord, "slni", defValR),
                            formatNumStr(5, soilRecord, "slphw", defValR),
                            formatNumStr(5, soilRecord, "slphb", defValR),
                            formatNumStr(5, soilRecord, "slcec", defValR),
                            formatNumStr(5, soilRecord, "sladc", defValR)));
                    // part two
                    sbLyrP2.append(String.format(" %1$5s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s %15$5s %16$5s %17$5s\r\n",
                            formatNumStr(5, soilRecord, "sllb", defValR),
                            formatNumStr(5, soilRecord, "slpx", defValR),
                            formatNumStr(5, soilRecord, "slpt", defValR),
                            formatNumStr(5, soilRecord, "slpo", defValR),
                            formatNumStr(5, soilRecord, "caco3", defValR), // P.S. Different with document (DSSAT vol2.pdf)
                            formatNumStr(5, soilRecord, "slal", defValR),
                            formatNumStr(5, soilRecord, "slfe", defValR),
                            formatNumStr(5, soilRecord, "slmn", defValR),
                            formatNumStr(5, soilRecord, "slbs", defValR),
                            formatNumStr(5, soilRecord, "slpa", defValR),
                            formatNumStr(5, soilRecord, "slpb", defValR),
                            formatNumStr(5, soilRecord, "slke", defValR),
                            formatNumStr(5, soilRecord, "slmg", defValR),
                            formatNumStr(5, soilRecord, "slna", defValR),
                            formatNumStr(5, soilRecord, "slsu", defValR),
                            formatNumStr(5, soilRecord, "slec", defValR),
                            formatNumStr(5, soilRecord, "slca", defValR)));
                    // Check if there is 2nd part of layer data for output
                    if (!p2Flg) {
                        for (String p2Id : p2Ids) {
                            if (!getValueOr(soilRecord, p2Id, "").equals("")) {
                                p2Flg = true;
                                break;
                            }
                        }
                    }
                }

                // Add part two
                if (p2Flg) {
                    sbData.append(sbLyrP2.toString()).append("\r\n");
                    sbLyrP2 = new StringBuilder();
                } else {
                    sbData.append("\r\n");
                }

                // Finish one site
                sbSites.append(sbError.toString());
                sbSites.append(sbData.toString());
            }

            // Output finish
            sbTitle.append("\r\n\r\n");
            bwS.write(sbTitle.toString());
            bwS.write(sbSites.toString());
            bwS.close();

        } catch (IOException e) {
            LOG.error(DssatCommonOutput.getStackTrace(e));
        }
    }
}
