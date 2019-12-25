package com.bdproj;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

public class MySQLConnParams {

    private static final String CONF_FILE_PATH = "conf/conn.conf";
    private static final int CODE_OFFSET = 12;
    enum ConfParamsEnum { PARAM, VALUE };
    private static ArrayList<EnumMap<ConfParamsEnum, String>> confParams;

    private static final String PARAM_SERVER_NAME = "server";
    private static final String PARAM_PORT_NAME = "port";
    private static final String PARAM_DATABASE_NAME = "database";
    private static final String PARAM_USER_NAME = "user";
    private static final String PARAM_PASSWORD_NAME = "password";

    private static String lastError;

    public static String getDatabaseUserPass() {
        return MySQLConnParams.getParamValue(PARAM_PASSWORD_NAME);
    }

    public static String getDatabaseUser() {
        return MySQLConnParams.getParamValue(PARAM_USER_NAME);
    }

    public static String getDatabase() {
        return MySQLConnParams.getParamValue(PARAM_DATABASE_NAME);
    }

    public static String getServerPort() {
        return MySQLConnParams.getParamValue(PARAM_PORT_NAME);
    }

    public static String getServerAddress() {
        return MySQLConnParams.getParamValue(PARAM_SERVER_NAME);
    }

    public static String getLastError() {
        return lastError;
    }

    public static boolean readConnParamsFromFile() {

        String confString;
        try {
            confString = Files.readString(Paths.get(CONF_FILE_PATH));
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

    private static String getParamValue(String paramName) {
        return confParams
                .stream()
                .filter(param -> param.get(ConfParamsEnum.PARAM).equals(paramName))
                .findAny()
                .map(param -> param.get(ConfParamsEnum.VALUE))
                .orElse("");
    }

    public static String decode(String enc, int offset) {
        return encode(enc, 26-offset);
    }

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
