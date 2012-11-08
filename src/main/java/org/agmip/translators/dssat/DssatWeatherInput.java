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
     * Constructor with no parameters Set jsonKey as "weather"
     *
     */
    public DssatWeatherInput() {
        super();
        jsonKey = "weather";
    }

    /**
     * DSSAT Weather Data input method for only inputing weather file
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     */
    @Override
    protected HashMap readFile(HashMap brMap) throws IOException {
        HashMap ret = new HashMap();
        ArrayList<HashMap> files = readDailyData(brMap, new HashMap());
//        compressData(files);
        ret.put("weathers", files);

        return ret;
    }

    /**
     * DSSAT Weather Data input method for Controller using (return value will
     * not be compressed)
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     */
    protected ArrayList<HashMap> readDailyData(HashMap brMap, HashMap ret) throws IOException {

        ArrayList<HashMap> files = new ArrayList();
        ArrayList<HashMap<String, String>> daily;
        ArrayList titles;
        HashMap file;
        String line;
        BufferedReader brW = null;
        Object buf;
        HashMap mapW;
        LinkedHashMap formats = new LinkedHashMap();
        HashMap<String, ArrayList<HashMap<String, String>>> dailyById = new HashMap();

        mapW = (HashMap) brMap.get("W");

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
            file = new HashMap();
            daily = new ArrayList();
            titles = new ArrayList();

            while ((line = brW.readLine()) != null) {

                // Get content type of line
                judgeContentType(line);

                // Read Weather File Info
                if (flg[0].equals("weather") && flg[1].equals("") && flg[2].equals("data")) {

                    // header info
                    file.put("wst_name", line.replaceFirst("\\*[Ww][Ee][Aa][Tt][Hh][Ee][Rr]\\s*([Dd][Aa][Tt][Aa]\\s*)*:?", "").trim());

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
                        file.putAll(readLine(line, formats));
                        String wst_id = (String) file.get("wst_id");
                        String wst_name = (String) file.get("wst_name");
                        if (wst_id != null) {
                            if (wst_name != null) {
                                file.put("wst_name", wst_id + " " + wst_name);
                            } else {
                                file.put("wst_name", wst_id);
                            }
                        }

                    } // Weather daily data
                    else if (flg[1].startsWith("date ")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("w_date", 5);
                        for (int i = 0; i < titles.size(); i++) {
                            formats.put(titles.get(i), 6);
                        }
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats, "");
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

            if (!dailyById.containsKey(file.get("wst_id"))) {
                dailyById.put((String) file.get("wst_id"), daily);
                file.put("wst_source", "DSSAT");
                file.put(dailyKey, daily);
                files.add(file);
            } else {
                ArrayList tmpArr = dailyById.get(file.get("wst_id"));
//                tmpArr.addAll(daily);
                addDaily(tmpArr, daily);
            }
        }

        brW.close();

        return files;
    }

    /**
     * Set reading flgs for title lines (marked with *)
     *
     * @param line the string of reading line
     */
    @Override
    protected void setTitleFlgs(String line) {
        flg[0] = "weather";
        flg[1] = "";
        flg[2] = "data";
    }

    /**
     * Add new daily data into array with ascending order
     *
     * @param arr original array
     * @param cur new data for insert
     */
    private void addDaily(ArrayList<HashMap<String, String>> arr, ArrayList<HashMap<String, String>> cur) {
        if (arr.isEmpty()) {
            arr.addAll(cur);
        } else if (!cur.isEmpty()) {
            int curDay;
            int arrDay;
            try {
                curDay = Integer.parseInt(cur.get(cur.size() - 1).get("w_date"));
                arrDay = Integer.parseInt(arr.get(0).get("w_date"));
                if (curDay <= arrDay) {
                    ArrayList tmp = new ArrayList();
                    tmp.addAll(arr);
                    arr.clear();
                    arr.addAll(cur);
                    arr.addAll(tmp);
                    return;
                }
                curDay = Integer.parseInt(cur.get(0).get("w_date"));
                arrDay = Integer.parseInt(arr.get(arr.size() - 1).get("w_date"));
                if (curDay >= arrDay) {
                    ArrayList tmp = new ArrayList();
                    arr.addAll(cur);
                    return;
                }
                int yearEnd = 0;
                ArrayList before = new ArrayList();
                ArrayList after = new ArrayList();
                for (int i = 0; i < arr.size();) {
                    arrDay = Integer.parseInt(arr.get(i).get("w_date"));
                    if (arrDay < curDay) {
                        before.addAll(arr.subList(0, i));
                        after.addAll(arr.subList(i + 1, arr.size() - 1));
                        break;
                    } else {
                        if ((i / 1000) % 4 == 0) {
                            i += 366;
                        } else {
                            i += 365;
                        }
                    }
                }
                if (after.isEmpty()) {
                    arr.addAll(cur);
                } else {
                    arr.clear();
                    arr.addAll(before);
                    arr.addAll(cur);
                    arr.addAll(after);
                }
            } catch (NumberFormatException e) {
                arr.addAll(cur);
            }
        }
    }
}
