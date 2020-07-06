### A Sequence-based and Context Modelling Framework for Recommendation (ActivRec)

#### Description:
This is a generic activity recommendation framework, which is suitable for generating recommendations based on context-rich user activity patterns. The key aspect which differentiates the activity recommendation framework proposed in this work from the traditional recommendation approaches is that it considers both sequence and context information in the user activities, during the recommendation generation process (input) as well as for the recommended output. (_This code is part of my PhD research work_). 


#### Datasets
The Gowalla and Geolife datasets used are available (for research purposes) at the following links, while the lifelog dataset is available on request. 

- [Gowalla dataset source](https://www.yongliu.org/datasets) (released solely for research purpose)
- [Geolife dataset source](https://www.microsoft.com/en-us/download/details.aspx?id=52367&from=https%3A%2F%2Fresearch.microsoft.com%2Fen-us%2Fdownloads%2Fb16d359d-d164-469e-9fd4-daa38f2b2e13%2Fdefault.aspx) (Microsoft Research License Agreement-Non-Commercial Use Only)


#### Data Pre-processing:

For Geolife dataset:

- A subset of this trajectory dataset contains labels for the mode of transport associated with the trajectories. For our experiments, this subset is used to build the activity timelines for each user and the name of the mode of transport is considered the activity name. 

- Each activity object in these timelines corresponds to an instance of a mode of transport used along with its features extracted from the corresponding list of  GPS trajectories. This dataset contains 10 different modes of transport, namely, bike, bus, car, subway, taxi, train, walk, airplane, boat and run. Moreover, each activity object in the timeline contains the mode of transport (activity name) as the item feature, user id as the user feature, and the following six context features: start time, duration, distance-travelled, average altitude, start and end geo-coordinates. 

- Only the data corresponding to weekdays are considered. 

- Consecutive modes of transport which have the same name and which occur within 5 minutes of each other are merged and considered as a single activity. 

- Of this data, a subset of 18 users are selected for the purpose of evaluation; these are the users for which the evaluation methodology allows at least 10 opportunities for recommending the next mode of transport. 

For Gowalla dataset:

- A subset of this dataset, has locations with categories assigned to them, such as, Italian Food, Bookstore, Theme Park, etc. These locations also have geographical features such as latitude, longitude, and other features such as number of users checking in to it, number of photos taken at the location, etc. In relation to the activity recommendation framework, each of the location categories is considered as an activity name and the recommendations made will be for the next category. Hence, for evaluation, only those checkin locations are considered which have assigned categories. 

- Categories are organised in a three-level hierarchy, consisting of 7, 134 and 151 level 1, 2, and 3 categories, respectively. For example, the level 1 category Food has child categories African, American,  Asian, Coffee Shop, etc. at level 2, while Coffee Shop has child categories Starbucks and Dunkin Donuts at level 3. Given the objective is to recommend activities (categories) to users, the level 2 categories are considered the most suitable level of granularity, and hence any checkin locations with level 3 categories are assigned the parent category at level 2. As such, the activity names in user timelines are given by the level 2 categories of the locations checked in to by users. 

- The activity objects created from this dataset contains user id as the user feature, the level 2 category name (activity name) as the item feature, and popularity (total number of checkins for that location), location, distance from previous, and duration from previous checkin as context features. 

- Only the data corresponding to weekdays are considered.

- To address multiple consecutive checkins by users at the same location, checkins were merged for a user if they had the same category, were less than 600 meters apart and occurred within an interval of 10 minutes in succession. 

- This dataset is spread over multiple countries and regions; however, for these experiments, the dataset was filtered to only checkins within locations lying in the Chicago timezone. 

- As the number of unique geo-locations in this dataset were quite large (85,233), for the purpose of computations and also for generalisation, they were mapped to hexagonal grids, each of area 1.18 km2 using the ISEA3H geodesic discrete global grid system. For each location, the geo-location of the centre of the grid bounding it was considered for distance computations. This resulted in 75% reduction in the number of unique geo-locations with 21,034 unique grids in this dataset. 

- Consecutive checkins belonging to the same category, which are within 600 meters and 10 minutes from each other, are merged (i.e. considered as a single activity object). 

- Only weekdays with a minimum of 5 checkins, with the checkins belonging to more than 1 distinct category and at least 3 distinct locations, were considered.    

- Days with more than 40 checkins were excluded as these were considered outliers. Those users with 10 or more days satisfying the aforementioned criteria were considered.  



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

3. Pre-process data and store in *./dataToRead/*.
 
4. Execute *org.activity.controller.SuperController* via setting build.gradle and executing *gradle run*.

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
