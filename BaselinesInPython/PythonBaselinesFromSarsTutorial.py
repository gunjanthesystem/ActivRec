#!/usr/bin/env python
# coding: utf-8

# ## Aim :
# ### 1. implement full verion of HGRU4Rec as or comparable to i https://github.com/mquad/hgru4rec 
# ### 2. do hyperparameter tuning 

# In[ ]:


############ Start of added on 1 Feb for GPU issue
import os
# os.environ["THEANO_FLAGS"] = "mode=FAST_RUN,device=cuda3,floatX=float32"
os.environ["THEANO_FLAGS"] = "device=cuda1,dnn.enabled=False,floatX=float32,force_device=True"
os.environ["CUDA_VISIBLE_DEVICES"]="1,3"

import theano
# theano.config.root = '/usr/local/cuda-9.1/'
# theano.config.dnn.enabled = 'True'
# theano.config.dnn.base_path = '/usr/local/cuda-9.1/'
# theano.config.dnn.library_path = '/usr/local/cuda-9.1/lib64/'
# theano.config.dnn.include_path = '/usr/local/cuda-9.1/include/'
# theano.config.dnn.bin_path = '/usr/local/cuda-9.1/lib64/'
# THEANO_FLAGS='device=cuda,dnn.enabled=auto,floatX=float32'

#print(theano.config)
print('~~~~~~~~~~~~~~~~~~~~~~~~~~~')
#exit()
############ End of added on 1 Feb for GPU issue


# In[ ]:


#import theano


# In[ ]:


print(theano.config.device)


# In[ ]:


import numpy as np
import pandas as pd
#import matplotlib.pyplot as plt
#matplotlib inline
import datetime
import pickle
import pathlib

from util.data_utils import create_seq_db_filter_top_k, sequences_to_spfm_format
from util.split import last_session_out_split
from util.metrics import precision, recall, mrr
from util import evaluation


# In[ ]:


def get_test_sequences(test_data, given_k):
    # we can run evaluation only over sequences longer than abs(LAST_K)
    test_sequences = test_data.loc[test_data['sequence'].map(len) > abs(given_k), 'sequence'].values
    return test_sequences

def get_test_sequences_and_users(test_data, given_k, train_users):
    # we can run evaluation only over sequences longer than abs(LAST_K)
    mask = test_data['sequence'].map(len) > abs(given_k)
    mask &= test_data['user_id'].isin(train_users)
    test_sequences = test_data.loc[mask, 'sequence'].values
    test_users = test_data.loc[mask, 'user_id'].values
    return test_sequences, test_users


# In[ ]:


# gunjan
########## Start of parameters to set #############
database_name = "lifelog2" #"gowalla1"  "geolife1" lifelog2
root_path = 'datasets/'

if database_name == "lifelog2":
    root_path += 'lifelog2/'
elif database_name == "geolife1":
    root_path += 'geolife1/'
elif database_name == "gowalla1":
    root_path += 'gowalla1/'
########## End of parameters to set #############


# In[ ]:


# gunjan
if database_name == "lifelog2":
    invalid_actIDs = ['0','11']
else:
    invalid_actIDs = []
    
path_to_write = root_path + database_name + "MQ_" + '' + "/"
pathlib.Path(path_to_write).mkdir(parents=True, exist_ok=True) 

#dataset_path = 'datasets/sessions.csv'
dataset_path = root_path + database_name+ 'InMQFormat.csv'
print("dataset_path = ",dataset_path)
# load this sample if you experience a severe slowdown with the previous dataset
#dataset_path = 'datasets/sessions_sample_10.csv'

# for the sake of speed, let's keep only the top-1k most popular items in the last month
#dataset = create_seq_db_filter_top_k(path=dataset_path, topk=1000, last_months=1) 

dataset = create_seq_db_filter_top_k(path=dataset_path, topk=10000, last_months=600) 
dataset.head()


# In[ ]:


from collections import Counter
cnt = Counter()
dataset.sequence.map(cnt.update);


# In[ ]:


# gunjan
print("dataset.shape = ", dataset.shape)
sequence_length = dataset.sequence.map(len).values
n_sessions_per_user = dataset.groupby('user_id').size()

print('Number of items: {}'.format(len(cnt)))
num_of_unique_actIDs = len(cnt) # gunjan
print('Number of users: {}'.format(dataset.user_id.nunique()))
print('Number of sessions: {}'.format(len(dataset)) )

