import numpy as np
import matplotlib
import matplotlib.pyplot as plt
import pandas as pd
import sys
import pylab

cdf222_last = pd.read_csv('cdf_0.0025_p-2_d-2_s-2_last.txt', sep=' ')
cdf333_last = pd.read_csv('cdf_0.0025_p-3_d-3_s-3_last.txt', sep=' ')
cdf444_last = pd.read_csv('cdf_0.0025_p-4_d-4_s-4_last.txt', sep=' ')
cdf222_first = pd.read_csv('cdf_0.0025_p-2_d-2_s-2_first.txt', sep=' ')
cdf333_first = pd.read_csv('cdf_0.0025_p-3_d-3_s-3_first.txt', sep=' ')
cdf444_first = pd.read_csv('cdf_0.0025_p-4_d-4_s-4_first.txt', sep=' ')
cdf222_last.columns = ['x', 'y']
cdf333_last.columns = ['x', 'y']
cdf444_last.columns = ['x', 'y']
cdf222_first.columns = ['x', 'y']
cdf333_first.columns = ['x', 'y']
cdf444_first.columns = ['x', 'y']

cdf_noRej = pd.read_csv('cdf_0.0025_p-3_d-3_s-3_first.txt', sep=' ')
cdf_1Rej = pd.read_csv('cdf_cycle_0.0025_L5_p-3_d-3_s-3_first_l-1.txt', sep=' ')
cdf_2Rej = pd.read_csv('cdf_cycle_0.0025_L5_p-3_d-3_s-3_first_l-2.txt', sep=' ')
cdf_3Rej_2Cycles = pd.read_csv('cdf_cycle_0.0025_L2_p-3_d-3_s-3_first_l-3.txt', sep=' ')
cdf_3Rej_3Cycles = pd.read_csv('cdf_cycle_0.0025_L3_p-3_d-3_s-3_first_l-3.txt', sep=' ')
cdf_3Rej_4Cycles = pd.read_csv('cdf_cycle_0.0025_L4_p-3_d-3_s-3_first_l-3.txt', sep=' ')
cdf_3Rej_5Cycles = pd.read_csv('cdf_cycle_0.0025_L5_p-3_d-3_s-3_first_l-3.txt', sep=' ')
cdf_GT = pd.read_csv('cdf_sampler.txt', sep=' ')
cdf_noRej.columns = ['x', 'y']
cdf_1Rej.columns = ['x', 'y']
cdf_2Rej.columns = ['x', 'y']
cdf_3Rej_2Cycles.columns = ['x', 'y']
cdf_3Rej_3Cycles.columns = ['x', 'y']
cdf_3Rej_4Cycles.columns = ['x', 'y']
cdf_3Rej_5Cycles.columns = ['x', 'y']
cdf_GT.columns = ['x', 'y']

plt.xlabel('time (h)')
plt.xlim([0,10])
plt.ylim([0,1.03])
plt.plot(cdf222_first['x'], cdf222_first['y'], label='222-first - analysis', color='#2ECC71')
plt.plot(cdf222_last['x'], cdf222_last['y'], label='222-last - analysis', color='#2ECC71', linestyle='--')
plt.plot(cdf333_first['x'], cdf333_first['y'], label='333-first - analysis', color='#6187FF')
plt.plot(cdf333_last['x'], cdf333_last['y'], label='333-last - analysis', color='#6187FF', linestyle='--')
plt.plot(cdf444_first['x'], cdf444_first['y'], label='444-first - analysis', color='#FF0F0F')
plt.plot(cdf444_last['x'], cdf444_last['y'], label='444-last - analysis', color='#FF0F0F', linestyle='--')
plt.legend(loc = 'lower right')
plt.tight_layout()
plt.savefig("cdf.pdf") 
plt.figure()

plt.xlabel('time (h)')
plt.xlim([0,5])
plt.ylim([0,1.03])
plt.plot(cdf_noRej['x'], cdf_noRej['y'], label='333-first - analysis', color='#6187FF')
plt.plot(cdf_1Rej['x'], cdf_1Rej['y'], label='333-first-1-rej - analysis with $M_2=5$', color='#00FD13')
plt.plot(cdf_2Rej['x'], cdf_2Rej['y'], label='333-first-2-rej - analysis with $M_2=5$', color='#FCC772')
plt.plot(cdf_3Rej_2Cycles['x'], cdf_3Rej_2Cycles['y'], label='333-first-3-rej - analysis with $M_2=2$', color='#FD29F3', linestyle='--')
plt.plot(cdf_3Rej_3Cycles['x'], cdf_3Rej_3Cycles['y'], label='333-first-3-rej - analysis with $M_2=3$', color='#FD29F3', linestyle='-.')
plt.plot(cdf_3Rej_4Cycles['x'], cdf_3Rej_4Cycles['y'], label='333-first-3-rej - analysis with $M_2=4$', color='#FD29F3', linestyle=':')
plt.plot(cdf_3Rej_5Cycles['x'], cdf_3Rej_5Cycles['y'], label='333-first-3-rej - analysis with $M_2=5$', color='#FD29F3')
plt.plot(cdf_GT['x'], cdf_GT['y'], label='333-first-3-rej - ground truth', color='black')
plt.legend(loc = 'lower right')
plt.tight_layout()
plt.savefig("cdfRej.pdf")


