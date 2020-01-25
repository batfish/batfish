package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.representation.juniper.FwTerm.Field;

/**
 * Class for match conditions in firewall filters, security policies, and host-inbound traffic
 * filters.
 */
public interface FwFrom extends Serializable {

  void applyTo(
      HeaderSpace.Builder headerSpaceBuilder, JuniperConfiguration jc, Warnings w, Configuration c);

  default Field getField() {
    throw new BatfishException("not implemented");
  }

  default AclLineMatchExpr toAclLineMatchExpr(
      JuniperConfiguration jc, Configuration c, Warnings w) {
    throw new BatfishException("not implemented");
  }
}
