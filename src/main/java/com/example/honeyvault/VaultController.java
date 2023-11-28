package com.example.honeyvault;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import com.example.honeyvault.data_access.path.PathStatistic;
import com.example.honeyvault.paper23_list_version.EncoderDecoderList;
import com.example.honeyvault.paper23_markov_version.EncoderDecoderMarkov;
import com.example.honeyvault.paper19.EncoderDecoderWithoutPII;
import com.xiaoleilu.hutool.util.CharsetUtil;
import com.xiaoleilu.hutool.util.RandomUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


@RestController
public class VaultController {
    @Resource
    EncoderDecoderMarkov encoderDecoderMarkov;
    @Resource
    EncoderDecoderList encoderDecoderList;
    @Resource
    EncoderDecoderWithoutPII encoderDecoderWithoutPII;

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
        List<Pair<String, String>> encode = encoderDecoderMarkov.encode(vault, fixedLength, mkv, lambdaOp, lambdaTimes,
                lambdaMkv, lambdaMkv_1);
        List<String> encodedString = new ArrayList<>();
        encode.forEach(e -> encodedString.add(e.getValue()));
        return encoderDecoderMarkov.decode(encodedString, mkv);
    }

    @PostMapping("/decodeMarkov")
    public List<String> decodeMarkov(@RequestBody List<String> encodedStrings, @RequestParam String mainPswd, int mkv) {
            return encoderDecoderMarkov.decode(encodedStrings, mkv);
    }


    @PostMapping("/encodeList")
    public List<String> encodeList(@RequestBody List<String> vault,
                                   @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                                   @RequestParam double listLambda
    ) {
        encoderDecoderList.init(lambdaOp, lambdaTimes, listLambda);
        List<Pair<String, String>> encode = encoderDecoderList.encode(vault, fixedLength,listLambda);
        System.out.println(encode.size());
        List<String> encodedString = new ArrayList<>();
        encode.forEach(e -> encodedString.add(e.getValue()));
        System.out.println(encodedString.size());
        return encoderDecoderList.decode(encodedString, listLambda);
    }


    @PostMapping("/decodeList")
    public List<String> decodeList(@RequestBody List<String> encodedStrings, double lambdaOp, double lambdaTimes, double listLambda) {
        encoderDecoderList.init(lambdaOp, lambdaTimes, listLambda);
        return encoderDecoderList.decode(encodedStrings,listLambda);
    }


    @PostMapping("/encodeNoPII")
    public List<String> encodeWithoutPII(@RequestBody List<String> vault, @RequestParam int mkv,
                                         @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1,
                                         @RequestParam double lambdaOp, @RequestParam double lambdaTimes) {
        encoderDecoderWithoutPII.init(mkv, lambdaMkv, lambdaMkv_1, lambdaOp, lambdaTimes);
        List<Pair<String, String>> encode = encoderDecoderWithoutPII.encode(vault, fixedLength, mkv, lambdaMkv);
        List<String> encodedString = new ArrayList<>();
        System.out.println(encode.toString());
        encode.forEach(e -> encodedString.add(e.getValue()));
        return encoderDecoderWithoutPII.decode(encodedString, mkv, lambdaMkv);
    }

    @GetMapping("/gen")
    public void genFakeCsv(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                           @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        CsvWriter writer = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/fake.csv",
                CharsetUtil.CHARSET_UTF_8);
        int count = 0;
        encoderDecoderMarkov.init(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        for (int i = 0; i < 101756; i++) {
            List<String> s = new ArrayList<>();
            s.add(fillWithRandom());
            String s1 = encoderDecoderMarkov.decode(s, 1).toString();
            writer.write(Collections.singleton(s1));
            System.out.println(++count);
        }
        System.out.println("done");
    }


    String fillWithRandom() {
        StringBuilder filledString = new StringBuilder();
        while (filledString.length() < 8192) {
            filledString.append(RandomUtil.randomInt(0, 2));
        }

        return filledString.toString();
    }


    @GetMapping("decoy")
    public void genDecoyVault(@RequestParam int mkv, @RequestParam double lambdaOp, @RequestParam double lambdaTimes,
                              @RequestParam double lambdaMkv, @RequestParam double lambdaMkv_1) {
        encoderDecoderMarkov.init(mkv, lambdaOp, lambdaTimes, lambdaMkv, lambdaMkv_1);
        List<Integer> decoyVaultData = pathStatistic.getDecoyVaultData();
        CsvWriter writer = CsvUtil.getWriter("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/decoyVault.csv",
                CharsetUtil.CHARSET_UTF_8);
        AtomicInteger count = new AtomicInteger();
        System.out.println("开始");
        decoyVaultData.forEach(vaultLength -> {
            List<String> decoyVault = new ArrayList<>();
            for (int i = 0; i < vaultLength; i++) {
                decoyVault.add(fillWithRandom());
            }
            List<String> decodeDecoyVault = encoderDecoderMarkov.decode(decoyVault, mkv);
            writer.write(decodeDecoyVault);
            System.out.println(count.incrementAndGet());
        });

    }

}

