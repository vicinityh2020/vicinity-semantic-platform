package sk.intersoft.vicinity.platform.semantic;

import java.io.File;
import java.util.Scanner;

public class TestUtil {
    public static String file2string(String path) {
        try{
            return new Scanner(new File(path)).useDelimiter("\\Z").next();
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public static String path(String path) {
        return new File("").getAbsolutePath() + path;
    }
}
