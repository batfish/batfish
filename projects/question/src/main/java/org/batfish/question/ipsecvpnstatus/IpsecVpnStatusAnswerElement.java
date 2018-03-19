package org.batfish.question.ipsecvpnstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.answers.AnswerElement;

public class IpsecVpnStatusAnswerElement extends AnswerElement {

  private static final String PROP_IPSEC_VPNS = "ipsecVpns";

  private SortedSet<IpsecVpnInfo> _ipsecVpns;

  public IpsecVpnStatusAnswerElement() {
    this(null);
  }

  @JsonCreator
  public IpsecVpnStatusAnswerElement(
      @JsonProperty(PROP_IPSEC_VPNS) SortedSet<IpsecVpnInfo> ipsecVpns) {
    _ipsecVpns = ipsecVpns == null ? new TreeSet<>() : ipsecVpns;
  }

  @JsonProperty(PROP_IPSEC_VPNS)
  public SortedSet<IpsecVpnInfo> getIpsecVpns() {
    return _ipsecVpns;
  }
}
