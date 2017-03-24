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

public class UndefinedReferencesQuestionPlugin extends QuestionPlugin {

   public static class UndefinedReferencesAnswerElement
         extends ProblemsAnswerElement {

      private SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>> _undefinedReferences;

      public UndefinedReferencesAnswerElement() {
         _undefinedReferences = new TreeMap<>();
      }

      public SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>> getUndefinedReferences() {
         return _undefinedReferences;
      }

      @Override
      public String prettyPrint() {
         final StringBuilder sb = new StringBuilder();
         _undefinedReferences.forEach((hostname, byType) -> {
            sb.append(hostname + ":\n");
            byType.forEach((type, byName) -> {
               sb.append("  " + type + ":\n");
               byName.forEach((name, byUsage) -> {
                  sb.append("    " + name + ":\n");
                  byUsage.forEach((usage, lines) -> {
                     sb.append("      " + usage + ": lines " + lines.toString()
                           + "\n");
                  });
               });
            });
         });
         return sb.toString();
      }

      public void setUndefinedReferences(
            SortedMap<String, SortedMap<String, SortedMap<String, SortedMap<String, SortedSet<Integer>>>>> undefinedReferences) {
         _undefinedReferences = undefinedReferences;
      }

   }

   public static class UndefinedReferencesAnswerer extends Answerer {

      public UndefinedReferencesAnswerer(Question question, IBatfish batfish) {
         super(question, batfish);
      }

      @Override
      public UndefinedReferencesAnswerElement answer() {
         UndefinedReferencesQuestion question = (UndefinedReferencesQuestion) _question;
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
         UndefinedReferencesAnswerElement answerElement = new UndefinedReferencesAnswerElement();
         ConvertConfigurationAnswerElement ccae = _batfish
               .loadConvertConfigurationAnswerElement();
         ccae.getUndefinedReferences().forEach((hostname, byType) -> {
            if (nodeRegex.matcher(hostname).matches()) {
               answerElement.getUndefinedReferences().put(hostname, byType);
            }
         });
         ParseVendorConfigurationAnswerElement pvcae = _batfish
               .loadParseVendorConfigurationAnswerElement();
         SortedMap<String, String> hostnameFilenameMap = pvcae.getFileMap();
         answerElement.getUndefinedReferences().forEach((hostname, byType) -> {
            String filename = hostnameFilenameMap.get(hostname);
            if (filename != null) {
               byType.forEach((type, byName) -> {
                  byName.forEach((name, byUsage) -> {
                     byUsage.forEach((usage, lines) -> {
                        String problemShort = "undefined:" + type + ":usage:"
                              + usage + ":" + name;
                        Problem problem = answerElement.getProblems()
                              .get(problemShort);
                        if (problem == null) {
                           problem = new Problem();
                           String problemLong = "Undefined reference to structure of type: '"
                                 + type + "' with usage: '" + usage
                                 + "' named '" + name + "'";
                           problem.setDescription(problemLong);
                           answerElement.getProblems().put(problemShort,
                                 problem);
                        }
                        problem.getFiles().put(filename, lines);
                     });
                  });
               });
            }
         });
         return answerElement;
      }
   }

   // <question_page_comment>
   /**
    * Outputs cases where undefined structures (e.g., ACL, routemaps) are
    * referenced.
    * <p>
    * Such occurrences indicate configuration errors and can have serious
    * consequences with some vendors.
    *
    * @type UndefinedReferences onefile
    *
    * @param nodeRegex
    *           Regular expression for names of nodes to include. Default value
    *           is '.*' (all nodes).
    *
    * @example bf_answer("Nodes", nodeRegex="as1.*") Analyze all nodes whose
    *          names begin with "as1".
    */
   public static class UndefinedReferencesQuestion extends Question {

      private static final String NODE_REGEX_VAR = "nodeRegex";

      private String _nodeRegex;

      public UndefinedReferencesQuestion() {
         _nodeRegex = ".*";
      }

      @Override
      public boolean getDataPlane() {
         return false;
      }

      @Override
      public String getName() {
         return "undefinedreferences";
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
      return new UndefinedReferencesAnswerer(question, batfish);
   }

   @Override
   protected Question createQuestion() {
      return new UndefinedReferencesQuestion();
   }

}
