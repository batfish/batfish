package org.batfish.grammar.recovery_rule_alts;

import org.batfish.grammar.recovery_rule_alts.RecoveryRuleAltsParser.I_mtuContext;
import org.batfish.grammar.recovery_rule_alts.RecoveryRuleAltsParser.Iip_ospf_costContext;
import org.batfish.grammar.recovery_rule_alts.RecoveryRuleAltsParser.Ip_routingContext;
import org.batfish.grammar.recovery_rule_alts.RecoveryRuleAltsParser.S_interfaceContext;

public final class RecoveryRuleAltsExtractor extends RecoveryRuleAltsParserBaseListener {

  public int getInterfaceCount() {
    return _interfaceCount;
  }

  public int getInterfaceIpOspfCostCount() {
    return _interfaceIpOspfCostCount;
  }

  public int getInterfaceMtuCount() {
    return _interfaceMtuCount;
  }

  public int getIpRoutingCount() {
    return _ipRoutingCount;
  }

  @Override
  public void exitI_mtu(I_mtuContext ctx) {
    _interfaceMtuCount++;
  }

  @Override
  public void exitIp_routing(Ip_routingContext ctx) {
    _ipRoutingCount++;
  }

  @Override
  public void exitIip_ospf_cost(Iip_ospf_costContext ctx) {
    _interfaceIpOspfCostCount++;
  }

  @Override
  public void exitS_interface(S_interfaceContext ctx) {
    _interfaceCount++;
  }

  private int _interfaceCount;
  private int _interfaceIpOspfCostCount;
  private int _interfaceMtuCount;
  private int _ipRoutingCount;
}
