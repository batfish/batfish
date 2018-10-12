package org.batfish.identifiers;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class QuestionSettingsId extends Id {

  public static final QuestionSettingsId DEFAULT_ID = new QuestionSettingsId("NONE");

  public QuestionSettingsId(String id) {
    super(id);
  }
}
