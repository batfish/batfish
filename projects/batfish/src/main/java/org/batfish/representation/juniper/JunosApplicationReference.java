package org.batfish.representation.juniper;

public class JunosApplicationReference extends ApplicationSetMemberReference {

  private JunosApplication _junosApplication;

  public JunosApplicationReference(JunosApplication junosApplication) {
    _junosApplication = junosApplication;
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    return _junosApplication;
  }
}
