import numpy as np
import matplotlib
import matplotlib.pyplot as plt

# L2, L3, L4
D2_P2 = (2,4,8)
D2_P3 = (3,6,12)
D2_P4 = (4,8,16)
D3_P2 = (41,74,144)
D3_P3 = (51,119,230)
D3_P4 = (74,169,323)
D4_P2 = (96,220,422)
D4_P3 = (202,466,900)
D4_P4 = (362,826,1604)

L2 = (D2_P2[0], D2_P3[0], D2_P4[0], D3_P2[0], D3_P3[0], D3_P4[0], D4_P2[0], D4_P3[0], D4_P4[0])
L3 = (D2_P2[1], D2_P3[1], D2_P4[1], D3_P2[1], D3_P3[1], D3_P4[1], D4_P2[1], D4_P3[1], D4_P4[1])
L4 = (D2_P2[2], D2_P3[2], D2_P4[2], D3_P2[2], D3_P3[2], D3_P4[2], D4_P2[2], D4_P3[2], D4_P4[2])

labels = ('D=2, P=2', 'D=2, P=3', 'D=2, P=4', 'D=3, P=2', 'D=3, P=3', 'D=3, P=4', 'D=4, P=2', 'D=4, P=3', 'D=4, P=4')

ind = np.arange(len(L2))  # the x locations for the groups
width = 0.08  # the width of the bars

font = {'size' : 16}
matplotlib.rc('font', **font)
plt.rcParams["figure.figsize"] = [20,8]

fig, ax = plt.subplots()
ax.set_xticks(ind)
ax.set_xticklabels(labels, rotation = 0)
plt.ylim(1, 1650)
plt.ylabel('computation time (s)')

rects1 = plt.bar(ind - width, L2, width, color='#DBB6F5', label='L=2')
rects2 = plt.bar(ind, L3, width, color='#C784F7', label='L=3')
rects3 = plt.bar(ind + width, L4, width, color='#9A11F5', label='L=4')

plt.yscale('log')
plt.legend(loc = 'upper left')
plt.tight_layout()
plt.savefig("caseStudyTFLA_computationTimes.pdf")
