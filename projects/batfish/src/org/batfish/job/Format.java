package org.batfish.job;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.batfish.main.ConfigurationFormat;

public final class Format {

   public static final String BATFISH_FLATTENED_JUNIPER_HEADER = "####BATFISH FLATTENED JUNIPER CONFIG####\n";

   public static final String BATFISH_FLATTENED_VYOS_HEADER = "####BATFISH FLATTENED VYOS CONFIG####\n";

   public static ConfigurationFormat identifyConfigurationFormat(String fileText) {
      if (fileText.contains("IOS XR")) {
         return ConfigurationFormat.CISCO_IOS_XR;
      }
      char firstChar = fileText.trim().charAt(0);
      Matcher setMatcher = Pattern.compile("(?m)^set ").matcher(fileText);
      Matcher flattenedJuniperMatcher = Pattern.compile(
            Pattern.quote(BATFISH_FLATTENED_JUNIPER_HEADER)).matcher(fileText);
      Matcher aristaMatcher = Pattern.compile("(?m)^boot system flash.*\\.swi")
            .matcher(fileText);
      Matcher ciscoLike = Pattern.compile(
            "(?m)(^boot system flash.*$)|(^interface .*$)").matcher(fileText);
      if (fileText.contains("set system config-management commit-revisions")) {
         return ConfigurationFormat.FLAT_VYOS;
      }
      else if (fileText.contains("System.SystemName")) {
         return ConfigurationFormat.MRV;
      }
      else if (fileText.contains("system") && fileText.contains("{")
            && fileText.contains("}") && fileText.contains("config-management")
            && fileText.contains("commit-revisions")) {
         return ConfigurationFormat.VYOS;
      }
      else if (aristaMatcher.find()) {
         return ConfigurationFormat.ARISTA;
      }
      else if (firstChar == '!' && fileText.contains("set prompt")) {
         return ConfigurationFormat.VXWORKS;
      }
      else if (fileText.contains("set hostname")) {
         return ConfigurationFormat.JUNIPER_SWITCH;
      }
      else if (flattenedJuniperMatcher.find(0)
            || fileText.contains("set system host-name")
            || (fileText.contains("apply-groups") && setMatcher.find(0))) {
         return ConfigurationFormat.FLAT_JUNIPER;
      }
      else if (firstChar == '#'
            || (fileText.contains("version") && fileText.contains("system")
                  && fileText.contains("{") && fileText.contains("}")
                  && fileText.contains("host-name") && fileText
                     .contains("interfaces"))) {
         return ConfigurationFormat.JUNIPER;
      }
      else if (aristaMatcher.find()) {
         return ConfigurationFormat.ARISTA;
      }
      else if (ciscoLike.find()) {
         return ConfigurationFormat.CISCO;
      }
      return ConfigurationFormat.UNKNOWN;
   }

   private Format() {
   }

}
