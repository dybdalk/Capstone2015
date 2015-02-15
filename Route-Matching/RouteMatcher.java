import java.util.*;
import java.io.*;

class RouteMatcher
{
	//Test out some different matching algorithms based on different input data
	public static void main(String[] args)
	{
		//gather all the information
		ArrayList<Route> routes = parseData(args[0]);
		// for(Route n : routes)
		// 	System.out.println("Route: "+n+"\n");
		//assign scores to all the points and put it all in a hashmap to see if it's reasonable.
		for(Route n : routes)
		{
			System.out.println("----------------------------");
			// System.out.println("1st Route: "+n);
			// System.out.println("----------------------------");
			for(Route m: routes)
			{
				//System.out.println("Other Route: "+m);
				System.out.println("Score: "+score(m,n));
				System.out.println("------------------------");
			}
			//System.out.println("----------------------------");
		}


	}

	//this parses a data set, presumably in a text file.
	private static ArrayList<Route> parseData(String filename)
	{
		//make the scanner for this file
		Scanner sc = null;
		try
		{
			sc = new Scanner(new File(filename));
		}
		catch(FileNotFoundException e) //user is dumb
		{
			System.out.println("could not find the file \""+filename+"\"."); //tell user they are dumb
			System.exit(1); //do not use this program anymore dummy.
		}

		//user is not dumb, give them memory to use.
		ArrayList<Route> data = new ArrayList<Route>();
		//ArrayList<Double> stimes = new ArrayList<Double>();
		//ArrayList<Double> etimes = new ArrayList<Double>();

		//now parse the file they input.
		while(sc.hasNextLine())
		{
			//all data items
			Double slat = sc.nextDouble();
			Double slong = sc.nextDouble();
			Double elat = sc.nextDouble();
			Double elong = sc.nextDouble();
			data.add(new Route(slat,slong,elat,elong));
			// System.out.println("num of Routes: "+data.size());
			// System.out.println("--------------------------------------");
			// System.out.println(data.get(data.size()-1).toString());
			// System.out.println("--------------------------------------");

		}

		return data;
	}

	//Calculate crow flies distance between points using the Haversine distance formula.
	private static double score(Route n, Route m)
	{
		return Math.abs(startScore(n,m))+Math.abs(endScore(n,m));
	}

	//see how far apart the start points are (in km)
	private static double startScore(Route n, Route m)
	{
		double R = 6372.8;
		//start point stuff
		// System.out.println("n.slat: "+n.getslat()+"\t m.slat: "+m.getslat());
		// System.out.println("difference: "+(n.getslat()-m.getslat()));
		double dsLat = Math.toRadians(m.getslat() - n.getslat());
		double dsLon = Math.toRadians(m.getslong() - n.getslong());
		//System.out.println("dsLat: "+dsLat+"\t dsLong: "+dsLon);
		Double nslat = Math.toRadians(n.getslat());
		Double mslat = Math.toRadians(m.getslat());
		//System.out.println("nslat: "+nslat+"\t mslat: "+mslat);
		double a = Math.sin(dsLat / 2) * Math.sin(dsLat / 2) + Math.sin(dsLon / 2) * Math.sin(dsLon / 2) * Math.cos(nslat) * Math.cos(mslat);
		double c = 2 * Math.asin(Math.sqrt(a));
		return R*c;
	}

	//see how far apart the endpoints are (in km)
	private static double endScore(Route n, Route m)
	{
		//end point stuff
		double R = 6372.8;
		double deLat = Math.toRadians(m.elat - n.elat);
		double deLon = Math.toRadians(m.elong - n.elong);
		Double nelat = Math.toRadians(n.getelat());
		Double melat = Math.toRadians(m.getelat());
		double b = Math.sin(dsLat / 2) * Math.sin(deLat / 2) + Math.sin(deLon / 2) * Math.sin(deLon / 2) * Math.cos(nelat) * Math.cos(melat);
		double d = 2 * Math.asin(Math.sqrt(b));

		return R*d;
	}

	  /////////////////////////////////////////////////////////////////////////////////////////////////
	 ///////////////////////////////////////////// Route //////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////



	private static class Route
	{
		private Double slat; //start latitude
		private Double slong; //start longitude
		private Double elat; //end latitude
		private Double elong; //end longitude

		/**
		* Constructor for Route objects
		*/
		public Route(Double slat, Double slong, Double elat, Double elong)
		{
			this.slat = slat;
			this.slong = slong;
			this.elat = elat;
			this.elong = elong;
		}

		public Double getslat(){return slat;}
		public Double getslong(){return slong;}
		public Double getelat(){return elat;}
		public Double getelong(){return elong;}

		public boolean equals(Route other)
		{
			boolean starts = slat == other.getslat() && slong == other.getslong();
			boolean ends = elat == other.getelong() && elong == other.getelong();
			return starts && ends;
		}

		public String toString()
		{
			String toReturn = "Start lat: "+slat+". Start long: "+slong;
			toReturn += "\nEnd lat: "+elat+". End long: "+elong;
			return toReturn;
		}

	}
}