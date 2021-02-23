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

script = sys.argv[0]
if len(sys.argv) < 3:
  print("USAGE: merge <folder_a> <folder_b> <merge-folder>")
  exit(0)

left_folder = sys.argv[1]
right_folder = sys.argv[2]

left_matrix = [process_file(f) for f in get_data_files(left_folder)]
right_matrix = [process_file(f) for f in get_data_files(right_folder)]

path = "data/merge"
try:
    os.mkdir(path)
except OSError:
    print ("Creation of the directory %s failed" % path)
else:
    print ("Successfully created the directory %s " % path)
for i in range(len(left_matrix)):
    merged = np.concatenate((left_matrix[i], right_matrix[i][1:]))
    pretty_print = np.column_stack(merged)
    file_name = path + "/-merge_random-" + str(i) + ".0.txt"
    np.savetxt(file_name, (pretty_print))

