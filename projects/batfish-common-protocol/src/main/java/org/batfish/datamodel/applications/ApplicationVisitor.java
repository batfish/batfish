package org.batfish.datamodel.applications;

/** Visitor class for {@link Application} */
public interface ApplicationVisitor<T> {
  T visitTcpApplication(TcpApplication app);

  T visitUdpApplication(UdpApplication app);

  T visitIcmpTypesApplication(IcmpTypesApplication app);

  T visitIcmpTypeCodesApplication(IcmpTypeCodesApplication app);
}
