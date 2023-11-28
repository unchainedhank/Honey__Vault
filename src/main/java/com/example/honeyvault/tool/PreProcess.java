package com.example.honeyvault.tool;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.example.honeyvault.UserVault;
import com.example.honeyvault.data_access.markov.MarkovUser;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PreProcess {
    HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();

    public List<List<String>> modifyPswd2PII(Set<MarkovUser> all) {
        List<List<String>> result = new LinkedList<>();
        for (MarkovUser user : all) {
            String id12306 = user.getId_card();
            if (id12306 == null) continue;
            if (id12306.length() < 17) continue;
            if (user.getRealName().length() < 2) continue;

//            if (StringUtils.containsAny(user.getRealName(),"qwertyuiopasdfghjklzxcvbbnm")) continue;
            String password = user.getPassword();
            List<String> vault = new LinkedList<>();
            vault.add(password);
            Date date = new Date();
            try {
                date = parseBirthdayFromIdCard(user.getId_card());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            UserVault piiInstance =
                    UserVault.builder().vault(vault).name(user.getRealName()).birthDate(date).email(user.getEmail()).idCard(user.getId_card()).phone(user.getPhone()).build();
            List<String> vaultWithPII = preProcessPII(piiInstance);
            result.add(vaultWithPII);
        }
        return result;
    }

    public Map<String,String> piiMap(Set<MarkovUser> all) {
        Map<String, String> result = new LinkedHashMap<>();
        for (MarkovUser user : all) {
            String id12306 = user.getId_card();
            if (id12306 == null) continue;
            if (id12306.length() < 17) continue;
            if (user.getRealName().length() < 2) continue;

//            if (StringUtils.containsAny(user.getRealName(),"qwertyuiopasdfghjklzxcvbbnm")) continue;
            String password = user.getPassword();
            List<String> vault = new LinkedList<>();
            vault.add(password);
            Date date = new Date();
            try {
                date = parseBirthdayFromIdCard(user.getId_card());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            UserVault piiInstance =
                    UserVault.builder().vault(vault).name(user.getRealName()).birthDate(date).email(user.getEmail()).idCard(user.getId_card()).phone(user.getPhone()).build();
            List<String> pswdListWithPII = new LinkedList<>();
            preProcessPII(piiInstance).forEach(s -> pswdListWithPII.add(String.join("", s)));
            result.put(user.getPassword(), pswdListWithPII.toString());
        }
        return result;
    }


    public List<String> preProcessPII(UserVault user) {
        Map<String, String> nameMap = preName(user.getName());
        Map<String, String> birthDayMap = preBirth(user.getBirthDate());
        Map<String, String> emailMap = preEmail(user.getEmail());
        Map<String, String> phoneMap = prePhone(user.getPhone());
        Map<String, String> idMap = preIdCard(user.getIdCard());
        List<String> vault = user.getVault();
        List<String> updatedVault = new LinkedList<>();
        vault.forEach(password -> {
            String updatedPassword = passwordWithPII(password, nameMap, birthDayMap, emailMap, phoneMap, idMap);
            updatedVault.add(updatedPassword);
        });
        return updatedVault;
    }


    public Map<String, String> preName(String name) {
        Map<String, String> preNameMap = new LinkedHashMap<>();
        try {
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            format.setVCharType(HanyuPinyinVCharType.WITH_V);

            String  firstNamePinyin= getNamePinyin(name.substring(0, 1), "");
            String lastNamePinyin = getNamePinyin(name.substring(1), "");

            String namePinyin = getNamePinyin(name, " ");
            String[] s = namePinyin.split(" ");
            String fullName = getNamePinyin(name, "");

            preNameMap.put("Α", fullName);
            preNameMap.put("τ", lastNamePinyin+firstNamePinyin);
            preNameMap.put("Β", getN6(s));
            preNameMap.put("Γ", getN5(s));
            preNameMap.put("Δ", getN9(name));
            preNameMap.put("σ", getN2(s));
            preNameMap.put("Ε", firstNamePinyin);
            preNameMap.put("Ζ", lastNamePinyin);
            preNameMap.put("Η", getN7(s));
            preNameMap.put("ρ", Character.toUpperCase(firstNamePinyin.charAt(0)) + firstNamePinyin.substring(1));
        } catch (Exception e) {
            System.out.println(name);
            e.printStackTrace();
        }
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
        Map<String, String> bValues = new LinkedHashMap<>();
        String last2DigitsOfYear = year.substring(year.length() - 2);
        bValues.put("Θ", year + month + day);
        bValues.put("Ι", month + day + year);
        bValues.put("Κ", day + month + year);
        bValues.put("Λ", last2DigitsOfYear + month + day);
        bValues.put("Μ", month + day + last2DigitsOfYear);
        bValues.put("Ν", day + month + last2DigitsOfYear);
        bValues.put("Ξ", year + month);
        bValues.put("Ο", month + year);
        bValues.put("Π", month + day);
        bValues.put("Ρ", year);

        return bValues;
    }


//    public Map<String, String> preAccountName(String name) {
//        Map<String, String> segments = new HashMap<>();
//        segments.put("A1", name); // A1
//        segments.put("A2", extractFirstLetterSegment(name)); // A2
//        segments.put("A3", extractFirstDigitSegment(name)); // A3
//        return segments;
//    }


    public Map<String, String> preEmail(String email) {
        String[] split = email.split("@");
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Φ", split[0]);
        map.put("Χ", extractFirstLetterSegment1(email));
        map.put("Ψ", extractFirstDigitSegment1(email));
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
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Ω", phone);
        map.put("ω", phone.substring(0, 3));
        map.put("ψ", phone.substring(phone.length() - 4));
        return map;
    }

    public Map<String, String> preIdCard(String card) {
        Map<String, String> segments = new LinkedHashMap<>();
        segments.put("χ", card.substring(card.length() - 4)); // I1
        segments.put("φ", card.substring(0, 6)); // I3
        segments.put("υ", card.substring(0, 3)); // I2
        return segments;
    }

    private String passwordWithPII(String password, Map<String, String> nameMap,
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
        StringBuilder result = new StringBuilder();
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
                result.append(window);
                i += 2;
            } else {
                result.append(password.charAt(i));
                i += 1;
            }
        }

        return result.toString();
    }


}
