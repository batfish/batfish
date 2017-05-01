package org.batfish.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Version {

   private static final String UNKNOWN_VERSION = "0.0.0";

   private static final String VERSION = "0.18.0";

   public static void checkCompatibleVersion(String myName, String otherName,
         String otherVersion) {
      if (otherVersion == null) {
         otherVersion = UNKNOWN_VERSION;
      }
      if (!isCompatibleVersion(myName, otherName, otherVersion)) {
         throw new IllegalArgumentException(otherName + " version: '"
               + otherVersion + "' is not compatible with " + myName
               + " version: '" + VERSION + "'");
      }
   }

   public static String getVersion() {
      return VERSION;
   }

   public static List<Integer> getVersionBreakdown(String name,
         String version) {
      List<Integer> result;
      try {
         List<String> parts = Arrays.asList(version.split("\\."));
         result = parts.stream().map(str -> Integer.parseInt(str))
               .collect(Collectors.toList());
      }
      catch (Exception e) {
         throw new BatfishException(
               name + " version has bad format: " + version);
      }
      if (result.size() != 3) {
         throw new BatfishException(
               name + " version does not have 3 subparts: " + version);
      }
      return result;
   }

   public static boolean isCompatibleVersion(String myName, String otherName,
         String otherVersion) {
      if (otherVersion == null) {
         otherVersion = UNKNOWN_VERSION;
      }
      List<Integer> myBits = getVersionBreakdown(myName, VERSION);
      List<Integer> otherBits = getVersionBreakdown(otherName, otherVersion);
      boolean compatible = myBits.get(0).equals(otherBits.get(0))
            && myBits.get(1).equals(otherBits.get(1));
      return compatible;
   }

   private Version() {
   }
}
