package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.SpecifierFactories;

/** Enables specification of a set of Ospf process properties. */
public class OspfProcessPropertySpecifier extends PropertySpecifier {

  public static final String AREA_BORDER_ROUTER = "Area_Border_Router";
  public static final String AREAS = "Areas";
  public static final String EXPORT_POLICY_SOURCES = "Export_Policy_Sources";
  public static final String REFERENCE_BANDWIDTH = "Reference_Bandwidth";
  public static final String RFC_1583_COMPATIBLE = "RFC1583_Compatible";
  public static final String ROUTER_ID = "Router_ID";

  private static final Map<String, PropertyDescriptor<OspfProcess>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<OspfProcess>>()
          .put(
              AREA_BORDER_ROUTER,
              new PropertyDescriptor<>(
                  OspfProcess::isAreaBorderRouter,
                  Schema.BOOLEAN,
                  "Whether this process is at the area border (with at least one interface in Area"
                      + " 0 and one in another area)"))
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
          // skip all max metrics
          .put(
              REFERENCE_BANDWIDTH,
              new PropertyDescriptor<>(
                  OspfProcess::getReferenceBandwidth,
                  Schema.DOUBLE,
                  "Reference bandwidth in bits/sec used to calculate interface OSPF cost"))
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

  /** Holds all properties */
  public static final OspfProcessPropertySpecifier ALL =
      new OspfProcessPropertySpecifier(JAVA_MAP.keySet());

  /** Returns the property descriptor for {@code property} */
  public static PropertyDescriptor<OspfProcess> getPropertyDescriptor(String property) {
    checkArgument(JAVA_MAP.containsKey(property), "Property " + property + " does not exist");
    return JAVA_MAP.get(property);
  }

  private final @Nonnull List<String> _properties;

  /**
   * Create an ospf process property specifier from provided expression. If the expression is null
   * or empty, a specifier with all properties is returned.
   */
  public static OspfProcessPropertySpecifier create(@Nullable String expression) {
    return new OspfProcessPropertySpecifier(
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                expression,
                Grammar.OSPF_PROCESS_PROPERTY_SPECIFIER,
                new ConstantEnumSetSpecifier<>(JAVA_MAP.keySet()))
            .resolve());
  }

  public OspfProcessPropertySpecifier(Set<String> properties) {
    Set<String> diffSet = Sets.difference(properties, JAVA_MAP.keySet());
    checkArgument(
        diffSet.isEmpty(),
        "Invalid properties supplied: %s. Valid properties are %s",
        diffSet,
        JAVA_MAP.keySet());
    _properties = properties.stream().sorted().collect(ImmutableList.toImmutableList());
  }

  @Override
  public List<String> getMatchingProperties() {
    return _properties;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof OspfProcessPropertySpecifier)) {
      return false;
    }
    return _properties.equals(((OspfProcessPropertySpecifier) o)._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_properties);
  }
}
