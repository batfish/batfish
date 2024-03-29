package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.InterfaceWithConnectedIpsSpecifier;
import org.batfish.specifier.NameInterfaceSpecifier;
import org.batfish.specifier.NameRegexInterfaceSpecifier;
import org.batfish.specifier.NodeSpecifierInterfaceSpecifier;
import org.batfish.specifier.ReferenceInterfaceGroupInterfaceSpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.TypesInterfaceSpecifier;
import org.batfish.specifier.VrfNameInterfaceSpecifier;
import org.batfish.specifier.ZoneNameInterfaceSpecifier;
import org.batfish.specifier.parboiled.ParboiledIpSpaceSpecifier.IpSpaceAstNodeToIpSpace;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** An {@link InterfaceSpecifier} that resolves based on the AST generated by {@link Parser}. */
@ParametersAreNonnullByDefault
public final class ParboiledInterfaceSpecifier implements InterfaceSpecifier {

  @ParametersAreNonnullByDefault
  private final class InterfaceAstNodeToInterfaces
      implements InterfaceAstNodeVisitor<Set<NodeInterfacePair>> {
    private final SpecifierContext _ctxt;
    private final Set<String> _nodes;

    InterfaceAstNodeToInterfaces(Set<String> nodes, SpecifierContext ctxt) {
      _nodes = nodes;
      _ctxt = ctxt;
    }

    @Override
    public @Nonnull Set<NodeInterfacePair> visitConnectedToInterfaceAstNode(
        ConnectedToInterfaceAstNode connectedToInterfaceAstNode) {
      return new InterfaceWithConnectedIpsSpecifier(
              connectedToInterfaceAstNode
                  .getIpSpaceAstNode()
                  .accept(new IpSpaceAstNodeToIpSpace(_ctxt)))
          .resolve(_nodes, _ctxt);
    }

    @Override
    public @Nonnull Set<NodeInterfacePair> visitDifferenceInterfaceAstNode(
        DifferenceInterfaceAstNode differenceInterfaceAstNode) {
      return Sets.difference(
          differenceInterfaceAstNode.getLeft().accept(this),
          differenceInterfaceAstNode.getRight().accept(this));
    }

    @Override
    public @Nonnull Set<NodeInterfacePair> visitInterfaceGroupInterfaceAstNode(
        InterfaceGroupInterfaceAstNode interfaceGroupInterfaceAstNode) {
      // Because we changed the input on Apr 30 2019 from (group, book) to (book, group), we
      // first interpret the user input as (book, group) if the book exists. Otherwise, we interpret
      // it is as (group, book)
      if (_ctxt.getReferenceBook(interfaceGroupInterfaceAstNode.getReferenceBook()).isPresent()) {
        return new ReferenceInterfaceGroupInterfaceSpecifier(
                interfaceGroupInterfaceAstNode.getInterfaceGroup(),
                interfaceGroupInterfaceAstNode.getReferenceBook())
            .resolve(_nodes, _ctxt);
      } else if (_ctxt
          .getReferenceBook(interfaceGroupInterfaceAstNode.getInterfaceGroup())
          .isPresent()) {
        return new ReferenceInterfaceGroupInterfaceSpecifier(
                interfaceGroupInterfaceAstNode.getReferenceBook(),
                interfaceGroupInterfaceAstNode.getInterfaceGroup())
            .resolve(_nodes, _ctxt);
      }
      throw new NoSuchElementException(
          "Reference book "
              + interfaceGroupInterfaceAstNode.getReferenceBook()
              + " is not present");
    }

    @Override
    public @Nonnull Set<NodeInterfacePair> visitIntersectionInterfaceAstNode(
        IntersectionInterfaceAstNode intersectionInterfaceAstNode) {
      return Sets.intersection(
          intersectionInterfaceAstNode.getLeft().accept(this),
          intersectionInterfaceAstNode.getRight().accept(this));
    }

    @Override
    public Set<NodeInterfacePair> visitInterfaceWithNodeInterfaceAstNode(
        InterfaceWithNodeInterfaceAstNode interfaceWithNodeInterfaceAstNode) {
      return Sets.intersection(
          new NodeSpecifierInterfaceSpecifier(
                  new ParboiledNodeSpecifier(interfaceWithNodeInterfaceAstNode.getNodeAstNode()))
              .resolve(_nodes, _ctxt),
          new ParboiledInterfaceSpecifier(interfaceWithNodeInterfaceAstNode.getInterfaceAstNode())
              .resolve(_nodes, _ctxt));
    }

    @Override
    public @Nonnull Set<NodeInterfacePair> visitNameInterfaceNode(
        NameInterfaceAstNode nameInterfaceAstNode) {
      return new NameInterfaceSpecifier(nameInterfaceAstNode.getName()).resolve(_nodes, _ctxt);
    }

    @Override
    public @Nonnull Set<NodeInterfacePair> visitNameRegexInterfaceAstNode(
        NameRegexInterfaceAstNode nameRegexInterfaceAstNode) {
      return new NameRegexInterfaceSpecifier(nameRegexInterfaceAstNode.getPattern())
          .resolve(_nodes, _ctxt);
    }

    @Override
    public @Nonnull Set<NodeInterfacePair> visitTypeInterfaceNode(
        TypeInterfaceAstNode typeInterfaceAstNode) {
      return new TypesInterfaceSpecifier(ImmutableSet.of(typeInterfaceAstNode.getInterfaceType()))
          .resolve(_nodes, _ctxt);
    }

    @Override
    public @Nonnull Set<NodeInterfacePair> visitUnionInterfaceAstNode(
        UnionInterfaceAstNode unionInterfaceAstNode) {
      return Sets.union(
          unionInterfaceAstNode.getLeft().accept(this),
          unionInterfaceAstNode.getRight().accept(this));
    }

    @Override
    public @Nonnull Set<NodeInterfacePair> visitVrfInterfaceAstNode(
        VrfInterfaceAstNode vrfInterfaceAstNode) {
      return new VrfNameInterfaceSpecifier(vrfInterfaceAstNode.getVrfName()).resolve(_nodes, _ctxt);
    }

    @Override
    public @Nonnull Set<NodeInterfacePair> visitZoneInterfaceAstNode(
        ZoneInterfaceAstNode zoneInterfaceAstNode) {
      return new ZoneNameInterfaceSpecifier(zoneInterfaceAstNode.getZoneName())
          .resolve(_nodes, _ctxt);
    }
  }

