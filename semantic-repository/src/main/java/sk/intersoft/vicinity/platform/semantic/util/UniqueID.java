package sk.intersoft.vicinity.platform.semantic.util;

import java.util.UUID;

public class UniqueID {
    public static String create(){
        return UUID.randomUUID().toString();
    }
}
