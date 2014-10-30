package org.agmip.translators.dssat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translation runner for multiple thread mode
 *
 * @author Meng Zhang
 */
public class DssatTranslateRunner implements Callable<File> {

    private final DssatCommonOutput translator;
    private Map data;
    private ArrayList<HashMap> dataArr;
    private final String outputDirectory;
    private static Logger LOG = LoggerFactory.getLogger(DssatTranslateRunner.class);

    public DssatTranslateRunner(DssatCommonOutput translator, Map data, String outputDirectory) {
        this.translator = translator;
        this.data = data;
        this.outputDirectory = outputDirectory;
    }

    public DssatTranslateRunner(DssatCommonOutput translator, ArrayList<HashMap> dataArr, String outputDirectory) {
        this.translator = translator;
        this.dataArr = dataArr;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public File call() throws Exception {
        LOG.debug("Starting new thread!");
        try {
            if (translator instanceof DssatBtachFile) {
                ((DssatBtachFile) translator).writeFile(outputDirectory, dataArr);
            } else {
                translator.writeFile(outputDirectory, data);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        File ret = translator.getOutputFile();

        if (ret == null) {
            LOG.debug("Job canceled!");
            throw new NoOutputFileException();
        } else {
            LOG.debug("Job done for " + ret.getName());
            return ret;
        }
    }

    class NoOutputFileException extends RuntimeException {
    }
}
