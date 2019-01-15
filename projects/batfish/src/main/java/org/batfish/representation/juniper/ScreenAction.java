package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.datamodel.LineAction;

/** Represents the action part of a Juniper screen ids-option */
public interface ScreenAction extends Serializable {
  /** */
  String getName();

  /** Convert ScreenAction to ACL line action */
  LineAction toAclLineAction();
}
