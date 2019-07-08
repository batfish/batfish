package org.batfish.representation.cisco_nxos;

import java.io.Serializable;

/** A line of an {@link IpAccessList}. */
public interface IpAccessListLine extends Serializable {

  <T> T accept(IpAccessListLineVisitor<T> visitor);
}
