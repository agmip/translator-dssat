package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * DSSAT Weather Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatWeatherInput extends DssatCommonInput {

    public String dailyKey = "dailyWeather";  // P.S. the key name might change

    /**
     * Constructor with no parameters
     * Set jsonKey as "weather"
     * 
     */
    public DssatWeatherInput() {
        super();
        jsonKey = "weather";
    }

    /**
     * DSSAT Weather Data input method for only inputing weather file
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    @Override
    protected ArrayList<LinkedHashMap> readFile(HashMap brMap) throws IOException {
        LinkedHashMap metaData = new LinkedHashMap();
        ArrayList<LinkedHashMap> files = readDailyData(brMap, metaData);
//        compressData(files);
        ArrayList<LinkedHashMap> ret = new ArrayList();
        for (int i = 0; i < files.size(); i++) {
            LinkedHashMap tmp = new LinkedHashMap();
            tmp.put(jsonKey, files.get(i));
            ret.add(tmp);
        }

        return ret;
    }

    /**
     * DSSAT Weather Data input method for Controller using (return value will not be compressed)
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    protected ArrayList<LinkedHashMap> readDailyData(HashMap brMap, LinkedHashMap ret) throws IOException {

        ArrayList<LinkedHashMap> files = new ArrayList();
        ArrayList<LinkedHashMap> daily = new ArrayList();
        ArrayList titles = new ArrayList();
        LinkedHashMap fileTmp = null;
        LinkedHashMap file = null;
        String line;
        BufferedReader brW = null;
        Object buf;
        LinkedHashMap mapW;
        LinkedHashMap formats = new LinkedHashMap();

        mapW = (LinkedHashMap) brMap.get("W");

        // If Weather File is no been found
        if (mapW.isEmpty()) {
            return files;
        }

        for (Object key : mapW.keySet()) {

            buf = mapW.get(key);
            if (buf instanceof char[]) {
                brW = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brW = (BufferedReader) buf;
            }
            fileTmp = new LinkedHashMap();
            daily = new ArrayList();
            titles = new ArrayList();

            while ((line = brW.readLine()) != null) {

                // Get content type of line
                judgeContentType(line);

                // Read Weather File Info
                if (flg[0].equals("weather") && flg[1].equals("") && flg[2].equals("data")) {

                    // header info
                    fileTmp.put("wst_name", line.replaceFirst("\\*[Ww][Ee][Aa][Tt][Hh][Ee][Rr]\\s*([Dd][Aa][Tt][Aa]\\s*)*:?", "").trim());

                } // Read Weather Data
                else if (flg[2].equals("data")) {

                    // Weather station info
                    if (flg[1].contains("insi ")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("wst_id", 6);
                        formats.put("wst_lat", 9);
                        formats.put("wst_long", 9);
                        formats.put("elev", 6);
                        formats.put("tav", 6);
                        formats.put("tamp", 6);
                        formats.put("refht", 6);
                        formats.put("wndht", 6);
                        // Read line and save into return holder
                        fileTmp.putAll(readLine(line, formats));

                    } // Weather daily data
                    else if (flg[1].startsWith("date ")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("w_date", 5);
                        for (int i = 0; i < titles.size(); i++) {
                            formats.put(titles.get(i), 6);
                        }
                        // Read line and save into return holder
                        LinkedHashMap tmp = readLine(line, formats, "");
                        // translate date from yyddd format to yyyymmdd format
                        translateDateStr(tmp, "w_date");

                        daily.add(tmp);

                    } else {
                    }
                } // Data Title Info
                else if (flg[2].equals("title")) {
                    // Dialy Data Title
                    if (flg[1].startsWith("date ")) {
                        for (int i = 6; i < line.length(); i += 6) {
                            String title = line.substring(i, Math.min(i + 6, line.length())).trim();
                            if (title.equalsIgnoreCase("DEWP")) {
                                titles.add("tdew");
                            } else if (title.equalsIgnoreCase("PAR")) {
                                titles.add("pard");
                            } else if (title.equals("")) {
                                titles.add("null" + i);
                            } else {
                                titles.add(title.toLowerCase());
                            }
                        }
                    } else {
                    }
                } else {
                }
            }

            if (file == null || !file.get("wst_id").equals(fileTmp.get("wst_id"))) {
                file = fileTmp;
                fileTmp.put(dailyKey, daily);
                files.add(file);
            } else {
                ArrayList tmpArr = (ArrayList) file.get(dailyKey);
                for (int i = 0; i < daily.size(); i++) {
                    tmpArr.add(daily.get(i));
                }
            }
        }

        brW.close();

        return files;
    }

    /**
     * Set reading flgs for title lines (marked with *)
     * 
     * @param line  the string of reading line
     */
    @Override
    protected void setTitleFlgs(String line) {
        flg[0] = "weather";
        flg[1] = "";
        flg[2] = "data";
    }
}
