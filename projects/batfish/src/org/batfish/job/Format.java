package org.batfish.job;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.batfish.main.ConfigurationFormat;

public final class Format {

   public static ConfigurationFormat identifyConfigurationFormat(String fileText) {
      if (fileText.contains("IOS XR")) {
         return ConfigurationFormat.CISCO_IOS_XR;
      }
      char firstChar = fileText.trim().charAt(0);
      if (firstChar == '!') {
         Matcher aristaMatcher = Pattern.compile("boot system flash.*\\.swi")
               .matcher(fileText);
         if (fileText.contains("set prompt")) {
            return ConfigurationFormat.VXWORKS;
         }
         else if (aristaMatcher.find()) {
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
                  return ConfigurationFormat.CISCO; // may change in future
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
      else if (fileText.contains("version") && fileText.contains("system")
            && fileText.contains("{") && fileText.contains("}")
            && fileText.contains("host-name")
            && fileText.contains("interfaces")) {
         return ConfigurationFormat.JUNIPER;
      }
      return ConfigurationFormat.UNKNOWN;
   }

   private Format() {
   }

}
