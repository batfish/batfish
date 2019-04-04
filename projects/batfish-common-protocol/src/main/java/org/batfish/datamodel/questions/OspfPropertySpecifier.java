package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

  public static final String AREA_BORDER_ROUTER = "Area_Border_Router";
  public static final String AREAS = "Areas";
  public static final String EXPORT_POLICY = "Export_Policy";
  public static final String EXPORT_POLICY_SOURCES = "Export_Policy_Sources";
  public static final String GENERATED_ROUTES = "Generated_Routes";
  public static final String MAX_METRIC_EXTERNAL_NETWORKS = "Max_Metric_External_Networks";
  public static final String MAX_METRIC_STUB_NETWORKS = "Max_Metric_Stub_Networks";
  public static final String MAX_METRIC_SUMMARY_NETWORKS = "Max_Metric_Summary_Networks";
  public static final String MAX_METRIC_TRANSIT_LINKS = "Max_Metric_Transit_Links";
  public static final String NEIGHBORS = "Neighbors";
  public static final String REFERENCE_BANDWIDTH = "Reference_Bandwidth";
  public static final String RFC_1583_COMPATIBLE = "RFC1583_Compatible";
  public static final String ROUTER_ID = "Router_ID";

  public static Map<String, PropertyDescriptor<OspfProcess>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<OspfProcess>>()
          .put(
              AREA_BORDER_ROUTER,
              new PropertyDescriptor<>(OspfProcess::isAreaBorderRouter, Schema.BOOLEAN))
          // will go from Long to String --> area Ids are not integral anyway
          .put(AREAS, new PropertyDescriptor<>(OspfProcess::getAreas, Schema.set(Schema.STRING)))
          .put(EXPORT_POLICY, new PropertyDescriptor<>(OspfProcess::getExportPolicy, Schema.STRING))
          .put(
              EXPORT_POLICY_SOURCES,
              new PropertyDescriptor<>(
                  OspfProcess::getExportPolicySources, Schema.set(Schema.STRING)))
          .put(
              GENERATED_ROUTES,
              new PropertyDescriptor<>(OspfProcess::getGeneratedRoutes, Schema.set(Schema.STRING)))
          // All max-metrics go from Long to String
          .put(
              MAX_METRIC_EXTERNAL_NETWORKS,
              new PropertyDescriptor<>(OspfProcess::getMaxMetricExternalNetworks, Schema.INTEGER))
          .put(
              MAX_METRIC_STUB_NETWORKS,
              new PropertyDescriptor<>(OspfProcess::getMaxMetricStubNetworks, Schema.INTEGER))
          .put(
              MAX_METRIC_SUMMARY_NETWORKS,
              new PropertyDescriptor<>(OspfProcess::getMaxMetricSummaryNetworks, Schema.INTEGER))
          .put(
              MAX_METRIC_TRANSIT_LINKS,
              new PropertyDescriptor<>(OspfProcess::getMaxMetricTransitLinks, Schema.INTEGER))
          .put(
              REFERENCE_BANDWIDTH,
              new PropertyDescriptor<>(OspfProcess::getReferenceBandwidth, Schema.DOUBLE))
          .put(
              RFC_1583_COMPATIBLE,
              new PropertyDescriptor<>(OspfProcess::getRfc1583Compatible, Schema.BOOLEAN))
          .put(ROUTER_ID, new PropertyDescriptor<>(OspfProcess::getRouterId, Schema.IP))
          .build();

  public static final OspfPropertySpecifier ALL = new OspfPropertySpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public OspfPropertySpecifier(String expression) {
    _expression = expression;
    _pattern = Pattern.compile(_expression.trim().toLowerCase()); // canonicalize
  }

  @JsonCreator
  public OspfPropertySpecifier(Collection<String> properties) {
    // quote and join
    _expression =
        properties.stream().map(String::trim).map(Pattern::quote).collect(Collectors.joining("|"));
    _pattern = Pattern.compile(_expression, Pattern.CASE_INSENSITIVE);
  }

  @Override
  public List<String> getMatchingProperties() {
    return JAVA_MAP.keySet().stream()
        .filter(prop -> _pattern.matcher(prop.toLowerCase()).matches())
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
