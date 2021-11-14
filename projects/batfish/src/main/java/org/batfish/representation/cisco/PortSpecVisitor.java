package org.batfish.representation.cisco;

/** A visitor of {@link PortSpec}. */
public interface PortSpecVisitor<T> {

  T visitLiteralPortSpec(LiteralPortSpec literalPortSpec);

  T visitPortGroupPortSpec(PortObjectGroupPortSpec portGroupPortSpec);
}
