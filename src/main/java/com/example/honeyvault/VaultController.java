package com.example.honeyvault;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import cn.hutool.core.util.CharsetUtil;
import com.example.honeyvault.chinese.paper19.EncoderDecoderWithoutPIICN;
import com.example.honeyvault.chinese.paper23_list_version.EncoderDecoderListCN;
import com.example.honeyvault.chinese.paper23_markov_version.EncoderDecoderMarkovCN;
import com.example.honeyvault.english.paper19.EncoderDecoderWithoutPIIEng;
import com.example.honeyvault.english.paper23_list_version.EncoderDecoderListEng;
import com.example.honeyvault.english.paper23_markov_version.EncoderDecoderMarkovEng;
import com.xiaoleilu.hutool.util.RandomUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;


@RestController
public class VaultController {
    @Resource
    EncoderDecoderMarkovCN encoderDecoderMarkovCN;
    @Resource
    EncoderDecoderListCN encoderDecoderListCN;
    @Resource
    EncoderDecoderWithoutPIICN encoderDecoderWithoutPIICN;

    @Resource

    EncoderDecoderMarkovEng encoderDecoderMarkovEng;
    @Resource
    EncoderDecoderListEng encoderDecoderListEng;

    @Resource
    EncoderDecoderWithoutPIIEng encoderDecoderWithoutPIIEng;


    String mainPswd;

    private final int fixedLength = 74 * 128;

    @PostMapping("init")
    public String initMainPswd(@RequestParam String mainPswd) {
        this.mainPswd = mainPswd;
        return mainPswd;
    }

    @PostMapping("/encodeMarkov")
    public List<String> encodeMarkov(@RequestBody List<String> vault, @RequestParam int mkv,
                                     @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                     @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1
    ) {
        List<Pair<String, String>> encode = encoderDecoderMarkovCN.encode(vault, fixedLength, mkv, lambdaOp,
                lambdaTimes,
                lambdaMkv, lambdaMkv_1);
        List<String> encodedString = new ArrayList<>();
        encode.forEach(e -> encodedString.add(e.getValue()));
        return encoderDecoderMarkovCN.decode(encodedString, mkv);
    }


    @PostMapping("/encodeList")
    public List<String> encodeList(@RequestBody List<String> vault,
                                   @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                   @RequestParam double listLambda
    ) {
        encoderDecoderListCN.init(lambdaOp, lambdaTimes, listLambda);
        List<Pair<String, String>> encode = encoderDecoderListCN.encode(vault, fixedLength, listLambda);
        System.out.println(encode.size());
        List<String> encodedString = new ArrayList<>();
        encode.forEach(e -> encodedString.add(e.getValue()));
        System.out.println(encodedString.size());
        return encoderDecoderListCN.decode(encodedString, listLambda);
    }


//    @PostMapping("/encodeNoPII")
//    public List<String> encodeWithoutPII(@RequestBody List<String> vault, @RequestParam int mkv,
//                                         @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1,
//                                         @RequestParam double lambdaOp, @RequestParam double lambdaTimes) {
//        encoderDecoderWithoutPIICN.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);
//        List<Pair<String, String>> encode = encoderDecoderWithoutPIICN.encode(vault, fixedLength, mkv, lambdaMkv);
//        List<String> encodedString = new ArrayList<>();
//        System.out.println(encode.toString());
//        encode.forEach(e -> encodedString.add(e.getValue()));
//        return encoderDecoderWithoutPIICN.decode(encodedString, mkv, lambdaMkv);
//    }

    String genRandomStr() {
        StringBuilder filledString = new StringBuilder();
        while (filledString.length() < fixedLength) {
            filledString.append(RandomUtil.randomInt(0, 2));
        }
        return filledString.toString();
    }


    @GetMapping("genDV1")
    public void genDecoyVault19(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_1.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_3.csv", CharsetUtil.CHARSET_UTF_8);
//
        encoderDecoderWithoutPIICN.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);
        for (int i = 0; i < 150000; i++) {
            List<String> decode;
            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            decode = encoderDecoderWithoutPIICN.decode(decoyVault, mkv, lambdaMkv);
            writer1.writeLine(String.valueOf(decode));
        }
        writer1.close();
        System.out.println("19文件1成功");

