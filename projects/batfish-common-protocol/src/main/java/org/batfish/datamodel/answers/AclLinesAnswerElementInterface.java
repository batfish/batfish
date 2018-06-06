package org.batfish.datamodel.answers;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Pair;
import org.batfish.datamodel.acl.CanonicalAcl;

@ParametersAreNonnullByDefault
public interface AclLinesAnswerElementInterface {

  class AclSpecs {

    public static class Builder {
      private CanonicalAcl _acl;
      private Map<String, Set<String>> _sources = new TreeMap<>();

      public AclSpecs build() {
        return new AclSpecs(_acl, _sources);
      }

      public Builder addSource(String hostname, String aclName) {
        _sources.computeIfAbsent(hostname, h -> new TreeSet<>()).add(aclName);
        return this;
      }

      public CanonicalAcl getAcl() {
        return _acl;
      }

      public Builder setAcl(CanonicalAcl acl) {
        _acl = acl;
        return this;
      }
    }

    public final CanonicalAcl acl;
    public final Map<String, Set<String>> sources;

    public AclSpecs(CanonicalAcl acl, Map<String, Set<String>> sources) {
      this.acl = acl;
      this.sources = ImmutableMap.copyOf(sources);
    }

    public static Builder builder() {
      return new Builder();
    }

    public Pair<String, String> getRepresentativeHostnameAclPair() {
      String hostname = sources.keySet().iterator().next();
      return new Pair<>(hostname, sources.get(hostname).iterator().next());
    }
  }

  void addReachableLine(AclSpecs aclSpecs, int lineNumber);

  void addUnreachableLine(
      AclSpecs aclSpecs, int lineNumber, boolean unmatchable, SortedSet<Integer> blockingLines);

  void addCycle(String hostname, List<String> aclsInCycle);
}
