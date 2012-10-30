package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.agmip.util.JSONAdapter;
import static org.agmip.util.MapUtil.*;

/**
 * DSSAT ACMO mini json Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatACMOJsonOutput extends DssatCommonOutput {

    /**
     * DSSAT ACMO mini json Data Output method
     *
     * @param arg0 file output path
     * @param result data holder object
     */
    @Override
    public void writeFile(String arg0, Map result) {

        // Initial variables
        HashMap record;       // Data holder for summary data
        BufferedWriter bwJ;                         // output object

        try {

            // Set default value for missing data
            setDefVal();

            // Get Data from input holder
            record = (HashMap) getObjectOr(result, "acmo", new HashMap());
            if (record.isEmpty()) {
                return;
            }

            // Initial BufferedWriter
            String fileName = "ACMO.json";
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwJ = new BufferedWriter(new FileWriter(outputFile));

            // Output finish
            bwJ.write(JSONAdapter.toJSON(record));
            bwJ.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
