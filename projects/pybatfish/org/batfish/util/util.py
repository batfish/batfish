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

def zip_dir(dirpath, outFile):
    zipWriter = zipfile.ZipFile(outFile, 'w', zipfile.ZIP_DEFLATED)
    relroot = os.path.abspath(os.path.join(dirpath, os.pardir))
 
    for root, dirs, files in os.walk(dirpath):
        zipWriter.write(root, os.path.relpath(root, relroot), zipfile.ZIP_STORED)
        for file in files:
            filename = os.path.join(root, file)
            arcname = os.path.join(os.path.relpath(root, relroot), file)
            zipWriter.write(filename, arcname)

    zipWriter.close()
