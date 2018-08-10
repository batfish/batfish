package org.batfish.datamodel.answers;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.acl.CanonicalAcl;

/** Interface for representing answers to aclReachability and aclReachability2. */
@ParametersAreNonnullByDefault
public interface AclLinesAnswerElementInterface {

  /**
   * Representation of an ACL, including a {@link CanonicalAcl} and all sources that contain that
   * ACL as a map of hostnames to sets of ACL names.
   */
  class AclSpecs {

    public static class Builder {
      private CanonicalAcl _acl;
      private Map<String, Set<String>> _sources = new TreeMap<>();
      private String _reprHostname;

      public AclSpecs build() {
        return new AclSpecs(_acl, _reprHostname, _sources);
      }

      public Builder addSource(String hostname, String aclName) {
        if (_sources.isEmpty()) {
          _reprHostname = hostname;
        }
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

    /**
     * The hostname of the first node where this ACL was found; as a result, this host contains the
     * ACL under the name {@code acl.getAclName()}.
     */
    public final String reprHostname;

    public final Map<String, Set<String>> sources;

    public AclSpecs(CanonicalAcl acl, String hostname, Map<String, Set<String>> sources) {
      this.acl = acl;
      this.reprHostname = hostname;
      this.sources = ImmutableMap.copyOf(sources);
    }

    public static Builder builder() {
      return new Builder();
    }
  }

  /**
   * Records line as unreachable (possibly unmatchable).
   *
   * @param aclSpecs Definition of ACL in which to report line
   * @param lineNumber Which line to report unreachable
   * @param unmatchable Whether the unreachable line is independently unmatchable
   * @param blockingLines Which lines are blocking the unreachable line (none if unmatchable)
   */
  void addUnreachableLine(
      AclSpecs aclSpecs, int lineNumber, boolean unmatchable, SortedSet<Integer> blockingLines);

  /**
   * Reports a cycle of circular references in the given host.
   *
   * @param hostname Name of host containing cycle
   * @param aclsInCycle Names of ACLs forming the cycle, in reference order (i.e. 0th element
   *     references 1st element)
   */
  void addCycle(String hostname, List<String> aclsInCycle);
}
