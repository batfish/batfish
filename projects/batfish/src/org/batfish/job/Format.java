package org.batfish.job;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.batfish.datamodel.ConfigurationFormat;

public final class Format {

   public static final String BATFISH_FLATTENED_JUNIPER_HEADER = "####BATFISH FLATTENED JUNIPER CONFIG####\n";

   public static final String BATFISH_FLATTENED_VYOS_HEADER = "####BATFISH FLATTENED VYOS CONFIG####\n";

   public static ConfigurationFormat identifyConfigurationFormat(String fileText) {
      String trimmedText = fileText.trim();
      if (trimmedText.length() == 0) {
         return ConfigurationFormat.EMPTY;
      }
      if (fileText.contains("IOS XR")) {
         return ConfigurationFormat.CISCO_IOS_XR;
      }
      char firstChar = trimmedText.charAt(0);
      Matcher setMatcher = Pattern.compile("(?m)^set ").matcher(fileText);
      Matcher flattenedJuniperMatcher = Pattern.compile(
            Pattern.quote(BATFISH_FLATTENED_JUNIPER_HEADER)).matcher(fileText);
      Matcher flatJuniperHostnameDeclarationMatcher = Pattern.compile(
            "(?m)^set (groups [^ ][^ ]* )?system host-name ").matcher(fileText);
      Matcher aristaMatcher = Pattern.compile("(?m)^boot system flash.*\\.swi")
            .matcher(fileText);
      Matcher bladeNetworkMatcher = Pattern.compile("(?m)^switch-type")
            .matcher(fileText);
      Matcher ciscoLike = Pattern.compile(
            "(?m)(^boot system flash.*$)|(^interface .*$)").matcher(fileText);
      Matcher alcatelAosMatcher = Pattern.compile("(?m)^system name").matcher(
            fileText);
      Matcher mssMatcher = Pattern.compile("(?m)^set system name").matcher(
            fileText);
      if (fileText.contains("set system config-management commit-revisions")) {
         return ConfigurationFormat.FLAT_VYOS;
      }
      else if (fileText.contains("INPUT") && fileText.contains("OUTPUT")
            && fileText.contains("FORWARD")) {
         return ConfigurationFormat.IPTABLES;
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
      else if (bladeNetworkMatcher.find()) {
         return ConfigurationFormat.BLADENETWORK;
      }
      else if (firstChar == '!' && fileText.contains("set prompt")) {
         return ConfigurationFormat.VXWORKS;
      }
      else if (fileText.contains("set hostname")) {
         return ConfigurationFormat.JUNIPER_SWITCH;
      }
      else if (flattenedJuniperMatcher.find(0)
            || flatJuniperHostnameDeclarationMatcher.find(0)
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
      else if (ciscoLike.find() || firstChar == '!') {
         return ConfigurationFormat.CISCO;
      }
      else if (alcatelAosMatcher.find()) {
         return ConfigurationFormat.ALCATEL_AOS;
      }
      else if (mssMatcher.find()) {
         return ConfigurationFormat.MSS;
      }
      return ConfigurationFormat.UNKNOWN;
   }

   private Format() {
   }

}
