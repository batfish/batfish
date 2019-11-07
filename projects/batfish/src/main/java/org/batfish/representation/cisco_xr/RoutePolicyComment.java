package org.batfish.representation.cisco_xr;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.statement.Comment;
import org.batfish.datamodel.routing_policy.statement.Statement;

public class RoutePolicyComment extends RoutePolicySetStatement {

  private String _text;

  public RoutePolicyComment(String text) {
    _text = text;
  }

  @Override
  protected Statement toSetStatement(CiscoXrConfiguration cc, Configuration c, Warnings w) {
    return new Comment(_text);
  }
}
