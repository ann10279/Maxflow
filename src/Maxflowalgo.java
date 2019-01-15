import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
 
public class Maxflowalgo
{
	public final static int NODE_NUM = 130;
        private int[] parent;
        private Queue<Integer> queue;
        private int numberOfVertices;
        private boolean[] visited;
        private static Set<Pair> cutSet;
        private ArrayList<Integer> reachable;
        private ArrayList<Integer> unreachable;
	public final static int WORKER_NUM = 130; //1 server and 3 clients
	//client 
	public final static int[] w1 = {10,8,4}; //w_x of host x  //網路成本 $1/MB 
	public final static int[] k1 = {2,1,1};  //k_x of host x  //計算成本 $1/second
	public final static int[] w2 = {26,8,4}; 
	public final static int[] k2 = {1,1,1}; 
	public final static int[] w3 = {50,8,4}; 
	public final static int[] k3 = {1,2,2};
	//server
	public final static int[] w4 = {12,8,4}; 
	public final static int[] k4 = {1,1,2};
	
	    //client1
		//w ; n_i of node i
		public static int[] n1 = {//檔案大小
				1,2,3,4,5,6,7,8,
				1,2,3,4,5,6,7,8} ;
		//k ; c_i of node i
		public static int[] c1 = { //工作時間
				1,2,3,4,5,6,7,8,
				1,2,3,4,5,6,7,8};	
		
		//client2
		public static int[] n2 = {
				1,6,2,6,3,6,4,6,
				6,1,6,2,6,3,6,4} ;
		
		public static int[] c2 = {
				6,1,6,1,6,1,6,1,
				6,7,9,9,8,9,2,9} ;
			
		//client3
		public static int[] n3 = {
				1,2,3,4,5,6,7,8,
				1,2,3,4,5,6,7,8} ;

		public static int[] c3 = {
				1,2,3,4,5,6,7,8,
				1,2,3,4,5,6,7,8} ;

		//server
		public static int[] n4 = {
				1,2,3,4,5,6,7,8,
				9,8,7,6,5,4,7,1} ;
		
		public static int[] c4 = {
				1,2,3,4,5,6,7,8,
				9,2,8,4,5,6,5,1} ;
	
    public Maxflowalgo (int numberOfVertices)
    {
        this.numberOfVertices = numberOfVertices;
        this.queue = new LinkedList<Integer>();
        parent = new int[numberOfVertices + 1];
        visited = new boolean[numberOfVertices + 1];
        cutSet = new HashSet<Pair>();
        reachable = new ArrayList<Integer>();
        unreachable = new ArrayList<Integer>();
    }
 
    public boolean bfs (int source, int goal, int graph[][])
    {
        boolean pathFound = false;
        int destination, element;
        for (int vertex = 1; vertex <= numberOfVertices; vertex++)
        {
            parent[vertex] = -1;
            visited[vertex] = false;
        }
        queue.add(source);
        parent[source] = -1;
        visited[source] = true;
 
        while (!queue.isEmpty())
        {
            element = queue.remove();
            destination = 1;
            while (destination <= numberOfVertices)
            {
                if (graph[element][destination] > 0 &&  !visited[destination])
                {
                    parent[destination] = element;
                    queue.add(destination);
                    visited[destination] = true;
                }
                destination++;
            }
        }
 
        if (visited[goal])
        {
            pathFound = true;
        }
        return pathFound;
    }
 
    public int  maxFlowMinCut (int graph[][], int source, int destination)
    {
        int u, v;
        int maxFlow = 0;
        int pathFlow;
        int[][] residualGraph = new int[numberOfVertices + 1][numberOfVertices + 1];
 
        for (int sourceVertex = 1; sourceVertex <= numberOfVertices; sourceVertex++)
        {
            for (int destinationVertex = 1; destinationVertex <= numberOfVertices; destinationVertex++)
            {
                residualGraph[sourceVertex][destinationVertex] = graph[sourceVertex][destinationVertex];
            }
        }
 
        /*max flow*/
        while (bfs(source, destination, residualGraph))
        {
            pathFlow = Integer.MAX_VALUE;
            for (v = destination; v != source; v = parent[v])
            {
                u = parent[v];
                pathFlow = Math.min(pathFlow,residualGraph[u][v]);
            }
            for (v = destination; v != source; v = parent[v])
            {
                u = parent[v];
                residualGraph[u][v] -= pathFlow;
                residualGraph[v][u] += pathFlow;
            }
            maxFlow += pathFlow;	
        }
 
        /*calculate the cut set*/		
        for (int vertex = 1; vertex <= numberOfVertices; vertex++)
        {
            if (bfs(source, vertex, residualGraph))
            {
                reachable.add(vertex);
            }
            else
            {
                unreachable.add(vertex);
            }
        }
        for (int i = 0; i < reachable.size(); i++)
        {
            for (int j = 0; j < unreachable.size(); j++)
            {
                if (graph[reachable.get(i)][unreachable.get(j)] > 0)
                {
                    cutSet.add(new Pair(reachable.get(i), unreachable.get(j)));
                }
            }
        }
        return maxFlow;
    }
 