print('\nSession length:\n\tAverage: {:.2f}\n\tMedian: {}\n\tMin: {}\n\tMax: {}'.format(
    sequence_length.mean(), 
    np.quantile(sequence_length, 0.5), 
    sequence_length.min(), 
    sequence_length.max()))

print('Sessions per user:\n\tAverage: {:.2f}\n\tMedian: {}\n\tMin: {}\n\tMax: {}'.format(
    n_sessions_per_user.mean(), 
    np.quantile(n_sessions_per_user, 0.5), 
    n_sessions_per_user.min(), 
    n_sessions_per_user.max()))
print('Most popular items: {}'.format(cnt.most_common(5)))


# In[ ]:


# gunjan's
# load training sessionIDs
path_train_sessionIDs = root_path + 'trainSessionIDs.csv'
path_test_sessionIDs = root_path + 'testSessionIDs.csv'
path_valid_rts = root_path + 'sessionIDSTInSecsOfValidRTs.csv'#'sessionIDIndexInDayOfValidRTs.csv'
path_sampled_test_userIDs = root_path + 'SampledUserIDs.csv'

train_sessions = pd.read_csv(path_train_sessionIDs,header= None)
test_sessions = pd.read_csv(path_test_sessionIDs,header= None)
valid_rts = pd.read_csv(path_valid_rts,header= None)
sampled_test_userIDs = pd.read_csv(path_sampled_test_userIDs,header= None)
sampled_test_userIDs = list(sampled_test_userIDs[0])

print("train_sessions.shape = ",train_sessions.shape)
print("test_sessions.shape = ", test_sessions.shape)
print("valid_rts.shape = ", valid_rts.shape)
print("valid_rts.head():\n ",valid_rts.head())

# convert to list
train_sessions_list = train_sessions.iloc[:,0].tolist()
test_sessions_list = test_sessions.iloc[:,0].tolist()
valid_rts_list = valid_rts.iloc[:,0].tolist()

#valid_rts_list
# len(valid_rts_list)
print("train_sessions_list = ",train_sessions_list)
print("valid_rts_list = ",valid_rts_list)
print("len(valid_rts_list) = ",len(valid_rts_list))
print("sampled_test_userIDs = ",sampled_test_userIDs)
print("len(sampled_test_userIDs) = ",len(sampled_test_userIDs))


# In[ ]:


#gunjan's split
# sort into training and test by matching sessionids
train_data = dataset[dataset.session_id.isin(train_sessions_list)]
test_data = dataset[dataset.session_id.isin(test_sessions_list)]
print("Train sessions: {} - Test sessions: {}".format(len(train_data), len(test_data)))
print("dataset.head() = \n",dataset.head())
print("train_data.head() = \n",train_data.head())
print("test_data.head() = \n",test_data.head())
# gunjan's
#user_key='user_id'
# session_key='session_id'
# time_key='ts'
# sessions = dataset.sort_values(by=[user_key, time_key]).groupby(user_key)[session_key]
# sessions.tail(n=20)
#last_session = sessions.last()
#train = data[~data.session_id.isin(last_session.values)].copy()
#test = data[data.session_id.isin(last_session.values)].copy()


# In[ ]:


#gunjan's  # get test sequences
given_k = 1 # only used for extracting test_sequences
test_sequences = test_data.loc[test_data['sequence'].map(len) > abs(given_k), 'sequence'].values
# get sessionIDs of corresponding test sequences
test_sequences_sessionIDs = test_data.loc[test_data['sequence'].map(len) > abs(given_k), 'session_id'].values
test_sequences_user_ids = test_data.loc[test_data['sequence'].map(len) > abs(given_k), 'user_id'].values


test_sequences = list(test_sequences)
test_sequences_sessionIDs = list(test_sequences_sessionIDs)
test_sequences_user_ids = list(test_sequences_user_ids)

#expecting test_sequences and test_sequences_sessionIDs to be in same order
print(test_data.head())
# print(test_sequences.head())
print(test_data['sequence'].values)
print(test_data['session_id'].values)

print('test_sequences = ',test_sequences)
print("test_sequences_sessionIDs = ",test_sequences_sessionIDs)
print("test_sequences_user_ids = ",test_sequences_user_ids)

print('test_sequences = ',len(test_sequences))
print("test_sequences_sessionIDs = ",len(test_sequences_sessionIDs))
print("test_sequences_user_ids = ",len(test_sequences_user_ids))
print("len(sampled_test_userIDs) = ",len(sampled_test_userIDs)) # users for which to make recommendations

