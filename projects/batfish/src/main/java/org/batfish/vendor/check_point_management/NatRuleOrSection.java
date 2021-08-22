package org.batfish.vendor.check_point_management;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.io.Serializable;

/** A member of a {@link NatRulebase}. */
@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = NatRule.class, name = "nat-rule"),
  @JsonSubTypes.Type(value = NatSection.class, name = "nat-section")
})
public interface NatRuleOrSection extends Serializable {}
