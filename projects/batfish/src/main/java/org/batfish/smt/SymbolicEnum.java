package org.batfish.smt;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a symbolic variable for a small, finite number of choices.
 * For optimization purposes, we use a small domain bitvector to represent the possble
 * choices.
 *
 * @param <T> The underlying domain of values
 * @author Ryan Beckett
 */

class SymbolicEnum<T> {

    protected EncoderSlice _enc;

    protected BitVecExpr _bitvec;

    protected int _numBits;

    protected List<T> _values;

    protected Map<T, BitVecExpr> _valueMap;


    SymbolicEnum(EncoderSlice slice, List<T> values, String name) {
        _enc = slice;
        _numBits = numBits(values);
        int size = values.size();
        initValues(values);

        if (_numBits == 0) {
            _bitvec = null;
        } else {

            _bitvec = _enc.getCtx().mkBVConst(name, _numBits);

            if (name != null) {
                slice.getAllVariables().put(_bitvec.toString(), _bitvec);
            }

            if (!isPowerOfTwo(size)) {
                BitVecExpr maxValue = slice.getCtx().mkBV(size-1, _numBits);
                BoolExpr constraint = slice.getCtx().mkBVULE(_bitvec, maxValue);
                slice.add( constraint );
            }
        }
    }

    SymbolicEnum(SymbolicEnum<T> other) {
        _enc = other._enc;
        _bitvec = other._bitvec;
        _numBits = other._numBits;
        _values = other._values;
        _valueMap = other._valueMap;
    }


    protected SymbolicEnum(EncoderSlice slice, List<T> values, T value) {
        _enc = slice;
        int idx = values.indexOf(value);
        _numBits = numBits(values);

        if (_numBits == 0) {
            _bitvec = null;
        } else {
            _bitvec = _enc.getCtx().mkBV(idx, _numBits);
        }

        initValues(values);
    }

    private int numBits(List<T> values) {
        int size = values.size();
        double log = Math.log((double) size);
        double base = Math.log((double) 2);
        if (size == 0) {
            return 0;
        } else {
            return ((int) Math.ceil(log / base));
        }
    }

    private void initValues(List<T> values) {
        int i = 0;
        _values = new ArrayList<>();
        _valueMap = new HashMap<>();
        for (T value : values) {
            _values.add(value);
            if (_numBits > 0) {
                _valueMap.put(value, _enc.getCtx().mkBV(i, _numBits));
            }
            i++;
        }
    }

    private boolean isPowerOfTwo(int x) {
        return (x & -x) == x;
    }

    public void setBitVec(BitVecExpr bv) {
        this._bitvec = bv;
    }

    public BoolExpr Eq(SymbolicEnum<T> other) {
        if (_bitvec == null || other._bitvec == null) {
            return _enc.True();
        }
        return _enc.Eq(_bitvec, other._bitvec);
    }

    public BoolExpr checkIfValue(T p) {
        if (_bitvec == null) {
            T q = _values.get(0);
            return _enc.Bool(p == q);
        }

        BitVecExpr bv = _valueMap.get(p);
        if (bv == null) {
            return _enc.False();
        }

        return _enc.Eq(_bitvec, bv);
    }

    public BoolExpr isDefaultValue() {
        if (_bitvec == null) {
            return _enc.True();
        }
        return _enc.Eq(_bitvec, _enc.getCtx().mkBV(0, _numBits));
    }

    public BitVecExpr defaultValue() {
        return _enc.getCtx().mkBV(0, _numBits);
    }

    public T value(int i) {
        return _values.get(i);
    }

    public BitVecExpr getBitVec() {
        return _bitvec;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SymbolicEnum<?> that = (SymbolicEnum<?>) o;

        if (_numBits != that._numBits)
            return false;
        return _bitvec != null ? _bitvec.equals(that._bitvec) : that._bitvec == null;
    }

    @Override
    public int hashCode() {
        int result = _bitvec != null ? _bitvec.hashCode() : 0;
        result = 31 * result + _numBits;
        return result;
    }
}
