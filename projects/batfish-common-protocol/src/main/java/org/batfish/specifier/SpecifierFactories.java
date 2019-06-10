package org.batfish.specifier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.specifier.parboiled.ParboiledApplicationSpecifier;
import org.batfish.specifier.parboiled.ParboiledFilterSpecifier;
import org.batfish.specifier.parboiled.ParboiledInterfaceSpecifier;
import org.batfish.specifier.parboiled.ParboiledIpProtocolSpecifier;
import org.batfish.specifier.parboiled.ParboiledIpSpaceSpecifier;
import org.batfish.specifier.parboiled.ParboiledLocationSpecifier;
import org.batfish.specifier.parboiled.ParboiledNamedStructureSpecifier;
import org.batfish.specifier.parboiled.ParboiledNodeSpecifier;
import org.batfish.specifier.parboiled.ParboiledRoutingPolicySpecifier;

/**
 * This class enables a global choice of the grammar that is used by different question parameters.
 * Questions call the static functions below to directly get the specifier and never explicitly pick
 * the factory.
 */
@ParametersAreNonnullByDefault
public final class SpecifierFactories {

  public enum Version {
    V1, // original, regex-based parsing of "flexible" specifiers
    V2 // newer parboiled-based implementation
  }

  private SpecifierFactories() {}

  /** Which grammar version is currently in use */
  public static final Version ACTIVE_VERSION = Version.V2;

