package org.batfish.grammar.cisco_nxos;

import static org.batfish.grammar.cisco_nxos.CiscoNxosControlPlaneExtractor.toType;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.vendor_family.cisco_nxos.NexusPlatform;
import org.batfish.datamodel.vendor_family.cisco_nxos.NxosMajorVersion;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Boot_kickstartContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Boot_nxosContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Boot_systemContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Cisco_nxos_configurationContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_ip_addressContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_no_switchportContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchportContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_switchportContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_vrf_memberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.No_sysds_switchportContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_interface_regularContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.S_versionContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Sysds_switchportContext;
import org.batfish.representation.cisco_nxos.CiscoNxosConfiguration;
import org.batfish.representation.cisco_nxos.CiscoNxosInterfaceType;

/**
 * Collects metadata from and sets defaults for a {@link CiscoNxosConfiguration} from a
 * corresponding parse tree.
 *
 * <p>Walks the parse tree to determine:
 *
 * <ul>
 *   <li>NX-OS major version
 *   <li>Nexus platform
 *   <li>default mode (L2 or L3) of Ethernet/port-channel interfaces
 *   <li>default shutdown status of L2 interfaces
 *   <li>default shutdown status of L3 interfaces
 * </ul>
 *
 * <p>Determines the default interface mode (L2 vs L3) by counting the number of L2- or L3- only
 * commands that appear in interfaces where switchport mode has not been explicitly configured
 * (using switchport or no switchport).
 *
 * <p>These defaults are used in {@link CiscoNxosControlPlaneExtractor} to achieve reasonable
 * behavior.
 */
@ParametersAreNonnullByDefault
public final class CiscoNxosPreprocessor extends CiscoNxosParserBaseListener {

  // NX-OS image version patterns
  private static final Pattern NEXUS_3K5K6K7K_IMAGE_MAJOR_VERSION_PATTERN =
      Pattern.compile(".*?[A-Za-z][0-9]\\.([0-9]).*");
  private static final Pattern NEXUS_9000_IMAGE_MAJOR_VERSION_PATTERN =
      Pattern.compile(".*nxos\\.([0-9]).*");
  private static final Pattern KICKSTART_MAJOR_VERSION_PATTERN =
      Pattern.compile(".*kickstart\\.([0-9]).*");

  /**
   * Infers {@code NexusPlatform} of a configuration based on explicit version string or names of
   * boot image files. Returns {@link NxosMajorVersion#UNKNOWN} if unique inference cannot be made.
   */
  public static @Nonnull NxosMajorVersion inferMajorVersion(CiscoNxosConfiguration vc) {
    String versionString = vc.getVersion();
    if (versionString != null) {
      NxosMajorVersion explicit = inferMajorVersionFromVersion(vc.getVersion());
      if (explicit != null) {
        return explicit;
      }
    }
    return Stream.of(
            vc.getBootNxosSup1(),
            vc.getBootNxosSup2(),
            vc.getBootSystemSup1(),
            vc.getBootSystemSup2(),
            vc.getBootKickstartSup1(),
            vc.getBootKickstartSup2())
        .filter(Objects::nonNull)
        .map(CiscoNxosPreprocessor::inferMajorVersionFromImage)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(NxosMajorVersion.UNKNOWN);
  }

  /**
   * Infers {@code NxosMajorVersion} of a configuration based on explicit version string. Returns
   * {@code null} if unique inference cannot be made.
   */
  @VisibleForTesting
  static @Nullable NxosMajorVersion inferMajorVersionFromVersion(@Nullable String version) {
    switch (version.charAt(0)) {
      case '4':
        return NxosMajorVersion.NXOS4;
      case '5':
        return NxosMajorVersion.NXOS5;
      case '6':
        return NxosMajorVersion.NXOS6;
      case '7':
        return NxosMajorVersion.NXOS7;
      case '9':
        return NxosMajorVersion.NXOS9;
      default:
        return null;
    }
  }

