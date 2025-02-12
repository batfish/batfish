package org.batfish.grammar.cisco_nxos;

import static org.batfish.grammar.cisco_nxos.CiscoNxosControlPlaneExtractor.toType;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.Optional;
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
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_no_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_shutdownContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchportContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_switchport_switchportContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.I_vrf_memberContext;
import org.batfish.grammar.cisco_nxos.CiscoNxosParser.Inos_switchportContext;
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
 * <p>Determines the NX-OS major version via structure of the version string and/or boot image
 * name(s)
 *
 * <p>Determines the platform via NX-OS major version and structure of the boot image name(s). In
 * some cases, this information only narrows down to Nexus 3000 or Nexus 9000. For NX-OS 9, there is
 * no functional difference between defaults, so Nexus 9000 is assumed. For earlier versions of
 * NX-OS, the functional difference is that L3 interfaces are shutdown by default on Nexus 9000, but
 * on by default on Nexus 3000. To distinguish platform on such versions, inference is performed to
 * determine whether default L3 status is shutdown or not. A decision is made by counting how many
 * L3 interfaces are explicitly shutdown, and how many are explicity not shutdown. Based on the
 * result, the more likely platform (Nexus 3000 or Nexus 9000) is selected.
 *
 * <p>Determines default L3 shutdown status based on inferred platform and NX-OS major version.
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
      Pattern.compile(".*nxos\\.(10|[0-9]).*");
  private static final Pattern KICKSTART_MAJOR_VERSION_PATTERN =
      Pattern.compile(".*kickstart\\.(10|[0-9]).*");

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
      case '1':
        switch (version.charAt(1)) {
          case '0':
            return NxosMajorVersion.NXOS10;
          default:
            return null;
        }
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
  public static @Nonnull NexusPlatform inferPlatform(
      CiscoNxosConfiguration vc,
      NxosMajorVersion majorVersion,
      int numLayer3ExplicitShutdown,
      int numLayer3ExplicitNoShutdown) {
    Optional<NexusPlatform> initialGuessOrNoInformation =
        Stream.of(
                vc.getBootNxosSup1(),
                vc.getBootNxosSup2(),
                vc.getBootSystemSup1(),
                vc.getBootSystemSup2(),
                vc.getBootKickstartSup1(),
                vc.getBootKickstartSup2())
            .filter(Objects::nonNull)
            .findFirst()
            .map(CiscoNxosPreprocessor::inferPlatformFromImage);

    if (!initialGuessOrNoInformation.isPresent()) {
      // No boot information from which to infer platform
      return NexusPlatform.UNKNOWN;
    }
    NexusPlatform initialGuess = initialGuessOrNoInformation.get();
    if (initialGuess != NexusPlatform.UNKNOWN) {
      return initialGuess;
    }
    // Assume that UNKNOWN means Nexus 3000/9000. Find likely default layer-3 shutdown behavior, and
    // infer platform accordingly
    if (majorVersion == NxosMajorVersion.NXOS9) {
      // In this case, defaults are same for Nexus 3000 and Nexus 9000. So just arbitrarily choose
      // Nexus 9000.
      return NexusPlatform.NEXUS_9000;
    }
    if (majorVersion == NxosMajorVersion.NXOS10) {
      // In this case, defaults are same for Nexus 3000 and Nexus 9000. So just arbitrarily choose
      // Nexus 9000.
      return NexusPlatform.NEXUS_9000;
    }
    if (numLayer3ExplicitNoShutdown == 0 && numLayer3ExplicitShutdown > 0) {
      // Apparently, default layer-3 admin status is no shutdown. This corresponds to Nexus 3000.
      return NexusPlatform.NEXUS_3000;
    }
    if (numLayer3ExplicitNoShutdown > 0 && numLayer3ExplicitShutdown == 0) {
      // Apparently, default layer-3 admin status is shutdown. This corresponds to Nexus 9000.
      return NexusPlatform.NEXUS_9000;
    }
    // Explicits are either both zero or both non-zero.
    // No idea, really. Bias towards no shutdown by default, i.e. Nexus 3000.
    return NexusPlatform.NEXUS_3000;
  }

  /**
   * Infers {@code NexusPlatform} of a configuration based on name of boot image file. Returns
   * {@link NexusPlatform#UNKNOWN} if unique inference cannot be made.
   */
  @VisibleForTesting
  static @Nonnull NexusPlatform inferPlatformFromImage(String image) {
    if (image.contains("n3000")) {
      return NexusPlatform.NEXUS_3000;
    } else if (image.contains("n5000")) {
      return NexusPlatform.NEXUS_5000;
    } else if (image.contains("n6000")) {
      return NexusPlatform.NEXUS_6000;
    } else if (image.contains("n7000") || image.contains("n7700") || image.contains("titanium")) {
      return NexusPlatform.NEXUS_7000;
    } else {
      return NexusPlatform.UNKNOWN;
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
    return switch (platform) {
      case NEXUS_1000V ->
          // TODO: verify
          true;
      case NEXUS_3000 ->
          // verified for NXOS6
          true;
      case NEXUS_5000 ->
          // verified for NXOS7
          true;
      case NEXUS_6000 ->
          // verified for NXOS7
          true;
      case NEXUS_7000 ->
          // verified for NXOS6
          false;
      case NEXUS_9000, UNKNOWN ->
          switch (majorVersion) {
            case NXOS5 ->
                // docs are ambiguous, but sounds like switchport is default
                // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus3548/sw/cmd_ref/503_A1/interfaces/3548_cmd_ref_if/3k_cmd_ref_if_cmds.html
                true;
            case NXOS6 ->
                // TODO: add doc pointer
                false;
            case NXOS7, NXOS9, NXOS10, UNKNOWN ->
                // Assume NXOS7+ behavior arbitrarily when version is unknown.
                // Value depends on model, so just make an educated guess based on statistical
                // analysis
                defaultLayer2EvidenceCount > defaultLayer3EvidenceCount;
            default ->
                throw new IllegalArgumentException(
                    String.format("Unsupported major version: %s", majorVersion));
          };
    };
  }

  public static boolean getNonSwitchportDefaultShutdown(
      NexusPlatform platform, NxosMajorVersion majorVersion) {
    return switch (platform) {
      case NEXUS_1000V ->
          // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus1000/sw/4_2_1_s_v_1_4/command/reference/n1000v_cmd_ref/n1000v_cmds_s.html
          false;
      case NEXUS_3000 ->
          switch (majorVersion) {
            case NXOS5, NXOS6, NXOS7 ->
                // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus3000/sw/command/reference/5_0_3/interfaces/3k_cmd_ref_if/3k_cmd_ref_if_cmds.html
                // TODO: verify for NXOS6,7
                false;
            case NXOS9 ->
                // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus3600/sw/93x/interfaces/configuration/guide/b-cisco-nexus-3600-nx-os-interfaces-configuration-guide-93x/b-cisco-nexus-3600-nx-os-interfaces-configuration-guide-93x_chapter_011.html
                true;
            case NXOS10 ->
                // https://www.cisco.com/c/en/us/td/docs/dcn/nx-os/nexus9000/101x/configuration/interfaces/cisco-nexus-9000-nx-os-interfaces-configuration-guide-101x/b-cisco-nexus-9000-nx-os-interfaces-configuration-guide-93x_chapter_0101.html#:~:text=you%20would%20use.-,Default%20Settings,Admin%20state,-Shut
                true;
            case NXOS4, UNKNOWN ->
                throw new IllegalArgumentException(
                    String.format("Unsupported major version: %s", majorVersion));
          };
      case NEXUS_5000 ->
          // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus5000/sw/interfaces/command/cisco_nexus_5000_interfaces_command_ref/s_commands.html
          false;
      case NEXUS_6000 ->
          // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus6000/sw/command/reference/interfaces/N6k_if_cmd_ref/n6k_if_cmds_s.html
          false;
      case NEXUS_7000 ->
          // NO DEFAULT according to
          // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus7000/sw/interfaces/command/cisco_nexus7000_interfaces_command_ref/s_commands.html#wp2891012724
          // Since it doesn't matter, just default to false
          false;
      case NEXUS_9000 ->
          // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus9000/sw/6-x/interfaces/configuration/guide/b_Cisco_Nexus_9000_Series_NX-OS_Interfaces_Configuration_Guide/b_Cisco_Nexus_9000_Series_NX-OS_Interfaces_Configuration_Guide_chapter_010.html
          // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus9000/sw/92x/interfaces/configuration/guide/b-cisco-nexus-9000-nx-os-interfaces-configuration-guide-92x/b-cisco-nexus-9000-nx-os-interfaces-configuration-guide-92x_chapter_011.html#concept_B279E7CC6BC04683BE07B09298887229
          true;
      case UNKNOWN ->
          // bias towards interfaces being on
          false;
    };
  }

  private final @Nonnull CiscoNxosConfiguration _configuration;

  @SuppressWarnings("unused")
  private final @Nonnull CiscoNxosCombinedParser _parser;

  @SuppressWarnings("unused")
  private final @Nonnull String _text;

  @SuppressWarnings("unused")
  private final @Nonnull Warnings _w;

  /**
   * {@code true} if current interface appears to have L3 configuration. {@code false} if current
   * interface appears to have L2 configuration. {@code null} if unknown.
   */
  private Boolean _currentInterfaceLayer3;

  /**
   * {@code true} if current interface has explicit 'shutdown'; {@code false} if current interface
   * has explicit 'no shutdown'; or else {@code null}.
   */
  private Boolean _currentInterfaceShutdown;

  private Boolean _currentInterfaceSubinterface;
  private Boolean _currentInterfaceSwitchport;
  private CiscoNxosInterfaceType _currentInterfaceType;
  private int _defaultLayer2EvidenceCount;
  private int _defaultLayer3EvidenceCount;

  /** Number of interfaces that have L3 configuration and explicit 'no shutdown' */
  private int _numLayer3ExplicitNoShutdown;

  /** Number of interfaces that have L3 configuration and explicit 'shutdown' */
  private int _numLayer3ExplicitShutdown;

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
    NexusPlatform platform =
        inferPlatform(
            _configuration, majorVersion, _numLayer3ExplicitShutdown, _numLayer3ExplicitNoShutdown);
    _configuration.setPlatform(platform);
    _configuration.setNonSwitchportDefaultShutdown(
        getNonSwitchportDefaultShutdown(platform, majorVersion));
    _configuration.setSystemDefaultSwitchport(
        getDefaultDefaultSwitchport(
            _configuration,
            platform,
            majorVersion,
            _defaultLayer2EvidenceCount,
            _defaultLayer3EvidenceCount));
    if (_defaultLayer2EvidenceCount > 0 && _defaultLayer3EvidenceCount > 0) {
      _w.redFlagf(
          "Guessing Ethernet interfaces default to layer-%d mode based on statistical analysis"
              + " of configuration.",
          _configuration.getSystemDefaultSwitchport() ? 2 : 3);
    }
  }

  @Override
  public void exitS_version(S_versionContext ctx) {
    _configuration.setVersion(ctx.version.getText());
  }

  @Override
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
    _currentInterfaceLayer3 = false;
    _currentInterfaceShutdown = null;
    _currentInterfaceSwitchport = null;
    _currentInterfaceSubinterface = ctx.irange.getText().contains(".");
    _currentInterfaceType = toType(ctx.irange.iname.prefix);
  }

  @Override
  public void exitS_interface_regular(S_interface_regularContext ctx) {
    if (providesLayer3DefaultShutdownEvidence()) {
      if (_currentInterfaceShutdown) {
        _numLayer3ExplicitShutdown++;
      } else {
        _numLayer3ExplicitNoShutdown++;
      }
    }

    _currentInterfaceLayer3 = null;
    _currentInterfaceShutdown = null;
    _currentInterfaceSwitchport = null;
    _currentInterfaceSubinterface = null;
    _currentInterfaceType = null;
  }

  @Override
  public void exitI_shutdown(I_shutdownContext ctx) {
    _currentInterfaceShutdown = true;
  }

  @Override
  public void exitI_no_shutdown(I_no_shutdownContext ctx) {
    _currentInterfaceShutdown = false;
  }

  @Override
  public void exitInos_switchport(Inos_switchportContext ctx) {
    if (providesSwitchportEvidence()) {
      // explicit setting of layer-3 is evidence of default layer-2
      _defaultLayer2EvidenceCount++;
    }
    _currentInterfaceSwitchport = false;
    _currentInterfaceLayer3 = true;
  }

  @Override
  public void exitI_switchport_switchport(I_switchport_switchportContext ctx) {
    if (providesSwitchportEvidence()) {
      // explicit setting of layer-2 is evidence of default layer-3
      _defaultLayer3EvidenceCount++;
    }
    _currentInterfaceSwitchport = true;
    _currentInterfaceLayer3 = false;
  }

  @Override
  public void exitNo_sysds_switchport(No_sysds_switchportContext ctx) {
    _explicitSystemDefaultSwitchport = true;
  }

  @Override
  public void exitSysds_switchport(Sysds_switchportContext ctx) {
    _explicitSystemDefaultSwitchport = true;
  }

  /**
   * Returns {@code true} iff the current statement interface provides evidence useful for inferring
   * whether default mode for physical/port-channel interfaces is L2 or L3.
   */
  private boolean providesSwitchportEvidence() {
    return !_explicitSystemDefaultSwitchport
        && !_currentInterfaceSubinterface
        && _currentInterfaceSwitchport == null
        && (_currentInterfaceType == CiscoNxosInterfaceType.ETHERNET
            || _currentInterfaceType == CiscoNxosInterfaceType.PORT_CHANNEL);
  }

  /**
   * Returns {@code true} iff the current interface provides evidence useful for inferring whether
   * L3 interfaces are (not) shutdown by default.
   */
  private boolean providesLayer3DefaultShutdownEvidence() {
    return _currentInterfaceShutdown != null
        && !_currentInterfaceSubinterface
        && Boolean.TRUE.equals(_currentInterfaceLayer3)
        && (_currentInterfaceType == CiscoNxosInterfaceType.ETHERNET
            || _currentInterfaceType == CiscoNxosInterfaceType.PORT_CHANNEL);
  }

  // layer-2-only commands

  @Override
  public void exitI_switchport(I_switchportContext ctx) {
    // Note that if this contains I_switchport_switchport, by now providesSwitchportEvidence will
    // return false. This avoids double-counting.
    if (providesSwitchportEvidence()) {
      _defaultLayer2EvidenceCount++;
    }
  }

  // layer-3-only commands

  @Override
  public void exitI_ip_address(I_ip_addressContext ctx) {
    if (providesSwitchportEvidence()) {
      _defaultLayer3EvidenceCount++;
    }
    _currentInterfaceLayer3 = true;
  }

  @Override
  public void exitI_vrf_member(I_vrf_memberContext ctx) {
    if (providesSwitchportEvidence()) {
      _defaultLayer3EvidenceCount++;
    }
    _currentInterfaceLayer3 = true;
  }
}
