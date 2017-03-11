package org.batfish.question;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.datamodel.answers.Problem;
import org.batfish.datamodel.answers.ProblemsAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UnusedStructuresQuestionPlugin extends QuestionPlugin {

   public static class UnusedStructuresAnswerElement
         extends ProblemsAnswerElement {

      private SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> _unusedStructures;

      public UnusedStructuresAnswerElement() {
         _unusedStructures = new TreeMap<>();
      }

      public SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> getUnusedStructures() {
         return _unusedStructures;
      }

      @Override
      public String prettyPrint() {
         final StringBuilder sb = new StringBuilder();
         _unusedStructures.forEach((node, types) -> {
            sb.append(node + ":\n");
            types.forEach((type, members) -> {
               sb.append("  " + type + ":\n");
               members.forEach((member, lines) -> {
                  sb.append(
                        "    " + member + " lines " + lines.toString() + "\n");
               });
            });
         });
         return sb.toString();
      }

      public void setUnusedStructures(
            SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>> undefinedReferences) {
         _unusedStructures = undefinedReferences;
      }
   }

   public static class UnusedStructuresAnswerer extends Answerer {

      public UnusedStructuresAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public UnusedStructuresAnswerElement answer() {
         UnusedStructuresQuestion question = (UnusedStructuresQuestion) _question;
         Pattern nodeRegex;
         try {
            nodeRegex = Pattern.compile(question.getNodeRegex());
         }
         catch (PatternSyntaxException e) {
            throw new BatfishException(
                  "Supplied regex for nodes is not a valid java regex: \""
                        + question.getNodeRegex() + "\"",
                  e);
         }
         _batfish.checkConfigurations();
         UnusedStructuresAnswerElement answerElement = new UnusedStructuresAnswerElement();
         ConvertConfigurationAnswerElement ccae = _batfish
               .loadConvertConfigurationAnswerElement();
         ccae.getUnusedStructures().forEach((hostname, byType) -> {
            if (nodeRegex.matcher(hostname).matches()) {
               answerElement.getUnusedStructures().put(hostname, byType);
            }
         });

         ParseVendorConfigurationAnswerElement pvcae = _batfish
               .loadParseVendorConfigurationAnswerElement();
         SortedMap<String, String> hostnameFilenameMap = pvcae.getFileMap();
         answerElement.getUnusedStructures().forEach((hostname, byType) -> {
            String filename = hostnameFilenameMap.get(hostname);
            if (filename != null) {
               byType.forEach((type, byName) -> {
                  byName.forEach((name, lines) -> {
                     String problemShort = "unused:" + type + ":" + name;
                     Problem problem = answerElement.getProblems()
                           .get(problemShort);
                     if (problem == null) {
                        problem = new Problem();
                        String problemLong = "Unused structure of type: '"
                              + type + "' with name: '" + name + "'";
                        problem.setDescription(problemLong);
                        answerElement.getProblems().put(problemShort, problem);
                     }
                     problem.getFiles().put(filename, lines);
                  });
               });
            }
         });
         return answerElement;
      }

   }

   // <question_page_comment>
   /**
    * Outputs cases where structures (e.g., ACL, routemaps) are defined but not
    * used.
    * <p>
    * Such occurrences could be configuration errors or leftover cruft.
    *
    * @type UnusedStructures onefile
    *
    * @param nodeRegex
    *           Regular expression for names of nodes to include. Default value
    *           is '.*' (all nodes).
    *
    * @example bf_answer("Nodes", nodeRegex="as1.*") Analyze all nodes whose
    *          names begin with "as1".
    */
   public static class UnusedStructuresQuestion extends Question {

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private String _nodeRegex;

      public UnusedStructuresQuestion() {
         _nodeRegex = ".*";
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "unusedstructures";
      }

      @JsonProperty(NODE_REGEX_VAR)
      public String getNodeRegex() {
         return _nodeRegex;
      }

      @Override
      public boolean getTraffic() {
         return false;
      }

      @Override
      public void setJsonParameters(JSONObject parameters) {
         super.setJsonParameters(parameters);
         Iterator<?> paramKeys = parameters.keys();
         while (paramKeys.hasNext()) {
            String paramKey = (String) paramKeys.next();
            if (isBaseParamKey(paramKey)) {
               continue;
            }
            try {
               switch (paramKey) {
               case NODE_REGEX_VAR:
                  setNodeRegex(parameters.getString(paramKey));
                  break;
               default:
                  throw new BatfishException("Unknown key in "
                        + getClass().getSimpleName() + ": " + paramKey);
               }
            }
            catch (JSONException e) {
               throw new BatfishException("JSONException in parameters", e);
            }
         }
      }

      public void setNodeRegex(String nodeRegex) {
         _nodeRegex = nodeRegex;
      }
   }

   @Override
   protected Answerer createAnswerer(Question question, IBatfish batfish) {
      return new UnusedStructuresAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new UnusedStructuresQuestion();
   }

}
