package org.batfish.representation.juniper;

public class JunosApplicationReference extends ApplicationSetMemberReference {

  /** */
  private static final long serialVersionUID = 1L;

  private JunosApplication _junosApplication;

  public JunosApplicationReference(JunosApplication junosApplication) {
    _junosApplication = junosApplication;
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    return _junosApplication;
  }
}
