package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The base type for a next-hop in a route that is produced by a Batfish question (see {@link
 * org.batfish.datamodel.questions.BgpRoute}).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = NextHopConcrete.class, name = "concrete"),
  @JsonSubTypes.Type(value = NextHopBgpPeerAddress.class, name = "peer"),
  @JsonSubTypes.Type(value = NextHopSelf.class, name = "self"),
})
public interface NextHopResult {}
