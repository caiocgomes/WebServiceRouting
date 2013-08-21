package com.springapp.mvc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.joda.time.Period;

import com.maplink.framework.routing.vehiclerouting.classes.Tuple;
import com.maplink.framework.routing.vehiclerouting.location.LatLng;
import com.maplink.framework.routing.vehiclerouting.webservice.ServiceGetter;

public class Main {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		String string = RouteExecutor2.start(200, 82, 8);
		System.out.println(string);
//		testAddressFinder();
//		getPointsFromSPTrans();
//		getFromFile();
//		fillPlotCost();
//		FixFileDistanceWithNewLatLngVersion();
	}

	static void testAddressFinder() throws RemoteException, ServiceException {
		// R. LUIS PALES MATOS, 19 CS 1 - VILA FACHINI
		// R. FREI CANECA, 351, BELA VISTA


		LatLng point = ServiceGetter.getLatLngFromServiceAddressFinder("R. FREI CANECA", "351", null, "BELA VISTA", "são paulo", "sp");
		System.out.println(point);

	}

	static void getPointsFromSPTrans() throws ServiceException {
		try {

			BufferedReader br = new BufferedReader(new FileReader("C:/Users/su.yinhe/logistica/webservice/programacaoSPTrans.csv"));

			PrintWriter writer = new PrintWriter("programacaoSPTrans" + ".txt");

			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(";");

				try {
					LatLng point1 = ServiceGetter.getLatLngFromServiceAddressFinder(parts[1], parts[2], null, parts[3], "são paulo", "sp");
					LatLng point2 = ServiceGetter.getLatLngFromServiceAddressFinder(parts[4], parts[5], null, parts[6], "são paulo", "sp");

					writer.println(parts[0] + ";"
							+ point1.getLat() + "#" + point1.getLng() + ";"
							+ point2.getLat() + "#" + point2.getLng() + ";"
							+ parts[8] + ";"
							+ parts[9] + ";"
							+ parts[10] + ";"
							+ parts[11] + ";"
							);
				} catch (Exception e) {
					e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
				}
			}
			br.close();
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		}
	}

	static void fillPlotCost() throws Exception {
		try {

			String file = "1375886204275";
			BufferedReader br = new BufferedReader(new FileReader("C:/Users/su.yinhe/logistica/webservice/monteCarlo" + file + ".txt"));
			String line;

			PrintWriter plotWriter = new PrintWriter("plotCost" + file + ".txt");

			int count = 0;

			double minimum = Double.MAX_VALUE;
			double previous = Double.MAX_VALUE;
			while ((line = br.readLine()) != null) {

				if (line.startsWith("ContainerCost")) {
					String[] parts = line.split(":");
					double num = Double.parseDouble(parts[1].trim());

					if (num < minimum) {
						minimum = num;
					}

					if (previous != num) {
						previous = num;
						line = br.readLine();
						parts = line.split(":");
						plotWriter.println(num + "," + parts[1].trim());
					}
				}

				if (line.startsWith("Same")) {
					double num = minimum;

					if (previous != num) {
						previous = num;
						String[] parts = line.split(":");
						plotWriter.println(num + "," + parts[1].trim());
					}
				}

				if (count % 100 < 1) {
					plotWriter.flush();
				}
				count++;
			}
			br.close();
			plotWriter.close();

		} catch (IOException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		}
	}

	static void getFromFile() throws Exception {
		try {

			BufferedReader br = new BufferedReader(new FileReader("C:/Users/su.yinhe/logistica/webservice/monteCarlo28.txt"));
			String line;
			LatLng lastPoint = null;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
//
//				LatLng point = new LatLng(NumberFormat.getNumberInstance().parse(parts[2].trim()).doubleValue(),
//						NumberFormat.getNumberInstance().parse(parts[3].trim()).doubleValue());

				LatLng point = new LatLng(Double.parseDouble(parts[2].trim()), Double.parseDouble(parts[3].trim()));

				if (lastPoint != null) {
					Period period = ServiceGetter.getRouteTimeFromServiceRoute(lastPoint, point);
					System.out.println(period);
				}

				lastPoint = point;
			}
			br.close();

		} catch (IOException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		}
	}

	static void FixFileDistanceWithNewLatLngVersion() {

		Map<LatLng, Tuple<LatLng, Period>> pointMap = new HashMap<LatLng, Tuple<LatLng, Period>>();
		Map<LatLng, Tuple<LatLng, Period>> newPointMap = new HashMap<LatLng, Tuple<LatLng, Period>>();

		try {

			// FileInputStream fileIn =
			// new FileInputStream("distanceDict.ttt");

			// InputStream in = Common.class.getClassLoader().getResourceAsStream("/distanceWithPeriod.ttt");
			// InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("/distanceWithPeriod.ttt");

			FileInputStream in = new FileInputStream("C:/Users/su.yinhe/logistica/webservice/distanceWithPeriod.ttt");
			if (in != null) {
				ObjectInputStream objectInputStream = new ObjectInputStream(in);
				pointMap = (Map<LatLng, Tuple<LatLng, Period>>) objectInputStream.readObject();
				objectInputStream.close();
				in.close();

				for (LatLng point : pointMap.keySet()) {
					newPointMap.put(new LatLng(point.getLat(), point.getLng()),
							new Tuple<LatLng, Period>(new LatLng(pointMap.get(point).getItem1().getLat(), pointMap.get(point).getItem1().getLng()),
									pointMap.get(point).getItem2()));
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		}

		try
		{
			FileOutputStream fileOut = new FileOutputStream("distanceWithPeriod.ttt");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(newPointMap);
			out.close();
			fileOut.close();
		} catch (IOException i)
		{
			i.printStackTrace();
		}
	}

}
