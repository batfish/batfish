package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Optional;
import org.batfish.datamodel.NodeRoleSpecifier;
import org.batfish.role.OutliersHypothesis;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public interface IRoleConsistencyQuestion extends IQuestion {

  OutliersHypothesis getHypothesis();

  Optional<NodeRoleSpecifier> getRoleSpecifier();
}
