package org.batfish.representation.juniper;

import java.io.Serializable;

public abstract class ApplicationSetMemberReference implements Serializable {

  public abstract ApplicationSetMember resolve(JuniperConfiguration jc);
}
