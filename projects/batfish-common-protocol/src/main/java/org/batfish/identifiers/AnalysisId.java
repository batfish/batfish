package org.batfish.identifiers;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AnalysisId extends Id {

  public AnalysisId(String id) {
    super(id);
  }

  @Override
  public IdType getType() {
    return IdType.ANALYSIS;
  }
}
