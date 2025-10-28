package io.github.youtuku;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Main {

    // 获取所有的省市县的行政区划代码
    Map<String,String> getRegionNumber(){
        Map<String,String> regionMap = new HashMap<>();
        Connection conn = null;
        Statement stmt;
        try {
            String url = "jdbc:sqlite:src\\main\\resources\\db\\china_divisions.db";
            conn = java.sql.DriverManager.getConnection(url);
            stmt = conn.createStatement();
            
            String sql = "SELECT * FROM divisions;";
            try (java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    regionMap.put(rs.getString("code"),rs.getString("namep"));
                }
            }
            stmt.close();
            conn.close();
        } catch (java.sql.SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (java.sql.SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return regionMap;
    }

    // 生成随机YYYYMMDD出生日期
    String generateBirthDate(String startYear, String endYear){
        String birthDate;
        int daysInMonth;
        Random rand = new Random();
        int year = rand.nextInt(Integer.parseInt(endYear) - Integer.parseInt(startYear) + 1) + Integer.parseInt(startYear);
        int month = rand.nextInt(12) + 1;
        switch (month) {
            case 4, 6, 9, 11 -> daysInMonth = 30;
            case 2 ->  {
                if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                    daysInMonth = 29;
                } else {
                    daysInMonth = 28;
                }
                        }
            default -> daysInMonth = 31;
        }        
        int day = rand.nextInt(daysInMonth) + 1;
        birthDate = String.format("%04d%02d%02d", year, month, day);
        return birthDate;
    }

    // 生成随机顺序码
    String generateSequenceCode(){
        Random rand = new Random();
        int seqCode = rand.nextInt(999) + 1;
        return String.format("%03d", seqCode);
    }

    // 计算校验码
    char calculateCheckCode(String id17){
        int[] weight = {7, 9, 10, 5, 8  , 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checkCodeList = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        int sum = 0;
        for (int i = 0; i < id17.length(); i++) {
            sum += Character.getNumericValue(id17.charAt(i)) * weight[i];
        }
        int mod = sum % 11;
        return checkCodeList[mod];
    }
    // 检验身份证号码是否合法
    boolean validateID(String idNumber){
        if (idNumber.length() != 18) {
            return false;
        }
        String id17 = idNumber.substring(0, 17);
        char expectedCheckCode = calculateCheckCode(id17);
        char actualCheckCode = idNumber.charAt(17);
        return expectedCheckCode == actualCheckCode;
    }

    public static void main(String[] args) {
        System.out.println("Fake ID Generator\n请选择操作：\n1. 生成身份证号码\n2. 验证身份证号码");
        try (Scanner scanner = new Scanner(System.in)) {
            int choice = scanner.nextInt();
            Map<String,String> regionMap = new Main().getRegionNumber();
            
            switch (choice) {
                case 1 -> {
                    String regionNumber = (String) regionMap.keySet().toArray()[new Random().nextInt(regionMap.size())];
                    System.out.println("选择期望的出生年份范围(例如1970-2099)");
                    System.out.println("注意年份必须为四位数字，且介于1970-2099");

                    System.out.println("请输入起始年份：");
                    int startYear = scanner.nextInt();
                    System.out.println("请输入结束年份：");
                    int endYear = scanner.nextInt();
                    
                    String birthDate = new Main().generateBirthDate(String.valueOf(startYear), String.valueOf(endYear));

                    String sequenceCode = new Main().generateSequenceCode();
                    String id17 = regionNumber + birthDate + sequenceCode;
                    char checkCode = new Main().calculateCheckCode(id17);
                    String fullID = id17 + checkCode;
                    
                    System.out.println("生成的身份证号码为: " + fullID);
                    System.out.println("所属地区: " + regionMap.get(regionNumber));

                    if(sequenceCode.charAt(2) % 2 == 0){
                        System.out.println("性别: 女");
                    } else {
                        System.out.println("性别: 男");
                    }
                    System.out.println("出生日期: " + birthDate.substring(0,4) + "年" + birthDate.substring(4,6) + "月" + birthDate.substring(6,8) + "日");
                    System.err.println("注意：此身份证号码为虚假生成，仅供测试使用，请勿用于非法用途！");
                }
                case 2 -> {
                    System.out.println("请输入要验证的身份证号码:");
                    String idToValidate = scanner.next();
                    boolean isValid = new Main().validateID(idToValidate);
                    if (isValid) {
                        String regionNumber = idToValidate.substring(0, 6);
                        System.out.println("所属地区: " + regionMap.get(regionNumber));
                    } else {
                        System.out.println("身份证号码 " + idToValidate + " 是不合法的。");
                    }
                }
                default -> System.out.println("无效的选择。");
            }
        }
    }
}