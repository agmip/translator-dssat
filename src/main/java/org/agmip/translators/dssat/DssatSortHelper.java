package org.agmip.translators.dssat;

import java.util.Comparator;
import java.util.HashMap;
import static org.agmip.util.MapUtil.getValueOr;

/**
 * DSSAT Sorting Data helper Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
class DssatSortHelper implements Comparator<HashMap> {

    private String[] sortIds;
    private int decVal = 1;
    private int ascVal = -1;

    /**
     * Constructor, initial the array of sorting id
     *
     * @param sortIds array of id for sorting process, order in the array means
     * priority of sorting
     */
    public DssatSortHelper(String[] sortIds) {
        this(sortIds, true);
    }

    /**
     * Constructor, initial the array of sorting id
     *
     * @param sortIds array of id for sorting process, order in the array means
     * priority of sorting
     * @sortFlg true: descending; false:ascending
     */
    public DssatSortHelper(String[] sortIds, boolean sortFlg) {
        super();
        if (sortIds != null) {
            this.sortIds = sortIds;
        } else {
            this.sortIds = new String[0];
        }
        if (!sortFlg) {
            decVal = -1;
            ascVal = 1;
        }
    }

    /**
     * Compare two element in the array, by using sorting IDs.
     *
     * @param m1 Data record 1st
     * @param m2 Data record 2nd
     * @return
     */
    @Override
    public int compare(HashMap m1, HashMap m2) {
        double val1;
        double val2;
        for (String sortId : sortIds) {
            val1 = getValue(m1, sortId);
            val2 = getValue(m2, sortId);
            if (val1 > val2) {
                return decVal;
            } else if (val1 < val2) {
                return ascVal;
            }
        }
        return 0;
    }

    /**
     * Get number value by using key from map
     *
     * @param m Data record
     * @param key variable name
     * @return
     */
    private double getValue(HashMap m, String key) {
        try {
            return Double.parseDouble(getValueOr(m, key, ""));
        } catch (Exception e) {
            return Double.MIN_VALUE;
        }
    }
}
