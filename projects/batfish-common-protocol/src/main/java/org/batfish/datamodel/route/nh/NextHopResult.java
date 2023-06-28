package org.batfish.datamodel.route.nh;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The base type for a next-hop in a route that is produced by a Batfish question. The {@link
 * NextHop} subtype is the base type for all concrete next hops, and the other subtypes are symbolic
 * representations of next hops that can be produced by the {@link
 * org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion} question.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = NextHop.class),
  @JsonSubTypes.Type(value = NextHopSelf.class),
  @JsonSubTypes.Type(value = NextHopBgpPeerAddress.class)
})
public interface NextHopResult {}
