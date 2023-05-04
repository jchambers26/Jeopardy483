# automatically run all of the different options

def main():
    analyzers = {"StandardAnalyzer": "-s",
                 "EnglishAnalyzer": "-e",
                 "SimpleAnalyzer": "-si",
                 "StopAnalyzer": "-st",
                 "KeywordAnalyzer": "-k",
                 "SnowballAnalyzer": "-sn",
                 "WhitespaceAnalyzer": "-w"}
    cosine_similarity['-y', '-n']
    for analyzer in analyzers:
        run_jeopardy(analyzers[analyzer], )


if __name__ == "main":
    main()

    // args[0] - pick analyzer
    // StandardAnalyzer - - "-s"
    // EnglishAnalyzer - - "-e"
    // SimpleAnalyzer - - "-si"
    // StopAnalyzer - - "-st"
    // KeywordAnalyzer - - "-k"
    // SnowballAnalyzer - - "-sn"
    // WhitespaceAnalyzer - - "-w"
