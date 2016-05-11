package org.batfish.datamodel.questions;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.batfish.common.CleanBatfishException;
import org.batfish.common.util.SubRange;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NamedStructType;
import org.batfish.datamodel.NeighborType;
import org.batfish.datamodel.NodeType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.collections.NeighborTypeSet;
import org.batfish.datamodel.collections.NodeTypeSet;

public class QuestionParameters {

	private Map<String, Object> _store;

	private Map<String, VariableType> _typeBindings;

	public QuestionParameters() {
		_store = new HashMap<String, Object>();
		_typeBindings = new HashMap<String, VariableType>();
	}

	private void confirmTypeBinding(String var, VariableType expectedType) {
		VariableType actualType = _typeBindings.get(var);
		if (actualType == null) {
			throw new CleanBatfishException(
					"You must provide a value for question parameter: \"" + var
					+ "\"");
		}
		if (actualType != expectedType) {
			throw new CleanBatfishException(
					"Expected type of question parameter \"" + var + "\" was "
							+ expectedType.toString() + ", but provided type was: "
							+ actualType);
		}
	}

	public ForwardingAction getAction(String var) {
		confirmTypeBinding(var, VariableType.ACTION);
		return (ForwardingAction) _store.get(var);
	}

	public boolean getBoolean(String var) {
		confirmTypeBinding(var, VariableType.BOOLEAN);
		return (boolean) _store.get(var);
	}

	public long getInt(String var) {
		confirmTypeBinding(var, VariableType.INT);
		return (long) _store.get(var);
	}

	public Ip getIp(String var) {
		confirmTypeBinding(var, VariableType.IP);
		return (Ip) _store.get(var);
	}

   public NamedStructType getNamedStructType(String var) {
      confirmTypeBinding(var, VariableType.NAMED_STRUCT_TYPE);
      return (NamedStructType) _store.get(var);
   }

	public NeighborType getNeighborType(String var) {
		confirmTypeBinding(var, VariableType.NEIGHBOR_TYPE);
		return (NeighborType) _store.get(var);
	}

   public NeighborTypeSet getNeighborTypeSet(String var) {
      confirmTypeBinding(var, VariableType.SET_NEIGHBOR_TYPE);
      return (NeighborTypeSet) _store.get(var);
   }

	public NodeType getNodeType(String var) {
		confirmTypeBinding(var, VariableType.NODE_TYPE);
		return (NodeType) _store.get(var);
	}

   public NodeTypeSet getNodeTypeSet(String var) {
      confirmTypeBinding(var, VariableType.SET_NODE_TYPE);
      return (NodeTypeSet) _store.get(var);
   }

	public Prefix getPrefix(String var) {
		confirmTypeBinding(var, VariableType.PREFIX);
		return (Prefix) _store.get(var);
	}

	@SuppressWarnings("unchecked")
	public Set<Prefix> getPrefixSet(String var) {
		confirmTypeBinding(var, VariableType.SET_PREFIX);
		return (Set<Prefix>) _store.get(var);
	}

	@SuppressWarnings("unchecked")
	public Set<SubRange> getRange(String var) {
		confirmTypeBinding(var, VariableType.RANGE);
		return (Set<SubRange>) _store.get(var);
	}

	public String getRegex(String var) {
		confirmTypeBinding(var, VariableType.REGEX);
		return (String) _store.get(var);
	}

	public Map<String, Object> getStore() {
		return _store;
	}

	public String getString(String var) {
		confirmTypeBinding(var, VariableType.STRING);
		return (String) _store.get(var);
	}

	@SuppressWarnings("unchecked")
	public Set<String> getStringSet(String var) {
		confirmTypeBinding(var, VariableType.SET_STRING);
		return (Set<String>) _store.get(var);
	}

	public Map<String, VariableType> getTypeBindings() {
		return _typeBindings;
	}
}
