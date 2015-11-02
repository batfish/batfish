package org.batfish.question;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.batfish.common.CleanBatfishException;
import org.batfish.grammar.question.VariableType;
import org.batfish.representation.Ip;
import org.batfish.representation.Prefix;
import org.batfish.util.SubRange;

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

   public long getInt(String var) {
      confirmTypeBinding(var, VariableType.INT);
      return (long) _store.get(var);
   }

   public Ip getIp(String var) {
      confirmTypeBinding(var, VariableType.IP);
      return (Ip) _store.get(var);
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

   public Map<String, VariableType> getTypeBindings() {
      return _typeBindings;
   }

}
