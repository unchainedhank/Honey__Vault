package com.example.honeyvault;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import com.example.honeyvault.data_access.path.PathStatistic;
import com.example.honeyvault.chinese.paper23_list_version.EncoderDecoderListCN;
import com.example.honeyvault.chinese.paper23_markov_version.EncoderDecoderMarkovCN;
import com.example.honeyvault.chinese.paper19.EncoderDecoderWithoutPIICN;
import com.example.honeyvault.english.paper23_list_version.EncoderDecoderListEngl;
import com.example.honeyvault.english.paper23_markov_version.EncoderDecoderMarkovEngl;
import com.example.honeyvault.english.paper19.EncoderDecoderWithoutPIIEngl;
import com.xiaoleilu.hutool.util.CharsetUtil;
import com.xiaoleilu.hutool.util.RandomUtil;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.web.bind.annotation.*;


import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


@RestController
public class VaultController {
    @Resource
    EncoderDecoderMarkovCN encoderDecoderMarkovCN;
    @Resource
    EncoderDecoderListCN encoderDecoderListCN;
    @Resource
    EncoderDecoderWithoutPIICN encoderDecoderWithoutPIICN;

    @Resource
    EncoderDecoderMarkovEngl encoderDecoderMarkovEngl;
    @Resource
    EncoderDecoderListEngl encoderDecoderListEngl;
    @Resource
    EncoderDecoderWithoutPIIEngl encoderDecoderWithoutPIIEngl;

    String mainPswd;
    @Resource
    PathStatistic pathStatistic;


    private final int fixedLength = 74 * 128;

    Set<String> candidateSetCN = new HashSet<>(Arrays.asList("Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η", "ρ",
            "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
            "Φ", "Χ", "Ψ",
            "Ω", "ω", "ψ",
            "χ", "φ", "υ"));
    Set<String> candidateSetEngl = new HashSet<>(Arrays.asList("Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η", "ρ",
            "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
            "Φ", "Χ", "Ψ",
            "Ω", "ω", "ψ"));

    @PostMapping("init")
    public String initMainPswd(@RequestParam String mainPswd) {
        this.mainPswd = mainPswd;
        return mainPswd;
    }

    String genRandomStr() {
        StringBuilder filledString = new StringBuilder();
        while (filledString.length() < fixedLength) {
            filledString.append(RandomUtil.randomInt(0, 2));
        }
        return filledString.toString();
    }

    @PostMapping("/encodeMarkovCN")
    public List<String> encodeMarkovCN(@RequestBody List<String> vault, @RequestParam int mkv,
                                       @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                       @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1
    ) {
        List<Pair<String, String>> encode = encoderDecoderMarkovCN.encode(vault, fixedLength, mkv, lambdaOp,
                lambdaTimes,
                lambdaMkv, lambdaMkv_1);
        List<String> encodedString = new ArrayList<>();
        encode.forEach(e -> encodedString.add(e.getValue()));
        System.out.println(encodedString);
        return encoderDecoderMarkovCN.decode(encodedString, mkv);
    }

    @PostMapping("/encodeMarkovEngl")
    public List<String> encodeMarkovEngl(@RequestBody List<String> vault, @RequestParam int mkv,
                                         @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                         @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1
    ) {
        List<Pair<String, String>> encode = encoderDecoderMarkovEngl.encode(vault, fixedLength, mkv, lambdaOp,
                lambdaTimes,
                lambdaMkv, lambdaMkv_1);
        List<String> encodedString = new ArrayList<>();
        encode.forEach(e -> encodedString.add(e.getValue()));
        return encoderDecoderMarkovEngl.decode(encodedString, mkv);
    }
    @PostMapping("/decodeMarkovCN")
    public List<String> decodeMarkovCN(@RequestBody List<String> encodedStrings, @RequestParam String mainPswd, int mkv) {
        return encoderDecoderMarkovCN.decode(encodedStrings, mkv);
    }


    @PostMapping("/decodeMarkovEngl")
    public List<String> decodeMarkovEngl(@RequestBody List<String> encodedStrings, @RequestParam String mainPswd, int mkv) {
        return encoderDecoderMarkovEngl.decode(encodedStrings, mkv);
    }

    @PostMapping("/encodeListCN")
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

