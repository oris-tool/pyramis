import numpy as np
import matplotlib
import matplotlib.pyplot as plt

# S1, S2, S3
VMM_Rejuvenation_Waiting = (0.000003714, 0.0000005212, 0.00000007659)
VM_Rejuvenation_Waiting = (0.000004226, 0.000002459, 0.0000001037)
VM_Aging = (0.00008966, 0.00002494, 0.000002587)
VM_Stop_Waiting = (0.0005465, 0.00004890, 0.00001538)
VM_Rejuvenating = (0.00007328, 0.00006938, 0.00002414)
VMM_Aging = (0.0004021, 0.0001435, 0.00003051)
VM_Restarting = (0.0002752, 0.0007257, 0.0001690)
VMM_Rejuvenating = (0.001817, 0.001072, 0.0001914)
VM_Failing = (0.008065, 0.002355, 0.0002311)

S1 = (VMM_Rejuvenation_Waiting[0], VM_Rejuvenation_Waiting[0], VM_Aging[0], VM_Stop_Waiting[0], VM_Rejuvenating[0], VMM_Aging[0], VM_Restarting[0], VMM_Rejuvenating[0], VM_Failing[0])
S2 = (VMM_Rejuvenation_Waiting[1], VM_Rejuvenation_Waiting[1], VM_Aging[1], VM_Stop_Waiting[1], VM_Rejuvenating[1], VMM_Aging[1], VM_Restarting[1], VMM_Rejuvenating[1], VM_Failing[1])
S3 = (VMM_Rejuvenation_Waiting[2], VM_Rejuvenation_Waiting[2], VM_Aging[2], VM_Stop_Waiting[2], VM_Rejuvenating[2], VMM_Aging[2], VM_Restarting[2], VMM_Rejuvenating[2], VM_Failing[2])

labels = ('VMM_Rej_Wait', 'VM_Rej_Wait', 'VM_Aging', 'VM_Stop_Wait', 'VM_Rejuvenating', 'VMM_Aging', 'VM_Restarting', 'VMM_Rejuvenating', 'VM_Failing')

ind = np.arange(len(S1))  # the x locations for the groups
width = 0.08  # the width of the bars

font = {'size' : 16}
matplotlib.rc('font', **font)
plt.rcParams["figure.figsize"] = [20,8]

fig, ax = plt.subplots()
ax.set_xticks(ind)
ax.set_xticklabels(labels, rotation = 0)
plt.ylim(1e-8, 1e-2)
plt.ylabel('relative error')

rects1 = plt.bar(ind - width, S1, width, color='#FAD7A0', label='S1')
rects2 = plt.bar(ind, S2, width, color='#F0B27A', label='S2')
rects3 = plt.bar(ind + width, S3, width, color='#CA6F1E', label='S3')

plt.yscale('log')
plt.legend(loc = 'upper left')
plt.tight_layout()
plt.savefig("plot.pdf")
