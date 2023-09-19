package org.batfish.datamodel;

import javax.annotation.Nonnull;

/** A route or route builder with readable BGP originator IP. */
public interface HasReadableOriginatorIp {

  @Nonnull
  Ip getOriginatorIp();
}
