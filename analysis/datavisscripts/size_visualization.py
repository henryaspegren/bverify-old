import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import os 


##############################
### Record Aggregation Size
#############################
def plotRecordAggregationSize():
	filename = os.path.join(os.path.dirname(os.getcwd()), "benchmarking/record_aggregation_size.csv")
	res = pd.read_csv(filename)

	filtered = res[res['NumberNumerical'] == res['NumberCategorical']]
	x = filtered['NumberNumerical']
	y = filtered['Size']

	fig, ax = plt.subplots()
	# scales linearly so add a line of fit for comparison
	fit = np.polyfit(x, y, deg=1)
	ax.plot(x, fit[0] * x + fit[1], color='red')

	ax.scatter(x, y, color='blue')
	ax.set_title('Record Aggregation Size')
	ax.set_xlabel('Number of Attributes In Record')
	ax.set_ylabel('Size of Record Aggregation (in bytes)')	
	plt.show()


##############################
### Record History Tree Size
##############################

def plotHistoryTreeSize():
	filename = os.path.join(os.path.dirname(os.getcwd()), "benchmarking/full_history_tree_size.csv")
	filename_pruned = os.path.join(os.path.dirname(os.getcwd()), "benchmarking/pruned_history_tree_size.csv")
	
	res = pd.read_csv(filename)
	pruned_res = pd.read_csv(filename_pruned)
	
	
	x2 = res[res['NumberAttributes'] == 2]['NumberRecords']
	y2 = res[res['NumberAttributes'] == 2]['Size']
	
	x2pruned = pruned_res[pruned_res['NumberAttributes'] == 2]['NumberRecords']
	y2pruned = pruned_res[pruned_res['NumberAttributes'] == 2]['Size']
	
	
	x128 = res[res['NumberAttributes'] == 128]['NumberRecords']
	y128 = res[res['NumberAttributes'] == 128]['Size']
	
	x128pruned = pruned_res[pruned_res['NumberAttributes'] == 128]['NumberRecords']
	y128pruned = pruned_res[pruned_res['NumberAttributes'] == 128]['Size']
	
	
	x512 = res[res['NumberAttributes'] == 512]['NumberRecords']
	y512 = res[res['NumberAttributes'] == 512]['Size']
	
	x512pruned = pruned_res[pruned_res['NumberAttributes'] == 512]['NumberRecords']
	y512pruned = pruned_res[pruned_res['NumberAttributes'] == 512]['Size']
	
	fig, ax = plt.subplots()
	
	
	ax.scatter(np.log(x2), np.log(y2), color='red', marker='o', label='2 Attributes, Full Tree')
	ax.scatter(np.log(x2pruned), np.log(y2pruned), color='red', marker='^', label='2 Attributes, Merkle Path')
	fit2 = np.polyfit(x2, y2, deg=1)
	ax.plot(np.log(x2), np.log(fit2[0] *x2 + fit2[1]), color='red', label='_nolegend_', linestyle='--')
	fit2pruned = np.polyfit(np.log(x2pruned), y2pruned, deg=1)
	ax.plot(np.log(x2pruned), np.log(fit2pruned[0] * np.log(x2pruned) + fit2pruned[1]), label='_nolegend_', color='red')
	
	
	
	ax.scatter(np.log(x128), np.log(y128), color='blue', marker='o', label='128 Attributes, Full Tree')
	ax.scatter(np.log(x128pruned), np.log(y128pruned), color='blue', marker='^', label='128 Attributes, Merkle Path')
	fit128 = np.polyfit(x128, y128, deg=1)
	ax.plot(np.log(x128), np.log(fit128[0] *x128 + fit128[1]), color='blue', linestyle='--', label='_nolegend_')
	fit128pruned = np.polyfit(np.log(x128pruned), y128pruned, deg=1)
	ax.plot(np.log(x128pruned), np.log(fit128pruned[0] * np.log(x128pruned) + fit128pruned[1]), color='blue', label='_nolegend_')
	
	
	ax.scatter(np.log(x512), np.log(y512), color='green', marker='o', label='512 Attributes, Full Tree')
	ax.scatter(np.log(x512pruned), np.log(y512pruned), color='green', marker='^', label='512 Attributes, Merkle Path')
	fit512 = np.polyfit(x512, y512, deg = 1)
	ax.plot(np.log(x512), np.log(fit512[0]*x512 + fit512[1]), color="green", linestyle='--', label='_nolegend_')	
	fit512pruned = np.polyfit(np.log(x512pruned), y512pruned, deg=1)
	ax.plot(np.log(x512pruned), np.log(fit512pruned[0] * np.log(x512pruned) + fit512pruned[1]), color='green', label='_nolegend_')
	
		
	ax.set_title("History Tree Size")
	ax.set_xlabel("Log of Number of Records")
	ax.set_ylabel("Log of Size (in bytes)")
	
	plt.legend(loc='upper left')
	
	plt.show()
	
################################
### Categorical Query Proof Size 
################################

filename = os.path.join(os.path.dirname(os.getcwd()), "benchmarking/query_proof_unsorted_size.csv")
res = pd.read_csv(filename)

print(res)

fig, ax = plt.subplots()

ax.scatter(res['NumberOfRecordsMatching'], res['ProofSize'], color='red', marker='^')
ax.scatter(res['NumberOfRecordsMatching'], res['SizeOfRecordsMatching'], color='blue', marker='o')
ax.axhline(y=res['SizeOfRecordsAll'][0],c="black",linewidth=0.5, linestyle='--')
ax.axhline(y=res['SizeOfProofAll'][0],c="black",linewidth=0.5)

plt.show()




