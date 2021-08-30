package org.batfish.vendor.check_point_management;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.io.Serializable;

/** A member of an {@link AccessLayer}. */
@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = AccessRule.class, name = "access-rule"),
  @JsonSubTypes.Type(value = AccessSection.class, name = "access-section")
})
public interface AccessRuleOrSection extends Serializable {}
