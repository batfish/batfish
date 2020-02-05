package org.batfish.specifier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.specifier.parboiled.Grammar;
import org.batfish.specifier.parboiled.ParboiledAppSpecifier;
import org.batfish.specifier.parboiled.ParboiledEnumSetSpecifier;
import org.batfish.specifier.parboiled.ParboiledFilterSpecifier;
import org.batfish.specifier.parboiled.ParboiledInterfaceSpecifier;
import org.batfish.specifier.parboiled.ParboiledIpProtocolSpecifier;
import org.batfish.specifier.parboiled.ParboiledIpSpaceSpecifier;
import org.batfish.specifier.parboiled.ParboiledLocationSpecifier;
import org.batfish.specifier.parboiled.ParboiledNameSetSpecifier;
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
        return ParboiledAppSpecifier.parse(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static FilterSpecifier getFilterSpecifier(String input, Version version) {
    switch (version) {
      case V1:
        throw new IllegalArgumentException("V1 filter grammar has been nixed");
      case V2:
        return ParboiledFilterSpecifier.parse(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static InterfaceSpecifier getInterfaceSpecifier(String input, Version version) {
    switch (version) {
      case V1:
        throw new IllegalArgumentException("V1 interface grammar has been nixed");
      case V2:
        return ParboiledInterfaceSpecifier.parse(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static IpProtocolSpecifier getIpProtocolSpecifier(String input, Version version) {
    switch (version) {
      case V1:
      case V2:
        return ParboiledIpProtocolSpecifier.parse(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static IpSpaceSpecifier getIpSpaceSpecifier(String input, Version version) {
    switch (version) {
      case V1:
        throw new IllegalArgumentException("V1 IpSpace grammar has been nixed");
      case V2:
        return ParboiledIpSpaceSpecifier.parse(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static LocationSpecifier getLocationSpecifier(String input, Version version) {
    switch (version) {
      case V1:
        throw new IllegalArgumentException("V1 location grammar has been nixed");
      case V2:
        return ParboiledLocationSpecifier.parse(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static <T> EnumSetSpecifier<T> getEnumSetSpecifier(
      String input, Grammar grammar, Version version) {
    switch (version) {
      case V1:
      case V2:
        return ParboiledEnumSetSpecifier.parse(input, grammar);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static NameSetSpecifier getNameSetSpecifier(
      String input, Grammar grammar, Version version) {
    switch (version) {
      case V1:
      case V2:
        return ParboiledNameSetSpecifier.parse(input, grammar);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static NodeSpecifier getNodeSpecifier(String input, Version version) {
    switch (version) {
      case V1:
        throw new IllegalArgumentException("V1 grammar has been completely removed");
      case V2:
        return ParboiledNodeSpecifier.parse(input);
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static RoutingPolicySpecifier getRoutingPolicySpecifier(String input, Version version) {
    switch (version) {
      case V1:
      case V2:
        return ParboiledRoutingPolicySpecifier.parse(input);
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

  public static <T> EnumSetSpecifier<T> getEnumSetSpecifierOrDefault(
      @Nullable String input, Grammar grammar, EnumSetSpecifier<T> defaultSpecifier) {
    return getEnumSetSpecifierOrDefault(input, grammar, defaultSpecifier, ACTIVE_VERSION);
  }

  public static NameSetSpecifier getNameSetSpecifierOrDefault(
      @Nullable String input, Grammar grammar, NameSetSpecifier defaultSpecifier) {
    return getNameSetSpecifierOrDefault(input, grammar, defaultSpecifier, ACTIVE_VERSION);
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

  public static <T> EnumSetSpecifier<T> getEnumSetSpecifierOrDefault(
      @Nullable String input, Grammar grammar, EnumSetSpecifier<T> defaultSpecifier, Version v) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : getEnumSetSpecifier(input, grammar, v);
  }

  public static NameSetSpecifier getNameSetSpecifierOrDefault(
      @Nullable String input, Grammar grammar, NameSetSpecifier defaultSpecifier, Version v) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : getNameSetSpecifier(input, grammar, v);
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
