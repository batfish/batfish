package org.batfish.symbolic.bdd;

import java.util.Map;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDSourceNat;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.transformation.DynamicNatRule;
import org.batfish.datamodel.transformation.GenericTransformationRuleVisitor;
import org.batfish.datamodel.transformation.StaticNatRule;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.Transformation.RuleAction;

/** Visitor that converts a {@link Transformation} to a @{link BDDSourceNat}. */
// TODO more javadoc
// TODO need to generalize this and BDDSourceNat
public class TransformationToBDD implements GenericTransformationRuleVisitor<BDDSourceNat> {

  private final Map<String, Map<String, BDD>> _aclPermitBDDs;
  private final BDDPacket _bddPacket;
  private final String _node;

  public TransformationToBDD(String node, Map<String, Map<String, BDD>> aclPermitBDDs, BDDPacket bddPacket) {
    _aclPermitBDDs = aclPermitBDDs;
    _bddPacket = bddPacket;
    _node = node;
  }

  // TODO static NAT
  @Override
  public BDDSourceNat visitStaticTransformationRule(StaticNatRule rule) {
    return null;
  }

  @Override
  public BDDSourceNat visitDynamicTransformationRule(DynamicNatRule rule) {
    // TODO Require src interface to match if specified in rule

    if (rule.getAcl() == null) {
      // TODO handle dynamic NATs without ACLs (permit all)
      return null;
    }
    if (rule.getAction() != RuleAction.SOURCE_INSIDE) {
      // TODO handle destination NAT
      return null;
    }

    String aclName = rule.getAcl().getName();
    BDD match = _aclPermitBDDs.get(_node).get(aclName);
    BDD setSrcIp =
        _bddPacket
            .getSrcIp()
            .geq(rule.getPoolIpFirst().asLong())
            .and(_bddPacket.getSrcIp().leq(rule.getPoolIpLast().asLong()));
    return new BDDSourceNat(match, setSrcIp);
  }
}
