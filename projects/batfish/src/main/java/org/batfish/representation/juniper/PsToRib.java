package org.batfish.representation.juniper;

/** Represents a "to rib" line in a {@link PsTerm} */
public final class PsToRib extends PsTo {

  private final String _ribName;

  public PsToRib(String ribName) {
    _ribName = ribName;
  }

  public String getRibName() {
    return _ribName;
  }
}