  public static ApplicationSpecifier getApplicationSpecifier(String input, Version version) {
    switch (version) {
      case V1:
      case V2:
        return new ParboiledApplicationSpecifier(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static FilterSpecifier getFilterSpecifier(String input, Version version) {
    switch (version) {
      case V1:
        throw new IllegalArgumentException("V1 filter grammar has been nixed");
      case V2:
        return new ParboiledFilterSpecifier(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static InterfaceSpecifier getInterfaceSpecifier(String input, Version version) {
    switch (version) {
      case V1:
        throw new IllegalArgumentException("V1 interface grammar has been nixed");
      case V2:
        return new ParboiledInterfaceSpecifier(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static IpProtocolSpecifier getIpProtocolSpecifier(String input, Version version) {
    switch (version) {
      case V1:
      case V2:
        return new ParboiledIpProtocolSpecifier(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static IpSpaceSpecifier getIpSpaceSpecifier(String input, Version version) {
    switch (version) {
      case V1:
        throw new IllegalArgumentException("V1 IpSpace grammar has been nixed");
      case V2:
        return new ParboiledIpSpaceSpecifier(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static LocationSpecifier getLocationSpecifier(String input, Version version) {
    switch (version) {
      case V1:
        throw new IllegalArgumentException("V1 location grammar has been nixed");
      case V2:
        return new ParboiledLocationSpecifier(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static NamedStructureSpecifier getNamedStructureSpecifier(String input, Version version) {
    switch (version) {
      case V1:
      case V2:
        return new ParboiledNamedStructureSpecifier(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static NodeSpecifier getNodeSpecifier(String input, Version version) {
    switch (version) {
      case V1:
        throw new IllegalArgumentException("V1 grammar has been completely removed");
      case V2:
        return new ParboiledNodeSpecifier(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static RoutingPolicySpecifier getRoutingPolicySpecifier(String input, Version version) {
    switch (version) {
      case V1:
      case V2:
        return new ParboiledRoutingPolicySpecifier(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static ApplicationSpecifier getApplicationSpecifierOrDefault(
      @Nullable String input, ApplicationSpecifier defaultSpecifier) {
    return getApplicationSpecifierOrDefault(input, defaultSpecifier, ACTIVE_VERSION);
  }

  public static FilterSpecifier getFilterSpecifierOrDefault(
      @Nullable String input, FilterSpecifier defaultSpecifier) {
    return getFilterSpecifierOrDefault(input, defaultSpecifier, ACTIVE_VERSION);
  }

  public static InterfaceSpecifier getInterfaceSpecifierOrDefault(
      @Nullable String input, InterfaceSpecifier defaultSpecifier) {
    return getInterfaceSpecifierOrDefault(input, defaultSpecifier, ACTIVE_VERSION);
  }

  public static IpProtocolSpecifier getIpProtocolSpecifierOrDefault(
      @Nullable String input, IpProtocolSpecifier defaultSpecifier) {
    return getIpProtocolSpecifierOrDefault(input, defaultSpecifier, ACTIVE_VERSION);
  }

  public static IpSpaceSpecifier getIpSpaceSpecifierOrDefault(
      @Nullable String input, IpSpaceSpecifier defaultSpecifier) {
    return getIpSpaceSpecifierOrDefault(input, defaultSpecifier, ACTIVE_VERSION);
  }

  public static LocationSpecifier getLocationSpecifierOrDefault(
      @Nullable String input, LocationSpecifier defaultSpecifier) {
    return getLocationSpecifierOrDefault(input, defaultSpecifier, ACTIVE_VERSION);
  }

  public static NamedStructureSpecifier getNamedStructureSpecifierOrDefault(
      @Nullable String input, NamedStructureSpecifier defaultSpecifier) {
    return getNamedStructureSpecifierOrDefault(input, defaultSpecifier, ACTIVE_VERSION);
  }

  public static NodeSpecifier getNodeSpecifierOrDefault(
      @Nullable String input, NodeSpecifier defaultSpecifier) {
    return getNodeSpecifierOrDefault(input, defaultSpecifier, ACTIVE_VERSION);
  }

  public static RoutingPolicySpecifier getRoutingPolicySpecifierOrDefault(
      @Nullable String input, RoutingPolicySpecifier defaultSpecifier) {
    return getRoutingPolicySpecifierOrDefault(input, defaultSpecifier, ACTIVE_VERSION);
  }

  public static ApplicationSpecifier getApplicationSpecifierOrDefault(
      @Nullable String input, ApplicationSpecifier defaultSpecifier, Version v) {
    return input == null || input.isEmpty() ? defaultSpecifier : getApplicationSpecifier(input, v);
  }

  public static FilterSpecifier getFilterSpecifierOrDefault(
      @Nullable String input, FilterSpecifier defaultSpecifier, Version v) {
    return input == null || input.isEmpty() ? defaultSpecifier : getFilterSpecifier(input, v);
  }

  public static InterfaceSpecifier getInterfaceSpecifierOrDefault(
      @Nullable String input, InterfaceSpecifier defaultSpecifier, Version v) {
    return input == null || input.isEmpty() ? defaultSpecifier : getInterfaceSpecifier(input, v);
  }

  public static IpProtocolSpecifier getIpProtocolSpecifierOrDefault(
      @Nullable String input, IpProtocolSpecifier defaultSpecifier, Version v) {
    return input == null || input.isEmpty() ? defaultSpecifier : getIpProtocolSpecifier(input, v);
  }

  public static IpSpaceSpecifier getIpSpaceSpecifierOrDefault(
      @Nullable String input, IpSpaceSpecifier defaultSpecifier, Version v) {
    return input == null || input.isEmpty() ? defaultSpecifier : getIpSpaceSpecifier(input, v);
  }

  public static LocationSpecifier getLocationSpecifierOrDefault(
      @Nullable String input, LocationSpecifier defaultSpecifier, Version v) {
    return input == null || input.isEmpty() ? defaultSpecifier : getLocationSpecifier(input, v);
  }

  public static NamedStructureSpecifier getNamedStructureSpecifierOrDefault(
      @Nullable String input, NamedStructureSpecifier defaultSpecifier, Version v) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : getNamedStructureSpecifier(input, v);
  }

  public static NodeSpecifier getNodeSpecifierOrDefault(
      @Nullable String input, NodeSpecifier defaultSpecifier, Version v) {
    return input == null || input.isEmpty() ? defaultSpecifier : getNodeSpecifier(input, v);
  }

  public static RoutingPolicySpecifier getRoutingPolicySpecifierOrDefault(
      @Nullable String input, RoutingPolicySpecifier defaultSpecifier, Version v) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : getRoutingPolicySpecifier(input, v);
  }
}
