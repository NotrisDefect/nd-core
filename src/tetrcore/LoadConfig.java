package tetrcore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoadConfig {
    
    static InputStream file = LoadConfig.class.getResourceAsStream("stupidconfig.txt");
    
    public static void load() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(file));
        GameLogic.STAGESIZEX = Integer.valueOf(br.readLine());
        GameLogic.STAGESIZEY = Integer.valueOf(br.readLine());
        GameLogic.VISIBLEROWS = Integer.valueOf(br.readLine());
        GameLogic.NEXTPIECESMAX = Integer.valueOf(br.readLine());
    }
}