type(test_sequences_user_ids)
# type(list(test_sequences_user_ids))
# test_sequences_sessionIDs
# print(seq)
# print(seq[:14])


# ## Start of addition

# ### Create validation set - last session (day) of each user

# In[ ]:


train_dataV, valid_dataV = last_session_out_split(train_data)
print('dataset.shape',dataset.shape)
print('train_data.shape',train_data.shape)
print('train_dataV.shape',train_dataV.shape)
print('valid_dataV.shape',valid_dataV.shape)
print(train_dataV.head())
valid_dataV
# train_data1.shape


# In[ ]:


# Get 10% of days as validation set
# find how many days i.e. sessions in training data per user
# (train_data)
# print(train_data.columns)
# print(train_data.describe)
# train_data_byUser = train_data.groupby(by="user_id")
# print(train_data_byUser.columns)


# In[ ]:


# print(givenSessionData.info())
# numOfSessionsForEachUser = sessionsGroupedByUser.count()
# df['a'] = sessionsTemp['a'].apply(lambda x: x + 1)
# print('numOfSessionsForEachUser:\n',numOfSessionsForEachUser)
# print('numOf last Sessions to split out for each user:\n',((percentageToSplitOut/100)*numOfSessionsForEachUser))
# 0.10*numOfSessionsForEachUser
#     last_session = sessions.last()
#     train = data[~data.session_id.isin(last_session.values)].copy()
#     test = data[data.session_id.isin(last_session.values)].copy()
#############################################################################
### Since 13 July 2019

import math
percentageToSplitOut = 10 # for validation
givenSessionData = train_data


sessionsGroupedByUser = givenSessionData.sort_values(by=['user_id', 'ts']).groupby('user_id')['session_id']

chosenLastSessionIDs = np.empty([0,])
# from each group get x% of their last session ids and the rest session ids separated
for name,group in sessionsGroupedByUser:
    print("group name = ",name) #print((group))
    numOfSessions = group.shape[0] #print('shape = ',group.shape)
    print('numOfSessions = ',numOfSessions)
    numOfSessionIdsForXPercentage = (percentageToSplitOut/100)*numOfSessions
    #print('numOfSessionIdsForXPercentage = ',numOfSessionIdsForXPercentage)
    numOfSessionIdsForXPercentage = math.ceil(numOfSessionIdsForXPercentage)
    print('numOfSessionIdsForXPercentage after ceil = ',numOfSessionIdsForXPercentage)
    selectedSessionIDsForXPercentage = group[-numOfSessionIdsForXPercentage:]
    #print('selectedSessionIDsForXPercentage = \n',selectedSessionIDsForXPercentage)
    print('selectedSessionIDsForXPercentage as values = \n',selectedSessionIDsForXPercentage.values)
    #print('selectedSessionIDsForXPercentage as values = \n',type(selectedSessionIDsForXPercentage.values))
    chosenLastSessionIDs = np.append(chosenLastSessionIDs,selectedSessionIDsForXPercentage.values)
    print('------')
print("chosenLastSessionIDs = ", chosenLastSessionIDs)
print("--------Splitting last ",percentageToSplitOut,"% into validation set")
train_dataV2 = train_data[~train_data.session_id.isin(chosenLastSessionIDs)].copy()
valid_dataV2 = train_data[train_data.session_id.isin(chosenLastSessionIDs)].copy()
print('dataset.shape',dataset.shape)
print('train_data.shape',train_data.shape)
print('train_dataV.shape',train_dataV.shape) ## older one, last day only taken out
print('valid_dataV.shape',valid_dataV.shape) ## older one, last day only taken out
print('train_dataV2.shape',train_dataV2.shape) ## new one, x% of last days  taken out
print('valid_dataV2.shape',valid_dataV2.shape) ## new one, x% of last days  taken out

# print(train_dataV.head())
# valid_dataV


# In[ ]:


valid_dataV2.head()


# In[ ]:


# from util.data_utils import dataset_to_gru4rec_format
# valid_dataVGRU = dataset_to_gru4rec_format(valid_dataV)
# valid_dataVGRU.head()


# In[ ]:


useHierarchicalGRU = True #False  ## IMPORTANT PARAMETER

# Create a dictionary of possible parameters configurations to test
# Used on 14 July 2019
import numpy
from random import choices

# define the grid search parameters
if database_name == "lifelog2":
    batch_sizes_for_database = [4]
