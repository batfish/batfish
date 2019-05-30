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
  public static final String EXPORT_POLICY_SOURCES = "Export_Policy_Sources";
  public static final String MAX_METRIC_EXTERNAL_NETWORKS = "Max_Metric_External_Networks";
  public static final String MAX_METRIC_STUB_NETWORKS = "Max_Metric_Stub_Networks";
  public static final String MAX_METRIC_SUMMARY_NETWORKS = "Max_Metric_Summary_Networks";
  public static final String MAX_METRIC_TRANSIT_LINKS = "Max_Metric_Transit_Links";
  public static final String REFERENCE_BANDWIDTH = "Reference_Bandwidth";
  public static final String RFC_1583_COMPATIBLE = "RFC1583_Compatible";
  public static final String ROUTER_ID = "Router_ID";

  public static Map<String, PropertyDescriptor<OspfProcess>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<OspfProcess>>()
          .put(
              AREA_BORDER_ROUTER,
              new PropertyDescriptor<>(
                  OspfProcess::isAreaBorderRouter,
                  Schema.BOOLEAN,
                  "Whether this process is at the area border (with at least one interface in Area 0 and one in another area)"))
          // will go from Long to String --> area Ids are not integral anyway
          .put(
              AREAS,
              new PropertyDescriptor<>(
                  OspfProcess::getAreas,
                  Schema.set(Schema.STRING),
                  "All OSPF areas for this process"))
          .put(
              EXPORT_POLICY_SOURCES,
              new PropertyDescriptor<>(
                  OspfProcess::getExportPolicySources,
                  Schema.set(Schema.STRING),
                  "Names of policies that determine which routes are exported into OSPF"))
          // All max-metrics go from Long to String
          .put(
              MAX_METRIC_EXTERNAL_NETWORKS,
              new PropertyDescriptor<>(
                  OspfProcess::getMaxMetricExternalNetworks,
                  Schema.INTEGER,
                  "Max OSPF metric for external networks"))
          .put(
              MAX_METRIC_STUB_NETWORKS,
              new PropertyDescriptor<>(
                  OspfProcess::getMaxMetricStubNetworks,
                  Schema.INTEGER,
                  "Max OSPF metric for stub networks"))
          .put(
              MAX_METRIC_SUMMARY_NETWORKS,
              new PropertyDescriptor<>(
                  OspfProcess::getMaxMetricSummaryNetworks,
                  Schema.INTEGER,
                  "Max OSPF metric for summary networks"))
          .put(
              MAX_METRIC_TRANSIT_LINKS,
              new PropertyDescriptor<>(
                  OspfProcess::getMaxMetricTransitLinks,
                  Schema.INTEGER,
                  "Max OSPF metric for transit links"))
          .put(
              REFERENCE_BANDWIDTH,
              new PropertyDescriptor<>(
                  OspfProcess::getReferenceBandwidth,
                  Schema.DOUBLE,
                  "Reference bandwidth in bits/s used to calculate interface OSPF cost"))
          .put(
              RFC_1583_COMPATIBLE,
              new PropertyDescriptor<>(
                  OspfProcess::getRfc1583Compatible,
                  Schema.BOOLEAN,
                  "Whether the process is compatible with RFC 1583 (OSPF v2)"))
          .put(
              ROUTER_ID,
              new PropertyDescriptor<>(
                  OspfProcess::getRouterId, Schema.IP, "Router ID of the process"))
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
