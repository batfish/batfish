package org.batfish.identifiers;

import javax.annotation.Nullable;

public class AnalysisId extends Id {

  private final String _name;

  public AnalysisId(String id, @Nullable String name) {
    super(id);
    _name = name;
  }

  public @Nullable String getName() {
    return _name;
  }
}
