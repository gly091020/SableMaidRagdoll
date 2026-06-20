package com.gly091020.SableMaidRagdoll.server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GlobalHandledMaidPart {
    private static final List<UUID> HANDED = new ArrayList<>();

    public static void clear(){
        HANDED.clear();
    }

    public static boolean handled(UUID uuid){
        return HANDED.contains(uuid);
    }

    public static void handle(UUID uuid){
        HANDED.add(uuid);
    }
}
