
import os
import re
import sys

class PatchFile:
	pass
class Hunk:
	pass

hunk_header_pattern = re.compile(r"^@@ -(\d+),(\d+) \+(\d+),(\d+) @@(.*)$")

def get_each_content(content):
	content_a = []
	content_b = []
	in_a = False
	in_b = False
	for line in content.splitlines(keepends = True):
		if in_a:
			if line.startswith("======="):
				in_a = False
				in_b = True
			else:
				content_a.append(line)
		elif in_b:
			if line.startswith("<<<<<<<"):
				in_b = False
			else:
				content_b.append(line)
		else:
			if line.startswith(">>>>>>>"):
				in_a = True
			else:
				content_a.append(line)
				content_b.append(line)
	return (content_a, content_b)

def parse_file(lines):
	patch = PatchFile()
	patch.hunks = []
	current_hunk = None
	current_lines = []
	for line in lines:
		match = hunk_header_pattern.match(line)
		if match != None:
			if current_hunk == None:
				patch.header = current_lines
			current_hunk = Hunk()
			current_hunk.start_a = int(match[1])
			current_hunk.len_a = int(match[2])
			current_hunk.start_b = int(match[3])
			current_hunk.len_b = int(match[4])
			current_hunk.suffix = match[5]
			current_lines = []
			current_hunk.lines = current_lines
			patch.hunks.append(current_hunk)
		else:
			current_lines.append(line)
	return patch

def process_file(content):
	lines_a, lines_b = get_each_content(content)
	file_a = parse_file(lines_a)
	file_b = parse_file(lines_b)
	if file_a.header != file_b.header:
		return None
	sorted_hunks = files_a.hunks + files_b.hunks
	sorted_hunks.sort(key = lambda hunk: hunk.start_a)
	
	offset = 0
	last_hunk = None
	for hunk in sorted_hunks:
		hunk.is_duplicate = False
		if last_hunk != None:
			if hunk.start_a < last_hunk.start_a + last_hunk.len_a:
				if hunk.start_a != last_hunk.start_a or hunk.len_a != last_hunk.len_a or hunk.len_b != last_hunk.len_b or hunk.lines != last_hunk.lines:
					return None
				else:
					hunk.is_duplicate = True
		hunk.start_b = hunk.start_a + offset
		offset += hunk.len_b - hunk.len_a
		last_hunk = hunk
	
	output = ""
	
	for line in file_a.header:
		output += line
	for hunk in sorted_hunks:
		if not hunk.is_duplicate:
			output += "@@ -" + str(hunk.start_a) + "," + str(hunk.len_a) + " +" + str(hunk.start_b) + "," + str(hunk.len_b) + " @@" + hunk.suffix
			for line in hunk.lines:
				output += line
	
	return output

def main(files):
	for filename in files:
		with open(filename, "rw") as f:
			content = f.read()
			content = process_file(content)
			if content == None:
				print("Could not resolve conflicts in file " + filename)
			else:
				file.write(content)
				os.system("git add " + filename)
				print("Resolved conflicts in file " + filename)

if len(sys.argv) < 2:
	print("patch_mergetool.py <files...>")
else:
	main(sys.argv[1:])