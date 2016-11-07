package org.batfish.datamodel.routing_policy.expr;

import java.util.List;

import org.batfish.datamodel.routing_policy.Environment;

public class ExplicitAsPathSet implements AsPathSetExpr {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private List<AsPathSetElem> _elems;

   public ExplicitAsPathSet(List<AsPathSetElem> elems) {
      _elems = elems;
   }

   public List<AsPathSetElem> getElems() {
      return _elems;
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
