# automatically run all of the different options
import sys
import subprocess


def cmd(command):
    print("\n\nCOMMAND:", command, end='\n\n')
    process = subprocess.Popen(
        command, stdout=subprocess.PIPE, stderr=None, shell=True)
    output = process.communicate()

    print(output[0])


def run_jeopardy(analyzer, cs_option):

    cmd(
        f'mvn exec:java -Dexec.mainClass=arizona.Jeopardy -Dexec.args="{analyzer} {cs_option}"')


if __name__ == "__main__":
    print("started")
    analyzers = {"StandardAnalyzer": "-s",
                 "EnglishAnalyzer": "-e",
                 "SimpleAnalyzer": "-si",
                 #  "StopAnalyzer": "-st",
                 "KeywordAnalyzer": "-k",
                 #  "SnowballAnalyzer": "-sn",
                 "WhitespaceAnalyzer": "-w"}
    cosine_similarity = ['-y', '-n']
    cmd("mvn package")

    for analyzer in analyzers:
        for tf in cosine_similarity:
            run_jeopardy(analyzers[analyzer], tf)
