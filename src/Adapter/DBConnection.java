package Adapter;

import oracle.jdbc.driver.OracleDriver;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

class DBConnection {

    static Connection getConnection(String BaseUrl, String DataBaseName, String UserName, String PassWord) throws SQLException {
        Driver driver = new OracleDriver();
        DriverManager.deregisterDriver(driver); // 注册驱动
        Properties pro = new Properties();
        pro.put("user", UserName); // 账号
        pro.put("password", PassWord); // 密码
        return driver.connect("JDBC:oracle:thin:@localhost:1521/" + DataBaseName, pro); //连接数据库
    }
}