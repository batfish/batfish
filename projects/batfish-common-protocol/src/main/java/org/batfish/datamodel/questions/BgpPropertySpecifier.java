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

  public static Map<String, PropertyDescriptor<BgpProcess>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<BgpProcess>>()
          .put(
              "Active_Neighbors",
              new PropertyDescriptor<>(BgpProcess::getActiveNeighbors, Schema.set(Schema.STRING)))
          .put(
              "Cluster_Ids",
              new PropertyDescriptor<>(BgpProcess::getClusterIds, Schema.set(Schema.STRING)))
          .put(
              "Generated_Routes",
              new PropertyDescriptor<>(BgpProcess::getGeneratedRoutes, Schema.set(Schema.STRING)))
          .put(
              "Multipath_Equivalent_Aspath_Match_Mode",
              new PropertyDescriptor<>(
                  BgpProcess::getMultipathEquivalentAsPathMatchMode, Schema.STRING))
          .put(
              "Multipath_Ebgp",
              new PropertyDescriptor<>(BgpProcess::getMultipathEbgp, Schema.BOOLEAN))
          .put(
              "Multipath_Ibgp",
              new PropertyDescriptor<>(BgpProcess::getMultipathIbgp, Schema.BOOLEAN))
          .put(
              "Origination_Space",
              new PropertyDescriptor<>(BgpProcess::getOriginationSpace, Schema.STRING))
          .put(
              "Passive_Neighbors",
              new PropertyDescriptor<>(BgpProcess::getPassiveNeighbors, Schema.set(Schema.STRING)))
          // skip router-id; included as part of process identity
          .put("Tie_Breaker", new PropertyDescriptor<>(BgpProcess::getTieBreaker, Schema.STRING))
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
        .filter(prop -> _pattern.matcher(prop).matches())
        .collect(Collectors.toSet());
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
