package com.example.honeyvault.tool;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CalMkv {
    public static void main(String[] args) {
        List<String> passwd1 = new ArrayList<>();
        passwd1.add("password1");
        passwd1.add("secure1");
        passwd1.add("secret1");
        passwd1.add("test1");
        passwd1.add("12345");

        List<String> passwd2 = new ArrayList<>();
        passwd2.add("password2");
        passwd2.add("secure2");
        passwd2.add("secret2");
        passwd2.add("test2");
        passwd2.add("54321");

//        Map<Character, Integer> result1 = analyzePasswords(passwd1);
//        Map<Character, Integer> result2 = analyzePasswords(passwd2);

    }

    public Map<String, Integer> analyzePasswords(List<String> passwords,int mkv) {
        Map<String, Integer> analysisResult = new HashMap<>();

        for (String password : passwords) {
            if (password != null) {
                for (int i = 0; i < password.length() - (mkv-1); i++) {
                    String substring = password.substring(i, i + mkv);
//                    char lastChar = substring.charAt(4);

                    analysisResult.put(substring, analysisResult.getOrDefault(substring, 0) + 1);
                }
            }
        }

        return analysisResult;
    }

}
