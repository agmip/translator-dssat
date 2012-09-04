package org.agmip.translators.dssat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import org.agmip.core.types.TranslatorOutput;
import org.agmip.translators.dssat.DssatControllerOutput;
import static org.agmip.translators.dssat.DssatCommonInput.getSectionData;
import static org.agmip.util.MapUtil.getObjectOr;

/**
 *
 * @author Meng Zhang
 */
public class DssatThreadOutput {

    public void execDssatTranslator(String arg0, LinkedHashMap result, ExecutorService executor) {
        ArrayList<LinkedHashMap> expArr = getObjectOr(result, "experiments", new ArrayList());
        ArrayList<LinkedHashMap> soilArr = getObjectOr(result, "soils", new ArrayList());
        ArrayList<LinkedHashMap> wthArr = getObjectOr(result, "weathers", new ArrayList());
        for (int i = 0; i < expArr.size(); i++) {
            LinkedHashMap expData = expArr.get(i);
            LinkedHashMap soilData = getSectionData(soilArr, "soil_id", getObjectOr(expData, "soil_id", ""));
            if (soilData != null) {
                expData.put("soil", soilData);
            }
            LinkedHashMap wthData = getSectionData(wthArr, "wst_id", getObjectOr(expData, "wst_id", ""));
            if (wthData != null) {
                expData.put("weather", wthData);
            }
            executor.execute(new TranslateRunner(new DssatControllerOutput(), expData, arg0));

        }

    }

    private class TranslateRunner implements Runnable {

        private TranslatorOutput translator;
        private LinkedHashMap data;
        private String outputDirectory;
//	private static Logger LOG = LoggerFactory.getLogger(TranslateRunner.class);

        public TranslateRunner(TranslatorOutput translator, LinkedHashMap data, String outputDirectory) {
            this.translator = translator;
            this.data = data;
            this.outputDirectory = outputDirectory;
        }

        @Override
        public void run() {
//		LOG.debug("Starting new thread!");
//            try {
            translator.writeFile(outputDirectory, data);
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }
    }
}
