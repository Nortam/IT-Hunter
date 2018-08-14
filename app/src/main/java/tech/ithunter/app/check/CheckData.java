package tech.ithunter.app.check;

public class CheckData {

    public static boolean isEmailValid(String email) {
        return email.contains("@");
    }

    public static boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

}
