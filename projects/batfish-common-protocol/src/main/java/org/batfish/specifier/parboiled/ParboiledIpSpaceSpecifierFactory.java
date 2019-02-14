package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.IpSpaceSpecifierFactory;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/** An IpSpaceSpecifierFactory whose grammar is encoded in {@link Parser#IpSpaceExpression()} */
@AutoService(IpSpaceSpecifierFactory.class)
public class ParboiledIpSpaceSpecifierFactory implements IpSpaceSpecifierFactory {

  public static final String NAME = ParboiledIpSpaceSpecifierFactory.class.getSimpleName();

  @Override
  public IpSpaceSpecifier buildIpSpaceSpecifier(Object input) {
    checkArgument(input instanceof String, "%s requires String input", NAME);

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(
                Parser.INSTANCE.input(Parser.INSTANCE.IpSpaceExpression()))
            .run((String) input);

    if (!result.parseErrors.isEmpty()) {
      String error =
          ParserUtils.getErrorString(
              (String) input,
              "IpSpace",
              (InvalidInputError) result.parseErrors.get(0),
              Parser.COMPLETION_TYPES);
      throw new IllegalArgumentException(error);
    }

    AstNode ast = ParserUtils.getAst(result);

    checkArgument(ast instanceof IpSpaceAstNode, "%s requires an IpSpace input", NAME);
    return new ParboiledIpSpaceSpecifier((IpSpaceAstNode) ast);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