  /**
   * Infers {@code NxosMajorVersion} of a configuration based on name of boot image file. Returns
   * {@code null} if unique inference cannot be made.
   */
  @VisibleForTesting
  static @Nullable NxosMajorVersion inferMajorVersionFromImage(String image) {
    // DO NOT REORDER
    return Stream.of(
            KICKSTART_MAJOR_VERSION_PATTERN,
            NEXUS_9000_IMAGE_MAJOR_VERSION_PATTERN,
            NEXUS_3K5K6K7K_IMAGE_MAJOR_VERSION_PATTERN)
        .map(
            p -> {
              Matcher m = p.matcher(image);
              if (!m.matches()) {
                return null;
              }
              return inferMajorVersionFromVersion(m.group(1));
            })
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  /**
   * Infers {@code NexusPlatform} of a configuration based on names of boot image files. Returns
   * {@link NexusPlatform#UNKNOWN} if unique inference cannot be made.
   */
  public static @Nonnull NexusPlatform inferPlatform(CiscoNxosConfiguration vc) {
    return Stream.of(
            vc.getBootNxosSup1(),
            vc.getBootNxosSup2(),
            vc.getBootSystemSup1(),
            vc.getBootSystemSup2(),
            vc.getBootKickstartSup1(),
            vc.getBootKickstartSup2())
        .filter(Objects::nonNull)
        .map(CiscoNxosPreprocessor::inferPlatformFromImage)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(NexusPlatform.UNKNOWN);
  }

  /**
   * Infers {@code NexusPlatform} of a configuration based on name of boot image file. Returns
   * {@code null} if unique inference cannot be made.
   */
  @VisibleForTesting
  static @Nullable NexusPlatform inferPlatformFromImage(String image) {
    if (image.contains("n3000")) {
      return NexusPlatform.NEXUS_3000;
    } else if (image.contains("n5000")) {
      return NexusPlatform.NEXUS_5000;
    } else if (image.contains("n6000")) {
      return NexusPlatform.NEXUS_6000;
    } else if (image.contains("n7000") || image.contains("n7700") || image.contains("titanium")) {
      return NexusPlatform.NEXUS_7000;
    } else {
      return null;
    }
  }

  /**
   * Returns {@code true} if Ethernet/port-channel interfaces should be in layer-2 mode by default,
   * or {code false} if they should be in layer-3 mode by default.
   */
  public static boolean getDefaultDefaultSwitchport(
      CiscoNxosConfiguration vc,
      NexusPlatform platform,
      NxosMajorVersion majorVersion,
      int defaultLayer2EvidenceCount,
      int defaultLayer3EvidenceCount) {
    switch (platform) {
      case NEXUS_1000V:
        // TODO: verify
        return true;
      case NEXUS_3000:
        // verified for NXOS6
        return true;
      case NEXUS_5000:
        // verified for NXOS7
        return true;
      case NEXUS_6000:
        // verified for NXOS7
        return true;
      case NEXUS_7000:
        // verified for NXOS6
        return false;
      case NEXUS_9000:
      case UNKNOWN:
        switch (majorVersion) {
          case NXOS5:
            // docs are ambiguous, but sounds like switchport is default
            // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus3548/sw/cmd_ref/503_A1/interfaces/3548_cmd_ref_if/3k_cmd_ref_if_cmds.html
            return true;
          case NXOS6:
            // TODO: add doc pointer
            return false;

          case NXOS7:
          case NXOS9:
          case UNKNOWN:
            // Assume NXOS7+ behavior arbitrarily when version is unknown.
            // Value depends on model, so just make an educated guess based on statistical analysis
            return defaultLayer2EvidenceCount > defaultLayer3EvidenceCount;

          case NXOS4:
          default:
            throw new IllegalArgumentException(
                String.format("Unsupported major version: %s", majorVersion));
        }
      default:
        throw new IllegalArgumentException(String.format("Unsupported platform: %s", platform));
    }
  }

  public static boolean getNonSwitchportDefaultShutdown(NexusPlatform platform) {
    switch (platform) {
      case NEXUS_1000V:
        // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus1000/sw/4_2_1_s_v_1_4/command/reference/n1000v_cmd_ref/n1000v_cmds_s.html
        return false;
      case NEXUS_3000:
        // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus3000/sw/command/reference/5_0_3/interfaces/3k_cmd_ref_if/3k_cmd_ref_if_cmds.html
        return false;
      case NEXUS_5000:
        // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus5000/sw/interfaces/command/cisco_nexus_5000_interfaces_command_ref/s_commands.html
        return false;
      case NEXUS_6000:
        // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus6000/sw/command/reference/interfaces/N6k_if_cmd_ref/n6k_if_cmds_s.html
        return false;
      case NEXUS_7000:
        // NO DEFAULT according to
        // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus7000/sw/interfaces/command/cisco_nexus7000_interfaces_command_ref/s_commands.html#wp2891012724
        // Since it doesn't matter, just default to false
        return false;
      case NEXUS_9000:
        // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus9000/sw/6-x/interfaces/configuration/guide/b_Cisco_Nexus_9000_Series_NX-OS_Interfaces_Configuration_Guide/b_Cisco_Nexus_9000_Series_NX-OS_Interfaces_Configuration_Guide_chapter_010.html
        return true;
      case UNKNOWN:
        // currently includes nexus 9000; nexus 3000 with NX-OS 9. In either case, shutdown by
        // default.
        // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus9000/sw/92x/interfaces/configuration/guide/b-cisco-nexus-9000-nx-os-interfaces-configuration-guide-92x/b-cisco-nexus-9000-nx-os-interfaces-configuration-guide-92x_chapter_011.html#concept_B279E7CC6BC04683BE07B09298887229
        return true;
      default:
        throw new IllegalArgumentException(String.format("Unsupported platform: %s", platform));
    }
  }

  private final @Nonnull CiscoNxosConfiguration _configuration;

  @SuppressWarnings("unused")
  private final @Nonnull CiscoNxosCombinedParser _parser;

  @SuppressWarnings("unused")
  private final @Nonnull String _text;

  @SuppressWarnings("unused")
  private final @Nonnull Warnings _w;

  private Boolean _currentInterfaceSubinterface;
  private Boolean _currentInterfaceSwitchport;
  private CiscoNxosInterfaceType _currentInterfaceType;
  private int _defaultLayer3EvidenceCount;
  private int _defaultLayer2EvidenceCount;

  // stop collecting evidence once this becomes true
  private boolean _explicitSystemDefaultSwitchport;

  public CiscoNxosPreprocessor(
      String text,
      CiscoNxosCombinedParser parser,
      Warnings warnings,
      CiscoNxosConfiguration configuration) {
    _text = text;
    _parser = parser;
    _w = warnings;
    _configuration = configuration;
  }

  @Override
  public void exitCisco_nxos_configuration(Cisco_nxos_configurationContext ctx) {
    NxosMajorVersion majorVersion = inferMajorVersion(_configuration);
    _configuration.setMajorVersion(majorVersion);
    NexusPlatform platform = inferPlatform(_configuration);
    _configuration.setPlatform(platform);
    _configuration.setNonSwitchportDefaultShutdown(getNonSwitchportDefaultShutdown(platform));
    _configuration.setSystemDefaultSwitchport(
        getDefaultDefaultSwitchport(
            _configuration,
            platform,
            majorVersion,
            _defaultLayer2EvidenceCount,
            _defaultLayer3EvidenceCount));
    if (_defaultLayer2EvidenceCount > 0 && _defaultLayer3EvidenceCount > 0) {
      _configuration
          .getWarnings()
          .redFlag(
              String.format(
                  "Guessing Ethernet interfaces default to layer-%d mode based on statistical analysis of configuration.",
                  _configuration.getSystemDefaultSwitchport() ? 2 : 3));
    }
  }

  @Override
  public void exitS_version(S_versionContext ctx) {
    _configuration.setVersion(ctx.version.getText());
  }

  public void exitBoot_kickstart(Boot_kickstartContext ctx) {
    String image = ctx.image.getText();
    if (ctx.SUP_1() != null) {
      _configuration.setBootKickstartSup1(image);
    } else if (ctx.SUP_2() != null) {
      _configuration.setBootKickstartSup2(image);
    } else {
      _configuration.setBootKickstartSup1(image);
      _configuration.setBootKickstartSup2(image);
    }
  }

  @Override
  public void exitBoot_nxos(Boot_nxosContext ctx) {
    String image = ctx.image.getText();
    if (ctx.SUP_1() != null) {
      _configuration.setBootNxosSup1(image);
    } else if (ctx.SUP_2() != null) {
      _configuration.setBootNxosSup2(image);
    } else {
      _configuration.setBootNxosSup1(image);
      _configuration.setBootNxosSup2(image);
    }
  }

  @Override
  public void exitBoot_system(Boot_systemContext ctx) {
    String image = ctx.image.getText();
    if (ctx.SUP_1() != null) {
      _configuration.setBootSystemSup1(image);
    } else if (ctx.SUP_2() != null) {
      _configuration.setBootSystemSup2(image);
    } else {
      _configuration.setBootSystemSup1(image);
      _configuration.setBootSystemSup2(image);
    }
  }

  @Override
  public void enterS_interface_regular(S_interface_regularContext ctx) {
    _currentInterfaceType = toType(ctx.irange.iname.prefix);
    _currentInterfaceSwitchport = null;
    _currentInterfaceSubinterface = ctx.irange.getText().contains(".");
  }

  @Override
  public void exitS_interface_regular(S_interface_regularContext ctx) {
    _currentInterfaceType = null;
    _currentInterfaceSwitchport = null;
    _currentInterfaceSubinterface = null;
  }

  @Override
  public void exitI_no_switchport(I_no_switchportContext ctx) {
    _currentInterfaceSwitchport = false;
  }

  @Override
  public void exitI_switchport_switchport(I_switchport_switchportContext ctx) {
    _currentInterfaceSwitchport = true;
  }

  @Override
  public void exitNo_sysds_switchport(No_sysds_switchportContext ctx) {
    _explicitSystemDefaultSwitchport = true;
  }

  @Override
  public void exitSysds_switchport(Sysds_switchportContext ctx) {
    _explicitSystemDefaultSwitchport = true;
  }

  private boolean providesEvidence() {
    return !_explicitSystemDefaultSwitchport
        && !_currentInterfaceSubinterface
        && _currentInterfaceSwitchport == null
        && (_currentInterfaceType == CiscoNxosInterfaceType.ETHERNET
            || _currentInterfaceType == CiscoNxosInterfaceType.PORT_CHANNEL);
  }

  // layer-2-only commands

  @Override
  public void exitI_switchport(I_switchportContext ctx) {
    // any line but "switchport\n" (i_switchoprt_switchport) is configuring an L2 property.
    boolean configuringL2Property = (ctx.i_switchport_switchport() == null);
    if (configuringL2Property && providesEvidence()) {
      _defaultLayer2EvidenceCount++;
    }
  }

  // layer-3-only commands

  @Override
  public void exitI_ip_address(I_ip_addressContext ctx) {
    if (providesEvidence()) {
      _defaultLayer3EvidenceCount++;
    }
  }

  @Override
  public void exitI_vrf_member(I_vrf_memberContext ctx) {
    if (providesEvidence()) {
      _defaultLayer3EvidenceCount++;
    }
  }
}
