package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.agmip.util.MapUtil;

/**
 * DSSAT Soil Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatSoilInput extends DssatCommonInput {

    public String layerKey = "soilLayer";  // P.S. the key name might change

    /**
     * Constructor with no parameters Set jsonKey as "soil"
     *
     */
    public DssatSoilInput() {
        super();
        jsonKey = "soil";
    }

    /**
     * DSSAT Soil Data input method for only inputing soil file
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     * @throws java.io.IOException
     */
    @Override
    protected HashMap readFile(HashMap brMap) throws IOException {
        HashMap ret = new HashMap();
        ArrayList<HashMap> sites = readSoilSites(brMap, new HashMap());
//        compressData(sites);
        ret.put("soils", sites);

        return ret;
    }

    /**
     * DSSAT Soil Data input method for Controller using (return map will not be
     * compressed)
     *
     * @param brMap The holder for BufferReader objects for all files
     * @param ret
     * @return result data holder object
     * @throws java.io.IOException
     */
    protected ArrayList<HashMap> readSoilSites(HashMap brMap, HashMap ret) throws IOException {

        String slNotes = null;
        ArrayList<HashMap> sites = new ArrayList<HashMap>();
        HashMap site = new HashMap();
        ArrayList layers = new ArrayList();
        String line;
        BufferedReader brS;
        Object buf;
        HashMap mapS;
        LinkedHashMap formats = new LinkedHashMap();

        mapS = (HashMap) brMap.get("S");

        // If Soil File is no been found
        if (mapS.isEmpty()) {
            return sites;
        }

        sites = new ArrayList();

        for (Object key : mapS.keySet()) {

            buf = mapS.get(key);
            if (buf instanceof char[]) {
                brS = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brS = (BufferedReader) buf;
            }

            while ((line = brS.readLine()) != null) {

                // Get content type of line
                judgeContentType(line);

                // Read SOILS Info
                if (flg[0].equals("soil") && flg[2].equals("data")) {

                    slNotes = line.replaceFirst("\\*[Ss][Oo][Ii][Ll][Ss]?\\s*:?", "").trim();

                } // Read Site Info
                else if (flg[0].equals("site")) {

                    // header info
                    if (flg[1].equals("") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("soil_id", 10);
                        formats.put("sl_source", 13);  // P.S. sl_system is always "SCS" for DSSAT
                        formats.put("sltx", 6);
                        formats.put("sldp", 6);
                        formats.put("soil_name", 51);
                        // Read line and save into return holder
//                        sites.add(readLine(line.substring(1), formats));
                        site = readLine(line.substring(1), formats);
                        if (slNotes != null && !slNotes.equals("")) {
                            site.put("sl_notes", slNotes);
                        }
                        String sltx = MapUtil.getValueOr(site, "sltx", "");
                        if (!sltx.equals("")) {
                            site.put("sltx", transSltx(sltx));
                        }
                        sites.add(site);
                        layers = new ArrayList();
//                        ((HashMap) sites.get(sites.size() - 1)).put(layerKey, new ArrayList());

                    } // Site detail info
                    else if (flg[1].startsWith("site ") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("sl_loc_3", 12);
                        formats.put("sl_loc_1", 12);  // P.S. old key name ("scount")
                        formats.put("soil_lat", 10); // P.S. Definition changed 9 -> 10 (06/24)
                        formats.put("soil_long", 8); // P.S. Definition changed 9 -> 8  (06/24)
                        formats.put("classification", 51);    // P.S. "fd_name" for query using, not this time
                        // Read line and save into return holder
//                        ((HashMap) sites.get(sites.size() - 1)).putAll(readLine(line, formats));
                        site.putAll(readLine(line, formats));

                    } // soil info
                    else if (flg[1].startsWith("scom ") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("sscol", 6);
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
//                        ((HashMap) sites.get(sites.size() - 1)).putAll(readLine(line, formats));
                        site.putAll(readLine(line, formats));

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
                        formats.put("slbdm", 6);
                        formats.put("sloc", 6);
                        formats.put("slcly", 6);
                        formats.put("slsil", 6);
                        formats.put("slcf", 6);
                        formats.put("slni", 6);
                        formats.put("slphw", 6);
                        formats.put("slphb", 6);
                        formats.put("slcec", 6);
                        formats.put("sladc", 6);
                        // Read line and save into return holder
//                        addToArray((ArrayList) ((HashMap) sites.get(sites.size() - 1)).get(layerKey),
//                                readLine(line, formats),
//                                "sllb");
                        addToArray(layers, readLine(line, formats), "sllb");
                        site.put(layerKey, layers);

                    } // layer part two info
                    else if (flg[1].startsWith("slb  slpx ") && flg[2].equals("data")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("sllb", 6);
                        formats.put("slpx", 6);
                        formats.put("slpt", 6);
                        formats.put("slpo", 6);
                        if (flg[1].contains("caco3")) {
                            formats.put("caco3", 6);    // P.S. For old version of soil file
                        } else {
                            formats.put("slca", 6);
                        }
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
                        if (flg[1].contains("caco3")) {
                            formats.put("slca", 6);
                        }
                        // Read line and save into return holder
//                        addToArray((ArrayList) ((HashMap) sites.get(sites.size() - 1)).get(layerKey),
//                                readLine(line, formats),
//                                "sllb");
                        addToArray(layers, readLine(line, formats), "sllb");

                    } else {
                    }
                } else {
                }
            }
            brS.close();
        }

//        compressData(sites);
//        ret.put(jsonKey, sites);

        return sites;
    }

    /**
     * Set reading flgs for title lines (marked with *)
     *
     * @param line the string of reading line
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
