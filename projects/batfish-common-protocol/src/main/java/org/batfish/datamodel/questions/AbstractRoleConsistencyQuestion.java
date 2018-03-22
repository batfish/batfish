package org.batfish.datamodel.questions;

import java.util.Optional;
import org.batfish.datamodel.NodeRoleSpecifier;
import org.batfish.role.OutliersHypothesis;

public abstract class AbstractRoleConsistencyQuestion extends Question {

  public abstract OutliersHypothesis getHypothesis();

  public abstract Optional<NodeRoleSpecifier> getRoleSpecifier();
}
