package org.batfish.vendor.cool_nos.representation;

import java.io.Serializable;

/** The next hop of a Cool NOS route. */
public interface NextHop extends Serializable {

  <T> T accept(NextHopVisitor<T> visitor);
}
