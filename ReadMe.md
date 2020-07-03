### A Sequence-based and Context Modelling Framework for Recommendation (ActivRec)

#### Description:
This is a generic activity recommendation framework, which is suitable for generating recommendations based on context-rich user activity patterns. The key aspect which differentiates the activity recommendation framework proposed in this work from the traditional recommendation approaches is that it considers both sequence and context information in the user activities, during the recommendation generation process (input) as well as for the recommended output. (_This code is part of my PhD research work_). 


#### Datasets
The Gowalla and Geolife datasets used are publicly available at the following links, while the lifelog dataset is available on request. 

- [Gowalla dataset source](https://www.yongliu.org/datasets)
- [Geolife dataset source](https://www.microsoft.com/en-us/download/details.aspx?id=52367&from=https%3A%2F%2Fresearch.microsoft.com%2Fen-us%2Fdownloads%2Fb16d359d-d164-469e-9fd4-daa38f2b2e13%2Fdefault.aspx)

The processed Gowalla and Geolife datasets used in the experiments are available in csv format in *./ActivRec/dataToRead/*, and in *./BaselinesInPython/datasets/* for the baselines implemented in Python.


#### Execution:
For running the proposed Activity Recommendation Algorithms (ActivRec, in paper) and the Java-based baselines:

1. Import *./ActivRec* as Gradle project and build using *gradle build*.
2. Set parameters:
	- for the experiments and algorithms in *org.activity.constants.Constant*
	- for domain/dataset in *org.activity.constants.DomainConstants*
	- for setting paths for reading data (some pre-processed data) in *org.activity.constants.PathConstants*.
		- Data to read is expected in *./dataToRead/*
		- Output files are written to *./dataWritten/*
	- for controlling verbosity of output and files to write in *org.activity.constants.VerbosityConstants*
 
3. Execute *org.activity.controller.SuperController* via setting build.gradle and executing*gradle run*.

For running the Python-based baselines:
- Use *./BaselinesInPython/PythonBaselinesFromSarsTutorial.py*
   (See https://github.com/mquad/sars_tutorial/ for Python-related initial setup.)

#### Publications:
For reference to this work, please cite the following papers:

1.  Kumar, G., Jerbi, H., Gurrin, C. and O'Mahony, M. P.,
    _Towards Activity Recommendation from Lifelogs_, 
    Proceedings of the 16th International Conference on Information Integration and Web-based Applications & Services, ACM, 2014, pp. 87-96

2.  Kumar, G., Jerbi, H. and O'Mahony, M. P.,
    _Personalised Recommendations for Modes of Transport: A Sequence-based Approach_,
    The 5th ACM SIGKDD International Workshop on Urban Computing (UrbComp 2016), 2016

3.  Kumar, G., Jerbi, H. and O'Mahony, M. P.,
    _Towards the Recommendation of Personalised Activity Sequences in the Tourism Domain_,
    The 2nd ACM RecSys Workshop on Recommenders in Tourism (RecTour 2017), 2017 

4.  Kumar, G., Jerbi, H. and O'Mahony, M. P., 2020 (_in-submission._)

Additional documentation will be added soon. Queries can be sent to gunjan.kumar@insight-centre.org

#### Sources used for some baselines recommenders:    
The implementation for the following baselines are based on code from the following sources:  

1. AKOM-order: (http://www.philippe-fournier-viger.com/spmf/) [Java]. 
2. GRU4Rec: (https://github.com/hidasib/GRU4Rec) [Python].
3. HGRU4Rec: (https://github.com/mquad/hgru4rec) [Python].
4. Prod2VecRec: (https://github.com/mquad/sars_tutorial/blob/master/04_Prod2Vec.ipynb) [Python].
5. FPMCRec: (https://github.com/mquad/sars_tutorial/blob/master/03_FPMC.ipynb) [Python].


--------------------------------------

<p>
<a rel="license" href="http://creativecommons.org/licenses/by-nc/3.0/ie/"><img alt="Creative Commons Licence" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/3.0/ie/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc/3.0/ie/">Creative Commons Attribution-NonCommercial 3.0 Ireland License</a>.
</p>

--------------------------------------