    public static void printCutSet ()
    {
        Iterator<Pair> iterator = cutSet.iterator();
        while (iterator.hasNext())
        {
            Pair pair = iterator.next();
            System.out.println(pair.source + "-" + pair.destination);
        }
    }
    
    public static void printGraphviz (int[][] graph, int host[][])
    {       
    	System.out.println("digraph ethane {");
        System.out.println("newrank=true; ");
        System.out.println("ranksep = 0.2;");
        System.out.println("nodesep = 0.15;");        
    	//0,1,2,3,4 (4=host-to-host)
    	for (int hid = 0; hid<5; hid++) {

        	if(hid <= 3) // cluster 0,1,2,3
        		System.out.println("subgraph cluster_host"+ hid + "{");
    		
	        for (int i = 1; i <= graph.length-1; i++)
	        {
	            for (int j = 1; j <= graph[i].length-1; j++)
	            {
	            	if(host[i][j] != hid) // only print edges of host=hid
	            		continue;
	         

	                if (graph[i][j] > 0)
	                {
	                	//is a cut?
	                    Iterator<Pair> iterator = cutSet.iterator();
	                    boolean isCut=false;
	                    while (iterator.hasNext())
	                    {
	                        Pair pair = iterator.next();
	                        if (i==pair.source && 
	                        	j==pair.destination) {
	                        	isCut = true;
	                        	break;
	                        }
	                    }
	                    
	                    if(isCut){
	                    	if(j==i+16){
	                    		System.out.println("subgraph cluster"+i+"{");
	                        	System.out.println("" + i + "->" + j + " [label=\"" + graph[i][j] + "\" color=\"red\"]");
	                        	System.out.println("}");
	                    	}else
	                    	System.out.println("" + i + "->" + j + " [label=\"" + graph[i][j] + "\" color=\"red\"]");
	                    }
	                	else{
	                		if(j==i+16){
	                			System.out.println("subgraph cluster"+i+"{");
	                    		System.out.println("" + i + "->" + j + " [label=\"" + graph[i][j] + "\"]"); 
	                    		System.out.println("}");	
	                		}else
	                		System.out.println("" + i + "->" + j + " [label=\"" + graph[i][j] + "\"]");  
	                	}
	                    
	                }
	            }
	        }
	        
        	if(hid <= 3) // cluster 0,1,2,3
        		System.out.println("}");
        	
    	} //for host
    	
    }
       
