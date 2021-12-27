package org.batfish.datamodel.flow;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DelegatedToNextVrf.class, name = "DelegatedToNextVrf"),
  @JsonSubTypes.Type(value = ForwardedIntoVxlanTunnel.class, name = "ForwardedIntoVxlanTunnel"),
  @JsonSubTypes.Type(value = ForwardedOutInterface.class, name = "ForwardedOutInterface"),
  @JsonSubTypes.Type(value = Discarded.class, name = "Discarded"),
})
public interface ForwardingDetail {}
