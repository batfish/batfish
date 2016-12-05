'''
Created on Jan 8, 2016

@author: arifogel
'''
import os
import errno
import uuid
import zipfile

def get_uuid():
    return str(uuid.uuid4())
    
def make_sure_path_exists(path):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise

def sublist(l1, l2):
    l1Size = len(l1)
    l2Size = len(l2)
    if l1Size == 0:
        return -1
    if l1Size > l2Size:
        return False
    candidateIndices = dict()
    maxCandidateIndex = l2Size - l1Size
    for i in range(0, l2Size):
        if len(candidateIndices) == 0 and i > maxCandidateIndex:
            return False
        candidateIndices[i] = 0
        for (candidateIndex, offset) in candidateIndices.items():
            if l1[offset] == l2[candidateIndex + offset]:
                if offset == l1Size - 1:
                    return candidateIndex
                else:
                    candidateIndices[candidateIndex] = candidateIndices[candidateIndex] + 1
            else:
                del candidateIndices[candidateIndex]
    return False

def zip_dir(dirpath, outFile):
    zipWriter = zipfile.ZipFile(outFile, 'w', zipfile.ZIP_DEFLATED)
    relroot = os.path.abspath(os.path.join(dirpath, os.pardir))
 
    for root, _dirs, files in os.walk(dirpath):
        zipWriter.write(root, os.path.relpath(root, relroot), zipfile.ZIP_STORED)
        for f in files:
            filename = os.path.join(root, f)
            arcname = os.path.join(os.path.relpath(root, relroot), f)
            zipWriter.write(filename, arcname)

    zipWriter.close()
