package org.batfish.vendor.sros.representation;

/**
 * A protocol name usable in a routing-policy {@code from protocol name [...]} match. The modeled
 * subset of the SR-OS protocol enumeration; an unrecognized value is warned at extraction time
 * rather than stored, so conversion never has to handle an unknown protocol.
 */
public enum FromProtocol {
  STATIC,
  DIRECT,
  BGP,
  OSPF,
  ISIS;
}
