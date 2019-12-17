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
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;

/** Implemented {@link NoMatchMessages} for interfaces */
@ParametersAreNonnullByDefault
final class InterfaceNoMatchMessages implements NoMatchMessages {

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
      // this is doing a context insensitive evaluation of existence of nodes and interfaces
      // we can do slightly better if the node term is simple enough for us to be able to expand
      // leaving that as future work for now
      return concat(
          new NodeNoMatchMessages(interfaceWithNodeInterfaceAstNode.getNodeAstNode())
              .get(_completionMetadata, _nodeRolesData, _referenceLibrary),
          new InterfaceNoMatchMessages(interfaceWithNodeInterfaceAstNode.getInterfaceAstNode())
              .get(_completionMetadata, _nodeRolesData, _referenceLibrary));
    }

    @Override
    public List<String> visitNameInterfaceNode(NameInterfaceAstNode nameInterfaceAstNode) {
      return _completionMetadata.getInterfaces().stream()
              .anyMatch(i -> i.getInterface().equalsIgnoreCase(nameInterfaceAstNode.getName()))
          ? ImmutableList.of()
          : ImmutableList.of(
              getErrorMessageMissingName(nameInterfaceAstNode.getName(), "interface"));
    }

    @Override
    public List<String> visitNameRegexInterfaceAstNode(
        NameRegexInterfaceAstNode nameRegexInterfaceAstNode) {
      return _completionMetadata.getInterfaces().stream()
              .anyMatch(
                  i -> nameRegexInterfaceAstNode.getPattern().matcher(i.getInterface()).find())
          ? ImmutableList.of()
          : ImmutableList.of(
              getErrorMessageEmptyNameRegex(nameRegexInterfaceAstNode.getRegex(), "interface"));
    }

    @Override
    public List<String> visitVrfInterfaceAstNode(VrfInterfaceAstNode vrfInterfaceAstNode) {
      return _completionMetadata.getVrfs().stream()
              .anyMatch(v -> v.equalsIgnoreCase(vrfInterfaceAstNode.getVrfName()))
          ? ImmutableList.of()
          : ImmutableList.of(getErrorMessageMissingName(vrfInterfaceAstNode.getVrfName(), "VRF"));
    }

    @Override
    public List<String> visitZoneInterfaceAstNode(ZoneInterfaceAstNode zoneInterfaceAstNode) {
      return _completionMetadata.getZones().stream()
              .anyMatch(v -> v.equalsIgnoreCase(zoneInterfaceAstNode.getZoneName()))
          ? ImmutableList.of()
          : ImmutableList.of(
              getErrorMessageMissingName(zoneInterfaceAstNode.getZoneName(), "zone"));
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
              getErrorMessageMissingGroup(
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
    return Objects.hashCode(_ast);
  }

  @Override
  public List<String> get(
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    return _ast.accept(new Checker(completionMetadata, nodeRolesData, referenceLibrary));
  }
}
