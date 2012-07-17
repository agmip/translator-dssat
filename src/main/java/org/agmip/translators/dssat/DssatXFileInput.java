package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import static org.agmip.util.MapUtil.*;

/**
 * DSSAT Experiment Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatXFileInput extends DssatCommonInput {

    private String eventKey = "data";
    private String icEventKey = "soilLayer";
    
    /**
     * Constructor with no parameters
     * Set jsonKey as "experiment"
     * 
     */
    public DssatXFileInput() {
        super();
        jsonKey = "management";
    }

    /**
     * DSSAT XFile Data input method for only inputing experiment file
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    @Override
    protected LinkedHashMap readFile(HashMap brMap) throws IOException {

        // TODO need to be revised later
        LinkedHashMap ret = new LinkedHashMap();
        ArrayList<LinkedHashMap> trArr = readTreatments(brMap, ret);
//        compressData(trArr);
        ret.put(jsonKey, trArr);
        return ret;
    }

    /**
     * DSSAT XFile Data input method for Controller using (no compressing data)
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return trArr The array of treatment data
     */
    protected ArrayList<LinkedHashMap> readTreatments(HashMap brMap, LinkedHashMap metaData) throws IOException {

        String line;
        BufferedReader br;
        Object buf;
        BufferedReader brw = null;
        char[] bufW = null;
        HashMap mapW;
        String wid;
        String fileName;
        LinkedHashMap formats = new LinkedHashMap();
        ArrayList<LinkedHashMap> trArr = new ArrayList<LinkedHashMap>();
        LinkedHashMap trData = new LinkedHashMap();
        ArrayList<LinkedHashMap> evtArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> sqArr = new ArrayList<LinkedHashMap>();
        LinkedHashMap sqData;
        ArrayList<LinkedHashMap> cuArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> flArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> saArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> sadArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> icArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> icdArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> plArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> irArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> irdArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> feArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> omArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> chArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> tiArr = new ArrayList<LinkedHashMap>();
//        ArrayList<LinkedHashMap> emArr = new ArrayList<LinkedHashMap>();    // TODO add in the future
        ArrayList<LinkedHashMap> haArr = new ArrayList<LinkedHashMap>();
        ArrayList<LinkedHashMap> smArr = new ArrayList<LinkedHashMap>();
        ArrayList smSubArr = new ArrayList();
        String ireff = "";    // P.S. special handling for EFIR in irrigation section
        DssatAFileInput obvReaderA = new DssatAFileInput();
        DssatTFileInput obvReaderT = new DssatTFileInput();
//        DssatSoilInput soilReader = new DssatSoilInput(); // TODO will be deleted

        buf = brMap.get("X");
        mapW = (HashMap) brMap.get("W");
        fileName = (String) brMap.get("Z");
        wid = fileName.length() > 4 ? fileName.substring(0, 4) : fileName;

        // If XFile is no been found
        if (buf == null) {
            // TODO reprot file not exist error
            return trArr;
        } else {
            if (buf instanceof char[]) {
                br = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                br = (BufferedReader) buf;
            }
        }

//        ret.put("cultivar", cuArr);
//        ret.put("field", flArr);
//        ret.put("soil_analysis", saArr);
//        ret.put("initial_condition", icArr);
//        ret.put("plant", plArr);
//        ret.put("irrigation", irArr);
//        ret.put("fertilizer", feArr);
//        ret.put("residue_organic", omArr);
//        ret.put("chemical", chArr);
//        ret.put("tillage", tiArr);
//        ret.put("emvironment", emArr);
//        ret.put("harvest", haArr);
//        ret.put("simulation", smArr);

        while ((line = br.readLine()) != null) {

            // Get content type of line
            judgeContentType(line);

            // Read Exp title info
            if (flg[0].startsWith("exp.details:") && flg[2].equals("")) {

                // Set variables' formats
                formats.clear();
                formats.put("exname", 11);
                formats.put("local_name", 61);
                // Read line and save into return holder
                metaData.putAll(readLine(line.substring(13), formats));
                metaData.put("institutes", line.substring(14, 16).trim());
            } // Read General Section
            else if (flg[0].startsWith("general")) {

                // People info
                if (flg[1].equals("people") && flg[2].equals("data")) {
                    metaData.put("people", line.trim());

                } // Address info
                else if (flg[1].equals("address") && flg[2].equals("data")) {
                    String[] addr = line.split(",[ ]*");
                    metaData.put("fl_loc_1", "");
                    metaData.put("fl_loc_2", "");
                    metaData.put("fl_loc_3", "");
                    metaData.put("institutes", line.trim());
//                    ret.put("address", line.trim());    // P.S. no longer to use this field

                    switch (addr.length) {
                        case 0:
                            break;
                        case 1:
                            metaData.put("fl_loc_1", addr[0]);
                            break;
                        case 2:
                            metaData.put("fl_loc_1", addr[1]);
                            metaData.put("fl_loc_2", addr[0]);
                            break;
                        case 3:
                            metaData.put("fl_loc_1", addr[2]);
                            metaData.put("fl_loc_2", addr[1]);
                            metaData.put("fl_loc_3", addr[0]);
                            break;
                        default:
                            metaData.put("fl_loc_1", addr[addr.length - 1]);
                            metaData.put("fl_loc_2", addr[addr.length - 2]);
                            String loc3 = "";
                            for (int i = 0; i < addr.length - 2; i++) {
                                loc3 += addr[i] + ", ";
                            }
                            metaData.put("fl_loc_3", loc3.substring(0, loc3.length() - 2));
                    }

                } // Site info
                else if ((flg[1].equals("site") || flg[1].equals("sites")) && flg[2].equals("data")) {
                    //$ret[$flg[0]]["site"] = trim($line);
                    // P.S. site is missing in the master variables list
                    metaData.put("site", line.trim());

                } // Plot Info
                else if (flg[1].startsWith("parea") && flg[2].equals("data")) {

                    // Set variables' formats
                    formats.clear();
                    formats.put("parea", 7);
                    formats.put("prno", 6);
                    formats.put("plen", 6);
                    formats.put("pldr", 6);
                    formats.put("plsp", 6);
                    formats.put("play", 6);
                    formats.put("pltha", 6);
                    formats.put("hrno", 6);
                    formats.put("hlen", 6);
                    formats.put("plthm", 16);
                    // Read line and save into return holder
                    metaData.putAll(readLine(line, formats));

                } // Notes field
                else if (flg[1].equals("tr_notes") && flg[2].equals("data")) {
                    if (!metaData.containsKey("tr_notes")) {
                        metaData.put("tr_notes", line.trim() + "\\r\\n");
                    } else {
                        String notes = (String) metaData.get("tr_notes");
                        notes += line.trim() + "\\r\\n";
                        metaData.put("tr_notes", notes);
                    }
                } else {
                }

            } // Read TREATMENTS Section
            else if (flg[0].startsWith("treatments")) {

                // Read TREATMENTS data / Rotation data
                if (flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("trno", 2);
                    formats.put("sq", 2);
                    formats.put("op", 2);
                    formats.put("co", 2);
                    formats.put("tr_name", 26);
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
                    sqArr.add(readLine(line, formats));
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
                    cuArr.add(readLine(line, formats));
//                    if (cuArr.size() == 1) {
//                        metaData.put("crid", line.substring(3, 5).trim()); // TODO keep for the early version; just first entry
//                    }
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
                    formats.put("wst_id", 9);       //P.S. id do not match with the master list "wth_id"; might have another id name
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
                    addToArray(flArr, readLine(line, formats), "fl");
                    // Read weather station id
                    wid = line.substring(12, 20).trim();

                }// // Read field info 2nd line
                else if (flg[1].startsWith("l ...") && flg[2].equals("data")) {

                    // Set variables' formats
                    formats.clear();
                    formats.put("fl", 2);
                    formats.put("fl_lat", 16);
                    formats.put("fl_long", 16);
                    formats.put("flele", 10);
                    formats.put("farea", 18);
                    formats.put("", 6);             // P.S. id do not find in the master list (? it seems to be calculated by other fields)
                    formats.put("fllwr", 6);
                    formats.put("flsla", 6);
                    formats.put("flhst", 6);        // P.S. id do not find in the master list (field histoy code)
                    formats.put("fhdur", 6);        // P.S. id do not find in the master list (duration associated with field history code in years)
                    // Read line and save into return holder
                    LinkedHashMap tmp = readLine(line, formats);

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
//                    if (flArr.size() == 1) {
//                        metaData.put("fl_lat", strLat); // TODO Keep the meta data handling for the early version
//                        metaData.put("fl_long", strLong); // TODO Keep the meta data handling for the early version
//                    }
                    if (tmp.containsKey("fl_lat")) {
                        tmp.put("fl_lat", strLat);
                    }
                    if (tmp.containsKey("fl_long")) {
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
                    formats.put("samhb", 6);
                    formats.put("sampx", 6);
                    formats.put("samke", 6);
                    // Read line and save into return holder
                    LinkedHashMap tmp = readLine(line, formats);
                    translateDateStr(tmp, "sadat");
                    saArr.add(tmp);
                    sadArr = new ArrayList<LinkedHashMap>();
                    tmp.put(eventKey, sadArr);

                } // Read SOIL ANALYSIS layer data
                else if (flg[1].startsWith("a  sabl") && flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("", 2);         // P.S. ignore the data index "sa"
                    formats.put("sabl", 6);
                    formats.put("sabdm", 6);
                    formats.put("saoc", 6);
                    formats.put("sani", 6);
                    formats.put("saphw", 6);
                    formats.put("saphb", 6);
                    formats.put("sapx", 6);
                    formats.put("sake", 6);
                    formats.put("sasc", 6);     // P.S. id do not find in the master list (Measured stable organic C by soil layer, g[C]/100g[Soil])
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
                    formats.put("icrz#", 6);    // P.S. use the "icrz#" instead of "icrzno"
                    formats.put("icrze", 6);
                    formats.put("icwt", 6);
                    formats.put("icrag", 6);
                    formats.put("icrn", 6);
                    formats.put("icrp", 6);
                    formats.put("icrip", 6);
                    formats.put("icrdp", 6);
                    formats.put("ic_name", line.length());
                    // Read line and save into return holder
                    LinkedHashMap tmp = readLine(line, formats);
                    translateDateStr(tmp, "icdat");
                    icArr.add(tmp);
                    icdArr = new ArrayList<LinkedHashMap>();
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
                    formats.put("pldae", 6);
                    formats.put("plpop", 6);
                    formats.put("plpoe", 6);
                    formats.put("plma", 6);     // P.S. 2012.07.13 changed from plme to plma
                    formats.put("plds", 6);
                    formats.put("plrs", 6);
                    formats.put("plrd", 6);
                    formats.put("pldp", 6);
                    formats.put("plmwt", 6);
                    formats.put("page", 6);
                    formats.put("penv", 6);
                    formats.put("plph", 6);
                    formats.put("plspl", 6);
                    formats.put("pl_name", line.length());
                    // Read line and save into return holder
                    LinkedHashMap tmp = readLine(line, formats);
                    translateDateStr(tmp, "pdate");
                    translateDateStr(tmp, "pldae");
                    plArr.add(tmp);
//                    if (plArr.size() == 1) {
//                        metaData.put("pdate", tmp.get("pdate")); // TODO keep for the early version
//                    }
                } else {
                }

            } // Read IRRIGATION AND WATER MANAGEMENT Section
            else if (flg[0].startsWith("irrigation")) {

                // Read IRRIGATION global data
                if (flg[1].startsWith("i  efir") && flg[2].equals("data")) {
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
                    LinkedHashMap tmp = readLine(line, formats);
                    ireff = (String) tmp.get("ireff");
                    if (ireff != null) {
                        tmp.remove("ireff");
                    }
                    irArr.add(tmp);
                    irdArr = new ArrayList<LinkedHashMap>();
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
                    LinkedHashMap tmp = readLine(line, formats);
//                    tmp.put("idate", translateDateStr((String) tmp.getOr("idate"))); // TODO DOY handling
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
                    LinkedHashMap tmp = readLine(line, formats);
//                    translateDateStr(tmp, "fdate"); // TODO DOY handling
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
                    formats.put("omn%", 6);      // P.S. id do not match with the master list "omnpct"
                    formats.put("omp%", 6);      // P.S. id do not match with the master list "omppct"
                    formats.put("omk%", 6);      // P.S. id do not match with the master list "omkpct"
                    formats.put("ominp", 6);
                    formats.put("omdep", 6);
                    formats.put("omacd", 6);
                    formats.put("om_name", line.length());
                    // Read line and save into return holder
                    LinkedHashMap tmp = readLine(line, formats);
//                    translateDateStr(tmp, "omdat"); // TODO DOY handling
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
                    LinkedHashMap tmp = readLine(line, formats);
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
                    LinkedHashMap tmp = readLine(line, formats);
                    translateDateStr(tmp, "tdate");
                    tiArr.add(tmp);
                } else {
                }

//            } // Read ENVIRONMENT MODIFICATIONS Section
//            else if (flg[0].startsWith("environment")) {
//
//                // Read ENVIRONMENT MODIFICATIONS data
//                if (flg[2].equals("data")) {
//                    // Set variables' formats
//                    formats.clear();
//                    formats.put("em", 2);
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
//                    // Read line and save into return holder
//                    LinkedHashMap tmp = readLine(line, formats);
//                    translateDateStr(tmp, "emday");
//                    emArr.add(tmp);
//                } else {
//                }

            } // Read HARVEST DETAILS Section
            else if (flg[0].startsWith("harvest")) {

                // Read HARVEST data
                if (flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("ha", 2);
                    formats.put("hdate", 6);
                    formats.put("hastg", 6);
                    formats.put("hacom", 6);
                    formats.put("hasiz", 6);
                    formats.put("hapc", 6);
                    formats.put("habpc", 6);
                    formats.put("ha_name", line.length());
                    // Read line and save into return holder
                    LinkedHashMap tmp = readLine(line, formats);
                    translateDateStr(tmp, "hdate");
                    haArr.add(tmp);
//                    if (haArr.size() == 1) {
//                        metaData.put("hdate", tmp.get("hdate")); // TODO keep for early version
//                    }
                } else {
                }


            } // Read SIMULATION CONTROLS Section // P.S. no need to be divided
            else if (flg[0].startsWith("simulation")) {

                // Read general info
                if (flg[1].startsWith("n general") && flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("sm", 2);
//                    formats.put("general", 12);
//                    formats.put("nyers", 6);
//                    formats.put("nreps", 6);
//                    formats.put("start", 6);
//                    formats.put("sdate", 6);
//                    formats.put("rseed", 6);
//                    formats.put("sname", 26);
//                    formats.put("model", line.length());
                    // Read line and save into return holder
                    LinkedHashMap tmp = readLine(line, formats);
//                    translateDateStr(tmp, "sdate");
                    smSubArr = new ArrayList();
                    smArr.add(tmp);
                    tmp.put("data", smSubArr);
                    smSubArr.add(line);
//                    addToArray(smArr, tmp, "sm");


                } // Read options info
                else if (flg[1].startsWith("n options") && flg[2].equals("data")) {
                    // Set variables' formats
//                    formats.clear();
//                    formats.put("sm", 2);
//                    formats.put("options", 12);
//                    formats.put("water", 6);
//                    formats.put("nitro", 6);
//                    formats.put("symbi", 6);
//                    formats.put("phosp", 6);
//                    formats.put("potas", 6);
//                    formats.put("dises", 6);
//                    formats.put("chem", 6);
//                    formats.put("till", 6);
//                    formats.put("co2", 6);
                    // Read line and save into return holder
//                    LinkedHashMap tmp = readLine(line, formats);
//                    tmp.put("options", line);
//                    addToArray(smArr, tmp, "sm");
                    smSubArr.add(line);

                } // Read methods info
                else if (flg[1].startsWith("n methods") && flg[2].equals("data")) {
                    // Set variables' formats
//                    formats.clear();
//                    formats.put("sm", 2);
//                    formats.put("methods", 12);
//                    formats.put("wther", 6);
//                    formats.put("incon", 6);
//                    formats.put("light", 6);
//                    formats.put("evapo", 6);
//                    formats.put("infil", 6);
//                    formats.put("photo", 6);
//                    formats.put("hydro", 6);
//                    formats.put("nswit", 6);
//                    formats.put("mesom", 6);
//                    formats.put("mesev", 6);
//                    formats.put("mesol", 6);
                    // Read line and save into return holder
//                    LinkedHashMap tmp = readLine(line, formats);
//                    tmp.put("methods", line);
//                    addToArray(smArr, tmp, "sm");
                    smSubArr.add(line);

                } // Read management info
                else if (flg[1].startsWith("n management") && flg[2].equals("data")) {
                    // Set variables' formats
//                    formats.clear();
//                    formats.put("sm", 2);
//                    formats.put("management", 12);
//                    formats.put("plant", 6);
//                    formats.put("irrig", 6);
//                    formats.put("ferti", 6);
//                    formats.put("resid", 6);
//                    formats.put("harvs", 6);
                    // Read line and save into return holder
//                    LinkedHashMap tmp = readLine(line, formats);
//                    tmp.put("management", line);
//                    addToArray(smArr, tmp, "sm");
                    smSubArr.add(line);

                } // Read outputs info
                else if (flg[1].startsWith("n outputs") && flg[2].equals("data")) {
                    // Set variables' formats
//                    formats.clear();
//                    formats.put("sm", 2);
//                    formats.put("outputs", 12);
//                    formats.put("fname", 6);
//                    formats.put("ovvew", 6);
//                    formats.put("sumry", 6);
//                    formats.put("fropt", 6);
//                    formats.put("grout", 6);
//                    formats.put("caout", 6);
//                    formats.put("waout", 6);
//                    formats.put("niout", 6);
//                    formats.put("miout", 6);
//                    formats.put("diout", 6);
//                    formats.put("long", 6);
//                    formats.put("chout", 6);
//                    formats.put("opout", 6);
                    // Read line and save into return holder
//                    LinkedHashMap tmp = readLine(line, formats);
//                    tmp.put("outputs", line);
//                    addToArray(smArr, tmp, "sm");
                    smSubArr.add(line);

                } // Read planting info
                else if (flg[1].startsWith("n planting") && flg[2].equals("data")) {
                    // Set variables' formats
//                    formats.clear();
//                    formats.put("sm", 2);
//                    formats.put("planting", 12);
//                    formats.put("pfrst", 6);
//                    formats.put("plast", 6);
//                    formats.put("ph20l", 6);
//                    formats.put("ph2ou", 6);
//                    formats.put("ph20d", 6);
//                    formats.put("pstmx", 6);
//                    formats.put("pstmn", 6);
                    // Read line and save into return holder
//                    LinkedHashMap tmp = readLine(line, formats);
//                    translateDateStr(tmp, "pfrst");
//                    translateDateStr(tmp, "plast");
//                    tmp.put("planting", line);
//                    addToArray(smArr, tmp, "sm");
                    smSubArr.add(line);

                } // Read irrigation info
                else if (flg[1].startsWith("n irrigation") && flg[2].equals("data")) {
                    // Set variables' formats
//                    formats.clear();
//                    formats.put("sm", 2);
//                    formats.put("irrigation", 12);
//                    formats.put("imdep", 6);
//                    formats.put("ithrl", 6);
//                    formats.put("ithru", 6);
//                    formats.put("iroff", 6);
//                    formats.put("imeth", 6);
//                    formats.put("iramt", 6);
//                    formats.put("ireff", 6);
                    // Read line and save into return holder
//                    LinkedHashMap tmp = readLine(line, formats);
//                    tmp.put("irrigation", line);
//                    addToArray(smArr, tmp, "sm");
                    smSubArr.add(line);

                } // Read nitrogen info
                else if (flg[1].startsWith("n nitrogen") && flg[2].equals("data")) {
                    // Set variables' formats
//                    formats.clear();
//                    formats.put("sm", 2);
//                    formats.put("nitrogen", 12);
//                    formats.put("nmdep", 6);
//                    formats.put("nmthr", 6);
//                    formats.put("namnt", 6);
//                    formats.put("ncode", 6);
//                    formats.put("naoff", 6);
                    // Read line and save into return holder
//                    LinkedHashMap tmp = readLine(line, formats);
//                    tmp.put("nitrogen", line);
//                    addToArray(smArr, tmp, "sm");
                    smSubArr.add(line);

                } // Read residues info
                else if (flg[1].startsWith("n residues") && flg[2].equals("data")) {
                    // Set variables' formats
//                    formats.clear();
//                    formats.put("sm", 2);
//                    formats.put("residues", 12);
//                    formats.put("ripcn", 6);
//                    formats.put("rtime", 6);
//                    formats.put("ridep", 6);
                    // Read line and save into return holder
//                    LinkedHashMap tmp = readLine(line, formats);
//                    tmp.put("residues", line);
//                    addToArray(smArr, tmp, "sm");
                    smSubArr.add(line);

                } // Read harvest info
                else if (flg[1].startsWith("n harvest") && flg[2].equals("data")) {
                    // Set variables' formats
//                    formats.clear();
//                    formats.put("sm", 2);
//                    formats.put("harvests", 12);
//                    formats.put("hfrst", 6);    // P.S. Keep the original value
//                    formats.put("hlast", 6);
//                    formats.put("hpcnp", 6);
//                    formats.put("hrcnr", 6);
                    // Read line and save into return holder
//                    LinkedHashMap tmp = readLine(line, formats);
//                    translateDateStr(tmp, "hlast");
//                    tmp.put("harvest", line);
//                    addToArray(smArr, tmp, "sm");
                    smSubArr.add(line);

                } else {
                }

            } else {
            }
        }

        br.close();
        if (brw != null) {
            brw.close();
        }

        // Get Observed data info
        LinkedHashMap obvAFile = obvReaderA.readFileWithoutCompress(brMap);
        ArrayList<LinkedHashMap> obvAArr = getObjectOr(obvAFile, eventKey, new ArrayList<LinkedHashMap>());
        LinkedHashMap obvTFile = obvReaderT.readFileWithoutCompress(brMap);
        ArrayList<LinkedHashMap> obvTArr = getObjectOr(obvTFile, eventKey, new ArrayList<LinkedHashMap>());

        // Set meta data info for each treatment
        ArrayList<LinkedHashMap> trMetaArr = new ArrayList<LinkedHashMap>();
        LinkedHashMap trMetaData;
        metaData.put("tr_meta", trMetaArr);

        // Combine all the sections data into the related treatment block
        String trno = null;
        LinkedHashMap dssatSq = new LinkedHashMap();
        dssatSq.put("data", sqArr);
        metaData.put("dssat_sequence", dssatSq);
        for (int i = 0, seqid = 1; i < sqArr.size(); i++, seqid++) {
            sqData = (LinkedHashMap) sqArr.get(i);

            trMetaData = new LinkedHashMap();
            trMetaArr.add(trMetaData);

            if (!sqData.get("trno").equals(trno)) {
                trno = sqData.get("trno").toString();
                trData = new LinkedHashMap();
                evtArr = new ArrayList<LinkedHashMap>();
                trArr.add(trData);
                trData.put("events", evtArr);
            }
            sqData.put("seqid", seqid);

            // cultivar
            LinkedHashMap geTmp;
            if (!getObjectOr(sqData, "ge", "0").equals("0")) {
//                treatment.put("cultivar", getSectionData(cuArr, "ge", treatment.get("ge").toString()));
                // TODO move some stuff into dssat_info block or planting event
                trData.putAll((LinkedHashMap) getSectionData(cuArr, "ge", sqData.get("ge").toString()));
                geTmp = (LinkedHashMap) getSectionData(cuArr, "ge", sqData.get("ge").toString());
            } else {
                geTmp = new LinkedHashMap();
            }

            // field
            if (!getObjectOr(sqData, "fl", "0").equals("0")) {
                // TODO move some stuff into dssat_info block
                trMetaData.putAll((LinkedHashMap) getSectionData(flArr, "fl", sqData.get("fl").toString()));
                trMetaData.remove("fl");
            }

            // initial_condition
            if (!getObjectOr(sqData, "ic", "0").equals("0")) {
                // TODO move out to top block
                trData.put("initial_condition", getSectionData(icArr, "ic", sqData.get("ic").toString()));
            }

            // planting
            if (!getObjectOr(sqData, "pl", "0").equals("0")) {
                LinkedHashMap plTmp = CopyList((LinkedHashMap) getSectionData(plArr, "pl", sqData.get("pl").toString()), "pdate", "planting", i+1);
                if (geTmp.containsKey("cul_name"))
                plTmp.put("cul_name", geTmp.get("cul_name"));

                evtArr.add(plTmp);
            }

//            // irrigation
//            if (!getObjectOr(sqData, "ir", "0").equals("0")) {
//                sqData.put("irrigation", getSectionData(irArr, "ir", sqData.get("ir").toString()));
//            }
//
//            // fertilizer
//            if (!getObjectOr(sqData, "fe", "0").equals("0")) {
//                sqData.put("fertilizer", getSectionData(feArr, "fe", sqData.get("fe").toString()));
//            }
//
//            // residue_organic
//            if (!getObjectOr(sqData, "om", "0").equals("0")) {
//                sqData.put("residue_organic", getSectionData(omArr, "om", sqData.get("om").toString()));
//            }
//
//            // chemical
//            if (!getObjectOr(sqData, "ch", "0").equals("0")) {
//                sqData.put("chemical", getSectionData(chArr, "ch", sqData.get("ch").toString()));
//            }
//
//            // tillage
//            if (!getObjectOr(sqData, "ti", "0").equals("0")) {
//                sqData.put("tillage", getSectionData(tiArr, "ti", sqData.get("ti").toString()));
//            }
//
//            // emvironment
////            if (!getObjectOr(treatment, "em", "0").equals("0")) {
////                treatment.put("emvironment", getSectionData(emArr, "em", treatment.get("em").toString()));
////            }
//
//            // harvest
//            if (!getObjectOr(sqData, "ha", "0").equals("0")) {
//                sqData.putAll((LinkedHashMap) getSectionData(haArr, "ha", sqData.get("ha").toString()));
//            }
//
//            // simulation
//            if (!getObjectOr(sqData, "sm", "0").equals("0")) {
//                sqData.put("simulation", getSectionData(smArr, "sm", sqData.get("sm").toString()));
//            }
//
//            // soil_analysis
//            if (!getObjectOr(sqData, "sa", "0").equals("0")) {
//                LinkedHashMap saTmp = (LinkedHashMap) getSectionData(saArr, "sa", sqData.get("sa").toString());
//                ArrayList<LinkedHashMap> saSubArr = (ArrayList) saTmp.get("data");
//
//                // Add SADAT into soil block
////                // If there is soil data come with experiment data
////                if (!ret.containsKey("soil")) {
////                    if (saTmp.containsKey("sadat")) {
////                        treatment.put("sadat", saTmp.get("sadat"));
////                    }
////                }
////                // If there is no soil data, create dummy soil section
////                else {
////                    // TODO create dummy soil block to hold SADAT
////                    LinkedHashMap soilData = new LinkedHashMap();
////                    ArrayList<LinkedHashMap> soilDataArr = new ArrayList<LinkedHashMap>();
////                    soilData.put("sadat", saTmp.get("sadat"));
////                    
////                    
////                    LinkedHashMap soilTmp = (LinkedHashMap) treatment.get("soil");
////                    if (saTmp.containsKey("sadat")) {
////                        soilTmp.put("sadat", saTmp.get("sadat"));
////                    }
////                }
//
//                if (saTmp.containsKey("sadat")) {
//                    sqData.put("sadat", saTmp.get("sadat"));
//                }
//
//
//
//                // Add SASC into initial condition block
//                if (!sqData.containsKey("initial_condition")) {
//                    // TODO add a dummy ic block to hold SASC
//                } else {
//                    LinkedHashMap icTmp = (LinkedHashMap) sqData.get("initial_condition");
//                    ArrayList<LinkedHashMap> icSubArr = (ArrayList) icTmp.get("data");
//                    ArrayList<LinkedHashMap> icSubArrNew = new ArrayList<LinkedHashMap>();
//                    ArrayList<String> sablArr = getLayerNumArray(saSubArr, "sabl");
//                    ArrayList<String> icblArr = getLayerNumArray(icSubArr, "icbl");
//
//                    int cnt = 0;
//                    for (int j = 0; j < sablArr.size(); j++) {
//                        LinkedHashMap tmp;
//                        String sabl = sablArr.get(j);
//                        String icbl = "";
//                        String sasc = getValueOr((LinkedHashMap) saSubArr.get(j), "sasc", "");
//
//                        for (int k = cnt; k < icblArr.size(); k++, cnt++) {
//                            icbl = icblArr.get(k);
//                            if (Double.parseDouble(icbl) == Double.parseDouble(sabl)) {
//                                tmp = (LinkedHashMap) getSectionData(icSubArr, "icbl", icbl);
//                                icSubArrNew.add(tmp);
//                                tmp.put("sasc", sasc);
//                                break;
//                            } else if (Double.parseDouble(icbl) > Double.parseDouble(sabl)) {
//                                tmp = CopyList((LinkedHashMap) getSectionData(icSubArr, "icbl", icbl));
//                                tmp.put("icbl", sabl);
//                                icSubArrNew.add(tmp);
//                                tmp.put("sasc", sasc);
//                                break;
//                            } else if (!sablArr.contains(icbl)) {
//                                tmp = (LinkedHashMap) getSectionData(icSubArr, "icbl", icbl);
//                                icSubArrNew.add(tmp);
//                                tmp.put("sasc", sasc);
//                            }
//                        }
//                    }
//
//                    for (int j = cnt; j < icblArr.size(); j++) {
//                        icSubArrNew.add((LinkedHashMap) getSectionData(icSubArr, "icbl", icblArr.get(j)));
//                    }
//
//                    String sasc = null;
//                    for (int j = icSubArrNew.size() - 1; j > 0; j--) {
//                        if (icSubArrNew.get(j).containsKey("sasc")) {
//                            sasc = (String) icSubArrNew.get(j).get("sasc");
//                        } else {
//                            if (sasc == null) {
//                                for (int k = icSubArrNew.size() - 1; k > 0; k--) {
//                                    if (icSubArrNew.get(k).get("sasc") != null) {
//                                        sasc = (String) icSubArrNew.get(k).get("sasc");
//                                        break;
//                                    }
//                                }
//                            }
//                            icSubArrNew.get(j).put("sasc", sasc);
//
//                        }
//                    }
//                    icTmp.put("data", icSubArrNew);
//                }
//            }
//
//            // observed data (summary)
//            LinkedHashMap obv = new LinkedHashMap();
//            sqData.put("observed", obv);
//            if (!getObjectOr(sqData, "trno", "0").equals("0")) {
//                obv.put("summary", getSectionData(obvAArr, "trno_a", sqData.get("trno").toString()));
//            }
//
//            // observed data (time-series)
//            if (!getObjectOr(sqData, "trno", "0").equals("0")) {
//                obv.put("time_series", getSectionData(obvTArr, "trno_t", sqData.get("trno").toString()));
//            }
//
//            // Revise the date value for FEDATE, IDATE, MLADAT
//            // Get Planting date
//            String pdate = "";
//            ArrayList<LinkedHashMap> plTmps = (ArrayList) getObjectOr(sqData, "planting", new ArrayList());
//            if (!plTmps.isEmpty()) {
//                pdate = getObjectOr(plTmps.get(0), "pdate", "");
//                if (pdate.length() > 5) {
//                    pdate = pdate.substring(2);
//                }
//            }
//
//            // Date adjust based on realted treatment info (handling for DOY type value)
//            // Fertilizer Date
//            ArrayList<LinkedHashMap> feTmps = (ArrayList) getObjectOr(sqData, "fertilizer", new ArrayList());
//            LinkedHashMap feTmp;
//            for (int j = 0; j < feTmps.size(); j++) {
//                feTmp = (LinkedHashMap) feTmps.get(j);
//                translateDateStrForDOY(feTmp, "fdate", pdate);
//            }
//
//            // Irrigation date
//            LinkedHashMap irTmp = (LinkedHashMap) sqData.get("irrigation");
//            if (irTmp != null) {
//                ArrayList<LinkedHashMap> irTmpSubs = getObjectOr(irTmp, "data", new ArrayList());
//                for (int j = 0; j < irTmpSubs.size(); j++) {
//                    LinkedHashMap irTmpSub = irTmpSubs.get(j);
//                    translateDateStrForDOY(irTmpSub, "idate", pdate);
//                }
//            }
//
//            // Mulch application date
//            ArrayList<LinkedHashMap> omTmps = (ArrayList) getObjectOr(sqData, "residue_organic", new ArrayList());
//            LinkedHashMap omTmp;
//            for (int j = 0; j < omTmps.size(); j++) {
//                omTmp = omTmps.get(j);
//                translateDateStrForDOY(omTmp, "omdat", pdate);
//            }

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
        idNames.add("em");
        idNames.add("ha");
        idNames.add("sm");
        idNames.add("trno");
        idNames.add("trno_a");
        idNames.add("trno_t");
        removeIndex(trArr, idNames);

        // TODO
//        if (!getObjectOr(obvAFile, "local_name", "").equals(getObjectOr(ret, "local_name", ""))) {
//
//            ret.put("local_name_a", obvAFile.get("local_name"));
//        }
//        if (!getObjectOr(obvTFile, "local_name", "").equals(getObjectOr(ret, "local_name", ""))) {
//            ret.put("local_name_t", obvTFile.get("local_name"));
//        }

        return trArr;
    }

    /**
     * Set reading flags for title lines (marked with *)
     * 
     * @param line  the string of reading line
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
     * @param secArr    Section data array
     * @param key       index variable name
     * @param value     index variable value
     */
    private Object getSectionData(ArrayList secArr, Object key, String value) {

        ArrayList ret = null;
        // Get First data node
        if (secArr.isEmpty() || value == null) {
            return null;
        }
        // Define the section with single sub data
        ArrayList singleSubRecSecList = new ArrayList();
        singleSubRecSecList.add("ge");
        singleSubRecSecList.add("fl");
        singleSubRecSecList.add("pl");
        singleSubRecSecList.add("ha");
        singleSubRecSecList.add("sm");
        singleSubRecSecList.add("trno_a");

        // TFile data
        if (key.equals("trno_t")) {

            ret = new ArrayList();
            // Loop blocks with different titles
            for (int i = 0; i < secArr.size(); i++) {
                ArrayList secSubArr = (ArrayList) secArr.get(i);
                // Loop blocks with differnt treatment number
                for (int j = 0; j < secSubArr.size(); j++) {
                    ArrayList secSubSubArr = (ArrayList) secSubArr.get(j);
                    if (!secSubSubArr.isEmpty()) {
                        LinkedHashMap fstNode = (LinkedHashMap) secSubSubArr.get(0);
                        if (value.equals(fstNode.get(key))) {
                            ret.add(CopyList(secSubSubArr));
                            break;
                        }
                    }
                }

            }
            return ret;

        } else if (key.equals("icbl")) {
            for (int i = 0; i < secArr.size(); i++) {
                LinkedHashMap m = (LinkedHashMap) secArr.get(i);
                if (value.equals(m.get(key))) {
                    return m;
                }
            }
        } else {
            LinkedHashMap fstNode = (LinkedHashMap) secArr.get(0);
            // If it contains multiple sub array of data, or it does not have multiple sub records
            if (fstNode.containsKey(eventKey) || fstNode.containsKey(icEventKey) || singleSubRecSecList.contains(key)) {
                for (int i = 0; i < secArr.size(); i++) {
                    if (value.equals(((LinkedHashMap) secArr.get(i)).get(key))) {
                        return CopyList((LinkedHashMap) secArr.get(i));
                    }
                }

            } // If it is simple array
            else {
                ret = new ArrayList();
                LinkedHashMap node;
                for (int i = 0; i < secArr.size(); i++) {
                    node = (LinkedHashMap) secArr.get(i);
                    if (value.equals(node.get(key))) {
                        ret.add(CopyList(node));
                    }
                }
            }
        }

        return ret;
    }

    /**
     * remove all the relation index for the input array
     * 
     * @param arr    The array of treatment data 
     * @param idNames The array of id want be removed
     */
    private void removeIndex(ArrayList arr, ArrayList idNames) {

        for (int i = 0; i < arr.size(); i++) {
            Object item = arr.get(i);
            if (item instanceof ArrayList) {
                removeIndex((ArrayList) item, idNames);
            } else if (item instanceof LinkedHashMap) {
                removeIndex((LinkedHashMap) item, idNames);
            }

        }

    }

    /**
     * remove all the relation index for the input map
     * 
     * @param m    the array of treatment data 
     */
    private void removeIndex(LinkedHashMap m, ArrayList idNames) {

        Object[] keys = m.keySet().toArray();
        for (Object key : keys) {
            Object item = m.get(key);
            if (item instanceof ArrayList) {
                removeIndex((ArrayList) item, idNames);
            } else if (item instanceof LinkedHashMap) {
                removeIndex((LinkedHashMap) item, idNames);
            } else if (item instanceof String && idNames.contains(key)) {
                m.remove(key);
            }
        }
    }

    private ArrayList<String> getLayerNumArray(ArrayList<LinkedHashMap> arr, String key) {

        ArrayList<String> ret = new ArrayList();
        for (int i = 0; i < arr.size(); i++) {
            ret.add((String) (arr.get(i).get(key)));
        }
        return ret;
    }

    /**
     * Get a copy of input map
     *
     * @param m input map
     * @return the copy of whole input map
     */
    private static LinkedHashMap CopyList(LinkedHashMap m, String dateId, String eventName, int seqId) {

        LinkedHashMap<String, String> ret = new LinkedHashMap();
//        LinkedHashMap ret = new LinkedHashMap();
        if (m.containsKey(dateId)) {
            ret.put("date", m.get(dateId).toString());
        }
        ret.put("event", eventName);
        ret.putAll(CopyList(m));
        if (ret.containsKey(dateId)) {
            ret.remove(dateId);
        }
        ret.put("seqid", seqId + "");

        return ret;
    }
}
