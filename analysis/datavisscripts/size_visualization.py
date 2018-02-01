import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import os 


##############################
### Record Aggregation and Record Size
#############################
def plotRecordAndRecordAggregationSize():
	filename = os.path.join(os.path.dirname(os.getcwd()), "benchmarking/record_and_record_aggregation_size.csv")
	res = pd.read_csv(filename)
	
	# filter 
	filtered = res[res['NumberNumerical'] == res['NumberCategorical']]
	x_record_aggregation = filtered['NumberNumerical']
	y_record_aggregation = filtered['RecordAggregationSize']
	x_record = filtered['NumberNumerical']
	y_record = filtered['RecordSize']
	
	fig, ax = plt.subplots()
	
	# scales linearly so add a line of fit for comparison
	fit_record_aggregation = np.polyfit(x_record_aggregation, y_record_aggregation, deg=1)
	ax.plot(x_record_aggregation, fit_record_aggregation[0] * x_record_aggregation + fit_record_aggregation[1], color='red', label='_nolegend_')
	
	fit_record = np.polyfit(x_record, y_record, deg=1)
	ax.plot(x_record, fit_record[0] * x_record + fit_record[1], color='blue', label='_nolegend_')	

	ax.scatter(x_record_aggregation, y_record_aggregation, color='red', label='Record Aggregation')
	ax.scatter(x_record, y_record, color='blue', label='Record')
	ax.set_title('Size')
	ax.set_xlabel('Number of Attributes In Record')
	ax.set_ylabel('Size of Record Aggregation (in bytes)')	
	plt.legend(loc='upper left')
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
		
	x16 = res[res['NumberAttributes'] == 16]['NumberRecords']
	y16 = res[res['NumberAttributes'] == 16]['Size']
	
	x16pruned = pruned_res[pruned_res['NumberAttributes'] == 16]['NumberRecords']
	y16pruned = pruned_res[pruned_res['NumberAttributes'] == 16]['Size']
		
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
	
	
	ax.scatter(np.log(x16), np.log(y16), color='green', marker='o', label='16 Attributes, Full Tree')
	ax.scatter(np.log(x16pruned), np.log(y16pruned), color='green', marker='^', label='16 Attributes, Merkle Path')
	fit16= np.polyfit(x16, y16, deg = 1)
	ax.plot(np.log(x16), np.log(fit16[0]*x16 + fit16[1]), color="green", linestyle='--', label='_nolegend_')	
	fit16pruned = np.polyfit(np.log(x16pruned), y16pruned, deg=1)
	ax.plot(np.log(x16pruned), np.log(fit16pruned[0] * np.log(x16pruned) + fit16pruned[1]), color='green', label='_nolegend_')
	
	plt.xlim(xmin=0)
	plt.ylim(ymin=0)
	ax.set_title("History Tree Size")
	ax.set_xlabel("Log of Number of Records")
	ax.set_ylabel("Log of Size (in bytes)")
	
	plt.legend(loc='upper left')
	
	plt.show()
	
################################
### Categorical Query Proof Size 
################################


def plotCategoricalQueryProofSize():
	filename = os.path.join(os.path.dirname(os.getcwd()), "benchmarking/query_proof_size.csv")
	res = pd.read_csv(filename)
		
	fig, ax = plt.subplots()
	
	colors = ['red', 'purple', 'green', 'yellow', 'orange']
	ws = np.unique(res['w'])
	
	for i in range(len(ws)):
		w = ws[i]
		color = colors[i]
		filtered = res[res['w'] == w]
		ax.scatter(filtered['NumberOfRecordsMatching'], filtered['ProofSize'], color=color, marker='^', label=str(w/max(ws) * 100)+ '% sorted')
	
	ax.scatter(filtered['NumberOfRecordsMatching'], filtered['SizeOfRecordsMatching'], color='blue', marker='o', label = "Size Of Matching Records")	
	ax.axhline(y=max(res['SizeOfRecordsAll']),c="black",linewidth=0.5, linestyle='--', label = "Size of All Records Raw Serialization")
	ax.axhline(y=max(res['SizeOfProofAll']),c="black",linewidth=0.5, label = "Size of Tree Serialization With All Records")
	
	number_of_numerical_attributes = res['NumberNumericalAttributes'][0]
	number_of_categorical_attributes = res['NumberCategoricalAttributes'][0]
	plt.title("Cateogrical Query Proof Size Benchmark")
	plt.suptitle("Number of Numerical Attributes: "+str(number_of_numerical_attributes)+
				"| Number of Categorical Attributes: "+str(number_of_categorical_attributes))
	ax.set_xlabel("Number Of Records Matching Query")
	ax.set_ylabel("Size of Proof (bytes)")
	
	plt.legend(loc='upper left')
	plt.show()


""""
Uncomment to run
"""
#plotRecordAndRecordAggregationSize()
plotHistoryTreeSize()
#plotCategoricalQueryProofSize()


