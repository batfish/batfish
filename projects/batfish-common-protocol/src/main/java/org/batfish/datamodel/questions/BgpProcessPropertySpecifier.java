package org.batfish.datamodel.questions;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.Schema;

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
                  BgpProcessPropertySpecifier::isRouteReflector, Schema.BOOLEAN))
          .put(
              MULTIPATH_EQUIVALENT_AS_PATH_MATCH_MODE,
              new PropertyDescriptor<>(
                  BgpProcess::getMultipathEquivalentAsPathMatchMode, Schema.STRING))
          .put(
              MULTIPATH_EBGP,
              new PropertyDescriptor<>(BgpProcess::getMultipathEbgp, Schema.BOOLEAN))
          .put(
              MULTIPATH_IBGP,
              new PropertyDescriptor<>(BgpProcess::getMultipathIbgp, Schema.BOOLEAN))
          .put(
              NEIGHBORS,
              new PropertyDescriptor<>(
                  (process) ->
                      Iterables.concat(
                          process.getActiveNeighbors().keySet(),
                          process.getPassiveNeighbors().keySet()),
                  Schema.set(Schema.STRING)))
          // skip router-id; included as part of process identity
          .put(TIE_BREAKER, new PropertyDescriptor<>(BgpProcess::getTieBreaker, Schema.STRING))
          .build();

  /** A {@link BgpProcessPropertySpecifier} that matches all BGP properties. */
  public static final BgpProcessPropertySpecifier ALL = new BgpProcessPropertySpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public BgpProcessPropertySpecifier(@Nullable String expression) {
    _expression = firstNonNull(expression, ".*");
    _pattern = Pattern.compile(_expression.trim().toLowerCase()); // canonicalize
  }

  /** Returns a specifier that maps to all properties in {@code properties} */
  public BgpProcessPropertySpecifier(Collection<String> properties) {
    // quote and join
    _expression =
        properties.stream().map(String::trim).map(Pattern::quote).collect(Collectors.joining("|"));
    _pattern = Pattern.compile(_expression, Pattern.CASE_INSENSITIVE);
  }

  /**
   * Returns a list of suggestions based on the query, based on {@link
   * PropertySpecifier#baseAutoComplete}.
   */
  public static List<AutocompleteSuggestion> autoComplete(String query) {
    return PropertySpecifier.baseAutoComplete(query, JAVA_MAP.keySet());
  }

  @Override
  public Set<String> getMatchingProperties() {
    return JAVA_MAP
        .keySet()
        .stream()
        .filter(prop -> _pattern.matcher(prop.toLowerCase()).matches())
        .collect(Collectors.toSet());
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }

  /**
   * Returns {@code true} iff the given process has any BGP peer configured as a route reflector
   * client.
   */
  @VisibleForTesting
  static boolean isRouteReflector(BgpProcess p) {
    return p.getActiveNeighbors()
            .values()
            .stream()
            .anyMatch(BgpActivePeerConfig::getRouteReflectorClient)
        || p.getPassiveNeighbors()
            .values()
            .stream()
            .anyMatch(BgpPassivePeerConfig::getRouteReflectorClient);
  }
}
