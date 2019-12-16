package com.sjtu.objectdataengine.utils;

import java.util.HashSet;
import java.util.Set;

public class SetOp {
    public static boolean haveIntersection(Set<String> set1, Set<String> set2) {
        if (set1 != null && set2 != null) {
            HashSet<String> set11 = new HashSet<>(set1);
            HashSet<String> set22 = new HashSet<>(set2);
            set11.retainAll(set22);
            return set11.size() != 0;
        }
        return false;
    }
}
