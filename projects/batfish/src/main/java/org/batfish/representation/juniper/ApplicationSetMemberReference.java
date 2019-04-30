package org.batfish.representation.juniper;

import java.io.Serializable;

public abstract class ApplicationSetMemberReference implements Serializable {

  private static final long serialVersionUID = 1L;

  public abstract ApplicationSetMember resolve(JuniperConfiguration jc);
}
