package com.bdproj.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

/**
 * Klasa przechowyująca parametry służące do połączenia się z bazą danych.
 */
public class MySQLConnParams {

    private static final String CONF_FILE_PATH = "conf/conn.conf";  /**< Ścieżka do pliku konfiguracyjnego. */
    private static final int CODE_OFFSET = 12;                      /**< Offset szyfrowania danych. */
    private static final String PARAM_SERVER_NAME = "server";       /**< Nazwa parametru dotyczącego adresu serwera. */
    private static final String PARAM_PORT_NAME = "port";           /**< Nazwa paramteru dotyczącego portu serwera. */
    private static final String PARAM_DATABASE_NAME = "database";   /**< Nazwa parametru dotyczącego nazwy bazy danych. */
    private static final String PARAM_USER_NAME = "user";           /**< Nazwa parametru dotyczącego nazwy użytkownika. */
    private static final String PARAM_PASSWORD_NAME = "password";   /**< Nazwa paramtery dotyczącego hasła użytkownika. */

    /**
     * Dane przechowywanych parametrów.
     */
    enum ConfParamsEnum {
        PARAM,  /**< Nazwa parametru. */
        VALUE   /**< Wartość parametru. */
    };
    /**
     * Lista parametrów.
     */
    private static ArrayList<EnumMap<ConfParamsEnum, String>> confParams;

    private static String lastError;    /**< Opis ostatniego błędu. */

    /**
     * Getter.
     * @param paramName Nazwa parametru.
     * @return Wartość parametru lub '' (pusty String) jeżeli brak parametru o zadanej nazwie.
     * @see readConnParamsFromFile()
     */
    private static String getParamValue(String paramName) {
        return confParams
                .stream()
                .filter(param -> param.get(ConfParamsEnum.PARAM).equals(paramName))
                .findAny()
                .map(param -> param.get(ConfParamsEnum.VALUE))
                .orElse("");
    }

    /**
     * Getter.
     * @return Zwraca hasło użytkownika bazy danych.
     * @see readConnParamsFromFile()
     */
    public static String getDatabaseUserPass() {
        return MySQLConnParams.getParamValue(PARAM_PASSWORD_NAME);
    }

    /**
     * Getter.
     * @return Zwraca nazwę użytkownika bazy danych.
     * @see readConnParamsFromFile()
     */
    public static String getDatabaseUser() {
        return MySQLConnParams.getParamValue(PARAM_USER_NAME);
    }

    /**
     * Getter.
     * @return Zwraca nazwą bazy danych.
     * @see readConnParamsFromFile()
     */
    public static String getDatabase() {
        return MySQLConnParams.getParamValue(PARAM_DATABASE_NAME);
    }

    /**
     * Getter.
     * @return Zwraca port serwera.
     * @see readConnParamsFromFile()
     */
    public static String getServerPort() {
        return MySQLConnParams.getParamValue(PARAM_PORT_NAME);
    }

    /**
     * Getter.
     * @return Zwraca adres serwera.
     * @see readConnParamsFromFile()
     */
    public static String getServerAddress() {
        return MySQLConnParams.getParamValue(PARAM_SERVER_NAME);
    }

    /**
     * Getter.
     * @return Zwraca opis ostatniego błędu.
     */
    public static String getLastError() {
        return lastError;
    }

    /**
     * Metoda odczytująca parametry z pliku konfiguracyjnego.
     * @return Zwraca true jeżeli pomyślnie odczytano.
     */
    public static boolean readConnParamsFromFile() {

        String confString;
        try {
            //confString = Files.readString(Paths.get(CONF_FILE_PATH));
            confString = new String(Files.readAllBytes(Paths.get(CONF_FILE_PATH)));
        }
        catch (IOException ex) {
            lastError = ex.getMessage();
            return false;
        }
        confString = MySQLConnParams.decode(confString, CODE_OFFSET);
        confString = confString.replace("\r", "");
        String[] confLines = confString.split("\n");
        confParams = new ArrayList<>();
        lastError = confString;
        Arrays.stream(confLines)
                .map(line -> {
                    String[] pairKeyVal = line.split(": ");
                    EnumMap<ConfParamsEnum, String> paramMap = new EnumMap<>(ConfParamsEnum.class);
                    paramMap.put(ConfParamsEnum.PARAM, pairKeyVal.length != 2 ? "" : pairKeyVal[0]);
                    paramMap.put(ConfParamsEnum.VALUE, pairKeyVal.length != 2 ? "" : pairKeyVal[1]);
                    return paramMap;
                })
                .forEach(confParams::add);
        return true;
    }
    /**
     * Metoda deszyfrująca.
     * @param enc Zakodowny łańcuch znaków.
     * @param offset Offset kodowania.
     * @return Rozszyfrowany łańcuch znaków.
     * @see encode()
     */
    public static String decode(String enc, int offset) {
        return encode(enc, 26-offset);
    }

    /**
     * Metoda szyfrująca.
     * @param enc Łańcuch znaków do zaszyfrowania.
     * @param offset Offset szyfrowania.
     * @return Zaszyfrowany łańcuch znaków.
     * @see decode()
     */
    public static String encode(String enc, int offset) {
        offset = offset % 26 + 26;
        StringBuilder encoded = new StringBuilder();
        for (char i : enc.toCharArray()) {
            if (Character.isLetter(i)) {
                if (Character.isUpperCase(i)) {
                    encoded.append((char) ('A' + (i - 'A' + offset) % 26 ));
                } else {
                    encoded.append((char) ('a' + (i - 'a' + offset) % 26 ));
                }
            } else {
                encoded.append(i);
            }
        }
        return encoded.toString();
    }
/*
    public static void genFileTmp() {
        String data = "server: localhost\nport: 3306\ndatabase: slavek_bd2\nuser: slavek_bd2_user\npassword: bd2@2020";
        String encodedData = MySQLConnParams.encode(data, 12);
        try (PrintWriter out = new PrintWriter(CONF_FILE_PATH)) {
            out.println(encodedData);
        }
        catch (IOException ex) {
            lastError = ex.getMessage();
        }
    }
*/
}
