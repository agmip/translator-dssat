package org.agmip.translators.dssat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.agmip.translators.dssat.DssatBatchFileOutput.DssatVersion;
import static org.agmip.translators.dssat.DssatCommonInput.getSectionDataWithNocopy;
import static org.agmip.translators.dssat.DssatCommonOutput.revisePath;
import static org.agmip.util.MapUtil.*;
import org.agmip.util.MapUtil.BucketEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class DssatControllerOutput extends DssatCommonOutput {

    private final HashMap<String, File> files = new HashMap();
    private final HashMap<String, Future<File>> futFiles = new HashMap();
//    private HashMap<String, Future<File>> soilFiles = new HashMap();
//    private HashMap<String, Future<File>> wthFiles = new HashMap();
    private final HashMap<String, Map> soilData = new HashMap();
    private final HashMap<String, Map> wthData = new HashMap();
    private ExecutorService executor = Executors.newFixedThreadPool(64);
    private static final Logger LOG = LoggerFactory.getLogger(DssatControllerOutput.class);
//    private ArrayList<File> 

    public DssatControllerOutput() {
        soilHelper = new DssatSoilFileHelper();
        wthHelper = new DssatWthFileHelper();
    }

    /**
     * ALL DSSAT Data Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeMultipleExp(String arg0, Map result) throws FileNotFoundException, IOException {

        arg0 = revisePath(arg0);
        ArrayList<HashMap> expArr = getObjectOr(result, "experiments", new ArrayList());
        ArrayList<HashMap> soilArr = getObjectOr(result, "soils", new ArrayList());
        ArrayList<HashMap> wthArr = getObjectOr(result, "weathers", new ArrayList());

        // Setup output file
        Calendar cal = Calendar.getInstance();
        if (!expArr.isEmpty() && soilArr.isEmpty() && wthArr.isEmpty()) {
            outputFile = new File(arg0 + "AGMIP_DSSAT_EXPERIMENTS_" + cal.getTimeInMillis() + ".zip");
        } else if (expArr.isEmpty() && !soilArr.isEmpty() && wthArr.isEmpty()) {
            outputFile = new File(arg0 + "AGMIP_DSSAT_SOILS_" + cal.getTimeInMillis() + ".zip");
        } else if (expArr.isEmpty() && soilArr.isEmpty() && !wthArr.isEmpty()) {
            outputFile = new File(arg0 + "AGMIP_DSSAT_WEATHERS_" + cal.getTimeInMillis() + ".zip");
        } else {
            outputFile = new File(arg0 + "AGMIP_DSSAT_" + cal.getTimeInMillis() + ".zip");
        }

        // Write files
        String soil_id;
        String wth_id;
        expArr = combineExps(expArr);
        for (HashMap expData : expArr) {
            ArrayList<HashMap> rootArr = getObjectOr(expData, "dssat_root", new ArrayList());
            if (rootArr.isEmpty()) {
                soil_id = getObjectOr(expData, "soil_id", "");
                wth_id = getObjectOr(expData, "wst_id", "");
                expData.put("soil", getSectionDataWithNocopy(soilArr, "soil_id", soil_id));
                expData.put("weather", getSectionDataWithNocopy(wthArr, "wst_id", wth_id));
                recordSWData(expData, new DssatSoilOutput());
                recordSWData(expData, new DssatWeatherOutput());
            } else {
                ArrayList<HashMap> soilArrTmp = new ArrayList();
                ArrayList<HashMap> wthArrTmp = new ArrayList();
                for (HashMap rootData : rootArr) {
                    soil_id = getObjectOr(rootData, "soil_id", "");
                    wth_id = getObjectOr(rootData, "wst_id", "");
                    HashMap soilTmp = new HashMap();
                    HashMap wthTmp = new HashMap();
                    soilTmp.put("soil", getSectionDataWithNocopy(soilArr, "soil_id", soil_id));
                    soilTmp.put("soil_id", soil_id);
                    soilTmp.put("exname", rootData.get("exname"));
                    soilTmp.put("id", rootData.get("id"));
                    wthTmp.put("weather", getSectionDataWithNocopy(wthArr, "wst_id", wth_id));
                    wthTmp.put("wst_id", wth_id);
                    recordSWData(soilTmp, new DssatSoilOutput());
                    recordSWData(wthTmp, new DssatWeatherOutput());
                    soilArrTmp.add((HashMap) soilTmp.get("soil"));
                    wthArrTmp.add(wthTmp);
                    rootData.put("wst_id", getObjectOr(wthTmp, "wst_id", wth_id));
                    rootData.put("soil_id", getObjectOr(soilTmp, "soil_id", wth_id));
                }
                expData.put("soil", soilArrTmp);
                expData.put("weather", wthArrTmp);
            }
            String exname = getValueOr(expData, "exname", "N/A");
            //            DssatCommonOutput[] outputs = {
//                new DssatXFileOutput(),
//                new DssatAFileOutput(),
//                new DssatTFileOutput(),
//                new DssatCulFileOutput(),};
            writeSingleExp(arg0, expData, new DssatXFileOutput(), exname + "_X");
            writeSingleExp(arg0, expData, new DssatAFileOutput(), exname + "_A");
            writeSingleExp(arg0, expData, new DssatTFileOutput(), exname + "_T");
            writeSingleExp(arg0, expData, new DssatCulFileOutput(), exname + "_Cul");
        }

        // If experiment data is included
        if (!expArr.isEmpty()) {
            // Write all batch files
//            futFiles.put("DSSBatch.v45", executor.submit(new DssatTranslateRunner(new DssatBatchFileOutput(DssatVersion.DSSAT45), expArr, arg0)));
//            futFiles.put("DSSBatch.v46", executor.submit(new DssatTranslateRunner(new DssatBatchFileOutput(DssatVersion.DSSAT46), expArr, arg0)));
            futFiles.put("DSSBatch.v47", executor.submit(new DssatTranslateRunner(new DssatBatchFileOutput(DssatVersion.DSSAT47), expArr, arg0)));
            futFiles.put("DSSBatch.v48", executor.submit(new DssatTranslateRunner(new DssatBatchFileOutput(DssatVersion.DSSAT48), expArr, arg0)));
//            futFiles.put("Run45.bat", executor.submit(new DssatTranslateRunner(new DssatRunFileOutput(DssatVersion.DSSAT45), expArr, arg0)));
//            futFiles.put("Run46.bat", executor.submit(new DssatTranslateRunner(new DssatRunFileOutput(DssatVersion.DSSAT46), expArr, arg0)));
            futFiles.put("Run47.bat", executor.submit(new DssatTranslateRunner(new DssatRunFileOutput(DssatVersion.DSSAT47), expArr, arg0)));
            futFiles.put("Run48.bat", executor.submit(new DssatTranslateRunner(new DssatRunFileOutput(DssatVersion.DSSAT48), expArr, arg0)));
        } // If only weather or soil data is included
        else {
            for (HashMap sData : soilArr) {
                HashMap tmp = new HashMap();
                tmp.put("soil", sData);
                tmp.put("soil_id", getObjectOr(sData, "soil_id", ""));
                recordSWData(tmp, new DssatSoilOutput());
            }
            for (HashMap wData : wthArr) {
                HashMap tmp = new HashMap();
                tmp.put("weather", wData);
                tmp.put("wst_id", getObjectOr(wData, "wst_id", ""));
                recordSWData(tmp, new DssatWeatherOutput());
            }
        }

        // Write soil and weather files
        writeWthFiles(arg0);
        writeSoilFiles(arg0);

        // compress all output files into one zip file
        //createZip();
    }

    /**
     * ALL DSSAT Data Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     *
     */
    @Override
    public void writeFile(String arg0, Map result) {

        long estTime = 0;

        try {
            if (getObjectOr(result, "experiments", new ArrayList()).isEmpty()
                    && getObjectOr(result, "soils", new ArrayList()).isEmpty()
                    && getObjectOr(result, "weathers", new ArrayList()).isEmpty()) {

                // Calculate estimated time consume
                HashMap wth = getObjectOr(result, "weather", new HashMap());
                estTime += getObjectOr(wth, "dailyWeather", new ArrayList()).size();

                // Write files
                recordSWData(result, new DssatSoilOutput());
                recordSWData(result, new DssatWeatherOutput());
                writeWthFiles(arg0);
                writeSoilFiles(arg0);
                String exname = getValueOr(result, "exname", "N/A");
                writeSingleExp(arg0, result, new DssatXFileOutput(), exname + "_X");
                writeSingleExp(arg0, result, new DssatAFileOutput(), exname + "_A");
                writeSingleExp(arg0, result, new DssatTFileOutput(), exname + "_T");
                writeSingleExp(arg0, result, new DssatCulFileOutput(), exname + "_Cul");
//                writeSingleExp(arg0, result, new DssatBatchFileOutput(DssatVersion.DSSAT45), "DSSBatch.v45");
//                writeSingleExp(arg0, result, new DssatBatchFileOutput(DssatVersion.DSSAT46), "DSSBatch.v46");
                writeSingleExp(arg0, result, new DssatBatchFileOutput(DssatVersion.DSSAT47), "DSSBatch.v47");
                writeSingleExp(arg0, result, new DssatBatchFileOutput(DssatVersion.DSSAT48), "DSSBatch.v48");
//                writeSingleExp(arg0, result, new DssatRunFileOutput(DssatVersion.DSSAT45), "Run45.bat");
//                writeSingleExp(arg0, result, new DssatRunFileOutput(DssatVersion.DSSAT46), "Run46.bat");
                writeSingleExp(arg0, result, new DssatRunFileOutput(DssatVersion.DSSAT47), "Run47.bat");
                writeSingleExp(arg0, result, new DssatRunFileOutput(DssatVersion.DSSAT48), "Run48.bat");

                // compress all output files into one zip file
                outputFile = new File(revisePath(arg0) + exname + ".ZIP");

                //createZip();
            } else {
                // Calculate estimated time consume
                for (HashMap wth : (ArrayList<HashMap>) getObjectOr(result, "weathers", new ArrayList<HashMap>())) {
                    estTime += getObjectOr(wth, "dailyWeather", new ArrayList()).size();
                }
                writeMultipleExp(arg0, result);
            }

            if (estTime > 0) {
                estTime *= 10;
            } else {
                estTime = 10000;
            }

            executor.shutdown();
            long timer = System.currentTimeMillis();
            HashSet<String> keys = new HashSet(futFiles.keySet());
            int counter = 0;
            while (!executor.isTerminated()) {
                if (System.currentTimeMillis() - timer > estTime) {
                    counter++;
                    timer = System.currentTimeMillis();
                    if (keys.isEmpty()) {
                        LOG.info("The executor should be terminated");
                        executor.shutdownNow();
                    } else if (counter > 3) {
                        LOG.info("The executor is going to be force terminated");
                        executor.shutdownNow();
                    }
                    String[] arr = keys.toArray(new String[0]);
                    for (String key : arr) {
                        Future futFile = futFiles.get(key);
                        if (futFile.isCancelled() || futFile.isDone()) {
                            keys.remove(key);
                        } else {
                            LOG.info("DSSAT translation for {} is still under processing...", key);
                        }
                    }
                }
            }
            executor = null;
            LOG.debug("Consume {} s", (System.currentTimeMillis() - timer) / 1000.0);

            // Get output result files into output array
            for (String key : futFiles.keySet()) {
                try {
                    File f = futFiles.get(key).get();
                    if (f != null) {
                        files.put(f.getPath(), f);
                    }
                } catch (InterruptedException ex) {
                    LOG.error(getStackTrace(ex));
                } catch (ExecutionException ex) {
                    if (!ex.getMessage().contains("NoOutputFileException")) {
                        LOG.error(getStackTrace(ex));
                    }
                }
            }

        } catch (FileNotFoundException e) {
            LOG.error(getStackTrace(e));
        } catch (IOException e) {
            LOG.error(getStackTrace(e));
        }
    }

    /**
     * Write files and add file objects in the array
     *
     * @param arg0 file output path
     * @param result data holder object
     * @param output DSSAT translator object
     * @param file Generated DSSAT file identifier
     */
    private void writeSingleExp(String arg0, Map result, DssatCommonOutput output, String file) {
        futFiles.put(file, executor.submit(new DssatTranslateRunner(output, result, arg0)));
    }

    /**
     * write soil/weather files
     *
     * @param expData The holder for experiment data include soil/weather data
     * @param output The DSSAT Writer object
     */
    private void recordSWData(Map expData, DssatCommonOutput output) {
        String id;
        HashMap<String, Map> swData;
        try {
            if (output instanceof DssatSoilOutput) {
                Map soilTmp = getObjectOr(expData, "soil", new HashMap());
                if (soilTmp.isEmpty()) {
                    id = getObjectOr(expData, "soil_id", "");
                } else {
                    id = soilHelper.getSoilID(soilTmp);
                }
//                id = id.substring(0, 2);
                swData = soilData;
                expData.put("soil_id", id);
            } else {
                //            id = getObjectOr(expData, "wst_id", "");
                //            id = getWthFileName(getObjectOr(expData, "weather", new HashMap()));
                Map wthTmp = getObjectOr(expData, "weather", new HashMap());
                if (wthTmp.isEmpty()) {
                    id = getObjectOr(expData, "wst_id", "");
                } else {
                    id = wthHelper.createWthFileName(wthTmp);
                }
                swData = wthData;
                expData.put("wst_id", id);
            }
            if (!id.isEmpty() && !swData.containsKey(id)) {
                swData.put(id, expData);
//                Future fut = executor.submit(new DssatTranslateRunner(output, expData, arg0));
//                swfiles.put(id, fut);
//                futFiles.add(fut);
            }
        } catch (Exception e) {
            LOG.error(getStackTrace(e));
        }
    }

    private void writeWthFiles(String arg0) {

        for (Map data : wthData.values()) {
            writeSingleExp(arg0, data, new DssatWeatherOutput(), getValueOr(data, "wst_id", "N/A") + "_wth");
        }

    }

    private void writeSoilFiles(String arg0) {

        Map<String, ArrayList<Map>> soilTmp = new HashMap();
        for (Map data : soilData.values()) {

            String soil_id = getObjectOr(data, "soil_id", "");
            if (soil_id.length() < 2) {
                soil_id = "";
            } else {
                soil_id = soil_id.substring(0, 2);
            }

            Map soil = (Map) data.get("soil");
            if (soil == null || soil.isEmpty()) {
                continue;
            }

            if (soilTmp.containsKey(soil_id)) {
                soilTmp.get(soil_id).add(data);
            } else {
                ArrayList<Map> arr = new ArrayList();
                arr.add(data);
                soilTmp.put(soil_id, arr);
            }
        }

        for (String key : soilTmp.keySet()) {
            HashMap data = new HashMap();
            data.put("soils", soilTmp.get(key));
            writeSingleExp(arg0, data, new DssatSoilOutput(), key + "_soil");
        }
    }

    /**
     * Compress the files in one zip
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void createZip() throws FileNotFoundException, IOException {
        createZip(true);
    }

    public void createZip(File out) throws FileNotFoundException, IOException {
        createZip(out, true);
    }

    /**
     * Compress the files in one zip
     *
     * @param isDelete Flag for if deleted the files after compression
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void createZip(boolean isDelete) throws FileNotFoundException, IOException {
        createZip(outputFile, isDelete);
    }

    public void createZip(File outputFile, boolean isDelete) throws FileNotFoundException, IOException {

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
        ZipEntry entry;
        BufferedInputStream bis;
        byte[] data = new byte[1024];

        LOG.info("Start zipping all the files...");

        // Check if there is file been created
        if (files == null) {
            LOG.warn("No files here for zipping");
            return;
        }

        for (File file : files.values()) {
            if (file == null) {
                continue;
            }

            if (outputFile.getParent() != null) {
                entry = new ZipEntry(file.getPath().substring(outputFile.getParent().length() + 1));
            } else {
                entry = new ZipEntry(file.getPath());
            }
            out.putNextEntry(entry);
            bis = new BufferedInputStream(new FileInputStream(file));

            int count;
            while ((count = bis.read(data)) != -1) {
                out.write(data, 0, count);
            }
            bis.close();
            file.delete();
        }

        out.close();
        LOG.info("End zipping");
    }

    /**
     * Get all output files
     *
     * @return list of output files
     */
    public ArrayList<File> getOutputFiles() {
        return new ArrayList(files.values());
    }

//    /**
//     * Get output zip file name by using experiment file name
//     *
//     * @param outputs DSSAT Output objects
//     */
//    private String getZipFileName(DssatCommonOutput... outputs) {
//
//        for (int i = 0; i < outputs.length; i++) {
//            if (outputs[i] instanceof DssatXFileOutput) {
//                if (outputs[i].getOutputFile() != null) {
//                    return outputs[i].getOutputFile().getName().replaceAll("\\.", "_") + ".ZIP";
//                } else {
//                    break;
//                }
//            }
//        }
//
//        return "OUTPUT.ZIP";
//    }
    /**
     * Get output zip file
     *
     * @return output zip file
     */
    public File getOutputZipFile() {
        return outputFile;
    }

    private ArrayList<HashMap> combineExps(ArrayList<HashMap> expArr) {

        // Set experiment groups with same exname
        ArrayList<HashMap> ret = new ArrayList();
        LinkedHashMap<String, ArrayList<HashMap>> expGroupMap = new LinkedHashMap();
        ArrayList<HashMap> subExpArr;
        String exname;
        for (int i = 0; i < expArr.size(); i++) {
            exname = getValueOr(expArr.get(i), "exname", "");

            if (exname.isEmpty()) {
                subExpArr = new ArrayList();
                subExpArr.add(expArr.get(i));
                expGroupMap.put("Experiment_" + i, subExpArr);
            } else {
                exname = getFileName(expArr.get(i), "");
                subExpArr = expGroupMap.get(exname);
                if (subExpArr == null) {
                    subExpArr = new ArrayList();
                    expGroupMap.put(exname, subExpArr);
                }
                subExpArr.add(expArr.get(i));
            }
        }

        // Combine the experiments in the same group
        Set<Entry<String, ArrayList<HashMap>>> expGroups = expGroupMap.entrySet();
        for (Entry<String, ArrayList<HashMap>> expGroup : expGroups) {
            if (expGroup.getValue().size() == 1) {
                ret.add(expGroup.getValue().get(0));
            } else {
                HashMap tmp = DssatCommonInput.CopyList(expGroup.getValue().get(0));
                if (!tmp.containsKey("dssat_sequence")) {
                    HashMap seq = new HashMap();
                    ArrayList<HashMap> seqArr = new ArrayList();
                    HashMap seqData = new HashMap();
                    String trt_name = getValueOr(tmp, "trt_name", getValueOr(tmp, "exname", ""));
                    if (!trt_name.isEmpty()) {
                        seqData.put("trt_name", trt_name);
                    }
                    seqArr.add(seqData);
                    tmp.put("dssat_sequence", seq);
                    seq.put("data", seqArr);
                }
                ArrayList rootArr = new ArrayList();
                tmp.put("dssat_root", rootArr);
                rootArr.add(combinaRoot(tmp));
                for (int i = 1; i < expGroup.getValue().size(); i++) {
                    HashMap exp = expGroup.getValue().get(i);
                    rootArr.add(combinaRoot(exp));
                    updateGroupExps(tmp, exp, i + 1);
                }
                ret.add(tmp);
            }
        }
        return ret;
    }

    private HashMap combinaRoot(Map m) {
        Set<Entry<String, Object>> entries = m.entrySet();
        HashMap items = new HashMap();
        for (Entry<String, Object> e : entries) {
            if (e.getValue() instanceof String || e.getKey().equals("dssat_info") || e.getKey().equals("initial_conditions")) {
                items.put(e.getKey(), e.getValue());
            }
        }
        return items;
    }

    private void updateGroupExps(HashMap out, HashMap expData, int trno) {
        int baseId = trno * 1000;
        int seqid;
        int sm;
        int em;
        // Update dssat_sequence data
        ArrayList<HashMap<String, String>> seqArr = new BucketEntry(getObjectOr(expData, "dssat_sequence", new HashMap())).getDataList();
        if (seqArr.isEmpty()) {
            seqArr.add(new HashMap());
        }
        for (HashMap<String, String> tmp : seqArr) {
            try {
                seqid = Integer.parseInt(getValueOr(tmp, "seqid", "0")) + baseId;
                sm = Integer.parseInt(getValueOr(tmp, "sm", "0")) + baseId;
                em = Integer.parseInt(getValueOr(tmp, "em", "0")) + baseId;
            } catch (NumberFormatException e) {
                seqid = baseId;
                sm = baseId;
                em = baseId;
            }
            tmp.put("seqid", seqid + "");
            tmp.put("sm", sm + "");
            tmp.put("em", em + "");
            tmp.put("trno", trno + "");
            if (tmp.get("trt_name") == null) {
                String trt_name = getValueOr(expData, "trt_name", getValueOr(expData, "exname", ""));
                if (!trt_name.isEmpty()) {
                    tmp.put("trt_name", trt_name);
                }
            }
        }
        combineData(out, seqArr, "dssat_sequence");

        // Update management events data
        ArrayList<HashMap<String, String>> evtArr = new BucketEntry(getObjectOr(expData, "management", new HashMap())).getDataList();
        for (HashMap<String, String> tmp : evtArr) {
            try {
                seqid = Integer.parseInt(getValueOr(tmp, "seqid", "0")) + baseId;
            } catch (NumberFormatException e) {
                seqid = baseId;
            }
            tmp.put("seqid", seqid + "");
        }
        combineData(out, evtArr, "management");

        // Update adjustments data
        ArrayList<HashMap<String, String>> adjArr = getObjectOr(expData, "adjustments", new ArrayList());
        for (HashMap<String, String> tmp : adjArr) {
            try {
                seqid = Integer.parseInt(getValueOr(tmp, "seqid", "0")) + baseId;
            } catch (NumberFormatException e) {
                seqid = baseId;
            }
            tmp.put("seqid", seqid + "");
        }
        combineArr(out, adjArr, "adjustments");

        // Update dssat_environment_modification data
        ArrayList<HashMap<String, String>> emArr = new BucketEntry(getObjectOr(expData, "dssat_environment_modification", new HashMap())).getDataList();
        for (HashMap<String, String> tmp : emArr) {
            try {
                em = Integer.parseInt(getValueOr(tmp, "em", "0")) + baseId;
            } catch (NumberFormatException e) {
                em = baseId;
            }
            tmp.put("em", em + "");
        }
        combineData(out, emArr, "dssat_environment_modification");

        // Update dssat_simulation_control data
        ArrayList<HashMap<String, Object>> smArr = getObjectOr(getObjectOr(expData, "dssat_simulation_control", new HashMap()), "data", new ArrayList());
        for (HashMap<String, Object> tmp : smArr) {
            try {
                sm = Integer.parseInt(getValueOr(tmp, "sm", "0")) + baseId;
            } catch (NumberFormatException e) {
                sm = baseId;
            }
            tmp.put("sm", sm + "");
        }
        combineData(out, smArr, "dssat_simulation_control");

        // Update observation data
        HashMap obvData = getObjectOr(expData, "observed", new HashMap());
        if (!obvData.isEmpty()) {
            obvData.put("trno", trno + "");
            Object outObvData = getObjectOr(out, "observed", new Object());
            if (outObvData instanceof HashMap) {
                ((HashMap) outObvData).put("trno", "1");
                ArrayList obvArr = new ArrayList();
                obvArr.add(outObvData);
                obvArr.add(obvData);
                out.put("observed", obvArr);
            } else if (outObvData instanceof ArrayList) {
                ((ArrayList) outObvData).add(obvData);
            } else {
                ArrayList obvArr = new ArrayList();
                obvArr.add(obvData);
                out.put("observed", obvArr);
            }
        }
    }

    private void combineData(HashMap out, ArrayList arr, String secName) {
        ArrayList<HashMap<String, String>> seqArrOut;
        // combine the array of data
//        seqArrOut = new BucketEntry(getObjectOr(out, secName, new HashMap())).getDataList();
        if (!arr.isEmpty()) {
            HashMap data = getObjectOr(out, secName, new HashMap());
            if (data.isEmpty()) {
                out.put(secName, data);
            }
            String subSecName;
            if (secName.equals("management")) {
                subSecName = "events";
            } else {
                subSecName = "data";
            }
            combineArr(data, arr, subSecName);
        }
    }

    private void combineArr(HashMap out, ArrayList arr, String secName) {
        ArrayList<HashMap<String, String>> seqArrOut;
        // combine the array of data
        if (!arr.isEmpty()) {
            seqArrOut = getObjectOr(out, secName, new ArrayList());
            if (seqArrOut.isEmpty()) {
                out.put(secName, seqArrOut);
            }
            seqArrOut.addAll(arr);
        }
    }
}
