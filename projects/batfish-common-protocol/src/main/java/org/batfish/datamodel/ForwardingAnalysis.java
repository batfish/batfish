package org.batfish.datamodel;

import java.util.Map;

public interface ForwardingAnalysis {

  /** Mapping: hostname -&gt; inInterface -&gt; ipsToArpReplyTo */
  Map<String, Map<String, IpSpace>> getArpReplies();

  /** Mapping: edge -&gt; dstIpsForWhichArpReplySent */
  Map<Edge, IpSpace> getArpTrueEdge();

  /** Mapping: hostname -&gt; vrfName -&gt; outInterface -&gt; dstIpsForWhichNoArpResponse */
  Map<String, Map<String, Map<String, IpSpace>>> getNeighborUnreachable();

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
