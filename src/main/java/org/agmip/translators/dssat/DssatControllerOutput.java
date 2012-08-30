package org.agmip.translators.dssat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.agmip.translators.dssat.DssatCommonInput.getSectionData;
import static org.agmip.translators.dssat.DssatCommonOutput.revisePath;
import static org.agmip.util.MapUtil.getObjectOr;

/**
 *
 * @author Meng Zhang
 */
public class DssatControllerOutput {

    private DssatCommonOutput[] outputs = {
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
    private File zipFile;
    private ArrayList<File> files = new ArrayList();

    /**
     * ALL DSSAT Data Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeMultipleExp(String arg0, LinkedHashMap result) throws FileNotFoundException, IOException {

        arg0 = revisePath(arg0);
        String exname;
        ArrayList<String> exnames = new ArrayList();
        ArrayList<LinkedHashMap> expArr = getObjectOr(result, "experiments", new ArrayList());
        LinkedHashMap expData;
        ArrayList<LinkedHashMap> soilArr = getObjectOr(result, "soils", new ArrayList());
        ArrayList<LinkedHashMap> wthArr = getObjectOr(result, "weathers", new ArrayList());

        // Write files
        for (int i = 0; i < expArr.size(); i++) {
            expData = expArr.get(i);
            expData.put("soil", getSectionData(soilArr, "soil_id", getObjectOr(expData, "soil_id", "")));
            expData.put("weather", getSectionData(wthArr, "wst_id", getObjectOr(expData, "wst_id", "")));
            exname = getObjectOr(expData, "exname", "Experiment_" + i);
            writeSingleExp(arg0 + exname, expData);
            exnames.add(exname);
        }

        // If experiment data is included
        if (!expArr.isEmpty()) {
            // Write all batch files
            DssatBatchFileOutput batchTran = new DssatBatchFileOutput();
            batchTran.writeFile(arg0, expArr);
            if (batchTran.getOutputFile() != null) {
                files.add(batchTran.getOutputFile());
            }
            DssatRunFileOutput runTran = new DssatRunFileOutput();
            runTran.writeFile(arg0, expArr);
            if (runTran.getOutputFile() != null) {
                files.add(runTran.getOutputFile());
            }
        } // If only weather or soil data is included
        else {
            for (int i = 0; i < soilArr.size(); i++) {
                LinkedHashMap tmp = new LinkedHashMap();
                tmp.put("soil", soilArr.get(i));
                writeSingleExp(arg0, tmp);
            }
            for (int i = 0; i < wthArr.size(); i++) {
                LinkedHashMap tmp = new LinkedHashMap();
                tmp.put("weather", wthArr.get(i));
                writeSingleExp(arg0, tmp);
            }
        }

        // compress all output files into one zip file
        zipFile = new File(arg0 + "AGMIP_DSSAT.zip");
        createZip(files);

        // Delete the remained folders
        File dir;
        for (int i = 0; i < exnames.size(); i++) {
            dir = new File(arg0 + exnames.get(i));
            dir.delete();
        }
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
    public void writeFile(String arg0, LinkedHashMap result) throws FileNotFoundException, IOException {

        if (getObjectOr(result, "experiments", new ArrayList()).isEmpty()
                && getObjectOr(result, "soils", new ArrayList()).isEmpty()
                && getObjectOr(result, "weathers", new ArrayList()).isEmpty()) {
            // Write files
            writeSingleExp(arg0, result);

            // compress all output files into one zip file
            zipFile = new File(revisePath(arg0) + getZipFileName());
            createZip(files);

        } else {
            writeMultipleExp(arg0, result);
        }

    }

    /**
     * Write files and add file objects in the array
     *
     * @param arg0 file output path
     * @param result data holder object
     */
    private void writeSingleExp(String arg0, LinkedHashMap result) {
        for (int i = 0; i < outputs.length; i++) {
            outputs[i].writeFile(arg0, result);
            if (outputs[i].getOutputFile() != null) {
                files.add(outputs[i].getOutputFile());
            }
        }
    }

    /**
     * Compress the files in one zip
     *
     * @param files The array for files which need to be zipped
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void createZip(ArrayList<File> files) throws FileNotFoundException, IOException {

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        ZipEntry entry;
        BufferedInputStream bis;
        byte[] data = new byte[1024];
        File file;

        for (int i = 0; i < files.size(); i++) {
            file = files.get(i);
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
     * Get all output files
     */
    public ArrayList<File> getOutputFiles() {
        return files;
    }

    /**
     * Get output zip file name by using experiment file name
     *
     */
    private String getZipFileName() {

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
}
