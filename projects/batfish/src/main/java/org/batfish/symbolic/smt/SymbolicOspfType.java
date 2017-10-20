package org.batfish.symbolic.smt;

import com.microsoft.z3.BoolExpr;
import org.batfish.symbolic.OspfType;

/**
 * A symbolic enum representing the OSPF type, which is either O, OIA, E1, E2.
 *
 * @author Ryan Beckett
 */
class SymbolicOspfType extends SymbolicEnum<OspfType> {

  SymbolicOspfType(EncoderSlice slice, String name) {
    super(slice, OspfType.values, name);
  }

  SymbolicOspfType(EncoderSlice slice, OspfType t) {
    super(slice, OspfType.values, t);
  }

  SymbolicOspfType(SymbolicOspfType other) {
    super(other);
  }

  BoolExpr isInternal() {
    if (this._bitvec == null) {
      return _enc.mkTrue();
    }
    return _enc.getCtx().mkBVULE(_bitvec, _enc.getCtx().mkBV(1, 2));
  }
}
