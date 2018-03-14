package org.batfish.datamodel;

import java.util.Map;
import java.util.Set;

public interface ArpAnalysis {

  /** Mapping: hostname -> inInterface -> ipsToArpReplyTo */
  Map<String, Map<String, IpSpace>> getArpReplies();

  /**
   * Mapping: hostname -> vrfName -> route -> outInterface -> arpIpChoice ->
   * dstIpsForWhichSomeoneSendsArpReply
   */
  Map<String, Map<String, Map<AbstractRoute, Map<String, Map<ArpIpChoice, Set<IpSpace>>>>>>
      getArpRequests();

  /** Mapping: edge -> dstIpsForWhichArpReplySent */
  Map<Edge, IpSpace> getArpTrueEdge();

  /** Mapping: hostname -> vrfName -> outInterface -> dstIpsForWhichNoArpResponse */
  Map<String, Map<String, Map<String, IpSpace>>> getNeighborUnreachable();
}
