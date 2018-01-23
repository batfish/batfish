package org.batfish.datamodel.questions;

import java.util.Optional;
import org.batfish.datamodel.NodeRoleSpecifier;
import org.batfish.role.OutliersHypothesis;

public interface IRoleConsistencyQuestion extends IQuestion {

  OutliersHypothesis getHypothesis();

  Optional<NodeRoleSpecifier> getRoleSpecifier();

}
