package org.batfish.representation.cisco;

/** A visitor of {@link PortSpec} that returns a generic value. */
public interface PortSpecVisitor<T> {

  T visitLiteralPortSpec(LiteralPortSpec literalPortSpec);

  T visitPortGroupPortSpec(PortObjectGroupPortSpec portGroupPortSpec);
}
