package org.batfish.smt;


import com.microsoft.z3.BoolExpr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SymbolicOriginatorId extends SymbolicEnum<Integer> {

    private static List<Integer> addZero(Collection<Integer> vals) {
        List<Integer> values = new ArrayList<>(vals);
        values.add(0);
        Collections.sort(values);
        return values;
    }

    private static List<Integer> values(EncoderSlice slice) {
        return addZero(slice.getGraph().getOriginatorId().values());
    }

    public SymbolicOriginatorId(EncoderSlice slice, String name) {
        super(slice, values(slice), name);
    }

    public SymbolicOriginatorId(EncoderSlice slice, Integer value) {
        super(slice, values(slice), value);
    }

    SymbolicOriginatorId(SymbolicOriginatorId other) {
        super(other);
    }

    BoolExpr isNotFromClient() {
        if (this._bitvec == null) {
            return _enc.True();
        }
        return _enc.getCtx().mkEq(_bitvec, _enc.getCtx().mkBV(0, _numBits));
    }

    BoolExpr isFromClient() {
        return _enc.Not(isNotFromClient());
    }
}
