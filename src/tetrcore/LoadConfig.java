package tetrcore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoadConfig {

    static InputStream file = LoadConfig.class.getResourceAsStream("config.txt");

    public static void load() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(file));
        GameLogic.STAGESIZEX = Integer.parseInt(br.readLine());
        GameLogic.STAGESIZEY = Integer.parseInt(br.readLine());
        GameLogic.PLAYABLEROWS = Integer.parseInt(br.readLine());
        GameLogic.NEXTPIECESMAX = Integer.parseInt(br.readLine());
    }
}