if database_name == "geolife1":
    batch_sizes_for_database = [10] #[9, 18]
if database_name == "gowalla1":
    batch_sizes_for_database = [80] #10, 50, 100]

epochs = [100]
############################################################
possible_dropouts = [0.0, 0.1, 0.2, 0.3]
# possible_dropouts_wts = [0.2, 0.5, 0.2, 0.1]
possible_dropouts_wts = [0.25, 0.25, 0.25, 0.25] # equal prob
if useHierarchicalGRU:
    possible_dropouts_count = 2 #Geolife 2  #DCU:2  
else:
    possible_dropouts_count = 6 #Geolife 2  #DCU:2  
############################################################    
learning_rates = [0.05, 0.1, 0.2, 0.5]
# learning_rates_wts = [0.2, 0.6, 0.1 ,0.1]
learning_rates_wts = [0.25, 0.25, 0.25, 0.25] # equal prob
# learning_rates_wts = [0, 0, 0.2 ,0.8]
if useHierarchicalGRU:
    learning_rates_count = 4 #Geolife 4  #DCU:4 
else:
    learning_rates = [0.05, 0.1, 0.2, 0.3, 0.4, 0.5]
    learning_rates_wts = [0.16, 0.16, 0.16, 0.16, 0.16,0.16] # equal prob
    learning_rates_count = 10 #Geolife 4  #DCU:4 
############################################################    
momentums = [0.0, 0.1, 0.2, 0.3] ## Added 0.3 on 15 Jul 2019
# momentums_wts = [0.33333, 0.33333, 0.33333] #[0.4, 0.4, 0.2]
momentums_wts = [0.25, 0.25, 0.25, 0.25] #[0.4, 0.4, 0.2]
if useHierarchicalGRU:
    momentums_count = 3 #Geolife 3  #DCU:3 
else:
    momentums_count = 10 #Geolife 3  #DCU:3 
############################################################    
nonrep_count = 0
max_nonrep_count = 20 #100 #100 ## added on 15 July 2019
numOfRepetitionsAvoided = 0
# print(range(1, possible_dropouts_count))

# for i in range(0,2):
#     print(i)
dict_of_models = {}

if useHierarchicalGRU:
    max_nonrep_countTemp = len(possible_dropouts)*len(possible_dropouts)*len(possible_dropouts)*len(learning_rates)*len(momentums)
    if max_nonrep_countTemp < max_nonrep_count:
        max_nonrep_count = max_nonrep_countTemp
    print('max_nonrep_count = ',max_nonrep_count)
    while nonrep_count < max_nonrep_count:
        for batch_size in batch_sizes_for_database:
            for epoch in epochs:
                for drop1_i in range(0, possible_dropouts_count):
                    drop1 = choices(possible_dropouts, possible_dropouts_wts)[0]
        #             print(drop1)
                    for drop2_i in range(0, possible_dropouts_count):
                        drop2 = choices(possible_dropouts, possible_dropouts_wts)[0]
                        for drop3_i in range(0, possible_dropouts_count):
                            drop3 = choices(possible_dropouts, possible_dropouts_wts)[0]
                            for learning_rate_i in range(0, learning_rates_count):
                                learning_rate = choices(learning_rates, learning_rates_wts)[0]
                                for momentum_i in range(0, momentums_count):
                                    momentum = choices(momentums, momentums_wts)[0]
                                    # count += 1 
                                    if nonrep_count > max_nonrep_count:
                                        break
                                    else:
                                        paramaters = {}
                                        paramaters["batch_size"] = batch_size
                                        paramaters["epoch"] = epoch
                                        paramaters["drop1"] = drop1
                                        paramaters["drop2"] = drop2
                                        paramaters["drop3"] = drop3
                                        paramaters["learning_rate"] = learning_rate
                                        paramaters["momentum"] = momentum

                                        if paramaters in dict_of_models.values():
                                            numOfRepetitionsAvoided += 1
                                        else:
                                            dict_of_models[nonrep_count] = paramaters
                                            nonrep_count += 1
                                            print(nonrep_count,":- batch_size=",batch_size," epoch=",
                                                  epoch," drop1=",drop1," drop2=",drop2," drop3=",drop3,
                                                  " learning_rate=",learning_rate," momentum=",momentum
                                                  )
                            