        for (int i = 0; i < 150000; i++) {
            List<String> decode = null;
            int maxRetries = 5; // 设置最大重试次数
            int retries = 0;
            while (retries < maxRetries) {
                try {
                    decode = decode6(mkv, lambdaMkv);
                    writer3.writeLine(String.valueOf(decode));
                    break;
                } catch (Exception e) {
                    // 捕获异常后输出错误信息
                    e.printStackTrace();

                    // 增加重试次数
                    retries++;
                }
            }
        }
        writer3.close();
        System.out.println("19文件3成功");
    }


    @GetMapping("genDV2")
    public void genDecoyVault23Markov(@RequestParam int mkv, @RequestParam double lambdaOp,
                                      @RequestParam double lambdaTimes,
                                      @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault23MKV_1.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault23MKV_3.csv", CharsetUtil.CHARSET_UTF_8);


//        CsvWriter writer1 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_3.csv", CharsetUtil.CHARSET_UTF_8);


        encoderDecoderMarkovCN.init(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        for (int i = 0; i < 150000; i++) {
            List<String> decode;
            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            decode = encoderDecoderMarkovCN.decode(decoyVault, mkv);
            writer1.writeLine(String.valueOf(decode));
        }
        writer1.close();
        System.out.println("23M文件1成功");

        for (int i = 0; i < 150000; i++) {
            List<String> decode;
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                decoyVault.add(genRandomStr());
            }
            decode = encoderDecoderMarkovCN.decode(decoyVault, mkv);
            writer3.writeLine(String.valueOf(decode));
        }
        writer3.close();
        System.out.println("23M文件3成功");

    }

    @GetMapping("genDV3")
    public void genDecoyVault23List(@RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                    @RequestParam double listLambda) {
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault23List_1.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault23List_3.csv", CharsetUtil.CHARSET_UTF_8);

//        CsvWriter writer1 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_3.csv", CharsetUtil.CHARSET_UTF_8);


        encoderDecoderListCN.init(lambdaOp, lambdaTimes, listLambda);

        for (int i = 0; i < 150000; i++) {
            List<String> decode;
            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            decode = encoderDecoderListCN.decode(decoyVault, listLambda);
            writer1.writeLine(String.valueOf(decode));
        }
        writer1.close();
        System.out.println("23L文件1成功");


        for (int i = 0; i < 150000; i++) {
            List<String> decode;
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                decoyVault.add(genRandomStr());
            }
            decode = encoderDecoderListCN.decode(decoyVault, listLambda);

            writer3.writeLine(String.valueOf(decode));
        }
        writer3.close();
        System.out.println("23L文件3成功");

    }


