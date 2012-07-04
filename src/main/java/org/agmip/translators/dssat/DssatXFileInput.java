package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.agmip.core.types.AdvancedHashMap;

/**
 * DSSAT Experiment Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatXFileInput extends DssatCommonInput {

    /**
     * Constructor with no parameters
     * Set jsonKey as "experiment"
     * 
     */
    public DssatXFileInput() {
        super();
        jsonKey = "experiment";
    }

    /**
     * DSSAT XFile Data input method for Controller using
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    @Override
    protected AdvancedHashMap readFile(HashMap brMap) throws IOException {

        AdvancedHashMap ret = new AdvancedHashMap();
        String line;
        BufferedReader br;
        Object buf;
        BufferedReader brw = null;
        char[] bufW = null;
        HashMap mapW;
        String wid;
        String fileName;
        LinkedHashMap formats = new LinkedHashMap();
        ArrayList trArr = new ArrayList();
        ArrayList cuArr = new ArrayList();
        ArrayList flArr = new ArrayList();
        ArrayList saArr = new ArrayList();
        ArrayList sadArr = new ArrayList();
        ArrayList icArr = new ArrayList();
        ArrayList icdArr = new ArrayList();
        ArrayList plArr = new ArrayList();
        ArrayList irArr = new ArrayList();
        ArrayList irdArr = new ArrayList();
        ArrayList feArr = new ArrayList();
        ArrayList omArr = new ArrayList();
        ArrayList chArr = new ArrayList();
        ArrayList tiArr = new ArrayList();
        ArrayList emArr = new ArrayList();
        ArrayList haArr = new ArrayList();
        ArrayList smArr = new ArrayList();
        String eventKey = "data";

        buf = brMap.get("X");
        mapW = (HashMap) brMap.get("W");
        fileName = (String) brMap.get("Z");
        wid = fileName.length() > 4 ? fileName.substring(0, 4) : fileName;

        // If XFile is no been found
        if (buf == null) {
            // TODO reprot file not exist error
            return ret;
        } else {
            if (buf instanceof char[]) {
                br = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                br = (BufferedReader) buf;
            }
        }

        ret.put("treatment", trArr);
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
                ret.put(readLine(line.substring(13), formats));
                ret.put("institutes", line.substring(14, 16).trim());
            } // Read General Section
            else if (flg[0].startsWith("general")) {

                // People info
                if (flg[1].equals("people") && flg[2].equals("data")) {
                    ret.put("people", line.trim());

                } // Address info
                else if (flg[1].equals("address") && flg[2].equals("data")) {
                    String[] addr = line.split(",[ ]*");
                    ret.put("fl_loc_1", "");
                    ret.put("fl_loc_2", "");
                    ret.put("fl_loc_3", "");
                    ret.put("institutes", line.trim());
//                    ret.put("address", line.trim());    // P.S. no longer to use this field

                    switch (addr.length) {
                        case 0:
                            break;
                        case 1:
                            ret.put("fl_loc_1", addr[0]);
                            break;
                        case 2:
                            ret.put("fl_loc_1", addr[1]);
                            ret.put("fl_loc_2", addr[0]);
                            break;
                        case 3:
                            ret.put("fl_loc_1", addr[2]);
                            ret.put("fl_loc_2", addr[1]);
                            ret.put("fl_loc_3", addr[0]);
                            break;
                        default:
                            ret.put("fl_loc_1", addr[addr.length - 1]);
                            ret.put("fl_loc_2", addr[addr.length - 2]);
                            String loc3 = "";
                            for (int i = 0; i < addr.length - 2; i++) {
                                loc3 += addr[i] + ", ";
                            }
                            ret.put("fl_loc_3", loc3.substring(0, loc3.length() - 2));
                    }

                } // Site info
                else if ((flg[1].equals("site") || flg[1].equals("sites")) && flg[2].equals("data")) {
                    //$ret[$flg[0]]["site"] = trim($line);
                    // P.S. site is missing in the master variables list
                    ret.put("site", line.trim());

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
                    ret.put("plot_info", readLine(line, formats));

                } // Notes field
                else if (flg[1].equals("notes") && flg[2].equals("data")) {
                    //$ret[$flg[0]]["notes"] = addArray($ret[$flg[0]]["notes"], " ". trim($line), "");
                    if (!ret.containsKey("notes")) {
                        ret.put("notes", line.trim() + "\\r\\n");
                    } else {
                        String notes = (String) ret.get("notes");
                        notes += line.trim() + "\\r\\n";
                        ret.put("notes", notes);
                    }
                } else {
                }

            } // Read TREATMENTS Section
            else if (flg[0].startsWith("treatments")) {

                // Read TREATMENTS data
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
                    trArr.add(readLine(line, formats));
                } else {
                }


            } // Read CULTIVARS Section
            else if (flg[0].startsWith("cultivars")) {

                // Read CULTIVARS data
                if (flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("ge", 2);
                    formats.put("cr", 3);
                    formats.put("cul_id", 7);
                    formats.put("cul_name", 17);
                    // Read line and save into return holder
                    cuArr.add(readLine(line, formats));
                    if (cuArr.size() == 1) {
                        ret.put("cr", line.substring(3, 5).trim()); // TODO keep for the early version; just first entry
                    }
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
                    formats.put("wsta_id", 9);      //P.S. id do not match with the master list "wth_id"; might have another id name
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
                    AdvancedHashMap tmp = readLine(line, formats);

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
                                strLat = defValI;
                                strLong = defValI;
                            }
                        } // if weather file is not avaliable to read, set invalid value for lat and long
                        else {
                            strLat = defValI;
                            strLong = defValI;
                        }
                    }
                    if (flArr.isEmpty()) {
                        ret.put("fl_lat", strLat); // TODO Keep the meta data handling for the early version
                        ret.put("fl_long", strLong); // TODO Keep the meta data handling for the early version
                    }
                    tmp.put("fl_lat", strLat);
                    tmp.put("fl_long", strLong);
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
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("sadat", translateDateStr((String) tmp.get("sadat")));
                    saArr.add(tmp);
                    sadArr = new ArrayList();
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
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("icdat", translateDateStr((String) tmp.get("icdat")));
                    icArr.add(tmp);
                    icdArr = new ArrayList();
                    tmp.put(eventKey, icdArr);

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
                    formats.put("plme", 6);
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
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("pdate", translateDateStr((String) tmp.get("pdate")));
                    tmp.put("pldae", translateDateStr((String) tmp.get("pldae")));
                    plArr.add(tmp);
                    if (plArr.size() == 1) {
                        ret.put("pdate", translateDateStr(line.substring(3, 8).trim())); // TODO keep for the early version
                    }
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
                    AdvancedHashMap tmp = readLine(line, formats);
                    irArr.add(tmp);
                    irdArr = new ArrayList();
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
                    AdvancedHashMap tmp = readLine(line, formats);
//                    tmp.put("idate", translateDateStr((String) tmp.get("idate"))); // TODO DOY handling
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
                    AdvancedHashMap tmp = readLine(line, formats);
//                    tmp.put("fdate", translateDateStr((String) tmp.get("fdate"))); // TODO DOY handling
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
                    AdvancedHashMap tmp = readLine(line, formats);
//                    tmp.put("omdat", translateDateStr((String) tmp.get("omdat"))); // TODO DOY handling
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
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("cdate", translateDateStr((String) tmp.get("cdate")));
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
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("tdate", translateDateStr((String) tmp.get("tdate")));
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
                    formats.put("emday", 6);
                    formats.put("ecdyl", 2);
                    formats.put("emdyl", 4);
                    formats.put("ecrad", 2);
                    formats.put("emrad", 4);
                    formats.put("ecmax", 2);
                    formats.put("emmax", 4);
                    formats.put("ecmin", 2);
                    formats.put("emmin", 4);
                    formats.put("ecrai", 2);
                    formats.put("emrai", 4);
                    formats.put("ecco2", 2);
                    formats.put("emco2", 4);
                    formats.put("ecdew", 2);
                    formats.put("emdew", 4);
                    formats.put("ecwnd", 2);
                    formats.put("emwnd", 4);
                    formats.put("em_name", line.length());
                    // Read line and save into return holder
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("emday", translateDateStr((String) tmp.get("emday")));
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
                    formats.put("hdate", 6);
                    formats.put("hastg", 6);
                    formats.put("hacom", 6);
                    formats.put("hasiz", 6);
                    formats.put("hapc", 6);
                    formats.put("habpc", 6);
                    formats.put("ha_name", line.length());
                    // Read line and save into return holder
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("hdate", translateDateStr((String) tmp.get("hdate")));
                    haArr.add(tmp);
                    if (haArr.size() == 1) {
                        ret.put("hdate", translateDateStr(line.substring(3, 8).trim())); // TODO keep for early version
                    }
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
                    AdvancedHashMap tmp = readLine(line, formats);
//                    tmp.put("sdate", translateDateStr((String) tmp.get("sdate")));
                    tmp.put("general", line);
                    addToArray(smArr, tmp, "sm");


                } // Read options info
                else if (flg[1].startsWith("n options") && flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("sm", 2);
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
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("options", line);
                    addToArray(smArr, tmp, "sm");

                } // Read methods info
                else if (flg[1].startsWith("n methods") && flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("sm", 2);
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
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("methods", line);
                    addToArray(smArr, tmp, "sm");

                } // Read management info
                else if (flg[1].startsWith("n management") && flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("sm", 2);
//                    formats.put("management", 12);
//                    formats.put("plant", 6);
//                    formats.put("irrig", 6);
//                    formats.put("ferti", 6);
//                    formats.put("resid", 6);
//                    formats.put("harvs", 6);
                    // Read line and save into return holder
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("management", line);
                    addToArray(smArr, tmp, "sm");

                } // Read outputs info
                else if (flg[1].startsWith("n outputs") && flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("sm", 2);
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
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("outputs", line);
                    addToArray(smArr, tmp, "sm");

                } // Read planting info
                else if (flg[1].startsWith("n planting") && flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("sm", 2);
//                    formats.put("planting", 12);
//                    formats.put("pfrst", 6);
//                    formats.put("plast", 6);
//                    formats.put("ph20l", 6);
//                    formats.put("ph2ou", 6);
//                    formats.put("ph20d", 6);
//                    formats.put("pstmx", 6);
//                    formats.put("pstmn", 6);
                    // Read line and save into return holder
                    AdvancedHashMap tmp = readLine(line, formats);
//                    tmp.put("pfrst", translateDateStr((String) tmp.get("pfrst")));
//                    tmp.put("plast", translateDateStr((String) tmp.get("plast")));
                    tmp.put("planting", line);
                    addToArray(smArr, tmp, "sm");

                } // Read irrigation info
                else if (flg[1].startsWith("n irrigation") && flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("sm", 2);
//                    formats.put("irrigation", 12);
//                    formats.put("imdep", 6);
//                    formats.put("ithrl", 6);
//                    formats.put("ithru", 6);
//                    formats.put("iroff", 6);
//                    formats.put("imeth", 6);
//                    formats.put("iramt", 6);
//                    formats.put("ireff", 6);
                    // Read line and save into return holder
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("irrigation", line);
                    addToArray(smArr, tmp, "sm");

                } // Read nitrogen info
                else if (flg[1].startsWith("n nitrogen") && flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("sm", 2);
//                    formats.put("nitrogen", 12);
//                    formats.put("nmdep", 6);
//                    formats.put("nmthr", 6);
//                    formats.put("namnt", 6);
//                    formats.put("ncode", 6);
//                    formats.put("naoff", 6);
                    // Read line and save into return holder
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("nitrogen", line);
                    addToArray(smArr, tmp, "sm");

                } // Read residues info
                else if (flg[1].startsWith("n residues") && flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("sm", 2);
//                    formats.put("residues", 12);
//                    formats.put("ripcn", 6);
//                    formats.put("rtime", 6);
//                    formats.put("ridep", 6);
                    // Read line and save into return holder
                    AdvancedHashMap tmp = readLine(line, formats);
                    tmp.put("residues", line);
                    addToArray(smArr, tmp, "sm");

                } // Read harvest info
                else if (flg[1].startsWith("n harvest") && flg[2].equals("data")) {
                    // Set variables' formats
                    formats.clear();
                    formats.put("sm", 2);
//                    formats.put("harvests", 12);
//                    formats.put("hfrst", 6);    // P.S. Keep the original value
//                    formats.put("hlast", 6);
//                    formats.put("hpcnp", 6);
//                    formats.put("hrcnr", 6);
                    // Read line and save into return holder
                    AdvancedHashMap tmp = readLine(line, formats);
//                    tmp.put("hlast", translateDateStr((String) tmp.get("hlast")));
                    tmp.put("harvest", line);
                    addToArray(smArr, tmp, "sm");

                } else {
                }

            } else {
            }
        }

        br.close();
        if (brw != null) {
            brw.close();
        }

        for (int i = 0; i < trArr.size(); i++) {
            AdvancedHashMap treatment = (AdvancedHashMap) trArr.get(i);

            // cultivar
            if (!treatment.getOr("ge", "0").equals("0")) {
                treatment.put("cultivar", getSectionData(cuArr, "ge", treatment.get("ge").toString()));
            }

            // field
            if (!treatment.getOr("fl", "0").equals("0")) {
                treatment.put("field", getSectionData(flArr, "fl", treatment.get("fl").toString()));
            }

            // soil_analysis
            if (!treatment.getOr("sa", "0").equals("0")) {
                treatment.put("soil_analysis", getSectionData(saArr, "sa", treatment.get("sa").toString()));
            }

            // initial_condition
            if (!treatment.getOr("ic", "0").equals("0")) {
                treatment.put("initial_condition", getSectionData(icArr, "ic", treatment.get("ic").toString()));
            }

            // plant
            if (!treatment.getOr("pl", "0").equals("0")) {
                treatment.put("plant", getSectionData(plArr, "pl", treatment.get("pl").toString()));
            }

            // irrigation
            if (!treatment.getOr("ir", "0").equals("0")) {
                treatment.put("irrigation", getSectionData(irArr, "ir", treatment.get("ir").toString()));
            }

            // fertilizer
            if (!treatment.getOr("fe", "0").equals("0")) {
                treatment.put("fertilizer", getSectionData(feArr, "fe", treatment.get("fe").toString()));
            }

            // residue_organic
            if (!treatment.getOr("om", "0").equals("0")) {
                treatment.put("residue_organic", getSectionData(omArr, "om", treatment.get("om").toString()));
            }

            // chemical
            if (!treatment.getOr("ch", "0").equals("0")) {
                treatment.put("chemical", getSectionData(chArr, "ch", treatment.get("ch").toString()));
            }

            // tillage
            if (!treatment.getOr("ti", "0").equals("0")) {
                treatment.put("tillage", getSectionData(tiArr, "ti", treatment.get("ti").toString()));
            }

            // emvironment
            if (!treatment.getOr("em", "0").equals("0")) {
                treatment.put("emvironment", getSectionData(emArr, "em", treatment.get("em").toString()));
            }

            // harvest
            if (!treatment.getOr("ha", "0").equals("0")) {
                treatment.put("harvest", getSectionData(haArr, "ha", treatment.get("ha").toString()));
            }

            // simulation
            if (!treatment.getOr("sm", "0").equals("0")) {
                treatment.put("simulation", getSectionData(smArr, "sm", treatment.get("sm").toString()));
            }

            // Revise the date value for FEDATE, IDATE, MLADAT
            // Get Planting date
            String pdate = "";
            ArrayList plTmps = (ArrayList) treatment.getOr("planting", new ArrayList());
            if (!plTmps.isEmpty()) {
                pdate = (String) ((AdvancedHashMap) plTmps.get(0)).getOr("pdate", "");
                if (pdate.length() > 5) {
                    pdate = pdate.substring(2);
                }
            }

            // Fertilizer Date
            ArrayList feTmps = (ArrayList) treatment.getOr("fertilizer", new ArrayList());
            AdvancedHashMap feTmp;
            for (int j = 0; j < feTmps.size(); j++) {
                feTmp = (AdvancedHashMap) feTmps.get(j);
                feTmp.put("fdate", translateDateStrForDOY((String) feTmp.get("fdate"), pdate));
            }

            // Irrigation date
            AdvancedHashMap irTmp = (AdvancedHashMap) treatment.get("irrigation");
            if (irTmp != null) {
                ArrayList irTmpSubs = (ArrayList) irTmp.getOr("data", new ArrayList());
                for (int j = 0; j < irTmpSubs.size(); j++) {
                    AdvancedHashMap irTmpSub = (AdvancedHashMap) irTmpSubs.get(j);
                    irTmpSub.put("idate", translateDateStrForDOY((String) irTmpSub.getOr("idate", defValI), pdate));
                }
            }

            // Mulch application date
            ArrayList omTmps = (ArrayList) treatment.getOr("residue_organic", new ArrayList());
            AdvancedHashMap omTmp;
            for (int j = 0; j < omTmps.size(); j++) {
                omTmp = (AdvancedHashMap) omTmps.get(j);
                omTmp.put("omdat", translateDateStrForDOY((String) omTmp.get("omdat"), pdate));
            }

        }
        compressData(ret);

        return ret;
    }

    /**
     * Set reading flgs for title lines (marked with *)
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
        if (secArr.isEmpty()) {
            return null;
        }
        // Define the section with single sub data
        ArrayList singleSubRecSecList = new ArrayList();
        singleSubRecSecList.add("ge");
        singleSubRecSecList.add("fl");
        singleSubRecSecList.add("pl");
//        singleSubRecSecList.add("om");  // P.S. wait for confirmation that single sub record
//        singleSubRecSecList.add("ti");  // P.S. wait for confirmation that single sub record
        singleSubRecSecList.add("sm");
        AdvancedHashMap fstNode = (AdvancedHashMap) secArr.get(0);
        // If it contains multiple sub array of data, or it does not have multiple sub records
        if (fstNode.containsKey("data") || singleSubRecSecList.contains(key)) {
            for (int i = 0; i < secArr.size(); i++) {
                if (((AdvancedHashMap) secArr.get(i)).get(key).equals(value)) {
                    return CopyMap((AdvancedHashMap) secArr.get(i));
                }
            }

        } // If it is simple array
        else {
            ret = new ArrayList();
            AdvancedHashMap node;
            for (int i = 0; i < secArr.size(); i++) {
                node = (AdvancedHashMap) secArr.get(i);
                if (node.get(key).equals(value)) {
                    ret.add(CopyMap(node));
                }
            }
        }

        return ret;
    }
}
