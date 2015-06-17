package org.batfish.job;

import org.batfish.main.ConfigurationFormat;

public final class Format {

   public static ConfigurationFormat identifyConfigurationFormat(String fileText) {
      if (fileText.contains("IOS XR")) {
         return ConfigurationFormat.CISCO_IOS_XR;
      }
      char firstChar = fileText.trim().charAt(0);
      if (firstChar == '!') {
         if (fileText.contains("set prompt")) {
            return ConfigurationFormat.VXWORKS;
         }
         else if (fileText.contains("boot system flash")) {
            return ConfigurationFormat.ARISTA;
         }
         else {
            String[] lines = fileText.split("\\n");
            for (String line : lines) {
               String trimmedLine = line.trim();
               if (trimmedLine.length() == 0 || trimmedLine.startsWith("!")) {
                  continue;
               }
               if (line.startsWith("version")) {
                  return ConfigurationFormat.CISCO;
               }
               else {
                  break;
               }
            }
         }
      }
      else if (fileText.contains("set hostname")) {
         return ConfigurationFormat.JUNIPER_SWITCH;
      }
      else if (firstChar == '#') {
         if (fileText.contains("set version")) {
            return ConfigurationFormat.FLAT_JUNIPER;
         }
         else {
            return ConfigurationFormat.JUNIPER;
         }
      }
      return ConfigurationFormat.UNKNOWN;
   }

   private Format() {
   }

}
