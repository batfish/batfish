package org.batfish.representation.juniper;

/** Represents a "to protocol" line in a {@link PsTerm} */
public final class PsToProtocol extends PsTo {

  private final PsProtocol _protocol;

  public PsToProtocol(PsProtocol protocol) {
    _protocol = protocol;
  }

  public PsProtocol getProtocol() {
    return _protocol;
  }
}
