package org.batfish.identifiers;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AnswerId extends Id {

  private AnswerId(String id) {
    super(id);
  }
  
}
