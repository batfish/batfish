package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.ospf.OspfProcess;

/**
 * Enables specification a set of Ospf process properties.
 *
 * <p>Example specifiers:
 *
 * <ul>
 *   <li>max-metric-summary-links -&gt; gets the process's corresponding value
 *   <li>max-metric-.* -&gt; gets all properties that start with 'max-metric-'
 * </ul>
 */
public class OspfPropertySpecifier extends PropertySpecifier {

  public static Map<String, PropertyDescriptor<OspfProcess>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<OspfProcess>>()
          .put(
              "Area_Border_Router",
              new PropertyDescriptor<>(OspfProcess::isAreaBorderRouter, Schema.BOOLEAN))
          // will go from Long to String --> area Ids are not integral anyway
          .put("Areas", new PropertyDescriptor<>(OspfProcess::getAreas, Schema.set(Schema.STRING)))
          .put(
              "Export_Policy",
              new PropertyDescriptor<>(OspfProcess::getExportPolicy, Schema.STRING))
          .put(
              "Generated_Routes",
              new PropertyDescriptor<>(OspfProcess::getGeneratedRoutes, Schema.set(Schema.STRING)))
          // All max-metrics go from Long to String
          .put(
              "Max_Metric_External_Networks",
              new PropertyDescriptor<>(OspfProcess::getMaxMetricExternalNetworks, Schema.INTEGER))
          .put(
              "Max_Metric_Stub_Networks",
              new PropertyDescriptor<>(OspfProcess::getMaxMetricStubNetworks, Schema.INTEGER))
          .put(
              "Max_Metric_Summary_Networks",
              new PropertyDescriptor<>(OspfProcess::getMaxMetricSummaryNetworks, Schema.INTEGER))
          .put(
              "Max_Metric_Transit_Links",
              new PropertyDescriptor<>(OspfProcess::getMaxMetricTransitLinks, Schema.INTEGER))
          .put(
              "Neighbors",
              new PropertyDescriptor<>(OspfProcess::getOspfNeighbors, Schema.set(Schema.STRING)))
          .put(
              "Reference_Bandwidth",
              new PropertyDescriptor<>(OspfProcess::getReferenceBandwidth, Schema.DOUBLE))
          .put(
              "Rfc1583_Compatible",
              new PropertyDescriptor<>(OspfProcess::getRfc1583Compatible, Schema.BOOLEAN))
          .put("Router_Id", new PropertyDescriptor<>(OspfProcess::getRouterId, Schema.IP))
          .build();

  public static final OspfPropertySpecifier ALL = new OspfPropertySpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public OspfPropertySpecifier(String expression) {
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
