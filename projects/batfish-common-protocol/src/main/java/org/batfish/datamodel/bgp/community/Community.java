package org.batfish.datamodel.bgp.community;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/**
 * Represents a BGP community value, which could be <a
 * href="https://tools.ietf.org/html/rfc1997">standard</a>, <a
 * href="https://tools.ietf.org/html/rfc4360">extended </a> or <a
 * href="https://tools.ietf.org/html/rfc8092>large</a>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = StandardCommunity.class, name = "standard"),
  @JsonSubTypes.Type(value = ExtendedCommunity.class, name = "extended"),
  @JsonSubTypes.Type(value = LargeCommunity.class, name = "large")
})
public interface Community extends Serializable {

  /**
   * Whether this community is transitive (can traverse from autonomous system to autonomous system)
   */
  boolean isTransitive();

  /** Return a string representation of the community suitable for regex matching. */
  String matchString();

  /** Return a string representation of the community in canonical form. */
  @Override
  String toString();
}
