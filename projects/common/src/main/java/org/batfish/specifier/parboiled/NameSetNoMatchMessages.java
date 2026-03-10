package org.batfish.specifier.parboiled;

import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageEmptyNameRegex;
import static org.batfish.datamodel.answers.InputValidationUtils.getErrorMessageMissingName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.CompletionMetadata;
import org.batfish.referencelibrary.ReferenceLibrary;
import org.batfish.role.NodeRolesData;
import org.batfish.specifier.Grammar;

/** Implemented {@link NoMatchMessages} for name sets */
@ParametersAreNonnullByDefault
final class NameSetNoMatchMessages implements NoMatchMessages {

  @ParametersAreNonnullByDefault
  private final class Checker implements NameSetAstNodeVisitor<List<String>> {

    private final @Nonnull Set<String> _allNames;

    private final @Nonnull String _nameType;

    Checker(Set<String> allNames, String nameType) {
      _allNames = allNames;
      _nameType = nameType;
    }

    private List<String> concat(List<String> a, List<String> b) {
      return Lists.newArrayList(Iterables.concat(a, b));
    }

    @Override
    public List<String> visitNameNameSetAstNode(SingletonNameSetAstNode nameSetAstNode) {
      return (_allNames.stream().anyMatch(n -> n.equalsIgnoreCase(nameSetAstNode.getName())))
          ? ImmutableList.of()
          : ImmutableList.of(getErrorMessageMissingName(nameSetAstNode.getName(), _nameType));
    }

    @Override
    public List<String> visitRegexNameSetAstNode(RegexNameSetAstNode regexNameSetAstNode) {
      return (_allNames.stream().anyMatch(n -> regexNameSetAstNode.getPattern().matcher(n).find()))
          ? ImmutableList.of()
          : ImmutableList.of(
              getErrorMessageEmptyNameRegex(regexNameSetAstNode.getRegex(), _nameType));
    }

    @Override
    public List<String> visitUnionNameSetAstNode(UnionNameSetAstNode unionNameSetAstNode) {
      return concat(
          unionNameSetAstNode.getLeft().accept(this), unionNameSetAstNode.getRight().accept(this));
    }
  }

  private final @Nonnull NameSetAstNode _ast;

  private final @Nonnull Grammar _grammar;

  NameSetNoMatchMessages(NameSetAstNode ast, Grammar grammar) {
    _ast = ast;
    _grammar = grammar;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NameSetNoMatchMessages)) {
      return false;
    }
    return Objects.equals(_ast, ((NameSetNoMatchMessages) o)._ast)
        && Objects.equals(_grammar, ((NameSetNoMatchMessages) o)._grammar);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ast, _grammar.ordinal());
  }

  @Override
  public List<String> get(
      CompletionMetadata completionMetadata,
      NodeRolesData nodeRolesData,
      ReferenceLibrary referenceLibrary) {
    return _ast.accept(
        new Checker(Grammar.getNames(completionMetadata, _grammar), Grammar.getNameType(_grammar)));
  }
}
