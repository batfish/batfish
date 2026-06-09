package org.batfish.vendor.sros.representation;

/**
 * SR-OS BGP peer {@code type} (YANG {@code nokia-types-bgp:peer-type}). Determines whether a
 * session is iBGP ({@code internal}) or eBGP ({@code external}) directly, rather than by comparing
 * the peer-as to the local AS.
 */
public enum PeerType {
  INTERNAL,
  EXTERNAL
}
