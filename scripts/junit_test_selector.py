import os
import random
from datetime import datetime

project1_tests = r"scripts\output\eclipse-collections-11.1.0.txt"   # ALL Eclipse Collections tests
project2_tests = r"scripts\output\RxJava-3.1.8.txt"                 # ALL RxJava tests
project3_tests = r"scripts\output\stubby4j-7.6.0.txt"               # ALL stubby4j tests

num_tests = 30  # Number of junit tests to select from each project
jmh_command_base = "java -jar \"ju2jmh-jmh.jar\" -f 1 -wi 0 -i 1 -r 100ms -foe true"
def select_random_junit_tests(project_tests_path, num_tests):
    with open(project_tests_path, "r") as f:
        lines = f.readlines()

    # Remove newline characters from the end of each line
    lines = [line.strip() for line in lines]

    # Create a list of tuples containing (row_number, line_content)
    enumerated_lines = list(enumerate(lines, start=1))

    # Shuffle the lines randomly
    random.shuffle(enumerated_lines)

    # Take the first 'num_tests' lines and return it
    return enumerated_lines[:num_tests]

def generate_output(project1_tests, project2_tests, project3_tests):
    print("Selecting junit tests...")
    selected_tests = {}

    current_time = datetime.now().time()
    time_str = str(current_time.hour) + "h" + \
               str(current_time.minute) + "m" + \
               str(current_time.second) + "s" + \
               str(current_time.microsecond)[:-2] + "ms"

    with open(os.path.join(os.path.dirname(__file__), "output", f"selected_junit_tests-{time_str}.txt"), "w") as f:
        for project_tests in [project1_tests, project2_tests, project3_tests]:
            
            project_tests_path, num_tests = project_tests
            project_name = extract_project_name(project_tests_path)
            selected_tests[project_name] = select_random_junit_tests(project_tests_path, num_tests)

            jmh_command = jmh_command_base
            f.write(f"# {project_name} | SELECTED {num_tests} TESTS \n ROW IN ORIGIN FILE | TEST NR. | TEST METHOD PATH \n")
            i = 1
            for row_number, row_content in selected_tests[project_name]:
                row_number_str = str(row_number).rjust(7)
                f.write(f"{row_number_str} |     {i}.    {row_content[:7]}{row_content[7:]}\n")
                i += 1
                jmh_command += ' ' + row_content
            f.write("\n")

            f.write(jmh_command + '\n\n')

            print(f"{project_name} done!")
    print("ALL DONE!")

    for project_tests in [project1_tests, project2_tests, project3_tests]:
        
        project_tests_path, num_tests = project_tests
        project_name = extract_project_name(project_tests_path)

        with open(os.path.join(os.path.dirname(__file__), "output", f"{project_name}-{time_str}.txt"), "w") as f:
            class_names = []
            for row_number, row_content in selected_tests[project_name]:
                row_content = row_content[:row_content.rfind('.')]
                class_names.append(row_content)
            for class_name in list(dict.fromkeys(class_names)):
                f.write(f"{class_name}\n")
            print(f"{project_name} done again!")

def extract_project_name(file_path):
    start_index = file_path.find("scripts\\output\\") + len("scripts\\output\\")
    end_index = file_path.find(".txt", start_index)
    if end_index != -1:
        return file_path[start_index:end_index]
    else:
        return file_path[start_index:]

generate_output((project1_tests, num_tests), 
                (project2_tests, num_tests), 
                (project3_tests, num_tests))