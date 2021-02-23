import sys
import numpy as np
import matplotlib.pyplot as plt
import os
from os import listdir
from os.path import isfile, join
import re
import pylab as pl                   # frange
import math                          # isnan, isinf, ceil
import pprint
from collections import defaultdict
import ruamel.yaml as yaml
from textwrap import wrap
from functools import reduce
# returns: list of full paths of files (under directory `basedir`) filtered through a file prefix (`basefn`)
def get_data_files(basedir):
  return [join(basedir,p) for p in listdir(basedir) if isfile(join(basedir,p)) and p.endswith(".txt")]

script = sys.argv[0]
if len(sys.argv) < 3:
  print("USAGE: merge <folder_a> <folder_b> <merge-folder>")
  exit(0)

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

left_folder = sys.argv[1]
right_folder = sys.argv[2]

left_matrix = [process_file(f) for f in get_data_files(left_folder)]
right_matrix = [process_file(f) for f in get_data_files(right_folder)]

path = "output"
try:
    os.mkdir(path)
except OSError:
    print ("Creation of the directory %s failed" % path)
else:
    print ("Successfully created the directory %s " % path)
for i in range(len(left_matrix)):
    merged = np.dstack(np.array((left_matrix[i][0], right_matrix[i][1], left_matrix[i][1])))
    file_name = path + "/20210222" + "-wildlife_random-" + str(i) + ".0.txt"
    np.savetxt(file_name, (merged[0]))

