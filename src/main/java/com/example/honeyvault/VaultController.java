package com.example.honeyvault;

import cn.hutool.core.lang.Pair;
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


    private final int fixedLength = 64 * 128;

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

    @GetMapping("/gen")
    public void genFakeCsv(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                           @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        CsvWriter writer = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/fake.csv",
                CharsetUtil.CHARSET_UTF_8);
        int count = 0;
        encoderDecoderMarkovCN.init(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        for (int i = 0; i < 101756; i++) {
            List<String> s = new ArrayList<>();
            s.add(genRandomStr());
            String s1 = encoderDecoderMarkovCN.decode(s, 1).toString();
            writer.write(Collections.singleton(s1));
            System.out.println(++count);
        }
        System.out.println("done");
    }


    String genRandomStr() {
        StringBuilder filledString = new StringBuilder();
        while (filledString.length() < 8192) {
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
            writer.write(decodeDecoyVault);
            System.out.println(count.incrementAndGet());
        });

    }

    @GetMapping("genDV1")
    public void genDecoyVault19(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_1.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer2 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_2.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault19_3.csv", CharsetUtil.CHARSET_UTF_8);
        encoderDecoderWithoutPIICN.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);

        for (int i = 0; i < 101756; i++) {
            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            List<String> decode = encoderDecoderWithoutPIICN.decode(decoyVault, mkv, lambdaMkv);
            writer1.write(decode);
        }
        for (int i = 0; i < 91576; i++) {
            List<Integer> decoyVaultData = pathStatistic.getDecoyVaultData();
            decoyVaultData.forEach(vaultLength -> {
                List<String> decoyVault = new ArrayList<>();
                for (int j = 0; j < vaultLength; j++) {
                    decoyVault.add(genRandomStr());
                }
                List<String> decode = encoderDecoderWithoutPIICN.decode(decoyVault, mkv, lambdaMkv);
                writer2.write(decode);
            });
        }
        for (int i = 0; i < 999; i++) {
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                decoyVault.add(genRandomStr());
            }
            List<String> decode = encoderDecoderWithoutPIICN.decode(decoyVault, mkv, lambdaMkv);
            writer3.write(decode);
        }
    }

    @GetMapping("genDV2")
    public void genDecoyVault23Markov(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                      @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault23MKV_1.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer2 = CsvUtil.getWriter("/app/HvExpData/decoyVault23MKV_2.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault23MKV_3.csv", CharsetUtil.CHARSET_UTF_8);
        encoderDecoderMarkovCN.init(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        for (int i = 0; i < 101756; i++) {
            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            List<String> decode = encoderDecoderMarkovCN.decode(decoyVault,mkv);
            writer1.write(decode);
        }

        for (int i = 0; i < 91576; i++) {
            List<Integer> decoyVaultData = pathStatistic.getDecoyVaultData();
            decoyVaultData.forEach(vaultLength -> {
                List<String> decoyVault = new ArrayList<>();
                for (int j = 0; j < vaultLength; j++) {
                    decoyVault.add(genRandomStr());
                }
                List<String> decode = encoderDecoderMarkovCN.decode(decoyVault,mkv);
                writer2.write(decode);
            });
        }

        for (int i = 0; i < 999; i++) {
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                decoyVault.add(genRandomStr());
            }
            List<String> decode = encoderDecoderMarkovCN.decode(decoyVault,mkv);
            writer3.write(decode);
        }
    }

    @GetMapping("genDV3")
    public void genDecoyVault23List( @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                    @RequestParam double listLambda) {
        CsvWriter writer1 = CsvUtil.getWriter("/app/HvExpData/decoyVault23List_1.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer2 = CsvUtil.getWriter("/app/HvExpData/decoyVault23List_2.csv", CharsetUtil.CHARSET_UTF_8);
        CsvWriter writer3 = CsvUtil.getWriter("/app/HvExpData/decoyVault23List_3.csv", CharsetUtil.CHARSET_UTF_8);
        encoderDecoderListCN.init(lambdaOp,lambdaTimes,listLambda);

        for (int i = 0; i < 101756; i++) {
            String ranStr = genRandomStr();
            List<String> decoyVault = new ArrayList<>();
            decoyVault.add(ranStr);
            List<String> decode = encoderDecoderListCN.decode(decoyVault,listLambda);
            writer1.write(decode);
        }

        for (int i = 0; i < 91576; i++) {
            List<Integer> decoyVaultData = pathStatistic.getDecoyVaultData();
            decoyVaultData.forEach(vaultLength -> {
                List<String> decoyVault = new ArrayList<>();
                for (int j = 0; j < vaultLength; j++) {
                    decoyVault.add(genRandomStr());
                }
                List<String> decode = encoderDecoderListCN.decode(decoyVault,listLambda);

                writer2.write(decode);
            });
        }

        for (int i = 0; i < 999; i++) {
            List<String> decoyVault = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                decoyVault.add(genRandomStr());
            }
            List<String> decode = encoderDecoderListCN.decode(decoyVault,listLambda);

            writer3.write(decode);
        }
    }

}

