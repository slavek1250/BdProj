package com.bdproj;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasa służąca do generowania losowego hasła o ośmiu znakach takich jak: a-z, A-Z, 0-9,!,@,#,$,%.
 */
public class PassGen {

    private String str; /** < Zmienna pomocnicza z wygenerowanym hasłem. */
    private int randInt; /** <Zmienna do pobierania indeksu z listy. */
    private StringBuilder sb; /** <Obiekt do tworzenia łańcucha znaków z losowo wybranymi znakami z listy.*/
    private List<Integer> l; /** <Lista przechowująca wszystkie dostępne znaki potrzebne do generacji hasła. */

    /**
     * Metoda do generacji hasła, w liście zapisywane są indeksy z tabeli ASCII, a następnie losowo "wyciąganych" jest osiem indeksów, które zamieniane są na odpowiednie znaki z tabeli i zapiywane jako String.
     */
    private void buildPassword() {
        l = new ArrayList<>();
        sb = new StringBuilder();
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

        for (int i = 0; i < 8; i++) {
            randInt = l.get(new SecureRandom().nextInt(67));
            sb.append((char) randInt);
        }

        str = sb.toString();
    }

    /**
     * Metoda zwracająca losowo wygenerowane hasło.
     * @return Zwraca wygenerowane hasło.
     */
    public String generatePassword() {
        buildPassword();
        return str;
    }
}