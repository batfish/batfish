package org.batfish.vendor.cisco_nxos.representation;

public interface IpAccessListLineVisitor<T> {

  T visitActionIpAccessListLine(ActionIpAccessListLine actionIpAccessListLine);

  T visitRemarkIpAccessListLine(RemarkIpAccessListLine remarkIpAccessListLine);
}
