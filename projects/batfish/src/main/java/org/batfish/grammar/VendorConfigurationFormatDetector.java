package org.batfish.grammar;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.datamodel.ConfigurationFormat;

public final class VendorConfigurationFormatDetector {
  private static final Logger LOGGER =
      LogManager.getLogger(VendorConfigurationFormatDetector.class);

  public static final String BATFISH_FLATTENED_JUNIPER_HEADER =
      "####BATFISH FLATTENED JUNIPER CONFIG####\n";

  public static final String BATFISH_FLATTENED_PALO_ALTO_HEADER =
      "####BATFISH FLATTENED PALO ALTO CONFIG####\n";

  public static final String BATFISH_FLATTENED_VYOS_HEADER =
      "####BATFISH FLATTENED VYOS CONFIG####\n";

  public static ConfigurationFormat identifyConfigurationFormat(String fileText) {
    return new VendorConfigurationFormatDetector(fileText).identifyConfigurationFormat();
  }

  private static final Tell BATFISH_CONFIG_FORMAT_PATTERN =
      Tell.of("(?m)^[!#] *BATFISH[-_]FORMAT *: *([a-zA-Z0-9_-]+)", "BATFISH");

  private static final Tell BANNER_PATTERN = Tell.of("(?m)^banner ", "banner ");
  private static final Tell A10_PATTERN =
      Tell.of(
          "(?m)version \\d+.\\d+.\\d+[\\w-.]*, build \\d+"
              + " \\([A-Za-z]+-\\d{1,2}-\\d{4},\\d\\d:\\d\\d\\)",
          ", build ");
  private static final Tell ALCATEL_AOS_PATTERN = Tell.of("(?m)^system name", "system name");
  private static final Tell ARUBAOS_PATTERN = Tell.of("(?m)^netservice.*$", "netservice");
  private static final Tell BLADE_NETWORK_PATTERN = Tell.of("(?m)^switch-type", "switch-type");
  private static final Tell CADANT_NETWORK_PATTERN = Tell.of("(?m)^shelfname", "shelfname");
  private static final Tell CHECK_POINT_GATEWAY_PATTERN =
      Tell.of("(?m)^# Configuration of [\\w-]+\\R+# Language version: ", "# Language version: ");
  private static final Tell CUMULUS_CONCATENATED_PATTERN =
      Tell.of(
          "(?m)^# This file describes the network interfaces",
          "# This file describes the network interfaces");
  private static final Tell CUMULUS_NCLU_PATTERN = Tell.of("(?m)^net del all$", "net del all");
  private static final Tell F5_HOSTNAME_PATTERN = Tell.of("(?m)^tmsh .*$", "tmsh ");
  private static final Tell F5_BIGIP_STRUCTURED_HEADER_PATTERN =
      Tell.of("(?m)^#TMSH-VERSION: .*$", "#TMSH-VERSION: ");
  private static final Tell F5_BIGIP_STRUCTURED_LTM_GLOBAL_SETTINGS_PATTERN =
      Tell.of("(?m)^ltm\\s+global-settings\\s*(general|rule)\\s*\\{.*$", "global-settings");
  private static final Tell F5_BIGIP_STRUCTURED_SYS_GLOBAL_SETTINGS_PATTERN =
      Tell.of("(?m)^sys\\s+global-settings\\s*\\{.*$", "global-settings");
  private static final Tell METAMAKO_MOS_PATTERN =
      Tell.of("(?m)^! device: [^\\n]+ MOS-\\d+\\.\\d+\\.\\d+\\)$", " MOS-");
  private static final Tell MRV_HOSTNAME_PATTERN =
      Tell.of("(?m)^configuration hostname .*$", "configuration hostname ");
  private static final Tell MSS_PATTERN = Tell.of("(?m)^set system name", "set system name");

  private static final Tell FASTPATH_PATTERN =
      Tell.of(
          "(?m)^(?:!System Software Version"
              + "|!Current SNTP Synchronized Time:"
              + "|no 1583compatibility"
              + "|vlan participation "
              + "|serviceport )",
          "!System Software Version",
          "!Current SNTP Synchronized Time:",
          "no 1583compatibility",
          "vlan participation ",
          "serviceport ");

