package org.batfish.datamodel.acl;

import org.batfish.datamodel.AbstractAclLine;
import org.batfish.datamodel.IpAccessListLine;

/** Visitor for {@link IpAccessListLine} */
public interface GenericIpAccessListLineVisitor<R> {

  default R visit(AbstractAclLine line) {
    return line.accept(this);
  }

  R visitIpAccessListLine(IpAccessListLine ipAccessListLine);
}
