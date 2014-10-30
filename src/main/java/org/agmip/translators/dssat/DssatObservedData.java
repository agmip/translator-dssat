package org.agmip.translators.dssat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Define observed data codes
 *
 * @author Meng Zhang
 */
public enum DssatObservedData {
    INSTANCE;

    private final HashSet summaryList = new HashSet();        // Define the variables used in AFile
    private final HashSet timeCourseList = new HashSet();     // Define the variables used in TFile
    private final HashSet dateTypeList = new HashSet();       // Define the variables of data types
    private final HashMap dapDateTypeList = new HashMap();    // Define the variables of data types comes with day after planting (dap) variable; value means dap variable code

    DssatObservedData() {

        // Summary variables list
        summaryList.add("etcm");
        summaryList.add("prcm");
        summaryList.add("hdate");
        summaryList.add("adap");
        summaryList.add("cwam");
        summaryList.add("cwams");
        summaryList.add("hmah");
        summaryList.add("hwah");
        summaryList.add("hwahs");
        summaryList.add("hyah");
        summaryList.add("hyahs");
        summaryList.add("mdap");
        summaryList.add("r8aps");
        summaryList.add("adaps");
        summaryList.add("chtds");
        summaryList.add("hmahs");
        summaryList.add("adat");
        summaryList.add("adoy");
        summaryList.add("ap1d");
        summaryList.add("br1d");
        summaryList.add("br2d");
        summaryList.add("br3d");
        summaryList.add("br4d");
        summaryList.add("bwah");
        summaryList.add("bwam");
        summaryList.add("cdwa");
        summaryList.add("cfah");
        summaryList.add("chta");
        summaryList.add("chwa");
        summaryList.add("cnaa");
        summaryList.add("cnam");
        summaryList.add("cpaa");
        summaryList.add("cpam");
        summaryList.add("cwaa");
        summaryList.add("drcm");
        summaryList.add("drid");
        summaryList.add("dwap");
        summaryList.add("e#am");
        summaryList.add("e#um");
        summaryList.add("eemd");
        summaryList.add("eoaa");
        summaryList.add("epac");
        summaryList.add("epcm");
        summaryList.add("escm");
        summaryList.add("ewum");
        summaryList.add("fdat");
        summaryList.add("fwah");
        summaryList.add("gl%m");
        summaryList.add("gn%m");
        summaryList.add("gnam");
        summaryList.add("gp%m");
        summaryList.add("gpam");
        summaryList.add("gw%m");
        summaryList.add("gwam");
        summaryList.add("gwgm");
        summaryList.add("gwpm");
        summaryList.add("gyam");
        summaryList.add("gypm");
        summaryList.add("gyvm");
        summaryList.add("h#am");
        summaryList.add("h#gm");
        summaryList.add("h#um");
        summaryList.add("hastg");
        summaryList.add("hdap");
        summaryList.add("hiam");
        summaryList.add("hipm");
        summaryList.add("hprh");
        summaryList.add("hwac");
        summaryList.add("hwam");
        summaryList.add("hwum");
        summaryList.add("hyam");
        summaryList.add("icsw");
        summaryList.add("idap");
        summaryList.add("idat");
        summaryList.add("ir#m");
        summaryList.add("ircm");
        summaryList.add("l#sm");
        summaryList.add("l#sx");
        summaryList.add("laix");
        summaryList.add("lf3d");
        summaryList.add("lf5d");
        summaryList.add("liwam");
        summaryList.add("llfd");
        summaryList.add("lwam");
        summaryList.add("mdat");
        summaryList.add("mdat2");
        summaryList.add("mdoy");
        summaryList.add("niam");
        summaryList.add("nlcm");
        summaryList.add("nucm");
        summaryList.add("ocam");
        summaryList.add("oid");
        summaryList.add("oid");
        summaryList.add("oid");
        summaryList.add("onam");
        summaryList.add("p#am");
        summaryList.add("pd1p");
        summaryList.add("pd1t");
        summaryList.add("pdfp");
        summaryList.add("pdft");
        summaryList.add("pwam");
        summaryList.add("r1at");
        summaryList.add("r2at");
        summaryList.add("r3at");
        summaryList.add("r4at");
        summaryList.add("r5at");
        summaryList.add("r6at");
        summaryList.add("r7at");
        summaryList.add("r8ap");
        summaryList.add("r8at");
        summaryList.add("r9at");
        summaryList.add("rnah");
        summaryList.add("rocm");
        summaryList.add("rpah");
        summaryList.add("snam");
        summaryList.add("spam");
        summaryList.add("sqdat");
        summaryList.add("sraa");
        summaryList.add("swxm");
        summaryList.add("t#am");
        summaryList.add("tdap");
        summaryList.add("tdat");
        summaryList.add("tham");
        summaryList.add("tl#c");
        summaryList.add("tnah");
        summaryList.add("tnim");
        summaryList.add("tspd");
        summaryList.add("twah");
        summaryList.add("twam");
        summaryList.add("un%h");
        summaryList.add("unam");
        summaryList.add("upam");
        summaryList.add("uwah");
        summaryList.add("uyah");
        summaryList.add("vwam");
        summaryList.add("z21d");
        summaryList.add("z30d");
        summaryList.add("z31d");
        summaryList.add("z37d");
        summaryList.add("z39d");

        // Time-course variable list
        timeCourseList.add("e#ad");
        timeCourseList.add("chtd");
        timeCourseList.add("ni#m");
        timeCourseList.add("amlc");
        timeCourseList.add("amls");
        timeCourseList.add("awag");
        timeCourseList.add("bwad");
        timeCourseList.add("casm");
        timeCourseList.add("cdad");
        timeCourseList.add("cday");
        timeCourseList.add("cdvd");
        timeCourseList.add("cew");
        timeCourseList.add("cfad");
        timeCourseList.add("cgrd");
        timeCourseList.add("chad");
        timeCourseList.add("chn%");
        timeCourseList.add("chnad");
        timeCourseList.add("chp%");
        timeCourseList.add("chpad");
        timeCourseList.add("chwad");
        timeCourseList.add("cl%d");
        timeCourseList.add("clai");
        timeCourseList.add("clfm");
        timeCourseList.add("cmad");
        timeCourseList.add("cn%d");
        timeCourseList.add("cnad");
        timeCourseList.add("co20c");
        timeCourseList.add("co2fp");
        timeCourseList.add("co2sc");
        timeCourseList.add("cp%d");
        timeCourseList.add("cpad");
        timeCourseList.add("cpo%");
        timeCourseList.add("crad");
        timeCourseList.add("crlf");
        timeCourseList.add("crlv");
        timeCourseList.add("crtm");
        timeCourseList.add("cs%d");
        timeCourseList.add("csd#");
        timeCourseList.add("csdm");
        timeCourseList.add("csh#");
        timeCourseList.add("cshm");
        timeCourseList.add("cstm");
        timeCourseList.add("cwad");
        timeCourseList.add("cwid");
        timeCourseList.add("cwpd");
        timeCourseList.add("dap");
        timeCourseList.add("das");
        timeCourseList.add("dasm");
        timeCourseList.add("day");
        timeCourseList.add("daylf");
        timeCourseList.add("dla");
        timeCourseList.add("dla%");
        timeCourseList.add("dlai");
        timeCourseList.add("dlfm");
        timeCourseList.add("dnag");
        timeCourseList.add("dpag");
        timeCourseList.add("dpo%");
        timeCourseList.add("drlf");
        timeCourseList.add("drlv");
        timeCourseList.add("drnc");
        timeCourseList.add("drtm");
        timeCourseList.add("dsd#");
        timeCourseList.add("dsdm");
        timeCourseList.add("dsh#");
        timeCourseList.add("dshm");
        timeCourseList.add("dstm");
        timeCourseList.add("dwad");
        timeCourseList.add("dwag");
        timeCourseList.add("eaid");
        timeCourseList.add("efac");
        timeCourseList.add("efad");
        timeCourseList.add("egwa");
        timeCourseList.add("egws");
        timeCourseList.add("eoad");
        timeCourseList.add("epaa");
        timeCourseList.add("epad");
        timeCourseList.add("esaa");
        timeCourseList.add("esac");
        timeCourseList.add("esad");
        timeCourseList.add("etaa");
        timeCourseList.add("etac");
        timeCourseList.add("etad");
        timeCourseList.add("ewad");
        timeCourseList.add("ewsd");
        timeCourseList.add("falg");
        timeCourseList.add("fali");
        timeCourseList.add("faw");
        timeCourseList.add("fden");
        timeCourseList.add("fl3c");
        timeCourseList.add("fl3n");
        timeCourseList.add("fl4c");
        timeCourseList.add("fl4n");
        timeCourseList.add("flagw");
        timeCourseList.add("flbd");
        timeCourseList.add("fldd");
        timeCourseList.add("flni");
        timeCourseList.add("flph");
        timeCourseList.add("flti");
        timeCourseList.add("flur");
        timeCourseList.add("frod");
        timeCourseList.add("fuhy");
        timeCourseList.add("fwad");
        timeCourseList.add("g#ad");
        timeCourseList.add("g#pd");
        timeCourseList.add("g#sd");
        timeCourseList.add("gc%d");
        timeCourseList.add("gl%d");
        timeCourseList.add("gn%d");
        timeCourseList.add("gnad");
        timeCourseList.add("gp%d");
        timeCourseList.add("gpad");
        timeCourseList.add("grad");
        timeCourseList.add("gstd");
        timeCourseList.add("gwad");
        timeCourseList.add("gwgd");
        timeCourseList.add("h2oa");
        timeCourseList.add("hcrd");
        timeCourseList.add("hiad");
        timeCourseList.add("hind");
        timeCourseList.add("hipd");
        timeCourseList.add("hum1");
        timeCourseList.add("hum2");
        timeCourseList.add("hum3");
        timeCourseList.add("hum4");
        timeCourseList.add("hum5");
        timeCourseList.add("hwad");
        timeCourseList.add("hyad");
        timeCourseList.add("infd");
        timeCourseList.add("ir#c");
        timeCourseList.add("irrc");
        timeCourseList.add("irrd");
        timeCourseList.add("l#ir");
        timeCourseList.add("l#sd");
        timeCourseList.add("la%d");
        timeCourseList.add("lafd");
        timeCourseList.add("laid");
        timeCourseList.add("lald");
        timeCourseList.add("laln");
        timeCourseList.add("lapd");
        timeCourseList.add("lard");
        timeCourseList.add("lc0d");
        timeCourseList.add("lc1d");
        timeCourseList.add("lc2d");
        timeCourseList.add("lc3d");
        timeCourseList.add("lc4d");
        timeCourseList.add("lc5d");
        timeCourseList.add("lcnf");
        timeCourseList.add("lctd");
        timeCourseList.add("ldad");
        timeCourseList.add("li%d");
        timeCourseList.add("li%n");
        timeCourseList.add("liwad");
        timeCourseList.add("lmhn");
        timeCourseList.add("lmln");
        timeCourseList.add("ln%d");
        timeCourseList.add("ln0d");
        timeCourseList.add("ln1d");
        timeCourseList.add("ln2d");
        timeCourseList.add("ln3d");
        timeCourseList.add("ln4d");
        timeCourseList.add("ln5d");
        timeCourseList.add("lnad");
        timeCourseList.add("lntd");
        timeCourseList.add("lnum");
        timeCourseList.add("lp%d");
        timeCourseList.add("lp0d");
        timeCourseList.add("lp1d");
        timeCourseList.add("lp2d");
        timeCourseList.add("lp3d");
        timeCourseList.add("lp4d");
        timeCourseList.add("lp5d");
        timeCourseList.add("lpad");
        timeCourseList.add("lptd");
        timeCourseList.add("lrsd");
        timeCourseList.add("lwad");
        timeCourseList.add("lwpd");
        timeCourseList.add("mec0d");
        timeCourseList.add("mec1d");
        timeCourseList.add("mec2d");
        timeCourseList.add("mec3d");
        timeCourseList.add("mec4d");
        timeCourseList.add("mec5d");
        timeCourseList.add("mectd");
        timeCourseList.add("men0d");
        timeCourseList.add("men1d");
        timeCourseList.add("men2d");
        timeCourseList.add("men3d");
        timeCourseList.add("men4d");
        timeCourseList.add("men5d");
        timeCourseList.add("mentd");
        timeCourseList.add("mep0d");
        timeCourseList.add("mep1d");
        timeCourseList.add("mep2d");
        timeCourseList.add("mep3d");
        timeCourseList.add("mep4d");
        timeCourseList.add("mep5d");
        timeCourseList.add("meptd");
        timeCourseList.add("mrad");
        timeCourseList.add("n%hn");
        timeCourseList.add("n%ln");
        timeCourseList.add("napc");
        timeCourseList.add("ndnc");
        timeCourseList.add("nfgd");
        timeCourseList.add("nfpd");
        timeCourseList.add("nfsd");
        timeCourseList.add("nftid");
        timeCourseList.add("nfud");
        timeCourseList.add("nfxc");
        timeCourseList.add("nfxd");
        timeCourseList.add("nfxm");
        timeCourseList.add("nh10d");
        timeCourseList.add("nh1d");
        timeCourseList.add("nh2d");
        timeCourseList.add("nh3d");
        timeCourseList.add("nh4d");
        timeCourseList.add("nh5d");
        timeCourseList.add("nh6d");
        timeCourseList.add("nh7d");
        timeCourseList.add("nh8d");
        timeCourseList.add("nh9d");
        timeCourseList.add("nhtd");
        timeCourseList.add("nhu1");
        timeCourseList.add("nhu2");
        timeCourseList.add("nhu3");
        timeCourseList.add("nhu4");
        timeCourseList.add("nhu5");
        timeCourseList.add("niad");
        timeCourseList.add("nicm");
        timeCourseList.add("nimc");
        timeCourseList.add("nitc");
        timeCourseList.add("nitd");
        timeCourseList.add("nlcc");
        timeCourseList.add("nmnc");
        timeCourseList.add("no10d");
        timeCourseList.add("no1d");
        timeCourseList.add("no2d");
        timeCourseList.add("no3d");
        timeCourseList.add("no4d");
        timeCourseList.add("no5d");
        timeCourseList.add("no6d");
        timeCourseList.add("no7d");
        timeCourseList.add("no8d");
        timeCourseList.add("no9d");
        timeCourseList.add("noad");
        timeCourseList.add("nspav");
        timeCourseList.add("nstd");
        timeCourseList.add("ntop");
        timeCourseList.add("nuac");
        timeCourseList.add("nuad");
        timeCourseList.add("nupc");
        timeCourseList.add("nupr");
        timeCourseList.add("nwad");
        timeCourseList.add("o#ad");
//        timeCourseList.add("obs_trt_id");
        timeCourseList.add("omac");
        timeCourseList.add("owad");
        timeCourseList.add("owgd");
        timeCourseList.add("oxrn");
        timeCourseList.add("p#ad");
        timeCourseList.add("p#am");
        timeCourseList.add("pac1d");
        timeCourseList.add("pac2d");
        timeCourseList.add("pac3d");
        timeCourseList.add("pac4d");
        timeCourseList.add("pac5d");
        timeCourseList.add("pactd");
        timeCourseList.add("pari");
        timeCourseList.add("parue");
        timeCourseList.add("pavtd");
        timeCourseList.add("phad");
        timeCourseList.add("phan");
        timeCourseList.add("pi#m");
        timeCourseList.add("picm");
        timeCourseList.add("pimc");
        timeCourseList.add("pittd");
        timeCourseList.add("plb1d");
        timeCourseList.add("plb2d");
        timeCourseList.add("plb3d");
        timeCourseList.add("plb4d");
        timeCourseList.add("plb5d");
        timeCourseList.add("plbtd");
        timeCourseList.add("plcc");
        timeCourseList.add("plp%d");
        timeCourseList.add("plpad");
        timeCourseList.add("pm%m");
        timeCourseList.add("pmad");
        timeCourseList.add("pmnc");
        timeCourseList.add("prec");
        timeCourseList.add("pst1a");
        timeCourseList.add("pst1d");
        timeCourseList.add("pst2a");
        timeCourseList.add("pst2d");
        timeCourseList.add("pst3d");
        timeCourseList.add("pst4d");
        timeCourseList.add("pst5d");
        timeCourseList.add("psttd");
        timeCourseList.add("ptf");
        timeCourseList.add("pupc");
        timeCourseList.add("pupd");
        timeCourseList.add("pwad");
        timeCourseList.add("pwdd");
        timeCourseList.add("rcnf");
        timeCourseList.add("rdad");
        timeCourseList.add("rdpd");
        timeCourseList.add("resc");
        timeCourseList.add("resnc");
        timeCourseList.add("respc");
        timeCourseList.add("rgrd");
        timeCourseList.add("rl10d");
        timeCourseList.add("rl1d");
        timeCourseList.add("rl2d");
        timeCourseList.add("rl3d");
        timeCourseList.add("rl4d");
        timeCourseList.add("rl5d");
        timeCourseList.add("rl6d");
        timeCourseList.add("rl7d");
        timeCourseList.add("rl8d");
        timeCourseList.add("rl9d");
        timeCourseList.add("rlad");
        timeCourseList.add("rlwd");
        timeCourseList.add("rn%d");
        timeCourseList.add("rnad");
        timeCourseList.add("rnua");
        timeCourseList.add("rofc");
        timeCourseList.add("rofd");
        timeCourseList.add("rs%d");
        timeCourseList.add("rsad");
        timeCourseList.add("rsfp");
        timeCourseList.add("rsnad");
        timeCourseList.add("rspad");
        timeCourseList.add("rspd");
        timeCourseList.add("rsvn");
        timeCourseList.add("rswad");
        timeCourseList.add("rtmpd");
        timeCourseList.add("rtopd");
        timeCourseList.add("rtp%d");
        timeCourseList.add("rtpad");
        timeCourseList.add("rtwm");
        timeCourseList.add("rwad");
        timeCourseList.add("rwld");
        timeCourseList.add("s#ad");
        timeCourseList.add("s#pd");
        timeCourseList.add("s1c0d");
        timeCourseList.add("s1c1d");
        timeCourseList.add("s1c2d");
        timeCourseList.add("s1c3d");
        timeCourseList.add("s1c4d");
        timeCourseList.add("s1c5d");
        timeCourseList.add("s1ctd");
        timeCourseList.add("s1n0d");
        timeCourseList.add("s1n1d");
        timeCourseList.add("s1n2d");
        timeCourseList.add("s1n3d");
        timeCourseList.add("s1n4d");
        timeCourseList.add("s1n5d");
        timeCourseList.add("s1ntd");
        timeCourseList.add("s1p0d");
        timeCourseList.add("s1p1d");
        timeCourseList.add("s1p2d");
        timeCourseList.add("s1p3d");
        timeCourseList.add("s1p4d");
        timeCourseList.add("s1p5d");
        timeCourseList.add("s1ptd");
        timeCourseList.add("s2c1d");
        timeCourseList.add("s2c2d");
        timeCourseList.add("s2c3d");
        timeCourseList.add("s2c4d");
        timeCourseList.add("s2c5d");
        timeCourseList.add("s2ctd");
        timeCourseList.add("s2n1d");
        timeCourseList.add("s2n2d");
        timeCourseList.add("s2n3d");
        timeCourseList.add("s2n4d");
        timeCourseList.add("s2n5d");
        timeCourseList.add("s2ntd");
        timeCourseList.add("s3c1d");
        timeCourseList.add("s3c2d");
        timeCourseList.add("s3c3d");
        timeCourseList.add("s3c4d");
        timeCourseList.add("s3c5d");
        timeCourseList.add("s3ctd");
        timeCourseList.add("s3n1d");
        timeCourseList.add("s3n2d");
        timeCourseList.add("s3n3d");
        timeCourseList.add("s3n4d");
        timeCourseList.add("s3n5d");
        timeCourseList.add("s3ntd");
        timeCourseList.add("said");
        timeCourseList.add("sc1d");
        timeCourseList.add("sc2d");
        timeCourseList.add("sc3d");
        timeCourseList.add("sc4d");
        timeCourseList.add("sc5d");
        timeCourseList.add("scdd");
        timeCourseList.add("scnf");
        timeCourseList.add("sctd");
        timeCourseList.add("scwa");
        timeCourseList.add("sdad");
        timeCourseList.add("sdmpd");
        timeCourseList.add("sdn%d");
        timeCourseList.add("sdnad");
        timeCourseList.add("sdopd");
        timeCourseList.add("sdp%d");
        timeCourseList.add("sdpad");
        timeCourseList.add("sdwad");
        timeCourseList.add("sdwt");
        timeCourseList.add("sead");
        timeCourseList.add("sedm");
        timeCourseList.add("senc0");
        timeCourseList.add("sencs");
        timeCourseList.add("senl0");
        timeCourseList.add("senls");
        timeCourseList.add("senn0");
        timeCourseList.add("senns");
        timeCourseList.add("sennt");
        timeCourseList.add("senwt");
        timeCourseList.add("sgsb");
        timeCourseList.add("sh%d");
        timeCourseList.add("shad");
        timeCourseList.add("shmpd");
        timeCourseList.add("shnd");
        timeCourseList.add("shopd");
        timeCourseList.add("shp%d");
        timeCourseList.add("shpad");
        timeCourseList.add("shwad");
        timeCourseList.add("sl");
        timeCourseList.add("slad");
        timeCourseList.add("slhn");
        timeCourseList.add("slln");
        timeCourseList.add("slmpd");
        timeCourseList.add("slnd");
        timeCourseList.add("slopd");
        timeCourseList.add("slp%d");
        timeCourseList.add("slpad");
        timeCourseList.add("slpf");
        timeCourseList.add("sn%d");
        timeCourseList.add("sn1d");
        timeCourseList.add("sn2d");
        timeCourseList.add("sn3d");
        timeCourseList.add("sn4d");
        timeCourseList.add("sn5d");
        timeCourseList.add("snad");
        timeCourseList.add("sndd");
        timeCourseList.add("snn0c");
        timeCourseList.add("snn1c");
        timeCourseList.add("snp0c");
        timeCourseList.add("snp1c");
        timeCourseList.add("sntd");
        timeCourseList.add("snw0c");
        timeCourseList.add("snw1c");
        timeCourseList.add("socd");
        timeCourseList.add("sp#p");
        timeCourseList.add("sp%d");
        timeCourseList.add("spad");
        timeCourseList.add("spam");
        timeCourseList.add("ssad");
        timeCourseList.add("stc0d");
        timeCourseList.add("stc1d");
        timeCourseList.add("stc2d");
        timeCourseList.add("stc3d");
        timeCourseList.add("stc4d");
        timeCourseList.add("stc5d");
        timeCourseList.add("stctd");
        timeCourseList.add("stn0d");
        timeCourseList.add("stn1d");
        timeCourseList.add("stn2d");
        timeCourseList.add("stn3d");
        timeCourseList.add("stn4d");
        timeCourseList.add("stn5d");
        timeCourseList.add("stntd");
        timeCourseList.add("suad");
        timeCourseList.add("sugd");
        timeCourseList.add("suid");
        timeCourseList.add("sw10d");
        timeCourseList.add("sw1d");
        timeCourseList.add("sw2d");
        timeCourseList.add("sw3d");
        timeCourseList.add("sw4d");
        timeCourseList.add("sw5d");
        timeCourseList.add("sw6d");
        timeCourseList.add("sw7d");
        timeCourseList.add("sw8d");
        timeCourseList.add("sw9d");
        timeCourseList.add("swad");
        timeCourseList.add("swpd");
        timeCourseList.add("swxd");
        timeCourseList.add("t#ad");
        timeCourseList.add("t#pd");
        timeCourseList.add("t#sd");
        timeCourseList.add("taid");
        timeCourseList.add("tdrw");
        timeCourseList.add("tdwa");
        timeCourseList.add("tfgd");
        timeCourseList.add("tfon");
        timeCourseList.add("tfpd");
        timeCourseList.add("tgav");
        timeCourseList.add("tgnn");
        timeCourseList.add("tkill");
        timeCourseList.add("tnad");
        timeCourseList.add("tpad");
        timeCourseList.add("trad");
        timeCourseList.add("trrd");
        timeCourseList.add("tuna");
        timeCourseList.add("twad");
        timeCourseList.add("un%d");
        timeCourseList.add("unad");
        timeCourseList.add("up%d");
        timeCourseList.add("upad");
        timeCourseList.add("uwad");
        timeCourseList.add("uyad");
        timeCourseList.add("vbc5");
        timeCourseList.add("vbc6");
        timeCourseList.add("vn%d");
        timeCourseList.add("vnad");
        timeCourseList.add("vp%d");
        timeCourseList.add("vpad");
        timeCourseList.add("vrnf");
        timeCourseList.add("wavr");
        timeCourseList.add("wfgd");
        timeCourseList.add("wfpd");
        timeCourseList.add("wfsd");
        timeCourseList.add("wftd");
        timeCourseList.add("wftid");
        timeCourseList.add("wlvg");
        timeCourseList.add("wsgd");
        timeCourseList.add("wspav");
        timeCourseList.add("wspd");
        timeCourseList.add("wupr");
        timeCourseList.add("year");

        // Date type varibale list
        dateTypeList.add("pldae");
        dateTypeList.add("edate");
        dateTypeList.add("edat");
        dateTypeList.add("adat");
        dateTypeList.add("r1at");
        dateTypeList.add("r7at");
        dateTypeList.add("r2at");
        dateTypeList.add("r3at");
        dateTypeList.add("r5at");
        dateTypeList.add("pd1t");
        dateTypeList.add("r6at");
        dateTypeList.add("pdft");
        dateTypeList.add("r4at");
        dateTypeList.add("hdate");
        dateTypeList.add("hdat");
        dateTypeList.add("r8at");
        dateTypeList.add("llfd");
        dateTypeList.add("idat");
        dateTypeList.add("mdat");
        dateTypeList.add("r9at");
        dateTypeList.add("tspd");
        dateTypeList.add("tdat");
        dateTypeList.add("z21d");
        dateTypeList.add("z30d");
        dateTypeList.add("z31d");
        dateTypeList.add("z37d");
        dateTypeList.add("z39d");

        // Date type varibale with dap variable list
        dapDateTypeList.put("adat", "adap");
        dapDateTypeList.put("pd1t", "pd1p");
        dapDateTypeList.put("pdft", "pdfp");
        dapDateTypeList.put("r4at", "pdfp");
        dapDateTypeList.put("hdate", "hdap");
        dapDateTypeList.put("hdat", "hdap");
        dapDateTypeList.put("r8at", "r8ap");
        dapDateTypeList.put("idat", "idap");
        dapDateTypeList.put("mdat", "mdap");
        dapDateTypeList.put("tdat", "tdap");

    }

