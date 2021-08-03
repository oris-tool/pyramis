
This is the implementation used for experimentations in the paper "Compositional Analysis of
Hierarchical UML Statecharts" by Laura Carnevali, Reinhard German, Francesco Santoni, and Enrico Vicario

The experiments executable files are all contained in the "src/test/java/it/unifi/hierarchical/" folder.

Respectively for the scalability analysis they are contained under the "analysisScalability" subfolder:

~Behaviour length, parallelism degree, and hierarchy depth. & Composite type.

results displayed in Tables 1,2,3 can be reproduced by the methods in Pyramis.java for the analysis, PyramisSampler.java's for the sampling of comparable times (automatically called during a call to Pyramis) and PyramisSamplerFromFiles.java for the sampling of the ground truth.

In the latter case, first files containing sufficient expolinomial samples have to be created (not present in the repository for the sake of space) through PyramisCreateSampFiles.java

The code in the current iteration is already runnable but will take samples from a single file, left in the src\main\resources\samplesExpolinomial and src\main\resources\samplesExpolinomial10 folders and will use the samples for each state without discrimination: this can lead to  inaccuracies for the low number of samples. 

In all cases even the sampling requires the generation of the corresponding HSMP model (not its analysis) to link the names of the different states.
Such models are present in src/test/java/model/example/hsmp_scalability


~Rare events

For the reproduction of the rare events case the corresponding "Rare" version of the classes should be used.

~Large-support PDFs.
For the reproduction of the large-support case the corresponding "LongEvents" version of the classes should be used.

~Cycles

For the reproduction of the cycles case the corresponding "Cycles" version of the classes should be used.


~Regenerative transient analysis

to produce regenerative transient analysis of the model via Petri Nets PyramisXPNs.java main is used

////
PyrStudy---- files are used to analyse and compare the results: in particular PyrStudyVarianceCycles creates a mapping of the corresponding states for sampling with cycles and without
////


For the case study analysis instead files are contained in the "analysis" subfolder with the corresponding models in the model/example/hsmp folder:


PyramisSampler can be used to produce a simulation as long as needed (S1,S2,S3,S4), while PyramisSamplerRelative.java can be used to repeat simulations with a given time length (S5,S6,S7)

Pyramis.java can be used to produce the analysis for A1,A2 and A3 while PyramisVariableTicks can produce A4,A5,and A6, in particular the latter can be used to study the system with any kind of mix of time ticks (still following the given rules of being dividends/multiple of the parents/childrens)



