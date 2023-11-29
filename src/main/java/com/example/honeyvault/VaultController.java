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
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_1.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer2 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_2.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_3.csv", CharsetUtil.CHARSET_UTF_8);

//        CsvWriter writer1 = CsvUtil.getWriter
//        ("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv/decoyVault19_1.csv", CharsetUtil
//        .CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault19_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault19_3_1.csv", CharsetUtil.CHARSET_UTF_8);

        encoderDecoderWithoutPIICN.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);

//        for (int i = 0; i < 1017; i++) {
        for (int i = 0; i < 101756; i++) {
            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            List<String> decode = encoderDecoderWithoutPIICN.decode(decoyVault, mkv, lambdaMkv);
            writer1.writeLine(String.valueOf(decode));
        }
        writer1.close();
        System.out.println("19文件1成功");

        List<Integer> decoyVaultData = pathStatistic.getDecoyVaultData();
        System.out.println("19年 读取数据行数："+decoyVaultData.size());
        for (Integer vaultLength : decoyVaultData) {
            int maxRetries = 5; // 设置最大重试次数
            int retries = 0;

            List<String> decode = null;

            while (retries < maxRetries) {
                try {
                    decode = decodeVaultLength(mkv, lambdaMkv, vaultLength);
                    // 如果函数执行成功，跳出循环
                    break;
                } catch (Exception e) {
                    // 捕获异常后输出错误信息
                    e.printStackTrace();
                    // 增加重试次数
                    retries++;
                }
            }

            // 在循环结束后，检查是否成功执行函数
            if (decode != null) {
                writer2.writeLine(String.valueOf(decode));
            }
        }
        System.out.println("19文件2成功");
        writer2.close();

        for (int i = 0; i < 1000; i++) {
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
        List<String> decode;
        List<String> decoyVault = new ArrayList<>();
        for (int j = 0; j < 6; j++) {
            decoyVault.add(genRandomStr());
        }
        decode = encoderDecoderWithoutPIICN.decode(decoyVault, mkv, lambdaMkv);
        return decode;
    }

    private List<String> decodeVaultLength(int mkv, double lambdaMkv, Integer vaultLength) {
        List<String> decoyVault = new ArrayList<>();
        for (int j = 0; j < vaultLength; j++) {
            decoyVault.add(genRandomStr());
        }
        return encoderDecoderWithoutPIICN.decode(decoyVault, mkv, lambdaMkv);
    }

    @GetMapping("genDV2")
    public void genDecoyVault23Markov(@RequestParam int mkv, @RequestParam double lambdaOp,
                                      @RequestParam double lambdaTimes,
                                      @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault23MKV_1.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer2 = CsvUtil.getWriter("/app/HvExpData/decoyVault23MKV_2.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault23MKV_3.csv", CharsetUtil.CHARSET_UTF_8);


//        CsvWriter writer1 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23MKV_3.csv", CharsetUtil.CHARSET_UTF_8);


        encoderDecoderMarkovCN.init(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        for (int i = 0; i < 101756; i++) {
            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            List<String> decode = encoderDecoderMarkovCN.decode(decoyVault, mkv);
            writer1.writeLine(String.valueOf(decode));
        }
        writer1.close();
        System.out.println("23M文件1成功");

        List<Integer> decoyVaultData = pathStatistic.getDecoyVaultData();
        System.out.println("23年M 读取数据行数："+decoyVaultData.size());

        decoyVaultData.forEach(vaultLength -> {
            List<String> decode = null;
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < vaultLength; j++) {
                decoyVault.add(genRandomStr());
            }
            int retries = 0;
            int maxRetries = 5;
            while (retries < maxRetries) {
                try {
                    decode = encoderDecoderMarkovCN.decode(decoyVault, mkv);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    retries++;
                }
            }
            if (decode != null) {
                writer2.writeLine(String.valueOf(decode));
            }
        });
        writer2.close();
        System.out.println("23M文件2成功");

        for (int i = 0; i < 1000; i++) {
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                decoyVault.add(genRandomStr());
            }
            List<String> decode = encoderDecoderMarkovCN.decode(decoyVault, mkv);
            writer3.writeLine(String.valueOf(decode));
        }
        writer3.close();
        System.out.println("23M文件3成功");

    }

    @GetMapping("genDV3")
    public void genDecoyVault23List(@RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                    @RequestParam double listLambda) {
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault23List_1.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer2 = CsvUtil.getWriter("/app/HvExpData/decoyVault23List_2.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault23List_3.csv", CharsetUtil.CHARSET_UTF_8);

//        CsvWriter writer1 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_1.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer2 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_2.csv", CharsetUtil.CHARSET_UTF_8);
//        CsvWriter writer3 = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/testCsv" +
//                "/decoyVault23List_3.csv", CharsetUtil.CHARSET_UTF_8);


        encoderDecoderListCN.init(lambdaOp, lambdaTimes, listLambda);

        for (int i = 0; i < 101756; i++) {
            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            List<String> decode = encoderDecoderListCN.decode(decoyVault, listLambda);
            writer1.writeLine(String.valueOf(decode));
        }
        writer1.close();
        System.out.println("23L文件1成功");

//        for (int i = 0; i < 91576; i++) {
        List<Integer> decoyVaultData = pathStatistic.getDecoyVaultData();
        System.out.println("23年L 读取数据行数："+decoyVaultData.size());
        decoyVaultData.forEach(vaultLength -> {
            List<String> decode = null;
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < vaultLength; j++) {
                decoyVault.add(genRandomStr());
            }
            int retries = 0;
            int maxRetries = 5;
            while (retries < maxRetries) {
                try {
                    decode = encoderDecoderListCN.decode(decoyVault, listLambda);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    retries++;
                }
            }
            if (decode != null) {
                writer2.writeLine(String.valueOf(decode));
            }
        });

        writer2.close();
        System.out.println("23L文件2成功");

        for (int i = 0; i < 1000; i++) {
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                decoyVault.add(genRandomStr());
            }
            List<String> decode = encoderDecoderListCN.decode(decoyVault, listLambda);
            writer3.writeLine(String.valueOf(decode));
        }
        writer3.close();
        System.out.println("23L文件3成功");

    }

}

