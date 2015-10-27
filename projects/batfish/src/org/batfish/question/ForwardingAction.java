package org.batfish.question;

public enum ForwardingAction {
   ACCEPT,
   DROP,
   DROP_ACL,
   DROP_ACL_IN,
   DROP_ACL_OUT,
   DROP_NO_ROUTE,
   DROP_NULL_ROUTE,
   FORWARD
}
