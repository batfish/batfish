package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.representation.juniper.FwTerm.Field;

/**
 * Class for match conditions in firewall filters, security policies, and host-inbound traffic
 * filters.
 */
public interface FwFrom extends Serializable {

  Field getField();

  AclLineMatchExpr toAclLineMatchExpr(JuniperConfiguration jc, Configuration c, Warnings w);
}
