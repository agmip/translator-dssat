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
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.agmip.translators.dssat.DssatCommonInput.getSectionData;
import static org.agmip.translators.dssat.DssatCommonOutput.revisePath;
import static org.agmip.util.MapUtil.*;

/**
 *
 * @author Meng Zhang
 */
public class DssatControllerOutput extends DssatCommonOutput {

    private File zipFile;
    private HashMap<String, File> files = new HashMap();
    private HashMap<String, File> soilFiles = new HashMap();
    private HashMap<String, File> wthFiles = new HashMap();
    private DssatWthFileHelper wthHelper = new DssatWthFileHelper();

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
        String exname;
        ArrayList<String> subDirs = new ArrayList();
        ArrayList<HashMap> expArr = getObjectOr(result, "experiments", new ArrayList());
        HashMap expData;
        ArrayList<HashMap> soilArr = getObjectOr(result, "soils", new ArrayList());
        ArrayList<HashMap> wthArr = getObjectOr(result, "weathers", new ArrayList());
        DssatCommonOutput[] outputs = {
            new DssatXFileOutput(),
            new DssatAFileOutput(),
            new DssatTFileOutput(),
            new DssatCulFileOutput(),
            //            new DssatBatchFileOutput(),
            //            new DssatRunFileOutput(),
            new DssatACMOJsonOutput() // TODO ACMO data also need to be combined?
        };

        // Write files
        String soil_id;
        String wth_id;
        HashMap<String, File> swFiles = new HashMap();
        boolean wsSubDirFlg = false;
        HashMap<String, String> expNameMap = checkMultiTrn(expArr);
        for (int i = 0; i < expArr.size(); i++) {
            expData = expArr.get(i);
            soil_id = getObjectOr(expData, "soil_id", "");
            wth_id = getObjectOr(expData, "wst_id", "");
            expData.put("soil", getSectionData(soilArr, "soil_id", soil_id));
            expData.put("weather", getSectionData(wthArr, "wst_id", wth_id));
            exname = getValueOr(expData, "exname", "Experiment_" + i);
            File soilFile = writeSWFile(arg0, expData, new DssatSoilOutput());
            File wthFile = writeSWFile(arg0, expData, new DssatWeatherOutput());
            writeSingleExp(arg0 + expNameMap.get(exname), expData, outputs);
            if (!expNameMap.get(exname).equals("")) {
                subDirs.add(expNameMap.get(exname));
                swFiles.put(exname + "_S", soilFile);
                swFiles.put(exname + "_W", wthFile);
                wsSubDirFlg = true;
            }
        }
        if (wsSubDirFlg) {
            subDirs.add("SOIL");
            subDirs.add("WEATHER");
        }

        // If experiment data is included
        if (!expArr.isEmpty()) {
            // Write all batch files
            DssatBatchFileOutput batchTran = new DssatBatchFileOutput();
            batchTran.writeFile(arg0, expArr, expNameMap);
            if (batchTran.getOutputFile() != null) {
//                files.add(batchTran.getOutputFile());
                files.put(batchTran.getOutputFile().getPath(), batchTran.getOutputFile());
            }
            DssatRunFileOutput runTran = new DssatRunFileOutput();
            runTran.writeFile(arg0, expArr);
            if (runTran.getOutputFile() != null) {
//                files.add(runTran.getOutputFile());
                files.put(runTran.getOutputFile().getPath(), runTran.getOutputFile());
            }
        } // If only weather or soil data is included
        else {
            for (int i = 0; i < soilArr.size(); i++) {
                HashMap tmp = new HashMap();
                tmp.put("soil", soilArr.get(i));
                writeSingleExp(arg0, tmp, new DssatSoilOutput());
            }
            for (int i = 0; i < wthArr.size(); i++) {
                HashMap tmp = new HashMap();
                tmp.put("weather", wthArr.get(i));
                writeSingleExp(arg0, tmp, new DssatWeatherOutput());
            }
        }

        // compress all output files into one zip file
        Calendar cal = Calendar.getInstance();
        zipFile = new File(arg0 + "AGMIP_DSSAT_" + cal.getTimeInMillis() + ".zip");
        createZip(swFiles);

