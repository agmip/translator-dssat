package org.agmip.translators.dssat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
        new DssatTFileOutput()};
    private File zipFile;

    /**
     * ALL DSSAT Data Output method
     * 
     * @param arg0   file output path
     * @param result  data holder object
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void writeFiles(String arg0, LinkedHashMap result) throws FileNotFoundException, IOException {

        for (int i = 0; i < outputs.length; i++) {
            outputs[i].writeFile(arg0, result);
        }

        // compress all output files into one zip file
        ArrayList<File> files = getOutputFiles();
        File file;
        zipFile = new File(outputs[0].revisePath(arg0) + getZipFileName(files));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        ZipEntry entry;
        BufferedInputStream bis;
        byte[] data = new byte[1024];

        for (int i = 0; i < files.size(); i++) {
            file = files.get(i);
            if (file == null) {
                continue;
            }

            entry = new ZipEntry(file.getPath());
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
        ArrayList<File> files = new ArrayList();
        for (int i = 0; i < outputs.length; i++) {
            if (outputs[i] instanceof DssatWeatherOutput) {
                files.addAll(Arrays.asList(((DssatWeatherOutput) outputs[i]).getOutputFiles()));
            } else {
                files.add(outputs[i].getOutputFile());
            }
        }
        return files;
    }

    /**
     * Get output zip file name by using experiment file name
     * 
     * @param files the output file array
     */
    private String getZipFileName(ArrayList<File> files) {

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
