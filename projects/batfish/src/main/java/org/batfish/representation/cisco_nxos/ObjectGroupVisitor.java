package org.batfish.representation.cisco_nxos;

/** A visitor of {@link ObjectGroup}. */
public interface ObjectGroupVisitor<T> {

  T visitObjectGroupIpAddress(ObjectGroupIpAddress objectGroupIpAddress);
}
