package com.gag.RuiwuYuexin.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;

public class ImageUploader2 {

    public static void main(String[] args) {
        String jdbcURL    = "jdbc:mysql://localhost:3306/fanyunai?useSSL=false&serverTimezone=UTC";
        String dbUser     = "root";
        String dbPassword = "123456";

        // 要插入的图片路径列表（可以多个）
        String[] imagePaths = {
                "D:\\照片\\test10.jpg",

        };

        // 1) 插入 product，仅写名称，图片字段已移除（或保留为主图可选）
        String insertProductSql =
                "INSERT INTO goods (good_name) VALUES (?)";

        // 2) 插入 product_image，关联 product_id
        String insertImageSql =
                "INSERT INTO goods_image (good_id, image_data, sort_order) VALUES (?, ?, ?)";

        try (
                Connection conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
                // 插入 product 用 RETURN_GENERATED_KEYS 拿回自增主键
                PreparedStatement psProduct = conn.prepareStatement(
                        insertProductSql, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement psImage = conn.prepareStatement(insertImageSql);
        ) {
            conn.setAutoCommit(false);  // 事务

            // ----- 1. 插入 product -----
            psProduct.setString(1, "绘画系列");
            int affected = psProduct.executeUpdate();
            if (affected != 1) {
                throw new SQLException("插入 product 失败，受影响行数=" + affected);
            }

            // 取回自增主键
            ResultSet rs = psProduct.getGeneratedKeys();
            if (!rs.next()) {
                throw new SQLException("无法获取插入的 product id");
            }
            int productId = rs.getInt(1);
            rs.close();

            // ----- 2. 插入 product_image -----
            for (int i = 0; i < imagePaths.length; i++) {
                File imgFile = new File(imagePaths[i]);
                try (InputStream fis = new FileInputStream(imgFile)) {
                    psImage.setInt   (1, productId);
                    psImage.setBinaryStream(2, fis, (int) imgFile.length());
                    psImage.setInt   (3, i);  // sort_order，按数组顺序
                    int rows = psImage.executeUpdate();
                    if (rows != 1) {
                        throw new SQLException("插入 product_image 第 " + i + " 张失败");
                    }
                }
            }

            conn.commit();
            System.out.println("插入成功：product_id = " + productId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
