package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.specifier.IpProtocolSpecifier;
import org.batfish.specifier.IpProtocolSpecifierFactory;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * An {@link org.batfish.specifier.IpProtocolSpecifier} whose grammar is encoded in {@link
 * Parser#IpProtocolSpec()}
 */
@AutoService(IpProtocolSpecifierFactory.class)
public class ParboiledIpProtocolSpecifierFactory implements IpProtocolSpecifierFactory {

  public static final String NAME = ParboiledIpProtocolSpecifierFactory.class.getSimpleName();

  @Override
  public IpProtocolSpecifier buildIpProtocolSpecifier(Object input) {
    checkArgument(input instanceof String, "%s requires String input", NAME);

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(
                Parser.instance().getInputRule(Grammar.IP_PROTOCOL_SPECIFIER))
            .run((String) input);

    if (!result.parseErrors.isEmpty()) {
      throw new IllegalArgumentException(
          ParserUtils.getErrorString(
              (String) input,
              Grammar.IP_PROTOCOL_SPECIFIER,
              (InvalidInputError) result.parseErrors.get(0),
              Parser.ANCHORS));
    }

    AstNode ast = ParserUtils.getAst(result);

    checkArgument(
        ast instanceof IpProtocolAstNode, "%s requires an IP protocol specifier input", NAME);
    return new ParboiledIpProtocolSpecifier((IpProtocolAstNode) ast);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
