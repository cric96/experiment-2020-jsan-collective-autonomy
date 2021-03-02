import sys
import numpy as np
import os
from os import listdir
from os.path import isfile, join
import re
import ruamel.yaml as yaml

# returns: list of full paths of files (under directory `basedir`) filtered through a file prefix (`basefn`)
def get_data_files(basedir):
  return [join(basedir,p) for p in listdir(basedir) if isfile(join(basedir,p)) and p.endswith(".txt")]

# Returns a pair (id,matrix) for each parsed file
def process_file(filepath):
  print(("\n>>> Processing file: " + filepath))

  fh = open(filepath, "r")
  matrix = process_file_content(fh)
  dimMatrix = matrix.transpose()

  # Closes file handle
  fh.close()

  return (dimMatrix)

def process_file_content(filehandle):
  # Read data
  lines = filehandle.readlines()
  # Removes empty and comment lines and maps to float
  data_rows = np.array([list(map(float, s.strip().split(" "))) for s in lines if len(s)>0 and s[0]!="#"], dtype='float')
  return data_rows

def processFolder(index):
    return [process_file(f) for f in get_data_files(sys.argv[index])]

if len(sys.argv) < 3:
  print("USAGE: merge <folder_a> <folder_b> <other folder>")
  exit(0)

left_folder = sys.argv[1]
right_folder = sys.argv[2]
matrices = np.array([processFolder(index + 1) for index in range(len(sys.argv) - 1)])

rightData = matrices[1:][:, :, 1:]
leftData = matrices[:1]

leftDataReshaped = leftData.reshape(*leftData.shape[1:])
rightDataReshaped = rightData.reshape(*rightData.shape[1:])

path = "data/merge"
try:
    os.mkdir(path)
except OSError:
    print ("...." % path)
else:
    print ("Successfully created the directory %s " % path)
for i in range(len(leftDataReshaped)):
    merged = np.concatenate((leftDataReshaped[i], rightDataReshaped[i][:]))
    pretty_print = np.column_stack(merged)
    file_name = path + "/-merge_random-" + str(i) + ".0.txt"
    np.savetxt(file_name, (pretty_print))

