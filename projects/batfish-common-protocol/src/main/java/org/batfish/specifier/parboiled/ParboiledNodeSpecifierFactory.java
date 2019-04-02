package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** An {@link NodeSpecifierFactory} whose grammar is encoded in {@link Parser#NodeSpec()} */
@AutoService(NodeSpecifierFactory.class)
public class ParboiledNodeSpecifierFactory implements NodeSpecifierFactory {

  public static final String NAME = ParboiledNodeSpecifierFactory.class.getSimpleName();

  @Override
  public NodeSpecifier buildNodeSpecifier(Object input) {
    checkArgument(input instanceof String, "%s requires String input", NAME);

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(Parser.instance().getInputRule(Grammar.NODE_SPECIFIER))
            .run((String) input);

    if (!result.parseErrors.isEmpty()) {
      throw new IllegalArgumentException(
          ParserUtils.getErrorString(
              (String) input,
              Grammar.NODE_SPECIFIER,
              (InvalidInputError) result.parseErrors.get(0),
              Parser.ANCHORS));
    }

    AstNode ast = ParserUtils.getAst(result);

    checkArgument(ast instanceof NodeAstNode, "%s requires a NodeSpecifier input", NAME);
    return new ParboiledNodeSpecifier((NodeAstNode) ast);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
