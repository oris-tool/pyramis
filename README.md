
# Pyramis library contents

In this repository is contained the code used for the experiments described in the paper "Compositional Analysis of Hierarchical UML Statecharts" by Laura Carnevali, Reinhard German, Francesco Santoni, and Enrico Vicario

For the sake of reproducibility the repository contains both the source code for the analysis implementation and the source code for the execution of the individual experiments. The code is available as .java classes to be executed through the Java virtual machine. Such experiments executables are all contained in the "src/test/java/it/unifi/hierarchical/" folder and are distributed among two subfolders: */analysisScalability* and */analysis*.

In the first subfolder are contained the .java files corresponding to the scalability analysis experiments; in the second subfolder are contained the .java files used in the case study experiments.



## Installation
The repository requires the ![Sirio library](https://github.com/oris-tool/sirio) and can be directly built through Maven from the provided 'pom.xml'

Note that Java 11 is required for Sirio to work.



## Experiments on scalability

The experiments on scalability rely on a ground truth obtained from a long running simulation. Such simulations require a long time of execution due both to reach a sufficient precision and for the high costs in sampling values for the expolinomial distributions present in the described test cases.
Due to the high number of ground truths required, given the many combinations of properties to test, it is preferable to sample and cache values for each expolinomial distribution and reuse them every time the same distribution is exercised in each different simulation.
In so doing we are reusing the same values by associating each different state of the models to a different sampling run.

To reproduce correctly our experiments such high number of files would need to be created and used: it is either taxing on computation time (at least 3 days of execution are needed to cache the values) or space.

While the user can absolutely re-cache the correct amount of files needed, we also propose an intermediate solution by instead offering 10 million samples of each different expolinomial distribution to be shared among the states (thus incurring in dependence). 

### Re-cache expolinomial samples (~days)
To re-cache the expolinomial samples (both for the support [0,1] and the support [0,10] the  **main()** in *src/test/java/it/unifi/hierarchical/analysisScalability/PyramisCreateSampFiles.java* to create 'c'*100 files each of which contains 1 million samples; 'c' is currently set to 10.

The created files are placed in the folders *src/main/resources/samplesExpolinomial* and *src/main/resources/samplesExpolinomial10* from which they are directly accessed by the sampling routines.


### Generation of the ground truth(~ several hours - 1 day)

Execute *src/test/java/it/unifi/hierarchical/analysisScalability/PyramisSamplerFromFiles.java"

The analysis is done for all combinations of the variables Behaviour length(seq values 2 to 4), parallelism degree(parallel values 2 to 4), hierarchy depth(depth values 2 to 4) and type of Regions in the HSMP (type FIRST and LAST)

For the purpose of visualizing the distribution of the failure time, for each combination of variables, a file containing 1000000 samples of the times to failure of the fault tree under study is generated.
Such files are generated in the folder *src/main/resources/groundTruthDistributions*.

The ground truth files are placed in *src/main/resources/pyramisSimulation/*

#### Rare events

Specifically for the rare events variation the ground truth is produced by executing *src/test/java/it/unifi/hierarchical/analysisScalability/PyrStudyResultsRareEvents.java*


#### CDF of the ground truth (~seconds)
Execute *src/test/java/it/unifi/hierarchical/analysisScalability/PyrStudyDrawCDF.java* to create secondary files in *src\main\resources\groundTruthCDF* containing the cdf of the samplings.

Such files can be used to plot the cdf of the ground truth.


### Scalability Analysis  (~hours - 1 day)

Execute *src/test/java/it/unifi/hierarchical/analysisScalability/Pyramis.java* it will execute the analysis of all the combinations of the variables Behaviour length(seq values 2 to 4), parallelism degree(parallel values 2 to 4), hierarchy depth(depth values 2 to 4) and type of Regions in the HSMP (respectively of type FIRST and LAST); for each combination 20 simulations requiring the same time will be executed: note that these will not use the pre-cached values.

The produced values are placed in *src/main/resources/pyramisAnalytic*.


#### Variations

To produce the results with longer events and cycles one can execute respectively:

For the version with longer events execute *src/test/java/it/unifi/hierarchical/analysisScalability/PyramisLongEvents.java*.

For the version with cycles execute *src/test/java/it/unifi/hierarchical/analysisScalability/PyramisCyles.java*.

Note that the folders  *pyramisAnalytic* and *pyramisSimulation* must be emptied before executing differing types.


### Comparisons between sampling and analyses (~seconds)

All comparison functions distinguish the files belonging to different properties combinations (Behavior Lenght, parallelism degree and hierarchy depth) based on the name of the files, the artifacts produced by the comparison functions are all generated to the *src/main/resources/pyramisRes*  folder.




Execute  *src/test/java/it/unifi/hierarchical/analysisScalability/PyrStudyResults.java* to obtain the min max average errors and standard deviation of values from the corresponding ground truth.


Execute  *src/test/java/it/unifi/hierarchical/analysisScalability/PyrStudyResultsVarianceOverSimulations.java* to obtain average errors on the many runs of same-time simulations: to do so execute the function after having removed the files corresponding to the analyses from src//main//resources//pyramisRes (the folder must contain only the same-time simulation results, automatically it reads all the folder files and groups them based on the name) in src//main//resources//summa produces the averages of errors over same-time simulation batches for each combination of properties

#### Comparisons for Cycle variation
For cycles execute *src/test/java/it/unifi/hierarchical/analysisScalability/PyrStudyResultsVarianceCycles.java* : the execution is different as the linkage of the states names between simulation and analysis is non-trivial


## Experiments on case study

All experiments on the case study refers to the *src/test/java/it/unifi/hierarchical/analysis* folder rather than *analysisScalability/*

Note that the pyramisRes, pyramisAnalytic and pyramisSimulation folders must be emptied.

### Ground truth (10h)
Execute *src/test/java/it/unifi/hierarchical/analysis/PyramisSampler.java*  to simulate the execution over 36000000 ms, repeatedly generating log files every 1M units of time of the model execution,

Produces the results in *src/main/resources/pyramisCaseStudyGroundTruth/*
### Analysis  (~1 hour)
Execute *src/test/java/it/unifi/hierarchical/analysis/Pyramis.java* to obtain the analysis up to LOOPS (default=5) cycles with time steps specified in the array "tics".

Produces the results in *src/main/resources/pyramis/*

#### Variable ticks
*PyramisVariableTicks.java* can be used to obtain the analysis with varying values of time steps at different levels in the model.


### Same time sampling (~15 hours)

Execute *src/test/java/it/unifi/hierarchical/analysis/PyramisSamplerRelative.java* to obtain the simulation with a given time limit (the same time of the analyses i.e. 190,487 and 2197 seconds) 20 times each to account for variation and obtain a mean.

Produces the results in *src/main/resources/pyramis/*


### Comparison (~seconds)
Chosen a given ground truth remove the others from *src/main/resources/pyramisCaseStudyGroundTruth/*

PyrStudyResults.java generates in src//main//resources//pyramisCaseStudyRes// the comparison files.

While for the same-time sampling PyrStudyResultsVarianceOld.java and PyrStudyResultsVarianceOld2.java  must be executed.

Respectively PyrStudyResultsVarianceOld give the average and variance of the states distribution values' relative errors, PyrStudyResultsVarianceOld2 gives the average and variance of the states distribution values.


## Production of images/plots

All images referring to the scalability analysis can be reproduced by executing the python scripts present in *src/main/resources/plots*