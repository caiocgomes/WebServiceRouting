package com.springapp.mvc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

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

		RouteExecutor2.start(60, 6, 30);
//		test();
//		test2();
//		fillPlotCost();
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

	static void test2() throws Exception {
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

	static void test() throws Exception {
		LatLng origin = new LatLng(-23.40568112270302, -46.68415895263672);
		LatLng destination = new LatLng(-23.48495219578804, -46.85448072631836);
		Period period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.48495219578804, -46.85448072631836);
		destination = new LatLng(-23.56427895103743, -47.01893232543947);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.56427895103743, -47.01893232543947);
		destination = new LatLng(-23.62642613560707, -46.93334787353515);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.62642613560707, -46.93334787353515);
		destination = new LatLng(-23.79742650263333, -46.91618173583984);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.79742650263333, -46.91618173583984);
		destination = new LatLng(-23.76551202306803, -46.82578360839844);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.76551202306803, -46.82578360839844);
		destination = new LatLng(-23.85471698145226, -46.75574576660156);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.85471698145226, -46.75574576660156);
		destination = new LatLng(-23.86350851568398, -46.60743033691406);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.86350851568398, -46.60743033691406);
		destination = new LatLng(-23.74917208724653, -46.44400870605469);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.74917208724653, -46.44400870605469);
		destination = new LatLng(-23.77556634849701, -46.31629264160156);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.77556634849701, -46.31629264160156);
		destination = new LatLng(-23.668699449390672, -46.206429360351564);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.668699449390672, -46.206429360351564);
		destination = new LatLng(-23.55670981619539, -46.13913810058593);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.55670981619539, -46.13913810058593);
		destination = new LatLng(-23.461002598059007, -46.25449454589844);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.461002598059007, -46.25449454589844);
		destination = new LatLng(-23.34883615855892, -46.18033683105469);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.34883615855892, -46.18033683105469);
		destination = new LatLng(-23.38161370727884, -46.41791617675781);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.38161370727884, -46.41791617675781);
		destination = new LatLng(-23.28325678030005, -46.42478263183593);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.28325678030005, -46.42478263183593);
		destination = new LatLng(-23.27694936669028, -46.68982779785156);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.27694936669028, -46.68982779785156);
		destination = new LatLng(-23.44210480420892, -46.59781729980468);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.44210480420892, -46.59781729980468);
		destination = new LatLng(-23.51893895962348, -46.63764273925781);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.51893895962348, -46.63764273925781);
		destination = new LatLng(-23.56048630516684, -46.71317374511719);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.56048630516684, -46.71317374511719);
		destination = new LatLng(-23.68882226117832, -46.70356070800781);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.68882226117832, -46.70356070800781);
		destination = new LatLng(-23.707684581643, -46.60880362792968);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.707684581643, -46.60880362792968);
		destination = new LatLng(-23.71774337118144, -46.48932730957031);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.71774337118144, -46.48932730957031);
		destination = new LatLng(-23.630960828223802, -46.411736367187494);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.630960828223802, -46.411736367187494);
		destination = new LatLng(-23.58062574621202, -46.35886466308593);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

		origin = new LatLng(-23.58062574621202, -46.35886466308593);
		destination = new LatLng(-23.49123344160243, -46.44263541503906);
		period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		System.out.println(period);

	}

	static void CreatePointsWithPeriod() {

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
