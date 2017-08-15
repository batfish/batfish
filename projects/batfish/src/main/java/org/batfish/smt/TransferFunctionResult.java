package org.batfish.smt;


import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import org.batfish.common.Pair;
import org.batfish.smt.collections.PList;

import java.util.HashSet;
import java.util.Set;

public class TransferFunctionResult {

    private PList<Pair<String,Expr>> _changedVariables; // should be a map

    private BoolExpr _returnValue;

    private BoolExpr _fallthroughValue;

    private BoolExpr _returnAssignedValue;

    public TransferFunctionResult() {
        this._changedVariables = PList.empty();
        this._returnValue = null;
        this._fallthroughValue = null;
        this._returnAssignedValue = null;
    }

    public TransferFunctionResult(TransferFunctionResult other) {
        this._changedVariables = other._changedVariables;
        this._returnValue = other._returnValue;
        this._fallthroughValue = other._fallthroughValue;
        this._returnAssignedValue = other._returnAssignedValue;
    }


    private Expr find(PList<Pair<String,Expr>> vals, String s) {
        for (Pair<String, Expr> pair : vals) {
            if (pair.getFirst().equals(s)) {
                return pair.getSecond();
            }
        }
        return null;
    }


    // TODO: this really needs to use persistent set data types
    public PList<Pair<String, Pair<Expr,Expr>>> mergeChangedVariables(TransferFunctionResult other) {
        Set<String> seen = new HashSet<>();
        PList<Pair<String, Pair<Expr,Expr>>> vars = PList.empty();

        for (Pair<String, Expr> cv1 : this._changedVariables) {
            String s = cv1.getFirst();
            Expr x = cv1.getSecond();
            if (!seen.contains(s)) {
                seen.add(s);
                Expr e = find(other._changedVariables, s);
                Pair<Expr, Expr> pair = new Pair<>(x, e);
                vars = vars.plus(new Pair<>(s, pair));
            }
        }

        for (Pair<String, Expr> cv1 : other._changedVariables) {
            String s = cv1.getFirst();
            Expr x = cv1.getSecond();
            if (!seen.contains(s)) {
                seen.add(s);
                Expr e = find(this._changedVariables, s);
                Pair<Expr, Expr> pair = new Pair<>(e,x); // preserve order
                vars = vars.plus(new Pair<>(s, pair));
            }
        }

        return vars;
    }

    public PList<Pair<String,Expr>> getChangedVariables() {
        return _changedVariables;
    }

    public BoolExpr getReturnValue() {
        return _returnValue;
    }

    public BoolExpr getFallthroughValue() {
        return _fallthroughValue;
    }

    public BoolExpr getReturnAssignedValue() {
        return _returnAssignedValue;
    }

    public TransferFunctionResult addChangedVariable(String s, Expr x) {
        TransferFunctionResult ret = new TransferFunctionResult(this);
        ret._changedVariables = ret._changedVariables.plus(new Pair<>(s,x));
        return ret;
    }

    public TransferFunctionResult addChangedVariables(TransferFunctionResult other) {
        TransferFunctionResult ret = new TransferFunctionResult(this);
        ret._changedVariables.plusAll(other._changedVariables);
        return ret;
    }

    public boolean isChanged(String s) {
        for (Pair<String, Expr> pair : this._changedVariables) {
            if (pair.getFirst().equals(s)) {
                return true;
            }
        }
        return false;
    }

    public TransferFunctionResult clearChanged() {
        TransferFunctionResult ret = new TransferFunctionResult(this);
        ret._changedVariables = PList.empty();
        return ret;
    }

    public TransferFunctionResult setReturnValue(BoolExpr x) {
        TransferFunctionResult ret = new TransferFunctionResult(this);
        ret._returnValue = x;
        return ret;
    }

    public TransferFunctionResult setFallthroughValue(BoolExpr x) {
        TransferFunctionResult ret = new TransferFunctionResult(this);
        ret._fallthroughValue = x;
        return ret;
    }


    public TransferFunctionResult setReturnAssignedValue(BoolExpr x) {
        TransferFunctionResult ret = new TransferFunctionResult(this);
        ret._returnAssignedValue = x;
        return ret;
    }
}
