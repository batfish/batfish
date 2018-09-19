package org.batfish.datamodel.transformation;

import javax.annotation.Nullable;

public interface GenericTransformationRuleVisitor<R> {

  @Nullable
  default R visit(Transformation rule) {
    return rule.accept(this);
  }

  R visitStaticTransformationRule(StaticNatRule rule);

  @Nullable
  R visitDynamicTransformationRule(DynamicNatRule rule);
}
