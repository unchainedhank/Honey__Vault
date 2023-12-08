package com.example.honeyvault.data_access.path;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import com.example.honeyvault.tool.CalPath;
import com.example.honeyvault.tool.PathInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PathStatistic {
    public Set<PathAndAlphaUser> parsePswds() {
        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306_163_replace.csv"));
//        CsvData data = reader.read(FileUtil.file("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static/t_12306_163_replace.csv"));
        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();
        for (int i = 1; i < 91577; i++) {
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
        CsvData data = reader.read(FileUtil.file("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static/t_12306_163.csv"));
//        CsvData data = reader.read(FileUtil.file("/app/classes/static/t_12306_163.csv"));
        List<CsvRow> rows = data.getRows();
        Set<PathAndAlphaUser> pathTrainSet = new HashSet<>();
        for (int i = 1; i < 91577; i++) {
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
        }
        return pathTrainSet;
    }







    public List<PathInfo> getPathTrainSet() {
        Set<PathAndAlphaUser> userSet = parsePswds();
        List<PathInfo> pathTrainList = new ArrayList<>();
        for (PathAndAlphaUser user : userSet) {
            String pw_163 = user.getReplace_163();
            String pw_12306 = user.getReplace_12306();
            if (pw_163 == null || pw_12306 == null) {
                continue;
            }
            List<List<String>> paths = CalPath.breadthFirstSearch(pw_12306, pw_163);
            paths.forEach(path -> {
                PathInfo build = PathInfo.builder().path(path.toString()).length(pw_12306.length()).build();
                pathTrainList.add(build);
            });
        }
        return pathTrainList;
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

}
