package com.gag.RuiwuYuexin.common;

import java.io.*;
import java.sql.*;

public class ImageUploader {
    public static void main(String[] args) {
        String jdbcURL = "jdbc:mysql://localhost:3306/fanyunai";
        String username = "root";
        String password = "123456";

        String imagePath = "D:\\照片\\test10.jpg";

        String sql = "INSERT INTO goods (good_name, good_image) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(jdbcURL, username, password);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             FileInputStream fis = new FileInputStream(new File(imagePath))) {

            pstmt.setString(1, "绘画");
            pstmt.setBinaryStream(2, fis, (int) new File(imagePath).length());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("图片成功存入数据库！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

