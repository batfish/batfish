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

  public enum FactoryGroup {
    V1, // original, regex-based parsing of "flexible" specifiers
    V2 // newer parboiled-based implementation
  }

  private SpecifierFactories() {}

  public static final FactoryGroup ACTIVE_GROUP = FactoryGroup.V1;

  public static final String Filter =
      ACTIVE_GROUP == FactoryGroup.V1
          ? FlexibleFilterSpecifierFactory.NAME
          : ParboiledFilterSpecifierFactory.NAME;

  public static final String Interface =
      ACTIVE_GROUP == FactoryGroup.V1
          ? FlexibleInterfaceSpecifierFactory.NAME
          : ParboiledInterfaceSpecifierFactory.NAME;

  public static final String IpSpace =
      ACTIVE_GROUP == FactoryGroup.V1
          ? FlexibleIpSpaceSpecifierFactory.NAME
          : ParboiledIpSpaceSpecifierFactory.NAME;

  public static final String Location =
      ACTIVE_GROUP == FactoryGroup.V1
          ? FlexibleLocationSpecifierFactory.NAME
          : ParboiledLocationSpecifierFactory.NAME;

  public static final String Node =
      ACTIVE_GROUP == FactoryGroup.V1
          ? FlexibleNodeSpecifierFactory.NAME
          : ParboiledNodeSpecifierFactory.NAME;

  public static FilterSpecifier getFilterSpecifierOrDefault(
      @Nullable String input, FilterSpecifier defaultSpecifier) {
    return getFilterSpecifierOrDefault(input, defaultSpecifier, Filter);
  }

  public static InterfaceSpecifier getInterfaceSpecifierOrDefault(
      @Nullable String input, InterfaceSpecifier defaultSpecifier) {
    return getInterfaceSpecifierOrDefault(input, defaultSpecifier, Interface);
  }

  public static IpSpaceSpecifier getIpSpaceSpecifierOrDefault(
      @Nullable String input, IpSpaceSpecifier defaultSpecifier) {
    return getIpSpaceSpecifierOrDefault(input, defaultSpecifier, IpSpace);
  }

  public static LocationSpecifier getLocationSpecifierOrDefault(
      @Nullable String input, LocationSpecifier defaultSpecifier) {
    return getLocationSpecifierOrDefault(input, defaultSpecifier, Location);
  }

  public static NodeSpecifier getNodeSpecifierOrDefault(
      @Nullable String input, NodeSpecifier defaultSpecifier) {
    return getNodeSpecifierOrDefault(input, defaultSpecifier, Node);
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
