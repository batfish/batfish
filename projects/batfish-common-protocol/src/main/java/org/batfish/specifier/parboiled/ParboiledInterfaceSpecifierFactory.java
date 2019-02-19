package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.specifier.InterfaceSpecifier;
import org.batfish.specifier.InterfaceSpecifierFactory;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * An {@link InterfaceSpecifierFactory} whose grammar is encoded in {@link
 * Parser#InterfaceExpression()}
 */
@AutoService(InterfaceSpecifierFactory.class)
public class ParboiledInterfaceSpecifierFactory implements InterfaceSpecifierFactory {

  public static final String NAME = ParboiledInterfaceSpecifierFactory.class.getSimpleName();

  @Override
  public InterfaceSpecifier buildInterfaceSpecifier(Object input) {
    checkArgument(input instanceof String, "%s requires String input", NAME);

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(
                Parser.INSTANCE.input(Parser.INSTANCE.InterfaceExpression()))
            .run((String) input);

    if (!result.parseErrors.isEmpty()) {
      String error =
          ParserUtils.getErrorString(
              (String) input,
              "InterfaceSpecifier",
              (InvalidInputError) result.parseErrors.get(0),
              Parser.ANCHORS);
      throw new IllegalArgumentException(error);
    }

    AstNode ast = ParserUtils.getAst(result);

    checkArgument(ast instanceof InterfaceAstNode, "%s requires an InterfaceSpecifier input", NAME);
    return new ParboiledInterfaceSpecifier((InterfaceAstNode) ast);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
