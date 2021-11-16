package ru.geekbrains.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.logging.Level;
import java.sql.*;


public class SQLHandler {
    private static Logger logger = LogManager.getLogger();
    private static Connection connection;
    private static Statement statement;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:server/database.db");
            statement = connection.createStatement();
            logger.info("Успешное подключение к базе данных");
        } catch (Exception e) {
            logger.error("Не удалось подключиться к базе данных " + connection.toString());
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            connection.close();
            logger.info("Отключение от базы данных");
        } catch (SQLException e) {
            logger.error("Не удалось отключитсья от базы данных");
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPassword(String login, String password) {
        try {
            //ResultSet rs = statement.executeQuery("SELECT nickname FROM users WHERE login ='" + login + "' AND password = '" + password + "'");
            logger.debug("Проверка комбинации login = " + login + " password = " + password);
            PreparedStatement ps = connection.prepareStatement("SELECT nickname FROM users WHERE login = ? AND password = ?");
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                logger.debug("пользователь найден");
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.debug("пользователь не найден");
        return null;
    }

    public static boolean tryToRegister(String login, String password, String nickname) {
        try {
            logger.info("Попытка регистрации пользователя с ником " + nickname + " и с логином " + login);
            statement.executeUpdate("INSERT INTO users (login, password, nickname) VALUES ('" + login + "','" + password + "','" + nickname + "')");
            logger.info("Пользователь " + login + " успешно зарегестрирован");
            return true;
        } catch (SQLException e) {
            logger.warn("Неудачная регистрация пользователя login = " + login, " nickname = " + nickname);
            return false;
        }
    }
}
