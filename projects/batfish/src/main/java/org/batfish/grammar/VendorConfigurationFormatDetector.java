package org.batfish.grammar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.batfish.datamodel.ConfigurationFormat;

public final class VendorConfigurationFormatDetector {

   public static final String BATFISH_FLATTENED_JUNIPER_HEADER = "####BATFISH FLATTENED JUNIPER CONFIG####\n";

   public static final String BATFISH_FLATTENED_VYOS_HEADER = "####BATFISH FLATTENED VYOS CONFIG####\n";

   public static ConfigurationFormat identifyConfigurationFormat(
         String fileText) {
      return new VendorConfigurationFormatDetector(fileText)
            .identifyConfigurationFormat();
   }

   private String _fileText;

   private char _firstChar;

   private boolean _notJuniper;

   private VendorConfigurationFormatDetector(String fileText) {
      _fileText = fileText;
   }

   private void blacklist() {
      Matcher bannerMatcher = Pattern.compile("(?m)^banner ")
            .matcher(_fileText);
      if (bannerMatcher.find()) {
         _notJuniper = true;
      }

   }

   private ConfigurationFormat checkAlcatelAos() {
      Matcher alcatelAosMatcher = Pattern.compile("(?m)^system name")
            .matcher(_fileText);
      if (alcatelAosMatcher.find()) {
         return ConfigurationFormat.ALCATEL_AOS;
      }
      return null;
   }

   private ConfigurationFormat checkArista() {
      Matcher aristaMatcher = Pattern
            .compile("(?m)^.*boot system flash.*\\.swi").matcher(_fileText);
      if (aristaMatcher.find()) {
         return ConfigurationFormat.ARISTA;
      }
      return null;
   }

   private ConfigurationFormat checkBlade() {
      Matcher bladeNetworkMatcher = Pattern.compile("(?m)^switch-type")
            .matcher(_fileText);
      if (bladeNetworkMatcher.find()) {
         return ConfigurationFormat.BLADENETWORK;
      }
      return null;
   }

   private ConfigurationFormat checkCisco() {
      Matcher ciscoLike = Pattern
            .compile("(?m)(^boot system flash.*$)|(^interface .*$)")
            .matcher(_fileText);
      Matcher ciscoStyleAcl = Pattern.compile("(?m)(^(ip )?access-list.*$)")
            .matcher(_fileText);
      Matcher nexusCommitLine = Pattern.compile("(?m)^ *commit *$")
            .matcher(_fileText);
      Matcher neighborActivateMatcher = Pattern
            .compile("(?m)^ *neighbor.*activate$").matcher(_fileText);
      Matcher neighborPeerGroupMatcher = Pattern
            .compile("(?m)^ *neighbor.*peer-group$").matcher(_fileText);
      if (ciscoLike.find() || _firstChar == '!' || ciscoStyleAcl.find()) {
         if (_fileText.contains("exit-address-family")
               || neighborActivateMatcher.find()
               || neighborPeerGroupMatcher.find()) {
            return ConfigurationFormat.CISCO_IOS;
         }
         else {
            return ConfigurationFormat.CISCO_NX;
         }
      }
      else if (nexusCommitLine.find()) {
         return ConfigurationFormat.CISCO_NX;
      }
      return null;
   }

   private ConfigurationFormat checkCiscoXr() {
      if (_fileText.contains("IOS XR")) {
         return ConfigurationFormat.CISCO_IOS_XR;
      }
      return null;
   }

   private ConfigurationFormat checkEmpty() {
      String trimmedText = _fileText.trim();
      if (trimmedText.length() == 0) {
         return ConfigurationFormat.EMPTY;
      }
      _firstChar = trimmedText.charAt(0);
      return null;
   }

   private ConfigurationFormat checkF5() {
      Matcher configurationHostname = Pattern.compile("(?m)^tmsh .*$")
            .matcher(_fileText);
      if (configurationHostname.find()) {
         return ConfigurationFormat.F5;
      }
      return null;
   }

   private ConfigurationFormat checkFlatVyos() {
      if (_fileText.contains("set system config-management commit-revisions")) {
         return ConfigurationFormat.FLAT_VYOS;
      }
      return null;
   }

   private ConfigurationFormat checkIpTables() {
      if (_fileText.contains("INPUT") && _fileText.contains("OUTPUT")
            && _fileText.contains("FORWARD")) {
         return ConfigurationFormat.IPTABLES;
      }
      return null;
   }

