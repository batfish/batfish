package org.batfish.representation.juniper;

import java.io.Serializable;

public abstract class ApplicationSetMemberReference implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  protected final String _name;

  protected final Application _application;

  protected ApplicationSetMemberReference(String name) {
    _name = name;
    _application = null;
  }

  protected ApplicationSetMemberReference(Application application) {
    _name = null;
    _application = application;
  }

  public abstract ApplicationSetMember resolve(JuniperConfiguration jc);
}
