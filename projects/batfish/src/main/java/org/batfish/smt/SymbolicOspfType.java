package org.batfish.smt;

import com.microsoft.z3.BoolExpr;


/**
 * <p>A symbolic enum representing the OSPF type,
 * which is either O, OIA, E1, E2. </p>
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

    BoolExpr isExternal() {
        if (this._bitvec == null) {
            return _enc.False();
        }
        return _enc.getCtx().mkBVUGE(_bitvec, _enc.getCtx().mkBV(2, 2));
    }

    BoolExpr isInternal() {
        if (this._bitvec == null) {
            return _enc.True();
        }
        return _enc.getCtx().mkBVULE(_bitvec, _enc.getCtx().mkBV(1, 2));
    }

}
