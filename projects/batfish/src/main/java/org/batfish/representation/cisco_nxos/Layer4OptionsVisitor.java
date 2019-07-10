package org.batfish.representation.cisco_nxos;

/** A visitor of {@link Layer4Options}. */
public interface Layer4OptionsVisitor<T> {

  T visitIcmpOptions(IcmpOptions icmpOptions);

  T visitIgmpOptions(IgmpOptions igmpOptions);

  T visitTcpOptions(TcpOptions tcpOptions);

  T visitUdpOptions(UdpOptions udpOptions);
}
