package org.batfish.question.tracefilters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.question.ipsecvpnstatus.IpsecVpnInfo;

public class TraceFiltersAnswerElement extends AnswerElement {

  private static final String PROP_IPSEC_VPNS = "ipsecVpns";

  private SortedSet<IpsecVpnInfo> _ipsecVpns;

  public TraceFiltersAnswerElement() {
    this(null);
  }

  @JsonCreator
  public TraceFiltersAnswerElement(
      @JsonProperty(PROP_IPSEC_VPNS) SortedSet<IpsecVpnInfo> ipsecVpns) {
    _ipsecVpns = ipsecVpns == null ? new TreeSet<>() : ipsecVpns;
  }

  @JsonProperty(PROP_IPSEC_VPNS)
  public SortedSet<IpsecVpnInfo> getIpsecVpns() {
    return _ipsecVpns;
  }
}
