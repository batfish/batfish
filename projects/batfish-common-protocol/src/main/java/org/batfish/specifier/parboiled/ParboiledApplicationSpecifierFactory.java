package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.specifier.ApplicationSpecifier;
import org.batfish.specifier.ApplicationSpecifierFactory;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * An {@link ApplicationSpecifierFactory} whose grammar is encoded in {@link
 * Parser#ApplicationSpec()}
 */
@AutoService(ApplicationSpecifierFactory.class)
public class ParboiledApplicationSpecifierFactory implements ApplicationSpecifierFactory {

  public static final String NAME = ParboiledApplicationSpecifierFactory.class.getSimpleName();

  @Override
  public ApplicationSpecifier buildApplicationSpecifier(Object input) {
    checkArgument(input instanceof String, "%s requires String input", NAME);

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(
                Parser.instance().getInputRule(Grammar.APPLICATION_SPECIFIER))
            .run((String) input);

    if (!result.parseErrors.isEmpty()) {
      throw new IllegalArgumentException(
          ParserUtils.getErrorString(
              (String) input,
              Grammar.APPLICATION_SPECIFIER,
              (InvalidInputError) result.parseErrors.get(0),
              Parser.ANCHORS));
    }

    AstNode ast = ParserUtils.getAst(result);

    checkArgument(
        ast instanceof ApplicationAstNode, "%s requires an ApplicationSpecifier input", NAME);
    return new ParboiledApplicationSpecifier((ApplicationAstNode) ast);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