else:  
    max_nonrep_countTemp = len(possible_dropouts)*len(learning_rates)*len(momentums)
    if max_nonrep_countTemp < max_nonrep_count:
        max_nonrep_count = max_nonrep_countTemp
    print('max_nonrep_count = ',max_nonrep_count)
    while nonrep_count < max_nonrep_count:
        for batch_size in batch_sizes_for_database:
            for epoch in epochs:
                for drop1_i in range(0, possible_dropouts_count):
                    drop1 = choices(possible_dropouts, possible_dropouts_wts)[0]
        #             print(drop1)
                    #for drop2_i in range(0, possible_dropouts_count):
                        #drop2 = choices(possible_dropouts, possible_dropouts_wts)[0]
                        #for drop3_i in range(0, possible_dropouts_count):
                            #drop3 = choices(possible_dropouts, possible_dropouts_wts)[0]
                    for learning_rate_i in range(0, learning_rates_count):
                        learning_rate = choices(learning_rates, learning_rates_wts)[0]
                        for momentum_i in range(0, momentums_count):
                            momentum = choices(momentums, momentums_wts)[0]
                            # count +=1 
                            if nonrep_count > max_nonrep_count:
                                break;
                            else:
                                paramaters = {}
                                paramaters["batch_size"] = batch_size
                                paramaters["epoch"] = epoch
                                paramaters["drop1"] = drop1
                                #paramaters["drop2"] = drop2
                                #paramaters["drop3"] = drop3
                                paramaters["learning_rate"] = learning_rate
                                paramaters["momentum"] = momentum

                                if paramaters in dict_of_models.values():
                                    numOfRepetitionsAvoided += 1
                                else:
                                    dict_of_models[nonrep_count] = paramaters                            
                                    nonrep_count += 1
                                    print(nonrep_count,":- batch_size=",batch_size," epoch=",
                                          epoch," drop1=",drop1," learning_rate=",learning_rate,
                                          " momentum=",momentum
                                         )
                                
print("numOfRepetitionsAvoided = ",numOfRepetitionsAvoided)
print("nonrep_count = ",nonrep_count)
print(dict_of_models)
print(len(dict_of_models))


# In[ ]:


from recommenders.RNNRecommenderNotSimplified import RNNRecommenderNotSimplified
dict_of_random_models = {}


if useHierarchicalGRU:
    for model in dict_of_models:
        print(model," = ", dict_of_models[model])
        print(dict_of_models[model]["batch_size"])
        HGRU4RecRecommender = RNNRecommenderNotSimplified(session_layers=[100], 
                                                          user_layers=[100],
                                                          batch_size= dict_of_models[model]["batch_size"],
                                                          learning_rate=dict_of_models[model]["learning_rate"],
                                                          momentum=dict_of_models[model]["momentum"],
                                                          dropout=(dict_of_models[model]["drop1"],
                                                                   dict_of_models[model]["drop2"],
                                                                   dict_of_models[model]["drop3"]),
                                                          epochs=dict_of_models[model]["epoch"],
                                                          personalized= True) ## ALERT
                                                          #early_stopping =False)
        HGRU4RecRecommender.fit(train_data = train_dataV2) ## CHANGED TO 13 JULY NEW VALIDATION DATA
        dict_of_random_models[model] = HGRU4RecRecommender
else:    
    for model in dict_of_models:
        print(model," = ", dict_of_models[model])
        print(dict_of_models[model]["batch_size"])
        HGRU4RecRecommender = RNNRecommenderNotSimplified(session_layers=[100], 
                                                          user_layers=[100],
                                                          batch_size= dict_of_models[model]["batch_size"],
                                                          learning_rate=dict_of_models[model]["learning_rate"],
                                                          momentum=dict_of_models[model]["momentum"],
                                                          dropout=(dict_of_models[model]["drop1"]),
                                                          epochs=dict_of_models[model]["epoch"],
                                                          personalized= False) ## True) ## ALERT
                                                          #early_stopping =False)

        HGRU4RecRecommender.fit(train_data = train_dataV2) ## CHANGED TO 13 JULY NEW VALIDATION DATA
        dict_of_random_models[model] = HGRU4RecRecommender    
    


# 
print(dict_of_random_models)

# HGRU4RecRecommender = RNNRecommenderNotSimplified(session_layers=[20], 
#                              user_layers=[20],
#                              batch_size= batch_size_for_database, #16, #4,  #16,
#                              learning_rate=0.5,
#                              momentum=0.1,
#                              dropout=(0.1,0.1,0.1),
#                              epochs=2,
#                              personalized=True)
#                              #early_stopping =False)
# HGRU4RecRecommender.fit(train_data = train_dataV)
# dict_of_recommenders['HGRU4RecRecommender'] = HGRU4RecRecommender


