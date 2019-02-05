package org.batfish.grammar.f5_bigip_structured;

import static org.batfish.representation.f5_bigip.F5BigipStructureType.INTERFACE;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.SELF;
import static org.batfish.representation.f5_bigip.F5BigipStructureType.VLAN;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.INTERFACE_SELF_REFERENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.SELF_SELF_REFERENCE;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.SELF_VLAN;
import static org.batfish.representation.f5_bigip.F5BigipStructureUsage.VLAN_INTERFACE;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Bundle_speedContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.F5_bigip_structured_configurationContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Net_interfaceContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Net_selfContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Net_vlanContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ns_addressContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Ns_vlanContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nv_tagContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Nvi_interfaceContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.Sgs_hostnameContext;
import org.batfish.grammar.f5_bigip_structured.F5BigipStructuredParser.UContext;
import org.batfish.representation.f5_bigip.F5BigipConfiguration;
import org.batfish.representation.f5_bigip.Interface;
import org.batfish.representation.f5_bigip.Self;
import org.batfish.representation.f5_bigip.Vlan;
import org.batfish.representation.f5_bigip.VlanInterface;
import org.batfish.vendor.StructureType;

@ParametersAreNonnullByDefault
public class F5BigipStructuredConfigurationBuilder extends F5BigipStructuredParserBaseListener {

  static String unquote(String text) {
    if (text.length() == 0) {
      return text;
    }
    if (text.charAt(0) != '"') {
      return text;
    } else if (text.charAt(text.length() - 1) != '"') {
      throw new BatfishException("Improperly-quoted string");
    } else {
      return text.substring(1, text.length() - 1);
    }
  }

  private @Nullable F5BigipConfiguration _c;
  private @Nullable Interface _currentInterface;
  private @Nullable Self _currentSelf;
  private @Nullable UContext _currentU;
  private @Nullable Vlan _currentVlan;
  private @Nullable VlanInterface _currentVlanInterface;
  private final F5BigipStructuredCombinedParser _parser;
  private final String _text;
  private final Warnings _w;

  public F5BigipStructuredConfigurationBuilder(
      F5BigipStructuredCombinedParser parser, String text, Warnings w) {
    _parser = parser;
    _text = text;
    _w = w;
  }

  private String convErrorMessage(Class<?> type, ParserRuleContext ctx) {
    return String.format("Could not convert to %s: %s", type.getSimpleName(), getFullText(ctx));
  }

  private <T, U extends T> T convProblem(
      Class<T> returnType, ParserRuleContext ctx, @Nullable U defaultReturnValue) {
    _w.redFlag(convErrorMessage(returnType, ctx));
    return defaultReturnValue;
  }

  /** Mark the specified structure as defined on each line in the supplied context */
  private void defineStructure(StructureType type, String name, RuleContext ctx) {
    /* Recursively process children to find all relevant definition lines for the specified context */
    for (int i = 0; i < ctx.getChildCount(); i++) {
      ParseTree child = ctx.getChild(i);
      if (child instanceof TerminalNode) {
        _c.defineStructure(type, name, ((TerminalNode) child).getSymbol().getLine());
      } else if (child instanceof RuleContext) {
        defineStructure(type, name, (RuleContext) child);
      }
    }
  }

  @Override
  public void enterF5_bigip_structured_configuration(F5_bigip_structured_configurationContext ctx) {
    _c = new F5BigipConfiguration();
  }

  @Override
  public void enterNet_interface(Net_interfaceContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(INTERFACE, name, ctx);
    _c.referenceStructure(INTERFACE, name, INTERFACE_SELF_REFERENCE, ctx.name.getStart().getLine());
    _currentInterface = _c.getInterfaces().computeIfAbsent(name, Interface::new);
  }

  @Override
  public void enterNet_self(Net_selfContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(SELF, name, ctx);
    _c.referenceStructure(SELF, name, SELF_SELF_REFERENCE, ctx.name.getStart().getLine());
    _currentSelf = _c.getSelves().computeIfAbsent(name, Self::new);
  }

  @Override
  public void enterNet_vlan(Net_vlanContext ctx) {
    String name = unquote(ctx.name.getText());
    defineStructure(VLAN, name, ctx);
    _currentVlan = _c.getVlans().computeIfAbsent(name, Vlan::new);
  }

  @Override
  public void enterU(UContext ctx) {
    if (_currentU == null) {
      _currentU = ctx;
    }
  }

  @Override
  public void exitBundle_speed(Bundle_speedContext ctx) {
    Double speed = toSpeed(ctx);
    _currentInterface.setSpeed(speed);
  }

  @Override
  public void exitNet_interface(Net_interfaceContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitNet_self(Net_selfContext ctx) {
    _currentSelf = null;
  }

  @Override
  public void exitNet_vlan(Net_vlanContext ctx) {
    _currentVlan = null;
  }

  @Override
  public void exitNs_address(Ns_addressContext ctx) {
    _currentSelf.setAddress(new InterfaceAddress(ctx.interface_address.getText()));
  }

  @Override
  public void exitNs_vlan(Ns_vlanContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(VLAN, name, SELF_VLAN, ctx.name.getStart().getLine());
    _currentSelf.setVlan(name);
  }

  @Override
  public void exitNv_tag(Nv_tagContext ctx) {
    _currentVlan.setTag(toInteger(ctx.tag));
  }

  @Override
  public void exitNvi_interface(Nvi_interfaceContext ctx) {
    String name = unquote(ctx.name.getText());
    _c.referenceStructure(INTERFACE, name, VLAN_INTERFACE, ctx.name.getStart().getLine());
    _currentVlanInterface = _currentVlan.getInterfaces().computeIfAbsent(name, VlanInterface::new);
  }

  @Override
  public void exitSgs_hostname(Sgs_hostnameContext ctx) {
    String hostname = unquote(ctx.hostname.getText());
    _c.setHostname(hostname);
  }

  @Override
  public void exitU(UContext ctx) {
    if (_currentU != ctx) {
      return;
    }
    unrecognized(ctx);
    _currentU = null;
  }

  public F5BigipConfiguration getConfiguration() {
    return _c;
  }

  private @Nonnull String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    String text = _text.substring(start, end + 1);
    return text;
  }

  private int toInteger(ParserRuleContext ctx) {
    return Integer.parseUnsignedInt(ctx.getText(), 10);
  }

  private @Nullable Double toSpeed(Bundle_speedContext ctx) {
    if (ctx.FORTY_G() != null) {
      return 40E9D;
    } else if (ctx.ONE_HUNDRED_G() != null) {
      return 100E9D;
    } else {
      return convProblem(Double.class, ctx, null);
    }
  }

  private void unrecognized(UContext ctx) {
    Token start = ctx.getStart();
    int line = start.getLine();
    _w.getParseWarnings()
        .add(
            new ParseWarning(
                line,
                start.getText(),
                ctx.toString(Arrays.asList(_parser.getParser().getRuleNames())),
                "This syntax is unrecognized"));
  }
}
