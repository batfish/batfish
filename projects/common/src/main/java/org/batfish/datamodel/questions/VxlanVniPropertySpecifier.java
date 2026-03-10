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
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.Schema;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.SpecifierFactories;

/**
 * Enables specification of a set of VXLAN VNI properties.
 *
 * <p>Currently supported example specifiers:
 *
 * <ul>
 *   <li>vlan gets the VLAN ID property
 *   <li>.*vtep.* gets all properties that include 'VTEP'
 * </ul>
 */
@ParametersAreNonnullByDefault
public class VxlanVniPropertySpecifier extends PropertySpecifier {

  public static final String NODE = "Node";
  public static final String LOCAL_VTEP_IP = "Local_VTEP_IP";
  public static final String MULTICAST_GROUP = "Multicast_Group";
  public static final String VLAN = "VLAN";
  public static final String VNI = "VNI";
  public static final String VTEP_FLOOD_LIST = "VTEP_Flood_List";
  public static final String VXLAN_PORT = "VXLAN_Port";

  private static final Map<String, PropertyDescriptor<VxlanVniPropertiesRow>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<VxlanVniPropertiesRow>>()
          .put(
              LOCAL_VTEP_IP,
              new PropertyDescriptor<>(
                  VxlanVniPropertiesRow::getLocalVtepIp,
                  Schema.IP,
                  "IPv4 address of the local VTEP"))
          .put(
              MULTICAST_GROUP,
              new PropertyDescriptor<>(
                  VxlanVniPropertiesRow::getMulticastGroup,
                  Schema.IP,
                  "IPv4 address of the multicast group"))
          .put(
              VLAN,
              new PropertyDescriptor<>(
                  VxlanVniPropertiesRow::getVlan, Schema.INTEGER, "VLAN number for the VNI"))
          .put(
              VTEP_FLOOD_LIST,
              new PropertyDescriptor<>(
                  VxlanVniPropertiesRow::getVtepFloodList,
                  Schema.list(Schema.IP),
                  "All IPv4 addresses in the VTEP flood list"))
          .put(
              VXLAN_PORT,
              new PropertyDescriptor<>(
                  VxlanVniPropertiesRow::getVxlanPort,
                  Schema.INTEGER,
                  "Destination port number for the VXLAN tunnel"))
          .build();

  /** Includes all properties */
  public static final VxlanVniPropertySpecifier ALL =
      new VxlanVniPropertySpecifier(JAVA_MAP.keySet());

  /** Returns the property descriptor for {@code property} */
  public static PropertyDescriptor<VxlanVniPropertiesRow> getPropertyDescriptor(String property) {
    checkArgument(JAVA_MAP.containsKey(property), "Property " + property + " does not exist");
    return JAVA_MAP.get(property);
  }

  private final @Nonnull List<String> _properties;

  public static VxlanVniPropertySpecifier create(@Nullable String expression) {
    return new VxlanVniPropertySpecifier(
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                expression,
                Grammar.VXLAN_VNI_PROPERTY_SPECIFIER,
                new ConstantEnumSetSpecifier<>(JAVA_MAP.keySet()))
            .resolve());
  }

  public VxlanVniPropertySpecifier(Set<String> properties) {
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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof VxlanVniPropertySpecifier)) {
      return false;
    }
    return _properties.equals(((VxlanVniPropertySpecifier) o)._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_properties);
  }
}
