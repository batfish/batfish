package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.answers.Schema;
import org.batfish.specifier.ConstantEnumSetSpecifier;
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

  public static final String MULTIPATH_EQUIVALENT_AS_PATH_MATCH_MODE = "Multipath_Match_Mode";
  public static final String MULTIPATH_EBGP = "Multipath_EBGP";
  public static final String MULTIPATH_IBGP = "Multipath_IBGP";
  public static final String NEIGHBORS = "Neighbors";
  public static final String ROUTE_REFLECTOR = "Route_Reflector";
  public static final String TIE_BREAKER = "Tie_Breaker";

  public static final Map<String, PropertyDescriptor<BgpProcess>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<BgpProcess>>()
          .put(
              ROUTE_REFLECTOR,
              new PropertyDescriptor<>(
                  BgpProcessPropertySpecifier::isRouteReflector,
                  Schema.BOOLEAN,
                  "Whether any BGP peer in this process is configured as a route reflector client"))
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
                  "All peers configured on this process, identified by peer address (for active and dynamic peers) or peer interface (for BGP unnumbered peers)"))
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
          .build();

  /** A {@link BgpProcessPropertySpecifier} that matches all BGP properties. */
  public static final BgpProcessPropertySpecifier ALL =
      new BgpProcessPropertySpecifier(JAVA_MAP.keySet());

  @Nonnull private final List<String> _properties;

  /**
   * Create a bgp process property specifier from provided expression. If the expression is null or
   * empty, a specifier with all properties is returned.
   */
  public static BgpProcessPropertySpecifier create(@Nullable String expression) {
    return new BgpProcessPropertySpecifier(
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                expression, JAVA_MAP.keySet(), new ConstantEnumSetSpecifier<>(JAVA_MAP.keySet()))
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

  @Nonnull
  @Override
  public List<String> getMatchingProperties() {
    return _properties;
  }

  /**
   * Returns {@code true} iff the given process has any BGP peer configured as a route reflector
   * client.
   */
  @VisibleForTesting
  static boolean isRouteReflector(BgpProcess p) {
    return p.getActiveNeighbors().values().stream()
            .anyMatch(BgpActivePeerConfig::getRouteReflectorClient)
        || p.getPassiveNeighbors().values().stream()
            .anyMatch(BgpPassivePeerConfig::getRouteReflectorClient);
  }
}
