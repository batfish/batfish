package org.batfish.datamodel;

import java.util.Map;

public interface ForwardingAnalysis {

  /** Mapping: hostname -&gt; inInterface -&gt; ipsToArpReplyTo */
  Map<String, Map<String, IpSpace>> getArpReplies();

  /**
   * Mapping: hostname -&gt; vrfName -&gt; edge -&gt; dst IPs for which that vrf will forward out
   * the source of the edge and receive an ARP reply from the target of the edge.
   */
  Map<String, Map<String, Map<Edge, IpSpace>>> getArpTrueEdge();

  /** Mapping: hostname -&gt; vrfName -&gt; outInterface -&gt; dstIpsForWhichNoArpResponse */
  Map<String, Map<String, Map<String, IpSpace>>> getNeighborUnreachableOrExitsNetwork();

  /** Mapping: hostname -&gt; vrfName -&gt; outInterface -&gt; dstIPsWhichDeliveredToSubnet */
  Map<String, Map<String, Map<String, IpSpace>>> getDeliveredToSubnet();

  /** Mapping: hostname -&gt; vrfName -&gt; outInterface -&gt; dstIPsWhichExitsNetwork */
  Map<String, Map<String, Map<String, IpSpace>>> getExitsNetwork();

  /** Mapping: hostname -&gt; outInterface -&gt; dstIpsForWhichCannotReachNeighbors */
  Map<String, Map<String, Map<String, IpSpace>>> getNeighborUnreachable();

  /** Mapping: hostname -&gt; outInterface -&gt; dstIpsForWhichNoSufficientInfoToDetermine */
  Map<String, Map<String, Map<String, IpSpace>>> getInsufficientInfo();

  /**
   * Mapping: hostname -&gt; vrfName -&gt; nullRoutedIps <br>
   * A nullable IP is a destination IP for which there is a longest-prefix-match route that discards
   * the packet rather than forwarding it out some interface.
   */
  Map<String, Map<String, IpSpace>> getNullRoutedIps();

  /**
   * Mapping: hostname -&gt; vrfName -&gt; routableIps <br>
   * A routable IP is a destination IP for which there is a longest-prefix-match route.
   */
  Map<String, Map<String, IpSpace>> getRoutableIps();
}
