package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.IOException;
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
        BufferedReader brw = null;
        HashMap mapW;
        String wid;
        String fileName;
        LinkedHashMap formats = new LinkedHashMap();

        br = (BufferedReader) brMap.get("X");
        mapW = (HashMap) brMap.get("W");
        fileName = (String) brMap.get("Z");
        wid = fileName.length() > 4 ? fileName.substring(0, 4) : fileName;

        // If XFile is no been found
        if (br == null) {
            // TODO reprot file not exist error
            return ret;
        }

        while ((line = br.readLine()) != null) {
            // TODO Create holders for each section and build final result holder in the last
            // Get content type of line
            judgeContentType(line);

            // Read Exp Detail
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

                if (flg[1].equals("people") && flg[2].equals("data")) {
                    ret.put("people", line.trim());
                } else if (flg[1].equals("address") && flg[2].equals("data")) {
                    //$ret[$flg[0]]["address"] = trim($line);
                    String[] addr = line.split(",[ ]*");
                    ret.put("fl_loc_1", "");
                    ret.put("fl_loc_2", "");
                    ret.put("fl_loc_3", "");
                    if (!line.trim().equals("")) {
                        ret.put("institutes", line.trim());
                    }
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
                } else if ((flg[1].equals("site") || flg[1].equals("sites")) && flg[2].equals("data")) {
                    //$ret[$flg[0]]["site"] = trim($line);
                } else if (flg[1].startsWith("parea") && flg[2].equals("data")) {
                } else if (flg[1].equals("notes") && flg[2].equals("data")) {
                    //$ret[$flg[0]]["notes"] = addArray($ret[$flg[0]]["notes"], " ". trim($line), "");
                } else {
                }

            } // Read TREATMENTS Section
            else if (flg[0].startsWith("treatments")) {
                //TODO
            } // Read CULTIVARS Section
            else if (flg[0].startsWith("cultivars")) {
                // TODO
                ret.put("cr", line.substring(3, 5).trim());
            } // Read FIELDS Section
            else if (flg[0].startsWith("fields")) {

                if (flg[1].startsWith("l id_") && flg[2].equals("data")) {
                    //TODO
                    wid = line.substring(12, 20).trim();
                } else if (flg[1].startsWith("l ...") && flg[2].equals("data")) {

                    String strLat = line.substring(3, 18).trim();
                    String strLong = line.substring(19, 34).trim();

                    // If lat or long is not valid data, read data from weather file
                    if (!checkValidValue(strLat) || !checkValidValue(strLong) || (Double.parseDouble(strLat) == 0 && Double.parseDouble(strLong) == 0)) {

                        // check if weather is validable
                        for (Object key : mapW.keySet()) {
                            if (((String) key).contains(wid)) {
                                brw = (BufferedReader) mapW.get(key);
                                break;
                            }
                        }
                        if (brw != null) {
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
                    ret.put("fl_lat", strLong);
                    ret.put("fl_long", strLat);
                }

            } // Read SOIL ANALYSIS Section
            else if (flg[0].startsWith("soil")) {
                // TODO
            } // Read INITIAL CONDITIONS Section
            else if (flg[0].startsWith("initial")) {
                // TODO
            } // Read PLANTING DETAILS Section
            else if (flg[0].startsWith("planting")) {
                // TODO
                ret.put("pdate", line.substring(3, 8).trim());
            } // Read IRRIGATION AND WATER MANAGEMENT Section
            else if (flg[0].startsWith("irrigation")) {
                // TODO
            } // Read FERTILIZERS (INORGANIC) Section
            else if (flg[0].startsWith("fertilizers")) {
                // TODO
            } // Read RESIDUES AND OTHER ORGANIC MATERIALS Section
            else if (flg[0].startsWith("residues")) {
                // TODO
            } // Read CHEMICAL APPLICATIONS Section
            else if (flg[0].startsWith("chemical")) {
                // TODO
            } // Read TILLAGE Section
            else if (flg[0].startsWith("tillage")) {
                // TODO
            } // Read ENVIRONMENT MODIFICATIONS Section
            else if (flg[0].startsWith("environment")) {
                // TODO
            } // Read HARVEST DETAILS Section
            else if (flg[0].startsWith("harvest")) {
                // TODO
                ret.put("hdate", line.substring(3, 8).trim());
            } // Read SIMULATION CONTROLS Section
            else if (flg[0].startsWith("simulation")) {
                // TODO
            } else {
            }


        }

        br.close();
        brw.close();

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
}
