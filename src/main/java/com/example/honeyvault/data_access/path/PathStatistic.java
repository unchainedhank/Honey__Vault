package com.example.honeyvault.data_access.path;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import com.example.honeyvault.tool.CalPath;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PathStatistic {
    public Set<PathAndAlphaUser> parsePswds() {
        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306_163_replace.csv"));
        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();
        for (int i = 1; i < 91243; i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String p1 = rawList.get(4);
            String p2 = rawList.get(5);
            if (CalPath.LongestComSubstr(p1, p2).length() >=
                    0.5 * Math.max(p1.length(), p2.length())) {
                String replace_12306 = rawList.get(6);
                String replace_163 = rawList.get(7);

                PathAndAlphaUser user =
                        PathAndAlphaUser.builder().replace_163(replace_163).replace_12306(replace_12306).build();
                pathTrainSet.add(user);
            }

        }
        return pathTrainSet;
    }

    public Set<PathAndAlphaUser> parsePswdsWithoutPII() {
        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306_163.csv"));
        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();
        for (int i = 1; i < 91226; i++) {
//        for (int i = 1; i < 9122; i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            String p1 = rawList.get(4);
            String p2 = rawList.get(5);
            if (CalPath.LongestComSubstr(p1, p2).length() >=
                    0.5 * Math.max(p1.length(), p2.length())) {
                String pswd12306 = rawList.get(4);
                String pswd163 = rawList.get(5);

                PathAndAlphaUser user =
                        PathAndAlphaUser.builder().replace_163(pswd163).replace_12306(pswd12306).build();
                pathTrainSet.add(user);
            }
//            好的，现在加入另外两个编辑函数hi()和ti()，当bfSource!=target且hd()和td()不再能使bfSrouce发生变化时，尝试广度搜索hi和ti，搜索的逻辑同上，将路径
        }
        return pathTrainSet;
    }

    public List<String> getPathTrainSetWithoutPII() {
        Set<PathAndAlphaUser> userSet = parsePswdsWithoutPII();
        List<String> pathTrainList = new ArrayList<>();
        for (PathAndAlphaUser user : userSet) {
            String pw_163 = user.getReplace_163();
            String pw_12306 = user.getReplace_12306();
            if (pw_163 == null || pw_12306 == null) {
                continue;
            }
            List<List<String>> paths = CalPath.breadthFirstSearch(pw_12306, pw_163);
            paths.forEach(path -> pathTrainList.add(path.toString()));

        }
        return pathTrainList;
    }


    public List<String> getPathTrainSet() {
        Set<PathAndAlphaUser> userSet = parsePswds();
        List<String> pathTrainList = new ArrayList<>();
        for (PathAndAlphaUser user : userSet) {
            String pw_163 = user.getReplace_163();
            String pw_12306 = user.getReplace_12306();
            if (pw_163 == null || pw_12306 == null) {
                continue;
            }
            List<List<String>> paths = CalPath.breadthFirstSearch(pw_12306, pw_163);
            paths.forEach(path -> pathTrainList.add(path.toString()));

        }
        return pathTrainList;
    }

    public List<Integer> getDecoyVaultData() {
        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("/app/classes/static/chinese_merge_noUN.csv"));
        List<CsvRow> rows = data.getRows();
        List<Integer> result = new ArrayList<>();
        for (int i = 1; i < 91595; i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            List<String> decoyVaultData_i = new ArrayList<>();
            Optional.ofNullable(rawList.get(4)).ifPresent(decoyVaultData_i::add);
            Optional.ofNullable(rawList.get(5)).ifPresent(decoyVaultData_i::add);
            Optional.ofNullable(rawList.get(6)).ifPresent(decoyVaultData_i::add);
            Optional.ofNullable(rawList.get(7)).ifPresent(decoyVaultData_i::add);
            Optional.ofNullable(rawList.get(8)).ifPresent(decoyVaultData_i::add);
            Optional.ofNullable(rawList.get(9)).ifPresent(decoyVaultData_i::add);
            result.add(decoyVaultData_i.size());
        }
        return result;
    }

}
