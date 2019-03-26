package org.batfish.specifier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.specifier.parboiled.ParboiledApplicationSpecifierFactory;
import org.batfish.specifier.parboiled.ParboiledFilterSpecifierFactory;
import org.batfish.specifier.parboiled.ParboiledInterfaceSpecifierFactory;
import org.batfish.specifier.parboiled.ParboiledIpProtocolSpecifierFactory;
import org.batfish.specifier.parboiled.ParboiledIpSpaceSpecifierFactory;
import org.batfish.specifier.parboiled.ParboiledLocationSpecifierFactory;
import org.batfish.specifier.parboiled.ParboiledNodeSpecifierFactory;
import org.batfish.specifier.parboiled.ParboiledRoutingPolicySpecifierFactory;

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

  public static ApplicationSpecifierFactory getApplicationFactory(Version version) {
    switch (version) {
      case V1:
      case V2:
        return new ParboiledApplicationSpecifierFactory();
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static FilterSpecifierFactory getFilterFactory(Version version) {
    switch (version) {
      case V1:
        return new FlexibleFilterSpecifierFactory();
      case V2:
        return new ParboiledFilterSpecifierFactory();
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static InterfaceSpecifierFactory getInterfaceFactory(Version version) {
    switch (version) {
      case V1:
        return new FlexibleInterfaceSpecifierFactory();
      case V2:
        return new ParboiledInterfaceSpecifierFactory();
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static IpProtocolSpecifierFactory getIpProtocolFactory(Version version) {
    switch (version) {
      case V1:
      case V2:
        return new ParboiledIpProtocolSpecifierFactory();
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static IpSpaceSpecifierFactory getIpSpaceFactory(Version version) {
    switch (version) {
      case V1:
        return new FlexibleIpSpaceSpecifierFactory();
      case V2:
        return new ParboiledIpSpaceSpecifierFactory();
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static LocationSpecifierFactory getLocationFactory(Version version) {
    switch (version) {
      case V1:
        return new FlexibleLocationSpecifierFactory();
      case V2:
        return new ParboiledLocationSpecifierFactory();
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static NodeSpecifierFactory getNodeFactory(Version version) {
    switch (version) {
      case V1:
        return new FlexibleNodeSpecifierFactory();
      case V2:
        return new ParboiledNodeSpecifierFactory();
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  public static RoutingPolicySpecifierFactory getRoutingPolicyFactory(Version version) {
    switch (version) {
      case V1:
      case V2:
        return new ParboiledRoutingPolicySpecifierFactory();
      default:
        throw new IllegalStateException("Unhandled grammar version " + version);
    }
  }

  /** Define these constants, so we don't have to keep computing them */
  private static final ApplicationSpecifierFactory ActiveApplicationFactory =
      getApplicationFactory(ACTIVE_VERSION);

  private static final FilterSpecifierFactory ActiveFilterFactory =
      getFilterFactory(ACTIVE_VERSION);

  private static final InterfaceSpecifierFactory ActiveInterfaceFactory =
      getInterfaceFactory(ACTIVE_VERSION);

  private static final IpProtocolSpecifierFactory ActiveIpProtocolFactory =
      getIpProtocolFactory(ACTIVE_VERSION);

  private static final IpSpaceSpecifierFactory ActiveIpSpaceFactory =
      getIpSpaceFactory(ACTIVE_VERSION);

  private static final LocationSpecifierFactory ActiveLocationFactory =
      getLocationFactory(ACTIVE_VERSION);

  private static final NodeSpecifierFactory ActiveNodeFactory = getNodeFactory(ACTIVE_VERSION);

  private static final RoutingPolicySpecifierFactory ActiveRoutingPolicySpecifier =
      getRoutingPolicyFactory(ACTIVE_VERSION);

  public static ApplicationSpecifier getApplicationSpecifierOrDefault(
      @Nullable String input, ApplicationSpecifier defaultSpecifier) {
    return getApplicationSpecifierOrDefault(input, defaultSpecifier, ActiveApplicationFactory);
  }

  public static FilterSpecifier getFilterSpecifierOrDefault(
      @Nullable String input, FilterSpecifier defaultSpecifier) {
    return getFilterSpecifierOrDefault(input, defaultSpecifier, ActiveFilterFactory);
  }

  public static InterfaceSpecifier getInterfaceSpecifierOrDefault(
      @Nullable String input, InterfaceSpecifier defaultSpecifier) {
    return getInterfaceSpecifierOrDefault(input, defaultSpecifier, ActiveInterfaceFactory);
  }

  public static IpProtocolSpecifier getIpProtocolSpecifierOrDefault(
      @Nullable String input, IpProtocolSpecifier defaultSpecifier) {
    return getIpProtocolSpecifierOrDefault(input, defaultSpecifier, ActiveIpProtocolFactory);
  }

  public static IpSpaceSpecifier getIpSpaceSpecifierOrDefault(
      @Nullable String input, IpSpaceSpecifier defaultSpecifier) {
    return getIpSpaceSpecifierOrDefault(input, defaultSpecifier, ActiveIpSpaceFactory);
  }

  public static LocationSpecifier getLocationSpecifierOrDefault(
      @Nullable String input, LocationSpecifier defaultSpecifier) {
    return getLocationSpecifierOrDefault(input, defaultSpecifier, ActiveLocationFactory);
  }

  public static NodeSpecifier getNodeSpecifierOrDefault(
      @Nullable String input, NodeSpecifier defaultSpecifier) {
    return getNodeSpecifierOrDefault(input, defaultSpecifier, ActiveNodeFactory);
  }

  public static ApplicationSpecifier getApplicationSpecifierOrDefault(
      @Nullable String input,
      ApplicationSpecifier defaultSpecifier,
      ApplicationSpecifierFactory factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : factory.buildApplicationSpecifier(input);
  }

  public static FilterSpecifier getFilterSpecifierOrDefault(
      @Nullable String input, FilterSpecifier defaultSpecifier, FilterSpecifierFactory factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : factory.buildFilterSpecifier(input);
  }

  public static InterfaceSpecifier getInterfaceSpecifierOrDefault(
      @Nullable String input,
      InterfaceSpecifier defaultSpecifier,
      InterfaceSpecifierFactory factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : factory.buildInterfaceSpecifier(input);
  }

  public static IpProtocolSpecifier getIpProtocolSpecifierOrDefault(
      @Nullable String input,
      IpProtocolSpecifier defaultSpecifier,
      IpProtocolSpecifierFactory factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : factory.buildIpProtocolSpecifier(input);
  }

  public static IpSpaceSpecifier getIpSpaceSpecifierOrDefault(
      @Nullable String input, IpSpaceSpecifier defaultSpecifier, IpSpaceSpecifierFactory factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : factory.buildIpSpaceSpecifier(input);
  }

  public static LocationSpecifier getLocationSpecifierOrDefault(
      @Nullable String input,
      LocationSpecifier defaultSpecifier,
      LocationSpecifierFactory factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : factory.buildLocationSpecifier(input);
  }

  public static NodeSpecifier getNodeSpecifierOrDefault(
      @Nullable String input, NodeSpecifier defaultSpecifier, NodeSpecifierFactory factory) {
    return input == null || input.isEmpty() ? defaultSpecifier : factory.buildNodeSpecifier(input);
  }

  public static RoutingPolicySpecifier getRoutingPolicySpecifierOrDefault(
      @Nullable String input, RoutingPolicySpecifier defaultSpecifier) {
    return getRoutingPolicySpecifierOrDefault(
        input, defaultSpecifier, ActiveRoutingPolicySpecifier);
  }

  public static RoutingPolicySpecifier getRoutingPolicySpecifierOrDefault(
      @Nullable String input,
      RoutingPolicySpecifier defaultSpecifier,
      RoutingPolicySpecifierFactory factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : factory.buildRoutingPolicySpecifier(input);
  }
}