  private final InterfaceAstNode _ast;

  ParboiledInterfaceSpecifier(InterfaceAstNode ast) {
    _ast = ast;
  }

  /**
   * Returns an {@link InterfaceSpecifier} based on {@code input} which is parsed as {@link
   * Grammar#INTERFACE_SPECIFIER}.
   *
   * @throws IllegalArgumentException if the parsing fails or does not produce the expected AST
   */
  public static ParboiledInterfaceSpecifier parse(String input) {
    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(
                Parser.instance().getInputRule(Grammar.INTERFACE_SPECIFIER))
            .run(input);

    if (!result.parseErrors.isEmpty()) {
      throw new IllegalArgumentException(
          ParserUtils.getErrorString(
              input,
              Grammar.INTERFACE_SPECIFIER,
              (InvalidInputError) result.parseErrors.get(0),
              Parser.ANCHORS));
    }

    AstNode ast = ParserUtils.getAst(result);
    checkArgument(
        ast instanceof InterfaceAstNode,
        "ParboiledInterfaceSpecifier requires an InterfaceSpecifier input");

    return new ParboiledInterfaceSpecifier((InterfaceAstNode) ast);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ParboiledInterfaceSpecifier)) {
      return false;
    }
    return Objects.equals(_ast, ((ParboiledInterfaceSpecifier) o)._ast);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ast);
  }

  @Override
  public Set<NodeInterfacePair> resolve(Set<String> nodes, SpecifierContext ctxt) {
    return _ast.accept(new InterfaceAstNodeToInterfaces(nodes, ctxt));
  }
}
