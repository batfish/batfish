package org.batfish.datamodel.route.nh;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = NextHop.class),
  @JsonSubTypes.Type(value = NextHopSelf.class),
  @JsonSubTypes.Type(value = NextHopBgpPeerAddress.class)
})
public interface NextHopResult {}
