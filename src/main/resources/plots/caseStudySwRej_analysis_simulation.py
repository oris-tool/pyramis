import numpy as np
import matplotlib
import matplotlib.pyplot as plt


# A1, A2, A3, A4, A5, A6, S5, S6, S7
VMM_Rejuvenation_Waiting = (0.0005829, 0.0001294, 0.00002236, 0.000004501, 0.000004895, 0.000004908, 0.0001091, 0.00002282, 0.00004097)
VMM_Rejuvenating = (0.2946, 0.07358, 0.01832, 0.000004446, 0.00009936, 0.00009885, 0.01657, 0.02876, 0.01810)
VMM_Aging = (0.0004693, 0.00001625, 0.00009163, 0.0001182, 0.0001181, 0.0001189, 0.003049, 0.001732, 0.001415)
VM_Rejuvenation_Waiting = (0.002783, 0.0006944, 0.0001703, 0.002197, 0.0005602, 0.0001431, 0.0001096, 0.0001425, 0.00004421)
VM_Aging = (0.002984, 0.0008975, 0.0003677, 0.002398, 0.0007633, 0.0003404, 0.001848, 0.0007230, 0.0002045)
VM_Stop_Waiting = (0.03418, 0.01719, 0.008752, 0.0002984, 0.0004021, 0.0004016, 0.02001, 0.01780, 0.01820)
VM_Rejuvenating = (0.1574, 0.03933, 0.009781, 0.1581, 0.03947, 0.009809, 0.003162, 0.003309, 0.01088)
VM_Failing = (0.01448, 0.01675, 0.01678, 0.01507, 0.01688, 0.01680, 0.1492, 0.04970, 0.01371)
VM_Restarting = (0.2709, 0.06768, 0.01683, 0.2716, 0.06783, 0.01686, 0.009226, 0.02328, 0.001829)

labels = ('VMM_Rej_Wait', 'VMM_Rejuvenating', 'VMM_Aging', 'VM_Rej_Wait', 'VM_Aging', 'VM_Stop_Wait', 'VM_Rejuvenating', 'VM_Failing', 'VM_Restarting')
A1 = (VMM_Rejuvenation_Waiting[0], VMM_Rejuvenating[0], VMM_Aging[0], VM_Rejuvenation_Waiting[0], VM_Aging[0], VM_Stop_Waiting[0], VM_Rejuvenating[0], VM_Failing[0], VM_Restarting[0])
A2 = (VMM_Rejuvenation_Waiting[1], VMM_Rejuvenating[1], VMM_Aging[1], VM_Rejuvenation_Waiting[1], VM_Aging[1], VM_Stop_Waiting[1], VM_Rejuvenating[1], VM_Failing[1], VM_Restarting[1])
A3 = (VMM_Rejuvenation_Waiting[2], VMM_Rejuvenating[2], VMM_Aging[2], VM_Rejuvenation_Waiting[2], VM_Aging[2], VM_Stop_Waiting[2], VM_Rejuvenating[2], VM_Failing[2], VM_Restarting[2])
A4 = (VMM_Rejuvenation_Waiting[3], VMM_Rejuvenating[3], VMM_Aging[3], VM_Rejuvenation_Waiting[3], VM_Aging[3], VM_Stop_Waiting[3], VM_Rejuvenating[3], VM_Failing[3], VM_Restarting[3])
A5 = (VMM_Rejuvenation_Waiting[4], VMM_Rejuvenating[4], VMM_Aging[4], VM_Rejuvenation_Waiting[4], VM_Aging[4], VM_Stop_Waiting[4], VM_Rejuvenating[4], VM_Failing[4], VM_Restarting[4])
A6 = (VMM_Rejuvenation_Waiting[5], VMM_Rejuvenating[5], VMM_Aging[5], VM_Rejuvenation_Waiting[5], VM_Aging[5], VM_Stop_Waiting[5], VM_Rejuvenating[5], VM_Failing[5], VM_Restarting[5])
S5 = (VMM_Rejuvenation_Waiting[6], VMM_Rejuvenating[6], VMM_Aging[6], VM_Rejuvenation_Waiting[6], VM_Aging[6], VM_Stop_Waiting[6], VM_Rejuvenating[6], VM_Failing[6], VM_Restarting[6])
S6 = (VMM_Rejuvenation_Waiting[7], VMM_Rejuvenating[7], VMM_Aging[7], VM_Rejuvenation_Waiting[7], VM_Aging[7], VM_Stop_Waiting[7], VM_Rejuvenating[7], VM_Failing[7], VM_Restarting[7])
S7 = (VMM_Rejuvenation_Waiting[8], VMM_Rejuvenating[8], VMM_Aging[8], VM_Rejuvenation_Waiting[8], VM_Aging[8], VM_Stop_Waiting[8], VM_Rejuvenating[8], VM_Failing[8], VM_Restarting[8])

ind = np.arange(len(A1))  # the x locations for the groups
width = 0.08  # the width of the bars

font = {'size' : 18}
matplotlib.rc('font', **font)
plt.rcParams["figure.figsize"] = [23,8]

fig, ax = plt.subplots()
ax.set_xticks(ind)
ax.set_xticklabels(labels, rotation = 0)
plt.ylim(1e-6, 1)
plt.ylabel('relative error')

rects1 = plt.bar(ind - 4*width - width/2, A1, width, color='#D6EAF8', label='A1')
rects2 = plt.bar(ind - 3*width - width/2, A2, width, color='#BEC6FA', label='A2')
rects3 = plt.bar(ind - 2*width - width/2, A3, width, color='#2874A6', label='A3')
rects4 = plt.bar(ind - width - width/2, A4, width, color='#ABEBC6', label='A4')
rects5 = plt.bar(ind - width/2, A5, width, color='#2ECC71', label='A5')
rects6 = plt.bar(ind + width/2, A6, width, color='#1D8348', label='A6')
rects7 = plt.bar(ind + width + width/2, S5, width, color='#EAA7A1', label='S5')
rects8 = plt.bar(ind + 2*width + width/2, S6, width, color='#BF7079', label='S6')
rects9 = plt.bar(ind + 3*width + width/2, S7, width, color='#B03A2E', label='S7')


plt.yscale('log')
plt.legend(loc = 'upper left')
plt.tight_layout()
plt.savefig("plot.pdf")
