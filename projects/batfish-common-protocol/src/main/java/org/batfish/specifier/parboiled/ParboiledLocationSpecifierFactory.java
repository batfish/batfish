package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.LocationSpecifierFactory;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** An {@link LocationSpecifierFactory} whose grammar is encoded in {@link Parser#LocationSpec()} */
@AutoService(LocationSpecifierFactory.class)
public class ParboiledLocationSpecifierFactory implements LocationSpecifierFactory {

  public static final String NAME = ParboiledLocationSpecifierFactory.class.getSimpleName();

  @Override
  public LocationSpecifier buildLocationSpecifier(Object input) {
    checkArgument(input instanceof String, "%s requires String input", NAME);

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(
                Parser.instance().getInputRule(Grammar.LOCATION_SPECIFIER))
            .run((String) input);

    if (!result.parseErrors.isEmpty()) {
      throw new IllegalArgumentException(
          ParserUtils.getErrorString(
              (String) input,
              Grammar.LOCATION_SPECIFIER,
              (InvalidInputError) result.parseErrors.get(0),
              Parser.ANCHORS));
    }

    AstNode ast = ParserUtils.getAst(result);

    checkArgument(ast instanceof LocationAstNode, "%s requires a LocationSpecifier input", NAME);
    return new ParboiledLocationSpecifier((LocationAstNode) ast);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
