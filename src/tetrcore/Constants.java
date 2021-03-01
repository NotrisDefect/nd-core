package tetrcore;

public class Constants {
    public static int idLength = 3;
    public static String idCharSet = "ABCDEF";
    public static boolean iKnowWhatIAmDoing = true;

    public enum DeathAnimation {
        NONE, EXPLOSION, GRAYSCALE, CLEAR, DISAPPEAR
    }

    public static DeathAnimation deathAnim = DeathAnimation.GRAYSCALE;
}
