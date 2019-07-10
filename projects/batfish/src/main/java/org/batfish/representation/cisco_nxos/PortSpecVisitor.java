package org.batfish.representation.cisco_nxos;

/** A visitor of {@link PortSpec}. */
public interface PortSpecVisitor<T> {

  T visitLiteralPortSpec(LiteralPortSpec literalPortSpec);

  T visitPortGroupPortSpec(PortGroupPortSpec portGroupPortSpec);
}
