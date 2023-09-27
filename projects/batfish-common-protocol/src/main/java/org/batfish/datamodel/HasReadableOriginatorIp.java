package org.batfish.datamodel;

/** A route or route builder with readable BGP originator IP. */
public interface HasReadableOriginatorIp {

  Ip getOriginatorIp();
}
