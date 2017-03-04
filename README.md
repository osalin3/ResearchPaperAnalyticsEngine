# ResearchPaperAnalyticsEngine

This repository contains an abstract of a Distributed Analysis application written in Java, to examine the contents of research papers using Hadoop’s map/reduce model.Included are two java src files, MapReduce.java and downloadFiles.java, where MapReduce.java calls the downloadFiles.java main class.

MapReduce.java uses the Apache OpenNLP to process text and detect Named entities within the text. Hadoop logic runs to extract the named references that have been collected. 

downloadFiles.java holds logic to sumbit a search query, detect and extract PDF metadata, programatically download PDFs, and produce txt versions that can be input to hadoop. 