  // checkSros patterns (Nokia SR-OS / SR-SIM, MD-CLI). SR-OS configs are emitted by
  // `admin show configuration` (brace/hierarchical, rooted at `configure {`) and can also
  // be supplied in the absolute-path flat form (`/configure ...` lines, the Junos-`set`
  // analog). The TiMOS banner and the "Configuration format version" header are
  // SR-OS-specific tells that brace form alone (which collides with Juniper) is not.
  private static final Tell SROS_TIMOS_PATTERN = Tell.of("(?m)^# *TiMOS-", "TiMOS-");
  private static final Tell SROS_CONFIG_VERSION_PATTERN =
      Tell.of(
          "(?m)^# *Configuration format version \\d+\\.\\d+ revision \\d+",
          "Configuration format version ");
  private static final Tell SROS_FLAT_CONFIGURE_PATTERN =
      Tell.of("(?m)^\\s*/configure ", "/configure ");

  private static final Tell RANCID_BASE_PATTERN =
      Tell.of("(?m)^[!#]RANCID-CONTENT-TYPE: ([a-zA-Z0-9_-]+)", "RANCID-CONTENT-TYPE: ");

  // checkCisco patterns
  private static final Tell ASA_VERSION_LINE_PATTERN =
      Tell.of("(?m)(^ASA Version.*$)", "ASA Version");
  private static final Tell CISCO_LIKE_PATTERN =
      Tell.of("(?m)(^boot system flash.*$)|(^interface .*$)", "boot system flash", "interface ");
  private static final Tell CISCO_STYLE_ACL_PATTERN =
      Tell.of("(?m)(^(ip )?access-list.*$)", "access-list");
  private static final Tell NEXUS_COMMIT_LINE_PATTERN = Tell.of("(?m)^ *commit *$", "commit");
  private static final Tell NEXUS_FEATURE_LINE_PATTERN =
      Tell.of("(?m)^\\s*(no\\s*)?feature\\s+[^\\s+].*$", "feature");
  private static final Tell NEXUS_BOOT_NXOS_PATTERN = Tell.of("boot nxos", "boot nxos");
  private static final Tell NEXUS_BOOTFLASH_PATTERN =
      Tell.of("bootflash:(n\\d+|/?nxos)", "bootflash:");

  // checkJuniper patterns
  private static final Tell FLAT_JUNIPER_HOSTNAME_DECLARATION_PATTERN =
      Tell.of("(?m)^set (groups [^ ][^ ]* )?system host-name ", "system host-name ");
  private static final Tell FLATTENED_JUNIPER_PATTERN =
      Tell.of(Pattern.quote(BATFISH_FLATTENED_JUNIPER_HEADER), BATFISH_FLATTENED_JUNIPER_HEADER);
  // Juniper-specific keywords and stanzas (stanzas match "keyword {" or "keyword{")
  private static final Tell JUNIPER_PATTERN =
      Tell.of(
          "(firewall|policy-options|snmp|routing-instances|groups) *\\{|apply-groups|replace:",
          "{",
          "apply-groups",
          "replace:");

  // checkPaloAlto patterns
  private static final Tell FLAT_PALO_ALTO_PATTERN =
      Tell.of(
          Pattern.quote(BATFISH_FLATTENED_PALO_ALTO_HEADER), BATFISH_FLATTENED_PALO_ALTO_HEADER);
  private static final Tell PALO_ALTO_PANORAMA_DEVICECONFIG_PATTERN =
      Tell.of(
          "(?m)(send-to-panorama|panorama-server|deviceconfig)",
          "send-to-panorama",
          "panorama-server",
          "deviceconfig");
  // open brace not likely to be opening a string literal of a JSON object
  private static final Tell PALO_ALTO_NESTED_PATTERN = Tell.of("(?m)[^\"']\\{", "{");

  private String _fileText;

  private char _firstChar;

  private boolean _notJuniper;

  private VendorConfigurationFormatDetector(String fileText) {
    _fileText = fileText;
  }

  /**
   * A regular expression together with literals of which every match contains at least one. A
   * heuristic usually does not match, and {@link String#contains} rules that out far faster than a
   * regex scan of the whole file does, so the regex runs only when a literal is present.
   */
  private static final class Tell {
    private final @Nonnull Pattern _pattern;
    private final @Nonnull String[] _literals;

    private Tell(Pattern pattern, String... literals) {
      checkArgument(literals.length > 0, "A literal is needed for %s", pattern);
      _pattern = pattern;
      _literals = literals;
    }

    /** {@code literals}: every match of {@code regex} contains at least one of them. */
    static Tell of(String regex, String... literals) {
      return new Tell(Pattern.compile(regex), literals);
    }

    boolean matches(String text) {
      for (String literal : _literals) {
        if (text.contains(literal)) {
          return _pattern.matcher(text).find();
        }
      }
      return false;
    }

    /**
     * A matcher positioned at the first match in {@code text}, or {@code null} if there is none.
     */
    @Nullable
    Matcher find(String text) {
      if (!matches(text)) {
        return null;
      }
      Matcher m = _pattern.matcher(text);
      checkState(m.find());
      return m;
    }

    @Override
    public String toString() {
      return _pattern.toString();
    }
  }

  private boolean fileTextMatches(Tell tell) {
    return tell.matches(_fileText);
  }

  private void configureHeuristicBlacklist() {
    if (fileTextMatches(BANNER_PATTERN)) {
      _notJuniper = true;
    }
  }

