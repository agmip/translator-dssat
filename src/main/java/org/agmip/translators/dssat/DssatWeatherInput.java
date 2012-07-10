package org.agmip.translators.dssat;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.agmip.core.types.Weather;
import java.lang.reflect.*;

/**
 * DSSAT Weather Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatWeatherInput extends DssatCommonInput {

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
     * DSSAT Weather Data input method for Controller using
     * 
     * @param brMap  The holder for BufferReader objects for all files
     * @return result data holder object
     */
    @Override
    protected Experiment readFile(HashMap brMap) throws IOException {

        LinkedHashMap ret = new LinkedHashMap();
        Weather wth = new Weather();
        ArrayList files = new ArrayList();
        ArrayList daily = new ArrayList();
        ArrayList titles = new ArrayList();
        LinkedHashMap file;
        String line;
        BufferedReader brW = null;
        Object buf;
        LinkedHashMap mapW;
        LinkedHashMap formats = new LinkedHashMap();
        String dailyKey = "data";  // P.S. the key name might change

        mapW = (LinkedHashMap) brMap.get("W");

        // If Weather File is no been found
        if (mapW.isEmpty()) {
            // TODO reprot file not exist error
            return ret;
        }

        for (Object key : mapW.keySet()) {

            buf = mapW.get(key);
            if (buf instanceof char[]) {
                brW = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brW = (BufferedReader) buf;
            }
            file = new LinkedHashMap();
            daily = new ArrayList();
            titles = new ArrayList();
            files.add(file);

            while ((line = brW.readLine()) != null) {

                // Get content type of line
                judgeContentType(line);

                // Read Weather File Info
                if (flg[0].equals("weather") && flg[1].equals("") && flg[2].equals("data")) {

                    // header info
                    wth.setWst_name = line.replaceFirst("\\*[Ww][Ee][Aa][Tt][Hh][Ee][Rr]\\s*[Dd][Aa][Tt][Aa]\\s*:?", "").trim();

                } // Read Weather Data
                else if (flg[2].equals("data")) {

                    // Weather station info
                    if (flg[1].contains("insi ")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("wst_insi", 6);
                        formats.put("wst_lat", 9);
                        formats.put("wst_long", 9);
                        formats.put("elev", 6);
                        formats.put("tav", 6);
                        formats.put("tamp", 6);
                        formats.put("refht", 6);
                        formats.put("wndht", 6);
                        // Read line and save into return holder
                        Map insi = readLine(line, formats);
                        wth.setWst_insi = insi.get("wst_insi");
                        wth.setWst_lat = Double.parseDouble(insi.get("wst_lat"));
                        wth.setWst_long = Double.parseDouble(insi.get("wst_long"));
                        wth.setElev =  Double.parseDouble(insi.get("elev"));
                        wth.setTav  =  Double.parseDouble(insi.get("tav"));
                        wth.setRefht = Double.parseDouble(insi.get("refht"));
                        wth.setWndht = Double.parseDouble(insi.get("wndht"));

                    } // Weather daily data
                    else if (flg[1].startsWith("date ")) {

                        // Set variables' formats
                        formats.clear();
                        formats.put("w_date", 5);
                        for (int i = 0; i < titles.size(); i++) {
                            formats.put(titles.get(i), 6);
                        }
                        // Read line and save into return holder
                        Map tmp = readLine(line, formats, "");
                        Weather.DailyWeather t2 = new Weather.DailyWeather();
                        // translate date from yyddd format to yyyymmdd format
                        translateDateStr(tmp, "w_date");
                        t2.setW_date(tmp.get("w_date"));

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
            
            file.put(dailyKey, daily);
        }

        compressData(files);
        ret.put(jsonKey, files);
        brW.close();

        return ret;
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

    protected static void setDailyVars(Map m, DailyWeather w) {
        Class noparams[] = {};
        Class[] paramString = new Class[1];	
        paramString[0] = String.class;
 
        Class[] paramInt = new Class[1];	
        paramInt[0] = Integer.TYPE;

        Class[] paramDbl = new Class[1];
        paramDbl[0] = Double.TYPE;

        Class cls = w.getClass();
        for(Map.Entry entry : m.entrySet()) {
            String key = entry.getKey();
            String method = key.substring(0,1).toUpperCase()+method.substring(1);
            Method getter = cls.getDeclaredMethod("get"+method, noparams);
            String type = getter.getReturnType().getName();
            if ( type.equals("Integer") ) {
                Method setter = cls.getDeclaredMethod("set"+method, paramInt);
                setter.invoke(w, Integer.parseInt(entry.getValue()));
            } else if ( type.equals("Double") ) {
                Method setter = cls.getDeclaredMethod("set"+method, paramDbl);
                setter.invoke(w, Double.parseDouble(entry.getValue()));
            } else {
                Method setter = cls.getDeclaredMethod("set"+method, paramString);
                setter.invoke(w, entry.getValue());
            }
        }
    }
}