    public static void main (String...arg)
    {
        int[][] graph;
        int[][] host;
        int numberOfNodes;
        int source;
        int sink;
        int maxFlow;
        int ct2=32;
        int ct3=64;
        int sr1=96;
  
        numberOfNodes = NODE_NUM; //scanner.nextInt();
        graph = new int[numberOfNodes + 1][numberOfNodes + 1];
        host = new int[numberOfNodes + 1][numberOfNodes + 1];
 
        for(int i=0; i<host.length; i++)
        	for(int j=0; j<host[i].length;j++)
        		host[i][j] = 4;
       
        //start  
        graph[1][2]     = Integer.MAX_VALUE;
        graph[1][2+ct2] = Integer.MAX_VALUE;
        graph[1][2+ct3] = Integer.MAX_VALUE;
        graph[1][2+sr1] = Integer.MAX_VALUE;
        
        //host=0,1,2,3
        //client 1
        int test1=1;//w1 //k1
        graph[2][18]  = w1[test1]*n1[0];
        graph[18][3]  = Integer.MAX_VALUE;
        graph[18][4]  = Integer.MAX_VALUE;
        graph[3][19]  = w1[test1]*n1[1];    
        graph[4][20]  = w1[test1]*n1[2];
        graph[19][5]  = Integer.MAX_VALUE;
        graph[19][6]  = Integer.MAX_VALUE;
        graph[20][6]  = Integer.MAX_VALUE;
        graph[5][21]  = w1[test1]*n1[3];
        graph[6][22]  = w1[test1]*n1[4];
        graph[21][7]  = Integer.MAX_VALUE;
        graph[22][7]  = Integer.MAX_VALUE;
        graph[22][8]  = Integer.MAX_VALUE;
        graph[7][23]  = w1[test1]*n1[5];
        graph[8][24]  = w1[test1]*n1[6];
        graph[23][9]  = Integer.MAX_VALUE;
        graph[23][8]  = Integer.MAX_VALUE;
        graph[24][9]  = Integer.MAX_VALUE;
        graph[9][25]  = w1[test1]*n1[7];
        graph[10][26] = w1[test1]*n1[8];
        graph[25][11] = Integer.MAX_VALUE;
        graph[25][10] = Integer.MAX_VALUE;
        graph[26][11] = Integer.MAX_VALUE;
        graph[26][12] = Integer.MAX_VALUE;
        graph[11][27] = w1[test1]*n1[9];
        graph[12][28] = w1[test1]*n1[10];
        graph[27][13] = Integer.MAX_VALUE;
        graph[28][14] = Integer.MAX_VALUE;
        graph[13][29] = w1[test1]*n1[11];
        graph[14][30] = w1[test1]*n1[12];
        graph[29][15] = Integer.MAX_VALUE;
        graph[29][16] = Integer.MAX_VALUE;
        graph[30][16] = Integer.MAX_VALUE;
        graph[15][31] = w1[test1]*n1[13];
        graph[16][32] = w1[test1]*n1[14];
        graph[31][17] = Integer.MAX_VALUE;
        graph[32][17] = Integer.MAX_VALUE;
        graph[17][33] = w1[test1]*n1[15];
        graph[2][3]   = k1[test1]*c1[0];
        graph[2][4]   = k1[test1]*c1[1];
        graph[2][5]   = k1[test1]*c1[2];
        graph[2][6]   = k1[test1]*c1[3];
        graph[2][7]   = k1[test1]*c1[4];
        graph[2][8]   = k1[test1]*c1[5];
        graph[2][9]   = k1[test1]*c1[6];
        graph[2][10]  = k1[test1]*c1[7];
        graph[2][11]  = k1[test1]*c1[8];
        graph[2][12]  = k1[test1]*c1[9];
        graph[2][13]  = k1[test1]*c1[10];
        graph[2][14]  = k1[test1]*c1[11];
        graph[2][15]  = k1[test1]*c1[12];
        graph[2][16]  = k1[test1]*c1[13];
        graph[2][17]  = k1[test1]*c1[14];
        
        host[2][18]  = 1;
        host[18][3]  = 1;
        host[18][4]  = 1;
        host[3][19]  = 1;   
        host[4][20]  = 1;
        host[19][5]  = 1;
        host[19][6]  = 1;
        host[20][6]  = 1;
        host[5][21]  = 1;
        host[6][22]  = 1;
        host[21][7]  = 1;
        host[22][7]  = 1;
        host[22][8]  = 1;
        host[7][23]  = 1;
        host[8][24]  = 1;
        host[23][9]  = 1;
        host[23][8]  = 1;
        host[24][9]  = 1;
        host[9][25]  = 1;
        host[10][26] = 1;
        host[25][11] = 1;
        host[25][10] = 1;
        host[26][11] = 1;
        host[26][12] = 1;
        host[11][27] = 1;
        host[12][28] = 1;
        host[27][13] = 1;
        host[28][14] = 1;
        host[13][29] = 1;
        host[14][30] = 1;
        host[29][15] = 1;
        host[29][16] = 1;
        host[30][16] = 1;
        host[15][31] = 1;
        host[16][32] = 1;
        host[31][17] = 1;
        host[32][17] = 1;
        host[17][33] = 1;
        host[2][3]   = 1;
        host[2][4]   = 1;
        host[2][5]   = 1;
        host[2][6]   = 1;
        host[2][7]   = 1;
        host[2][8]   = 1;
        host[2][9]   = 1;
        host[2][10]  = 1;
        host[2][11]  = 1;
        host[2][12]  = 1;
        host[2][13]  = 1;
        host[2][14]  = 1;
        host[2][15]  = 1;
        host[2][16]  = 1;
        host[2][17]  = 1;
        
      //client 2
        int test2=1;//w2 //k2
        graph[2+ct2][18+ct2]  = w2[test2]*n2[0];
        graph[18+ct2][3+ct2]  = Integer.MAX_VALUE;
        graph[18+ct2][4+ct2]  = Integer.MAX_VALUE;
        graph[3+ct2][19+ct2]  = w2[test2]*n2[1];        
        graph[4+ct2][20+ct2]  = w2[test2]*n2[2];
        graph[19+ct2][5+ct2]  = Integer.MAX_VALUE;
        graph[19+ct2][6+ct2]  = Integer.MAX_VALUE;
        graph[20+ct2][6+ct2]  = Integer.MAX_VALUE;
        graph[5+ct2][21+ct2]  = w2[test2]*n2[3];
        graph[6+ct2][22+ct2]  = w2[test2]*n2[4];
        graph[21+ct2][7+ct2]  = Integer.MAX_VALUE;
        graph[22+ct2][7+ct2]  = Integer.MAX_VALUE;
        graph[22+ct2][8+ct2]  = Integer.MAX_VALUE;
        graph[7+ct2][23+ct2]  = w2[test2]*n2[5];
        graph[8+ct2][24+ct2]  = w2[test2]*n2[6];
        graph[23+ct2][9+ct2]  = Integer.MAX_VALUE;
        graph[23+ct2][8+ct2]  = Integer.MAX_VALUE;
        graph[24+ct2][9+ct2]  = Integer.MAX_VALUE;
        graph[9+ct2][25+ct2]  = w2[test2]*n2[7];
        graph[10+ct2][26+ct2] = w2[test2]*n2[8];
        graph[25+ct2][11+ct2] = Integer.MAX_VALUE;
        graph[25+ct2][10+ct2] = Integer.MAX_VALUE;
        graph[26+ct2][11+ct2] = Integer.MAX_VALUE;
        graph[26+ct2][12+ct2] = Integer.MAX_VALUE;
        graph[11+ct2][27+ct2] = w2[test2]*n2[9];
        graph[12+ct2][28+ct2] = w2[test2]*n2[10];
        graph[27+ct2][13+ct2] = Integer.MAX_VALUE;
        graph[28+ct2][14+ct2] = Integer.MAX_VALUE;
        graph[13+ct2][29+ct2] = w2[test2]*n2[11];
        graph[14+ct2][30+ct2] = w2[test2]*n2[12];
        graph[29+ct2][15+ct2] = Integer.MAX_VALUE;
        graph[29+ct2][16+ct2] = Integer.MAX_VALUE;
        graph[30+ct2][16+ct2] = Integer.MAX_VALUE;
        graph[15+ct2][31+ct2] = w2[test2]*n2[13];
        graph[16+ct2][32+ct2] = w2[test2]*n2[14];
        graph[31+ct2][17+ct2] = Integer.MAX_VALUE;
        graph[32+ct2][17+ct2] = Integer.MAX_VALUE;
        graph[17+ct2][33+ct2] = w2[test2]*n2[15];
        graph[2+ct2][3+ct2]   = k2[test2]*c2[0];
        graph[2+ct2][4+ct2]   = k2[test2]*c2[1];
        graph[2+ct2][5+ct2]   = k2[test2]*c2[2];
        graph[2+ct2][6+ct2]   = k2[test2]*c2[3];
        graph[2+ct2][7+ct2]   = k2[test2]*c2[4];
        graph[2+ct2][8+ct2]   = k2[test2]*c2[5];
        graph[2+ct2][9+ct2]   = k2[test2]*c2[6];
        graph[2+ct2][10+ct2]  = k2[test2]*c2[7];
        graph[2+ct2][11+ct2]  = k2[test2]*c2[8];
        graph[2+ct2][12+ct2]  = k2[test2]*c2[9];
        graph[2+ct2][13+ct2]  = k2[test2]*c2[10];
        graph[2+ct2][14+ct2]  = k2[test2]*c2[11];
        graph[2+ct2][15+ct2]  = k2[test2]*c2[12];
        graph[2+ct2][16+ct2]  = k2[test2]*c2[13];
        graph[2+ct2][17+ct2]  = k2[test2]*c2[14];
        
        host[2+ct2][18+ct2]  = 2;
        host[18+ct2][3+ct2]  = 2;
        host[18+ct2][4+ct2]  = 2;
        host[3+ct2][19+ct2]  = 2;   
        host[4+ct2][20+ct2]  = 2;
        host[19+ct2][5+ct2]  = 2;
        host[19+ct2][6+ct2]  = 2;
        host[20+ct2][6+ct2]  = 2;
        host[5+ct2][21+ct2]  = 2;
        host[6+ct2][22+ct2]  = 2;
        host[21+ct2][7+ct2]  = 2;
        host[22+ct2][7+ct2]  = 2;
        host[22+ct2][8+ct2]  = 2;
        host[7+ct2][23+ct2]  = 2;
        host[8+ct2][24+ct2]  = 2;
        host[23+ct2][9+ct2]  = 2;
        host[23+ct2][8+ct2]  = 2;
        host[24+ct2][9+ct2]  = 2;
        host[9+ct2][25+ct2]  = 2;
        host[10+ct2][26+ct2] = 2;
        host[25+ct2][11+ct2] = 2;
        host[25+ct2][10+ct2] = 2;
        host[26+ct2][11+ct2] = 2;
        host[26+ct2][12+ct2] = 2;
        host[11+ct2][27+ct2] = 2;
        host[12+ct2][28+ct2] = 2;
        host[27+ct2][13+ct2] = 2;
        host[28+ct2][14+ct2] = 2;
        host[13+ct2][29+ct2] = 2;
        host[14+ct2][30+ct2] = 2;
        host[29+ct2][15+ct2] = 2;
        host[29+ct2][16+ct2] = 2;
        host[30+ct2][16+ct2] = 2;
        host[15+ct2][31+ct2] = 2;
        host[16+ct2][32+ct2] = 2;
        host[31+ct2][17+ct2] = 2;
        host[32+ct2][17+ct2] = 2;
        host[17+ct2][33+ct2] = 2;
        host[2+ct2][3+ct2]   = 2;
        host[2+ct2][4+ct2]   = 2;
        host[2+ct2][5+ct2]   = 2;
        host[2+ct2][6+ct2]   = 2;
        host[2+ct2][7+ct2]   = 2;
        host[2+ct2][8+ct2]   = 2;
        host[2+ct2][9+ct2]   = 2;
        host[2+ct2][10+ct2]  = 2;
        host[2+ct2][11+ct2]  = 2;
        host[2+ct2][12+ct2]  = 2;
        host[2+ct2][13+ct2]  = 2;
        host[2+ct2][14+ct2]  = 2;
        host[2+ct2][15+ct2]  = 2;
        host[2+ct2][16+ct2]  = 2;
        host[2+ct2][17+ct2]  = 2;
                
      //client 3
        int test3=1;//w3 //k3
        graph[2+ct3][18+ct3]  = w2[test3]*n2[0];
        graph[18+ct3][3+ct3]  = Integer.MAX_VALUE;
        graph[18+ct3][4+ct3]  = Integer.MAX_VALUE;
        graph[3+ct3][19+ct3]  = w2[test3]*n2[1];        
        graph[4+ct3][20+ct3]  = w2[test3]*n2[2];
        graph[19+ct3][5+ct3]  = Integer.MAX_VALUE;
        graph[19+ct3][6+ct3]  = Integer.MAX_VALUE;
        graph[20+ct3][6+ct3]  = Integer.MAX_VALUE;
        graph[5+ct3][21+ct3]  = w2[test3]*n2[3];
        graph[6+ct3][22+ct3]  = w2[test3]*n2[4];
        graph[21+ct3][7+ct3]  = Integer.MAX_VALUE;
        graph[22+ct3][7+ct3]  = Integer.MAX_VALUE;
        graph[22+ct3][8+ct3]  = Integer.MAX_VALUE;
        graph[7+ct3][23+ct3]  = w2[test3]*n2[5];
        graph[8+ct3][24+ct3]  = w2[test3]*n2[6];
        graph[23+ct3][9+ct3]  = Integer.MAX_VALUE;
        graph[23+ct3][8+ct3]  = Integer.MAX_VALUE;
        graph[24+ct3][9+ct3]  = Integer.MAX_VALUE;
        graph[9+ct3][25+ct3]  = w2[test3]*n2[7];
        graph[10+ct3][26+ct3] = w2[test3]*n2[8];
        graph[25+ct3][11+ct3] = Integer.MAX_VALUE;
        graph[25+ct3][10+ct3] = Integer.MAX_VALUE;
        graph[26+ct3][11+ct3] = Integer.MAX_VALUE;
        graph[26+ct3][12+ct3] = Integer.MAX_VALUE;
        graph[11+ct3][27+ct3] = w2[test3]*n2[9];
        graph[12+ct3][28+ct3] = w2[test3]*n2[10];
        graph[27+ct3][13+ct3] = Integer.MAX_VALUE;
        graph[28+ct3][14+ct3] = Integer.MAX_VALUE;
        graph[13+ct3][29+ct3] = w2[test3]*n2[11];
        graph[14+ct3][30+ct3] = w2[test3]*n2[12];
        graph[29+ct3][15+ct3] = Integer.MAX_VALUE;
        graph[29+ct3][16+ct3] = Integer.MAX_VALUE;
        graph[30+ct3][16+ct3] = Integer.MAX_VALUE;
        graph[15+ct3][31+ct3] = w2[test3]*n2[13];
        graph[16+ct3][32+ct3] = w2[test3]*n2[14];
        graph[31+ct3][17+ct3] = Integer.MAX_VALUE;
        graph[32+ct3][17+ct3] = Integer.MAX_VALUE;
        graph[17+ct3][33+ct3] = w2[test3]*n2[15];
        graph[2+ct3][3+ct3]   = k2[test3]*c2[0];
        graph[2+ct3][4+ct3]   = k2[test3]*c2[1];
        graph[2+ct3][5+ct3]   = k2[test3]*c2[2];
        graph[2+ct3][6+ct3]   = k2[test3]*c2[3];
        graph[2+ct3][7+ct3]   = k2[test3]*c2[4];
        graph[2+ct3][8+ct3]   = k2[test3]*c2[5];
        graph[2+ct3][9+ct3]   = k2[test3]*c2[6];
        graph[2+ct3][10+ct3]  = k2[test3]*c2[7];
        graph[2+ct3][11+ct3]  = k2[test3]*c2[8];
        graph[2+ct3][12+ct3]  = k2[test3]*c2[9];
        graph[2+ct3][13+ct3]  = k2[test3]*c2[10];
        graph[2+ct3][14+ct3]  = k2[test3]*c2[11];
        graph[2+ct3][15+ct3]  = k2[test3]*c2[12];
        graph[2+ct3][16+ct3]  = k2[test3]*c2[13];
        graph[2+ct3][17+ct3]  = k2[test3]*c2[14];
        
        host[2+ct3][18+ct3]  = 3;
        host[18+ct3][3+ct3]  = 3;
        host[18+ct3][4+ct3]  = 3;
        host[3+ct3][19+ct3]  = 3;   
        host[4+ct3][20+ct3]  = 3;
        host[19+ct3][5+ct3]  = 3;
        host[19+ct3][6+ct3]  = 3;
        host[20+ct3][6+ct3]  = 3;
        host[5+ct3][21+ct3]  = 3;
        host[6+ct3][22+ct3]  = 3;
        host[21+ct3][7+ct3]  = 3;
        host[22+ct3][7+ct3]  = 3;
        host[22+ct3][8+ct3]  = 3;
        host[7+ct3][23+ct3]  = 3;
        host[8+ct3][24+ct3]  = 3;
        host[23+ct3][9+ct3]  = 3;
        host[23+ct3][8+ct3]  = 3;
        host[24+ct3][9+ct3]  = 3;
        host[9+ct3][25+ct3]  = 3;
        host[10+ct3][26+ct3] = 3;
        host[25+ct3][11+ct3] = 3;
        host[25+ct3][10+ct3] = 3;
        host[26+ct3][11+ct3] = 3;
        host[26+ct3][12+ct3] = 3;
        host[11+ct3][27+ct3] = 3;
        host[12+ct3][28+ct3] = 3;
        host[27+ct3][13+ct3] = 3;
        host[28+ct3][14+ct3] = 3;
        host[13+ct3][29+ct3] = 3;
        host[14+ct3][30+ct3] = 3;
        host[29+ct3][15+ct3] = 3;
        host[29+ct3][16+ct3] = 3;
        host[30+ct3][16+ct3] = 3;
        host[15+ct3][31+ct3] = 3;
        host[16+ct3][32+ct3] = 3;
        host[31+ct3][17+ct3] = 3;
        host[32+ct3][17+ct3] = 3;
        host[17+ct3][33+ct3] = 3;
        host[2+ct3][3+ct3]   = 3;
        host[2+ct3][4+ct3]   = 3;
        host[2+ct3][5+ct3]   = 3;
        host[2+ct3][6+ct3]   = 3;
        host[2+ct3][7+ct3]   = 3;
        host[2+ct3][8+ct3]   = 3;
        host[2+ct3][9+ct3]   = 3;
        host[2+ct3][10+ct3]  = 3;
        host[2+ct3][11+ct3]  = 3;
        host[2+ct3][12+ct3]  = 3;
        host[2+ct3][13+ct3]  = 3;
        host[2+ct3][14+ct3]  = 3;
        host[2+ct3][15+ct3]  = 3;
        host[2+ct3][16+ct3]  = 3;
        host[2+ct3][17+ct3]  = 3;
                
      //server 1
        int test4=1;//w4 //k4
        graph[2+sr1][18+sr1]  = w4[test4]*n4[0];
        graph[18+sr1][3+sr1]  = Integer.MAX_VALUE;
        graph[18+sr1][4+sr1]  = Integer.MAX_VALUE;
        graph[3+sr1][19+sr1]  = w4[test4]*n4[1];      
        graph[4+sr1][20+sr1]  = w4[test4]*n4[2];
        graph[19+sr1][5+sr1]  = Integer.MAX_VALUE;
        graph[19+sr1][6+sr1]  = Integer.MAX_VALUE;
        graph[20+sr1][6+sr1]  = Integer.MAX_VALUE;
        graph[5+sr1][21+sr1]  = w4[test4]*n4[3];
        graph[6+sr1][22+sr1]  = w4[test4]*n4[4];
        graph[21+sr1][7+sr1]  = Integer.MAX_VALUE;
        graph[22+sr1][7+sr1]  = Integer.MAX_VALUE;
        graph[22+sr1][8+sr1]  = Integer.MAX_VALUE;
        graph[7+sr1][23+sr1]  = w4[test4]*n4[5];
        graph[8+sr1][24+sr1]  = w4[test4]*n4[6];
        graph[23+sr1][9+sr1]  = Integer.MAX_VALUE;
        graph[23+sr1][8+sr1]  = Integer.MAX_VALUE;
        graph[24+sr1][9+sr1]  = Integer.MAX_VALUE;
        graph[9+sr1][25+sr1]  = w4[test4]*n4[7];
        graph[10+sr1][26+sr1] = w4[test4]*n4[8];
        graph[25+sr1][11+sr1] = Integer.MAX_VALUE;
        graph[25+sr1][10+sr1] = Integer.MAX_VALUE;
        graph[26+sr1][11+sr1] = Integer.MAX_VALUE;
        graph[26+sr1][12+sr1] = Integer.MAX_VALUE;
        graph[11+sr1][27+sr1] = w4[test4]*n4[9];
        graph[12+sr1][28+sr1] = w4[test4]*n4[10];
        graph[27+sr1][13+sr1] = Integer.MAX_VALUE;
        graph[28+sr1][14+sr1] = Integer.MAX_VALUE;
        graph[13+sr1][29+sr1] = w4[test4]*n4[11];
        graph[14+sr1][30+sr1] = w4[test4]*n4[12];
        graph[29+sr1][15+sr1] = Integer.MAX_VALUE;
        graph[29+sr1][16+sr1] = Integer.MAX_VALUE;
        graph[30+sr1][16+sr1] = Integer.MAX_VALUE;
        graph[15+sr1][31+sr1] = w4[test4]*n4[13];
        graph[16+sr1][32+sr1] = w4[test4]*n4[14];
        graph[31+sr1][17+sr1] = Integer.MAX_VALUE;
        graph[32+sr1][17+sr1] = Integer.MAX_VALUE;
        graph[17+sr1][33+sr1] = w4[test4]*n4[15];
        graph[18+sr1][33+sr1] = k4[test4]*c4[0];
        graph[19+sr1][33+sr1] = k4[test4]*c4[1];
        graph[20+sr1][33+sr1] = k4[test4]*c4[2];
        graph[21+sr1][33+sr1] = k4[test4]*c4[3];
        graph[22+sr1][33+sr1] = k4[test4]*c4[4];
        graph[23+sr1][33+sr1] = k4[test4]*c4[5];
        graph[24+sr1][33+sr1] = k4[test4]*c4[6];
        graph[25+sr1][33+sr1] = k4[test4]*c4[7];
        graph[26+sr1][33+sr1] = k4[test4]*c4[8];
        graph[27+sr1][33+sr1] = k4[test4]*c4[9];
        graph[28+sr1][33+sr1] = k4[test4]*c4[10];
        graph[29+sr1][33+sr1] = k4[test4]*c4[11];
        graph[30+sr1][33+sr1] = k4[test4]*c4[12];
        graph[31+sr1][33+sr1] = k4[test4]*c4[13];
        graph[32+sr1][33+sr1] = k4[test4]*c4[14];
             
        host[2+sr1][18+sr1]  = 0;
        host[18+sr1][3+sr1]  = 0;
        host[18+sr1][4+sr1]  = 0;
        host[3+sr1][19+sr1]  = 0;
        host[4+sr1][20+sr1]  = 0;
        host[19+sr1][5+sr1]  = 0;
        host[19+sr1][6+sr1]  = 0;
        host[20+sr1][6+sr1]  = 0;
        host[5+sr1][21+sr1]  = 0;
        host[6+sr1][22+sr1]  = 0;
        host[21+sr1][7+sr1]  = 0;
        host[22+sr1][7+sr1]  = 0;
        host[22+sr1][8+sr1]  = 0;
        host[7+sr1][23+sr1]  = 0;
        host[8+sr1][24+sr1]  = 0;
        host[23+sr1][9+sr1]  = 0;
        host[23+sr1][8+sr1]  = 0;
        host[24+sr1][9+sr1]  = 0;
        host[9+sr1][25+sr1]  = 0;
        host[10+sr1][26+sr1] = 0;
        host[25+sr1][11+sr1] = 0;
        host[25+sr1][10+sr1] = 0;
        host[26+sr1][11+sr1] = 0;
        host[26+sr1][12+sr1] = 0;
        host[11+sr1][27+sr1] = 0;
        host[12+sr1][28+sr1] = 0;
        host[27+sr1][13+sr1] = 0;
        host[28+sr1][14+sr1] = 0;
        host[13+sr1][29+sr1] = 0;
        host[14+sr1][30+sr1] = 0;
        host[29+sr1][15+sr1] = 0;
        host[29+sr1][16+sr1] = 0;
        host[30+sr1][16+sr1] = 0;
        host[15+sr1][31+sr1] = 0;
        host[16+sr1][32+sr1] = 0;
        host[31+sr1][17+sr1] = 0;
        host[32+sr1][17+sr1] = 0;
        host[17+sr1][33+sr1] = 0;
        host[18+sr1][33+sr1] = 0;
        host[19+sr1][33+sr1] = 0;
        host[20+sr1][33+sr1] = 0;
        host[21+sr1][33+sr1] = 0;
        host[22+sr1][33+sr1] = 0;
        host[23+sr1][33+sr1] = 0;
        host[24+sr1][33+sr1] = 0;
        host[25+sr1][33+sr1] = 0;
        host[26+sr1][33+sr1] = 0;
        host[27+sr1][33+sr1] = 0;
        host[28+sr1][33+sr1] = 0;
        host[29+sr1][33+sr1] = 0;
        host[30+sr1][33+sr1] = 0;
        host[31+sr1][33+sr1] = 0;
        host[32+sr1][33+sr1] = 0;
           
        //End  
        graph[33][130]     = Integer.MAX_VALUE;
        graph[33+ct2][130] = Integer.MAX_VALUE;
        graph[33+ct3][130] = Integer.MAX_VALUE;
        graph[33+sr1][130] = Integer.MAX_VALUE;
        
    //######################################
        
        graph[18][sr1+18]        = Integer.MAX_VALUE;
        graph[ct2+18][sr1+18]    = Integer.MAX_VALUE;
        graph[ct3+18] [sr1+18]   = Integer.MAX_VALUE;      
        
        graph[19][sr1+19]        = Integer.MAX_VALUE;
        graph[ct2+19][sr1+19]    = Integer.MAX_VALUE;
        graph[ct3+19][sr1+19]    = Integer.MAX_VALUE;   
        
        graph[20][sr1+20]        = Integer.MAX_VALUE;
        graph[ct2+20][sr1+20]    = Integer.MAX_VALUE;
        graph[ct3+20][sr1+20]    = Integer.MAX_VALUE;    
  
        graph[21][sr1+21]        = Integer.MAX_VALUE;
        graph[ct2+21][sr1+21]    = Integer.MAX_VALUE;
        graph[ct3+21][sr1+21]    = Integer.MAX_VALUE;
        
        graph[22][sr1+22]        = Integer.MAX_VALUE;
        graph[ct2+22][sr1+22]    = Integer.MAX_VALUE;
        graph[ct3+22][sr1+22]    = Integer.MAX_VALUE;   
        
        graph[23][sr1+23]        = Integer.MAX_VALUE;
        graph[ct2+23][sr1+23]    = Integer.MAX_VALUE;
        graph[ct3+23][sr1+23]    = Integer.MAX_VALUE;  
        
        graph[24][sr1+24]        = Integer.MAX_VALUE;
        graph[ct2+24][sr1+24]    = Integer.MAX_VALUE;
        graph[ct3+24][sr1+24]    = Integer.MAX_VALUE; 
        
        graph[25][sr1+25]        = Integer.MAX_VALUE;
        graph[ct2+25][sr1+25]    = Integer.MAX_VALUE;
        graph[ct3+25][sr1+25]    = Integer.MAX_VALUE;  
        
        graph[26][sr1+26]        = Integer.MAX_VALUE;
        graph[ct2+26][sr1+26]    = Integer.MAX_VALUE;
        graph[ct3+26][sr1+26]    = Integer.MAX_VALUE;  
        
        graph[27][sr1+27]        = Integer.MAX_VALUE;
        graph[ct2+27][sr1+27]    = Integer.MAX_VALUE;
        graph[ct3+27][sr1+27]    = Integer.MAX_VALUE;  
        
        graph[28][sr1+28]        = Integer.MAX_VALUE;
        graph[ct2+28][sr1+28]    = Integer.MAX_VALUE;
        graph[ct3+28][sr1+28]    = Integer.MAX_VALUE;  
        
        graph[29][sr1+29]        = Integer.MAX_VALUE;
        graph[ct2+29][sr1+29]    = Integer.MAX_VALUE;
        graph[ct3+29][sr1+29]    = Integer.MAX_VALUE;  
        
        graph[30][sr1+30]        = Integer.MAX_VALUE;
        graph[ct2+30][sr1+30]    = Integer.MAX_VALUE;
        graph[ct3+30][sr1+30]    = Integer.MAX_VALUE; 
        
        graph[31][sr1+31]        = Integer.MAX_VALUE;
        graph[ct2+31][sr1+31]    = Integer.MAX_VALUE;
        graph[ct3+31][sr1+31]    = Integer.MAX_VALUE;
        
        graph[32][sr1+32]        = Integer.MAX_VALUE;
        graph[ct2+32][sr1+32]    = Integer.MAX_VALUE;
        graph[ct3+32][sr1+32]    = Integer.MAX_VALUE;
        
        graph[33][sr1+33]        = Integer.MAX_VALUE;
        graph[ct2+33][sr1+33]    = Integer.MAX_VALUE;
        graph[ct3+33][sr1+33]    = Integer.MAX_VALUE;
        
        //######################################
        
        source= 1; //scanner.nextInt();
 
        sink = numberOfNodes; //scanner.nextInt();
        Maxflowalgo mf = new Maxflowalgo(numberOfNodes);
        maxFlow = mf.maxFlowMinCut(graph, source, sink);
 
        //MFMC.printCutSet();        
        Maxflowalgo.printGraphviz(graph, host);   
        
        ///////////
        System.out.println("{rank=same; 2;34;66;98;}");
        System.out.println("{rank=same; 3;4;35;36;67;68;99;100}");
        System.out.println("{rank=same; 5;6;37;38;69;70;101;102;}");
        System.out.println("{rank=same; 7;8;39;40;71;72;103;104;}");
        System.out.println("{rank=same; 9;10;41;42;73;74;105;106;}");
        System.out.println("{rank=same; 11;12;43;44;75;76;107;108;}");
        System.out.println("{rank=same; 13;14;45;46;77;78;109;110;}");
        System.out.println("{rank=same; 15;16;47;48;79;80;111;112;}");
        System.out.println("{rank=same; 17;49;81;113;}");
        System.out.println("1 [label=\"" + "start" + "\"]");
        System.out.println("130 [label=\"" + "end" + "\"]");
        System.out.println("}");
        
    }
}
 
class Pair
{
    public int source;
    public int destination;
 
    public Pair (int source, int destination)
    {
        this.source = source;
        this.destination = destination;
    }
}
