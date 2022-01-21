package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.agmip.util.MapUtil;
import static org.agmip.util.MapUtil.*;

/**
 * DSSAT Experiment Data I/O API Class1
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatXFileInput extends DssatCommonInput {

    public String eventKey = "data";
    public String icEventKey = "soilLayer";

    /**
     * Constructor with no parameters Set jsonKey as "experiment"
     *
     */
    public DssatXFileInput() {
        super();
        jsonKey = "management";
    }

    /**
     * DSSAT XFile Data input method for only inputing experiment file
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     * @throws java.io.IOException
     */
    @Override
    protected HashMap readFile(HashMap brMap) throws IOException {

        HashMap ret = new HashMap();
        HashMap metaData = new HashMap();
        ArrayList<HashMap> expArr = new ArrayList<HashMap>();
        HashMap expData;
        ArrayList<HashMap> mgnArr = readTreatments(brMap, metaData);

        for (int i = 0; i < mgnArr.size(); i++) {

            // Set meta data block for this treatment
            expData = setupMetaData(metaData, i);

            // Set soil_analysis block to soil block
            copyItem(expData, expData, "soil", "soil_analysis", true);
            HashMap soilTmp = getObjectOr(expData, "soil", new HashMap());
            if (!soilTmp.isEmpty()) {
                expData.put("soil_id", expData.get("soil_id") + "_" + (i + 1));
            }
            copyItem(soilTmp, expData, "soil_id");
            copyItem(soilTmp, expData, "sltx");
            copyItem(soilTmp, expData, "sldp");
            ArrayList<HashMap> soilSubArr = getObjectOr(soilTmp, icEventKey, new ArrayList());
            for (HashMap soilSubData : soilSubArr) {
                soilSubData.remove("slsc");
            }
            expData.put("soil", soilTmp);

            // Set management data for this treatment
            setupTrnData(expData, mgnArr.get(i));

            // Add to output array
            expArr.add(expData);

        }

//        compressData(expArr);
        ret.put("experiments", expArr);
        return ret;
    }

    /**
     * DSSAT XFile Data input method for Controller using (no compressing data)
     *
     * @param brMap The holder for BufferReader objects for all files
     * @param metaData
     * @return trArr The array of treatment data
     * @throws java.io.IOException
     */
    protected ArrayList<HashMap> readTreatments(HashMap brMap, HashMap metaData) throws IOException {

        String line;
        BufferedReader br;
        Object buf;
        BufferedReader brw = null;
        char[] bufW = null;
        HashMap mapX;
        HashMap mapW;
        String wid;
        String fileName;
        LinkedHashMap formats = new LinkedHashMap();
        ArrayList<HashMap> trArr = new ArrayList<HashMap>();
        HashMap trData = new HashMap();
        ArrayList<HashMap> evtArr = new ArrayList<HashMap>();
        String ireff = "";    // P.S. special handling for EFIR in irrigation section

        // Set meta data info for each treatment
        ArrayList<HashMap> trMetaArr = new ArrayList<HashMap>();
        HashMap trMetaData;
        metaData.put("tr_meta", trMetaArr);

        mapX = (HashMap) brMap.get("X");
        mapW = (HashMap) brMap.get("W");

        // If XFile is no been found
        if (mapX.isEmpty()) {
            return trArr;
        }

        for (Object keyX : mapX.keySet()) {
            buf = mapX.get(keyX);
            if (buf instanceof char[]) {
                br = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                br = (BufferedReader) buf;
            }

//        fileName = (String) brMap.get("Z");
            fileName = (String) keyX;
            wid = fileName.length() > 4 ? fileName.substring(0, 4) : fileName;
//            String exname = fileName.replaceAll("\\.", "").replaceAll("X$", "");
            String exname = fileName.replaceAll("\\.\\w\\wX$", "");
            HashMap meta = new HashMap();
            metaData.put(exname, meta);

            ArrayList<HashMap> sqArr = new ArrayList<HashMap>();
            HashMap sqData;
            ArrayList<HashMap> cuArr = new ArrayList<HashMap>();
            ArrayList<HashMap> flArr = new ArrayList<HashMap>();
            ArrayList<HashMap> saArr = new ArrayList<HashMap>();
            ArrayList<HashMap> sadArr = new ArrayList<HashMap>();
            ArrayList<HashMap> icArr = new ArrayList<HashMap>();
            ArrayList<HashMap> icdArr = new ArrayList<HashMap>();
            ArrayList<HashMap> plArr = new ArrayList<HashMap>();
            ArrayList<HashMap> irArr = new ArrayList<HashMap>();
            ArrayList<HashMap> irdArr = new ArrayList<HashMap>();
            ArrayList<HashMap> feArr = new ArrayList<HashMap>();
            ArrayList<HashMap> omArr = new ArrayList<HashMap>();
            ArrayList<HashMap> chArr = new ArrayList<HashMap>();
            ArrayList<HashMap> tiArr = new ArrayList<HashMap>();
            ArrayList<HashMap> emArr = new ArrayList<HashMap>();
            ArrayList<HashMap> haArr = new ArrayList<HashMap>();
            ArrayList<HashMap> smArr = new ArrayList<HashMap>();

            while ((line = br.readLine()) != null) {

                // Get content type of line
                judgeContentType(line);

                // Read Exp title info
                if (flg[0].startsWith("exp.details:") && flg[2].isEmpty()) {

                    // Set variables' formats
                    formats.clear();
                    formats.put("null_1", 14);
                    formats.put("null_2", 11);  // P.S. Since exname in top line is not reliable, read from file name
                    formats.put("local_name", 61);
                    // Read line and save into return holder
                    meta.putAll(readLine(line, formats));
                    meta.put("exname", exname);
                    meta.put("in", getObjectOr(meta, "exname", "  ").substring(0, 2).trim());
                } // Read General Section
                else if (flg[0].startsWith("general")) {

                    // People info
                    if (flg[1].equals("people") && flg[2].equals("data")) {
                        if (checkValidValue(line.trim())) {
                            meta.put("person_notes", line.trim());
                        }

                    } // Address info
                    else if (flg[1].equals("address") && flg[2].equals("data")) {
//                        String[] addr;
                        if (checkValidValue(line.trim())) {
//                            addr = line.split(",[ ]*");
                            meta.put("institution", line.trim());
                        } else {
//                            addr = new String[0];
                        }

//                        switch (addr.length) {
//                            case 0:
//                                break;
//                            case 1:
//                                meta.put("fl_loc_1", addr[0]);
//                                break;
//                            case 2:
//                                meta.put("fl_loc_1", addr[1]);
//                                meta.put("fl_loc_2", addr[0]);
//                                break;
//                            case 3:
//                                meta.put("fl_loc_1", addr[2]);
//                                meta.put("fl_loc_2", addr[1]);
//                                meta.put("fl_loc_3", addr[0]);
//                                break;
//                            default:
//                                meta.put("fl_loc_1", addr[addr.length - 1]);
//                                meta.put("fl_loc_2", addr[addr.length - 2]);
//                                String loc3 = "";
//                                for (int i = 0; i < addr.length - 2; i++) {
//                                    loc3 += addr[i] + ", ";
//                                }
//                                meta.put("fl_loc_3", loc3.substring(0, loc3.length() - 2));
//                        }
                    } // Site info
                    else if ((flg[1].equals("site") || flg[1].equals("sites")) && flg[2].equals("data")) {
                        // P.S. site is missing in the master variables list
                        if (checkValidValue(line.trim())) {
                            meta.put("site_name", line.trim());
                        }

                    } // Plot Info
                    else if (flg[1].startsWith("parea") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("plta", 7);
                        formats.put("pltr#", 6);
                        formats.put("pltln", 6);
                        formats.put("pldr", 6);
                        formats.put("pltsp", 6);
                        formats.put("pllay", 6);
                        formats.put("pltha", 6);
                        formats.put("plth#", 6);
                        formats.put("plthl", 6);
                        formats.put("plthm", 16);
                        // Read line and save into return holder
                        meta.putAll(readLine(line, formats));

                    } // Notes field
                    else if (flg[1].equals("notes") && flg[2].equals("data")) {
                        if (!meta.containsKey("tr_notes")) {
                            meta.put("tr_notes", line + "\r\n");
                        } else {
                            String notes = (String) meta.get("tr_notes");
                            notes += line + "\r\n";
                            meta.put("tr_notes", notes);
                        }
                    } else {
                    }

                } // Read TREATMENTS Section
                else if (flg[0].startsWith("treatments")) {

                    // Read TREATMENTS data / Rotation data
                    if (flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("trno", 3); // For 3-bit treatment number (2->3)
                        formats.put("sq", 1);   // For 3-bit treatment number (2->1)
                        formats.put("op", 2);
                        formats.put("co", 2);
                        formats.put("trt_name", 26);
                        formats.put("ge", 3);
                        formats.put("fl", 3);
                        formats.put("sa", 3);
                        formats.put("ic", 3);
                        formats.put("pl", 3);
                        formats.put("ir", 3);
                        formats.put("fe", 3);
                        formats.put("om", 3);
                        formats.put("ch", 3);
                        formats.put("ti", 3);
                        formats.put("em", 3);
                        formats.put("ha", 3);
                        formats.put("sm", 3);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        if (tmp.get("trt_name") == null) {
                            tmp.put("trt_name", meta.get("exname"));
                        }
                        sqArr.add(tmp);
                    } else {
                    }

                } // Read CULTIVARS Section
                else if (flg[0].startsWith("cultivars")) {

                    // Read CULTIVARS data
                    if (flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("ge", 2);
                        formats.put("crid", 3);
                        formats.put("cul_id", 7);
                        formats.put("cul_name", 17);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        Object cul_id = tmp.get("cul_id");
                        if (cul_id != null) {
                            tmp.put("dssat_cul_id", cul_id);
                        }
                        Object crid = tmp.get("crid");
                        if (crid == null) {
                            crid = fileName.replaceAll("\\w+\\.", "").replaceAll("X$", "");
                        }
                        tmp.put("crid", DssatCRIDHelper.get3BitCrid((String) crid));
                        cuArr.add(tmp);
                    } else {
                    }
                } // Read FIELDS Section
                else if (flg[0].startsWith("fields")) {

                    // Read field info 1st line
                    if (flg[1].startsWith("l id_") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("fl", 2);
                        formats.put("id_field", 9);
                        formats.put("wst_id", 9);       // P.S. id do not match with the master list "wth_id"; might have another id name
                        formats.put("flsl", 6);
                        formats.put("flob", 6);
                        formats.put("fl_drntype", 6);
                        formats.put("fldrd", 6);
                        formats.put("fldrs", 6);
                        formats.put("flst", 6);
                        formats.put("sltx", 6);
                        formats.put("sldp", 6);
                        formats.put("soil_id", 11);
                        formats.put("fl_name", line.length());
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        String sltx = MapUtil.getValueOr(tmp, "sltx", "");
                        if (!sltx.isEmpty()) {
                            tmp.put("sltx", transSltx(sltx));
                        }
                        addToArray(flArr, tmp, "fl");
                        // Read weather station id
                        wid = (String) tmp.get("wst_id");
                        if (wid != null) {
                            tmp.put("dssat_wst_id", wid);
                            if (wid.matches("\\w{4}\\d{4}$")) {
                                wid = wid.substring(0, 4);
                                tmp.put("wst_id", wid);
                            }
                        }

                    }// // Read field info 2nd line
                    else if (flg[1].startsWith("l ...") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("fl", 2);
                        formats.put("fl_long", 16);
                        formats.put("fl_lat", 16);
                        formats.put("flele", 10);
                        formats.put("farea", 18);
                        formats.put("", 6);             // P.S. id do not find in the master list (? it seems to be calculated by other fields)
                        formats.put("fllwr", 6);
                        formats.put("flsla", 6);
                        formats.put("flhst", 6);        // P.S. id do not find in the master list (field histoy code)
                        formats.put("fhdur", 6);        // P.S. id do not find in the master list (duration associated with field history code in years)
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);

                        // Read lat and long
                        String strLat = (String) tmp.get("fl_lat");
                        String strLong = (String) tmp.get("fl_long");

                        // If lat or long is not valid data, read data from weather file
                        if (!checkValidValue(strLat) || !checkValidValue(strLong) || (Double.parseDouble(strLat) == 0 && Double.parseDouble(strLong) == 0)) {

                            // check if weather is validable
                            for (Object key : mapW.keySet()) {
                                if (((String) key).contains(wid)) {
                                    bufW = (char[]) mapW.get(key);
                                    break;
                                }
                            }
                            if (bufW != null) {
                                brw = new BufferedReader(new CharArrayReader(bufW));
                                String lineW;
                                while ((lineW = brw.readLine()) != null) {
                                    if (lineW.startsWith("@ INSI")) {
                                        lineW = brw.readLine();
                                        strLat = lineW.substring(6, 15).trim();
                                        strLong = lineW.substring(15, 24).trim();
                                        break;
                                    }
                                }

                                // check if lat and long are valid in the weather file; if not, set invalid value for them
                                if (!checkValidValue(strLat) || !checkValidValue(strLong) || (Double.parseDouble(strLat) == 0 && Double.parseDouble(strLong) == 0)) {
                                    strLat = null;
                                    strLong = null;
                                }
                            } // if weather file is not avaliable to read, set invalid value for lat and long
                            else {
                                strLat = null;
                                strLong = null;
                            }
                        }

                        if (strLat != null) {
                            tmp.put("fl_lat", strLat);
                        }
                        if (strLong != null) {
                            tmp.put("fl_long", strLong);
                        }
                        addToArray(flArr, tmp, "fl");
                    }

                } // Read SOIL ANALYSIS Section
                else if (flg[0].startsWith("soil")) {

                    // Read SOIL ANALYSIS global data
                    if (flg[1].startsWith("a sadat") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("sa", 2);
                        formats.put("sadat", 6);
                        formats.put("smhb", 6);     // P.S. changed from samhb to smhb to match the soil variable name
                        formats.put("smpx", 6);     // P.S. changed from sampx to smpx to match the soil variable name
                        formats.put("smke", 6);     // P.S. changed from samke to smke to match the soil variable name
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        translateDateStr(tmp, "sadat");
                        saArr.add(tmp);
                        sadArr = new ArrayList<HashMap>();
                        tmp.put(icEventKey, sadArr);

                    } // Read SOIL ANALYSIS layer data
                    else if (flg[1].startsWith("a  sabl") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("", 2);         // P.S. ignore the data index "sa"
                        formats.put("sllb", 6);     // P.S. changed from sabl  to sllb to match the soil variable name
                        formats.put("slbdm", 6);    // P.S. changed from sabdm to slbdm to match the soil variable name
                        formats.put("sloc", 6);     // P.S. changed from saoc  to sloc to match the soil variable name
                        formats.put("slni", 6);     // P.S. changed from sani  to slni to match the soil variable name
                        formats.put("slphw", 6);    // P.S. changed from saphw to slphw to match the soil variable name
                        formats.put("slphb", 6);    // P.S. changed from saphb to slphb to match the soil variable name
                        formats.put("slpx", 6);     // P.S. changed from sapx  to slpx to match the soil variable name
                        formats.put("slke", 6);     // P.S. changed from sake  to slke to match the soil variable name
                        formats.put("slsc", 6);     // P.S. changed from sasc  to slsc to match the soil variable name
                        // Read line and save into return holder
                        sadArr.add(readLine(line, formats));
                    } else {
                    }

                } // Read INITIAL CONDITIONS Section
                else if (flg[0].startsWith("initial")) {

                    // Read INITIAL CONDITIONS global data
                    if (flg[1].startsWith("c   pcr") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("ic", 2);
                        formats.put("icpcr", 6);
                        formats.put("icdat", 6);
                        formats.put("icrt", 6);
                        formats.put("icnd", 6);
                        formats.put("icrz#", 6);
                        formats.put("icrze", 6);
                        formats.put("icwt", 6);
                        formats.put("icrag", 6);
                        formats.put("icrn", 6);
                        formats.put("icrp", 6);
                        formats.put("icrip", 6);
                        formats.put("icrdp", 6);
                        formats.put("ic_name", line.length());
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        translateDateStr(tmp, "icdat");
                        Object icpcr = tmp.get("icpcr");
                        if (icpcr != null) {
                            tmp.put("icpcr", DssatCRIDHelper.get3BitCrid((String) icpcr));
                        }
                        icArr.add(tmp);
                        icdArr = new ArrayList<HashMap>();
                        tmp.put(icEventKey, icdArr);

                    } else // INITIAL CONDITIONS layer data
                    if (flg[1].startsWith("c  icbl") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("", 2);         // P.S. ignore the detail (event) data index "ic"
                        formats.put("icbl", 6);
                        formats.put("ich2o", 6);
                        formats.put("icnh4", 6);
                        formats.put("icno3", 6);
                        // Read line and save into return holder
                        icdArr.add(readLine(line, formats));

                    } else {
                    }

                } // Read PLANTING DETAILS Section
                else if (flg[0].startsWith("planting")) {

                    // Read PLANTING data
                    if (flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("pl", 2);
                        formats.put("pdate", 6);
                        formats.put("edate", 6);
                        formats.put("plpop", 6);
                        formats.put("plpoe", 6);
                        formats.put("plma", 6);     // P.S. 2012.07.13 changed from plme to plma
                        formats.put("plds", 6);
                        formats.put("plrs", 6);
                        formats.put("plrd", 6);
                        formats.put("pldp", 6);
                        formats.put("plmwt", 6);
                        formats.put("page", 6);
                        formats.put("plenv", 6);
                        formats.put("plph", 6);
                        formats.put("plspl", 6);
                        formats.put("pl_name", line.length());
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        translateDateStr(tmp, "pdate");
                        translateDateStr(tmp, "edate");
                        // cm -> mm
                        String pldp = getObjectOr(tmp, "pldp", "");
                        if (!pldp.isEmpty()) {
                            try {
                                BigDecimal pldpBD = new BigDecimal(pldp);
                                pldpBD = pldpBD.multiply(new BigDecimal("10"));
                                tmp.put("pldp", pldpBD.toString());
                            } catch (NumberFormatException e) {
                            }
                        }
                        plArr.add(tmp);
                    } else {
                    }

                } // Read IRRIGATION AND WATER MANAGEMENT Section
                else if (flg[0].startsWith("irrigation")) {

                    // Read IRRIGATION global data
                    if ((flg[1].startsWith("i  efir") || flg4 % 2 == 1) && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("ir", 2);
                        formats.put("ireff", 6);
                        formats.put("irmdp", 6);
                        formats.put("irthr", 6);
                        formats.put("irept", 6);
                        formats.put("irstg", 6);
                        formats.put("iame", 6);
                        formats.put("iamt", 6);
                        formats.put("ir_name", line.length());
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        ireff = (String) tmp.get("ireff");
                        if (ireff != null) {
                            tmp.remove("ireff");
                        }
                        irArr.add(tmp);
                        irdArr = new ArrayList<HashMap>();
                        tmp.put(eventKey, irdArr);

                    } // Read IRRIGATION appliction data
                    else if (flg[1].startsWith("i idate") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("", 2);         // P.S. ignore the data index "ir"
                        formats.put("idate", 6);
                        formats.put("irop", 6);
                        formats.put("irval", 6);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        //                    tmp.put("idate", translateDateStr((String) tmp.getOr("idate"))); // P.S. DOY handling
                        tmp.put("ireff", ireff);
                        irdArr.add(tmp);

                    } else {
                    }

                } // Read FERTILIZERS (INORGANIC) Section
                else if (flg[0].startsWith("fertilizers")) {

                    // Read FERTILIZERS data
                    if (flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("fe", 2);
                        formats.put("fdate", 6);
                        formats.put("fecd", 6);
                        formats.put("feacd", 6);
                        formats.put("fedep", 6);
                        formats.put("feamn", 6);
                        formats.put("feamp", 6);
                        formats.put("feamk", 6);
                        formats.put("feamc", 6);
                        formats.put("feamo", 6);
                        formats.put("feocd", 6);
                        formats.put("fe_name", line.length());
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        //                    translateDateStr(tmp, "fdate"); // P.S. DOY handling
                        feArr.add(tmp);
                    } else {
                    }

                } // Read RESIDUES AND OTHER ORGANIC MATERIALS Section
                else if (flg[0].startsWith("residues")) {

                    // Read ORGANIC MATERIALS data
                    if (flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("om", 2);
                        formats.put("omdat", 6);    // P.S. id do not match with the master list "omday"
                        formats.put("omcd", 6);
                        formats.put("omamt", 6);
                        formats.put("omn%", 6);
                        formats.put("omp%", 6);
                        formats.put("omk%", 6);
                        formats.put("ominp", 6);
                        formats.put("omdep", 6);
                        formats.put("omacd", 6);
                        formats.put("om_name", line.length());
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        //                    translateDateStr(tmp, "omdat"); // P.S. DOY handling
                        omArr.add(tmp);
                    } else {
                    }

                } // Read CHEMICAL APPLICATIONS Section
                else if (flg[0].startsWith("chemical")) {

                    // Read CHEMICAL APPLICATIONS data
                    if (flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("ch", 2);
                        formats.put("cdate", 6);
                        formats.put("chcd", 6);
                        formats.put("chamt", 6);
                        formats.put("chacd", 6);
                        formats.put("chdep", 6);
                        formats.put("ch_targets", 6);
                        formats.put("ch_name", line.length());
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        translateDateStr(tmp, "cdate");
                        chArr.add(tmp);
                    } else {
                    }

                } // Read TILLAGE Section
                else if (flg[0].startsWith("tillage")) {

                    // Read TILLAGE data
                    if (flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("ti", 2);
                        formats.put("tdate", 6);
                        formats.put("tiimp", 6);
                        formats.put("tidep", 6);
                        formats.put("ti_name", line.length());
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        translateDateStr(tmp, "tdate");
                        tiArr.add(tmp);
                    } else {
                    }

                } // Read ENVIRONMENT MODIFICATIONS Section
                else if (flg[0].startsWith("environment")) {

                    // Read ENVIRONMENT MODIFICATIONS data
                    if (flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("em", 2);
                        //                    formats.put("emday", 6);
                        //                    formats.put("ecdyl", 2);
                        //                    formats.put("emdyl", 4);
                        //                    formats.put("ecrad", 2);
                        //                    formats.put("emrad", 4);
                        //                    formats.put("ecmax", 2);
                        //                    formats.put("emmax", 4);
                        //                    formats.put("ecmin", 2);
                        //                    formats.put("emmin", 4);
                        //                    formats.put("ecrai", 2);
                        //                    formats.put("emrai", 4);
                        //                    formats.put("ecco2", 2);
                        //                    formats.put("emco2", 4);
                        //                    formats.put("ecdew", 2);
                        //                    formats.put("emdew", 4);
                        //                    formats.put("ecwnd", 2);
                        //                    formats.put("emwnd", 4);
                        //                    formats.put("em_name", line.length());
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        //                    translateDateStr(tmp, "emday");
                        tmp.put("em_data", line.substring(2));
                        emArr.add(tmp);
                    } else {
                    }

                } // Read HARVEST DETAILS Section
                else if (flg[0].startsWith("harvest")) {

                    // Read HARVEST data
                    if (flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("ha", 2);
                        formats.put("hadat", 6);
                        formats.put("hastg", 6);
                        formats.put("hacom", 6);
                        formats.put("hasiz", 6);
                        formats.put("hap%", 6);
                        formats.put("hab%", 6);
                        formats.put("ha_name", line.length());
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        translateDateStr(tmp, "hadat");
                        haArr.add(tmp);
                    } else {
                    }

                } // Read SIMULATION CONTROLS Section // P.S. no need to be divided
                else if (flg[0].startsWith("simulation")) {

                    // Read general info
                    if (flg[1].startsWith("n general") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("sm", 2);
//                        formats.put("sm_general", line.length());
                        formats.put("null", 12);
                        formats.put("nyers", 6);
                        formats.put("nreps", 6);
                        formats.put("start", 6);
                        formats.put("sdyer", 3);
                        formats.put("sdday", 3);
                        formats.put("rseed", 6);
                        formats.put("sname", 26);
                        formats.put("model", line.length());
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        HashMap smData = new HashMap();
                        Object sm = tmp.remove("sm");
                        smData.put("sm", sm);
                        smData.put("general", tmp);
                        addToArray(smArr, smData, "sm");

                    } // Read options info
                    else if (flg[1].startsWith("n options") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("sm", 2);
//                        formats.put("sm_options", line.length());
                        formats.put("null", 12);
                        formats.put("water", 6);
                        formats.put("nitro", 6);
                        formats.put("symbi", 6);
                        formats.put("phosp", 6);
                        formats.put("potas", 6);
                        formats.put("dises", 6);
                        formats.put("chem", 6);
                        formats.put("till", 6);
                        formats.put("co2", 6);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        HashMap smData = new HashMap();
                        Object sm = tmp.remove("sm");
                        smData.put("sm", sm);
                        smData.put("options", tmp);
                        addToArray(smArr, smData, "sm");

                    } // Read methods info
                    else if (flg[1].startsWith("n methods") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("sm", 2);
//                        formats.put("sm_methods", line.length());
                        formats.put("null", 12);
                        formats.put("wther", 6);
                        formats.put("incon", 6);
                        formats.put("light", 6);
                        formats.put("evapo", 6);
                        formats.put("infil", 6);
                        formats.put("photo", 6);
                        formats.put("hydro", 6);
                        formats.put("nswit", 6);
                        formats.put("mesom", 6);
                        formats.put("mesev", 6);
                        formats.put("mesol", 6);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        HashMap smData = new HashMap();
                        Object sm = tmp.remove("sm");
                        smData.put("sm", sm);
                        smData.put("methods", tmp);
                        addToArray(smArr, smData, "sm");

                    } // Read management info
                    else if (flg[1].startsWith("n management") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("sm", 2);
//                        formats.put("sm_management", line.length());
                        formats.put("null", 12);
                        formats.put("plant", 6);
                        formats.put("irrig", 6);
                        formats.put("ferti", 6);
                        formats.put("resid", 6);
                        formats.put("harvs", 6);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        HashMap smData = new HashMap();
                        Object sm = tmp.remove("sm");
                        smData.put("sm", sm);
                        smData.put("management", tmp);
                        addToArray(smArr, smData, "sm");

                    } // Read outputs info
                    else if (flg[1].startsWith("n outputs") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("sm", 2);
//                        formats.put("sm_outputs", line.length());
                        formats.put("null", 12);
                        formats.put("fname", 6);
                        formats.put("ovvew", 6);
                        formats.put("sumry", 6);
                        formats.put("fropt", 6);
                        formats.put("grout", 6);
                        formats.put("caout", 6);
                        formats.put("waout", 6);
                        formats.put("niout", 6);
                        formats.put("miout", 6);
                        formats.put("diout", 6);
                        formats.put("vbose", 6);
                        formats.put("chout", 6);
                        formats.put("opout", 6);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        HashMap smData = new HashMap();
                        Object sm = tmp.remove("sm");
                        smData.put("sm", sm);
                        smData.put("outputs", tmp);
                        addToArray(smArr, smData, "sm");

                    } // Read planting info
                    else if (flg[1].startsWith("n planting") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("sm", 2);
//                        formats.put("sm_planting", line.length());
                        formats.put("null", 12);
                        formats.put("pfyer", 3);
                        formats.put("pfday", 3);
                        formats.put("plyer", 3);
                        formats.put("plday", 3);
                        formats.put("ph2ol", 6);
                        formats.put("ph2ou", 6);
                        formats.put("ph2od", 6);
                        formats.put("pstmx", 6);
                        formats.put("pstmn", 6);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        HashMap smData = new HashMap();
                        Object sm = tmp.remove("sm");
                        smData.put("sm", sm);
                        smData.put("planting", tmp);
                        addToArray(smArr, smData, "sm");

                    } // Read irrigation info
                    else if (flg[1].startsWith("n irrigation") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("sm", 2);
//                        formats.put("sm_irrigation", line.length());
                        formats.put("null", 12);
                        formats.put("imdep", 6);
                        formats.put("ithrl", 6);
                        formats.put("ithru", 6);
                        formats.put("iroff", 6);
                        formats.put("imeth", 6);
                        formats.put("iramt", 6);
                        formats.put("ireff", 6);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        HashMap smData = new HashMap();
                        Object sm = tmp.remove("sm");
                        smData.put("sm", sm);
                        smData.put("irrigation", tmp);
                        addToArray(smArr, smData, "sm");

                    } // Read nitrogen info
                    else if (flg[1].startsWith("n nitrogen") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("sm", 2);
//                        formats.put("sm_nitrogen", line.length());
                        formats.put("null", 12);
                        formats.put("nmdep", 6);
                        formats.put("nmthr", 6);
                        formats.put("namnt", 6);
                        formats.put("ncode", 6);
                        formats.put("naoff", 6);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        HashMap smData = new HashMap();
                        Object sm = tmp.remove("sm");
                        smData.put("sm", sm);
                        smData.put("nitrogen", tmp);
                        addToArray(smArr, smData, "sm");

                    } // Read residues info
                    else if (flg[1].startsWith("n residues") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("sm", 2);
//                        formats.put("sm_residues", line.length());
                        formats.put("null", 12);
                        formats.put("ripcn", 6);
                        formats.put("rtime", 6);
                        formats.put("ridep", 6);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        HashMap smData = new HashMap();
                        Object sm = tmp.remove("sm");
                        smData.put("sm", sm);
                        smData.put("residues", tmp);
                        addToArray(smArr, smData, "sm");

                    } // Read harvest info
                    else if (flg[1].startsWith("n harvest") && flg[2].equals("data")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("sm", 2);
//                        formats.put("sm_harvests", line.length());
                        formats.put("null", 12);
                        formats.put("hfrst", 6);    // P.S. Keep the original value
                        formats.put("hlyer", 3);
                        formats.put("hlday", 3);
                        formats.put("hpcnp", 6);
                        formats.put("hpcnr", 6);
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        HashMap smData = new HashMap();
                        Object sm = tmp.remove("sm");
                        smData.put("sm", sm);
                        smData.put("harvests", tmp);
                        addToArray(smArr, smData, "sm");

                    } else {
                    }

                } else {
                }
            }

            br.close();
            if (brw != null) {
                brw.close();
            }

            // Combine all the sections data into the related treatment block
            String trno = null;
            HashMap dssatSq;
//        HashMap dssatInfo = new HashMap();
            ArrayList<HashMap> sqArrNew = new ArrayList<HashMap>();
            for (int i = 0, seqid = 1; i < sqArr.size(); i++, seqid++) {
                sqData = (HashMap) sqArr.get(i);

                trMetaData = new HashMap();
                trMetaArr.add(trMetaData);

                if (!sqData.get("trno").equals(trno)) {
                    trno = sqData.get("trno").toString();
                    trData = new HashMap();
                    evtArr = new ArrayList<HashMap>();
                    trArr.add(trData);
                    trData.put("events", evtArr);

                    dssatSq = new HashMap();
                    sqArrNew = new ArrayList<HashMap>();
//                dssatInfo = new HashMap();
                    dssatSq.put(eventKey, sqArrNew);
                    trData.put("dssat_sequence", dssatSq);
//                trData.put("dssat_info", dssatInfo);
                    trMetaData.put("trt_name", sqData.get("trt_name"));
                    trMetaData.put("trno", trno);
                    trMetaData.put("exname", exname);
                    seqid = 1;
                } else {
                    // The case of multiple sequence
                    trMetaData.remove("trt_name");
                }
                sqData.put("seqid", seqid + "");
                sqArrNew.add(sqData);

                // cultivar
                HashMap crData = new HashMap();
                if (!getObjectOr(sqData, "ge", "0").equals("0")) {
                    // Get related cultivar data
                    crData = (HashMap) getSectionDataObj(cuArr, "ge", sqData.get("ge").toString());
                }

                // field
                if (!getObjectOr(sqData, "fl", "0").equals("0")) {
                    // Move field info into meta data block
                    trMetaData.putAll((HashMap) getSectionDataObj(flArr, "fl", sqData.get("fl").toString()));
                    trMetaData.remove("fl");
                }

                // initial_condition
                if (!getObjectOr(sqData, "ic", "0").equals("0")) {
                    HashMap icTmpArr = (HashMap) getSectionDataObj(icArr, "ic", sqData.get("ic").toString());
                    if (!icTmpArr.isEmpty()) {
                        trData.put("initial_conditions", icTmpArr);
                    }
                }

                // planting
                String pdate = "";
                boolean isFallow = false;
                if (!getObjectOr(sqData, "pl", "0").equals("0")) {
                    // add event data into array
                    addEvent(evtArr, (HashMap) getSectionDataObj(plArr, "pl", sqData.get("pl").toString()), "pdate", "planting", seqid);

                    // add cultivar data into planting event
                    if (crData != null) {
                        evtArr.get(evtArr.size() - 1).putAll(crData);
                    }
                    // Get planting date for DOY value handling
                    pdate = getValueOr(evtArr.get(evtArr.size() - 1), "date", "");
//                    if (pdate.length() > 5) {
//                        pdate = pdate.substring(2);
//                    }
                } else {
                    isFallow = true;
                }

                // simulation
                HashMap<String, String> smManagement = new HashMap();
                if (!getObjectOr(sqData, "sm", "0").equals("0")) {
                    String sm = (String) sqData.get("sm");
                    HashMap smData = (HashMap) getSectionDataObj(smArr, "sm", sm);
                    // Set SDAT and EXP_DUR
                    HashMap smGeneral = getObjectOr(smData, "general", new HashMap());
                    String sdyer = getValueOr(smGeneral, "sdyer", "");
                    String sdday = getValueOr(smGeneral, "sdday", "");
                    String sdat = translateDateStr(sdyer + sdday);
                    if (!sdat.equals("")) {
                        trMetaData.put("sdat", sdat);
                    }
                    String expDur = getValueOr(smGeneral, "nyers", "");
                    if (!expDur.equals("")) {
                        trMetaData.put("exp_dur", expDur);
                    }
                    // Get simulation control management section info
                    smManagement = getObjectOr(smData, "management", new HashMap());
                    HashMap tmp = getObjectOr(trData, "dssat_simulation_control", new HashMap());
                    ArrayList<HashMap> arr = getObjectOr(tmp, eventKey, new ArrayList());
                    boolean isExistFlg = false;
                    for (HashMap data : arr) {
                        if (sm.equals(data.get("sm"))) {
                            isExistFlg = true;
                            break;
                        }
                    }
                    if (!isExistFlg) {
                        arr.add(smData);
                    }
                    tmp.put(eventKey, arr);
                    trData.put("dssat_simulation_control", tmp);
                }

                // irrigation
                if (!getObjectOr(sqData, "ir", "0").equals("0")) {
                    // Date adjust based on realted treatment info (handling for DOY type value)
                    HashMap irTmp = (HashMap) getSectionDataObj(irArr, "ir", sqData.get("ir").toString());
                    ArrayList<HashMap> irTmpSubs = getObjectOr(irTmp, "data", new ArrayList());
                    for (HashMap irTmpSub : irTmpSubs) {
                        translateDateStrForDOY(irTmpSub, "idate", pdate, smManagement.get("irrig"));
                    }

                    // add event data into array
                    addEvent(evtArr, irTmp, "idate", "irrigation", seqid);
                }

                // fertilizer
                if (!getObjectOr(sqData, "fe", "0").equals("0")) {

                    ArrayList<HashMap> feTmps = (ArrayList) getSectionDataObj(feArr, "fe", sqData.get("fe").toString());
                    for (HashMap feTmp : feTmps) {
                        // Date adjust based on realted treatment info (handling for DOY type value)
                        translateDateStrForDOY(feTmp, "fdate", pdate, smManagement.get("ferti"));

                        // add event data into array
                        addEvent(evtArr, feTmp, "fdate", "fertilizer", seqid);
                    }
                }

                // organic_matter
                if (!getObjectOr(sqData, "om", "0").equals("0")) {
                    ArrayList<HashMap> omTmps = (ArrayList) getSectionDataObj(omArr, "om", sqData.get("om").toString());
                    for (HashMap omTmp : omTmps) {
                        // Date adjust based on realted treatment info (handling for DOY type value)
                        translateDateStrForDOY(omTmp, "omdat", pdate, smManagement.get("resid"));
                        // add event data into array
                        addEvent(evtArr, omTmp, "omdat", "organic_matter", seqid); // P.S. change event name to organic-materials; Back to organic_matter again
                    }
                }

                // chemical
                if (!getObjectOr(sqData, "ch", "0").equals("0")) {
                    ArrayList<HashMap> chTmps = (ArrayList) getSectionDataObj(chArr, "ch", sqData.get("ch").toString());
                    for (HashMap chTmp : chTmps) {
                        // add event data into array
                        addEvent(evtArr, chTmp, "cdate", "chemical", seqid);
                    }
                }

                // tillage
                if (!getObjectOr(sqData, "ti", "0").equals("0")) {
                    ArrayList<HashMap> tiTmps = (ArrayList) getSectionDataObj(tiArr, "ti", sqData.get("ti").toString());
                    for (HashMap tiTmp : tiTmps) {
                        // add event data into array
                        addEvent(evtArr, tiTmp, "tdate", "tillage", seqid);
                    }
                }

                // emvironment  // P.S. keep for furture using
                if (!getObjectOr(sqData, "em", "0").equals("0")) {
                    String em = (String) sqData.get("em");
                    ArrayList<HashMap> emDataArr = (ArrayList) getSectionDataObj(emArr, "em", em);
//                ArrayList arr = new ArrayList();
//                for (int j = 0; j < emDataArr.size(); j++) {
//                    arr.add(emDataArr.get(j).get("em_data"));
//                }
//                sqData.put("em_data", arr);

                    HashMap tmp = getObjectOr(trData, "dssat_environment_modification", new HashMap());
                    ArrayList<HashMap> arr = getObjectOr(tmp, eventKey, new ArrayList());
                    boolean isExistFlg = false;
                    for (HashMap data : arr) {
                        if (em.equals(data.get("em"))) {
                            isExistFlg = true;
                            break;
                        }
                    }
                    if (!isExistFlg) {
                        arr.addAll(emDataArr);
                    }
                    tmp.put(eventKey, arr);
                    trData.put("dssat_environment_modification", tmp);
                }

                // harvest
                if (!getObjectOr(sqData, "ha", "0").equals("0")) {
                    // add event data into array
                    HashMap haData = (HashMap) getSectionDataObj(haArr, "ha", sqData.get("ha").toString());
                    addEvent(evtArr, haData, "hadat", "harvest", seqid);
                    copyItem(trMetaData, haData, "endat", "hadat", false);
                }

                // soil_analysis
                if (!getObjectOr(sqData, "sa", "0").equals("0")) {
                    HashMap saTmp = (HashMap) getSectionDataObj(saArr, "sa", sqData.get("sa").toString());
//                ArrayList<HashMap> saSubArr = getObjectOr(saTmp, icEventKey, new ArrayList());

                    // temporally put soil_analysis block into treatment meta data
                    trMetaData.put("soil_analysis", saTmp);

//                // Add SASC into initial condition block
//                ArrayList<HashMap> icSubArrNew;
//                HashMap icTmp;
//                String[] copyKeys = {"slsc"};
//                if (!trData.containsKey("initial_condition")) {
//                    // Add a dummy ic block to hold SASC
//                    icTmp = new HashMap();
//                    trData.put("initial_condition", icTmp);
//                    icSubArrNew = combinLayers(new ArrayList<HashMap>(), saSubArr, "icbl", "sllb", copyKeys);
//                } else {
//                    // Add SASC into initial condition block
//                    icTmp = (HashMap) trData.get("initial_condition");
//                    ArrayList<HashMap> icSubArr = getObjectOr(icTmp, icEventKey, new ArrayList());
//                    icSubArrNew = combinLayers(icSubArr, saSubArr, "icbl", "sllb", copyKeys);
//                }
//                icTmp.put(icEventKey, icSubArrNew);
                }

            }
        }

        // Remove relational index
        ArrayList idNames = new ArrayList();
        idNames.add("ge");
        idNames.add("fl");
        idNames.add("sa");
        idNames.add("ic");
        idNames.add("pl");
        idNames.add("ir");
        idNames.add("fe");
        idNames.add("om");
        idNames.add("ch");
        idNames.add("ti");
