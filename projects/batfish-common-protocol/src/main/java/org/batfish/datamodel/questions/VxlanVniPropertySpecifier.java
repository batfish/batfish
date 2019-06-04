package org.batfish.datamodel.questions;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.answers.Schema;

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

  public static Map<String, PropertyDescriptor<VxlanVniPropertiesRow>> JAVA_MAP =
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

  public static final VxlanVniPropertySpecifier ALL = new VxlanVniPropertySpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public VxlanVniPropertySpecifier(@Nullable String expression) {
    _expression = firstNonNull(expression, ".*");
    _pattern = Pattern.compile(_expression.trim(), Pattern.CASE_INSENSITIVE);
  }

  public VxlanVniPropertySpecifier(Collection<String> properties) {
    _expression =
        properties.stream().map(String::trim).map(Pattern::quote).collect(Collectors.joining("|"));
    _pattern = Pattern.compile(_expression, Pattern.CASE_INSENSITIVE);
  }

  @Override
  public @Nonnull List<String> getMatchingProperties() {
    return JAVA_MAP.keySet().stream()
        .filter(prop -> _pattern.matcher(prop).matches())
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
