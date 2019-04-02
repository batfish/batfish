package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.service.AutoService;
import org.batfish.specifier.RoutingPolicySpecifier;
import org.batfish.specifier.RoutingPolicySpecifierFactory;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

/**
 * An {@link RoutingPolicySpecifierFactory} whose grammar is encoded in {@link
 * Parser#RoutingPolicySpec()}
 */
@AutoService(RoutingPolicySpecifierFactory.class)
public class ParboiledRoutingPolicySpecifierFactory implements RoutingPolicySpecifierFactory {

  public static final String NAME = ParboiledRoutingPolicySpecifierFactory.class.getSimpleName();

  @Override
  public RoutingPolicySpecifier buildRoutingPolicySpecifier(Object input) {
    checkArgument(input instanceof String, "%s requires String input", NAME);

    ParsingResult<AstNode> result =
        new ReportingParseRunner<AstNode>(
                Parser.instance().getInputRule(Grammar.ROUTING_POLICY_SPECIFIER))
            .run((String) input);

    if (!result.parseErrors.isEmpty()) {
      throw new IllegalArgumentException(
          ParserUtils.getErrorString(
              (String) input,
              Grammar.ROUTING_POLICY_SPECIFIER,
              (InvalidInputError) result.parseErrors.get(0),
              Parser.ANCHORS));
    }

    AstNode ast = ParserUtils.getAst(result);

    checkArgument(
        ast instanceof RoutingPolicyAstNode, "%s requires an RoutingPolicySpecifier input", NAME);
    return new ParboiledRoutingPolicySpecifier((RoutingPolicyAstNode) ast);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
