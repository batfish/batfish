package org.batfish.identifiers;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FinalAnswerId extends Id {

  private FinalAnswerId(String id) {
    super(id);
  }
  
}
