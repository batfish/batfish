package org.batfish.client;

import org.batfish.common.datamodel.questions.NodesQuestion;
import org.batfish.common.datamodel.questions.Question;
import org.batfish.common.datamodel.questions.QuestionParameters;
import org.batfish.common.datamodel.questions.QuestionType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class QuestionHelper {

	public static Question getQuestion(String questionType) throws Exception {
		
		QuestionType qType = QuestionType.valueOf(questionType);
		
		switch (qType) {
		case NODES:
			return new NodesQuestion(new QuestionParameters());
		case ACL_REACHABILITY:
			break;
		case COMPARE_SAME_NAME:
			break;
		case DESTINATION:
			break;
		case INGRESS_PATH:
			break;
		case LOCAL_PATH:
			break;
		case MULTIPATH:
			break;
		case NEIGHBORS:
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

	public static String getQuestionString(String questionType) throws Exception {
		Question question = getQuestion(questionType);
				
		ObjectMapper mapper = new ObjectMapper();		
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		
		String jsonString = mapper.writeValueAsString(question);
		
		return jsonString;
	}	
}
