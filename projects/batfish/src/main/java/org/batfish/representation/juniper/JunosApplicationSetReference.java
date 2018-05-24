package org.batfish.representation.juniper;

public class JunosApplicationSetReference extends ApplicationSetMemberReference {

  /** */
  private static final long serialVersionUID = 1L;

  private JunosApplicationSet _junosApplicationSet;

  public JunosApplicationSetReference(JunosApplicationSet junosApplicationSet) {
    _junosApplicationSet = junosApplicationSet;
  }

  @Override
  public ApplicationSetMember resolve(JuniperConfiguration jc) {
    return _junosApplicationSet.getApplicationSet();
  }
}
