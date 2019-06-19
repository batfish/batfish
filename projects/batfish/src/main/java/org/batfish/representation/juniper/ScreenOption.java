package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/** Represents the screen option part of a Juniper screen ids-option */
public interface ScreenOption extends Serializable {
  String getName();

  /** Convert a screen option to an ACL line expression that match flows this option rejects. */
  AclLineMatchExpr getAclLineMatchExpr();
}
