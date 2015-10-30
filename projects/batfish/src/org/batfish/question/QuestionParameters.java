package org.batfish.question;

import java.util.HashMap;
import java.util.Map;

import org.batfish.common.CleanBatfishException;
import org.batfish.grammar.question.VariableType;
import org.batfish.representation.Ip;

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

   public long getInt(String var) {
      confirmTypeBinding(var, VariableType.INT);
      return (long) _store.get(var);
   }

   public Ip getIp(String var) {
      confirmTypeBinding(var, VariableType.IP);
      return (Ip) _store.get(var);
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
