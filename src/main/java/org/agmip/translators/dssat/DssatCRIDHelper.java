package org.agmip.translators.dssat;

import org.agmip.ace.LookupCodes;

/**
 * Translate the crop id between 2-bit version and 3-bit version
 *
 * @author Meng Zhang
 */
public class DssatCRIDHelper {

//    private HashMap<String, String> crids;  // Mapping the 2-bit value and 3-bit value of crid
    private static final String def2BitVal = "XX";       // default 2-bit value for unknow crid
    private static final String def3BitVal = "XXX";      // default 3-bit value for unknow crid

//    public DssatCRIDHelper() {
//        crids = new HashMap();
//        crids.put("ALF", "AL");
//        crids.put("APL", "");
//        crids.put("APR", "");
//        crids.put("BAR", "BA");
//        crids.put("BHG", "G0");
//        crids.put("BLW", "BW");
//        crids.put("BND", "BN");
//        crids.put("BNG", "GB");
//        crids.put("BNN", "NN");
//        crids.put("BRC", "BR");
//        crids.put("BWH", "");
//        crids.put("CAM", "CM");
//        crids.put("CAR", "");
//        crids.put("CBG", "CB");
//        crids.put("CCN", "CC");
//        crids.put("CFE", "CF");
//        crids.put("CGR", "");
//        crids.put("CHP", "CH");
//        crids.put("CHR", "");
//        crids.put("CIT", "CT");
//        crids.put("CLV", "CV");
//        crids.put("CNL", "CN");
//        crids.put("COT", "CO");
//        crids.put("CRM", "CR");
//        crids.put("CST", "");
//        crids.put("CSV", "CS");
//        crids.put("CWP", "CP");
//        crids.put("FAL", "FA");
//        crids.put("FBN", "FB");
//        crids.put("FIG", "");
//        crids.put("FXM", "");
//        crids.put("GRV", "GR");
//        crids.put("GRW", "GW");
//        crids.put("GUA", "");
//        crids.put("HOP", "");
//        crids.put("JBN", "");
//        crids.put("JUT", "");
//        crids.put("LBN", "");
//        crids.put("LEN", "LN");
//        crids.put("MAZ", "MZ");
//        crids.put("NPG", "NP");
//        crids.put("OAT", "OA");
//        crids.put("OLP", "");
//        crids.put("ONN", "");
//        crids.put("PEA", "PE");
//        crids.put("PER", "");
//        crids.put("PGP", "PP");
//        crids.put("PML", "ML");
//        crids.put("PNA", "PI");
//        crids.put("PNT", "PN");
//        crids.put("POT", "PT");
//        crids.put("PPN", "PO");
//        crids.put("PPR", "PR");
//        crids.put("PRM", "");
//        crids.put("QUN", "");
//        crids.put("RAP", "RA");
//        crids.put("RHP", "RP");
//        crids.put("RIC", "RI");
//        crids.put("SAF", "");
//        crids.put("SBN", "SB");
//        crids.put("SBT", "BS");
//        crids.put("SES", "SS");
//        crids.put("SGG", "SG");
//        crids.put("STR", "ST");
//        crids.put("SUC", "SC");
//        crids.put("SUN", "SU");
//        crids.put("SWC", "SW");
//        crids.put("SWG", "SI");
//        crids.put("SWP", "SP");
//        crids.put("TAN", "TN");
//        crids.put("TAR", "TR");
//        crids.put("TBN", "");
//        crids.put("TGR", "");
//        crids.put("TOM", "TM");
//        crids.put("TRT", "TL");
//        crids.put("VBN", "VB");
//        crids.put("VIN", "VI");
//        crids.put("WHB", "WH");
//        crids.put("WHD", "WH");
//    }

    /**
     * get 2-bit version of crop id by given string
     *
     * @param str input string of 3-bit crid
     * @return 2-bit version of crop id
     */
    public static String get2BitCrid(String str) {
         if (str != null) {
             String crid = LookupCodes.lookupCode("CRID", str, "DSSAT");
             if (crid.equals(str) && crid.length() > 2) {
                 crid = def2BitVal;
             }
            return crid.toUpperCase();
        } else {
            return def2BitVal;
        }
//        if (str == null) {
//            return def2BitVal;
//        } else if (str.length() == 2) {
//            return str;
//        }
//        String ret = crids.get(str);
//        if (ret == null || ret.equals("")) {
//            return str;
//        } else {
//            return ret;
//        }
    }

    /**
     * get 3-bit version of crop id by given string
     *
     * @param str input string of 2-bit crop id
     * @return 3-bit version of crop id
     */
    public static String get3BitCrid(String str) {
        if (str != null) {
//            return LookupCodes.modelLookupCode("DSSAT", "CRID", str).toUpperCase();
            return LookupCodes.lookupCode("CRID", str, "code", "DSSAT").toUpperCase();
        } else {
            return def3BitVal;
        }
//        if (str == null || !crids.containsValue(str)) {
//            return def3BitVal;
//        } else if (str.length() == 3) {
//            return str;
//        }
//        for (String key : crids.keySet()) {
//            if (str.equals(crids.get(key))) {
//                return key;
//            }
//        }
//        return str;
    }
}
