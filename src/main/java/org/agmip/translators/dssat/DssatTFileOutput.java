package org.agmip.translators.dssat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.agmip.core.types.AdvancedHashMap;
import org.agmip.util.JSONAdapter;

/**
 * DSSAT Observation Data I/O API Class
 * 
 * @author Meng Zhang
 * @version 1.0
 */
public class DssatTFileOutput extends DssatCommonOutput {

    private File outputFile;

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * DSSAT Observation Data Output method
     *
     * @param arg0  file output path
     * @param result data holder object
     */
    @Override
    public void writeFile(String arg0, AdvancedHashMap result) {

        // Initial variables
        JSONAdapter adapter = new JSONAdapter();    // JSON Adapter
        AdvancedHashMap<String, Object> record;     // Data holder for daily data
        BufferedWriter bwT;                         // output object
        StringBuilder sbData = new StringBuilder();         // construct the data info in the output
        HashMap altTitleList = new HashMap();               // Define alternative fields for the necessary observation data fields; key is necessary field
        // TODO Add alternative fields here
        ArrayList optTitleList2 = getAllTitleList();         // Define optional observation data fields
        LinkedHashMap titleOutput;                          // contain output data field id
        DssatObservedData obvDataList = new DssatObservedData();    // Varibale list definition

        try {

            // Set default value for missing data
            setDefVal();

            // Get Data from input holder
            AdvancedHashMap obvFile = adapter.exportRecord((Map) result.getOr("time_course", result));
            AdvancedHashMap obvTFile = adapter.exportRecord((Map) obvFile.getOr("average", obvFile));
            ArrayList observeRecordsSections = ((ArrayList) obvTFile.getOr("data", new ArrayList()));

            // Initial BufferedWriter
            String exName = getExName(obvTFile);
            String fileName = exName;
            if (fileName.equals("")) {
                fileName = "a.tmp";
            } else {
                fileName = fileName.substring(0, fileName.length() - 2) + "." + fileName.substring(fileName.length() - 2) + "T";
            }
            arg0 = revisePath(arg0);
            outputFile = new File(arg0 + fileName);
            bwT = new BufferedWriter(new FileWriter(outputFile));

            // Output Observation File
            // Titel Section
            sbData.append(String.format("*EXP.DATA (T): %1$-10s %2$s\r\n\r\n", exName, obvTFile.getOr("local_name", defValC).toString()));

            // Loop Data Sections
            ArrayList observeRecords;
            for (int id = 0; id < observeRecordsSections.size(); id++) {
                observeRecords = (ArrayList) observeRecordsSections.get(id);
                titleOutput = new LinkedHashMap();

                // Get first record of observed data
                AdvancedHashMap fstObvData;
                if (observeRecords.isEmpty()) {
                    fstObvData = new AdvancedHashMap();
                } else {
                    fstObvData = adapter.exportRecord((Map) observeRecords.get(0));
                }

                // Check if which field is available
                for (Object key : fstObvData.keySet()) {
                    // check which optional data is exist, if not, remove from map
                    if (obvDataList.isTimeCourseData(key)) {
                        titleOutput.put(key, key);

                    } // check if the additional data is too long to output
                    else if (key.toString().length() <= 5) {
                        if (!key.equals("trno") && !key.equals("date")) {
                            titleOutput.put(key, key);
                        }

                    } // If it is too long for DSSAT, give a warning message
                    else {
                        sbError.append("! Waring: Unsuitable data for DSSAT observed data (too long): [").append(key).append("]\r\n");
                    }
                }

                // Check if all necessary field is available    // TODO conrently unuseful
                for (Object title : altTitleList.keySet()) {

                    // check which optional data is exist, if not, remove from map
                    if (!fstObvData.containsKey(title)) {

                        if (fstObvData.containsKey(altTitleList.get(title))) {
                            titleOutput.put(title, altTitleList.get(title));
                        } else {
                            // TODO throw new Exception("Incompleted record because missing data : [" + title + "]");
                            sbError.append("! Waring: Incompleted record because missing data : [").append(title).append("]\r\n");
                        }

                    } else {
                    }
                }

                // decompress observed data
                decompressData(observeRecords);
                // Observation Data Section
                Object[] titleOutputId = titleOutput.keySet().toArray();
                for (int i = 0; i < (titleOutputId.length / 39 + titleOutputId.length % 39 == 0 ? 0 : 1); i++) {

                    sbData.append("@TRNO   DATE");
                    int limit = Math.min(titleOutputId.length, (i + 1) * 39);
                    for (int j = i * 39; j < limit; j++) {
                        sbData.append(String.format("%1$6s", titleOutput.get(titleOutputId[j]).toString().toUpperCase()));
                    }
                    sbData.append("\r\n");

                    for (int j = 0; j < observeRecords.size(); j++) {

                        record = adapter.exportRecord((Map) observeRecords.get(j));
                        sbData.append(String.format(" %1$5s", record.getOr("trno", defValI).toString()));
                        sbData.append(String.format(" %1$5d", Integer.parseInt(formatDateStr(record.getOr("date", defValI).toString()))));
                        for (int k = i * 39; k < limit; k++) {

                            if (obvDataList.isDapDateType(titleOutputId[k], titleOutput.get(titleOutputId[k]))) {
                                String pdate = (String) ((AdvancedHashMap) result.getOr("experiment", new AdvancedHashMap())).getOr("pdate", defValD); // TODO need be updated after ear;y version
                                sbData.append(String.format("%1$6s", formatDateStr(pdate, record.getOr(titleOutput.get(titleOutputId[k]).toString(), defValI).toString())));
                            } else if (obvDataList.isDateType(titleOutputId[k])) {
                                sbData.append(String.format("%1$6s", formatDateStr(record.getOr(titleOutput.get(titleOutputId[k]).toString(), defValI).toString())));
                            } else {
                                sbData.append(" ").append(formatNumStr(5, record.getOr(titleOutput.get(titleOutputId[k]).toString(), defValI).toString()));
                            }

                        }
                        sbData.append("\r\n");
                    }
                }
                // Add section deviding line
                sbData.append("\r\n");
            }

            // Output finish
            bwT.write(sbError.toString());
            bwT.write(sbData.toString());
            bwT.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Set default value for missing data
     *
     * @version 1.0
     */
    private void setDefVal() {

        // defValD = ""; No need to set default value for Date type in Observation file
        defValR = "-99";
        defValC = "";
        defValI = "-99";
    }

    /**
     * Create the list of titles for all observed data
     *
     */
    private ArrayList getAllTitleList() {

        ArrayList ret = new ArrayList();
        ret.add("e#ad");
        ret.add("chtd");
        ret.add("ni#m");
        ret.add("amlc");
        ret.add("amls");
        ret.add("awag");
        ret.add("bwad");
        ret.add("casm");
        ret.add("cdad");
        ret.add("cday");
        ret.add("cdvd");
        ret.add("cew");
        ret.add("cfad");
        ret.add("cgrd");
        ret.add("chad");
        ret.add("chn%");
        ret.add("chnad");
        ret.add("chp%");
        ret.add("chpad");
        ret.add("chwad");
        ret.add("cl%d");
        ret.add("clai");
        ret.add("clfm");
        ret.add("cmad");
        ret.add("cn%d");
        ret.add("cnad");
        ret.add("co20c");
        ret.add("co2fp");
        ret.add("co2sc");
        ret.add("cp%d");
        ret.add("cpad");
        ret.add("cpo%");
        ret.add("crad");
        ret.add("crlf");
        ret.add("crlv");
        ret.add("crtm");
        ret.add("cs%d");
        ret.add("csd#");
        ret.add("csdm");
        ret.add("csh#");
        ret.add("cshm");
        ret.add("cstm");
        ret.add("cwad");
        ret.add("cwid");
        ret.add("cwpd");
        ret.add("dap");
        ret.add("das");
        ret.add("dasm");
        ret.add("day");
        ret.add("daylf");
        ret.add("dla");
        ret.add("dla%");
        ret.add("dlai");
        ret.add("dlfm");
        ret.add("dnag");
        ret.add("dpag");
        ret.add("dpo%");
        ret.add("drlf");
        ret.add("drlv");
        ret.add("drnc");
        ret.add("drtm");
        ret.add("dsd#");
        ret.add("dsdm");
        ret.add("dsh#");
        ret.add("dshm");
        ret.add("dstm");
        ret.add("dwad");
        ret.add("dwag");
        ret.add("eaid");
        ret.add("efac");
        ret.add("efad");
        ret.add("egwa");
        ret.add("egws");
        ret.add("eoad");
        ret.add("epaa");
        ret.add("epad");
        ret.add("esaa");
        ret.add("esac");
        ret.add("esad");
        ret.add("etaa");
        ret.add("etac");
        ret.add("etad");
        ret.add("ewad");
        ret.add("ewsd");
        ret.add("falg");
        ret.add("fali");
        ret.add("faw");
        ret.add("fden");
        ret.add("fl3c");
        ret.add("fl3n");
        ret.add("fl4c");
        ret.add("fl4n");
        ret.add("flagw");
        ret.add("flbd");
        ret.add("fldd");
        ret.add("flni");
        ret.add("flph");
        ret.add("flti");
        ret.add("flur");
        ret.add("frod");
        ret.add("fuhy");
        ret.add("fwad");
        ret.add("g#ad");
        ret.add("g#pd");
        ret.add("g#sd");
        ret.add("gc%d");
        ret.add("gl%d");
        ret.add("gn%d");
        ret.add("gnad");
        ret.add("gp%d");
        ret.add("gpad");
        ret.add("grad");
        ret.add("gstd");
        ret.add("gwad");
        ret.add("gwgd");
        ret.add("h2oa");
        ret.add("hcrd");
        ret.add("hiad");
        ret.add("hind");
        ret.add("hipd");
        ret.add("hum1");
        ret.add("hum2");
        ret.add("hum3");
        ret.add("hum4");
        ret.add("hum5");
        ret.add("hwad");
        ret.add("hyad");
        ret.add("infd");
        ret.add("ir#c");
        ret.add("irrc");
        ret.add("irrd");
        ret.add("l#ir");
        ret.add("l#sd");
        ret.add("la%d");
        ret.add("lafd");
        ret.add("laid");
        ret.add("lald");
        ret.add("laln");
        ret.add("lapd");
        ret.add("lard");
        ret.add("lc0d");
        ret.add("lc1d");
        ret.add("lc2d");
        ret.add("lc3d");
        ret.add("lc4d");
        ret.add("lc5d");
        ret.add("lcnf");
        ret.add("lctd");
        ret.add("ldad");
        ret.add("li%d");
        ret.add("li%n");
        ret.add("liwad");
        ret.add("lmhn");
        ret.add("lmln");
        ret.add("ln%d");
        ret.add("ln0d");
        ret.add("ln1d");
        ret.add("ln2d");
        ret.add("ln3d");
        ret.add("ln4d");
        ret.add("ln5d");
        ret.add("lnad");
        ret.add("lntd");
        ret.add("lnum");
        ret.add("lp%d");
        ret.add("lp0d");
        ret.add("lp1d");
        ret.add("lp2d");
        ret.add("lp3d");
        ret.add("lp4d");
        ret.add("lp5d");
        ret.add("lpad");
        ret.add("lptd");
        ret.add("lrsd");
        ret.add("lwad");
        ret.add("lwpd");
        ret.add("mec0d");
        ret.add("mec1d");
        ret.add("mec2d");
        ret.add("mec3d");
        ret.add("mec4d");
        ret.add("mec5d");
        ret.add("mectd");
        ret.add("men0d");
        ret.add("men1d");
        ret.add("men2d");
        ret.add("men3d");
        ret.add("men4d");
        ret.add("men5d");
        ret.add("mentd");
        ret.add("mep0d");
        ret.add("mep1d");
        ret.add("mep2d");
        ret.add("mep3d");
        ret.add("mep4d");
        ret.add("mep5d");
        ret.add("meptd");
        ret.add("mrad");
        ret.add("n%hn");
        ret.add("n%ln");
        ret.add("napc");
        ret.add("ndnc");
        ret.add("nfgd");
        ret.add("nfpd");
        ret.add("nfsd");
        ret.add("nftid");
        ret.add("nfud");
        ret.add("nfxc");
        ret.add("nfxd");
        ret.add("nfxm");
        ret.add("nh10d");
        ret.add("nh1d");
        ret.add("nh2d");
        ret.add("nh3d");
        ret.add("nh4d");
        ret.add("nh5d");
        ret.add("nh6d");
        ret.add("nh7d");
        ret.add("nh8d");
        ret.add("nh9d");
        ret.add("nhtd");
        ret.add("nhu1");
        ret.add("nhu2");
        ret.add("nhu3");
        ret.add("nhu4");
        ret.add("nhu5");
        ret.add("niad");
        ret.add("nicm");
        ret.add("nimc");
        ret.add("nitc");
        ret.add("nitd");
        ret.add("nlcc");
        ret.add("nmnc");
        ret.add("no10d");
        ret.add("no1d");
        ret.add("no2d");
        ret.add("no3d");
        ret.add("no4d");
        ret.add("no5d");
        ret.add("no6d");
        ret.add("no7d");
        ret.add("no8d");
        ret.add("no9d");
        ret.add("noad");
        ret.add("nspav");
        ret.add("nstd");
        ret.add("ntop");
        ret.add("nuac");
        ret.add("nuad");
        ret.add("nupc");
        ret.add("nupr");
        ret.add("nwad");
        ret.add("o#ad");
        ret.add("obs_trt_id");
        ret.add("omac");
        ret.add("owad");
        ret.add("owgd");
        ret.add("oxrn");
        ret.add("p#ad");
        ret.add("p#am");
        ret.add("pac1d");
        ret.add("pac2d");
        ret.add("pac3d");
        ret.add("pac4d");
        ret.add("pac5d");
        ret.add("pactd");
        ret.add("pari");
        ret.add("parue");
        ret.add("pavtd");
        ret.add("phad");
        ret.add("phan");
        ret.add("pi#m");
        ret.add("picm");
        ret.add("pimc");
        ret.add("pittd");
        ret.add("plb1d");
        ret.add("plb2d");
        ret.add("plb3d");
        ret.add("plb4d");
        ret.add("plb5d");
        ret.add("plbtd");
        ret.add("plcc");
        ret.add("plp%d");
        ret.add("plpad");
        ret.add("pm%m");
        ret.add("pmad");
        ret.add("pmnc");
        ret.add("prec");
        ret.add("pst1a");
        ret.add("pst1d");
        ret.add("pst2a");
        ret.add("pst2d");
        ret.add("pst3d");
        ret.add("pst4d");
        ret.add("pst5d");
        ret.add("psttd");
        ret.add("ptf");
        ret.add("pupc");
        ret.add("pupd");
        ret.add("pwad");
        ret.add("pwdd");
        ret.add("rcnf");
        ret.add("rdad");
        ret.add("rdpd");
        ret.add("resc");
        ret.add("resnc");
        ret.add("respc");
        ret.add("rgrd");
        ret.add("rl10d");
        ret.add("rl1d");
        ret.add("rl2d");
        ret.add("rl3d");
        ret.add("rl4d");
        ret.add("rl5d");
        ret.add("rl6d");
        ret.add("rl7d");
        ret.add("rl8d");
        ret.add("rl9d");
        ret.add("rlad");
        ret.add("rlwd");
        ret.add("rn%d");
        ret.add("rnad");
        ret.add("rnua");
        ret.add("rofc");
        ret.add("rofd");
        ret.add("rs%d");
        ret.add("rsad");
        ret.add("rsfp");
        ret.add("rsnad");
        ret.add("rspad");
        ret.add("rspd");
        ret.add("rsvn");
        ret.add("rswad");
        ret.add("rtmpd");
        ret.add("rtopd");
        ret.add("rtp%d");
        ret.add("rtpad");
        ret.add("rtwm");
        ret.add("rwad");
        ret.add("rwld");
        ret.add("s#ad");
        ret.add("s#pd");
        ret.add("s1c0d");
        ret.add("s1c1d");
        ret.add("s1c2d");
        ret.add("s1c3d");
        ret.add("s1c4d");
        ret.add("s1c5d");
        ret.add("s1ctd");
        ret.add("s1n0d");
        ret.add("s1n1d");
        ret.add("s1n2d");
        ret.add("s1n3d");
        ret.add("s1n4d");
        ret.add("s1n5d");
        ret.add("s1ntd");
        ret.add("s1p0d");
        ret.add("s1p1d");
        ret.add("s1p2d");
        ret.add("s1p3d");
        ret.add("s1p4d");
        ret.add("s1p5d");
        ret.add("s1ptd");
        ret.add("s2c1d");
        ret.add("s2c2d");
        ret.add("s2c3d");
        ret.add("s2c4d");
        ret.add("s2c5d");
        ret.add("s2ctd");
        ret.add("s2n1d");
        ret.add("s2n2d");
        ret.add("s2n3d");
        ret.add("s2n4d");
        ret.add("s2n5d");
        ret.add("s2ntd");
        ret.add("s3c1d");
        ret.add("s3c2d");
        ret.add("s3c3d");
        ret.add("s3c4d");
        ret.add("s3c5d");
        ret.add("s3ctd");
        ret.add("s3n1d");
        ret.add("s3n2d");
        ret.add("s3n3d");
        ret.add("s3n4d");
        ret.add("s3n5d");
        ret.add("s3ntd");
        ret.add("said");
        ret.add("sc1d");
        ret.add("sc2d");
        ret.add("sc3d");
        ret.add("sc4d");
        ret.add("sc5d");
        ret.add("scdd");
        ret.add("scnf");
        ret.add("sctd");
        ret.add("scwa");
        ret.add("sdad");
        ret.add("sdmpd");
        ret.add("sdn%d");
        ret.add("sdnad");
        ret.add("sdopd");
        ret.add("sdp%d");
        ret.add("sdpad");
        ret.add("sdwad");
        ret.add("sdwt");
        ret.add("sead");
        ret.add("sedm");
        ret.add("senc0");
        ret.add("sencs");
        ret.add("senl0");
        ret.add("senls");
        ret.add("senn0");
        ret.add("senns");
        ret.add("sennt");
        ret.add("senwt");
        ret.add("sgsb");
        ret.add("sh%d");
        ret.add("shad");
        ret.add("shmpd");
        ret.add("shnd");
        ret.add("shopd");
        ret.add("shp%d");
        ret.add("shpad");
        ret.add("shwad");
        ret.add("sl");
        ret.add("slad");
        ret.add("slhn");
        ret.add("slln");
        ret.add("slmpd");
        ret.add("slnd");
        ret.add("slopd");
        ret.add("slp%d");
        ret.add("slpad");
        ret.add("slpf");
        ret.add("sn%d");
        ret.add("sn1d");
        ret.add("sn2d");
        ret.add("sn3d");
        ret.add("sn4d");
        ret.add("sn5d");
        ret.add("snad");
        ret.add("sndd");
        ret.add("snn0c");
        ret.add("snn1c");
        ret.add("snp0c");
        ret.add("snp1c");
        ret.add("sntd");
        ret.add("snw0c");
        ret.add("snw1c");
        ret.add("socd");
        ret.add("sp#p");
        ret.add("sp%d");
        ret.add("spad");
        ret.add("spam");
        ret.add("ssad");
        ret.add("stc0d");
        ret.add("stc1d");
        ret.add("stc2d");
        ret.add("stc3d");
        ret.add("stc4d");
        ret.add("stc5d");
        ret.add("stctd");
        ret.add("stn0d");
        ret.add("stn1d");
        ret.add("stn2d");
        ret.add("stn3d");
        ret.add("stn4d");
        ret.add("stn5d");
        ret.add("stntd");
        ret.add("suad");
        ret.add("sugd");
        ret.add("suid");
        ret.add("sw10d");
        ret.add("sw1d");
        ret.add("sw2d");
        ret.add("sw3d");
        ret.add("sw4d");
        ret.add("sw5d");
        ret.add("sw6d");
        ret.add("sw7d");
        ret.add("sw8d");
        ret.add("sw9d");
        ret.add("swad");
        ret.add("swpd");
        ret.add("swxd");
        ret.add("t#ad");
        ret.add("t#pd");
        ret.add("t#sd");
        ret.add("taid");
        ret.add("tdrw");
        ret.add("tdwa");
        ret.add("tfgd");
        ret.add("tfon");
        ret.add("tfpd");
        ret.add("tgav");
        ret.add("tgnn");
        ret.add("tkill");
        ret.add("tnad");
        ret.add("tpad");
        ret.add("trad");
        ret.add("trrd");
        ret.add("tuna");
        ret.add("twad");
        ret.add("un%d");
        ret.add("unad");
        ret.add("up%d");
        ret.add("upad");
        ret.add("uwad");
        ret.add("uyad");
        ret.add("vbc5");
        ret.add("vbc6");
        ret.add("vn%d");
        ret.add("vnad");
        ret.add("vp%d");
        ret.add("vpad");
        ret.add("vrnf");
        ret.add("wavr");
        ret.add("wfgd");
        ret.add("wfpd");
        ret.add("wfsd");
        ret.add("wftd");
        ret.add("wftid");
        ret.add("wlvg");
        ret.add("wsgd");
        ret.add("wspav");
        ret.add("wspd");
        ret.add("wupr");
        ret.add("year");

        return ret;
    }
}
