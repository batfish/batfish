package org.batfish.question.prefix_set_expr.iface;

import java.util.Set;

import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;
import org.batfish.question.Environment;
import org.batfish.question.interface_expr.InterfaceExpr;

public class AllPrefixesInterfacePrefixSetExpr extends InterfacePrefixSetExpr {

   public AllPrefixesInterfacePrefixSetExpr(InterfaceExpr caller) {
      super(caller);
   }

   @Override
   public Set<Prefix> evaluate(Environment environment) {
      Interface iface = _caller.evaluate(environment);
      return iface.getAllPrefixes();
   }

}
