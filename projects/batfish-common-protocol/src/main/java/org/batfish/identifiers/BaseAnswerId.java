package org.batfish.identifiers;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BaseAnswerId extends Id {

  private BaseAnswerId(String id) {
    super(id);
  }
  
}
