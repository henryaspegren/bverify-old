package org.bverify.bverify;

import edu.rice.historytree.AggregationInterface;
import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.aggs.SHA256AggB64;
import edu.rice.historytree.storage.ArrayStore;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
		AggregationInterface<byte[],byte[]> aggobj = new SHA256AggB64();
		ArrayStore<byte[], byte[]> store = new ArrayStore<byte[],byte[]>();
		
		HistoryTree<byte[],byte[]> tree = new HistoryTree<byte[], byte[]>(aggobj,store);
		tree.append("somedata".getBytes());
		tree.append("moredata".getBytes());
		tree.append("evenmoredata".getBytes());
		tree.append("xxxxxxxxx".getBytes());
        System.out.println(tree.toString());
        System.out.println(tree.agg());
    }
    
    
}
