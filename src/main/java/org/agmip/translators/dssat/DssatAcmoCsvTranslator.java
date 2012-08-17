package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import static org.agmip.translators.dssat.DssatCommonOutput.revisePath;
import static org.agmip.util.MapUtil.*;

/**
 * DSSAT AFile Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatAcmoCsvTranslator {

    private File outputFile;

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * Generate ACMO CSV file
     *
     * @param outputCsvPath The path for output csv file
     * @param inputFilePath The path for input zip file which contains *.OUT
     * @param metaDataArr The array of mini json objects
     */
    public void writeCsvFile(String outputCsvPath, String inputFilePath, ArrayList<LinkedHashMap> metaDataArr) throws IOException {

        DssatOutputFileInput dssatReader = new DssatOutputFileInput();
        StringBuilder sbData = new StringBuilder();
        ArrayList<LinkedHashMap> inArr = dssatReader.readFileAll(inputFilePath);
        LinkedHashMap sumData = new LinkedHashMap();
        ArrayList<LinkedHashMap> sumSubArr = new ArrayList();
        LinkedHashMap sumSubData;
        ArrayList<LinkedHashMap> ovwSubArr = new ArrayList();
        LinkedHashMap ovwSubData;
        ArrayList<LinkedHashMap> soilOrgArr = new ArrayList();
        LinkedHashMap soilOrgData;
        LinkedHashMap metaData;
        if (!inArr.isEmpty()) {
            // Set Summary data
            sumData = inArr.get(0);
            sumSubArr = getObjectOr(sumData, "data", new ArrayList<LinkedHashMap>());
            // Set Overview data
            for (int i = 1; i < sumSubArr.size() + 1 && i < inArr.size(); i++) {
                ovwSubArr.add(inArr.get(i));
            }
            // Set soil organic matter data
            for (int i = 1 + sumSubArr.size(); i < inArr.size(); i++) {
                soilOrgArr.add(inArr.get(i));
            }
        }

        outputCsvPath = revisePath(outputCsvPath);
        outputFile = new File(outputCsvPath + "ACMO.csv");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

        // Write titles
        sbData.append("EID,CLIM_ID,CLIM_REP,RAP_ID,CROP_MODEL,REGION,TRTNO,SQ,ID_FIELD,INSTITUTION,WSTA_ID,SOIL_ID,FL_LAT,FL_LONG,CRID,CUL_ID,SDAT,PDATE,HDATE,SOILC_INIT,SURFC_INIT,IR#C,IR_TOT,IROP,FE_#,FEN_TOT,FEP_TOT,FEK_TOT,TI_#,TIIMP,RESC,PEST_MGMT,PRCM,HWAH,CWAM,BWAH,PLDAE,ADAT,MDAT,PWAM,HWUM,H#AM,H#UM,HIAM,LAIX,ETCM,EPCM,ESCM,ROCM,DRCM,SWXM,NUCM,NLCM,NIAM,CNAM,GNAM,SOILC_FINAL,SURFC_FINAL,N20,CH4\r\n");
        // Write data
        String version = getObjectOr(sumData, "vevsion", "Ver. N/A");
        for (int i = 0; i < ovwSubArr.size(); i++) {
            sumSubData = sumSubArr.get(i);
            ovwSubData = ovwSubArr.get(i);
            String runno = getObjectOr(sumSubData, "runno", "");
            soilOrgData = getSoilOrgData(soilOrgArr, runno);
            String trno = getObjectOr(ovwSubData, "trno", "");
            String exp_id = getObjectOr(ovwSubData, "exp_id", "");
            String cr = getObjectOr(ovwSubData, "cr", "");
            metaData = getMetaData(metaDataArr, trno, exp_id, cr);

            // Write CSV data
            sbData.append("\"").append(getObjectOr(metaData, "eid", "")).append("\","); // EID
            sbData.append("\"").append(getObjectOr(metaData, "clim_id", "")).append("\","); // CLIM_ID
            sbData.append("\"").append(getObjectOr(metaData, "clim_rep", "")).append("\","); // CLIM_REP
            sbData.append("\"").append(getObjectOr(metaData, "rap_id", "")).append("\","); // RAP_ID
            sbData.append("\"DSSAT ").append(getObjectOr(sumSubData, "model", "")).append(" ").append(version).append("\","); // CROP_MODEL
            sbData.append("\"").append(getObjectOr(metaData, "region", "")).append("\","); // REGION
            sbData.append("\"").append(getObjectOr(sumSubData, "trno", "")).append("\","); // TRTNO
            sbData.append("\"").append(getObjectOr(sumSubData, "r#", "")).append("\","); // SQ
            sbData.append("\"").append(getObjectOr(sumSubData, "fnam", "")).append("\","); // ID_FIELD
            sbData.append("\"").append(getObjectOr(metaData, "institution", "")).append("\","); // INSTITUTION
            sbData.append("\"").append(getObjectOr(metaData, "wsta_id", "")).append("\","); // WSTA_ID
            sbData.append("\"").append(getObjectOr(metaData, "soil_id", "")).append("\","); // SOIL_ID
            sbData.append("\"").append(getObjectOr(metaData, "fl_lat", "")).append("\","); // FL_LAT
            sbData.append("\"").append(getObjectOr(metaData, "fl_long", "")).append("\","); // FL_LONG
            sbData.append("\"").append(getObjectOr(metaData, "crid", "")).append("\","); // CRID
            sbData.append("\"").append(getObjectOr(metaData, "cul_id", "")).append("\","); // CUL_ID
            sbData.append("\"").append(getObjectOr(sumSubData, "sdat", "")).append("\","); // SDAT
            sbData.append("\"").append(getObjectOr(sumSubData, "pdat", "")).append("\","); // PDATE
            sbData.append("\"").append(getObjectOr(sumSubData, "hdat", "")).append("\","); // HDATE
            sbData.append("\"").append(getObjectOr(soilOrgData, "solic_init", "")).append("\","); // SOILC_INIT
            sbData.append("\"").append(getObjectOr(soilOrgData, "surfc_init", "")).append("\","); // SURFC_INIT
            sbData.append("\"").append(getObjectOr(sumSubData, "ir#m", "")).append("\","); // IR#C
            sbData.append("\"").append(getObjectOr(sumSubData, "ircm", "")).append("\","); // IR_TOT
            sbData.append("\"").append(getObjectOr(metaData, "irop", "")).append("\","); // IROP
            sbData.append("\"").append(getObjectOr(sumSubData, "ni#m", "")).append("\","); // FE_#
            sbData.append("\"").append(getObjectOr(sumSubData, "nicm", "")).append("\","); // FEN_TOT
            sbData.append("\"").append(getObjectOr(sumSubData, "picm", "")).append("\","); // FEP_TOT
            sbData.append("\"").append(getObjectOr(sumSubData, "kicm", "")).append("\","); // FEK_TOT
            sbData.append("\"").append(getObjectOr(metaData, "ti_#", "")).append("\","); // TI_#
            sbData.append("\"").append(getObjectOr(metaData, "tiimp", "")).append("\","); // TIIMP
            sbData.append("\"").append(getObjectOr(sumSubData, "recm", "")).append("\","); // RESC
            sbData.append(","); // PEST_MGMT
            sbData.append("\"").append(getObjectOr(sumSubData, "prcm", "")).append("\","); // PRCM
            sbData.append("\"").append(getObjectOr(sumSubData, "hwah", "")).append("\","); // HWAH
            sbData.append("\"").append(getObjectOr(sumSubData, "cwam", "")).append("\","); // CWAM
            sbData.append("\"").append(getObjectOr(sumSubData, "bwah", "")).append("\","); // BWAH
            sbData.append("\"").append(getObjectOr(sumSubData, "edat", "")).append("\","); // PLDAE
            sbData.append("\"").append(getObjectOr(sumSubData, "adat", "")).append("\","); // ADAT
            sbData.append("\"").append(getObjectOr(sumSubData, "mdat", "")).append("\","); // MDAT
            sbData.append("\"").append(getObjectOr(sumSubData, "pwam", "")).append("\","); // PWAM
            sbData.append("\"").append(getObjectOr(sumSubData, "hwum", "")).append("\","); // HWUM
            sbData.append("\"").append(getObjectOr(sumSubData, "h#am", "")).append("\","); // H#AM
            sbData.append("\"").append(getObjectOr(sumSubData, "h#um", "")).append("\","); // H#UM
            sbData.append("\"").append(getObjectOr(sumSubData, "hiam", "")).append("\","); // HIAM
            sbData.append("\"").append(getObjectOr(sumSubData, "laix", "")).append("\","); // LAIX
            sbData.append("\"").append(getObjectOr(sumSubData, "etcm", "")).append("\","); // ETCM
            sbData.append("\"").append(getObjectOr(sumSubData, "epcm", "")).append("\","); // EPCM
            sbData.append("\"").append(getObjectOr(sumSubData, "escm", "")).append("\","); // ESCM
            sbData.append("\"").append(getObjectOr(sumSubData, "rocm", "")).append("\","); // ROCM
            sbData.append("\"").append(getObjectOr(sumSubData, "drcm", "")).append("\","); // DRCM
            sbData.append("\"").append(getObjectOr(sumSubData, "swxm", "")).append("\","); // SWXM
            sbData.append("\"").append(getObjectOr(sumSubData, "nucm", "")).append("\","); // NUCM
            sbData.append("\"").append(getObjectOr(sumSubData, "nlcm", "")).append("\","); // NLCM
            sbData.append("\"").append(getObjectOr(sumSubData, "niam", "")).append("\","); // NIAM
            sbData.append("\"").append(getObjectOr(sumSubData, "cnam", "")).append("\","); // CNAM
            sbData.append("\"").append(getObjectOr(sumSubData, "gnam", "")).append("\","); // GNAM
            sbData.append("\"").append(getObjectOr(soilOrgData, "solic_final", "")).append("\","); // SOILC_FINAL
            sbData.append("\"").append(getObjectOr(soilOrgData, "surfc_final", "")).append("\","); // SURFC_FINAL
            sbData.append(","); // N20
            sbData.append(","); // CH4
            sbData.append("\r\n");

        }

        bw.write(sbData.toString());
        bw.close();
    }

    /**
     * Get soil organic matter data related with input info
     *
     * @param arr The array contains data objects
     * @param runno The run number
     */
    private LinkedHashMap getSoilOrgData(ArrayList<LinkedHashMap> arr, String runno) {

        for (int i = 0; i < arr.size(); i++) {
            if (getObjectOr(arr.get(i), "runno", "").equals(runno)) {
                return arr.get(i);
            }
        }

        return new LinkedHashMap();
    }

    /**
     * Get meta data related with input info
     *
     * @param arr The array contains data objects
     * @param trno The treatment number
     * @param exp_id The experiment id (8-bit)
     * @param cr The crop id (2-bit)
     */
    private LinkedHashMap getMetaData(ArrayList<LinkedHashMap> arr, String trno, String exp_id, String cr) {

        String exname;
        for (int i = 0; i < arr.size(); i++) {
            exname = getObjectOr(arr.get(i), "exname", "");
            if (exname.equals(exp_id + cr + "_" + trno) || exname.equals(exp_id + cr)) {
                return arr.get(i);
            }
        }

        return new LinkedHashMap();
    }
}