//        idNames.add("em");
        idNames.add("ha");
//        idNames.add("sm");
        removeIndex(trArr, idNames);
        removeIndex(metaData, idNames);

        return trArr;
    }

    /**
     * Set reading flags for title lines (marked with *)
     *
     * @param line the string of reading line
     */
    @Override
    protected void setTitleFlgs(String line) {
        flg[0] = line.substring(1).trim().toLowerCase();
        flg[1] = "";
        flg[2] = "";
    }

    /**
     * Get the section data by given index value and key
     *
     * @param secArr Section data array
     * @param key index variable name
     * @param value index variable value
     */
    private Object getSectionDataObj(ArrayList secArr, Object key, String value) {

        ArrayList ret = new ArrayList();
        // Define the section with single sub data
        ArrayList singleSubRecSecList = new ArrayList();
        singleSubRecSecList.add("ge");
        singleSubRecSecList.add("fl");
        singleSubRecSecList.add("pl");
        singleSubRecSecList.add("ha");
        singleSubRecSecList.add("sm");
        // Get First data node
        if (secArr.isEmpty() || value == null) {
            if (key.equals("icbl") || key.equals("ir") || key.equals("ic") || key.equals("sa") || singleSubRecSecList.contains(key)) {
                return new HashMap();
            } else {
                return ret;
            }
        }

        if (key.equals("icbl")) {
            return getSectionDataWithNocopy(secArr, key, value);
        } else {
            HashMap fstNode = (HashMap) secArr.get(0);
            // If it contains multiple sub array of data, or it does not have multiple sub records
            if (fstNode.containsKey(eventKey) || fstNode.containsKey(icEventKey) || singleSubRecSecList.contains(key)) {
                for (Object secData : secArr) {
                    if (value.equals(((HashMap) secData).get(key))) {
                        return CopyList((HashMap) secData);
                    }
                }

            } // If it is simple array
            else {
                HashMap node;
                for (Object secData : secArr) {
                    node = (HashMap) secData;
                    if (value.equals(node.get(key))) {
                        ret.add(CopyList(node));
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Add event data into event array from input data holder (map)
     *
     * @param events event array
     * @param mCopy input map
     * @param dateId the key name of event date variable
     * @param eventName the event name
     * @param seqId the sequence id for DSSAT
     */
    private void addEvent(ArrayList events, HashMap m, String dateId, String eventName, int seqId) {

        HashMap<String, String> ret = new HashMap<String, String>();
        HashMap mCopy = CopyList(m);
        if (mCopy.containsKey(dateId)) {
            ret.put("date", mCopy.remove(dateId).toString());
        }
        ret.put("event", eventName);
        if (mCopy.containsKey(eventKey)) {
            ArrayList<HashMap> subArr = (ArrayList) mCopy.remove(eventKey);

            if (subArr == null || subArr.isEmpty()) {
                ret.putAll(mCopy);
                events.add(ret);
            }

            for (HashMap tmp : subArr) {
                ret.put("date", tmp.remove(dateId).toString());
                ret.putAll(mCopy);
                ret.putAll(tmp);
                ret.put("seqid", seqId + "");
                events.add(CopyList(ret));
            }
        } else {
            ret.putAll(mCopy);
            ret.put("seqid", seqId + "");
            events.add(ret);
        }
    }

    /**
     * Setup the meta data block for the treatment
     *
     * @param metaData meta data holder
     * @param trId treatment array index for the current treatment
     *
     * @return expData experiment data holder (contain all blocks)
     */
    public HashMap setupMetaData(HashMap metaData, int trId) {

        // Set meta data for all treatment
        HashMap expData = new HashMap();
        // Set meta data per treatment
        ArrayList<HashMap> trMetaArr = (ArrayList<HashMap>) metaData.get("tr_meta");
        String exname = getValueOr(trMetaArr.get(trId), "exname", "");
        expData.putAll(getObjectOr(metaData, exname, new HashMap()));
        expData.putAll(trMetaArr.get(trId));
        if (!exname.equals("")) {
            expData.put("exname", exname + "_" + expData.get("trno"));
        }
        expData.put("exname_o", exname);

        return expData;
    }

    /**
     * Setup the treatment data, includes management, initial condition and
     * Dssat specific data block (for the case which only read XFile)
     *
     * @param expData experiment data holder (contain all blocks)
     * @param mgnData management data holder (contain events array)
     */
    private void setupTrnData(HashMap expData, HashMap mgnData) {
        setupTrnData(expData, mgnData, new HashMap(), new HashMap());
    }

    /**
     * Setup the treatment data, includes management, initial condition and
     * Dssat specific data block
     *
     * @param expData experiment data holder (contain all blocks)
     * @param mgnData management data holder (contain events array)
     * @param obvAFile summary observed data holder
     * @param obvTFile time series observed data holder
     */
    public void setupTrnData(HashMap expData, HashMap mgnData, HashMap obvAFile, HashMap obvTFile) {

        // remove unused variable
        expData.remove("sltx");
        expData.remove("sldp");
        // Set management data block for this treatment
        expData.put(jsonKey, mgnData);

        // Set Initial Condition data block for this treatment
        copyItem(expData, mgnData, "initial_conditions", true);

        // Set DSSAT specific data blocks
        // dssat_sequence
        copyItem(expData, mgnData, "dssat_sequence", true);
        // dssat_environment_modification
        copyItem(expData, mgnData, "dssat_environment_modification", true);
        // dssat_simulation_control
        copyItem(expData, mgnData, "dssat_simulation_control", true);
        // dssat_info
//        cutDataBlock(mgnData, expData, "dssat_info");
        // Create dssat info holder
        HashMap dssatInfo = new HashMap();
        // Move field history code into dssat_info block
        copyItem(dssatInfo, expData, "flhst", true);
        copyItem(dssatInfo, expData, "fhdur", true);
        // Set dssat_info if it is available
        if (!dssatInfo.isEmpty()) {
            expData.put("dssat_info", dssatInfo);
        }

        // remove index variables
        ArrayList idNames = new ArrayList();
        idNames.add("trno");
        idNames.add("trno_a");
        idNames.add("trno_t");
        removeIndex(expData, idNames);
    }
}
