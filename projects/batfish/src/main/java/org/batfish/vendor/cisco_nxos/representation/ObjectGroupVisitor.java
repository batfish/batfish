package org.batfish.vendor.cisco_nxos.representation;

/** A visitor of {@link ObjectGroup}. */
public interface ObjectGroupVisitor<T> {

  T visitObjectGroupIpAddress(ObjectGroupIpAddress objectGroupIpAddress);

  T visitObjectGroupIpPort(ObjectGroupIpPort objectGroupIpPort);
}
