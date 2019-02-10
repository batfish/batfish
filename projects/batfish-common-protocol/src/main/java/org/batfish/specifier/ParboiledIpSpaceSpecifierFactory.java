package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkArgument;

import org.batfish.specifier.parboiled.AstNode;
import org.batfish.specifier.parboiled.Parser;
import org.batfish.specifier.parboiled.ParserUtils;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * An IpSpaceSpecifierFactory whose grammar is encoded in the Parboiled-based Parser at {@link
 * Parser#IpSpaceExpression()}
 */
public class ParboiledIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {

  public static final String NAME = ParboiledIpSpaceSpecifierFactory.class.getSimpleName();

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(Object input) {
    checkArgument(input instanceof String, getName() + " requires String input");

    ParsingResult<?> result =
        new ReportingParseRunner<>(Parser.INSTANCE.input(Parser.INSTANCE.IpSpaceExpression()))
            .run((String) input);

    checkArgument(
        !result.parseErrors.isEmpty(),
        ParserUtils.getErrorString((InvalidInputError) result.parseErrors.get(0)));

    return new ParboiledIpSpaceSpecifier((AstNode) result.valueStack);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
