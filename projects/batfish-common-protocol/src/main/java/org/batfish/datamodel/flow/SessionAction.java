package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.batfish.datamodel.visitors.SessionActionVisitor;

/** An action that a firewall session takes for return traffic matching the session */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = Accept.class, name = "Accept"),
  @JsonSubTypes.Type(value = PreNatFibLookup.class, name = "PreNatFibLookup"),
  @JsonSubTypes.Type(value = PostNatFibLookup.class, name = "PostNatFibLookup"),
  @JsonSubTypes.Type(value = ForwardOutInterface.class, name = "ForwardOutInterface"),
})
public interface SessionAction {
  <T> T accept(SessionActionVisitor<T> visitor);
}
