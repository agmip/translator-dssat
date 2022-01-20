package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT Weather Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatWeatherInput extends DssatCommonInput {

    private static final Logger LOG = LoggerFactory.getLogger(DssatWeatherInput.class);
    public static final String dailyKey = "dailyWeather";  // P.S. the key name might change

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
     * @throws java.io.IOException
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
     * @param ret
     * @return result data holder object
     * @throws java.io.IOException
     */
    protected ArrayList<HashMap> readDailyData(HashMap brMap, HashMap ret) throws IOException {

        ArrayList<HashMap> files = new ArrayList();
        ArrayList<HashMap<String, String>> daily;
        ArrayList titles;
        HashMap file;
        String line;
        BufferedReader brW;
        Object buf;
        HashMap mapW;
        LinkedHashMap formats = new LinkedHashMap();
        HashMap<String, ArrayList<HashMap<String, String>>> dailyById = new HashMap();
        String fileName;
        boolean is4Y = false;

        mapW = (HashMap) brMap.get("W");

        // If Weather File is no been found
        if (mapW.isEmpty()) {
            return files;
        }

        for (Object key : mapW.keySet()) {

            fileName = (String) key;
            if (fileName.lastIndexOf(".") > 0) {
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
            }
            String wst_id;
            String clim_id;
            if (fileName.length() == 8 && !fileName.matches("\\w{4}\\d{4}$")) {
                wst_id = fileName;
                clim_id = fileName.substring(4);
            } else {
                wst_id = fileName.substring(0 ,4);
                clim_id = "0XXX";
            }
            
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
                if (flg[0].equals("weather") && flg[1].isEmpty() && flg[2].equals("data")) {

                    // header info
                    if (line.startsWith("$")) {
                        file.put("wst_notes", line.replaceFirst("\\$[Ww][Ee][Aa][Tt][Hh][Ee][Rr]\\s*([Dd][Aa][Tt][Aa]\\s*)*:?", "").trim());
                        is4Y = true;
                    } else {
                        file.put("wst_notes", line.replaceFirst("\\*[Ww][Ee][Aa][Tt][Hh][Ee][Rr]\\s*([Dd][Aa][Tt][Aa]\\s*)*:?", "").trim());
                    }

                } // Read Weather Data
                else if (flg[2].equals("data")) {

                    // Weather station info
                    if (flg[1].contains("insi ")) {

                        // Set variables' formats
                        formats.clear();
                        String[] names = flg[1].split("\\s+");
                        for (int i = 0; i < names.length; i++) {
                            if (names[i].equalsIgnoreCase("INSI")) {
                                formats.put("dssat_insi", 6);
                            } else if (names[i].equalsIgnoreCase("LAT")) {
                                formats.put("wst_lat", 9);
                            } else if (names[i].equalsIgnoreCase("LONG")) {
                                formats.put("wst_long", 9);
                            } else if (names[i].equalsIgnoreCase("ELEV")) {
                                formats.put("wst_elev", 6);
                            } else if (names[i].equalsIgnoreCase("TAV")) {
                                formats.put("tav", 6);
                            } else if (names[i].equalsIgnoreCase("AMP")) {
                                formats.put("tamp", 6);
                            } else if (names[i].equalsIgnoreCase("REFHT")) {
                                formats.put("refht", 6);
                            } else if (names[i].equalsIgnoreCase("WNDHT")) {
                                formats.put("wndht", 6);
                            } else if (names[i].equalsIgnoreCase("CO2")) {
                                formats.put("co2y", 6);
                            } else if (names[i].equalsIgnoreCase("CCO2")) {
                                formats.put("co2y", 6);
                            } else {
                                int end = flg[1].indexOf(names[i]) + names[i].length();
                                int start = 0;
                                if (i > 0) {
                                    start = flg[1].indexOf(names[i - 1]) + names[i - 1].length();
                                }
                                formats.put(names[i].toLowerCase(), end - start);
                            }
                        }

                        // Read line and save into return holder
                        file.putAll(readLine(line, formats));
                        // Check if the WST_ID inside file is matching with the file name
                        String dssat_insi = (String) file.get("dssat_insi");
                        if (dssat_insi == null || !wst_id.startsWith(dssat_insi)) {
                            LOG.warn("The name of weather file [{}] does not match with the INSI ({}) in the file.", key, dssat_insi);
                        }
//                        if (wst_name != null) {
//                            file.put("wst_name", fileName + " " + wst_name);
//                        } else {
//                            file.put("wst_name", fileName);
//                        }

                    } // Weather daily data
                    else if (flg[1].startsWith("date ")) {

                        // Set variables' formats
                        formats.clear();
                        if (is4Y) {
                            formats.put("w_date", 7);
                        } else {
                            formats.put("w_date", 5);
                        }
                        for (Object title : titles) {
                            formats.put(title, 6);
                        }
                        // Read line and save into return holder
                        HashMap tmp = readLine(line, formats);
                        // translate date from yyddd format to yyyymmdd format
                        translateDateStr(tmp, "w_date");
                        daily.add(tmp);

                    } else {
                    }
                } // Data Title Info
                else if (flg[2].equals("title")) {
                    // Dialy Data Title
                    if (flg[1].startsWith("date ")) {
                        int startDigit = 6;
                        if (is4Y) {
                            startDigit = 8;
                        }
                        for (int i = startDigit; i < line.length(); i += 6) {
                            String title = line.substring(i, Math.min(i + 6, line.length())).trim();
                            if (title.equalsIgnoreCase("DEWP")) {
                                titles.add("tdew");
                            } else if (title.equalsIgnoreCase("PAR")) {
                                titles.add("pard");
                            } else if (title.equalsIgnoreCase("VPRS")) {
                                titles.add("vprsd");
                            } else if (title.equalsIgnoreCase("RHUM")) {
                                titles.add("rhumd");
                            } else if (title.equals("")) {
                                titles.add("null" + i);
                            } else {
                                titles.add(title.toLowerCase());
                            }
                        }
                    } else {
                    }
//                } else if (flg[2].equals("comment")) {
//                    if (line.startsWith("!Climate ID: ")) {
//                        clim_id = line.substring(13).trim();
//                        if (clim_id.toUpperCase().equals("N/A")) {
//                            clim_id = "";
//                        }
//                    }
                } else {
                }
            }

            // Double check if the weather file name is following the old standard
            if (!daily.isEmpty() && wst_id.length() == 4) {
                String firstDay = daily.get(0).get("w_date");
                if (firstDay != null && firstDay.length() > 3) {
                    int year = 80;
                    try {
                        year = Integer.parseInt(firstDay.substring(2, 4));
                    } catch (NumberFormatException e) {
                    }
                    if (!fileName.startsWith(wst_id + String.format("%02d", year))) {
                        wst_id = fileName;
                        clim_id = fileName.substring(4);
                    }
                }
            }
            file.put("wst_name", wst_id);
            file.put("wst_id", wst_id);
            file.put("clim_id", clim_id);

            if (!dailyById.containsKey(wst_id)) {
                dailyById.put(wst_id, daily);
                file.put("wst_source", "DSSAT");
                file.put(dailyKey, daily);
                files.add(file);
            } else {
                ArrayList tmpArr = dailyById.get(wst_id);
//                tmpArr.addAll(daily);
                addDaily(tmpArr, daily);
            }
            brW.close();
        }

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
                    arr.addAll(cur);
                    return;
                }
                ArrayList before = new ArrayList();
                ArrayList after = new ArrayList();
                for (int i = 0; i < arr.size();) {
                    arrDay = Integer.parseInt(arr.get(i).get("w_date"));
                    if (arrDay > curDay) {
                        before.addAll(arr.subList(0, i));
                        after.addAll(arr.subList(i, arr.size()));
                        break;
                    } else {
                        // Get the index of the first day of next year
                        int curYear = arrDay / 10000;
                        int start = i;
                        i += 365;
                        if (curYear % 4 == 0) {
                            i++;
                        }
                        if (i >=  arr.size()) {
                            i = arr.size() - 1;
                        }
                        int lastDay = Integer.parseInt(arr.get(i).get("w_date"));
                        int lastDayYear = lastDay / 10000;
                        if (i == arr.size() - 1 && curYear == lastDayYear) {
                            break;
                        } else {
                            while (i > start + 1) {
                                lastDay = Integer.parseInt(arr.get(i - 1).get("w_date"));
                                lastDayYear = lastDay / 10000;
                                if (curYear == lastDayYear) {
                                    break;
                                }
                                i--;
                            }
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