  private @Nullable ConfigurationFormat checkA10() {
    if (fileTextMatches(A10_PATTERN)) {
      return ConfigurationFormat.A10_ACOS;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkAlcatelAos() {
    if (fileTextMatches(ALCATEL_AOS_PATTERN)) {
      return ConfigurationFormat.ALCATEL_AOS;
    }
    return null;
  }

  private static final String ARISTA_EOS_LINE_REGEX = "^! device: .*\\(.*EOS-\\d";
  private static final String ARISTA_FLASH_REGEX = "^.*boot system flash.*\\.swi";
  private static final String ARISTA_TELLS_REGEX = "^ip (ext)?community-list regexp";
  private static final Tell ARISTA_PATTERN =
      Tell.of(
          "(?m)("
              + ARISTA_EOS_LINE_REGEX
              + "|"
              + ARISTA_FLASH_REGEX
              + "|"
              + ARISTA_TELLS_REGEX
              + ")",
          "EOS-",
          ".swi",
          "community-list regexp");

  private @Nullable ConfigurationFormat checkArista() {
    if (fileTextMatches(ARISTA_PATTERN)) {
      return ConfigurationFormat.ARISTA;
    }

    return null;
  }

  private @Nullable ConfigurationFormat checkArubaOS() {
    if (fileTextMatches(ARUBAOS_PATTERN)) {
      return ConfigurationFormat.ARUBAOS;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkBlade() {
    if (fileTextMatches(BLADE_NETWORK_PATTERN)) {
      return ConfigurationFormat.BLADENETWORK;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkCadant() {
    if (fileTextMatches(CADANT_NETWORK_PATTERN)) {
      return ConfigurationFormat.CADANT;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkCheckPoint() {
    if (fileTextMatches(CHECK_POINT_GATEWAY_PATTERN)) {
      return ConfigurationFormat.CHECK_POINT_GATEWAY;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkFastPath() {
    if (fileTextMatches(FASTPATH_PATTERN)) {
      return ConfigurationFormat.FASTPATH;
    }
    return null;
  }

  /** Assuming Cisco device, try to find things that indicate IOS-XR. */
  private static final Tell XR_QUALIFIERS =
      Tell.of(
          "(?m)^\\s*(interface Bundle-Ether|end-policy\\b|end-set\\b|ipv4 access-list\\b)",
          "interface Bundle-Ether",
          "end-policy",
          "end-set",
          "ipv4 access-list");

  private @Nullable ConfigurationFormat checkCisco() {
    if (fileTextMatches(ASA_VERSION_LINE_PATTERN)) {
      return ConfigurationFormat.CISCO_ASA;
    } else if (fileTextMatches(XR_QUALIFIERS)) {
      return ConfigurationFormat.CISCO_IOS_XR;
    }
    if (fileTextMatches(NEXUS_BOOT_NXOS_PATTERN)
        || fileTextMatches(NEXUS_FEATURE_LINE_PATTERN)
        || fileTextMatches(NEXUS_BOOTFLASH_PATTERN)) {
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

  private @Nullable ConfigurationFormat checkCiscoXr() {
    if (_fileText.contains("IOS XR")) {
      return ConfigurationFormat.CISCO_IOS_XR;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkCumulusConcatenated() {
    if (fileTextMatches(CUMULUS_CONCATENATED_PATTERN)) {
      return ConfigurationFormat.CUMULUS_CONCATENATED;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkCumulusNclu() {
    if (fileTextMatches(CUMULUS_NCLU_PATTERN)) {
      return ConfigurationFormat.CUMULUS_NCLU;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkEmpty() {
    String trimmedText = _fileText.trim();
    if (trimmedText.isEmpty()) {
      return ConfigurationFormat.EMPTY;
    }
    _firstChar = trimmedText.charAt(0);
    return null;
  }

  private @Nullable ConfigurationFormat checkF5() {
    if (fileTextMatches(F5_BIGIP_STRUCTURED_SYS_GLOBAL_SETTINGS_PATTERN)
        && (fileTextMatches(F5_BIGIP_STRUCTURED_HEADER_PATTERN)
            || fileTextMatches(F5_BIGIP_STRUCTURED_LTM_GLOBAL_SETTINGS_PATTERN))) {
      // "sys global-settings {" is a little generic to be the determiner. Use that plus the
      // presence of either TMSH or "ltm global-settings {" to detect F5.
      return ConfigurationFormat.F5_BIGIP_STRUCTURED;
    }
    if (fileTextMatches(F5_HOSTNAME_PATTERN)) {
      return ConfigurationFormat.F5;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkFlatVyos() {
    if (_fileText.contains("set system config-management commit-revisions")) {
      return ConfigurationFormat.FLAT_VYOS;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkIpTables() {
    if (_fileText.contains("INPUT")
        && _fileText.contains("OUTPUT")
        && _fileText.contains("FORWARD")) {
      return ConfigurationFormat.IPTABLES;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkJuniper(boolean preMatch) {
    if (_notJuniper) {
      return null;
    } else if (fileTextMatches(FLATTENED_JUNIPER_PATTERN)) {
      return ConfigurationFormat.FLAT_JUNIPER;
    } else if (_fileText.contains("set hostname")) {
      return ConfigurationFormat.JUNIPER_SWITCH;
    }

    // Decide whether we believe this is a Juniper file.
    boolean isJuniper =
        preMatch
            || fileTextMatches(FLAT_JUNIPER_HOSTNAME_DECLARATION_PATTERN)
            || fileTextMatches(JUNIPER_PATTERN)
            || _fileText.contains("system")
                && _fileText.contains("{")
                && _fileText.contains("}")
                && _fileText.contains("host-name")
                && _fileText.contains("interfaces");
    if (isJuniper) {
      return (_fileText.contains("{"))
          ? ConfigurationFormat.JUNIPER
          : ConfigurationFormat.FLAT_JUNIPER;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkMetamako() {
    if (_fileText.contains("application metamux")
        || _fileText.contains("application metawatch")
        || fileTextMatches(METAMAKO_MOS_PATTERN)) {
      return ConfigurationFormat.METAMAKO;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkMrv() {
    if (_fileText.contains("System.SystemName")) {
      return ConfigurationFormat.MRV;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkMrvCommands() {
    if (fileTextMatches(MRV_HOSTNAME_PATTERN)) {
      return ConfigurationFormat.MRV_COMMANDS;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkMss() {
    if (fileTextMatches(MSS_PATTERN)) {
      return ConfigurationFormat.MSS;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkPaloAlto(boolean preMatch) {
    if (fileTextMatches(FLAT_PALO_ALTO_PATTERN)) {
      return ConfigurationFormat.PALO_ALTO;
    } else if (preMatch || fileTextMatches(PALO_ALTO_PANORAMA_DEVICECONFIG_PATTERN)) {
      if (fileTextMatches(PALO_ALTO_NESTED_PATTERN)) {
        return ConfigurationFormat.PALO_ALTO_NESTED;
      }
      return ConfigurationFormat.PALO_ALTO;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkBatfish() {
    Matcher m = BATFISH_CONFIG_FORMAT_PATTERN.find(_fileText);
    if (m == null) {
      return null;
    }
    String format = m.group(1);
    try {
      return ConfigurationFormat.valueOf(format.toUpperCase());
    } catch (IllegalArgumentException e) {
      LOGGER.warn("Unknown Batfish configuration format: {}", format);
      // This is not a known enum value, force unknown.
      return ConfigurationFormat.UNKNOWN;
    }
  }

  private @Nullable ConfigurationFormat checkRancid() {
    Matcher m = RANCID_BASE_PATTERN.find(_fileText);
    if (m == null) {
      return null;
    }
    // Based on types and aliases defined in
    // https://github.com/haussli/rancid/blob/dc27e9bbb9972e475d66b4abbea981b7da6eeb23/etc/rancid.types.base
    switch (m.group(1)) {
      case "a10":
        return ConfigurationFormat.A10_ACOS;
      case "arista":
        return ConfigurationFormat.ARISTA;
      case "cisco":
        // unfortunately, old RANCID cannot distinguish subtypes and also often does other vendors
        // like Arista
        return null;
      case "ios":
      case "cisco-ncs": // cisco NCS running ios-xe
        return ConfigurationFormat.CISCO_IOS;
      case "ios-nx":
      case "cisco-nx":
        return ConfigurationFormat.CISCO_NX;
      case "ios-xr":
      case "cisco-xr":
      case "ios-xr7":
      case "cisco-xr7":
      case "ios-exr":
      case "cisco-exr":
        return ConfigurationFormat.CISCO_IOS_XR;
      case "dnos9":
      case "dnos10":
      case "force10":
        return ConfigurationFormat.FORCE10;
      case "f5": // <=v10
      case "bigip": // v11
      case "bigip13": // v13
        return ConfigurationFormat.F5_BIGIP_STRUCTURED; // v13
      case "fortigate":
      case "fortigate-full":
        return ConfigurationFormat.FORTIOS;
      case "foundry":
        return ConfigurationFormat.FOUNDRY;
      case "ibmbnt": // legacy; not present in latest RANCID file
        return ConfigurationFormat.IBM_BNT;
      case "juniper":
      case "juniper-srx":
      case "junos":
        return checkJuniper(true);
      case "mrv": // legacy; not present in latest RANCID file
        return ConfigurationFormat.MRV;
      case "paloalto":
        return checkPaloAlto(true);
      case "sros":
      case "sros-md":
        return ConfigurationFormat.NOKIA_SROS;
      case "fastpath":
        return ConfigurationFormat.FASTPATH;
      case "agm":
      case "alteon":
      case "arbor":
      case "arcos":
      case "axis":
      case "avocent":
      case "baynet":
      case "brocade":
      case "ciena-ws":
      case "cat5":
      case "cisco-sb":
      case "cisco-wlc4":
      case "cisco-wlc5":
      case "cisco-wlc8":
      case "css":
      case "dell":
      case "edgemax":
      case "edgerouter":
      case "enterasys":
      case "erx":
      case "extreme":
      case "ezt3":
      case "frr": // stand-alone FRR (not cumulus, sonic)
      case "fss2":
      case "fxos":
      case "hitachi":
      case "hp":
      case "ios-sb":
      case "junos-evo":
      case "microtik":
      case "mrtd":
      case "netopia":
      case "netscreen":
      case "paloaltoxml":
      case "redback":
      case "riverbed":
      case "riverstone":
      case "routeros":
      case "smc":
      case "vrp":
      case "xirrus":
      case "zebra":
        return ConfigurationFormat.UNSUPPORTED;
      default:
        // We don't recognize the RANCID string, assert this config is unknown.
        return ConfigurationFormat.UNKNOWN;
    }
  }

  private @Nullable ConfigurationFormat checkSros() {
    if (fileTextMatches(SROS_TIMOS_PATTERN)
        || fileTextMatches(SROS_CONFIG_VERSION_PATTERN)
        || fileTextMatches(SROS_FLAT_CONFIGURE_PATTERN)) {
      return ConfigurationFormat.NOKIA_SROS;
    }
    return null;
  }

  private static final Tell RUCKUS_ICX_MODULE_PATTERN = Tell.of("(?m)module \\d+ icx", " icx");

  private @Nullable ConfigurationFormat checkRuckusIcx() {
    if (fileTextMatches(RUCKUS_ICX_MODULE_PATTERN)) {
      return ConfigurationFormat.RUCKUS_ICX;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkVxWorks() {
    if (_firstChar == '!' && _fileText.contains("set prompt")) {
      return ConfigurationFormat.VXWORKS;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkVyos() {
    if (_fileText.contains("system")
        && _fileText.contains("{")
        && _fileText.contains("}")
        && _fileText.contains("config-management")
        && _fileText.contains("commit-revisions")) {
      return ConfigurationFormat.VYOS;
    }
    return null;
  }

  private @Nullable ConfigurationFormat checkFortios() {
    if (_fileText.contains("config system global")) {
      return ConfigurationFormat.FORTIOS;
    }
    return null;
  }

  private ConfigurationFormat identifyConfigurationFormat() {
    ConfigurationFormat format = checkEmpty();
    format = (format == null) ? checkBatfish() : format;
    format = (format == null) ? checkRancid() : format;

    // Heuristics are somewhat brittle. This function adds information about which configuration
    // formats we know this file does not match.
    if (format == null) {
      configureHeuristicBlacklist();
    }

    format = (format == null) ? checkA10() : format;
    // SR-OS must run before checkJuniper/checkCisco: an SR-OS brace config contains
    // tokens (e.g. `policy-options {`, `interface ...`) that those heuristics would
    // otherwise claim. checkSros keys on SR-OS-specific tells, so it is safe to run early.
    format = (format == null) ? checkSros() : format;
    format = (format == null) ? checkFastPath() : format;
    format = (format == null) ? checkCheckPoint() : format;
    format = (format == null) ? checkFortios() : format;
    format = (format == null) ? checkRuckusIcx() : format;
    format = (format == null) ? checkCadant() : format;
    format = (format == null) ? checkCumulusConcatenated() : format;
    format = (format == null) ? checkCumulusNclu() : format;
    format = (format == null) ? checkF5() : format;
    format = (format == null) ? checkCiscoXr() : format;
    format = (format == null) ? checkFlatVyos() : format;
    format = (format == null) ? checkMetamako() : format;
    format = (format == null) ? checkMrv() : format;
    format = (format == null) ? checkMrvCommands() : format;
    format = (format == null) ? checkPaloAlto(false) : format;
    format = (format == null) ? checkVyos() : format;
    format = (format == null) ? checkArista() : format;
    format = (format == null) ? checkBlade() : format;
    format = (format == null) ? checkVxWorks() : format;
    format = (format == null) ? checkJuniper(false) : format;
    format = (format == null) ? checkAlcatelAos() : format;
    format = (format == null) ? checkMss() : format;
    format = (format == null) ? checkArubaOS() : format;
    format = (format == null) ? checkCisco() : format;
    format = (format == null) ? checkIpTables() : format;
    return (format == null) ? ConfigurationFormat.UNKNOWN : format;
  }
}
