package org.agmip.translators.dssat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.agmip.ace.AceDataset;
import org.agmip.ace.AceExperiment;
import org.agmip.ace.AceSoil;
import org.agmip.ace.AceWeather;
import org.agmip.ace.io.AceParser;
import org.agmip.common.Functions;
import org.agmip.core.types.TranslatorOutput;
import org.agmip.util.JSONAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class DssatAcebOutput implements TranslatorOutput {
    
    private static final Logger LOG = LoggerFactory.getLogger(DssatAcebOutput.class);

    @Override
    public void writeFile(String outputDirectory, Map data) throws IOException {
        DssatControllerOutput translator = new DssatControllerOutput();
        translator.writeFile(outputDirectory, data);
    }
    
    public static ArrayList<File> writeFile(String outputDirectory, InputStream acebIS) throws IOException {
        DssatControllerOutput translator = new DssatControllerOutput();
        translator.writeFile(outputDirectory, readAceb(acebIS));
        return translator.getOutputFiles();
    }

    public static File writeZipFile(String outputDirectory, InputStream acebIS) throws IOException {
        DssatControllerOutput translator = new DssatControllerOutput();
        translator.writeFile(outputDirectory, readAceb(acebIS));
        translator.createZip();
        return translator.getOutputZipFile();
    }
    
    private static HashMap readAceb(InputStream fileStream) {
        HashMap data = new HashMap();
        try {
            AceDataset ace = AceParser.parseACEB(fileStream);
            ace.linkDataset();
            ArrayList<HashMap> arr;
            // Experiments
            arr = new ArrayList();
            for (AceExperiment exp : ace.getExperiments()) {
                arr.add(JSONAdapter.fromJSON(new String(exp.rebuildComponent())));
            }
            if (!arr.isEmpty()) {
                data.put("experiments", arr);
            }
            // Soils
            arr = new ArrayList();
            for (AceSoil soil : ace.getSoils()) {
                arr.add(JSONAdapter.fromJSON(new String(soil.rebuildComponent())));
            }
            if (!arr.isEmpty()) {
                data.put("soils", arr);
            }
            // Weathers
            arr = new ArrayList();
            for (AceWeather wth : ace.getWeathers()) {
                arr.add(JSONAdapter.fromJSON(new String(wth.rebuildComponent())));
            }
            if (!arr.isEmpty()) {
                data.put("weathers", arr);
            }
        } catch (Exception ex) {
            LOG.error(Functions.getStackTrace(ex));
        }
        return data;
    }
}
