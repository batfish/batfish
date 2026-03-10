package org.batfish.vendor.cisco_nxos.representation;

/** A visitor of {@link PortSpec}. */
public interface PortSpecVisitor<T> {

  T visitLiteralPortSpec(LiteralPortSpec literalPortSpec);

  T visitPortGroupPortSpec(PortGroupPortSpec portGroupPortSpec);
}
