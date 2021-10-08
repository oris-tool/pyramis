import numpy as np
import matplotlib
import matplotlib.pyplot as plt


# A1, A2, A3, A4, A5, A6
VMM_Rejuvenation_Waiting = (0.0005829, 0.0001294, 0.00002236, 0.000004501, 0.000004895, 0.000004908)
VMM_Rejuvenating = (0.2946, 0.07358, 0.01832, 0.000004446, 0.00009936, 0.00009885)
VMM_Aging = (0.0004693, 0.00001625, 0.00009163, 0.0001182, 0.0001181, 0.0001189)
VM_Rejuvenation_Waiting = (0.002783, 0.0006944, 0.0001703, 0.002197, 0.0005602, 0.0001431)
VM_Aging = (0.002984, 0.0008975, 0.0003677, 0.002398, 0.0007633, 0.0003404)
VM_Stop_Waiting = (0.03418, 0.01719, 0.008752, 0.0002984, 0.0004021, 0.0004016)
VM_Rejuvenating = (0.1574, 0.03933, 0.009781, 0.1581, 0.03947, 0.009809)
VM_Failing = (0.01448, 0.01675, 0.01678, 0.01507, 0.01688, 0.01680)
VM_Restarting = (0.2709, 0.06768, 0.01683, 0.2716, 0.06783, 0.01686)

labels = ('VMM_Rej_Wait', 'VMM_Rejuvenating', 'VMM_Aging', 'VM_Rej_Wait', 'VM_Aging', 'VM_Stop_Wait', 'VM_Rejuvenating', 'VM_Failing', 'VM_Restarting')
A1 = (VMM_Rejuvenation_Waiting[0], VMM_Rejuvenating[0], VMM_Aging[0], VM_Rejuvenation_Waiting[0], VM_Aging[0], VM_Stop_Waiting[0], VM_Rejuvenating[0], VM_Failing[0], VM_Restarting[0])
A2 = (VMM_Rejuvenation_Waiting[1], VMM_Rejuvenating[1], VMM_Aging[1], VM_Rejuvenation_Waiting[1], VM_Aging[1], VM_Stop_Waiting[1], VM_Rejuvenating[1], VM_Failing[1], VM_Restarting[1])
A3 = (VMM_Rejuvenation_Waiting[2], VMM_Rejuvenating[2], VMM_Aging[2], VM_Rejuvenation_Waiting[2], VM_Aging[2], VM_Stop_Waiting[2], VM_Rejuvenating[2], VM_Failing[2], VM_Restarting[2])
A4 = (VMM_Rejuvenation_Waiting[3], VMM_Rejuvenating[3], VMM_Aging[3], VM_Rejuvenation_Waiting[3], VM_Aging[3], VM_Stop_Waiting[3], VM_Rejuvenating[3], VM_Failing[3], VM_Restarting[3])
A5 = (VMM_Rejuvenation_Waiting[4], VMM_Rejuvenating[4], VMM_Aging[4], VM_Rejuvenation_Waiting[4], VM_Aging[4], VM_Stop_Waiting[4], VM_Rejuvenating[4], VM_Failing[4], VM_Restarting[4])
A6 = (VMM_Rejuvenation_Waiting[5], VMM_Rejuvenating[5], VMM_Aging[5], VM_Rejuvenation_Waiting[5], VM_Aging[5], VM_Stop_Waiting[5], VM_Rejuvenating[5], VM_Failing[5], VM_Restarting[5])
S5_mean = (0.000084, 0.025103, 0.003982, 0.000147, 0.002063, 0.023882, 0.014985, 0.175525, 0.016713)
S6_mean = (0.000059, 0.017662, 0.003120, 0.000109, 0.001370, 0.023755, 0.011698, 0.118262, 0.012456)
S7_mean = (0.000029, 0.009588, 0.001557, 0.000058, 0.000677, 0.022608, 0.004945, 0.058331, 0.005941)
S5_stdDev = (0.000066, 0.018961, 0.002791, 0.000095, 0.001602, 0.014047, 0.010793, 0.137374, 0.012430)
S6_stdDev = (0.000048, 0.013295, 0.002168, 0.000088, 0.001124, 0.009798, 0.009114, 0.097499, 0.007787)
S7_stdDev = (0.000024, 0.007298, 0.001292, 0.000046, 0.000527, 0.004715, 0.003876, 0.045486, 0.005255)

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

rects1 = plt.bar(ind - 4*width - width/2, A1, width, color='#ABEBC6', label='A1')
rects2 = plt.bar(ind - 3*width - width/2, A2, width, color='#2ECC71', label='A2')
rects3 = plt.bar(ind - 2*width - width/2, A3, width, color='#1D8348', label='A3')
rects4 = plt.bar(ind - width - width/2, A4, width, color='#D6EAF8', label='A4')
rects5 = plt.bar(ind - width/2, A5, width, color='#BEC6FA', label='A5')
rects6 = plt.bar(ind + width/2, A6, width, color='#2874A6', label='A6')
rects7 = plt.bar(ind + width + width/2, S5_mean, width, yerr=S5_stdDev, color='#F5D6D6', label='S5')
rects8 = plt.bar(ind + 2*width + width/2, S6_mean, width, yerr=S6_stdDev, color='#F6A6A6', label='S6')
rects9 = plt.bar(ind + 3*width + width/2, S7_mean, width, yerr=S7_stdDev, color='#F67A7A', label='S7')

plt.yscale('log')
plt.legend(loc = 'upper left')
plt.tight_layout()
plt.savefig("plot_with_std.pdf")
