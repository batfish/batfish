package org.batfish.vendor.cisco_aci.representation;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Set;
import org.batfish.vendor.StructureType;

/** Named structure-types for Cisco ACI devices */
public enum AciStructureType implements StructureType {
  // Concrete structure types - ACI objects that are extracted
  BRIDGE_DOMAIN("Bridge Domain"),
  CONTRACT("Contract"),
  EPG("Endpoint Group"),
  FABRIC_NODE("Fabric Node"),
  INTERFACE("Interface"),
  L3_OUT("L3 External Network"),
  TENANT("Tenant"),
  VRF("VRF"),

  // Abstract structure types - things that may be referenced but not directly extracted
  CONTRACT_SUBJECT("Contract Subject"),
  CONTRACT_FILTER("Contract Filter"),
  ENDPOINT("Endpoint"),
  L3_EXT_EPG("L3 External EPG"),
  NODE_PATH("Node Path"),
  PATH_ATTACHMENT("Path Attachment"),
  SUBNET("Subnet");

  public static final Multimap<AciStructureType, AciStructureType> ABSTRACT_STRUCTURES =
      ImmutableListMultimap.<AciStructureType, AciStructureType>builder()
          .putAll(CONTRACT_SUBJECT, CONTRACT)
          .putAll(CONTRACT_FILTER, CONTRACT_SUBJECT)
          .putAll(ENDPOINT, EPG)
          .putAll(L3_EXT_EPG, L3_OUT)
          .putAll(NODE_PATH, INTERFACE)
          .putAll(PATH_ATTACHMENT, EPG)
          .putAll(PATH_ATTACHMENT, INTERFACE)
          .putAll(SUBNET, BRIDGE_DOMAIN)
          .build();

  public static final Set<AciStructureType> CONCRETE_STRUCTURES =
      ImmutableSet.copyOf(
          Sets.difference(ImmutableSet.copyOf(values()), ABSTRACT_STRUCTURES.keySet()));

  private final String _description;

  AciStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
