import json

import commands
from org.batfish.util.batfish_exception import BatfishException

questionTypeToClass = {
    "aclreachability" : "org.batfish.question.AclReachabilityQuestionPlugin$AclReachabilityQuestion",
    "assert" : "org.batfish.question.assertion.AssertQuestionPlugin$AssertQuestion",
    "bgpadvertisement" : "org.batfish.question.BgpAdvertisementsQuestionPlugin$BgpAdvertisementsQuestion",
    "bgpsessioncheck" : "org.batfish.question.BgpSessionCheckQuestionPlugin$BgpSessionCheckQuestion",
    "comparesamename" : "org.batfish.question.CompareSameNameQuestionPlugin$CompareSameNameQuestion",
    "environmentcreation" : "org.batfish.question.EnvironmentCreationQuestionPlugin$EnvironmentCreationQuestion",
    "error" : "org.batfish.question.ErrorQuestionPlugin$ErrorQuestion",
    "ipsecvpncheck" : "org.batfish.question.IpsecVpnCheckQuestionPlugin$IpsecVpnCheckQuestion",
    "isisloopbacks" : "org.batfish.question.IsisLoopbacksQuestionPlugin$IsisLoopbacksQuestion",
    "neighbors" : "org.batfish.question.NeighborsQuestionPlugin$NeighborsQuestion",
    "nodes" : "org.batfish.question.NodesQuestionPlugin$NodesQuestion",
    "nodespath" : "org.batfish.question.nodespath.NodesPathQuestionPlugin$NodesPathQuestion",
    "ospfloopbacks" : "org.batfish.question.OspfLoopbacksQuestionPlugin$OspfLoopbacksQuestion",
    "pairwisevpnconnectivity" : "org.batfish.question.PairwiseVpnConnectivityQuestionPlugin$PairwiseVpnConnectivityQuestion",
    "reachability" : "org.batfish.question.ReachabilityQuestionPlugin$ReachabilityQuestion",
    "routes" : "org.batfish.question.RoutesQuestionPlugin$RoutesQuestion",
    "selfadjacencies" : "org.batfish.question.SelfAdjacenciesQuestionPlugin$SelfAdjacenciesQuestion",
    "traceroute" : "org.batfish.question.TracerouteQuestionPlugin$TracerouteQuestion",
    "undefinedreferences" : "org.batfish.question.UndefinedReferencesQuestionPlugin$UndefinedReferencesQuestion",
    "uniquebgpprefixorigination" : "org.batfish.question.UniqueBgpPrefixOriginationQuestionPlugin$UniqueBgpPrefixOriginationQuestion",
    "uniqueipassignments" : "org.batfish.question.UniqueIpAssignmentsQuestionPlugin$UniqueIpAssignmentsQuestion",
    "unusedstructures" : "org.batfish.question.UnusedStructuresQuestionPlugin$UnusedStructuresQuestion",
}

def bf_traceroute(ingressNode, dstIp=None, differential=False, doDelta=False):
    questionJson = bf_get_question_json("traceroute");

    parametersJson = {}    
    parametersJson["ingressNode"] = ingressNode
    if (dstIp is not None):
        parametersJson["dstIp"] = dstIp
        
    return _get_answer(questionJson, parametersJson, doDelta, differential)    

def bf_get_question_json(questionType):
    if (questionType not in questionTypeToClass):
        raise BatfishException("Unknown question type " + questionType)
    
    questionJson = {}
    questionJson["class"] = questionTypeToClass[questionType]

    return questionJson

def _get_answer(questionJson, parametersJson, doDelta, differential):
    #add the correct value of differential if one does not already exist
    parametersJson["differential"] = str(differential)
    return commands.bf_answer(json.dumps(questionJson), json.dumps(parametersJson), doDelta)
    
    
