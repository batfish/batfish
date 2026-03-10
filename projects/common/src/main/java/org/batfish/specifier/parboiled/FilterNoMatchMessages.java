package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageEmptyNameRegex;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.CompletionMetadata;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;

/** Implemented {@link NoMatchMessages} for filters */
@ParametersAreNonnullByDefault
final class FilterNoMatchMessages implements NoMatchMessages {

  @ParametersAreNonnullByDefault
  private final class Checker implements FilterAstNodeVisitor<List<String>> {

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
    public List<String> visitDifferenceFilterAstNode(
        DifferenceFilterAstNode differenceFilterAstNode) {
      return concat(
          differenceFilterAstNode.getLeft().accept(this),
          differenceFilterAstNode.getRight().accept(this));
    }

    @Override
    public List<String> visitFilterWithNodeFilterAstNode(
        FilterWithNodeFilterAstNode filterWithNodeFilterAstNode) {
      // this is doing a context insensitive evaluation of existence of nodes and filters
      // we can do slightly better if the node term is simple enough for us to be able to expand
      // leaving that as future work for now
      return concat(
          new NodeNoMatchMessages(filterWithNodeFilterAstNode.getNodeAstNode())
              .get(_completionMetadata, _nodeRolesData, _referenceLibrary),
          new FilterNoMatchMessages(filterWithNodeFilterAstNode.getFilterAstNode())
              .get(_completionMetadata, _nodeRolesData, _referenceLibrary));
    }

    @Override
    public List<String> visitIntersectionFilterAstNode(
        IntersectionFilterAstNode intersectionFilterAstNode) {
      return concat(
          intersectionFilterAstNode.getLeft().accept(this),
          intersectionFilterAstNode.getRight().accept(this));
    }

    @Override
    public List<String> visitNameFilterAstNode(NameFilterAstNode nameFilterAstNode) {
      return (_completionMetadata.getFilterNames().stream()
              .anyMatch(n -> n.equalsIgnoreCase(nameFilterAstNode.getName())))
          ? ImmutableList.of()
          : ImmutableList.of(getErrorMessageMissingName(nameFilterAstNode.getName(), "filter"));
    }

    @Override
    public List<String> visitNameRegexFilterAstNode(NameRegexFilterAstNode nameRegexFilterAstNode) {
      return (_completionMetadata.getFilterNames().stream()
              .anyMatch(n -> nameRegexFilterAstNode.getPattern().matcher(n).find()))
          ? ImmutableList.of()
          : ImmutableList.of(
              getErrorMessageEmptyNameRegex(nameRegexFilterAstNode.getRegex(), "filter"));
    }

    @Override
    public List<String> visitInFilterAstNode(InFilterAstNode inFilterAstNode) {
      // we don't have information on which filters are attached to which interfaces, so only
      // checking if the interface exists.
      return new InterfaceNoMatchMessages(inFilterAstNode.getInterfaceAst())
          .get(_completionMetadata, _nodeRolesData, _referenceLibrary);
    }

    @Override
    public List<String> visitOutFilterAstNode(OutFilterAstNode outFilterAstNode) {
      // we don't have information on which filters are attached to which interfaces, so only
      // checking if the interface exists.
      return new InterfaceNoMatchMessages(outFilterAstNode.getInterfaceAst())
          .get(_completionMetadata, _nodeRolesData, _referenceLibrary);
    }

    @Override
    public List<String> visitUnionFilterAstNode(UnionFilterAstNode unionFilterAstNode) {
      return concat(
          unionFilterAstNode.getLeft().accept(this), unionFilterAstNode.getRight().accept(this));
    }
  }

  private final FilterAstNode _ast;

  FilterNoMatchMessages(FilterAstNode ast) {
    _ast = ast;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FilterNoMatchMessages)) {
      return false;
    }
    return Objects.equals(_ast, ((FilterNoMatchMessages) o)._ast);
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
