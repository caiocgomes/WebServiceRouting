package com.springapp.mvc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.joda.time.Period;

import com.maplink.framework.routing.vehiclerouting.location.LatLng;
import com.maplink.framework.routing.vehiclerouting.webservice.ServiceGetter;

public class Main {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

//		RouteExecutor2.start(13, 1, 30);
//		test();
		test2();
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

}
