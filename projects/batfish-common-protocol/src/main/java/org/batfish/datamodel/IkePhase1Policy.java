package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.ArrayList;
import java.util.List;
import org.batfish.common.util.ComparableStructure;

/** Represents the IKE policy used for IKE phase 1 negotiation */
public class IkePhase1Policy extends ComparableStructure<String> {

  private static final long serialVersionUID = 1L;

  private static final String PROP_IKE_PHASE1_PROPOSALS = "ikePhase1Proposals";

  private static final String PROP_PRE_SHARED_KEY = "preSharedKey";

  private List<IkePhase1Proposal> _ikePhase1Proposals;

  private String _preSharedKey;

  @JsonCreator
  public IkePhase1Policy(@JsonProperty(PROP_NAME) String name) {
    super(name);
    _ikePhase1Proposals = new ArrayList<>();
  }

  @JsonPropertyDescription("IKE phase 1 proposals to be used with this IKE phase 1 policy")
  @JsonProperty(PROP_IKE_PHASE1_PROPOSALS)
  public List<IkePhase1Proposal> getIkePhase1Proopsals() {
    return _ikePhase1Proposals;
  }

  @JsonPropertyDescription("Pre-shared key to be used with this IKE phase 1 policy")
  @JsonProperty(PROP_PRE_SHARED_KEY)
  public String getPreSharedKey() {
    return _preSharedKey;
  }

  @JsonProperty(PROP_IKE_PHASE1_PROPOSALS)
  public void setIkePhase1Proopsals(List<IkePhase1Proposal> ikePhase1Proopsals) {
    _ikePhase1Proposals = ikePhase1Proopsals;
  }

  @JsonProperty(PROP_PRE_SHARED_KEY)
  public void setPreSharedKey(String preSharedKey) {
    _preSharedKey = preSharedKey;
  }
}