# In[ ]:


print('len(dict_of_random_models) = ',len(dict_of_random_models))


# In[ ]:


METRICS = {'precision':precision, 
           'recall':recall,
           'mrr': mrr}
TOPN = 20 # length of the recommendation list

# GIVEN_K=1, LOOK_AHEAD=1, STEP=1 corresponds to the classical next-item evaluation
GIVEN_K = 1
LOOK_AHEAD = 1
STEP = 1

## CHANGED TO 13 JULY NEW VALIDATION DATA
valid_sequences, valid_users = get_test_sequences_and_users(valid_dataV2, GIVEN_K, train_dataV2['user_id'].values) # we need user ids now!
print('{} sequences available for evaluation on validation set ({} users)'.format(len(valid_sequences), len(np.unique(valid_users))))


# In[ ]:


dict_of_RNNResults = {}
for RNNModelNum in dict_of_random_models:
    print(RNNModelNum, " = ", dict_of_random_models[RNNModelNum])
    results = evaluation.sequential_evaluation(dict_of_random_models[RNNModelNum],
                                           test_sequences=valid_sequences,
                                           users=valid_users,
                                           given_k=GIVEN_K,
                                           look_ahead=LOOK_AHEAD,
                                           evaluation_functions=METRICS.values(),
                                           top_n=TOPN,
                                           scroll=True,  # scrolling averages metrics over all profile lengths
                                           step=STEP)
    dict_of_RNNResults[RNNModelNum] = results
    
print(dict_of_RNNResults)

    


# In[ ]:


import csv

label = ''
if useHierarchicalGRU:
    label = 'HGRU'
else:
    label = 'GRU'
    
with open(database_name + 'RandomParameterTuningResuls'+label+datetime.datetime.now().strftime("%Y-%m-%d-%H-%M")+".csv", 'w') as csv_file:
    writer = csv.writer(csv_file)
    writer.writerow(['ModelNum', 'Precision', 'Recall', 'MRR', 'ModelDetails'])
    for key, value in dict_of_RNNResults.items():
        writer.writerow([key, value[0], value[1], value[2],dict_of_models[key]])


# In[ ]:
## Added on 14 JUly 2019 to save the trained models
if False: # disabled on 16 July as pickling giving maximum recursion depth error
    label = ''
    if useHierarchicalGRU:
        label = 'HGRU'
    else:
        label = 'GRU'
    pickle_out = open(database_name + 'dict_of_random_models'+ label + datetime.datetime.now().strftime("%Y-%m-%d-%H-%M")+".pickle", 'wb')
    pickle.dump(dict_of_random_models, pickle_out)
    pickle_out.close
    #https://stackoverflow.com/questions/2134706/hitting-maximum-recursion-depth-using-pickle-cpickle


# In[ ]:


# from evaluationMQ import evaluate_sessions_batch_hier_bootstrap

# dict_of_RNNResults = {}
# for RNNModelNum in dict_of_random_models:
#     print(RNNModelNum, " = ", dict_of_random_models[RNNModelNum])
#     recall, mrr, df_ranks = evaluate_sessions_batch_hier_bootstrap(dict_of_random_models[RNNModelNum].model,
#                                                                train_data =  dataset_to_gru4rec_format(train_dataV),
#                                                                test_data =  dataset_to_gru4rec_format(valid_dataV),
#                                                                cut_off=10,
#                                                                output_rankings=True,
#                                                                bootstrap_length=5,
#                                                                batch_size=100,
# #                                                                items=None,
#                                                                session_key='session_id',
#                                                                user_key='user_id',
#                                                                item_key='item_id',
#                                                                time_key='ts')   



# # results = evaluation.sequential_evaluation(dict_of_random_models[RNNModelNum],
# #                                            test_sequences=valid_sequences,
# #                                            users=valid_users,
# #                                            given_k=GIVEN_K,
# #                                            look_ahead=LOOK_AHEAD,
# #                                            evaluation_functions=METRICS.values(),
# #                                            top_n=TOPN,
# #                                            scroll=True,  # scrolling averages metrics over all profile lengths
# #                                            step=STEP)
#     dict_of_RNNResults[RNNModelNum] = results
    
# print(dict_of_RNNResults)

print("ALL DONE!!")
# ## End of primary addition

# In[ ]:
