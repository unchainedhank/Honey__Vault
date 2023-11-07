package com.example.honeyvault.tool;

import cn.hutool.extra.pinyin.PinyinUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PreProcess {
    static HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();



    public static void main(String[] args) {
        System.out.println(preName("安超"));
    }


    static Map<String, String> preName(String name) {
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        String lastNamePinyin = getNamePinyin(name.substring(0, 1), "");
        String firstNamePinyin = getNamePinyin(name.substring(1), "");

        String namePinyin = getNamePinyin(name, " ");
        String[] s = namePinyin.split(" ");
        String fullName = getNamePinyin(name, "");

        Map<String, String> preNameMap = new HashMap<>();
        preNameMap.put("N1", fullName);
        preNameMap.put("N2", getN2(s));
        preNameMap.put("N3", lastNamePinyin);
        preNameMap.put("N4", firstNamePinyin);
        preNameMap.put("N5", getN5(s));
        preNameMap.put("N6", getN6(s));
        preNameMap.put("N7", getN7(s));
        preNameMap.put("N8", firstNamePinyin+lastNamePinyin);
        preNameMap.put("N9", getN9(name));
        preNameMap.put("N10", Character.toUpperCase(firstNamePinyin.charAt(0)) + firstNamePinyin.substring(1));
        return preNameMap;
    }

    private static String getNamePinyin(String name, String separator) {
        StringBuilder pinyin = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isWhitespace(c)) {
                continue;
            }
            try {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    pinyin.append(pinyinArray[0]).append(separator);
                } else {
                    pinyin.append(c).append(separator);
                }
            } catch (Exception e) {
                pinyin.append(c).append(separator);
            }
        }
        return pinyin.toString().trim();
    }

    private static String getN2(String[] s) {
        StringBuilder builder = new StringBuilder();
        for (String value : s) {
            String substring = value.substring(0, 1);
            builder.append(substring);
        }
        return builder.toString();
    }

    private static String getN5(String[] s) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < s.length; i++) {
            String substring = s[i].substring(0, 1);
            builder.append(substring);
        }
        return builder + s[0];
    }

    private static String getN6(String[] s) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < s.length; i++) {
            String substring = s[i].substring(0, 1);
            builder.append(substring);
        }
        return builder.insert(0, s[0]).toString();
    }

    private static String getN7(String[] s) {
        return s[0].substring(0, 1).toUpperCase() + s[0].substring(1);
    }

    private static String getN9(String name) {
        char[] chars = name.toCharArray();
        StringBuilder N9Builder = new StringBuilder();
        for (int i = 1; i < chars.length; i++) {
            N9Builder.append(PinyinUtil.getFirstLetter(chars[i]));
        }
        N9Builder.append(PinyinUtil.getFirstLetter(chars[0]));
        return N9Builder.toString();
    }



    public Map<String, String> preBirth(String year,String month,String day) {

        Map<String, String> bValues = new HashMap<>();
        bValues.put("B1", year + month + day);
        bValues.put("B2", month + day + year);
        bValues.put("B3", day + month + year);
        bValues.put("B4", month + day);
        bValues.put("B5", year);
        bValues.put("B6", year + month);
        bValues.put("B7", month + year);
        String last2DigitsOfYear = year.substring(year.length() - 2);
        bValues.put("B8", last2DigitsOfYear + month + day);
        bValues.put("B9", month + day + last2DigitsOfYear);
        bValues.put("B10", day + month + last2DigitsOfYear);

        return bValues;
    }


//    public Map<String, String> preAccountName(String name) {
//        Map<String, String> segments = new HashMap<>();
//        segments.put("A1", name); // A1
//        segments.put("A2", extractFirstLetterSegment(name)); // A2
//        segments.put("A3", extractFirstDigitSegment(name)); // A3
//        return segments;
//    }

    private String extractFirstLetterSegment(String name) {
        StringBuilder segment = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isLetter(c)) {
                segment.append(c);
            } else {
                break; // 停止提取，遇到非字母字符
            }
        }
        return segment.toString();
    }

    private String extractFirstDigitSegment(String name) {
        StringBuilder segment = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isDigit(c)) {
                segment.append(c);
            }
        }
        return segment.toString();
    }


    public Map<String, String> preEmail(String email) {
        String[] split = email.split("@");
        Map<String, String> map = new HashMap<>();
        map.put("E1", split[0]);
        map.put("E2", extractFirstLetterSegment1(email));
        map.put("E3", extractFirstDigitSegment1(email));
        return map;
    }

    private String extractFirstLetterSegment1(String email) {
        // 使用正则表达式提取邮箱前缀的首字母部分
        Pattern pattern = Pattern.compile("^([a-zA-Z]+)[0-9]+.*$");
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }

    private String extractFirstDigitSegment1(String email) {
        // 使用正则表达式提取邮箱前缀的首数字部分
        Pattern pattern = Pattern.compile("^[a-zA-Z]+([0-9]+).*@.*$");
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches() &&matcher.group(1).length()>=3) {
            return matcher.group(1);
        }
        return "";
    }


    public Map<String, String> prePhone(String phone) {
        Map<String, String> map = new HashMap<>();
        map.put("P1", phone);
        map.put("P2", phone.substring(0, 3));
        map.put("P3", phone.substring(phone.length() - 4));
        return map;
    }

    public Map<String, String> preIdCard(String card) {
        Map<String, String> segments = new HashMap<>();
        segments.put("I1", card.substring(card.length() - 4)); // I1
        segments.put("I2", card.substring(0, 3)); // I2
        segments.put("I3", card.substring(0, 6)); // I3
        return segments;
    }


}
