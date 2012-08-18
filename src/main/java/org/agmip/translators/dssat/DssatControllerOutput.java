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
        new DssatRunFileOutput()
    };
    private File zipFile;
    private ArrayList<File> files = new ArrayList();

    /**
     * ALL DSSAT Data Output method
     *
     * @param arg0 file output path
     * @param results data holder object
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void writeFiles(String arg0, ArrayList<LinkedHashMap> results) throws FileNotFoundException, IOException {
        LinkedHashMap result;
        arg0 = revisePath(arg0);
        String exname;
        ArrayList<String> exnames = new ArrayList();

        // Write files
        for (int i = 0; i < results.size(); i++) {
            result = results.get(i);
            exname = getObjectOr(result, "exname", "Experiment_" + i);
            writeFile(arg0 + exname, result);
            exnames.add(exname);
        }

        // Write all batch files
        DssatBatchFileOutput batchTran = new DssatBatchFileOutput();
        batchTran.writeFile(arg0, results);
        if (batchTran.getOutputFile() != null) {
            files.add(batchTran.getOutputFile());
        }
        DssatRunFileOutput runTran = new DssatRunFileOutput();
        runTran.writeFile(arg0, results);
        if (runTran.getOutputFile() != null) {
            files.add(runTran.getOutputFile());
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
    public void writeFiles(String arg0, LinkedHashMap result) throws FileNotFoundException, IOException {

        // Write files
        writeFile(arg0, result);

        // compress all output files into one zip file
        zipFile = new File(revisePath(arg0) + getZipFileName());
        createZip(files);
    }

    /**
     * Write files and add file objects in the array
     *
     * @param arg0 file output path
     * @param result data holder object
     */
    private void writeFile(String arg0, LinkedHashMap result) {
        for (int i = 0; i < outputs.length; i++) {
            outputs[i].writeFile(arg0, result);
            if (outputs[i].getOutputFile() != null) {
                files.add(outputs[i].getOutputFile());
            }
        }
    }

    private void createZip(ArrayList<File> files, ArrayList<String> paths) throws FileNotFoundException, IOException {
    }

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
