package org.batfish.identifiers;

import javax.annotation.Nullable;

public class QuestionSettingsId extends Id {

  private final String _questionClassId;

  public QuestionSettingsId(String id, @Nullable String questionClassId) {
    super(id);
    _questionClassId = questionClassId;
  }

  public @Nullable String getQuestionClassId() {
    return _questionClassId;
  }
}
