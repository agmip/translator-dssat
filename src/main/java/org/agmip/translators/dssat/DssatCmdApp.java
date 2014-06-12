package org.agmip.translators.dssat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.agmip.acmo.util.AcmoUtil;
import org.agmip.common.Functions;
import org.agmip.util.JSONAdapter;
import org.agmip.util.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class DssatCmdApp {

    private static boolean isCompressed = false;
    private static boolean isToModel = false;
    private static ArrayList<String> inputPaths = new ArrayList();
    private static String outputPath = getOutputPath("");
    private static final Logger LOG = LoggerFactory.getLogger(DssatCmdApp.class);

    public static void main(String... args) throws IOException {
        readCommand(args);
        if (inputPaths == null) {
            LOG.error("There is no valid input path, please check input arguments");
            return;
        }
        if (isToModel) {
            LOG.info("Translate {} to DSSAT...", inputPaths);
            DssatControllerOutput translator = new DssatControllerOutput();
            HashMap data = readJson();
            translator.writeFile(outputPath, data);
            if (isCompressed) {
                translator.createZip(new File(outputPath + File.separator + "DSSAT_Input.zip"));
            }
            writeAcmo(data);
        } else {
            LOG.info("Translate {} to JSON...", inputPaths.get(0));
            DssatControllerInput translator = new DssatControllerInput();
            Map result = translator.readFile(inputPaths.get(0));
            BufferedOutputStream bo;
            outputPath += File.separator;
            File f = new File(outputPath + new File(outputPath).getName().replaceAll("\\.\\w+$", ".json"));
            bo = new BufferedOutputStream(new FileOutputStream(f));

            // Output json for reading
            bo.write(JSONAdapter.toJSON(result).getBytes());
            bo.flush();
            bo.close();
        }
        LOG.info("Job done!");
    }

    private static void readCommand(String[] args) {

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-zip")) {
                isCompressed = true;
            } else if (args[i].toUpperCase().endsWith(".JSON")) {
                isToModel = true;
                inputPaths.add(args[i]);
            } else if (args[i].toUpperCase().endsWith(".ZIP")) {
                isToModel = false;
                inputPaths.add(args[i]);
            } else {
                outputPath = getOutputPath(args[i]);
            }
        }
        LOG.info("Read from {}", inputPaths);
        LOG.info("Output to {}", outputPath);
    }

    private static HashMap readJson() throws IOException {
        HashMap data = new HashMap();
        for (String in : inputPaths) {
            HashMap tmp = JSONAdapter.fromJSONFile(in);
            conbineData(data, tmp, "experiments");
            conbineData(data, tmp, "soils");
            conbineData(data, tmp, "weathers");
        }
        fixData(data);
        return data;
    }

    private static void conbineData(Map to, Map from, String key) {

        ArrayList fromArr = (ArrayList) from.get(key);
        if (fromArr != null) {
            ArrayList toArr = (ArrayList) to.get(key);
            if (toArr == null) {
                to.put(key, fromArr);
            } else {
                toArr.addAll(fromArr);
            }
        }
    }

    private static String getOutputPath(String arg) {

        String path;
        if (arg != null && !"".equals(arg)) {
            path = new File(arg).getPath() + File.separator + "DSSAT";
        } else {
            path = "DSSAT";
        }

        File dir = new File(path);
        int cnt = 1;
        while (dir.exists() && dir.list().length > 0) {
            dir = new File(path + "-" + cnt);
            cnt++;
        }
        return dir.getPath();
    }

    private static void fixData(Map data) {
        ArrayList<HashMap> exps = MapUtil.getObjectOr(data, "experiments", new ArrayList());
        HashSet soilIds = getIds(MapUtil.getObjectOr(data, "soils", new ArrayList()), "soil_id");
        HashSet wstIds = getIds(MapUtil.getObjectOr(data, "weathers", new ArrayList()), "wst_id");
        for (HashMap exp : exps) {
            fixDataLink(exp, soilIds, "soil_id", 10);
            fixDataLink(exp, wstIds, "wst_id", 4);
            fixEventDate(exp);
            fixSimCtrl(exp);
        }
    }

    private static HashSet<String> getIds(ArrayList<HashMap> arr, String idName) {
        HashSet ids = new HashSet();
        for (HashMap data : arr) {
            String id = (String) data.get(idName);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static void fixDataLink(HashMap expData, HashSet ids, String idName, int adjustLength) {

        String id = (String) expData.get(idName);
        boolean isIdFixed = false;
        if (id != null && !ids.isEmpty()) {
            while (!ids.contains(id)) {
                if (id.length() > adjustLength) {
                    id = id.substring(0, adjustLength);
                    isIdFixed = true;
                } else {
                    isIdFixed = false;
                    break;
                }
            }
        }
        if (isIdFixed) {
            LOG.info("Fix {} to {} for experiment {}", idName, id, MapUtil.getValueOr(expData, "exname", "N/A"));
            expData.put(idName, id);
        }
    }

    private static void fixEventDate(HashMap expData) {
        ArrayList<HashMap<String, String>> events = MapUtil.getBucket(expData, "management").getDataList();
        String pdate = getPdate(events);
        if (pdate == null) {
            LOG.warn("Planting date is missing, will quit fixing event date");
            return;
        }
        String exname = MapUtil.getValueOr(expData, "exname", "N/A");
        for (HashMap<String, String> event : events) {
            String date = event.get("date");
            if (date != null && date.length() != 8) {
                String newDate = Functions.dateOffset(pdate, date);
                event.put("date", newDate);
                LOG.info("Fix {} date to {} for experiment {}", event.get("event"), newDate, exname);
            }
        }
    }

    private static String getPdate(ArrayList<HashMap<String, String>> events) {
        for (HashMap<String, String> event : events) {
            if ("planting".equals(event.get("event"))) {
                return event.get("date");
            }
        }
        return null;
    }

    private static void writeAcmo(HashMap data) {
        AcmoUtil.writeAcmo(outputPath, data, "dssat", new HashMap());
    }

    private static void fixSimCtrl(HashMap expData) {
        String defValC = "-99";
        String defValI = "-99";
        String defValD = "-99";
        String defValR = "-99";
        HashMap smData = MapUtil.getObjectOr(expData, "dssat_simulation_control", new HashMap());
        ArrayList<HashMap> smArr = MapUtil.getObjectOr(smData, "data", new ArrayList());
        for (HashMap data : smArr) {
            if (!data.containsKey("sm_general")) {
                HashMap smCtrls = MapUtil.getObjectOr(data, "general", new HashMap());
                if (!smCtrls.isEmpty()) {
                    String smStr = String.format("%1$-11s %2$5s %3$5s %4$5s %5$5s %6$5s %7$-25s %8$s",
                            MapUtil.getValueOr(smCtrls, "general", "GE"),
                            MapUtil.getValueOr(smCtrls, "nyers", defValI),
                            MapUtil.getValueOr(smCtrls, "nreps", defValI),
                            MapUtil.getValueOr(smCtrls, "start", defValC).toString(),
                            getDate(smCtrls, "sdate", "sdyer","sdday", defValD),
                            MapUtil.getValueOr(smCtrls, "rseed", defValI),
                            MapUtil.getValueOr(smCtrls, "sname", defValC),
                            MapUtil.getValueOr(smCtrls, "model", defValC));
                    data.put("sm_general", smStr);
                }
            }
            if (!data.containsKey("sm_options")) {
                HashMap smCtrls = MapUtil.getObjectOr(data, "options", new HashMap());
                if (!smCtrls.isEmpty()) {
                    String smStr = String.format("%1$-11s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s",
                            MapUtil.getValueOr(smCtrls, "options", "OP"),
                            MapUtil.getValueOr(smCtrls, "water", defValC),
                            MapUtil.getValueOr(smCtrls, "nitro", defValC),
                            MapUtil.getValueOr(smCtrls, "symbi", defValC),
                            MapUtil.getValueOr(smCtrls, "phosp", defValC),
                            MapUtil.getValueOr(smCtrls, "potas", defValC),
                            MapUtil.getValueOr(smCtrls, "dises", defValC),
                            MapUtil.getValueOr(smCtrls, "chem", defValC),
                            MapUtil.getValueOr(smCtrls, "till", defValC),
                            MapUtil.getValueOr(smCtrls, "co2", defValC));
                    data.put("sm_options", smStr);
                }
            }
            if (!data.containsKey("sm_methods")) {
                HashMap smCtrls = MapUtil.getObjectOr(data, "methods", new HashMap());
                if (!smCtrls.isEmpty()) {
                    String smStr = String.format("%1$-11s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s",
                            MapUtil.getValueOr(smCtrls, "methods", "ME"),
                            MapUtil.getValueOr(smCtrls, "wther", defValC),
                            MapUtil.getValueOr(smCtrls, "incon", defValC),
                            MapUtil.getValueOr(smCtrls, "light", defValC),
                            MapUtil.getValueOr(smCtrls, "evapo", defValC),
                            MapUtil.getValueOr(smCtrls, "infil", defValC),
                            MapUtil.getValueOr(smCtrls, "photo", defValC),
                            MapUtil.getValueOr(smCtrls, "hydro", defValC),
                            MapUtil.getValueOr(smCtrls, "nswit", defValC),
                            MapUtil.getValueOr(smCtrls, "mesom", defValC),
                            MapUtil.getValueOr(smCtrls, "mesev", defValC),
                            MapUtil.getValueOr(smCtrls, "mesol", defValC));
                    data.put("sm_methods", smStr);
                }
            }
            if (!data.containsKey("sm_management")) {
                HashMap smCtrls = MapUtil.getObjectOr(data, "management", new HashMap());
                if (!smCtrls.isEmpty()) {
                    String smStr = String.format("%1$-11s %2$5s %3$5s %4$5s %5$5s %6$5s",
                            MapUtil.getValueOr(smCtrls, "management", "MA"),
                            MapUtil.getValueOr(smCtrls, "plant", defValC),
                            MapUtil.getValueOr(smCtrls, "irrig", defValC),
                            MapUtil.getValueOr(smCtrls, "ferti", defValC),
                            MapUtil.getValueOr(smCtrls, "resid", defValC),
                            MapUtil.getValueOr(smCtrls, "harvs", defValC));
                    data.put("sm_management", smStr);
                }
            }
            if (!data.containsKey("sm_outputs")) {
                HashMap smCtrls = MapUtil.getObjectOr(data, "outputs", new HashMap());
                if (!smCtrls.isEmpty()) {
                    String smStr = String.format("%1$-11s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s %9$5s %10$5s %11$5s %12$5s %13$5s %14$5s",
                            MapUtil.getValueOr(smCtrls, "outputs", "OU"),
                            MapUtil.getValueOr(smCtrls, "fname", defValC),
                            MapUtil.getValueOr(smCtrls, "ovvew", defValC),
                            MapUtil.getValueOr(smCtrls, "sumry", defValC),
                            MapUtil.getValueOr(smCtrls, "fropt", defValI),
                            MapUtil.getValueOr(smCtrls, "grout", defValC),
                            MapUtil.getValueOr(smCtrls, "caout", defValC),
                            MapUtil.getValueOr(smCtrls, "waout", defValC),
                            MapUtil.getValueOr(smCtrls, "niout", defValC),
                            MapUtil.getValueOr(smCtrls, "miout", defValC),
                            MapUtil.getValueOr(smCtrls, "diout", defValC),
                            MapUtil.getValueOr(smCtrls, "vbose", defValC),
                            MapUtil.getValueOr(smCtrls, "chout", defValC),
                            MapUtil.getValueOr(smCtrls, "opout", defValC));
                    data.put("sm_outputs", smStr);
                }
            }
            if (!data.containsKey("sm_planting")) {
                HashMap smCtrls = MapUtil.getObjectOr(data, "planting", new HashMap());
                if (!smCtrls.isEmpty()) {
                    String smStr = String.format("%1$-11s %2$5s %3$5s %4$5s %5$5s %6$5s %7$5s %8$5s",
                            MapUtil.getValueOr(smCtrls, "planting", "PL"),
                            getDate(smCtrls, "pfrst", "pfyer","pfday", defValD),
                            getDate(smCtrls, "plast", "plyer","plday", defValD),
                            MapUtil.getValueOr(smCtrls, "ph2ol", defValR),
                            MapUtil.getValueOr(smCtrls, "ph2ou", defValR),
                            MapUtil.getValueOr(smCtrls, "ph2od", defValR),
                            MapUtil.getValueOr(smCtrls, "pstmx", defValR),
                            MapUtil.getValueOr(smCtrls, "pstmn", defValR));
                    data.put("sm_planting", smStr);
                }
            }
            if (!data.containsKey("sm_irrigation")) {
                HashMap smCtrls = MapUtil.getObjectOr(data, "irrigation", new HashMap());
                if (!smCtrls.isEmpty()) {
                    String smStr = String.format("%1$-11s %2$5s %3$5s %4$5s %5$-5s %6$-5s %7$5s %8$5s",
                            MapUtil.getValueOr(smCtrls, "irrigation", "IR"),
                            MapUtil.getValueOr(smCtrls, "imdep", defValR),
                            MapUtil.getValueOr(smCtrls, "ithrl", defValR),
                            MapUtil.getValueOr(smCtrls, "ithru", defValR),
                            MapUtil.getValueOr(smCtrls, "iroff", defValC),
                            MapUtil.getValueOr(smCtrls, "imeth", defValC),
                            MapUtil.getValueOr(smCtrls, "iramt", defValR),
                            MapUtil.getValueOr(smCtrls, "ireff", defValR));
                    data.put("sm_irrigation", smStr);
                }
            }
            if (!data.containsKey("sm_nitrogen")) {
                HashMap smCtrls = MapUtil.getObjectOr(data, "nitrogen", new HashMap());
                if (!smCtrls.isEmpty()) {
                    String smStr = String.format("%1$-11s %2$5s %3$5s %4$5s %5$-5s %6$-5s",
                            MapUtil.getValueOr(smCtrls, "nitrogen", "NI"),
                            MapUtil.getValueOr(smCtrls, "nmdep", defValR),
                            MapUtil.getValueOr(smCtrls, "nmthr", defValR),
                            MapUtil.getValueOr(smCtrls, "namnt", defValR),
                            MapUtil.getValueOr(smCtrls, "ncode", defValC),
                            MapUtil.getValueOr(smCtrls, "naoff", defValC));
                    data.put("sm_nitrogen", smStr);
                }
            }
            if (!data.containsKey("sm_residues")) {
                HashMap smCtrls = MapUtil.getObjectOr(data, "residues", new HashMap());
                if (!smCtrls.isEmpty()) {
                    String smStr = String.format("%1$-11s %2$5s %3$5s %4$5s",
                            MapUtil.getValueOr(smCtrls, "residues", "RE"),
                            MapUtil.getValueOr(smCtrls, "ripcn", defValR),
                            MapUtil.getValueOr(smCtrls, "rtime", defValI),
                            MapUtil.getValueOr(smCtrls, "ridep", defValR));
                    data.put("sm_residues", smStr);
                }
            }
            if (!data.containsKey("sm_harvests")) {
                HashMap smCtrls = MapUtil.getObjectOr(data, "harvests", new HashMap());
                if (!smCtrls.isEmpty()) {
                    String smStr = String.format("%1$-11s %2$5s %3$5s %4$5s %5$5s",
                            MapUtil.getValueOr(smCtrls, "harvests", "HA"),
                            MapUtil.getValueOr(smCtrls, "hfrst", defValD),
                            getDate(smCtrls, "hlast", "hlyer","hlday", defValD),
                            MapUtil.getValueOr(smCtrls, "hpcnp", defValR),
                            MapUtil.getValueOr(smCtrls, "hpcnr", defValR));
                    data.put("sm_harvests", smStr);
                }
            }
        }
    }
    
    private static String getDate(HashMap data, String dateId, String yearId, String dayId, String defVal) {
        String date = DssatCommonOutput.formatDateStr(MapUtil.getValueOr(data, dateId, defVal));
        if (defVal.equals(date)) {
            date = String.format("%1$02d%2$03d",
                    new BigDecimal(MapUtil.getValueOr(data, yearId, "0")).intValue(),
                    new BigDecimal(MapUtil.getValueOr(data, dayId, "0")).intValue());
            if ("00000".equals(date)) {
                date = defVal;
            }
        }
        return date;
    }
}
