package org.batfish.grammar;

import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConfigurationFormat;

public final class VendorConfigurationFormatDetector {

  public static final String BATFISH_FLATTENED_JUNIPER_HEADER =
      "####BATFISH FLATTENED JUNIPER CONFIG####\n";

  public static final String BATFISH_FLATTENED_VYOS_HEADER =
      "####BATFISH FLATTENED VYOS CONFIG####\n";

  public static ConfigurationFormat identifyConfigurationFormat(String fileText) {
    return new VendorConfigurationFormatDetector(fileText).identifyConfigurationFormat();
  }

  private static final Pattern BANNER_PATTERN = Pattern.compile("(?m)^banner ");
  private static final Pattern ALCATEL_AOS_PATTERN = Pattern.compile("(?m)^system name");
  private static final Pattern ARISTA_PATTERN = Pattern.compile("(?m)^.*boot system flash.*\\.swi");
  private static final Pattern BLADE_NETWORK_PATTERN = Pattern.compile("(?m)^switch-type");
  private static final Pattern CADANT_NETWORK_PATTERN = Pattern.compile("(?m)^shelfname");
  private static final Pattern F5_HOSTNAME_PATTERN = Pattern.compile("(?m)^tmsh .*$");
  private static final Pattern METAMAKO_MOS_PATTERN =
      Pattern.compile("(?m)^! device: [^\\n]+ MOS-\\d+\\.\\d+\\.\\d+\\)$");
  private static final Pattern MRV_HOSTNAME_PATTERN =
      Pattern.compile("(?m)^configuration hostname .*$");
  private static final Pattern MSS_PATTERN = Pattern.compile("(?m)^set system name");
  private static final Pattern RANCID_CISCO_NX_PATTERN =
      Pattern.compile("(?m)^!RANCID-CONTENT-TYPE: cisco-nx$");
  private static final Pattern RANCID_CISCO_PATTERN =
      Pattern.compile("(?m)^!RANCID-CONTENT-TYPE: cisco$");
  private static final Pattern RANCID_FORCE_10_PATTERN =
      Pattern.compile("(?m)^!RANCID-CONTENT-TYPE: force10$");
  private static final Pattern RANCID_FOUNDRY_PATTERN =
      Pattern.compile("(?m)^!RANCID-CONTENT-TYPE: foundry$");
  private static final Pattern RANCID_JUNIPER_PATTERN =
      Pattern.compile("(?m)^!RANCID-CONTENT-TYPE: juniper$");
  private static final Pattern RANCID_MRV_PATTERN =
      Pattern.compile("(?m)^!RANCID-CONTENT-TYPE: mrv$");
  private static final Pattern RANCID_PALO_ALTO_PATTERN =
      Pattern.compile("(?m)^!RANCID-CONTENT-TYPE: paloalto");

  // checkCisco patterns
  private static final Pattern ASA_VERSION_LINE_PATTERN = Pattern.compile("(?m)(^ASA Version.*$)");
  private static final Pattern CISCO_LIKE_PATTERN =
      Pattern.compile("(?m)(^boot system flash.*$)|(^interface .*$)");
  private static final Pattern CISCO_STYLE_ACL_PATTERN =
      Pattern.compile("(?m)(^(ip )?access-list.*$)");
  private static final Pattern NEXUS_COMMIT_LINE_PATTERN = Pattern.compile("(?m)^ *commit *$");
  private static final Pattern NEXUS_FEATURE_LINE_PATTERN =
      Pattern.compile("(?m)^\\s*(no\\s*)?feature\\s+[^\\s+].*$");
  private static final Pattern NEXUS_BOOTFLASH_PATTERN = Pattern.compile("bootflash:(n\\d+|nxos)");

  // checkJuniper patterns
  private static final Pattern FLAT_JUNIPER_HOSTNAME_DECLARATION_PATTERN =
      Pattern.compile("(?m)^set (groups [^ ][^ ]* )?system host-name ");
  private static final Pattern FLATTENED_JUNIPER_PATTERN =
      Pattern.compile(Pattern.quote(BATFISH_FLATTENED_JUNIPER_HEADER));
  private static final Pattern JUNIPER_ACL_PATTERN = Pattern.compile("(?m)^firewall *\\{");
  private static final Pattern JUNIPER_POLICY_OPTIONS_PATTERN =
      Pattern.compile("(?m)^policy-options *\\{");
  private static final Pattern JUNIPER_SNMP_PATTERN = Pattern.compile("(?m)^snmp *\\{");
  private static final Pattern SET_PATTERN = Pattern.compile("(?m)^set ");

  // checkPaloAlto patterns
  private static final Pattern PALO_ALTO_PANORAMA_PATTERN =
      Pattern.compile("(?m)(send-to-panorama|panorama-server)");

  private String _fileText;

  private char _firstChar;

  private boolean _notJuniper;

  private VendorConfigurationFormatDetector(String fileText) {
    _fileText = fileText;
  }

  private boolean fileTextMatches(Pattern pattern) {
    return pattern.matcher(_fileText).find();
  }

  private void configureHeuristicBlacklist() {
    if (fileTextMatches(BANNER_PATTERN)) {
      _notJuniper = true;
    }
  }

