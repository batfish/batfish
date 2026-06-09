package org.batfish.vendor.sros.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.vendor.StructureType;

/** Named structures defined in a Nokia SR-OS configuration that may be referenced elsewhere. */
public enum SrosStructureType implements StructureType {
  BGP_GROUP("bgp group"),
  COMMUNITY("community"),
  POLICY_STATEMENT("policy-statement"),
  PREFIX_LIST("prefix-list");

  /** All concrete structure types, for {@code markConcreteStructure} at the end of conversion. */
  public static final List<SrosStructureType> CONCRETE_STRUCTURES =
      ImmutableList.of(BGP_GROUP, COMMUNITY, POLICY_STATEMENT, PREFIX_LIST);

  private final String _description;

  SrosStructureType(String description) {
    _description = description;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
