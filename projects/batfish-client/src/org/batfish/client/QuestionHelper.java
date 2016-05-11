package org.batfish.client;

import java.util.Map;

import org.batfish.datamodel.questions.CompareSameNameQuestion;
import org.batfish.datamodel.questions.NeighborsQuestion;
import org.batfish.datamodel.questions.NodesQuestion;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.QuestionType;
import org.codehaus.jettison.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class QuestionHelper {

	public static Question getQuestion(String questionType) throws Exception {
		
		QuestionType qType = QuestionType.valueOf(questionType);
		
		switch (qType) {
      case COMPARE_SAME_NAME:
         return new CompareSameNameQuestion();
      case NEIGHBORS:
         return new NeighborsQuestion();
		case NODES:
			return new NodesQuestion();
		case ACL_REACHABILITY:
			break;
		case DESTINATION:
			break;
		case INGRESS_PATH:
			break;
		case LOCAL_PATH:
			break;
		case MULTIPATH:
			break;
		case PROTOCOL_DEPENDENCIES:
			break;
		case REACHABILITY:
			break;
		case REDUCED_REACHABILITY:
			break;
		case TRACEROUTE:
			break;
		case VERIFY:
			break;
		default:
			break;
		}
		
		throw new Exception("Unsupported question type " + questionType);
	}

	public static String getParametersString(Map<String, String> parameters) throws Exception {
	   String retString = "{\n";
	   
	   for (String paramKey : parameters.keySet()) {
	      retString += String.format("\"%s\" : %s,\n", paramKey, parameters.get(paramKey));	      
	   }
	   
	   retString += "}\n";
	   
	   return retString;
	}
	  
	public static String getQuestionString(String questionType) throws Exception {
		Question question = getQuestion(questionType);
				
		ObjectMapper mapper = new ObjectMapper();		
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		String jsonString = mapper.writeValueAsString(question);
		
		//String newJson = applyParametersToQuestionJson(jsonString, parameters);
		      
		return jsonString;
	}

   private static String applyParametersToQuestionJson(String inputJsonString,
         Map<String, String> parameters) throws Exception {

      JSONObject jObj = new JSONObject(inputJsonString);
      
      for (String paramkey : parameters.keySet()) {
         if (!jObj.has(paramkey)) 
            throw new Exception("Parameter key " + paramkey + " does not exist in question");
         
         jObj.put(paramkey, parameters.get(paramkey));
      }
      
      return jObj.toString(4);
   }	
	
	
}
