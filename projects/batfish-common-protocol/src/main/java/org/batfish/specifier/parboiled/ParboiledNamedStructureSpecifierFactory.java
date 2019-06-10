package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import org.batfish.specifier.NamedStructureSpecifier;
import org.batfish.specifier.NamedStructureSpecifierFactory;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * An {@link NamedStructureSpecifierFactory} whose grammar is encoded in {@link
 * Parser#NamedStructureSpec()}
 */
public class ParboiledNamedStructureSpecifierFactory implements NamedStructureSpecifierFactory {

  @Override
  public NamedStructureSpecifier buildNamedStructureSpecifier(Object input) {
    checkArgument(
        input instanceof String, "ParboiledNamedStructureSpecifierFactory requires String input");

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(
                Parser.instance().getInputRule(Grammar.NAMED_STRUCTURE_SPECIFIER))
            .run((String) input);

    if (!result.parseErrors.isEmpty()) {
      throw new IllegalArgumentException(
          ParserUtils.getErrorString(
              (String) input,
              Grammar.NAMED_STRUCTURE_SPECIFIER,
              (InvalidInputError) result.parseErrors.get(0),
              Parser.ANCHORS));
    }

    AstNode ast = ParserUtils.getAst(result);

    checkArgument(
        ast instanceof NamedStructureAstNode,
        "ParboiledNamedStructureSpecifierFactory requires an NamedStructureSpecifier input");
    return new ParboiledNamedStructureSpecifier((NamedStructureAstNode) ast);
  }
}
