package org.batfish.grammar.iptables;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.iptables.IptablesParser.Built_in_targetContext;
import org.batfish.grammar.iptables.IptablesParser.CommandContext;
import org.batfish.grammar.iptables.IptablesParser.Command_tailContext;
import org.batfish.grammar.iptables.IptablesParser.Declaration_chain_policyContext;
import org.batfish.grammar.iptables.IptablesParser.Declaration_tableContext;
import org.batfish.grammar.iptables.IptablesParser.EndpointContext;
import org.batfish.grammar.iptables.IptablesParser.Iptables_configurationContext;
import org.batfish.grammar.iptables.IptablesParser.MatchContext;
import org.batfish.grammar.iptables.IptablesParser.ProtocolContext;
import org.batfish.grammar.iptables.IptablesParser.Rule_specContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.iptables.IptablesChain.ChainPolicy;
import org.batfish.representation.iptables.IptablesConfiguration;
import org.batfish.representation.iptables.IptablesMatch.MatchType;
import org.batfish.representation.iptables.IptablesRule;
import org.batfish.representation.iptables.IptablesRule.IptablesActionType;
import org.batfish.representation.iptables.IptablesVendorConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class IptablesControlPlaneExtractor extends IptablesParserBaseListener
    implements ControlPlaneExtractor, SilentSyntaxListener {

  private static int toInteger(Token t) {
    return Integer.parseInt(t.getText());
  }

  private IptablesConfiguration _configuration;

  private String _fileName;

  private boolean _formatIptablesSave = false;

  private IptablesCombinedParser _parser;

  private String _tableCurrent;

  private String _text;

  private IptablesVendorConfiguration _vendorConfiguration;

  private Warnings _w;

  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  public IptablesControlPlaneExtractor(
      String fileText,
      IptablesCombinedParser iptablesParser,
      Warnings warnings,
      String fileName,
      SilentSyntaxCollection silentSyntax) {
    _text = fileText;
    _parser = iptablesParser;
    _w = warnings;
    _fileName = fileName;
    _silentSyntax = silentSyntax;
  }

  @Override
  public @Nonnull SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Override
  public void enterIptables_configuration(Iptables_configurationContext ctx) {
    _vendorConfiguration = new IptablesVendorConfiguration();
    _configuration = _vendorConfiguration;
    _vendorConfiguration.setHostname(_fileName);
  }

  @Override
  public void exitCommand(CommandContext ctx) {

    // default table if not specified in the command
    String table = (_formatIptablesSave) ? _tableCurrent : "filter";

    if (ctx.table() != null) {
      table = ctx.table().getText();
    }

    Command_tailContext tailCtx = ctx.command_tail();

    if (tailCtx.command_append() != null) {
      String chain = tailCtx.command_append().chain().getText();
      IptablesRule rule = extractRule(tailCtx.command_append().rule_spec());
      _configuration.addRule(table, chain, rule, -1);
    } else if (tailCtx.command_check() != null) {
      warn(ctx, "Check command is not supported");
    } else if (tailCtx.command_delete() != null) {
      warn(ctx, "Delete command is not supported");
    } else if (tailCtx.command_delete_chain() != null) {
      warn(ctx, "Delete Chain command is not supported");
    } else if (tailCtx.command_flush() != null) {
      warn(ctx, "Flush command is not supported");
    } else if (tailCtx.command_help() != null) {
      warn(ctx, "Help command is not supported");
    } else if (tailCtx.command_insert() != null) {
      String chain = tailCtx.command_insert().chain().getText();
      int ruleNum = 1;
      if (tailCtx.command_insert().rulenum != null) {
        ruleNum = toInteger(tailCtx.command_insert().rulenum);
      }
      IptablesRule rule = extractRule(tailCtx.command_insert().rule_spec());
      _configuration.addRule(table, chain, rule, ruleNum);
    } else if (tailCtx.command_list() != null) {
      warn(ctx, "List command is not supported");
    } else if (tailCtx.command_list_rules() != null) {
      warn(ctx, "List Rules command is not supported");
    } else if (tailCtx.command_new_chain() != null) {
      String chain = tailCtx.command_new_chain().chain().getText();
      _configuration.addChain(table, chain);
    } else if (tailCtx.command_policy() != null) {
      String chain = tailCtx.command_policy().chain().getText();
      ChainPolicy policy = getBuiltInTarget(tailCtx.command_policy().built_in_target());
      _configuration.setChainPolicy(table, chain, policy);
    } else if (tailCtx.command_rename_chain() != null) {
      warn(ctx, "Rename Chain command is not supported");
    } else if (tailCtx.command_replace() != null) {
      warn(ctx, "Replace command is not supported");
    } else if (tailCtx.command_zero() != null) {
      warn(ctx, "Zero command is not supported");
    } else {
      warn(ctx, "Unknown command in rule");
    }
  }

  @Override
  public void exitDeclaration_chain_policy(Declaration_chain_policyContext ctx) {
    String chain = ctx.chain().getText();
    ChainPolicy policy = getBuiltInTarget(ctx.built_in_target());
    _configuration.setChainPolicy(_tableCurrent, chain, policy);
  }

  @Override
  public void exitDeclaration_table(Declaration_tableContext ctx) {
    _formatIptablesSave = true;
    _tableCurrent = ctx.table().getText();
  }

  private IptablesRule extractRule(Rule_specContext ctx) {
    IptablesRule rule = new IptablesRule();

    List<MatchContext> matches = ctx.match_list;

    for (MatchContext mCtx : matches) {

      boolean inverted = (mCtx.NOT() != null);

      if (mCtx.OPTION_IPV4() != null || mCtx.OPTION_IPV6() != null) {
        warn(ctx, String.format("Option '%s' is not supported", getFullText(mCtx)));
      } else if (mCtx.OPTION_DESTINATION() != null) {
        rule.addMatch(inverted, MatchType.DESTINATION, getEndpoint(mCtx.endpoint()));
      } else if (mCtx.OPTION_DESTINATION_PORT() != null) {
        rule.addMatch(inverted, MatchType.DESTINATION_PORT, toInteger(mCtx.port));
      } else if (mCtx.OPTION_IN_INTERFACE() != null) {
        rule.addMatch(inverted, MatchType.IN_INTERFACE, mCtx.interface_name.getText());
      } else if (mCtx.OPTION_MATCH() != null) {
        // iptables save does '-p tcp -m tcp' where '-m tcp' is redundant
        // spitting a warning for '-m tcp' is confusing in this case
        if (mCtx.match_module() != null && mCtx.match_module().match_module_tcp() != null) {
          boolean ruleHasProtocolTcp =
              rule.getMatchList().stream()
                  .anyMatch(
                      m ->
                          m.getMatchType() == MatchType.PROTOCOL
                              && m.getMatchData() == IpProtocol.TCP);
          if (!ruleHasProtocolTcp) {
            warn(
                ctx,
                String.format("Option '%s' is supported only with '-p tcp'", getFullText(mCtx)));
          }
        } else {
          warn(ctx, String.format("Option '%s' is not supported", getFullText(mCtx)));
        }
      } else if (mCtx.OPTION_PROTOCOL() != null) {
        rule.addMatch(inverted, MatchType.PROTOCOL, toProtocol(mCtx.protocol()));
      } else if (mCtx.OPTION_OUT_INTERFACE() != null) {
        rule.addMatch(inverted, MatchType.OUT_INTERFACE, mCtx.interface_name.getText());
      } else if (mCtx.OPTION_SOURCE() != null) {
        rule.addMatch(inverted, MatchType.SOURCE, getEndpoint(mCtx.endpoint()));
      } else if (mCtx.OPTION_SOURCE_PORT() != null) {
        rule.addMatch(inverted, MatchType.SOURCE_PORT, toInteger(mCtx.port));
      } else {
        warn(ctx, String.format("Option '%s' is not supported", getFullText(mCtx)));
      }
    }

    if (ctx.action().OPTION_JUMP() != null) {
      if (ctx.action().built_in_target() != null) {
        ChainPolicy policy = getBuiltInTarget(ctx.action().built_in_target());
        rule.setAction(policy);
      } else if (ctx.action().chain() != null) {
        rule.setAction(IptablesActionType.CHAIN, ctx.action().chain().getText());
      }
    } else if (ctx.action().OPTION_GOTO() != null) {
      rule.setAction(IptablesActionType.GOTO, ctx.action().chain().getText());
    } else {
      todo(ctx);
    }
    rule.setName(getFullText(ctx));
    return rule;
  }

  @Override
  public @Nonnull String getInputText() {
    return _text;
  }

  @Override
  public @Nonnull BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Override
  public @Nonnull Warnings getWarnings() {
    return _w;
  }

  private @Nullable ChainPolicy getBuiltInTarget(Built_in_targetContext ctx) {
    if (ctx.ACCEPT() != null) {
      return ChainPolicy.ACCEPT;
    } else if (ctx.DROP() != null) {
      return ChainPolicy.DROP;
    } else if (ctx.RETURN() != null) {
      return ChainPolicy.RETURN;
    } else {
      todo(ctx);
    }
    return null;
  }

  private @Nullable Object getEndpoint(EndpointContext endpoint) {
    if (endpoint.IP_ADDRESS() != null) {
      return Ip.parse(endpoint.IP_ADDRESS().getText());
    } else if (endpoint.IP_PREFIX() != null) {
      return Prefix.parse(endpoint.IP_PREFIX().getText());
    }
    todo(endpoint);
    return null;
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _vendorConfiguration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    walker.walk(this, tree);
  }

  private IpProtocol toProtocol(ProtocolContext protocol) {
    return IpProtocol.fromString(protocol.getText());
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }
}
