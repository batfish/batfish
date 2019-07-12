package org.batfish.representation.cisco_nxos;

/** A visitor of {@link IpAddressSpec}. */
public interface IpAddressSpecVisitor<T> {

  T visitAddrGroupIpAddressSpec(AddrGroupIpAddressSpec addrGroupIpAddressSpec);

  T visitLiteralIpAddressSpec(LiteralIpAddressSpec literalIpAddressSpec);
}
