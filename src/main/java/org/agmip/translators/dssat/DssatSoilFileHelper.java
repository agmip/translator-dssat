package org.agmip.translators.dssat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static org.agmip.util.MapUtil.*;

/**
 *
 * @author Meng Zhang
 */
public class DssatSoilFileHelper {

    private final HashSet<String> names = new HashSet();
    private final HashMap<String, String> hashToName = new HashMap();

    /**
     * Generate the soil file name for auto-generating (extend name not
     * included)
     *
     * @param soilData soil data holder
     * @return the soil id (10-bit)
     */
    public String getSoilID(Map soilData) {

        String hash = getObjectOr(soilData, "soil_id", "");

        if (hashToName.containsKey(hash)) {
            return hashToName.get(hash);
        } else {
            String soil_id;
            if (hash.length() > 10) {
                soil_id = hash.substring(0, 6) + "0001";
            } else if (hash.equals("")) {
                soil_id = "";
            } else {
                soil_id = hash;
                while (soil_id.length() < 10) {
                    soil_id += "_";
                }
            }
            int count;
            while (names.contains(soil_id)) {
                try {
                    count = Integer.parseInt(soil_id.substring(6, soil_id.length()));
                    count++;
                } catch (Exception e) {
                    count = 1;
                }
                soil_id = soil_id.substring(0, 6) + String.format("%04d", count);
            }
            names.add(soil_id);
            if (hash.equals("")) {
                hash = soil_id;
            }
            hashToName.put(hash, soil_id);
            return soil_id;
        }
    }
}