    /**
     * Get summary variable list
     *
     * @return summary variable list
     */
    public ArrayList getSummaryList() {
        return new ArrayList(summaryList);
    }

    /**
     * Get time-course variable list
     *
     * @return time-course variable list
     */
    public ArrayList getTimeCourseList() {
        return new ArrayList(timeCourseList);
    }

    /**
     * Get list of data type variable
     *
     * @return list of data type variable
     */
    public ArrayList getDateTypeList() {
        return new ArrayList(dateTypeList);
    }

    /**
     * Get the list of data type variables with dap variable
     *
     * @return the list of data type variables with dap variable
     */
    public HashMap getDapDateTypeList() {
        return dapDateTypeList;
    }

    /**
     * Check if the varibale is the summary variable
     *
     * @param variableName the code of variable, like "hwam"
     *
     * @return true for summary variable, false for not
     */
    public boolean isSummaryData(Object variableName) {
        return summaryList.contains(variableName);
    }

    /**
     * Check if the variable is the time-series variable
     *
     * @param variableName the code of variable, like "hwam"
     *
     * @return true for time-series variable, false for not
     */
    public boolean isTimeSeriesData(Object variableName) {
        return timeCourseList.contains(variableName);
    }

    /**
     * Check if the variable is the date type
     *
     * @param variableName the code of variable, like "adat"
     *
     * @return true for date type, false for not
     */
    public boolean isDateType(Object variableName) {
        return dateTypeList.contains(variableName);
    }

    /**
     * Check if the variable is DAP (Date After Planting) variable
     *
     * @param variableName the code of variable, like "adat"
     *
     * @return true for DAP variable, false for not
     */
    public boolean isDapDateType(Object variableName) {
        return dapDateTypeList.containsKey(variableName);
    }

    /**
     * Check if the variable is DAP (Date After Planting) variable
     *
     * @param variableName the code of variable, like "adat"
     * @param dapName the code of variable, like "adap"
     *
     * @return true for DAP variable, false for not
     */
    public boolean isDapDateType(Object variableName, Object dapName) {
        return (dapDateTypeList.containsKey(variableName) && dapDateTypeList.get(variableName).equals(dapName));
    }

    /**
     * Get the DAP variable name related to the input variable name
     *
     * @param variableName the code of variable, like "adat"
     *
     * @return the DAP variable name
     */
    public String getDapCode(Object variableName) {
        return (String) dapDateTypeList.get(variableName);
    }
}
