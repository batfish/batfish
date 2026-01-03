package org.batfish.specifier;

import javax.annotation.Nullable;
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

    return switch (nodesSpecifier.getType()) {
      case NAME -> new NameRegexNodeSpecifier(nodesSpecifier.getRegex());
      case ROLE ->
          new RoleRegexNodeSpecifier(nodesSpecifier.getRegex(), nodesSpecifier.getRoleDimension());
    };
  }
}
