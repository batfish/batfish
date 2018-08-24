package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.Schema;

/**
 * Enables specification a set of Ospf process properties.
 *
 * <p>Example specifiers:
 *
 * <ul>
 *   <li>multipath-ebgp —> gets the process's corresponding value
 *   <li>multipath-.* --> gets all properties that start with 'max-metric-'
 * </ul>
 */
public class BgpPropertySpecifier extends PropertySpecifier {

  public static final String ACTIVE_NEIGHBORS = "Active_Neighbors";
  public static final String CLUSTER_IDS = "Cluster_IDs";
  public static final String GENERATED_ROUTES = "Generated_Routes";
  public static final String MULTIPATH_EQUIVALENT_AS_PATH_MATCH_MODE =
      "Multipath_Equivalent_AS_Path_Match_Mode";
  public static final String MULTIPATH_EBGP = "Multipath_EBGP";
  public static final String MULTIPATH_IBGP = "Multipath_IBGP";
  public static final String ORIGINATION_SPACE = "Origination_Space";
  public static final String PASSIVE_NEIGHBORS = "Passive_Neighbors";
  public static final String TIE_BREAKER = "Tie_Breaker";

  public static Map<String, PropertyDescriptor<BgpProcess>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<BgpProcess>>()
          .put(
              ACTIVE_NEIGHBORS,
              new PropertyDescriptor<>(BgpProcess::getActiveNeighbors, Schema.set(Schema.STRING)))
          .put(
              CLUSTER_IDS,
              new PropertyDescriptor<>(BgpProcess::getClusterIds, Schema.set(Schema.STRING)))
          .put(
              GENERATED_ROUTES,
              new PropertyDescriptor<>(BgpProcess::getGeneratedRoutes, Schema.set(Schema.STRING)))
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
              ORIGINATION_SPACE,
              new PropertyDescriptor<>(BgpProcess::getOriginationSpace, Schema.STRING))
          .put(
              PASSIVE_NEIGHBORS,
              new PropertyDescriptor<>(BgpProcess::getPassiveNeighbors, Schema.set(Schema.STRING)))
          // skip router-id; included as part of process identity
          .put(TIE_BREAKER, new PropertyDescriptor<>(BgpProcess::getTieBreaker, Schema.STRING))
          .build();

  public static final BgpPropertySpecifier ALL = new BgpPropertySpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public BgpPropertySpecifier(String expression) {
    _expression = expression;
    _pattern = Pattern.compile(_expression.trim().toLowerCase()); // canonicalize
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
}
