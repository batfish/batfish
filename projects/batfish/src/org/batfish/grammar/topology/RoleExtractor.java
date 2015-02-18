package org.batfish.grammar.topology;

import org.antlr.v4.runtime.Token;
import org.batfish.collections.NodeRoleMap;
import org.batfish.collections.RoleSet;
import org.batfish.grammar.topology.RoleParser.Node_role_declarations_lineContext;
import org.batfish.grammar.topology.RoleParser.Role_declarationsContext;

public class RoleExtractor extends RoleParserBaseListener {

   private NodeRoleMap _nodeRoles;

   @Override
   public void enterRole_declarations(Role_declarationsContext ctx) {
      _nodeRoles = new NodeRoleMap();
   }

   @Override
   public void exitNode_role_declarations_line(
         Node_role_declarations_lineContext ctx) {
      String node = ctx.node.getText();
      RoleSet roles = new RoleSet();
      _nodeRoles.put(node, roles);
      for (Token t : ctx.roles) {
         String role = t.getText();
         roles.add(role);
      }
   }

   public NodeRoleMap getRoleMap() {
      return _nodeRoles;
   }

}
