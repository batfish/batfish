package org.batfish.identifiers;

import javax.annotation.Nullable;

public class QuestionId extends Id {

  private final String _name;

  public QuestionId(String id, @Nullable String name) {
    super(id);
    _name = name;
  }

  public @Nullable String getName() {
    return _name;
  }
}