        // Delete the remained folders
        File dir;
        for (int i = 0; i < subDirs.size(); i++) {
            dir = new File(arg0 + subDirs.get(i));
            dir.delete();
        }
    }

    /**
     * ALL DSSAT Data Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     *
     */
    public void writeFile(String arg0, Map result) {

        try {
            if (getObjectOr(result, "experiments", new ArrayList()).isEmpty()
                    && getObjectOr(result, "soils", new ArrayList()).isEmpty()
                    && getObjectOr(result, "weathers", new ArrayList()).isEmpty()) {
                // Write files
                DssatCommonOutput[] outputs = {
                    new DssatXFileOutput(),
                    new DssatSoilOutput(),
                    new DssatWeatherOutput(),
                    new DssatAFileOutput(),
                    new DssatTFileOutput(),
                    new DssatCulFileOutput(),
                    new DssatBatchFileOutput(),
                    new DssatRunFileOutput(),
                    new DssatACMOJsonOutput()
                };
                writeSingleExp(arg0, result, outputs);

                // compress all output files into one zip file
                zipFile = new File(revisePath(arg0) + getZipFileName(outputs));

                createZip();

            } else {
                writeMultipleExp(arg0, result);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
            try {
                outputs[i].writeFile(arg0, result);
                if (outputs[i].getOutputFile() != null) {
    //                files.add(outputs[i].getOutputFile());
                    files.put(outputs[i].getOutputFile().getPath(), outputs[i].getOutputFile());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }    
        }
    }

    /**
     * write soil/weather files
     *
     * @param arg0 The output path
     * @param expData The holder for experiment data include soil/weather data
     * @param output The DSSAT Writer object
     * @return The created soil/weather file object
     */
    private File writeSWFile(String arg0, Map expData, DssatCommonOutput output) {
        String id = "";
//        String fileName;
        HashMap<String, File> swfiles = null;
        try {
            if (output instanceof DssatSoilOutput) {
                id = getObjectOr(expData, "soil_id", "");
//                id = id.substring(0, 2);
                swfiles = soilFiles;
            } else {
    //            id = getObjectOr(expData, "wst_id", "");
    //            id = getWthFileName(getObjectOr(expData, "weather", new HashMap()));
    
                id = wthHelper.createWthFileName(getObjectOr(expData, "weather", new HashMap()));
                swfiles = wthFiles;
                expData.put("wst_id", id);
                getObjectOr(expData, "weather", new HashMap()).put("wst_id", id);
            }
            if (!id.equals("") && !swfiles.containsKey(id)) {
                output.writeFile(arg0, expData);
                if (output.getOutputFile() != null) {
                    swfiles.put(id, output.getOutputFile());
    //            files.add(output.getOutputFile());
                    files.put(output.getOutputFile().getPath(), output.getOutputFile());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return swfiles.get(id);
    }

    /**
     * Compress the files in one zip
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void createZip() throws FileNotFoundException, IOException {

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        ZipEntry entry;
        BufferedInputStream bis;
        byte[] data = new byte[1024];
//        File file;

        for (File file : files.values()) {
//            file = files.get(i);
            if (file == null) {
                continue;
            }

            if (zipFile.getParent() != null) {
                entry = new ZipEntry(file.getPath().substring(zipFile.getParent().length() + 1));
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
    }

    /**
     * Compress the files in one zip
     *
     * @param swFiles The map contain the relationship between experiments and
     * soil/weather files
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void createZip(HashMap<String, File> swFiles) throws FileNotFoundException, IOException {

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
//        File file;
        String zipPath;

        for (File file : files.values()) {
//            file = files.get(i);
            if (file == null) {
                continue;
            }

            if (zipFile.getParent() != null) {
                zipPath = file.getPath().substring(zipFile.getParent().length() + 1);
            } else {
                zipPath = file.getPath();
            }
            addToZip(out, zipPath, file);

            if (file.getName().matches(".+\\.\\w{2}[Xx]")) {
                zipPath = zipPath.substring(0, zipPath.length() - file.getName().length());
                if (!zipPath.equals("")) {
                    String exname = zipPath.substring(0, zipPath.length() - 1);
                    addToZip(out, zipPath, swFiles.get(exname + "_S"));
                    addToZip(out, zipPath, swFiles.get(exname + "_W"));
                }
            }
//            file.delete();
        }

        // Delete files
        for (File file : files.values()) {
            if (file != null) {
                file.delete();
            }
        }
        files.clear();
        soilFiles.clear();
        wthFiles.clear();

        // Delete Soil files
//        for (String id : soilFiles.keySet()) {
//            if (soilFiles.get(id) != null) {
//                soilFiles.get(id).delete();
//            }
//        }
//        soilFiles.clear();
//
//        // Delete Weather files
//        for (String id : wthFiles.keySet()) {
//            if (wthFiles.get(id) != null) {
//                wthFiles.get(id).delete();
//            }
//        }
//        wthFiles.clear();

        out.close();
    }

    /**
     * Add current file into zip package
     *
     * @param out The ZipOutputStream object
     * @param zipPath The inside file path of zip package
     * @param file The file which will be zipped
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void addToZip(ZipOutputStream out, String zipPath, File file) throws FileNotFoundException, IOException {

        if (file == null || !file.exists()) {
            return;
        } else if (zipPath.endsWith(File.separator)) {
            zipPath += file.getName();
        }

        ZipEntry entry = new ZipEntry(zipPath);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int count;
        byte[] data = new byte[1024];

        out.putNextEntry(entry);
        while ((count = bis.read(data)) != -1) {
            out.write(data, 0, count);
        }
        bis.close();
    }

    /**
     * Get all output files
     */
    public ArrayList<File> getOutputFiles() {
//        return files;
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
        return zipFile;
    }

    private HashMap<String, String> checkMultiTrn(ArrayList<HashMap> expArr) {
        HashMap<String, String> ret = new HashMap();
        HashMap<String, Boolean> multiTrnFlgs = new HashMap();
        String exname;
        String subDir;

        for (int i = 0; i < expArr.size(); i++) {
            exname = getValueOr(expArr.get(i), "exname", "");
            if (!exname.equals("")) {
                subDir = getExName(expArr.get(i));
                if (multiTrnFlgs.containsKey(subDir)) {
                    multiTrnFlgs.put(subDir, true);
                } else {
                    multiTrnFlgs.put(subDir, false);
                }
            }
        }

        for (int i = 0; i < expArr.size(); i++) {
            exname = getValueOr(expArr.get(i), "exname", "");
            if (exname.equals("")) {
                ret.put("Experiment_" + i, "Experiment_" + i);
            } else {
                subDir = getExName(expArr.get(i));
                if (multiTrnFlgs.get(subDir)) {
                    ret.put(exname, exname);
                } else {
                    ret.put(exname, "");
                }
            }
        }

        return ret;
    }
}
