package org.batfish.specifier;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.specifier.parboiled.ParboiledFilterSpecifierFactory;
import org.batfish.specifier.parboiled.ParboiledInterfaceSpecifierFactory;
import org.batfish.specifier.parboiled.ParboiledIpSpaceSpecifierFactory;
import org.batfish.specifier.parboiled.ParboiledLocationSpecifierFactory;
import org.batfish.specifier.parboiled.ParboiledNodeSpecifierFactory;

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

  public enum SpecifierType {
    FILTER,
    INTERFACE,
    IP_SPACE,
    LOCATION,
    NODE
  }

  private SpecifierFactories() {}

  public static final Version ACTIVE_VERSION = Version.V1;

  public static String getFactory(Version group, SpecifierType specifier) {
    switch (group) {
      case V1:
        switch (specifier) {
          case FILTER:
            return FlexibleFilterSpecifierFactory.NAME;
          case INTERFACE:
            return FlexibleInterfaceSpecifierFactory.NAME;
          case IP_SPACE:
            return FlexibleIpSpaceSpecifierFactory.NAME;
          case LOCATION:
            return FlexibleLocationSpecifierFactory.NAME;
          case NODE:
            return FlexibleNodeSpecifierFactory.NAME;
          default:
            throw new IllegalStateException("Unhandled specifier type " + specifier);
        }
      case V2:
        switch (specifier) {
          case FILTER:
            return ParboiledFilterSpecifierFactory.NAME;
          case INTERFACE:
            return ParboiledInterfaceSpecifierFactory.NAME;
          case IP_SPACE:
            return ParboiledIpSpaceSpecifierFactory.NAME;
          case LOCATION:
            return ParboiledLocationSpecifierFactory.NAME;
          case NODE:
            return ParboiledNodeSpecifierFactory.NAME;
          default:
            throw new IllegalStateException("Unhandled specifier type " + specifier);
        }
      default:
        throw new IllegalStateException("Unhandled group type " + group);
    }
  }

  /** Define these constants, so we don't have to keep computing them */
  private static final String ActiveFilterFactory =
      getFactory(ACTIVE_VERSION, SpecifierType.FILTER);

  private static final String ActiveInterfaceFactory =
      getFactory(ACTIVE_VERSION, SpecifierType.INTERFACE);

  private static final String ActiveIpSpaceFactory =
      getFactory(ACTIVE_VERSION, SpecifierType.IP_SPACE);

  private static final String ActiveLocationFactory =
      getFactory(ACTIVE_VERSION, SpecifierType.LOCATION);

  private static final String ActiveNodeFactory = getFactory(ACTIVE_VERSION, SpecifierType.NODE);

  public static FilterSpecifier getFilterSpecifierOrDefault(
      @Nullable String input, FilterSpecifier defaultSpecifier) {
    return getFilterSpecifierOrDefault(input, defaultSpecifier, ActiveFilterFactory);
  }

  public static InterfaceSpecifier getInterfaceSpecifierOrDefault(
      @Nullable String input, InterfaceSpecifier defaultSpecifier) {
    return getInterfaceSpecifierOrDefault(input, defaultSpecifier, ActiveInterfaceFactory);
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

  public static FilterSpecifier getFilterSpecifierOrDefault(
      @Nullable String input, FilterSpecifier defaultSpecifier, String factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : FilterSpecifierFactory.load(factory).buildFilterSpecifier(input);
  }

  public static InterfaceSpecifier getInterfaceSpecifierOrDefault(
      @Nullable String input, InterfaceSpecifier defaultSpecifier, String factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : InterfaceSpecifierFactory.load(factory).buildInterfaceSpecifier(input);
  }

  public static IpSpaceSpecifier getIpSpaceSpecifierOrDefault(
      @Nullable String input, IpSpaceSpecifier defaultSpecifier, String factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : IpSpaceSpecifierFactory.load(factory).buildIpSpaceSpecifier(input);
  }

  public static LocationSpecifier getLocationSpecifierOrDefault(
      @Nullable String input, LocationSpecifier defaultSpecifier, String factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : LocationSpecifierFactory.load(factory).buildLocationSpecifier(input);
  }

  public static NodeSpecifier getNodeSpecifierOrDefault(
      @Nullable String input, NodeSpecifier defaultSpecifier, String factory) {
    return input == null || input.isEmpty()
        ? defaultSpecifier
        : NodeSpecifierFactory.load(factory).buildNodeSpecifier(input);
  }
}
