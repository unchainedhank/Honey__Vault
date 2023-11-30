package com.example.honeyvault;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import com.example.honeyvault.data_access.path.PathStatistic;
import com.example.honeyvault.chinese.paper23_list_version.EncoderDecoderListCN;
import com.example.honeyvault.chinese.paper23_markov_version.EncoderDecoderMarkovCN;
import com.example.honeyvault.chinese.paper19.EncoderDecoderWithoutPIICN;
import com.xiaoleilu.hutool.util.CharsetUtil;
import com.xiaoleilu.hutool.util.RandomUtil;
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

    String mainPswd;
    @Resource
    PathStatistic pathStatistic;


    private final int fixedLength = 74 * 128;

    Set<String> candidateSet = new HashSet<>(Arrays.asList("Α", "τ", "Β", "Γ", "Δ", "σ", "Ε", "Ζ", "Η", "ρ",
            "Θ", "Ι", "Κ", "Λ", "Μ", "Ν", "Ξ", "Ο", "Π", "Ρ",
            "Φ", "Χ", "Ψ",
            "Ω", "ω", "ψ",
            "χ", "φ", "υ"));

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

    @PostMapping("/decodeMarkov")
    public List<String> decodeMarkov(@RequestBody List<String> encodedStrings, @RequestParam String mainPswd, int mkv) {
        return encoderDecoderMarkovCN.decode(encodedStrings, mkv);
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


    @PostMapping("/decodeList")
    public List<String> decodeList(@RequestBody List<String> encodedStrings, double lambdaOp, double lambdaTimes,
                                   double listLambda) {
        encoderDecoderListCN.init(lambdaOp, lambdaTimes, listLambda);
        return encoderDecoderListCN.decode(encodedStrings, listLambda);
    }


    @PostMapping("/encodeNoPII")
    public List<String> encodeWithoutPII(@RequestBody List<String> vault, @RequestParam int mkv,
                                         @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1,
                                         @RequestParam double lambdaOp, @RequestParam double lambdaTimes) {
        encoderDecoderWithoutPIICN.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);
        List<Pair<String, String>> encode = encoderDecoderWithoutPIICN.encode(vault, fixedLength, mkv, lambdaMkv);
        List<String> encodedString = new ArrayList<>();
        System.out.println(encode.toString());
        encode.forEach(e -> encodedString.add(e.getValue()));
        return encoderDecoderWithoutPIICN.decode(encodedString, mkv, lambdaMkv);
    }

    String genRandomStr() {
        StringBuilder filledString = new StringBuilder();
        while (filledString.length() < fixedLength) {
            filledString.append(RandomUtil.randomInt(0, 2));
        }
        return filledString.toString();
    }


    @GetMapping("decoy")
    public void genDecoyVault(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                              @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        encoderDecoderMarkovCN.init(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        List<Integer> decoyVaultData = pathStatistic.getDecoyVaultData();
        CsvWriter writer = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/decoyVault.csv",
                CharsetUtil.CHARSET_UTF_8);
        AtomicInteger count = new AtomicInteger();
        System.out.println("开始");
        decoyVaultData.forEach(vaultLength -> {
            List<String> decoyVault = new ArrayList<>();
            for (int i = 0; i < vaultLength; i++) {
                decoyVault.add(genRandomStr());
            }
            List<String> decodeDecoyVault = encoderDecoderMarkovCN.decode(decoyVault, mkv);
            writer.writeLine(String.valueOf(decodeDecoyVault));
            System.out.println(count.incrementAndGet());
        });

    }

    @GetMapping("genDV1")
    public void genDecoyVault19(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
//        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_3.csv", CharsetUtil.CHARSET_UTF_8);

        CsvWriter writer1 = CsvUtil.getWriter
        ("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv/decoyVault19_1.csv", CharsetUtil
        .CHARSET_UTF_8);
        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
                "/decoyVault19_2.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
                "/decoyVault19_3_1.csv", CharsetUtil.CHARSET_UTF_8);

        encoderDecoderWithoutPIICN.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);

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
        }
        writer3.close();
        System.out.println("19文件3成功");
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
            if (candidateSet.contains(s)) {
                finalLength += 5;
            } else finalLength++;
            if (finalLength > 16) return false;
        }
        return finalLength >= 5;
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
            List<String> decode = null;
            boolean foundInvalid = true;
            while (foundInvalid) {
                String ranStr = genRandomStr();
                List<String> decoyVault = new ArrayList<>();
                decoyVault.add(ranStr);
                decode = encoderDecoderMarkovCN.decode(decoyVault, mkv);
                foundInvalid = isFoundInvalid(decode);
            }
            writer1.writeLine(String.valueOf(decode));
        }
        writer1.close();
        System.out.println("23M文件1成功");

        for (int i = 0; i < 150000; i++) {
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
            List<String> decode = null;
            boolean foundInvalid = true;
            while (foundInvalid) {
                String ranStr = genRandomStr();
                List<String> decoyVault = new ArrayList<>();
                decoyVault.add(ranStr);
                decode = encoderDecoderListCN.decode(decoyVault, listLambda);
                foundInvalid = isFoundInvalid(decode);
            }
            writer1.writeLine(String.valueOf(decode));
        }
        writer1.close();
        System.out.println("23L文件1成功");


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
            writer3.writeLine(String.valueOf(decode));
        }
        writer3.close();
        System.out.println("23L文件3成功");

    }

    @GetMapping("test")
    public void test() {
        encoderDecoderMarkovCN.init(1, 0.01, 1, 0.001, 0.01);
    }


}

