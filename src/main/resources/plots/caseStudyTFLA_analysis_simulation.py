import numpy as np
import matplotlib
import matplotlib.pyplot as plt

A222 = 0.0005230
A222_std = 0.0005060
A322 = 0.0006890
A322_std = 0.0004920
A422 = 0.0008130
A422_std = 0.0005330
A232 = 0.0005370
A232_std = 0.0003210
A332 = 0.0007330
A332_std = 0.0003430
A432 = 0.0005560
A432_std = 0.0002790
A242 = 0.0007840
A242_std = 0.0005550
A342 = 0.0008520
A342_std = 0.0004190
A442 = 0.0008160
A442_std = 0.0003350

S222 = 0.02851
S222_std = 0.02061
S322 = 0.03148
S322_std = 0.02101
S422 = 0.03030
S422_std = 0.01990
S232 = 0.03395
S232_std = 0.02216
S332 = 0.03151
S332_std = 0.02120
S432 = 0.03419
S432_std = 0.02031
S242 = 0.03330
S242_std = 0.02102
S342 = 0.03113
S342_std = 0.02105
S442 = 0.02828
S442_std = 0.01883

A223 = 0.0005590
A223_std = 0.0004160
A323 = 0.0006190
A323_std = 0.0003630
A423 = 0.0007040
A423_std = 0.0005640
A233 = 0.001258
A233_std = 0.0003800
A333 = 0.001341
A333_std = 0.0004010
A433 = 0.001147
A433_std = 0.0004290
A243 = 0.002095
A243_std = 0.0009410
A343 = 0.002008
A343_std = 0.0007070
A443 = 0.002087
A443_std = 0.0004080

S223 = 0.02081
S223_std = 0.01438
S323 = 0.01864
S323_std = 0.01230
S423 = 0.01571
S423_std = 0.01079
S233 = 0.02613
S233_std = 0.01670
S333 = 0.02188
S333_std = 0.01475
S433 = 0.01810
S433_std = 0.01210
S243 = 0.02817
S243_std = 0.01708
S343 = 0.02229
S343_std = 0.01480
S443 = 0.02083
S443_std = 0.01298

A224 = 0.001217
A224_std = 0.0004720
A324 = 0.001126
A324_std = 0.0005010
A424 = 0.001171
A424_std = 0.0008630
A234 = 0.003916
A234_std = 0.0009540
A334 = 0.003872
A334_std = 0.0007890
A434 = 0.003529
A434_std = 0.0008190
A244 = 0.008126
A244_std = 0.0008550
A344 = 0.008005
A344_std = 0.001027
A444 = 0.007518
A444_std = 0.0007750

S224 = 0.02126
S224_std = 0.01477
S324 = 0.01705
S324_std = 0.01192
S424 = 0.01571
S424_std = 0.01034
S234 = 0.02233
S234_std = 0.01488
S334 = 0.01924
S334_std = 0.01258
S434 = 0.01331
S434_std = 0.01030
S244 = 0.02828
S244_std = 0.01883
S344 = 0.02567
S344_std = 0.01435
S444 = 0.01433
S444_std = 0.01020

labels_old = ('222', '322', '422', '232', '332', '432', '242', '342', '442', '223', '323', '423', '233', '333', '433', '243', '343', '443', '224', '324', '424', '234', '334', '434', '244', '344', '444')
labels = ('222', '223', '224', '232', '233', '234', '242', '243', '244', '322', '323', '324', '332', '333', '334', '342', '343', '344', '422', '423', '424', '432', '433', '434', '442', '443', '444')

A = (A222, A322, A422, A232, A332, A432, A242, A342, A442, A223, A323, A423, A233, A333, A433, A243, A343, A443, A224, A324, A424, A234, A334, A434, A244, A344, A444)
S = (S222, S322, S422, S232, S332, S432, S242, S342, S442, S223, S323, S423, S233, S333, S433, S243, S343, S443, S224, S324, S424, S234, S334, S434, S244, S344, S444)
A_std = (A222_std, A322_std, A422_std, A232_std, A332_std, A432_std, A242_std, A342_std, A442_std, A223_std, A323_std, A423_std, A233_std, A333_std, A433_std, A243_std, A343_std, A443_std, A224_std, A324_std, A424_std, A234_std, A334_std, A434_std, A244_std, A344_std, A444_std)
S_std = (S222_std, S322_std, S422_std, S232_std, S332_std, S432_std, S242_std, S342_std, S442_std, S223_std, S323_std, S423_std, S233_std, S333_std, S433_std, S243_std, S343_std, S443_std, S224_std, S324_std, S424_std, S234_std, S334_std, S434_std, S244_std, S344_std, S444_std)

ind = np.arange(len(A))  # the x locations for the groups
width = 0.2  # the width of the bars

font = {'size' : 18}
matplotlib.rc('font', **font)
plt.rcParams["figure.figsize"] = [23,8]

fig, ax = plt.subplots()
ax.set_xticks(ind)
ax.set_xticklabels(labels, rotation = 0)
plt.ylim(1e-5, 0.25)
plt.ylabel('relative error')

rects1 = plt.bar(ind - width/2, A, width, yerr=A_std, color='#2ECC71', label='Analysis')
rects2 = plt.bar(ind + width/2, S, width, yerr=S_std, color='#F6A6A6', label='Simulation')

plt.yscale('log')
plt.legend(loc = 'upper left')
plt.tight_layout()
plt.savefig("plot_27.pdf")