   private ConfigurationFormat checkJuniper() {
      if (_notJuniper) {
         return null;
      }
      Matcher setMatcher = Pattern.compile("(?m)^set ").matcher(_fileText);
      Matcher flattenedJuniperMatcher = Pattern
            .compile(Pattern.quote(BATFISH_FLATTENED_JUNIPER_HEADER))
            .matcher(_fileText);
      Matcher flatJuniperHostnameDeclarationMatcher = Pattern
            .compile("(?m)^set (groups [^ ][^ ]* )?system host-name ")
            .matcher(_fileText);
      Matcher juniperAclMatcher = Pattern.compile("(?m)^firewall *\\{")
            .matcher(_fileText);
      Matcher juniperPolicyOptionsMatcher = Pattern
            .compile("(?m)^policy-options *\\{").matcher(_fileText);
      Matcher juniperSnmpMatcher = Pattern.compile("(?m)^snmp *\\{")
            .matcher(_fileText);
      if (_fileText.contains("set hostname")) {
         return ConfigurationFormat.JUNIPER_SWITCH;
      }
      else if (flattenedJuniperMatcher.find(0)
            || flatJuniperHostnameDeclarationMatcher.find(0)
            || (_fileText.contains("apply-groups") && setMatcher.find(0))) {
         return ConfigurationFormat.FLAT_JUNIPER;
      }
      else if (_firstChar == '#'
            || (_fileText.contains("version") && _fileText.contains("system")
                  && _fileText.contains("{") && _fileText.contains("}")
                  && _fileText.contains("host-name")
                  && _fileText.contains("interfaces"))
            || juniperAclMatcher.find() || juniperPolicyOptionsMatcher.find()
            || juniperSnmpMatcher.find()) {
         return ConfigurationFormat.JUNIPER;
      }
      return null;
   }

   private ConfigurationFormat checkMrv() {
      if (_fileText.contains("System.SystemName")) {
         return ConfigurationFormat.MRV;
      }
      return null;
   }

   private ConfigurationFormat checkMrvCommands() {
      Matcher configurationHostname = Pattern
            .compile("(?m)^configuration hostname .*$").matcher(_fileText);
      if (configurationHostname.find()) {
         return ConfigurationFormat.MRV_COMMANDS;
      }
      return null;
   }

   private ConfigurationFormat checkMss() {
      Matcher mssMatcher = Pattern.compile("(?m)^set system name")
            .matcher(_fileText);
      if (mssMatcher.find()) {
         return ConfigurationFormat.MSS;
      }
      return null;
   }

   private ConfigurationFormat checkRancid() {
      Matcher rancidCisco = Pattern.compile("(?m)^!RANCID-CONTENT-TYPE: cisco$")
            .matcher(_fileText);
      Matcher rancidCiscoNx = Pattern
            .compile("(?m)^!RANCID-CONTENT-TYPE: cisco-nx$").matcher(_fileText);
      Matcher rancidForce10 = Pattern
            .compile("(?m)^!RANCID-CONTENT-TYPE: force10$").matcher(_fileText);
      Matcher rancidFoundry = Pattern
            .compile("(?m)^!RANCID-CONTENT-TYPE: foundry$").matcher(_fileText);
      Matcher rancidJuniper = Pattern
            .compile("(?m)^!RANCID-CONTENT-TYPE: juniper$").matcher(_fileText);
      Matcher rancidMrv = Pattern.compile("(?m)^!RANCID-CONTENT-TYPE: mrv$")
            .matcher(_fileText);
      if (rancidCisco.find()) {
         return checkCisco(); // unfortunately, old RANCID cannot distinguish
                              // subtypes
      }
      else if (rancidCiscoNx.find()) {
         return ConfigurationFormat.CISCO_NX;
      }
      else if (rancidForce10.find()) {
         return ConfigurationFormat.FORCE10;
      }
      else if (rancidFoundry.find()) {
         return ConfigurationFormat.FOUNDRY;
      }
      else if (rancidJuniper.find()) {
         return checkJuniper();
      }
      else if (rancidMrv.find()) {
         return ConfigurationFormat.MRV;
      }
      return null;
   }

   private ConfigurationFormat checkVxWorks() {
      if (_firstChar == '!' && _fileText.contains("set prompt")) {
         return ConfigurationFormat.VXWORKS;
      }
      return null;
   }

   private ConfigurationFormat checkVyos() {
      if (_fileText.contains("system") && _fileText.contains("{")
            && _fileText.contains("}")
            && _fileText.contains("config-management")
            && _fileText.contains("commit-revisions")) {
         return ConfigurationFormat.VYOS;
      }
      return null;
   }

   private ConfigurationFormat identifyConfigurationFormat() {
      ConfigurationFormat format;
      format = checkEmpty();
      if (format != null) {
         return format;
      }
      format = checkRancid();
      if (format != null) {
         return format;
      }
      blacklist();
      format = checkF5();
      if (format != null) {
         return format;
      }
      format = checkCiscoXr();
      if (format != null) {
         return format;
      }
      format = checkFlatVyos();
      if (format != null) {
         return format;
      }
      format = checkIpTables();
      if (format != null) {
         return format;
      }
      format = checkMrv();
      if (format != null) {
         return format;
      }
      format = checkMrvCommands();
      if (format != null) {
         return format;
      }
      format = checkVyos();
      if (format != null) {
         return format;
      }
      format = checkArista();
      if (format != null) {
         return format;
      }
      format = checkBlade();
      if (format != null) {
         return format;
      }
      format = checkVxWorks();
      if (format != null) {
         return format;
      }
      format = checkJuniper();
      if (format != null) {
         return format;
      }
      format = checkAlcatelAos();
      if (format != null) {
         return format;
      }
      format = checkMss();
      if (format != null) {
         return format;
      }
      format = checkCisco();
      if (format != null) {
         return format;
      }
      return ConfigurationFormat.UNKNOWN;
   }

}
