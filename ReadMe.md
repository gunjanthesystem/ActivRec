### A Sequence-based and Context Modelling Framework for Recommendation (SequenceContextRecSys)

#### Description:
This is a generic activity recommendation framework, which is suitable for generating recommendations based on context-rich user activity patterns. In recent years, the amount of available choices to users have increased drastically, while at the same time there has been a significant rise in pervasive computing and collection of user activity data by individuals and different applications. Recommender systems can leverage such data to help users in the decision making process by suggesting items of interest and discovering new items. The key aspect which differentiates the generic activity recommendation framework proposed in this work from the traditional recommendation approaches is that it considers both sequence and context information in the user activities. It has been argued in this work that it is important to consider both sequences and context in user activities, as they contain information relevant to user preferences. The proposed activity recommendation framework considers sequence and context during the recommendation generation process as well as for the recommended output. (_This code is part of my PhD research work_). 

#### Execution:
For running the proposed Activity Recommendation Algorithms (ActivRec, in paper) and the Java-based baselines:
1. Import './SequenceContextRecSys' as Gradle project.
2. Set parameters in org.activity.constants.Constant 
3. Execute org.activity.controller.SuperController 

For running the Python-based baselines:
- Use './BaselinesInPython/PythonBaselinesFromSarsTutorial.py'
   (see https://github.com/mquad/sars_tutorial/ for initial setup)

#### Publications:
Please cite the following papers related to this work:
1.  Kumar, G., Jerbi, H., Gurrin, C. and O'Mahony, M. P.,
    _Towards Activity Recommendation from Lifelogs_, 
    Proceedings of the 16th International Conference on Information Integration and Web-based Applications & Services, ACM, 2014, pp. 87-96

2.  Kumar, G., Jerbi, H. and O'Mahony, M. P.,
    _Personalised Recommendations for Modes of Transport: A Sequence-based Approach_,
    The 5th ACM SIGKDD International Workshop on Urban Computing (UrbComp 2016), 2016

3.  Kumar, G., Jerbi, H. and O'Mahony, M. P.,
    _Towards the Recommendation of Personalised Activity Sequences in the Tourism Domain_,
    The 2nd ACM RecSys Workshop on Recommenders in Tourism (RecTour 2017), 2017 

4.  Kumar, G., Jerbi, H. and O'Mahony, M. P., 2020 (_This latest publication is in-submission._)

Additional documentation will be provided soon. Queries can be sent to gunjan.kumar@insight-centre.org

#### Sources used for some baselines:    
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
