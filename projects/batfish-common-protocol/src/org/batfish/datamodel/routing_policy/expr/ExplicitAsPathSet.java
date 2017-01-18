package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.datamodel.routing_policy.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ExplicitAsPathSet extends AsPathSetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<AsPathSetElem> _elems;

   @JsonCreator
   private ExplicitAsPathSet() {
   }

   public ExplicitAsPathSet(List<AsPathSetElem> elems) {
      _elems = elems;
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
      ExplicitAsPathSet other = (ExplicitAsPathSet) obj;
      if (_elems == null) {
         if (other._elems != null) {
            return false;
         }
      }
      else if (!_elems.equals(other._elems)) {
         return false;
      }
      return true;
   }

   public List<AsPathSetElem> getElems() {
      return _elems;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_elems == null) ? 0 : _elems.hashCode());
      return result;
   }

   @Override
   public boolean matches(Environment environment) {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

   public void setElems(List<AsPathSetElem> elems) {
      _elems = elems;
   }

}
