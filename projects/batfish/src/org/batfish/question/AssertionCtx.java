package org.batfish.question;

import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;

public class AssertionCtx {

   private Interface _interface;

   private Configuration _node;

   public AssertionCtx copy() {
      AssertionCtx copy = new AssertionCtx();
      copy._node = _node;
      copy._interface = _interface;
      return copy;
   }

   public Interface getInterface() {
      return _interface;
   }

   public Configuration getNode() {
      return _node;
   }

   public void setInterface(Interface iface) {
      _interface = iface;
   }

   public void setNode(Configuration node) {
      _node = node;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("{ ");
      sb.append("node:" + _node + " ");
      sb.append("interface:" + _interface + " ");
      sb.append("}");
      return sb.toString();
   }

}
