package org.batfish.representation.juniper;

/** Represents a "to level" line in a {@link PsTerm} */
public final class PsToLevel extends PsTo {

  private final long _level;

  public PsToLevel(long level) {
    _level = level;
  }

  public long getLevel() {
    return _level;
  }
}
