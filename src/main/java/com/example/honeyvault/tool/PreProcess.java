package com.example.honeyvault.tool;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.example.honeyvault.UserVault;
import com.example.honeyvault.data_access.PasswdPath;
import com.example.honeyvault.data_access.PathRepo;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PreProcess {
    HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
    @Resource
    private PathRepo pathRepo;

    public List<List<String>> modifyPswd2PII(List<PasswdPath> all) {
        List<List<String>> result = new LinkedList<>();
        for (PasswdPath user : all) {
            String id12306 = user.getId12306();
            if (id12306==null) continue;
            if (id12306.length()<17) continue;
            String password163 = user.getPassword163();
            String passwdCN = user.getPasswd_CN();
            String password12306 = user.getPassword12306();
            List<String> vault = new LinkedList<>();
            vault.add(password12306);
            vault.add(passwdCN);
            if (password163 != null) {
                vault.add(password163);
            }
            Date date = new Date();
            try {
                date = parseBirthdayFromIdCard(user.getId12306());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            UserVault piiInstance =
                    UserVault.builder().vault(vault).name(user.getReal_name()).birthDate(date).email(user.getEmail_CN()).idCard(user.getId12306()).phone(user.getPhone()).build();
            List<String> pswdListWithPII = new LinkedList<>();
            preProcessPII(piiInstance).forEach(s -> pswdListWithPII.add(String.join("", s)));
            result.add(pswdListWithPII);
        }
        return result;
    }


    public List<List<String>> preProcessPII(UserVault user) {
        Map<String, String> nameMap = preName(user.getName());
        Map<String, String> birthDayMap = preBirth(user.getBirthDate());
        Map<String, String> emailMap = preEmail(user.getEmail());
        Map<String, String> phoneMap = prePhone(user.getPhone());
        Map<String, String> idMap = preIdCard(user.getIdCard());
        List<String> vault = user.getVault();
        List<List<String>> updatedVault = new LinkedList<>();
        vault.forEach(password -> {
            List<String> updatedPassword = passwordWithPII(password, nameMap, birthDayMap, emailMap, phoneMap, idMap);
            updatedVault.add(updatedPassword);
        });
        return updatedVault;
    }


    public Map<String, String> preName(String name) {
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
        preNameMap.put("N8", firstNamePinyin + lastNamePinyin);
        preNameMap.put("N9", getN9(name));
        preNameMap.put("N10", Character.toUpperCase(firstNamePinyin.charAt(0)) + firstNamePinyin.substring(1));
        return preNameMap;
    }

    private String getNamePinyin(String name, String separator) {
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

    private String getN2(String[] s) {
        StringBuilder builder = new StringBuilder();
        for (String value : s) {
            String substring = value.substring(0, 1);
            builder.append(substring);
        }
        return builder.toString();
    }

    private String getN5(String[] s) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < s.length; i++) {
            String substring = s[i].substring(0, 1);
            builder.append(substring);
        }
        return builder + s[0];
    }

    private String getN6(String[] s) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < s.length; i++) {
            String substring = s[i].substring(0, 1);
            builder.append(substring);
        }
        return builder.insert(0, s[0]).toString();
    }

    private String getN7(String[] s) {
        return s[0].substring(0, 1).toUpperCase() + s[0].substring(1);
    }

    private String getN9(String name) {
        char[] chars = name.toCharArray();
        StringBuilder N9Builder = new StringBuilder();
        for (int i = 1; i < chars.length; i++) {
            N9Builder.append(PinyinUtil.getFirstLetter(chars[i]));
        }
        N9Builder.append(PinyinUtil.getFirstLetter(chars[0]));
        return N9Builder.toString();
    }

    private Date parseBirthdayFromIdCard(String idCard) throws ParseException {
        // 身份证号码中的生日部分通常是从第7位到第14位
        if (idCard == null) {
            return new Date();

        }
        if (idCard.length() < 15) {
            return new Date();
        }
        String birthdayString = idCard.substring(6, 14);

        // 使用SimpleDateFormat进行解析
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.parse(birthdayString);
    }

    public Map<String, String> preBirth(Date birthDay) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedBirthday = dateFormat.format(birthDay);
        String year = (formattedBirthday.substring(0, 4));
        String month = (formattedBirthday.substring(5, 7));
        String day = (formattedBirthday.substring(8, 10));
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
        if (matcher.matches() && matcher.group(1).length() >= 3) {
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

    private List<String> passwordWithPII(String password, Map<String, String> nameMap,
                                         Map<String, String> birthDayMap, Map<String, String> emailMap, Map<String,
            String> phoneMap, Map<String, String> idMap) {
        Map<String, String> pii = new HashMap<>();
        pii.putAll(nameMap);
        pii.putAll(birthDayMap);
        pii.putAll(emailMap);
        pii.putAll(phoneMap);
        pii.putAll(idMap);
        List<String> key2Remove = new ArrayList<>();
        for (Map.Entry<String, String> piiMap : pii.entrySet()) {
            String pattern = piiMap.getValue();
            String token = piiMap.getKey();
            if ("".equals(pattern)) {
                key2Remove.add(token);
            }
        }
        key2Remove.forEach(pii::remove);

        Map<Integer, String> replaceIndex = new HashMap<>();
        List<String> result = new LinkedList<>();
        for (Map.Entry<String, String> kv : pii.entrySet()) {
            String pattern = kv.getValue();
            String token = kv.getKey();
            while (password.contains(pattern)) {
                int index = password.indexOf(pattern);
                password = password.replaceFirst(pattern, token);
                replaceIndex.put(index, token);
            }
        }

        for (int i = 0; i < password.length(); ) {
            String window;
            if (i < password.length() - 1) {
                window = password.substring(i, i + 2);
            } else {
                window = String.valueOf(password.charAt(i));
            }
            if (pii.containsKey(window) && (window.equals(replaceIndex.get(i)))) {
                result.add(window);
                i += 2;
            } else {
                result.add(String.valueOf(password.charAt(i)));
                i += 1;
            }
        }

        return result;
    }


}
