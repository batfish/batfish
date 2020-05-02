package org.batfish.identifiers;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AnswerId extends Id {

  public AnswerId(String id) {
    super(id);
  }

  @Override
  public IdType getType() {
    return IdType.ANSWER;
  }
}
