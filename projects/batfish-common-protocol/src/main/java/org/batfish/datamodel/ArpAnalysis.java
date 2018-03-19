package org.batfish.datamodel;

import java.util.Map;

public interface ArpAnalysis {

  /** Mapping: hostname -> inInterface -> ipsToArpReplyTo */
  Map<String, Map<String, IpSpace>> getArpReplies();

  /** Mapping: edge -> dstIpsForWhichArpReplySent */
  Map<Edge, IpSpace> getArpTrueEdge();

  /** Mapping: hostname -> vrfName -> outInterface -> dstIpsForWhichNoArpResponse */
  Map<String, Map<String, Map<String, IpSpace>>> getNeighborUnreachable();

  /**
   * Mapping: hostname -> vrfName -> nullableIps <br>
   * A nullable IP is a destination IP for which there is a longest-prefix-match route that discards
   * the packet rather than forwarding it out some interface.
   */
  Map<String, Map<String, IpSpace>> getNullableIps();

  /**
   * Mapping: hostname -> vrfName -> routableIps <br>
   * A routable IP is a destination IP for which there is a longest-prefix-match route.
   */
  Map<String, Map<String, IpSpace>> getRoutableIps();
}
