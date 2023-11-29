package test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.math.MathUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import com.xiaoleilu.hutool.util.RandomUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.*;


public class ToolTest {


    public static void main(String[] args) throws ParseException {
        getDecoyVaultData();
    }

    public static List<Integer> getDecoyVaultData() {
        CsvReader reader = CsvUtil.getReader();
        CsvData data = reader.read(FileUtil.file("/Users/a3/IdeaProjects/HoneyVault/src/main/resources/static" +
                "/chinese_merge_noUN.csv"));
//        CsvData data = reader.read(FileUtil.file("/app/classes/static/chinese_merge_noUN.csv"));
        List<CsvRow> rows = data.getRows();
        List<Integer> result = new ArrayList<>();
        int commaCount = 9;
        for (int i = 1; i < 91577; i++) {
            CsvRow csvRow = rows.get(i);
            List<String> rawList = csvRow.getRawList();
            for (int j = 4; j < 10; j++) {
                String s = rawList.get(j);
                if (s != null && s.length() < 5) {
                    System.out.println(s);
                }
            }
        }
        return result;
    }

}
