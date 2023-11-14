package com.example.honeyvault;

import cn.hutool.core.lang.Pair;
import com.example.honeyvault.tool.EncoderDecoder;
import com.example.honeyvault.tool.PreProcess;
import com.xiaoleilu.hutool.util.RandomUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Consumer;


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
    @Resource
    PreProcess preProcessor;

    private UserVault userVault;

    private final int fixedLength = 64 * 128;

    @PostMapping("init")
    public UserVault initVault(@RequestBody UserVault initVault) {
        System.out.println(initVault.getMainPassword());
        this.userVault = initVault;
//                UserVault.builder().mainPassword(initVault.getMainPassword()).vault(initVault.getVault()).build();
        return userVault;
    }

    @PostMapping("/encode")
    public List<Pair<String, String>> encode() {
        System.out.println(userVault);
        List<List<String>> passwordsPII = preProcessor.preProcessPII(userVault);
        System.out.println(passwordsPII);
        List<String> vault = new LinkedList<>();
        passwordsPII.forEach(passwordPII -> vault.add(String.join("", passwordPII)));
        System.out.println(vault);
        return encoderDecoder.encode(vault, fixedLength);
    }


    @PostMapping("/decode")
    public List<String> decode(@RequestBody List<String> encodedStrings, @RequestParam String mainPswd) {
        if (mainPswd != null && mainPswd.equals(userVault.getMainPassword())) {
        List<String> decodedStringList = encoderDecoder.decode(encodedStrings);
        Map<String, String> nameMap = preProcessor.preName(userVault.getName());
        Map<String, String> birthDayMap = preProcessor.preBirth(userVault.getBirthDate());
        Map<String, String> emailMap = preProcessor.preEmail(userVault.getEmail());
        Map<String, String> phoneMap = preProcessor.prePhone(userVault.getPhone());
        Map<String, String> idMap = preProcessor.preIdCard(userVault.getIdCard());
        List<String> updatedList = new ArrayList<>();
        for (String decodedString : decodedStringList) {
            for (Map.Entry<String, String> nameEntry : nameMap.entrySet()) {
                decodedString = decodedString.replace(nameEntry.getKey(), nameEntry.getValue());
            }
            for (Map.Entry<String, String> birthEntry : birthDayMap.entrySet()) {
                decodedString = decodedString.replace(birthEntry.getKey(), birthEntry.getValue());
            }
            for (Map.Entry<String, String> emailEntry : emailMap.entrySet()) {
                decodedString = decodedString.replace(emailEntry.getKey(), emailEntry.getValue());
            }
            for (Map.Entry<String, String> phoneEntry : phoneMap.entrySet()) {
                decodedString = decodedString.replace(phoneEntry.getKey(), phoneEntry.getValue());
            }
            for (Map.Entry<String, String> idEntry : idMap.entrySet()) {
                decodedString = decodedString.replace(idEntry.getKey(), idEntry.getValue());
            }
            updatedList.add(decodedString);
        }
        return updatedList;

        } else {
//                    输入错误的主口令
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
}

