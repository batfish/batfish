package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.Schema;

/**
 * Enables specification of a set of VXLAN VNI properties.
 *
 * <p>Currently supported example specifiers:
 *
 * <ul>
 *   <li>vlan gets the VLAN ID property
 * </ul>
 */
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
              new PropertyDescriptor<>(VxlanVniPropertiesRow::getLocalVtepIp, Schema.IP))
          .put(
              MULTICAST_GROUP,
              new PropertyDescriptor<>(VxlanVniPropertiesRow::getMulticastGroup, Schema.IP))
          .put(VLAN, new PropertyDescriptor<>(VxlanVniPropertiesRow::getVlan, Schema.INTEGER))
          .put(
              VTEP_FLOOD_LIST,
              new PropertyDescriptor<>(
                  VxlanVniPropertiesRow::getVtepFloodList, Schema.list(Schema.IP)))
          .put(
              VXLAN_PORT,
              new PropertyDescriptor<>(VxlanVniPropertiesRow::getVxlanPort, Schema.INTEGER))
          .build();

  public static final VxlanVniPropertySpecifier ALL = new VxlanVniPropertySpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public VxlanVniPropertySpecifier(String expression) {
    _expression = expression;
    _pattern = Pattern.compile(_expression.trim(), Pattern.CASE_INSENSITIVE);
  }

  public VxlanVniPropertySpecifier(Collection<String> properties) {
    _expression =
        properties.stream().map(String::trim).map(Pattern::quote).collect(Collectors.joining("|"));
    _pattern = Pattern.compile(_expression, Pattern.CASE_INSENSITIVE);
  }

  /** Returns a list of suggestions based on the query, based on {@link PropertySpecifier}. */
  public static List<AutocompleteSuggestion> autoComplete(String query) {
    return PropertySpecifier.baseAutoComplete(query, JAVA_MAP.keySet());
  }

  @Override
  public List<String> getMatchingProperties() {
    return JAVA_MAP
        .keySet()
        .stream()
        .filter(prop -> _pattern.matcher(prop).matches())
        .collect(Collectors.toList());
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
