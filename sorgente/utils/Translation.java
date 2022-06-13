package utils;

public class Translation {

    public static int rowTranslation(char x) {
        return ((int) x) - ((int) 'A');
    }

    public static char rowTranslationInv(int x) {
        return (char) (x + ((int) 'A'));
    }

    public static int colTranslation(char y) {
        return Character.getNumericValue(y) - 1;
    }

    public static int colTranslationInv(int y) {
        return y + 1;
    }
}