    @PostMapping("/encodeListEngl")
    public List<String> encodeListEngl(@RequestBody List<String> vault,
                                       @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                       @RequestParam double listLambda
    ) {
        encoderDecoderListEngl.init(lambdaOp, lambdaTimes, listLambda);
        List<Pair<String, String>> encode = encoderDecoderListEngl.encode(vault, fixedLength, listLambda);
        System.out.println(encode.size());
        List<String> encodedString = new ArrayList<>();
        encode.forEach(e -> encodedString.add(e.getValue()));
        System.out.println(encodedString.size());
        return encoderDecoderListEngl.decode(encodedString, listLambda);
    }

    @PostMapping("/decodeListCN")
    public List<String> decodeListCN(@RequestBody List<String> encodedStrings, double lambdaOp, double lambdaTimes,
                                     double listLambda) {
        encoderDecoderListCN.init(lambdaOp, lambdaTimes, listLambda);
        return encoderDecoderListCN.decode(encodedStrings, listLambda);
    }


    @PostMapping("/decodeListEngl")
    public List<String> decodeListEngl(@RequestBody List<String> encodedStrings, double lambdaOp, double lambdaTimes,
                                       double listLambda) {
        encoderDecoderListEngl.init(lambdaOp, lambdaTimes, listLambda);
        return encoderDecoderListEngl.decode(encodedStrings, listLambda);
    }


    @GetMapping("/encodeNoPIICN")
    public void encodeWithoutPIICN(@RequestBody List<String> vault, @RequestParam int mkv,
                                           @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1,
                                           @RequestParam double lambdaOp, @RequestParam double lambdaTimes) {

        Thread myThread1 = new Thread(new Runnable() {
            public void run() {
                encoderDecoderWithoutPIICN.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);
                System.out.println("jinru thread encodeNoPIICN");
                String path = "/writeData/encodeNoPIICN.csv";
                CsvWriter writer1 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
                List<Pair<String, String>> encode = encoderDecoderWithoutPIICN.encode(vault, fixedLength, mkv, lambdaOp,
                        lambdaTimes,
                        lambdaMkv, lambdaMkv_1);
                List<String> encodedString = new ArrayList<>();
                System.out.println(encode.toString());
                encode.forEach(e -> encodedString.add(e.getValue()));
                List<String> decode = null;
                decode = encoderDecoderWithoutPIICN.decode(encodedString, mkv, lambdaMkv);
                writer1.writeLine(String.valueOf(decode));
                writer1.close();
                System.out.println("encodeNoPIICN成功");
            }
        });
        myThread1.start();
        System.out.println("结束 encodeNoPIICN");
    }


    @PostMapping("/encodeNoPIIEngl")
    public List<String> encodeWithoutPIIEngl(@RequestBody List<String> vault, @RequestParam int mkv,
                                             @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1,
                                             @RequestParam double lambdaOp, @RequestParam double lambdaTimes) {
        encoderDecoderWithoutPIIEngl.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);
        List<Pair<String, String>> encode = encoderDecoderWithoutPIIEngl.encode(vault, fixedLength, mkv, lambdaMkv);
        List<String> encodedString = new ArrayList<>();
        System.out.println(encode.toString());
        encode.forEach(e -> encodedString.add(e.getValue()));
        return encoderDecoderWithoutPIIEngl.decode(encodedString, mkv, lambdaMkv);
    }


