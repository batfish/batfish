package org.batfish.z3.state;

import java.util.List;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.RuleExpr;

public interface Transition<T> {
  List<RuleExpr> generate(SynthesizerInput input);
}