//    ----------------------以下为English

    @PostMapping("/encodeMarkovEng")
    public List<String> encodeMarkovEng(@RequestBody List<String> vault, @RequestParam int mkv,
                                        @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                        @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1
    ) {
        List<Pair<String, String>> encode = encoderDecoderMarkovEng.encode(vault, fixedLength, mkv, lambdaOp,
                lambdaTimes,
                lambdaMkv, lambdaMkv_1);
        List<String> encodedString = new ArrayList<>();
        encode.forEach(e -> encodedString.add(e.getValue()));
        return encoderDecoderMarkovEng.decode(encodedString, mkv);
    }


    @PostMapping("/encodeListEng")
    public List<String> encodeListEng(@RequestBody List<String> vault,
                                      @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                      @RequestParam double listLambda
    ) {
        encoderDecoderListEng.init(lambdaOp, lambdaTimes, listLambda);
        List<Pair<String, String>> encode = encoderDecoderListEng.encode(vault, fixedLength, listLambda);
        System.out.println(encode.size());
        List<String> encodedString = new ArrayList<>();
        encode.forEach(e -> encodedString.add(e.getValue()));
        System.out.println(encodedString.size());
        return encoderDecoderListEng.decode(encodedString, listLambda);
    }

    @GetMapping("genDV1Eng")
    public void genDecoyVault19Eng(@RequestParam int mkv, @RequestParam double lambdaOp,
                                   @RequestParam double lambdaTimes,
                                   @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_1Eng.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_3Eng.csv", CharsetUtil.CHARSET_UTF_8);
//
//        CsvWriter writer1 = CsvUtil.getWriter
//        ("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv/decoyVault19_1.csv", CharsetUtil
//        .CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault19_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault19_3_1.csv", CharsetUtil.CHARSET_UTF_8);
        encoderDecoderWithoutPIIEng.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);

        for (int i = 0; i < 150000; i++) {
            List<String> decode;
            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            decode = encoderDecoderWithoutPIIEng.decode(decoyVault, mkv, lambdaMkv);
            writer1.writeLine(String.valueOf(decode));
        }
        writer1.close();
        System.out.println("英文19文件1成功");


        for (int i = 0; i < 150000; i++) {
            List<String> decode = null;
            int maxRetries = 5; // 设置最大重试次数
            int retries = 0;
            while (retries < maxRetries) {
                try {
                    decode = decode6Eng(mkv, lambdaMkv);
                    writer3.writeLine(String.valueOf(decode));
                    break;
                } catch (Exception e) {
                    // 捕获异常后输出错误信息
                    e.printStackTrace();

                    // 增加重试次数
                    retries++;
                }
            }
        }
        writer3.close();
        System.out.println("英文19文件3成功");
    }


    @GetMapping("genDV2Eng")
    public void genDecoyVault23MarkovEng(@RequestParam int mkv, @RequestParam double lambdaOp,
                                         @RequestParam double lambdaTimes,
                                         @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault23MKV_1Eng.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault23MKV_3Eng.csv", CharsetUtil.CHARSET_UTF_8);


//        CsvWriter writer1 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_3.csv", CharsetUtil.CHARSET_UTF_8);


        encoderDecoderMarkovEng.init(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        for (int i = 0; i < 150000; i++) {
            List<String> decode;
            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            decode = encoderDecoderMarkovEng.decode(decoyVault, mkv);
            writer1.writeLine(String.valueOf(decode));
        }
        writer1.close();
        System.out.println("英文23M文件1成功");

        for (int i = 0; i < 150000; i++) {
            List<String> decode;
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                decoyVault.add(genRandomStr());
            }
            decode = encoderDecoderMarkovEng.decode(decoyVault, mkv);
            writer3.writeLine(String.valueOf(decode));
        }
        writer3.close();
        System.out.println("英文23M文件3成功");

    }

    @GetMapping("genDV3Eng")
    public void genDecoyVault23ListEng(@RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                       @RequestParam double listLambda) {
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault23List_1Eng.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault23List_3Eng.csv", CharsetUtil.CHARSET_UTF_8);

//        CsvWriter writer1 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_3.csv", CharsetUtil.CHARSET_UTF_8);


        encoderDecoderListEng.init(lambdaOp, lambdaTimes, listLambda);

        for (int i = 0; i < 150000; i++) {
            List<String> decode;

            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            decode = encoderDecoderListEng.decode(decoyVault, listLambda);
            writer1.writeLine(String.valueOf(decode));
        }
        writer1.close();
        System.out.println("英文23L文件1成功");


        for (int i = 0; i < 150000; i++) {
            List<String> decode;

            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                decoyVault.add(genRandomStr());
            }
            decode = encoderDecoderListEng.decode(decoyVault, listLambda);
            writer3.writeLine(String.valueOf(decode));
        }
        writer3.close();
        System.out.println("英文23L文件3成功");

    }


    private List<String> decode6(int mkv, double lambdaMkv) {
        List<String> decode = null;


        List<String> decoyVault = new ArrayList<>();
        for (int j = 0; j < 6; j++) {
            decoyVault.add(genRandomStr());
        }
        decode = encoderDecoderWithoutPIICN.decode(decoyVault, mkv, lambdaMkv);
        return decode;
    }

    private List<String> decode6Eng(int mkv, double lambdaMkv) {
        List<String> decode = null;

        List<String> decoyVault = new ArrayList<>();
        for (int j = 0; j < 6; j++) {
            decoyVault.add(genRandomStr());
        }
        decode = encoderDecoderWithoutPIIEng.decode(decoyVault, mkv, lambdaMkv);
        return decode;
    }

    @GetMapping("checkTable19")
    public void checkTable19(@RequestParam int mkv, @RequestParam double lambdaOp,
                             @RequestParam double lambdaTimes,
                             @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        encoderDecoderWithoutPIICN.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);
        System.out.println(1);
    }

}

