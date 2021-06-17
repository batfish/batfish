package org.batfish.minesweeper.bdd;

import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.minesweeper.IDeepCopy;
import org.batfish.minesweeper.collections.PList;

public class TransferParam<T extends IDeepCopy<T>> {

  public enum CallContext {
    EXPR_CALL,
    STMT_CALL,
    NONE
  }

  public enum ChainContext {
    CONJUNCTION,
    DISJUNCTION,
    NONE
  }

  private T _data;

  private int _indent;

  private PList<String> _scopes;

  private CallContext _callContext;

  private ChainContext _chainContext;

  private BDD _defaultAccept;

  private BDD _defaultAcceptLocal;

  private SetDefaultPolicy _defaultPolicy;

  private boolean _debug;

  public TransferParam(T data, boolean debug) {
    _data = data;
    _callContext = CallContext.NONE;
    _chainContext = ChainContext.NONE;
    _indent = 0;
    _scopes = PList.empty();
    _defaultAccept = BDDRoute.factory.zero();
    _defaultAcceptLocal = BDDRoute.factory.zero();
    _defaultPolicy = null;
    _debug = debug;
  }

  private TransferParam(TransferParam<T> p) {
    _data = p._data;
    _callContext = p._callContext;
    _chainContext = p._chainContext;
    _indent = p._indent;
    _scopes = p._scopes;
    _defaultAccept = p._defaultAccept;
    _defaultAcceptLocal = p._defaultAcceptLocal;
    _defaultPolicy = p._defaultPolicy;
    _debug = p._debug;
  }

  public T getData() {
    return _data;
  }

  public CallContext getCallContext() {
    return _callContext;
  }

  public ChainContext getChainContext() {
    return _chainContext;
  }

  public BDD getDefaultAccept() {
    return _defaultAccept;
  }

  public BDD getDefaultAcceptLocal() {
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

  public TransferParam<T> deepCopy() {
    TransferParam<T> ret = new TransferParam<>(this);
    ret._data = ret._data.deepCopy();
    return ret;
  }

  public TransferParam<T> setData(T other) {
    TransferParam<T> ret = new TransferParam<>(this);
    ret._data = other;
    return ret;
  }

  public TransferParam<T> setCallContext(CallContext cc) {
    TransferParam<T> ret = new TransferParam<>(this);
    ret._callContext = cc;
    return ret;
  }

  public TransferParam<T> setChainContext(ChainContext cc) {
    TransferParam<T> ret = new TransferParam<>(this);
    ret._chainContext = cc;
    return ret;
  }

  public TransferParam<T> setDefaultAccept(BDD defaultAccept) {
    TransferParam<T> ret = new TransferParam<>(this);
    ret._defaultAccept = defaultAccept;
    return ret;
  }

  public TransferParam<T> setDefaultPolicy(@Nullable SetDefaultPolicy defaultPolicy) {
    TransferParam<T> ret = new TransferParam<>(this);
    ret._defaultPolicy = defaultPolicy;
    return ret;
  }

  public TransferParam<T> setDefaultAcceptLocal(BDD defaultAcceptLocal) {
    TransferParam<T> ret = new TransferParam<>(this);
    ret._defaultAcceptLocal = defaultAcceptLocal;
    return ret;
  }

  public TransferParam<T> setDefaultsFrom(TransferParam<T> updatedParam) {
    return setDefaultAccept(updatedParam._defaultAccept)
        .setDefaultPolicy(updatedParam._defaultPolicy)
        .setDefaultAcceptLocal(updatedParam._defaultAcceptLocal);
  }

  public TransferParam<T> enterScope(String name) {
    TransferParam<T> ret = new TransferParam<>(this);
    ret._scopes = ret._scopes.plus(name);
    return ret;
  }

  public TransferParam<T> indent() {
    TransferParam<T> ret = new TransferParam<>(this);
    ret._indent = _indent + 1;
    return ret;
  }

  public void debug(String str) {
    if (_debug) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < _indent; i++) {
        sb.append("    ");
      }
      String s = _scopes.get(0);
      String scope = (s == null ? "" : s);
      sb.append("[");
      sb.append(scope);
      sb.append("]: ");
      sb.append(str);
      System.out.println(sb);
    }
  }
}
