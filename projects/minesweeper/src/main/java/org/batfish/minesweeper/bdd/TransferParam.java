package org.batfish.minesweeper.bdd;

import com.google.errorprone.annotations.FormatMethod;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.minesweeper.collections.PList;

public class TransferParam {
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

  private @Nonnull BDDRoute _data;

  private int _indent;

  private @Nonnull PList<String> _scopes;

  private CallContext _callContext;

  private ChainContext _chainContext;

  private boolean _defaultAccept;

  private boolean _defaultAcceptLocal;

  private @Nullable SetDefaultPolicy _defaultPolicy;

  private boolean _readIntermediateBgpAttributes;

  private final boolean _debug;

  public TransferParam(@Nonnull BDDRoute data, boolean debug) {
    _data = data;
    _callContext = CallContext.NONE;
    _chainContext = ChainContext.NONE;
    _indent = 0;
    _scopes = PList.empty();
    _defaultAccept = false;
    _defaultAcceptLocal = false;
    _defaultPolicy = null;
    _readIntermediateBgpAttributes = false;
    _debug = debug;
  }

  private TransferParam(TransferParam p) {
    _data = p._data;
    _callContext = p._callContext;
    _chainContext = p._chainContext;
    _indent = p._indent;
    _scopes = p._scopes;
    _defaultAccept = p._defaultAccept;
    _defaultAcceptLocal = p._defaultAcceptLocal;
    _defaultPolicy = p._defaultPolicy;
    _readIntermediateBgpAttributes = p._readIntermediateBgpAttributes;
    _debug = p._debug;
  }

  public @Nonnull BDDRoute getData() {
    return _data;
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

  public @Nullable SetDefaultPolicy getDefaultPolicy() {
    return _defaultPolicy;
  }

  public boolean getReadIntermediateBgpAtttributes() {
    return _readIntermediateBgpAttributes;
  }

  public boolean getInitialCall() {
    return _indent == 0;
  }

  public String getScope() {
    return _scopes.get(0);
  }

  public TransferParam deepCopy() {
    TransferParam ret = new TransferParam(this);
    ret._data = ret._data.deepCopy();
    return ret;
  }

  public TransferParam setData(BDDRoute other) {
    TransferParam ret = new TransferParam(this);
    ret._data = other;
    return ret;
  }

  public TransferParam setCallContext(CallContext cc) {
    TransferParam ret = new TransferParam(this);
    ret._callContext = cc;
    return ret;
  }

  public TransferParam setChainContext(ChainContext cc) {
    TransferParam ret = new TransferParam(this);
    ret._chainContext = cc;
    return ret;
  }

  public TransferParam setDefaultAccept(boolean defaultAccept) {
    TransferParam ret = new TransferParam(this);
    ret._defaultAccept = defaultAccept;
    return ret;
  }

  public TransferParam setDefaultPolicy(@Nullable SetDefaultPolicy defaultPolicy) {
    TransferParam ret = new TransferParam(this);
    ret._defaultPolicy = defaultPolicy;
    return ret;
  }

  public TransferParam setDefaultAcceptLocal(boolean defaultAcceptLocal) {
    TransferParam ret = new TransferParam(this);
    ret._defaultAcceptLocal = defaultAcceptLocal;
    return ret;
  }

  public TransferParam setDefaultActionsFrom(TransferParam updatedParam) {
    return setDefaultAccept(updatedParam._defaultAccept)
        .setDefaultAcceptLocal(updatedParam._defaultAcceptLocal);
  }

  public TransferParam setReadIntermediateBgpAttributes(boolean b) {
    TransferParam ret = new TransferParam(this);
    ret._readIntermediateBgpAttributes = b;
    return ret;
  }

  public TransferParam enterScope(String name) {
    TransferParam ret = new TransferParam(this);
    ret._scopes = ret._scopes.plus(name);
    return ret;
  }

  public TransferParam indent() {
    TransferParam ret = new TransferParam(this);
    ret._indent = _indent + 1;
    return ret;
  }

  @FormatMethod
  public void debug(String fmt, Object... args) {
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
      sb.append(String.format(fmt, args));
      System.out.println(sb);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TransferParam)) {
      return false;
    }
    TransferParam that = (TransferParam) o;
    return _indent == that._indent
        && _defaultAccept == that._defaultAccept
        && _defaultAcceptLocal == that._defaultAcceptLocal
        && _readIntermediateBgpAttributes == that._readIntermediateBgpAttributes
        && _debug == that._debug
        && _data.equals(that._data)
        && _scopes.equals(that._scopes)
        && _callContext == that._callContext
        && _chainContext == that._chainContext
        && Objects.equals(_defaultPolicy, that._defaultPolicy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _data,
        _indent,
        _scopes,
        _callContext,
        _chainContext,
        _defaultAccept,
        _defaultAcceptLocal,
        _defaultPolicy,
        _readIntermediateBgpAttributes,
        _debug);
  }
}
