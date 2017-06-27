package org.batfish.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;

@JsonSchemaDescription("A generated/aggregate IPV4 route.")
public final class GeneratedRoute extends AbstractRoute {

   public static class Builder extends AbstractRouteBuilder<GeneratedRoute> {

      private List<SortedSet<Integer>> _asPath;

      private String _attributePolicy;

      private boolean _discard;

      private String _generationPolicy;

      private String _nextHopInterface;

      public Builder() {
         _asPath = new ArrayList<>();
         _nextHopIp = Route.UNSET_ROUTE_NEXT_HOP_IP;
      }

      @Override
      public GeneratedRoute build() {
         GeneratedRoute gr = new GeneratedRoute(_network, _nextHopIp);
         gr.setAdministrativePreference(_admin);
         gr.setMetric(_metric);
         gr.setGenerationPolicy(_generationPolicy);
         gr.setAttributePolicy(_attributePolicy);
         gr.setDiscard(_discard);
         gr.setAsPath(new AsPath(_asPath));
         gr.setNextHopInterface(_nextHopInterface);
         return gr;
      }

      public void setAsPath(List<SortedSet<Integer>> asPath) {
         _asPath = asPath;
      }

      public void setAttributePolicy(String attributePolicy) {
         _attributePolicy = attributePolicy;
      }

      public void setDiscard(boolean discard) {
         _discard = discard;
      }

      public void setGenerationPolicy(String generationPolicy) {
         _generationPolicy = generationPolicy;
      }

      public void setNextHopInterface(String nextHopInterface) {
         _nextHopInterface = nextHopInterface;
      }

   }

   private static final String AS_PATH_VAR = "asPath";

   private static final String DISCARD_VAR = "discard";

   private static final String METRIC_VAR = "metric";

   private static final long serialVersionUID = 1L;

   private int _administrativeCost;

   private AsPath _asPath;

   private String _attributePolicy;

   private boolean _discard;

   private String _generationPolicy;

   private Integer _metric;

   private String _nextHopInterface;

   @JsonCreator
   public GeneratedRoute(@JsonProperty(NETWORK_VAR) Prefix prefix) {
      super(prefix, Route.UNSET_ROUTE_NEXT_HOP_IP);
   }

   public GeneratedRoute(Prefix prefix, int administrativeCost) {
      super(prefix, Route.UNSET_ROUTE_NEXT_HOP_IP);
      _administrativeCost = administrativeCost;
   }

   public GeneratedRoute(Prefix prefix, Ip nextHopIp) {
      super(prefix, nextHopIp);
   }

   @Override
   public boolean equals(Object o) {
      GeneratedRoute rhs = (GeneratedRoute) o;
      return _network.equals(rhs._network);
   }

   @Override
   public int getAdministrativeCost() {
      return _administrativeCost;
   }

   @JsonProperty(AS_PATH_VAR)
   @JsonPropertyDescription("A BGP AS-path attribute to associate with this generated route")
   public AsPath getAsPath() {
      return _asPath;
   }

   @JsonPropertyDescription("The name of the policy that sets attributes of this route")
   public String getAttributePolicy() {
      return _attributePolicy;
   }

   @JsonProperty(DISCARD_VAR)
   @JsonPropertyDescription("Whether this route is route is meant to discard all matching packets")
   public boolean getDiscard() {
      return _discard;
   }

   @JsonPropertyDescription("The name of the policy that will generate this route if another route matches it")
   public String getGenerationPolicy() {
      return _generationPolicy;
   }

   @JsonProperty(METRIC_VAR)
   @Override
   public Integer getMetric() {
      return _metric;
   }

   @Override
   public String getNextHopInterface() {
      return _nextHopInterface;
   }

   @Override
   @JsonIgnore
   public Ip getNextHopIp() {
      return super.getNextHopIp();
   }

   @Override
   public RoutingProtocol getProtocol() {
      return RoutingProtocol.AGGREGATE;
   }

   @Override
   @JsonIgnore
   public int getTag() {
      return NO_TAG;
   }

   @Override
   public int hashCode() {
      return _network.hashCode();
   }

   @Override
   protected final String protocolRouteString() {
      return " asPath:" + _asPath + " attributePolicy:" + _attributePolicy
            + " discard:" + _discard + " generationPolicy:" + _generationPolicy;
   }

   @Override
   public int routeCompare(AbstractRoute rhs) {
      if (getClass() != rhs.getClass()) {
         return 0;
      }
      GeneratedRoute castRhs = (GeneratedRoute) rhs;
      int ret;
      if (_asPath == null) {
         if (castRhs._asPath != null) {
            ret = -1;
         }
         else {
            ret = 0;
         }
      }
      else if (castRhs._asPath == null) {
         ret = 1;
      }
      else {
         ret = _asPath.compareTo(castRhs._asPath);
      }
      if (ret != 0) {
         return ret;
      }
      if (_attributePolicy == null) {
         if (castRhs._attributePolicy != null) {
            ret = -1;
         }
         else {
            ret = 0;
         }
      }
      else if (castRhs._attributePolicy == null) {
         ret = 1;
      }
      else {
         ret = _attributePolicy.compareTo(castRhs._attributePolicy);
      }
      if (ret != 0) {
         return ret;
      }
      ret = Boolean.compare(_discard, castRhs._discard);
      if (ret != 0) {
         return ret;
      }
      if (_generationPolicy == null) {
         if (castRhs._generationPolicy != null) {
            ret = -1;
         }
         else {
            ret = 0;
         }
      }
      else if (castRhs._generationPolicy == null) {
         ret = 1;
      }
      else {
         ret = _generationPolicy.compareTo(castRhs._generationPolicy);
      }
      return ret;
   }

   @JsonProperty(ADMINISTRATIVE_COST_VAR)
   public void setAdministrativePreference(int preference) {
      _administrativeCost = preference;
   }

   @JsonProperty(AS_PATH_VAR)
   public void setAsPath(AsPath asPath) {
      _asPath = asPath;
   }

   public void setAttributePolicy(String attributePolicy) {
      _attributePolicy = attributePolicy;
   }

   @JsonProperty(DISCARD_VAR)
   public void setDiscard(boolean discard) {
      _discard = discard;
   }

   public void setGenerationPolicy(String generationPolicy) {
      _generationPolicy = generationPolicy;
   }

   @JsonProperty(METRIC_VAR)
   public void setMetric(int metric) {
      _metric = metric;
   }

   public void setNextHopInterface(String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
   }

}
