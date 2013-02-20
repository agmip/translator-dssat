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

    private HashMap<String, File> files = new HashMap();
    private ArrayList<Future<File>> futFiles = new ArrayList();
//    private HashMap<String, Future<File>> soilFiles = new HashMap();
//    private HashMap<String, Future<File>> wthFiles = new HashMap();
    private HashMap<String, Map> soilData = new HashMap();
    private HashMap<String, Map> wthData = new HashMap();
    private ExecutorService executor = Executors.newFixedThreadPool(64);
    private static final Logger log = LoggerFactory.getLogger(DssatControllerOutput.class);

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
        HashMap expData;
        ArrayList<HashMap> soilArr = getObjectOr(result, "soils", new ArrayList());
        ArrayList<HashMap> wthArr = getObjectOr(result, "weathers", new ArrayList());

        // Write files
        String soil_id;
        String wth_id;
        expArr = combineExps(expArr);
        for (int i = 0; i < expArr.size(); i++) {
            expData = expArr.get(i);
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
//                ArrayList<HashMap> wthArrTmp = new ArrayList();
                for (int j = 0; j < rootArr.size(); j++) {
                    soil_id = getObjectOr(rootArr.get(j), "soil_id", "");
                    wth_id = getObjectOr(rootArr.get(j), "wst_id", "");
                    HashMap soilTmp = new HashMap();
                    HashMap wthTmp = new HashMap();
                    soilTmp.put("soil", getSectionDataWithNocopy(soilArr, "soil_id", soil_id));
                    soilTmp.put("soil_id", soil_id);
                    soilTmp.put("exname", rootArr.get(j).get("exname"));
                    soilTmp.put("id", rootArr.get(j).get("id"));
                    wthTmp.put("weather", getSectionDataWithNocopy(wthArr, "wst_id", wth_id));
                    wthTmp.put("wst_id", wth_id);
                    recordSWData(soilTmp, new DssatSoilOutput());
                    recordSWData(wthTmp, new DssatWeatherOutput());
                    soilArrTmp.add((HashMap) soilTmp.get("soil"));
//                    wthArrTmp.add(wthTmp);
                    rootArr.get(j).put("wst_id", getObjectOr(wthTmp, "wst_id", wth_id));
                }
                expData.put("soil", soilArrTmp);
//                expData.put("weather", wthArrTmp);
            }

            DssatCommonOutput[] outputs = {
                new DssatXFileOutput(),
                new DssatAFileOutput(),
                new DssatTFileOutput(),
                new DssatCulFileOutput(),};
            writeSingleExp(arg0, expData, outputs);
        }

        // If experiment data is included
        if (!expArr.isEmpty()) {
            // Write all batch files
            futFiles.add(executor.submit(new DssatTranslateRunner(new DssatBatchFileOutput(), expArr, arg0)));
            futFiles.add(executor.submit(new DssatTranslateRunner(new DssatRunFileOutput(), expArr, arg0)));
        } // If only weather or soil data is included
        else {
            for (int i = 0; i < soilArr.size(); i++) {
                HashMap tmp = new HashMap();
                tmp.put("soil", soilArr.get(i));
                tmp.put("soil_id", getObjectOr(soilArr.get(i), "soil_id", ""));
                recordSWData(tmp, new DssatSoilOutput());
            }
            for (int i = 0; i < wthArr.size(); i++) {
                HashMap tmp = new HashMap();
                tmp.put("weather", wthArr.get(i));
                tmp.put("wst_id", getObjectOr(wthArr.get(i), "wst_id", ""));
                recordSWData(tmp, new DssatWeatherOutput());
            }
        }

        // Write soil and weather files
        writeWthFiles(arg0);
        writeSoilFiles(arg0);

        // compress all output files into one zip file
        Calendar cal = Calendar.getInstance();
        outputFile = new File(arg0 + "AGMIP_DSSAT_" + cal.getTimeInMillis() + ".zip");
        createZip();
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

        try {
            if (getObjectOr(result, "experiments", new ArrayList()).isEmpty()
                    && getObjectOr(result, "soils", new ArrayList()).isEmpty()
                    && getObjectOr(result, "weathers", new ArrayList()).isEmpty()) {
                // Write files
                DssatCommonOutput[] outputs = {
                    new DssatXFileOutput(),
                    new DssatAFileOutput(),
                    new DssatTFileOutput(),
                    new DssatCulFileOutput(),
                    new DssatBatchFileOutput(),
                    new DssatRunFileOutput()
                };
                recordSWData(result, new DssatSoilOutput());
                recordSWData(result, new DssatWeatherOutput());
                writeWthFiles(arg0);
                writeSoilFiles(arg0);
                writeSingleExp(arg0, result, outputs);

                // compress all output files into one zip file
                outputFile = new File(revisePath(arg0) + getZipFileName(outputs));

                createZip();

            } else {
                writeMultipleExp(arg0, result);
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            executor = null;

        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Write files and add file objects in the array
     *
     * @param arg0 file output path
     * @param result data holder object
     * @param outputs DSSAT Output objects
     */
    private void writeSingleExp(String arg0, Map result, DssatCommonOutput... outputs) {
        for (int i = 0; i < outputs.length; i++) {
            futFiles.add(executor.submit(new DssatTranslateRunner(outputs[i], result, arg0)));
        }
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
            if (!id.equals("") && !swData.containsKey(id)) {
                swData.put(id, expData);
//                Future fut = executor.submit(new DssatTranslateRunner(output, expData, arg0));
//                swfiles.put(id, fut);
//                futFiles.add(fut);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void writeWthFiles(String arg0) {

        for (Map data : wthData.values()) {
            writeSingleExp(arg0, data, new DssatWeatherOutput());
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

            if (soilTmp.containsKey(soil_id)) {
                soilTmp.get(soil_id).add(data);
            } else {
                ArrayList<Map> arr = new ArrayList();
                arr.add(data);
                soilTmp.put(soil_id, arr);
            }
        }

        for (ArrayList arr : soilTmp.values()) {
            HashMap data = new HashMap();
            data.put("soils", arr);
            writeSingleExp(arg0, data, new DssatSoilOutput());
        }
    }

    /**
     * Compress the files in one zip
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void createZip() throws FileNotFoundException, IOException {

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
        ZipEntry entry;
        BufferedInputStream bis;
        byte[] data = new byte[1024];

        // Get output result files into output array for zip package
        for (int i = 0; i < futFiles.size(); i++) {
            try {
                File f = futFiles.get(i).get();
                if (f != null) {
                    files.put(f.getPath(), f);
                }
            } catch (InterruptedException ex) {
                log.error(ex.getMessage());
            } catch (ExecutionException ex) {
                if (!ex.getMessage().contains("NoOutputFileException")) {
                    log.error(ex.getMessage());
                }
            }
        }

        log.info("Start zipping all the files...");

        // Check if there is file been created
        if (files == null) {
            log.warn("No files here for zipping");
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
        log.info("End zipping");
    }

    /**
     * Get all output files
     */
    public ArrayList<File> getOutputFiles() {
        return new ArrayList(files.values());
    }

    /**
     * Get output zip file name by using experiment file name
     *
     * @param outputs DSSAT Output objects
     */
    private String getZipFileName(DssatCommonOutput[] outputs) {

        for (int i = 0; i < outputs.length; i++) {
            if (outputs[i] instanceof DssatXFileOutput) {
                if (outputs[i].getOutputFile() != null) {
                    return outputs[i].getOutputFile().getName().replaceAll("\\.", "_") + ".ZIP";
                } else {
                    break;
                }
            }
        }

        return "OUTPUT.ZIP";
    }

    /**
     * Get output zip file
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

            if (exname.equals("")) {
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
                    if (!trt_name.equals("")) {
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
        for (int i = 0; i < seqArr.size(); i++) {
            HashMap tmp = seqArr.get(i);

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
                if (!trt_name.equals("")) {
                    tmp.put("trt_name", trt_name);
                }
            }
        }
        combineData(out, seqArr, "dssat_sequence");

        // Update management events data
        ArrayList<HashMap<String, String>> evtArr = new BucketEntry(getObjectOr(expData, "management", new HashMap())).getDataList();
        for (int i = 0; i < evtArr.size(); i++) {
            HashMap tmp = evtArr.get(i);
            try {
                seqid = Integer.parseInt(getValueOr(tmp, "seqid", "0")) + baseId;
            } catch (NumberFormatException e) {
                seqid = baseId;
            }
            tmp.put("seqid", seqid + "");
        }
        combineData(out, evtArr, "management");

        // Update dssat_environment_modification data
        ArrayList<HashMap<String, String>> emArr = new BucketEntry(getObjectOr(expData, "dssat_environment_modification", new HashMap())).getDataList();
        for (int i = 0; i < emArr.size(); i++) {
            HashMap tmp = emArr.get(i);
            try {
                em = Integer.parseInt(getValueOr(tmp, "em", "0")) + baseId;
            } catch (NumberFormatException e) {
                em = baseId;
            }
            tmp.put("em", em + "");
        }
        combineData(out, emArr, "dssat_environment_modification");

        // Update dssat_simulation_control data
        ArrayList<HashMap<String, String>> smArr = new BucketEntry(getObjectOr(expData, "dssat_simulation_control", new HashMap())).getDataList();
        for (int i = 0; i < smArr.size(); i++) {
            HashMap tmp = smArr.get(i);
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
            seqArrOut = getObjectOr(data, subSecName, new ArrayList());
            if (seqArrOut.isEmpty()) {
                data.put(subSecName, seqArrOut);
            }
            seqArrOut.addAll(arr);
        }
    }
}
