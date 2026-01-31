package org.batfish.vendor.cisco_nxos.representation;

/** A visitor of {@link IpAddressSpec}. */
public interface IpAddressSpecVisitor<T> {

  T visitAddrGroupIpAddressSpec(AddrGroupIpAddressSpec addrGroupIpAddressSpec);

  T visitLiteralIpAddressSpec(LiteralIpAddressSpec literalIpAddressSpec);
}
