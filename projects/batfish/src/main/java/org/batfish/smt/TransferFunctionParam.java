package org.batfish.smt;


import org.batfish.datamodel.routing_policy.statement.*;
import org.batfish.smt.collections.PList;


public class TransferFunctionParam {

    public enum CallContext {EXPR_CALL, STMT_CALL, NONE}

    public enum ChainContext {CONJUNCTION, DISJUNCTION, NONE}

    private SymbolicRecord _other;

    private int _indent;

    private PList<String> _scopes;

    private CallContext _callContext;

    private ChainContext _chainContext;

    private boolean _defaultAccept;

    private boolean _defaultAcceptLocal;

    private SetDefaultPolicy _defaultPolicy;

    public TransferFunctionParam(SymbolicRecord other) {
        _other = other;
        _callContext = CallContext.NONE;
        _chainContext = ChainContext.NONE;
        _indent = 0;
        _scopes = PList.empty();
        _defaultAccept = false;
        _defaultAcceptLocal = false;
        _defaultPolicy = null;
    }

    private TransferFunctionParam(TransferFunctionParam p) {
        _other = p._other;
        _callContext = p._callContext;
        _chainContext = p._chainContext;
        _indent = p._indent;
        _scopes = p._scopes;
        _defaultAccept = p._defaultAccept;
        _defaultAcceptLocal = p._defaultAcceptLocal;
        _defaultPolicy = p._defaultPolicy;
    }

    public SymbolicRecord getOther() {
        return _other;
    }

    public CallContext getCallContext() {
        return _callContext;
    }

    public ChainContext getChainContext() {
        return _chainContext;
    }

    public boolean getDefaultAccept() {
        return _defaultAccept;
    }


    public boolean getDefaultAcceptLocal() {
        return _defaultAcceptLocal;
    }

    public SetDefaultPolicy getDefaultPolicy() {
        return _defaultPolicy;
    }

    public boolean getInitialCall() {
        return _indent == 0;
    }

    public String getScope() {
        return _scopes.get(0);
    }

    public TransferFunctionParam setCallContext(CallContext cc) {
        TransferFunctionParam ret = new TransferFunctionParam(this);
        ret._callContext = cc;
        return ret;
    }

    public TransferFunctionParam setChainContext(ChainContext cc) {
        TransferFunctionParam ret = new TransferFunctionParam(this);
        ret._chainContext = cc;
        return ret;
    }

    public TransferFunctionParam copyRecord() {
        TransferFunctionParam ret = new TransferFunctionParam(this);
        ret._other = new SymbolicRecord(this._other);
        return ret;
    }

    public TransferFunctionParam setDefaultAccept(boolean _defaultAccept) {
        TransferFunctionParam ret = new TransferFunctionParam(this);
        ret._defaultAccept = _defaultAccept;
        return ret;
    }

    public TransferFunctionParam setDefaultPolicy(SetDefaultPolicy _defaultPolicy) {
        TransferFunctionParam ret = new TransferFunctionParam(this);
        ret._defaultPolicy = _defaultPolicy;
        return ret;
    }


    public TransferFunctionParam setDefaultAcceptLocal(boolean _defaultAcceptLocal) {
        TransferFunctionParam ret = new TransferFunctionParam(this);
        ret._defaultAcceptLocal = _defaultAcceptLocal;
        return ret;
    }

    public TransferFunctionParam enterScope(String name) {
        TransferFunctionParam ret = new TransferFunctionParam(this);
        ret._scopes = ret._scopes.plus(name);
        return ret;
    }

    public TransferFunctionParam indent() {
        TransferFunctionParam ret = new TransferFunctionParam(this);
        ret._indent = this._indent + 1;
        return ret;
    }

    public void debug(String str) {
        if (Encoder.ENABLE_DEBUGGING) {
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<_indent; i++) {
                sb.append("    ");
            }
            String s = _scopes.get(0);
            String scope = (s == null ? "" : s);
            sb.append("[");
            sb.append(scope);
            sb.append("]: ");
            sb.append(str);
            System.out.println(sb.toString());
        }
    }
}