//    @GetMapping("decoy")
//    public void genDecoyVault(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
//                              @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
//        encoderDecoderMarkovCN.init(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
//        List<Integer> decoyVaultData = pathStatistic.getDecoyVaultData();
//        CsvWriter writer = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/decoyVault.csv",
//                CharsetUtil.CHARSET_UTF_8);
//        AtomicInteger count = new AtomicInteger();
//        System.out.println("开始");
//        decoyVaultData.forEach(vaultLength -> {
//            List<String> decoyVault = new ArrayList<>();
//            for (int i = 0; i < vaultLength; i++) {
//                decoyVault.add(genRandomStr());
//            }
//            List<String> decodeDecoyVault = encoderDecoderMarkovCN.decode(decoyVault, mkv);
//            writer.writeLine(String.valueOf(decodeDecoyVault));
//            System.out.println(count.incrementAndGet());
//        });
//
//    }

    @GetMapping("genDV1CN")
    public void genDecoyVault19CN(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                  @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
//        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_3.csv", CharsetUtil.CHARSET_UTF_8);

        //CsvWriter writer1 = CsvUtil.getWriter("/writeData/decoyVaultCN19_1.csv", CharsetUtil.CHARSET_UTF_8);
        //CsvWriter writer2 = CsvUtil.getWriter("/writeData" +"/decoyVault19_2.csv", CharsetUtil.CHARSET_UTF_8);
        //CsvWriter writer3 = CsvUtil.getWriter("/writeData" +"/decoyVaultCN19_3.csv", CharsetUtil.CHARSET_UTF_8);

        encoderDecoderWithoutPIICN.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);
        Thread myThread1 = new Thread(new Runnable() {
            public void run() {
                System.out.println("jinru thread 19CN1");
                String path = "/writeData/decoyVaultCN19_1.csv";
                CsvWriter writer1 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
//        for (int i = 0; i < 1017; i++) {
                for (int i = 0; i < 150000; i++) {
                    List<String> decode = null;
                    boolean foundValid = true;
                    while (foundValid) {
                        String ranStr = genRandomStr();
                        List<String> decoyVault = new ArrayList<>();
                        decoyVault.add(ranStr);
                        decode = encoderDecoderWithoutPIICN.decode(decoyVault, mkv, lambdaMkv);
                        foundValid = isFoundInvalid(decode);
                    }
                    System.out.println(i + "CN19_1");
                    writer1.writeLine(String.valueOf(decode));
                }
                writer1.close();
                System.out.println("19文件CN1成功");
            }
        });
        myThread1.start();
        Thread myThread2 = new Thread(new Runnable() {
            public void run() {
                System.out.println("jinru thread 19CN3");
                String path = "/writeData/decoyVaultCN19_3.csv";
                CsvWriter writer3 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
                for (int i = 0; i < 150000; i++) {
                    List<String> decode = null;
                    int maxRetries = 5; // 设置最大重试次数
                    int retries = 0;
                    while (retries < maxRetries) {
                        try {
                            decode = decode6(mkv, lambdaMkv);
                            break;
                        } catch (Exception e) {
                            // 捕获异常后输出错误信息
                            e.printStackTrace();

                            // 增加重试次数
                            retries++;
                        }
                    }
                    if (decode != null) {
                        writer3.writeLine(String.valueOf(decode));
                    }
                    System.out.println(i + "CN19_3");
                }
                writer3.close();
                System.out.println("19文件CN3成功");
            }
        });
        myThread2.start();

        System.out.println("结束19CN");
    }


    private List<String> decode6(int mkv, double lambdaMkv) {
        List<String> decode = null;
        boolean foundInvalid = true;
        while (foundInvalid) {
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                decoyVault.add(genRandomStr());
            }
            decode = encoderDecoderWithoutPIICN.decode(decoyVault, mkv, lambdaMkv);
            foundInvalid = isFoundInvalid(decode);
        }
        return decode;
    }

    private boolean isFoundInvalid(List<String> decode) {
        boolean foundInvalid = false;
        for (String s : decode) {
            if (!isValid(s)) {
                foundInvalid = true;
                break;
            }
        }
        return foundInvalid;
    }

    private boolean isValid(String decodeString) {
        int finalLength = 0;
        for (char c : decodeString.toCharArray()) {
            String s = String.valueOf(c);
            if (candidateSetCN.contains(s)) {
                finalLength += 5;
            } else finalLength++;
            if (finalLength > 16) return false;
        }
        return finalLength >= 5;
    }


    @GetMapping("genDV1Engl")
    public void genDecoyVault19Engl(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                    @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
//        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_3.csv", CharsetUtil.CHARSET_UTF_8);
        System.out.println("进入genDV1Engl");
        // CsvWriter writer1 = CsvUtil.getWriter("/writeData/decoyVaultEngl19_1.csv", CharsetUtil.CHARSET_UTF_8);
        //CsvWriter writer2 = CsvUtil.getWriter("/writeData" +"/decoyVault19_2.csv", CharsetUtil.CHARSET_UTF_8);
        //CsvWriter writer3 = CsvUtil.getWriter("/writeData" +"/decoyVaultEngl19_3.csv", CharsetUtil.CHARSET_UTF_8);

        encoderDecoderWithoutPIIEngl.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);
        Thread myThread1 = new Thread(new Runnable() {
            public void run() {
                System.out.println("jinru thread 19EN1");
                String path = "/writeData/decoyVaultEngl19_1.csv";
                CsvWriter writer1 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
//        for (int i = 0; i < 1017; i++) {
                for (int i = 0; i < 300000; i++) {
                    List<String> decode = null;
                    boolean foundValid = true;
                    while (foundValid) {
                        String ranStr = genRandomStr();
                        List<String> decoyVault = new ArrayList<>();
                        decoyVault.add(ranStr);
                        decode = encoderDecoderWithoutPIIEngl.decode(decoyVault, mkv, lambdaMkv);
                        foundValid = isFoundInvalidEngl(decode);
                    }
                    System.out.println(i + "19EN1");
                    writer1.writeLine(String.valueOf(decode));
                }
                writer1.close();
                System.out.println("19文件EN1成功");
            }
        });
        myThread1.start();
        Thread myThread2 = new Thread(new Runnable() {
            public void run() {
                System.out.println("jinru thread 19EN3");
                String path = "/writeData/decoyVaultEngl19_3.csv";
                CsvWriter writer3 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
                for (int i = 0; i < 300000; i++) {
                    List<String> decode = null;
                    int maxRetries = 5; // 设置最大重试次数
                    int retries = 0;
                    while (retries < maxRetries) {
                        try {
                            decode = decode6Engl(mkv, lambdaMkv);
                            break;
                        } catch (Exception e) {
                            // 捕获异常后输出错误信息
                            e.printStackTrace();

                            // 增加重试次数
                            retries++;
                        }
                    }
                    if (decode != null) {
                        writer3.writeLine(String.valueOf(decode));
                    }
                    System.out.println(i + "EN19_3");
                }
                writer3.close();
                System.out.println("19文件EN3成功");
            }
        });
        myThread2.start();

        System.out.println("结束genDV1Engl");
    }



    private List<String> decode6Engl(int mkv, double lambdaMkv) {
        List<String> decode = null;
        boolean foundInvalid = true;
        while (foundInvalid) {
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                decoyVault.add(genRandomStr());
            }
            decode = encoderDecoderWithoutPIIEngl.decode(decoyVault, mkv, lambdaMkv);
            foundInvalid = isFoundInvalidEngl(decode);
        }
        return decode;
    }

    private boolean isFoundInvalidEngl(List<String> decode) {
        boolean foundInvalid = false;
        for (String s : decode) {
            if (!isValidEngl(s)) {
                foundInvalid = true;
                break;
            }
        }
        return foundInvalid;
    }

    private boolean isValidEngl(String decodeString) {
        int finalLength = 0;
        for (char c : decodeString.toCharArray()) {
            String s = String.valueOf(c);
            if (candidateSetEngl.contains(s)) {
                finalLength += 5;
            } else finalLength++;
            if (finalLength > 16) return false;
        }
        return finalLength >= 5;
    }

    @GetMapping("genDV2CN")
    public void genDecoyVault23MarkovCN(@RequestParam int mkv, @RequestParam double lambdaOp,
                                        @RequestParam double lambdaTimes,
                                        @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        System.out.println("进入genDV2CN");
//        CsvWriter writer1 = CsvUtil.getWriter("/writeData/decoyVaultCN23MKV_1.csv", CharsetUtil.CHARSET_UTF_8);
        //CsvWriter writer3 = CsvUtil.getWriter("/writeData/decoyVaultCN23MKV_3.csv", CharsetUtil.CHARSET_UTF_8);


//        CsvWriter writer1 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_3.csv", CharsetUtil.CHARSET_UTF_8);


        encoderDecoderMarkovCN.init(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        for (int i = 0; i < 15; i++) {
            int loop = i;
            Thread myThread = new Thread(new Runnable() {
                private int count = loop;
                public void run() {
                    System.out.println("jinru thread 23CNMarkov3");
                    String path = "/writeData/decoyVaultCN23MKV_3_" + count + ".csv";
                    CsvWriter writer3 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);

                    for (int i = 0; i < 1000; i++) {
                        List<String> decode = null;
                        boolean foundInvalid = true;
                        while (foundInvalid) {
                            List<String> decoyVault = new ArrayList<>();
                            for (int j = 0; j < 6; j++) {
                                decoyVault.add(genRandomStr());
                            }
                            decode = encoderDecoderMarkovCN.decode(decoyVault, mkv);
                            foundInvalid = isFoundInvalid(decode);
                        }
                        System.out.println(count + "    " + i);
//            System.out.println(decode);
                        writer3.writeLine(String.valueOf(decode));
                    }
                    writer3.close();
                    System.out.println("decoyVaultCN23MKV_3成功");
                }
            });
            myThread.start();
        }

        Thread myThread1 = new Thread(new Runnable() {
            public void run() {
                System.out.println("jinru thread Engl23MKV_3");
                String path = "/writeData/decoyVaultCN23MKV_1.csv";
                CsvWriter writer1 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
                System.out.println("over, fanhui 23CNMarkov");
                for (int i = 0; i < 150000; i++) {
                    List<String> decode = null;
                    boolean foundInvalid = true;
                    while (foundInvalid) {
                        String ranStr = genRandomStr();
                        List<String> decoyVault = new ArrayList<>();
                        decoyVault.add(ranStr);
                        decode = encoderDecoderMarkovCN.decode(decoyVault, mkv);
                        foundInvalid = isFoundInvalid(decode);
                    }
                    System.out.println(i + "Engl23MKV_3");
                    writer1.writeLine(String.valueOf(decode));
                }
                writer1.close();
                System.out.println("decoyVaultCN23MKV_1成功");
            }
        });
        myThread1.start();

        System.out.println("over, fanhui 23CNMarkov");
//        for (int i = 0; i < 150000; i++) {
//            List<String> decode = null;
//            boolean foundInvalid = true;
//            while (foundInvalid) {
//                String ranStr = genRandomStr();
//                List<String> decoyVault = new ArrayList<>();
//                decoyVault.add(ranStr);
//                decode = encoderDecoderMarkovCN.decode(decoyVault, mkv);
//                foundInvalid = isFoundInvalid(decode);
//            }
//            writer1.writeLine(String.valueOf(decode));
//        }
//        writer1.close();
//        System.out.println("23M文件1成功");

//        for (int i = 0; i < 150000; i++) {
//            List<String> decode = null;
//            boolean foundInvalid = true;
//            System.out.println("0 " + i);
//            while (foundInvalid) {
//                List<String> decoyVault = new ArrayList<>();
//                for (int j = 0; j < 6; j++) {
//                    decoyVault.add(genRandomStr());
//                }
//                decode = encoderDecoderMarkovCN.decode(decoyVault, mkv);
//                foundInvalid = isFoundInvalid(decode);
//            }
//            System.out.println("1 " + i);
////            System.out.println(decode);
//            writer3.writeLine(String.valueOf(decode));
//        }
//        writer3.close();
//        System.out.println("23M成功");

    }


    @GetMapping("genDV2Engl")
    public void genDecoyVault23MarkovEngl(@RequestParam int mkv, @RequestParam double lambdaOp,
                                          @RequestParam double lambdaTimes,
                                          @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
//        CsvWriter writer1 = CsvUtil.getWriter("/writeData/decoyVaultEngl23MKV_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/writeData/decoyVaultEngl23MKV_3.csv", CharsetUtil.CHARSET_UTF_8);
//

//        CsvWriter writer1 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_3.csv", CharsetUtil.CHARSET_UTF_8);

        System.out.println("进入genDV2Engl3");


        encoderDecoderMarkovEngl.init(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        for (int i = 0; i < 15; i++) {
            int loop = i;
            Thread myThread = new Thread(new Runnable() {
                private int count = loop;
                public void run() {
                    System.out.println("jinru threadEngl23MKV_3");
                    String path = "/writeData/decoyVaultEngl23MKV_3" + count + ".csv";
                    CsvWriter writer3 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
                    for (int i = 0; i < 2000; i++) {
                        List<String> decode = null;
                        boolean foundInvalid = true;
                        while (foundInvalid) {
                            List<String> decoyVault = new ArrayList<>();
                            for (int j = 0; j < 6; j++) {
                                decoyVault.add(genRandomStr());
                            }
                            decode = encoderDecoderMarkovEngl.decode(decoyVault, mkv);
                            foundInvalid = isFoundInvalidEngl(decode);
                        }
                        System.out.println(count + "    " + i);
                        writer3.writeLine(String.valueOf(decode));
                    }
                    writer3.close();
                    System.out.println("decoyVaultEngl23MKV_3成功");
                }
            });
            myThread.start();
        }


        Thread myThread1 = new Thread(new Runnable() {
            public void run() {
                System.out.println("jinru thread Engl23MKV_3");
                String path = "/writeData/decoyVaultEngl23MKV_1.csv";
                CsvWriter writer1 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
                for (int i = 0; i < 300000; i++) {
                    List<String> decode = null;
                    boolean foundInvalid = true;
                    while (foundInvalid) {
                        String ranStr = genRandomStr();
                        List<String> decoyVault = new ArrayList<>();
                        decoyVault.add(ranStr);
                        decode = encoderDecoderMarkovEngl.decode(decoyVault, mkv);
                        foundInvalid = isFoundInvalidEngl(decode);
                    }
                    System.out.println(i + "Engl23MKV_3");
                    writer1.writeLine(String.valueOf(decode));
                }
                writer1.close();
                System.out.println("decoyVaultEngl23MKV_1成功");
            }
        });
        myThread1.start();

        System.out.println("over, 23EnglMarkov");


//        for (int i = 0; i < 300000; i++) {
//            List<String> decode = null;
//            boolean foundInvalid = true;
//            while (foundInvalid) {
//                String ranStr = genRandomStr();
//                List<String> decoyVault = new ArrayList<>();
//                decoyVault.add(ranStr);
//                decode = encoderDecoderMarkovEngl.decode(decoyVault, mkv);
//                foundInvalid = isFoundInvalidEngl(decode);
//            }
//            writer1.writeLine(String.valueOf(decode));
//        }
//        writer1.close();
//        System.out.println("23M文件1成功");
//
//        for (int i = 0; i < 300000; i++) {
//            List<String> decode = null;
//            boolean foundInvalid = true;
//            while (foundInvalid) {
//                List<String> decoyVault = new ArrayList<>();
//                for (int j = 0; j < 6; j++) {
//                    decoyVault.add(genRandomStr());
//                }
//                decode = encoderDecoderMarkovEngl.decode(decoyVault, mkv);
//                foundInvalid = isFoundInvalidEngl(decode);
//            }
//
//            writer3.writeLine(String.valueOf(decode));
//        }
//        writer3.close();
//        System.out.println("23M文件3成功");

    }

    @GetMapping("genDV3CN")
    public void genDecoyVault23ListCN(@RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                      @RequestParam double listLambda) {
//        CsvWriter writer1 = CsvUtil.getWriter("/writeData/decoyVaultCN23List_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/writeData/decoyVaultCN23List_3.csv", CharsetUtil.CHARSET_UTF_8);

//        CsvWriter writer1 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_3.csv", CharsetUtil.CHARSET_UTF_8);


        encoderDecoderListCN.init(lambdaOp, lambdaTimes, listLambda);


        Thread myThread1 = new Thread(new Runnable() {
            public void run() {
                System.out.println("jinru thread CN23list_1");
                String path = "/writeData/decoyVaultCN23List_1.csv";
                CsvWriter writer1 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
                for (int i = 0; i < 150000; i++) {
                    List<String> decode = null;
                    boolean foundInvalid = true;
                    while (foundInvalid) {
                        String ranStr = genRandomStr();
                        List<String> decoyVault = new ArrayList<>();
                        decoyVault.add(ranStr);
                        decode = encoderDecoderListCN.decode(decoyVault, listLambda);
                        foundInvalid = isFoundInvalid(decode);
                    }
                    System.out.println(i + "CN23list_1");
                    writer1.writeLine(String.valueOf(decode));
                }
                writer1.close();
                System.out.println("decoyVaultCN23List_1 成功");
            }
        });
        myThread1.start();

        Thread myThread2 = new Thread(new Runnable() {
            public void run() {
                System.out.println("jinru thread CN23list_3");
                String path = "/writeData/decoyVaultCN23List_3.csv";
                CsvWriter writer3 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
                for (int i = 0; i < 150000; i++) {
                    List<String> decode = null;
                    boolean foundInvalid = true;
                    while (foundInvalid) {
                        List<String> decoyVault = new ArrayList<>();
                        for (int j = 0; j < 6; j++) {
                            decoyVault.add(genRandomStr());
                        }
                        decode = encoderDecoderListCN.decode(decoyVault, listLambda);
                        foundInvalid = isFoundInvalid(decode);
                    }
                    System.out.println(1 + "CN23list_3");
                    writer3.writeLine(String.valueOf(decode));
                }
                writer3.close();
                System.out.println("decoyVaultCN23List_3 成功");
            }
        });
        myThread2.start();


        System.out.println("over, CN23list");

    }


    @GetMapping("genDV3Engl")
    public void genDecoyVault23ListEngl(@RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                        @RequestParam double listLambda) {
        CsvWriter writer1 = CsvUtil.getWriter("/writeData/decoyVaultEngl23List_1.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/writeData/decoyVaultEngl23List_3.csv", CharsetUtil.CHARSET_UTF_8);

//        CsvWriter writer1 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_3.csv", CharsetUtil.CHARSET_UTF_8);


        encoderDecoderListEngl.init(lambdaOp, lambdaTimes, listLambda);



        Thread myThread1 = new Thread(new Runnable() {
            public void run() {
                System.out.println("jinru thread EN23list_1");
                String path = "/writeData/decoyVaultEngl23List_1.csv";
                CsvWriter writer1 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
                for (int i = 0; i < 300000; i++) {
                    List<String> decode = null;
                    boolean foundInvalid = true;
                    while (foundInvalid) {
                        String ranStr = genRandomStr();
                        List<String> decoyVault = new ArrayList<>();
                        decoyVault.add(ranStr);
                        decode = encoderDecoderListEngl.decode(decoyVault, listLambda);
                        foundInvalid = isFoundInvalidEngl(decode);
                    }
                    System.out.println(1 + "EN23list_1" );
                    writer1.writeLine(String.valueOf(decode));
                }
                writer1.close();
                System.out.println("decoyVaultEngl23List_1 成功");
            }
        });
        myThread1.start();

        Thread myThread2 = new Thread(new Runnable() {
            public void run() {
                System.out.println("jinru thread EN23list_3");
                String path = "/writeData/decoyVaultEngl23List_3.csv";
                CsvWriter writer3 = CsvUtil.getWriter(path, CharsetUtil.CHARSET_UTF_8);
                for (int i = 0; i < 300000; i++) {
                    List<String> decode = null;
                    boolean foundInvalid = true;
                    while (foundInvalid) {
                        List<String> decoyVault = new ArrayList<>();
                        for (int j = 0; j < 6; j++) {
                            decoyVault.add(genRandomStr());
                        }
                        decode = encoderDecoderListEngl.decode(decoyVault, listLambda);
                        foundInvalid = isFoundInvalidEngl(decode);
                    }
                    System.out.println(i + "EN23list_3");
                    writer3.writeLine(String.valueOf(decode));
                }
                writer3.close();
                System.out.println("decoyVaultEngl23List_3 成功");
            }
        });
        myThread2.start();


        System.out.println("over, CN23list");


    }


    @GetMapping("23MarkovtestInitCN")
    public void Markov23testInitCN(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                   @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        encoderDecoderMarkovCN.init( mkv, lambdaOp,  lambdaTimes, lambdaMkv,lambdaMkv_1);
    }

    @GetMapping("23ListtestInitCN")
    public void List23testInitCN(@RequestParam double lambdaOp, @RequestParam double lambdaTimes, @RequestParam double listLambda) {
        encoderDecoderListCN.init( lambdaOp, lambdaTimes, listLambda);
    }

    @GetMapping("Markov19testInitCN")
    public void testInitCN(@RequestParam int mkv, @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1,
                           @RequestParam double lambdaOp, @RequestParam double lambdaTimes) {
        encoderDecoderWithoutPIICN.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);
    }

    @GetMapping("23MarkovtestInitEngl")
    public void Markov23testInitEngl(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                     @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        encoderDecoderMarkovEngl.init( mkv, lambdaOp,  lambdaTimes, lambdaMkv,lambdaMkv_1);
    }

    @GetMapping("23ListtestInitEngl")
    public void List23testInitEngl(@RequestParam double lambdaOp, @RequestParam double lambdaTimes, @RequestParam double listLambda) {
        encoderDecoderListEngl.init(lambdaOp, lambdaTimes, listLambda);
    }

    @GetMapping("Markov19testInitEngl")
    public void testInitEngl(@RequestParam int mkv, @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1,
                             @RequestParam double lambdaOp, @RequestParam double lambdaTimes) {
        encoderDecoderWithoutPIIEngl.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);
    }


}

