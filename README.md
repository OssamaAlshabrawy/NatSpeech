# NatSpeech: Detection of Speech in Naturalistic Environments

NatSpeech is a speech activity detection (SAD) algorithm that work in naturalistic settings without any constraints on the recorded data and therefore the data is not recorded in lab-setting like most of the publiuc datasets. NatSpeech is a deep learning model built with deeplearning4j https://deeplearning4j.org. 

## Prerequisites
* Java (developer version) 1.7 or later (Only 64-Bit versions supported)
* Apache Maven (automated build and dependency manager)
* IntelliJ IDEA or Eclipse
* Git

**Use the command line to enter the following:**
```
$ git clone https://github.com/OssamaAlshabrawy/NatSpeech.git
$ cd NatSpeech/
$ mvn clean install
```

**The preprocessing:**
The wav files should be copied to the wav sub-directory in data directory and annotation files to annotationFiles sub-directory. Then follow the steps to run the preprocessing:
1. Run the file: RunFrames.java, then all the frames files will go to framesFiles sub-directory
2. Run the file: RunTrainingSamples.java, in order to get the annotated frames. Each frames is a row and the label will be the last coulmn in that row. Since we have 256 frame length, then the label will aprrear as the 257th column.
3. Now, the preprocessed files will be ready to be fed to the deep learning model.

**Training:**
* Run the file: GBLSTM.java


