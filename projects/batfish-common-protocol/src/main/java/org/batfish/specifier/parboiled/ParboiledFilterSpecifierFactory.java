package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.specifier.FilterSpecifier;
import org.batfish.specifier.FilterSpecifierFactory;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** An {@link FilterSpecifierFactory} whose grammar is encoded in {@link Parser#FilterSpec()} */
@AutoService(FilterSpecifierFactory.class)
public class ParboiledFilterSpecifierFactory implements FilterSpecifierFactory {

  public static final String NAME = ParboiledFilterSpecifierFactory.class.getSimpleName();

  @Override
  public FilterSpecifier buildFilterSpecifier(Object input) {
    checkArgument(input instanceof String, "%s requires String input", NAME);

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(Parser.instance().getInputRule(Grammar.FILTER_SPECIFIER))
            .run((String) input);

    if (!result.parseErrors.isEmpty()) {
      throw new IllegalArgumentException(
          ParserUtils.getErrorString(
              (String) input,
              Grammar.FILTER_SPECIFIER,
              (InvalidInputError) result.parseErrors.get(0),
              Parser.ANCHORS));
    }

    AstNode ast = ParserUtils.getAst(result);

    checkArgument(ast instanceof FilterAstNode, "%s requires an FilterSpecifier input", NAME);
    return new ParboiledFilterSpecifier((FilterAstNode) ast);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
