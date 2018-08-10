package org.batfish.specifier;

import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.questions.NodesSpecifier;

public class NodeSpecifiers {

  public static NodeSpecifier difference(
      @Nullable NodeSpecifier nodeSpecifier, @Nullable NodeSpecifier notNodeSpecifier) {
    if (nodeSpecifier != null && notNodeSpecifier != null) {
      return new DifferenceNodeSpecifier(nodeSpecifier, notNodeSpecifier);
    } else if (nodeSpecifier != null) {
      return nodeSpecifier;
    } else if (notNodeSpecifier != null) {
      return new DifferenceNodeSpecifier(AllNodesNodeSpecifier.INSTANCE, notNodeSpecifier);
    } else {
      return null;
    }
  }

  public static NodeSpecifier from(@Nullable NodesSpecifier nodesSpecifier) {
    if (nodesSpecifier == null) {
      return null;
    }

    switch (nodesSpecifier.getType()) {
      case NAME:
        return new NameRegexNodeSpecifier(nodesSpecifier.getRegex());
      case ROLE:
        return new RoleRegexNodeSpecifier(
            nodesSpecifier.getRegex(), nodesSpecifier.getRoleDimension());
      default:
        throw new BatfishException("Unexpected NodesSpecifier type: " + nodesSpecifier.getType());
    }
  }
}
