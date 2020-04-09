package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageEmptyNameRegex;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingBook;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingGroup;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.CompletionMetadata;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRoleDimension;
import org.batfish.role.NodeRolesData;

/** Implemented {@link NoMatchMessages} for nodes */
@ParametersAreNonnullByDefault
final class NodeNoMatchMessages implements NoMatchMessages {

  @ParametersAreNonnullByDefault
  private final class Checker implements NodeAstNodeVisitor<List<String>> {

    private final CompletionMetadata _completionMetadata;
    private final NodeRolesData _nodeRolesData;

    Checker(CompletionMetadata completionMetadata, NodeRolesData nodeRolesData) {
      _completionMetadata = completionMetadata;
      _nodeRolesData = nodeRolesData;
    }

    private List<String> concat(List<String> a, List<String> b) {
      return Lists.newArrayList(Iterables.concat(a, b));
    }

    @Override
    public List<String> visitDifferenceNodeAstNode(DifferenceNodeAstNode differenceNodeAstNode) {
      return concat(
          differenceNodeAstNode.getLeft().accept(this),
          differenceNodeAstNode.getRight().accept(this));
    }

    @Override
    public List<String> visitIntersectionNodeAstNode(
        IntersectionNodeAstNode intersectionNodeAstNode) {
      return concat(
          intersectionNodeAstNode.getLeft().accept(this),
          intersectionNodeAstNode.getRight().accept(this));
    }

    @Override
    public List<String> visitNameNodeAstNode(NameNodeAstNode nameNodeAstNode) {
      return (_completionMetadata.getNodes().keySet().stream()
              .anyMatch(n -> n.equalsIgnoreCase(nameNodeAstNode.getName())))
          ? ImmutableList.of()
          : ImmutableList.of(getErrorMessageMissingName(nameNodeAstNode.getName(), "Device"));
    }

    @Override
    public List<String> visitNameRegexNodeAstNode(NameRegexNodeAstNode nameRegexNodeAstNode) {
      return (_completionMetadata.getNodes().keySet().stream()
              .anyMatch(n -> nameRegexNodeAstNode.getPattern().matcher(n).find()))
          ? ImmutableList.of()
          : ImmutableList.of(
              getErrorMessageEmptyNameRegex(nameRegexNodeAstNode.getRegex(), "device"));
    }

    @Override
    public List<String> visitRoleNodeAstNode(RoleNodeAstNode roleNodeAstNode) {
      Optional<NodeRoleDimension> refBook =
          _nodeRolesData.nodeRoleDimensionFor(roleNodeAstNode.getDimensionName());
      if (refBook.isPresent()) {
        if (refBook.get().roleNamesFor(_completionMetadata.getNodes().keySet()).stream()
            .anyMatch(r -> r.equalsIgnoreCase(roleNodeAstNode.getRoleName()))) {
          return ImmutableList.of();
        } else {
          return ImmutableList.of(
              getErrorMessageMissingGroup(
                  roleNodeAstNode.getRoleName(),
                  "Node role",
                  roleNodeAstNode.getDimensionName(),
                  "dimension"));
        }
      } else {
        return ImmutableList.of(
            getErrorMessageMissingBook(roleNodeAstNode.getDimensionName(), "Node role dimension"));
      }
    }

    @Override
    public List<String> visitTypeNodeAstNode(TypeNodeAstNode typeNodeAstNode) {
      // device type information is not available
      return ImmutableList.of();
    }

    @Override
    public List<String> visitUnionNodeAstNode(UnionNodeAstNode unionNodeAstNode) {
      return concat(
          unionNodeAstNode.getLeft().accept(this), unionNodeAstNode.getRight().accept(this));
    }
  }

  private final NodeAstNode _ast;

  NodeNoMatchMessages(NodeAstNode ast) {
    _ast = ast;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NodeNoMatchMessages)) {
      return false;
    }
    return Objects.equals(_ast, ((NodeNoMatchMessages) o)._ast);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ast);
  }

  @Override
  public List<String> get(
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    return _ast.accept(new Checker(completionMetadata, nodeRolesData));
  }
}
