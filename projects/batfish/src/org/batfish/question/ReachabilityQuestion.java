package org.batfish.question;

import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.BatfishException;
import org.batfish.representation.Prefix;
import org.batfish.util.SubRange;

public class ReachabilityQuestion extends Question {

   private Set<ForwardingAction> _actions;

   private Set<SubRange> _dstPortRange;

   private Set<Prefix> _dstPrefixes;

   private Pattern _finalNodeRegex;

   private Pattern _ingressNodeRegex;

   private Set<SubRange> _ipProtocolRange;

   private Set<SubRange> _srcPortRange;

   private Set<Prefix> _srcPrefixes;

   public ReachabilityQuestion() {
      super(QuestionType.REACHABILITY);
      _actions = EnumSet.noneOf(ForwardingAction.class);
      // default action-- may change
      _actions.add(ForwardingAction.ACCEPT);

      _dstPortRange = new TreeSet<SubRange>();
      _dstPrefixes = new TreeSet<Prefix>();
      _ipProtocolRange = new TreeSet<SubRange>();
      _srcPortRange = new TreeSet<SubRange>();
      _srcPrefixes = new TreeSet<Prefix>();
   }

   public Set<ForwardingAction> getActions() {
      return _actions;
   }

   public Set<SubRange> getDstPortRange() {
      return _dstPortRange;
   }

   public Set<Prefix> getDstPrefixes() {
      return _dstPrefixes;
   }

   public Pattern getFinalNodeRegex() {
      return _finalNodeRegex;
   }

   public Pattern getIngressNodeRegex() {
      return _ingressNodeRegex;
   }

   public Set<SubRange> getIpProtocolRange() {
      return _ipProtocolRange;
   }

   public Set<SubRange> getSrcPortRange() {
      return _srcPortRange;
   }

   public Set<Prefix> getSrcPrefixes() {
      return _srcPrefixes;
   }

   public void setFinalNodeRegex(String regex) {
      try {
         _finalNodeRegex = Pattern.compile(regex);
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for final node is not a valid java regex: \""
                     + regex + "\"", e);
      }
   }

   public void setIngressNodeRegex(String regex) {
      try {
         _ingressNodeRegex = Pattern.compile(regex);
      }
      catch (PatternSyntaxException e) {
         throw new BatfishException(
               "Supplied regex for ingress node is not a valid java regex: \""
                     + regex + "\"", e);
      }
   }

}
