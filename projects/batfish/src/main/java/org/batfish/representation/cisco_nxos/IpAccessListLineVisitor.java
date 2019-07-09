package org.batfish.representation.cisco_nxos;

public interface IpAccessListLineVisitor<T> {

  T visitActionIpAccessListLine(ActionIpAccessListLine actionIpAccessListLine);

  T visitRemarkIpAccessListLine(RemarkIpAccessListLine remarkIpAccessListLine);
}
