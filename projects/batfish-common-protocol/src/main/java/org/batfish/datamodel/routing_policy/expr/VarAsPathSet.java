package org.batfish.datamodel.routing_policy.expr;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class VarAsPathSet extends AsPathSetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private String _var;

   @JsonCreator
   private VarAsPathSet() {
   }

   public VarAsPathSet(String var) {
      _var = var;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      VarAsPathSet other = (VarAsPathSet) obj;
      if (_var == null) {
         if (other._var != null) {
            return false;
         }
      }
      else if (!_var.equals(other._var)) {
         return false;
      }
      return true;
   }

   public String getVar() {
      return _var;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_var == null) ? 0 : _var.hashCode());
      return result;
   }

   @Override
   public boolean matches(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public void setVar(String var) {
      _var = var;
   }

}
