package org.batfish.vendor.a10.representation;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Set;
import org.batfish.vendor.StructureType;

/** Named structure-types for A10 devices */
public enum A10StructureType implements StructureType {
  INTERFACE("interface"),
  SERVER("server"),
  SERVICE_GROUP("service-group"),
  VIRTUAL_SERVER("virtual-server"),
  VRRP_A_FAIL_OVER_POLICY_TEMPLATE("vrrp-a fail-over-policy-template"),
  VRRP_A_VRID("vrrp-a vrid");

  // TODO add abstract structures to this map if/when they exist
  public static final Multimap<A10StructureType, A10StructureType> ABSTRACT_STRUCTURES =
      ImmutableListMultimap.<A10StructureType, A10StructureType>builder().build();

  public static final Set<A10StructureType> CONCRETE_STRUCTURES =
      ImmutableSet.copyOf(
          Sets.difference(ImmutableSet.copyOf(values()), ABSTRACT_STRUCTURES.keySet()));

  private final String _description;

  A10StructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
