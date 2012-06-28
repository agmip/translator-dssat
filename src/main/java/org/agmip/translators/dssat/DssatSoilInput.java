package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.agmip.core.types.AdvancedHashMap;

/**
 * DSSAT Soil Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatSoilInput extends DssatCommonInput {

    /**
     * Constructor with no parameters
     * Set jsonKey as "soil"
     * 
     */
    public DssatSoilInput() {
        super();
        jsonKey = "soil";
    }

    /**
     * DSSAT Soil Data input method for Controller using
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    @Override
    protected AdvancedHashMap readFile(HashMap brMap) throws IOException {

        AdvancedHashMap ret = new AdvancedHashMap();
        ArrayList files = new ArrayList();
        AdvancedHashMap file;
        ArrayList sites;
        String line;
        BufferedReader brS = null;
        LinkedHashMap mapS;
        LinkedHashMap formats = new LinkedHashMap();
        String layerKey = "SoilLayer";  // TODO the key name might change

        mapS = (LinkedHashMap) brMap.get("S");

        // If Soil File is no been found
        if (mapS.isEmpty()) {
            // TODO reprot file not exist error
            return ret;
        }

        for (Object key : mapS.keySet()) {

            brS = (BufferedReader) mapS.get(key);
            file = new AdvancedHashMap();
            sites = new ArrayList();
            file.put("soil_sites", sites);
            files.add(file);

            while ((line = brS.readLine()) != null) {
                
                // Get content type of line
                judgeContentType(line);

                // Read SOILS Info
                if (flg[0].equals("soil") && flg[2].equals("data")) {

                    file.put("address", line.replaceFirst("\\*[Ss][Oo][Ii][Ll][Ss]?\\s*:?", "").trim());

                } // Read Site Info
                else if (flg[0].equals("site")) {

                    // header info
                    if (flg[1].equals("") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("pedon", 10);
                        formats.put("sl_source", 13);  // P.S. sl_system is always "SCS" for DSSAT
                        formats.put("sltx", 6);
                        formats.put("sldp", 6);
                        formats.put("classification", 51);
                        // Read line and save into return holder
                        sites.add(readLine(line.substring(1), formats));
                        ((AdvancedHashMap) sites.get(sites.size() - 1)).put(layerKey, new ArrayList());

                    } // Site detail info
                    else if (flg[1].startsWith("site ") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("site", 12);
                        formats.put("scount", 12);  // TODO need confirm for the key name ("")
                        formats.put("soillat", 10); // P.S. Definition changed 9 -> 10 (06/24)
                        formats.put("soillong", 8); // P.S. Definition changed 9 -> 8  (06/24)
                        formats.put("name", 51);    // P.S. "fd_name" for query using, not this time
                        // Read line and save into return holder
                        ((AdvancedHashMap) sites.get(sites.size() - 1)).put(readLine(line, formats));

                    } // soil info
                    else if (flg[1].startsWith("scom ") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("scom", 6);
                        formats.put("salb", 6);
                        formats.put("slu1", 6);
                        formats.put("sldr", 6);
                        formats.put("slro", 6);
                        formats.put("slnf", 6);
                        formats.put("slpf", 6);
                        formats.put("smhb", 6);
                        formats.put("smpx", 6);
                        formats.put("smke", 6);
                        // Read line and save into return holder
                        ((AdvancedHashMap) sites.get(sites.size() - 1)).put(readLine(line, formats));

                    } // layer part one info
                    else if (flg[1].startsWith("slb  slmh") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("sllb", 6);
                        formats.put("slmh", 6);
                        formats.put("slll", 6);
                        formats.put("sldul", 6);
                        formats.put("slsat", 6);
                        formats.put("slrgf", 6);
                        formats.put("sksat", 6);
                        formats.put("sbdm", 6);
                        formats.put("sloc", 6);
                        formats.put("clay", 6);
                        formats.put("silt", 6);
                        formats.put("slcf", 6);
                        formats.put("slni", 6);
                        formats.put("slphw", 6);
                        formats.put("slphb", 6);
                        formats.put("slcec", 6);
                        formats.put("sadc", 6);
                        // Read line and save into return holder
                        addToArray((ArrayList) ((AdvancedHashMap) sites.get(sites.size() - 1)).get(layerKey),
                                readLine(line, formats),
                                "sllb");

                    } // layer part two info
                    else if (flg[1].startsWith("slb  slpx ") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("sllb", 6);
                        formats.put("slpx", 6);
                        formats.put("slpt", 6);
                        formats.put("slpo", 6);
                        formats.put("slca", 6);
                        formats.put("slal", 6);
                        formats.put("slfe", 6);
                        formats.put("slmn", 6);
                        formats.put("slbs", 6);
                        formats.put("slpa", 6);
                        formats.put("slpb", 6);
                        formats.put("slke", 6);
                        formats.put("slmg", 6);
                        formats.put("slna", 6);
                        formats.put("slsu", 6);
                        formats.put("slec", 6);
                        formats.put("slca", 6);
                        // Read line and save into return holder
                        addToArray((ArrayList) ((AdvancedHashMap) sites.get(sites.size() - 1)).get(layerKey),
                                readLine(line, formats),
                                "sllb");

                    } else {
                    }
                } else {
                }
            }
        }

        compressData(files);
        ret.put(jsonKey, files);
        brS.close();

        return ret;
    }

    /**
     * Set reading flgs for title lines (marked with *)
     * 
     * @param line  the string of reading line
     */
    @Override
    protected void setTitleFlgs(String line) {
        if (line.toLowerCase().indexOf("soil") == 1) {
            flg[0] = "soil";
        } else {
            flg[0] = "site";
        }
        flg[1] = "";
        flg[2] = "data";
    }
}
