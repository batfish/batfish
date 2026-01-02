package org.batfish.datamodel.questions;

import org.batfish.role.OutliersHypothesis;

public abstract class AbstractRoleConsistencyQuestion extends Question {

  public abstract OutliersHypothesis getHypothesis();

  public abstract String getRoleDimension();
}
