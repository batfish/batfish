package org.batfish.representation.cisco;

public class InspectClassMapMatchProtocol implements InspectClassMapMatch {

  /** */
  private static final long serialVersionUID = 1L;

  private final InspectClassMapProtocol _protocol;

  public InspectClassMapMatchProtocol(InspectClassMapProtocol protocol) {
    _protocol = protocol;
  }

  public InspectClassMapProtocol getProtocol() {
    return _protocol;
  }
}
