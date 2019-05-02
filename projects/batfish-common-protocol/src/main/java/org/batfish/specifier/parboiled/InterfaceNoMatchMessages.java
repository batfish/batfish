package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.ParboiledInputValidator.getErrorMessageMissingBook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.CompletionMetadata;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;

/** Checks if the node specifier results in an empty set */
@ParametersAreNonnullByDefault
final class InterfaceNoMatchMessages {

  @ParametersAreNonnullByDefault
  private final class Checker implements InterfaceAstNodeVisitor<List<String>> {

    private final CompletionMetadata _completionMetadata;
    private final NodeRolesData _nodeRolesData;
    private final ReferenceLibrary _referenceLibrary;

    Checker(
        CompletionMetadata completionMetadata,
        NodeRolesData nodeRolesData,
        ReferenceLibrary referenceLibrary) {
      _completionMetadata = completionMetadata;
      _nodeRolesData = nodeRolesData;
      _referenceLibrary = referenceLibrary;
    }

    private List<String> concat(List<String> a, List<String> b) {
      return Lists.newArrayList(Iterables.concat(a, b));
    }

    @Override
    public List<String> visitDifferenceInterfaceAstNode(
        DifferenceInterfaceAstNode differenceInterfaceAstNode) {
      return concat(
          differenceInterfaceAstNode.getLeft().accept(this),
          differenceInterfaceAstNode.getRight().accept(this));
    }

    @Override
    public List<String> visitConnectedToInterfaceAstNode(
        ConnectedToInterfaceAstNode connectedToInterfaceAstNode) {
      // we don't have the information
      return ImmutableList.of();
    }

    @Override
    public List<String> visitTypeInterfaceNode(TypeInterfaceAstNode typeInterfaceAstNode) {
      // we don't have the information
      return ImmutableList.of();
    }

    @Override
    public List<String> visitIntersectionInterfaceAstNode(
        IntersectionInterfaceAstNode intersectionInterfaceAstNode) {
      return concat(
          intersectionInterfaceAstNode.getLeft().accept(this),
          intersectionInterfaceAstNode.getRight().accept(this));
    }

    @Override
    public List<String> visitInterfaceWithNodeInterfaceAstNode(
        InterfaceWithNodeInterfaceAstNode interfaceWithNodeInterfaceAstNode) {
      // TODO: check if we figure out which interface is present on which node
      return ImmutableList.of();
    }

    @Override
    public List<String> visitNameInterfaceNode(NameInterfaceAstNode nameInterfaceAstNode) {
      // TODO: do something here
      return ImmutableList.of();
    }

    @Override
    public List<String> visitNameRegexInterfaceAstNode(
        NameRegexInterfaceAstNode nameRegexInterfaceAstNode) {
      // TODO: do something here
      return ImmutableList.of();
    }

    @Override
    public List<String> visitVrfInterfaceAstNode(VrfInterfaceAstNode vrfInterfaceAstNode) {
      // TODO: check vrf names
      return ImmutableList.of();
    }

    @Override
    public List<String> visitZoneInterfaceAstNode(ZoneInterfaceAstNode zoneInterfaceAstNode) {
      // TODO: check zone names
      return ImmutableList.of();
    }

    @Override
    public List<String> visitInterfaceGroupInterfaceAstNode(
        InterfaceGroupInterfaceAstNode interfaceGroupInterfaceAstNode) {
      Optional<ReferenceBook> refBook =
          _referenceLibrary.getReferenceBook(interfaceGroupInterfaceAstNode.getReferenceBook());
      if (refBook.isPresent()) {
        if (refBook.get().getInterfaceGroups().stream()
            .anyMatch(
                r ->
                    r.getName()
                        .equalsIgnoreCase(interfaceGroupInterfaceAstNode.getInterfaceGroup()))) {
          return ImmutableList.of();
        } else {
          return ImmutableList.of(
              ParboiledInputValidator.getErrorMessageMissingGroup(
                  interfaceGroupInterfaceAstNode.getInterfaceGroup(),
                  "Interface group",
                  interfaceGroupInterfaceAstNode.getReferenceBook(),
                  "reference book"));
        }
      } else {
        return ImmutableList.of(
            getErrorMessageMissingBook(
                interfaceGroupInterfaceAstNode.getReferenceBook(), "Reference book"));
      }
    }

    @Override
    public List<String> visitUnionInterfaceAstNode(UnionInterfaceAstNode unionInterfaceAstNode) {
      return concat(
          unionInterfaceAstNode.getLeft().accept(this),
          unionInterfaceAstNode.getRight().accept(this));
    }
  }

  private final InterfaceAstNode _ast;

  InterfaceNoMatchMessages(InterfaceAstNode ast) {
    _ast = ast;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceNoMatchMessages)) {
      return false;
    }
    return Objects.equals(_ast, ((InterfaceNoMatchMessages) o)._ast);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ast);
  }

  public List<String> get(
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    return _ast.accept(new Checker(completionMetadata, nodeRolesData, referenceLibrary));
  }
}
