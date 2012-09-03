package org.agmip.translators.dssat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import org.agmip.util.JSONAdapter;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
/**
 *
 * @author Meng Zhang
 */
public class DssatAcmoCsvTranslatorTest {

    DssatOutputFileInput obDssatOutputFileInput;
    DssatAcmoCsvTranslator obDssatAcmoCsvTanslator;

    @Before
    public void setUp() throws Exception {
        obDssatOutputFileInput = new DssatOutputFileInput();
        obDssatAcmoCsvTanslator = new DssatAcmoCsvTranslator();
    }

    @Test
    public void test() throws IOException, Exception {
        LinkedHashMap result;

        String filePath = "src\\test\\java\\org\\agmip\\translators\\dssat\\testCsv.ZIP";
        result = obDssatOutputFileInput.readFile(filePath);
//        System.out.println(JSONAdapter.toJSON(result));
        File f = new File("outputOut.txt");
        BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
        bo.write(JSONAdapter.toJSON(result).getBytes());
        bo.close();
        f.delete();

//        result = new ArrayList<LinkedHashMap>();
//        result.add(JSONAdapter.fromJSON("{\"eid\":\"c279aebd61\", \"clim_id\":\"0XXX\", \"clim_rep\":\"1\", \"rap_id\":\"1\", \"region\":\"NA\", \"institution\":\"KS\", \"wsta_id\":\"KSAS8101\", \"soil_id\":\"IBWH980018\", \"fl_lat\":\"\", \"fl_long\":\"\", \"crid\":\"WHT\", \"cul_id\":\"IB0488\", \"irop\":\"\", \"ti_#\":\"0\", \"tiimp\":\"\", \"exname\":\"KSAS8101WH_1\"}"));
//        result.add(JSONAdapter.fromJSON("{\"eid\":\"7f2190db1a\", \"clim_id\":\"0XXX\", \"clim_rep\":\"1\", \"rap_id\":\"1\", \"region\":\"NA\", \"institution\":\"KS\", \"wsta_id\":\"KSAS8101\", \"soil_id\":\"IBWH980018\", \"fl_lat\":\"\", \"fl_long\":\"\", \"crid\":\"WHT\", \"cul_id\":\"IB0488\", \"irop\":\"\", \"ti_#\":\"0\", \"tiimp\":\"\", \"exname\":\"KSAS8101WH_2\"}"));
//        result.add(JSONAdapter.fromJSON("{\"eid\":\"56f94b40d0\", \"clim_id\":\"0XXX\", \"clim_rep\":\"1\", \"rap_id\":\"1\", \"region\":\"NA\", \"institution\":\"UF\", \"wsta_id\":\"UFGA8201\", \"soil_id\":\"IBMZ910014\", \"fl_lat\":\"29.630\", \"fl_long\":\"-82.370\", \"crid\":\"MAZ\", \"cul_id\":\"IB0035\", \"irop\":\"IR001\", \"ti_#\":\"0\", \"tiimp\":\"\", \"exname\":\"UFGA8201MZ_1\"}"));
//        result.add(JSONAdapter.fromJSON("{\"eid\":\"873488b13f\", \"clim_id\":\"0XXX\", \"clim_rep\":\"1\", \"rap_id\":\"1\", \"region\":\"NA\", \"institution\":\"UF\", \"wsta_id\":\"UFGA8201\", \"soil_id\":\"IBMZ910014\", \"fl_lat\":\"29.630\", \"fl_long\":\"-82.370\", \"crid\":\"MAZ\", \"cul_id\":\"IB0035\", \"irop\":\"\", \"ti_#\":\"0\", \"tiimp\":\"\", \"exname\":\"UFGA8202MZ_1\"}"));
//        result.add(JSONAdapter.fromJSON("{\"eid\":\"11f2d3cc43\", \"clim_id\":\"0XXX\", \"clim_rep\":\"1\", \"rap_id\":\"1\", \"region\":\"NA\", \"institution\":\"SW\", \"wsta_id\":\"SWSW7501\", \"soil_id\":\"IBWH980019\", \"fl_lat\":\"\", \"fl_long\":\"\", \"crid\":\"WHT\", \"cul_id\":\"IB1500\", \"irop\":\"\", \"ti_#\":\"0\", \"tiimp\":\"\", \"exname\":\"SWSW7501WH_1\"}"));
        obDssatAcmoCsvTanslator.writeCsvFile("", filePath);
        File file = obDssatAcmoCsvTanslator.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertTrue(file.getName().equals("ACMO.csv"));
            assertTrue(file.delete());
        }
    }
}
