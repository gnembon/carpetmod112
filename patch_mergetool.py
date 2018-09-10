
import os
import re
import sys

class PatchFile:
	pass
class Hunk:
	pass
class Line:
	def __init__(self, content, added):
		self.content = content
		self.added = added
	def __eq__(self, other):
		return self.content == other.content and self.added == other.added

hunk_header_pattern = re.compile(r"^@@ -(\d+),(\d+) \+(\d+),(\d+) @@(.*)$", flags = re.DOTALL)

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
				content_a.append(Line(line, True))
		elif in_b:
			if line.startswith(">>>>>>>"):
				in_b = False
			else:
				content_b.append(Line(line, True))
		else:
			if line.startswith("<<<<<<<"):
				in_a = True
			else:
				content_a.append(Line(line, False))
				content_b.append(Line(line, False))
	return (content_a, content_b)

def parse_file(lines):
	patch = PatchFile()
	patch.hunks = []
	current_hunk = None
	current_lines = []
	for line in lines:
		match = hunk_header_pattern.match(line.content)
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
	sorted_hunks = file_a.hunks + file_b.hunks
	for hunk in sorted_hunks:
		hunk.changed = any(line.added for line in hunk.lines)
	sorted_hunks.sort(key = lambda hunk: hunk.start_a)
	
	last_hunk = None
	for hunk in sorted_hunks:
		hunk.is_duplicate = False
		if last_hunk != None:
			if hunk.start_a < last_hunk.start_a + last_hunk.len_a:
				if hunk.start_a != last_hunk.start_a or hunk.len_a != last_hunk.len_a or hunk.len_b != last_hunk.len_b or hunk.lines != last_hunk.lines:
					if hunk.changed and last_hunk.changed:
						return None
					elif hunk.changed:
						last_hunk.is_duplicate = True
					else:
						hunk.is_duplicate = True
				else:
					hunk.is_duplicate = True
		last_hunk = hunk
	
	sorted_hunks = [hunk for hunk in sorted_hunks if not hunk.is_duplicate]
	
	offset = 0
	for hunk in sorted_hunks:
		hunk.start_b = hunk.start_a + offset
		offset += hunk.len_b - hunk.len_a
	
	output = ""
	for line in file_a.header:
		output += line.content
	for hunk in sorted_hunks:
		output += "@@ -" + str(hunk.start_a) + "," + str(hunk.len_a) + " +" + str(hunk.start_b) + "," + str(hunk.len_b) + " @@" + hunk.suffix
		for line in hunk.lines:
			output += line.content
	
	return output

def main(files):
	for filename in files:
		with open(filename) as f:
			content = f.read()
		content = process_file(content)
		if content == None:
			print("Could not resolve conflicts in file " + filename)
		else:
			with open(filename, "w") as f:
				f.write(content)
			os.system("git add " + filename)
			print("Resolved conflicts in file " + filename)

if len(sys.argv) < 2:
	print("patch_mergetool.py <files...>")
else:
	main(sys.argv[1:])