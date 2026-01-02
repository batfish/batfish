package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.SpecifierFactories;

/** Enables specification of a set of Ospf interface properties. */
public class OspfInterfacePropertySpecifier extends PropertySpecifier {

  // OSPF interface properties are a subset of interface properties
  public static final String OSPF_AREA_NAME = "OSPF_Area_Name";
  public static final String OSPF_ENABLED = "OSPF_Enabled";
  public static final String OSPF_PASSIVE = "OSPF_Passive";
  public static final String OSPF_COST = "OSPF_Cost";
  public static final String OSPF_NETWORK_TYPE = "OSPF_Network_Type";
  public static final String OSPF_DEAD_INTERVAL = "OSPF_Dead_Interval";
  public static final String OSPF_HELLO_INTERVAL = "OSPF_Hello_Interval";

  // create an ordered list
  public static final List<String> PROPERTIES =
      ImmutableList.of(
          OSPF_AREA_NAME,
          OSPF_ENABLED,
          OSPF_PASSIVE,
          OSPF_COST,
          OSPF_NETWORK_TYPE,
          OSPF_HELLO_INTERVAL,
          OSPF_DEAD_INTERVAL);

  /** Hold a map of property name to property descriptor for OSPF interface properties */
  @VisibleForTesting
  static final Map<String, PropertyDescriptor<OspfInterfaceSettings>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<OspfInterfaceSettings>>()
          .put(
              OSPF_AREA_NAME,
              new PropertyDescriptor<>(
                  OspfInterfaceSettings::getAreaName,
                  Schema.LONG,
                  "OSPF area to which the interface belongs"))
          .put(
              OSPF_COST,
              new PropertyDescriptor<>(
                  OspfInterfaceSettings::getCost,
                  Schema.INTEGER,
                  "OSPF cost if explicitly configured"))
          .put(
              OSPF_DEAD_INTERVAL,
              new PropertyDescriptor<>(
                  OspfInterfaceSettings::getDeadInterval,
                  Schema.INTEGER,
                  "Interval in seconds before a silent OSPF neighbor is declared dead"))
          .put(
              OSPF_ENABLED,
              new PropertyDescriptor<>(
                  OspfInterfaceSettings::getEnabled, Schema.BOOLEAN, "Whether OSPF is enabled"))
          .put(
              OSPF_HELLO_INTERVAL,
              new PropertyDescriptor<>(
                  OspfInterfaceSettings::getHelloInterval,
                  Schema.INTEGER,
                  "Interval in seconds between sending OSPF hello messages"))
          .put(
              OSPF_PASSIVE,
              new PropertyDescriptor<>(
                  OspfInterfaceSettings::getPassive,
                  Schema.BOOLEAN,
                  "Whether interface is in OSPF passive mode"))
          .put(
              OSPF_NETWORK_TYPE,
              new PropertyDescriptor<>(
                  OspfInterfaceSettings::getNetworkType,
                  Schema.STRING,
                  "Type of OSPF network associated with the interface"))
          .build();

  /** Holds all properties */
  public static final OspfInterfacePropertySpecifier ALL =
      new OspfInterfacePropertySpecifier(JAVA_MAP.keySet());

  /** Returns the property descriptor for {@code property} */
  public static PropertyDescriptor<OspfInterfaceSettings> getPropertyDescriptor(String property) {
    checkArgument(JAVA_MAP.containsKey(property), "Property " + property + " does not exist");
    return JAVA_MAP.get(property);
  }

  private final @Nonnull List<String> _properties;

  /**
   * Create an ospf process property specifier from provided expression. If the expression is null
   * or empty, a specifier with all properties is returned.
   */
  public static OspfInterfacePropertySpecifier create(@Nullable String expression) {
    return new OspfInterfacePropertySpecifier(
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                expression,
                Grammar.OSPF_INTERFACE_PROPERTY_SPECIFIER,
                new ConstantEnumSetSpecifier<>(ImmutableSet.copyOf(PROPERTIES)))
            .resolve());
  }

  public OspfInterfacePropertySpecifier(Set<String> properties) {
    Set<String> diffSet = Sets.difference(properties, ImmutableSet.copyOf(PROPERTIES));
    checkArgument(
        diffSet.isEmpty(),
        "Invalid properties supplied: %s. Valid properties are %s",
        diffSet,
        PROPERTIES);
    // we do this to maintain order
    _properties =
        PROPERTIES.stream().filter(properties::contains).collect(ImmutableList.toImmutableList());
  }

  @Override
  public List<String> getMatchingProperties() {
    return _properties;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof OspfInterfacePropertySpecifier)) {
      return false;
    }
    return _properties.equals(((OspfInterfacePropertySpecifier) o)._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_properties);
  }
}
