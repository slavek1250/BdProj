package com.bdproj;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class PassGen {

    private String str;
    private int randInt;
    private StringBuilder sb;
    private List<Integer> l;

    public PassGen() {
        this.l = new ArrayList<>();
        this.sb = new StringBuilder();

        buildPassword();
    }

    private void buildPassword() {
        //Znaki 0-9, A-Z, a-z
        for (int i = 48; i < 58; i++) {
            l.add(i);
        }
        for (int i=65;i<91;i++){
            l.add(i);
        }
        for (int i=97;i<123;i++){
            l.add(i);
        }
        //Kilka znaków specjalnych: !,@,#,$,%
        l.add(33);
        l.add(35);
        l.add(36);
        l.add(37);
        l.add(64);

        /*Randomise over the ASCII numbers and append respective character
          values into a StringBuilder*/
        for (int i = 0; i < 8; i++) {
            randInt = l.get(new SecureRandom().nextInt(67));
            sb.append((char) randInt);
        }

        str = sb.toString();
    }

    public String generatePassword() {
        return str;
    }
}