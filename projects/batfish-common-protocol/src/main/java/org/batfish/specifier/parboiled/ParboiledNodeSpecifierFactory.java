package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.NodeSpecifierFactory;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** An {@link NodeSpecifierFactory} whose grammar is encoded in {@link Parser#NodeExpression()} */
@AutoService(NodeSpecifierFactory.class)
public class ParboiledNodeSpecifierFactory implements NodeSpecifierFactory {

  public static final String NAME = ParboiledNodeSpecifierFactory.class.getSimpleName();

  @Override
  public NodeSpecifier buildNodeSpecifier(Object input) {
    checkArgument(input instanceof String, "%s requires String input", NAME);

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(Parser.INSTANCE.input(Parser.INSTANCE.NodeExpression()))
            .run((String) input);

    if (!result.parseErrors.isEmpty()) {
      String error =
          ParserUtils.getErrorString(
              (String) input,
              "NodeSpecifier",
              (InvalidInputError) result.parseErrors.get(0),
              Parser.ANCHORS);
      throw new IllegalArgumentException(error);
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
