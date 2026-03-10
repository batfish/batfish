package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.bgp.AddressFamily;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.SpecifierFactories;

/**
 * Enables specification a set of BGP process properties.
 *
 * <p>Example specifiers:
 *
 * <ul>
 *   <li>multipath_ebgp —&gt; gets the process's corresponding value
 *   <li>multipath.* --&gt; gets all properties that start with 'multipath'
 * </ul>
 */
@ParametersAreNonnullByDefault
public class BgpProcessPropertySpecifier extends PropertySpecifier {

  public static final String CONFEDERATION_ID = "Confederation_ID";
  public static final String CONFEDERATION_MEMBERS = "Confederation_Members";
  public static final String MULTIPATH_EQUIVALENT_AS_PATH_MATCH_MODE = "Multipath_Match_Mode";
  public static final String MULTIPATH_EBGP = "Multipath_EBGP";
  public static final String MULTIPATH_IBGP = "Multipath_IBGP";
  public static final String NEIGHBORS = "Neighbors";
  public static final String ROUTE_REFLECTOR = "Route_Reflector";
  public static final String TIE_BREAKER = "Tie_Breaker";

  private static final Map<String, PropertyDescriptor<BgpProcess>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<BgpProcess>>()
          .put(
              ROUTE_REFLECTOR,
              new PropertyDescriptor<>(
                  BgpProcessPropertySpecifier::isIpv4UnicastRouteReflector,
                  Schema.BOOLEAN,
                  "Whether any BGP peer in this process is configured as a route reflector client,"
                      + " for ipv4 unicast address family"))
          .put(
              MULTIPATH_EQUIVALENT_AS_PATH_MATCH_MODE,
              new PropertyDescriptor<>(
                  BgpProcess::getMultipathEquivalentAsPathMatchMode,
                  Schema.STRING,
                  "Which AS paths are considered equivalent ("
                      + Arrays.stream(MultipathEquivalentAsPathMatchMode.values())
                          .map(Object::toString)
                          .collect(Collectors.joining(", "))
                      + ") when multipath BGP is enabled"))
          .put(
              MULTIPATH_EBGP,
              new PropertyDescriptor<>(
                  BgpProcess::getMultipathEbgp,
                  Schema.BOOLEAN,
                  "Whether multipath routing is enabled for EBGP"))
          .put(
              MULTIPATH_IBGP,
              new PropertyDescriptor<>(
                  BgpProcess::getMultipathIbgp,
                  Schema.BOOLEAN,
                  "Whether multipath routing is enabled for IBGP"))
          .put(
              NEIGHBORS,
              new PropertyDescriptor<>(
                  (process) ->
                      Iterables.concat(
                          process.getActiveNeighbors().keySet(),
                          process.getPassiveNeighbors().keySet(),
                          process.getInterfaceNeighbors().keySet()),
                  Schema.set(Schema.STRING),
                  "All peers configured on this process, identified by peer address (for active"
                      + " and dynamic peers) or peer interface (for BGP unnumbered peers)"))
          // skip router-id; included as part of process identity
          .put(
              TIE_BREAKER,
              new PropertyDescriptor<>(
                  BgpProcess::getTieBreaker,
                  Schema.STRING,
                  "Tie breaking mode ("
                      + Arrays.stream(BgpTieBreaker.values())
                          .map(Object::toString)
                          .collect(Collectors.joining(", "))
                      + ")"))
          .put(
              CONFEDERATION_ID,
              new PropertyDescriptor<>(
                  bgpProcess ->
                      bgpProcess.getConfederation() != null
                          ? bgpProcess.getConfederation().getId()
                          : null,
                  Schema.LONG,
                  "Externally visible autonomous system number for the confederation"))
          .put(
              CONFEDERATION_MEMBERS,
              new PropertyDescriptor<>(
                  bgpProcess ->
                      bgpProcess.getConfederation() != null
                          ? bgpProcess.getConfederation().getMembers()
                          : null,
                  Schema.STRING,
                  "Set of autonomous system numbers visible only within this BGP confederation"))
          .build();

  /** Returns the property descriptor for {@code property} */
  public static PropertyDescriptor<BgpProcess> getPropertyDescriptor(String property) {
    checkArgument(JAVA_MAP.containsKey(property), "Property " + property + " does not exist");
    return JAVA_MAP.get(property);
  }

  /** A {@link BgpProcessPropertySpecifier} that matches all BGP properties. */
  public static final BgpProcessPropertySpecifier ALL =
      new BgpProcessPropertySpecifier(JAVA_MAP.keySet());

  private final @Nonnull List<String> _properties;

  /**
   * Create a bgp process property specifier from provided expression. If the expression is null or
   * empty, a specifier with all properties is returned.
   */
  public static BgpProcessPropertySpecifier create(@Nullable String expression) {
    return new BgpProcessPropertySpecifier(
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                expression,
                Grammar.BGP_PROCESS_PROPERTY_SPECIFIER,
                new ConstantEnumSetSpecifier<>(JAVA_MAP.keySet()))
            .resolve());
  }

  /** Returns a specifier that maps to all properties in {@code properties} */
  public BgpProcessPropertySpecifier(Set<String> properties) {
    Set<String> diffSet = Sets.difference(properties, JAVA_MAP.keySet());
    checkArgument(
        diffSet.isEmpty(),
        "Invalid properties supplied: %s. Valid properties are %s",
        diffSet,
        JAVA_MAP.keySet());
    _properties = properties.stream().sorted().collect(ImmutableList.toImmutableList());
  }

  @Override
  public @Nonnull List<String> getMatchingProperties() {
    return _properties;
  }

  /**
   * Returns {@code true} iff the given process has any BGP peer configured as a route reflector
   * client, for IPv4 unicast family.
   */
  public static boolean isIpv4UnicastRouteReflector(BgpProcess p) {
    return StreamSupport.stream(p.getAllPeerConfigs().spliterator(), false)
        .map(BgpPeerConfig::getIpv4UnicastAddressFamily)
        .filter(Objects::nonNull)
        .anyMatch(AddressFamily::getRouteReflectorClient);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BgpProcessPropertySpecifier)) {
      return false;
    }
    return _properties.equals(((BgpProcessPropertySpecifier) o)._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_properties);
  }
}