  @Nullable
  private ConfigurationFormat checkAlcatelAos() {
    if (fileTextMatches(ALCATEL_AOS_PATTERN)) {
      return ConfigurationFormat.ALCATEL_AOS;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkArista() {
    if (fileTextMatches(ARISTA_PATTERN)) {
      return ConfigurationFormat.ARISTA;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkBlade() {
    if (fileTextMatches(BLADE_NETWORK_PATTERN)) {
      return ConfigurationFormat.BLADENETWORK;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkCadant() {
    if (fileTextMatches(CADANT_NETWORK_PATTERN)) {
      return ConfigurationFormat.CADANT;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkCisco() {
    if (fileTextMatches(ASA_VERSION_LINE_PATTERN)) {
      return ConfigurationFormat.CISCO_ASA;
    }
    if (checkCiscoXr() == ConfigurationFormat.CISCO_IOS_XR) {
      return ConfigurationFormat.CISCO_IOS_XR;
    }
    if (fileTextMatches(NEXUS_FEATURE_LINE_PATTERN) || fileTextMatches(NEXUS_BOOTFLASH_PATTERN)) {
      return ConfigurationFormat.CISCO_NX;
    }
    if (fileTextMatches(CISCO_LIKE_PATTERN)
        || _firstChar == '!'
        || fileTextMatches(CISCO_STYLE_ACL_PATTERN)) {
      return ConfigurationFormat.CISCO_IOS;
    } else if (fileTextMatches(NEXUS_COMMIT_LINE_PATTERN)) {
      return ConfigurationFormat.CISCO_NX;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkCiscoXr() {
    if (_fileText.contains("IOS XR")) {
      return ConfigurationFormat.CISCO_IOS_XR;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkEmpty() {
    String trimmedText = _fileText.trim();
    if (trimmedText.length() == 0) {
      return ConfigurationFormat.EMPTY;
    }
    _firstChar = trimmedText.charAt(0);
    return null;
  }

  @Nullable
  private ConfigurationFormat checkF5() {
    if (fileTextMatches(F5_HOSTNAME_PATTERN)) {
      return ConfigurationFormat.F5;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkFlatVyos() {
    if (_fileText.contains("set system config-management commit-revisions")) {
      return ConfigurationFormat.FLAT_VYOS;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkIpTables() {
    if (_fileText.contains("INPUT")
        && _fileText.contains("OUTPUT")
        && _fileText.contains("FORWARD")) {
      return ConfigurationFormat.IPTABLES;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkJuniper() {
    if (_notJuniper) {
      return null;
    } else if (_fileText.contains("set hostname")) {
      return ConfigurationFormat.JUNIPER_SWITCH;
    } else if (FLATTENED_JUNIPER_PATTERN.matcher(_fileText).find(0)
        || FLAT_JUNIPER_HOSTNAME_DECLARATION_PATTERN.matcher(_fileText).find(0)
        || (_fileText.contains("apply-groups") && SET_PATTERN.matcher(_fileText).find(0))) {
      return ConfigurationFormat.FLAT_JUNIPER;
    } else if (_firstChar == '#'
        || (_fileText.contains("version")
            && _fileText.contains("system")
            && _fileText.contains("{")
            && _fileText.contains("}")
            && _fileText.contains("host-name")
            && _fileText.contains("interfaces"))
        || fileTextMatches(JUNIPER_ACL_PATTERN)
        || fileTextMatches(JUNIPER_POLICY_OPTIONS_PATTERN)
        || fileTextMatches(JUNIPER_SNMP_PATTERN)) {
      return ConfigurationFormat.JUNIPER;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkMetamako() {
    if (_fileText.contains("application metamux")
        || _fileText.contains("application metawatch")
        || fileTextMatches(METAMAKO_MOS_PATTERN)) {
      return ConfigurationFormat.METAMAKO;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkMrv() {
    if (_fileText.contains("System.SystemName")) {
      return ConfigurationFormat.MRV;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkMrvCommands() {
    if (fileTextMatches(MRV_HOSTNAME_PATTERN)) {
      return ConfigurationFormat.MRV_COMMANDS;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkPaloAlto() {
    if (fileTextMatches(PALO_ALTO_PANORAMA_PATTERN)) {
      return ConfigurationFormat.PALO_ALTO;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkMss() {
    if (fileTextMatches(MSS_PATTERN)) {
      return ConfigurationFormat.MSS;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkRancid() {
    if (fileTextMatches(RANCID_CISCO_PATTERN)) {
      return checkCisco(); // unfortunately, old RANCID cannot distinguish
      // subtypes
    } else if (fileTextMatches(RANCID_CISCO_NX_PATTERN)) {
      return ConfigurationFormat.CISCO_NX;
    } else if (fileTextMatches(RANCID_FORCE_10_PATTERN)) {
      return ConfigurationFormat.FORCE10;
    } else if (fileTextMatches(RANCID_FOUNDRY_PATTERN)) {
      return ConfigurationFormat.FOUNDRY;
    } else if (fileTextMatches(RANCID_JUNIPER_PATTERN)) {
      return checkJuniper();
    } else if (fileTextMatches(RANCID_MRV_PATTERN)) {
      return ConfigurationFormat.MRV;
    } else if (fileTextMatches(RANCID_PALO_ALTO_PATTERN)) {
      return ConfigurationFormat.PALO_ALTO;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkVxWorks() {
    if (_firstChar == '!' && _fileText.contains("set prompt")) {
      return ConfigurationFormat.VXWORKS;
    }
    return null;
  }

  @Nullable
  private ConfigurationFormat checkVyos() {
    if (_fileText.contains("system")
        && _fileText.contains("{")
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

    // Heuristics are somewhat brittle. This function adds information about which configuration
    // formats we know this file does not match.
    configureHeuristicBlacklist();

    format = checkCadant();
    if (format != null) {
      return format;
    }
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
    format = checkMetamako();
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
    format = checkPaloAlto();
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
