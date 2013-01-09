package org.agmip.translators.dssat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.agmip.core.types.TranslatorOutput;
import org.agmip.translators.dssat.DssatControllerOutput;
import static org.agmip.translators.dssat.DssatCommonInput.getSectionDataWithNocopy;
import static org.agmip.util.MapUtil.getObjectOr;

/**
 *
 * @author Meng Zhang
 */
public class DssatThreadOutput {

    public void execDssatTranslator(String arg0, HashMap result) {
        ExecutorService executor = Executors.newFixedThreadPool(64);
        ArrayList<HashMap> expArr = getObjectOr(result, "experiments", new ArrayList());
        ArrayList<HashMap> soilArr = getObjectOr(result, "soils", new ArrayList());
        ArrayList<HashMap> wthArr = getObjectOr(result, "weathers", new ArrayList());
        ArrayList<Future<File>> files = new ArrayList();;
        for (int i = 0; i < expArr.size(); i++) {
            HashMap expData = expArr.get(i);
            HashMap soilData = getSectionDataWithNocopy(soilArr, "soil_id", getObjectOr(expData, "soil_id", ""));
            if (soilData != null) {
                expData.put("soil", soilData);
            }
            HashMap wthData = getSectionDataWithNocopy(wthArr, "wst_id", getObjectOr(expData, "wst_id", ""));
            if (wthData != null) {
                expData.put("weather", wthData);
            }
            files.add(executor.submit(new TranslateRunner(new DssatControllerOutput(), expData, arg0)));
        }

    }

    private class TranslateRunner implements Callable<File> {

        private DssatCommonOutput translator;
        private HashMap data;
        private String outputDirectory;
//	private static Logger LOG = LoggerFactory.getLogger(TranslateRunner.class);

        public TranslateRunner(DssatCommonOutput translator, HashMap data, String outputDirectory) {
            this.translator = translator;
            this.data = data;
            this.outputDirectory = outputDirectory;
        }

        @Override
        public File call() throws Exception {
//		LOG.debug("Starting new thread!");
            try {
                translator.writeFile(outputDirectory, data);
            } catch (Exception e) {
//                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            return translator.getOutputFile();
        }
    }
}
