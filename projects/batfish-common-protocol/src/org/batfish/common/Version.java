package org.batfish.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Version {

   private static final String VERSION = "0.2.0";

   public static String getVersion() {
      return VERSION;
   }

   public static List<Integer> getVersionBreakdown(String versionStr) {
      List<String> parts = Arrays.asList(versionStr.split("\\."));
      return parts.stream().map(str -> Integer.parseInt(str))
            .collect(Collectors.toList());
   }

   private Version() {
   }
}
