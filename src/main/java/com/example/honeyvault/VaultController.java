package com.example.honeyvault;

import cn.hutool.core.lang.Pair;
import com.example.honeyvault.tool.EncoderDecoder;
import com.xiaoleilu.hutool.util.RandomUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;


@RestController
public class VaultController {
//    @Resource
//    RenRepository renRepository;
//    @Resource
//    DuduRepository duduRepository;
    //    @Resource
//    PathRepo repo;
//    @Resource
//    PathRepoJdbc pathRepoJdbc;

    @Resource
    EncoderDecoder encoderDecoder;

    private UserVault userVault;

    private final int fixedLength = 64 * 128;

    @PostMapping("init")
    public UserVault initVault(@RequestBody UserVault initVault) {
        System.out.println(initVault.getMainPassword());
        this.userVault =
                UserVault.builder().mainPassword(initVault.getMainPassword()).vault(initVault.getVault()).build();
        return userVault;
    }

    @PostMapping("encodeUser")
    public List<Pair<String, String>> encode() {
        List<Pair<String, String>> encode = encoderDecoder.encode(this.userVault.getVault(), fixedLength);
        System.out.println(encode);
        return encode;
    }

    @PostMapping("/encode")
    public List<Pair<String, String>> encode(@RequestBody List<String> initVault) {

//        List<String> initVault = new ArrayList<>();
//        initVault.add("123456");
//        initVault.add("ababab");
//        initVault.add("123123");
//        initVault.add("abcabc");
//        initVault.add("1abab2");


        return encoderDecoder.encode(initVault, fixedLength);
//        System.out.println(encode);
//        List<String> encodedStrings = new LinkedList<>();
//        encode.forEach(pair->{
//            encodedStrings.add(pair.getValue());
//        });
//
//        return decode(encodedStrings);
    }

    @PostMapping("/decode")
    public List<String> decode(@RequestBody List<String> encodedStrings, @RequestParam String mainPswd) {
        if (mainPswd != null && mainPswd.equals(userVault.getMainPassword())) {
            return encoderDecoder.decode(encodedStrings);
        } else {
            //            输入错误的主口令
//            确定一个随机的vault长度 1-10
            int vaultLength = RandomUtil.randomInt(1, 11);
            List<String> randomizedEncodedStrings = new LinkedList<>();
            for (int i = 0; i < vaultLength; i++) {
//              128是安全参数
                String randomizedEncodedPswd = fillWithRandom();
                randomizedEncodedStrings.add(randomizedEncodedPswd);
            }

            return encoderDecoder.decode(randomizedEncodedStrings);

        }


    }

    String fillWithRandom() {
        StringBuilder filledString = new StringBuilder();
        while (filledString.length() < 8192) {
            filledString.append(RandomUtil.randomInt(0, 2));
        }

        return filledString.toString();
    }


//    @GetMapping("add163")
//    public String add163Password() {
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader("/Users/a3/Downloads/163.txt"));
//            String line;
//            int sum = 0;
//            while ((line = reader.readLine()) != null) {
//                String[] parts = line.split("<--DIVIDE-->");
//                String email = parts[0];
//                String passwd163 = parts[1];
//                int i = repo.updatePassword163ByEmail_CNEqualsIgnoreCase(passwd163, email);
//                if (i >= 1) {
//                    sum += i;
//                    System.out.println("update " + i + " row, sum:" + sum);
//                }
//            }
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//
//        }
//        return "S";
//    }

//    @GetMapping("addDudu")
//    public void addDudu() throws IOException {
//        File folder = new File("/Users/a3/Downloads/嘟嘟牛/");
//        File[] files = folder.listFiles();
//
//        if (files != null) {
//            for (int i = 0; i < files.length; i++) {
//                File file = files[i];
//                if (file.isFile()) {
//
//                    System.out.println("Processing file " + (i + 1) + " out of " + files.length + ": " + file
//                    .getName());
//                    BufferedReader br = new BufferedReader(new FileReader(file));
//                    String line;
//
//                    long startTime = System.currentTimeMillis();
//                    long maxL = 1;
//                    Pattern pattern = Pattern.compile("\\s+");
//                    int totalSize = 969569;
//                    double maxProgress = 0.1;
//                    while ((line = br.readLine()) != null) {
//                        long t1 = System.currentTimeMillis();
//                        String[] arr = pattern.split(line);
//                        if (arr.length != 3) continue;
//                        if (arr[2].length() < 2) continue;
//
//                        DuduEntity entity = new DuduEntity();
//                        long t2 = System.currentTimeMillis();
//                        entity.setUsername(arr[0]);
//                        if (t2 - t1 > 30) {
//                            System.out.println(line);
//                            continue;
//                        }
//
//
//                        long t3 = System.currentTimeMillis();
//                        entity.setEmail(arr[1]);
//                        if (t3 - t2 > 30) {
//                            System.out.println(line);
//
//                            continue;
//                        }
//
//                        long t4 = System.currentTimeMillis();
//                        entity.setPassword(arr[2]);
//                        if (t4 - t3 > 30) {
//                            System.out.println(line);
//                            continue;
//                        }
//
//
////                        long t5 = System.currentTimeMillis();
////                        dudus.add(entity);
////                        if (t5 - t4 > 30) {
////                            System.out.println(line);
////                            continue;
////                        }
//                        long t5 = System.currentTimeMillis();
//                        duduRepository.save(entity);
//                        long l = t5 - t4;
//                        if (l > maxL) {
//                            System.out.println("最大插入耗时：" + l);
//                            maxL = l;
//                        }
//
////                        if (dudus.size() > totalSize * maxProgress) {
////                            System.out.println("File" + (i + 1) + "已解析" + maxProgress * 100 + "%");
////                            maxProgress += 0.1;
////                        }
//
//                    }
//
////                    System.out.println("解析完成，插入sql");
////                    long t1 = System.currentTimeMillis();
////                    duduRepository.saveAll(dudus);
////                    long t2 = System.currentTimeMillis();
////                    long l = t2 - t1;
////                    System.out.println("插入完成，耗时" + l + "ms");
//                    br.close();
//                    long endTime = System.currentTimeMillis();
//                    long duration = endTime - startTime;
//                    System.out.println("File " + (i + 1) + " processed in " + duration + " milliseconds");
//                }
//            }
//        }
//    }
//
//    @GetMapping("addRen")
//    public void addRen() throws IOException {
//        File file = new File("/Users/a3/Downloads/嘟嘟牛/");
//
//        BufferedReader br = new BufferedReader(new FileReader(file));
//        String line;
//
//        Pattern pattern = Pattern.compile("\\s+");
//        while ((line = br.readLine()) != null) {
//            String[] arr = pattern.split(line);
//            if (arr.length != 2) continue;
//            if (arr[1].length() < 2) continue;
//            if (arr[0].length() < 2) continue;
//
//            RenEntity entity = new RenEntity();
//
//            long t2 = System.currentTimeMillis();
//            entity.setEmail(arr[0]);
//            long t3 = System.currentTimeMillis();
//            if (t3 - t2 > 30) {
//                System.out.println(line);
//                continue;
//            }
//
//            long t4 = System.currentTimeMillis();
//            entity.setPassword(arr[1]);
//            if (t4 - t3 > 30) {
//                System.out.println(line);
//                continue;
//            }
//            renRepository.save(entity);
//        }
//
//        br.close();
//    }

}
