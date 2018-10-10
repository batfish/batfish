package org.batfish.symbolic.bdd;

import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDSourceNat;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.transformation.DynamicNatRule;
import org.batfish.datamodel.transformation.GenericTransformationRuleVisitor;
import org.batfish.datamodel.transformation.StaticNatRule;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.Transformation.RuleAction;

/** Visitor that converts a {@link Transformation} to a @{link BDDSourceNat}. */
public class TransformationToBDD implements GenericTransformationRuleVisitor<BDDSourceNat> {

  private final Map<String, Map<String, Supplier<BDD>>> _aclPermitBDDs;
  private final BDDPacket _bddPacket;
  private final String _node;

  public TransformationToBDD(
      String node, Map<String, Map<String, Supplier<BDD>>> aclPermitBDDs, BDDPacket bddPacket) {
    _aclPermitBDDs = aclPermitBDDs;
    _bddPacket = bddPacket;
    _node = node;
  }

  // Static NAT not implemented
  @Nullable
  @Override
  public BDDSourceNat visitStaticTransformationRule(StaticNatRule rule) {
    return null;
  }

  @Override
  public BDDSourceNat visitDynamicTransformationRule(DynamicNatRule rule) {
    // Source interface matching not implemented

    if (rule.getAcl() == null) {
      // Handling dynamic NATs without ACLs is not implemented
      // This is Arista-specific
      return null;
    }
    if (rule.getAction() != RuleAction.SOURCE_INSIDE) {
      // Destination NAT not supported in BDD
      return null;
    }

    String aclName = rule.getAcl().getName();
    BDD match = _aclPermitBDDs.get(_node).get(aclName).get();
    BDD setSrcIp =
        _bddPacket
            .getSrcIp()
            .geq(rule.getPoolIpFirst().asLong())
            .and(_bddPacket.getSrcIp().leq(rule.getPoolIpLast().asLong()));
    return new BDDSourceNat(match, setSrcIp);
  }
}
