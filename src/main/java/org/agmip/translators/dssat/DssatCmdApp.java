package org.agmip.translators.dssat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import org.agmip.util.JSONAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Meng Zhang
 */
public class DssatCmdApp {

    private static boolean isCompressed = false;
    private static boolean isToModel = false;
    private static String inputPath = null;
    private static String outputPath = "DSSAT";
    private static final Logger LOG = LoggerFactory.getLogger(DssatCmdApp.class);

    public static void main(String... args) throws IOException {
        readCommand(args);
        if (isToModel) {
            LOG.info("Translate {} to DSSAT...", new File(inputPath).getName());
            DssatControllerOutput translator = new DssatControllerOutput();
            translator.writeFile(outputPath, readJson());
            if (isCompressed) {
                translator.createZip();
            }
        } else {
            LOG.info("Translate {} to JSON...", new File(inputPath).getName());
            DssatControllerInput translator = new DssatControllerInput();
            Map result = translator.readFile(inputPath);
            BufferedOutputStream bo;
            outputPath += File.separator;
            File f = new File(outputPath + new File(outputPath).getName().replaceAll("\\.\\w+$", ".json"));
            bo = new BufferedOutputStream(new FileOutputStream(f));

            // Output json for reading
            bo.write(JSONAdapter.toJSON(result).getBytes());
            bo.flush();
            bo.close();
        }
        LOG.info("Job done!");
    }

    private static void readCommand(String[] args) {

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-zip")) {
                isCompressed = true;
            } else if (args[i].toUpperCase().endsWith(".JSON")) {
                isToModel = true;
                inputPath = args[i];
                LOG.info("Read from {}", inputPath);
            } else if (args[i].toUpperCase().endsWith(".ZIP")) {
                isToModel = false;
                inputPath = args[i];
                LOG.info("Read from {}", inputPath);
            } else {
                outputPath = args[i];
                if (outputPath != null && !"".equals(outputPath)) {
                    outputPath = new File(outputPath).getPath() + File.separator + "DSSAT";
                } else {
                    outputPath = "DSSAT";
                }
                LOG.info("Output to {}", outputPath);
            }
        }
    }

    private static Map readJson() throws IOException {
        return JSONAdapter.fromJSONFile(inputPath);
    }
}
