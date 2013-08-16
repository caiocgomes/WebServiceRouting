package com.springapp.mvc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.Period;

import com.maplink.framework.routing.vehiclerouting.classes.Tuple;
import com.maplink.framework.routing.vehiclerouting.location.LatLng;
import com.maplink.framework.routing.vehiclerouting.webservice.ServiceGetter;


/**
 * Created with IntelliJ IDEA.
 * User: caio
 * Date: 16/05/13
 * Time: 16:24
 * To change this template use File | Settings | File Templates.
 */
public class Common {

	public static Map<LatLng, LatLng> CreatePoints(Integer quantityPairs){
		Map<LatLng, LatLng> pointMap = new HashMap<LatLng, LatLng>();

		while (pointMap.size() < quantityPairs){

			LatLng point = new LatLng(-23.54839 + .09 * (Math.random() * 2 - 1), -46.64035 + .09 * (Math.random() * 2 - 1));
			if (!pointMap.containsKey(point)){
				LatLng point2 = new LatLng(-23.54839 + .09 * (Math.random() * 2 - 1), -46.64035 + .09 * (Math.random() * 2 - 1));
				pointMap.put(point, point2);
			}
		}

		return pointMap;
	}

	@SuppressWarnings("unused")
	public static Tuple<LatLng, Map<LatLng, Period>> CreatePointsWithPeriodDeliveryBusinessFromExisting(Integer quantityPoints) throws Exception {

		Map<LatLng, Period> pointMap = new HashMap<LatLng, Period>();

		Tuple<LatLng, Map<LatLng, Period>> tuple = null;
//
//		try {
//
//			FileInputStream in = new FileInputStream("C:/Users/su.yinhe/logistica/webservice/distanceWithPeriodDeliveryBusiness.ttt");
//			if (in != null) {
//				ObjectInputStream objectInputStream = new ObjectInputStream(in);
//				tuple = (Tuple<LatLng, Map<LatLng, Period>>) objectInputStream.readObject();
//				objectInputStream.close();
//				in.close();
//
//				if (tuple.getItem2().size() >= quantityPoints) {
//					return tuple;
//				}
//			}
//			in.close();
//		} catch (IOException e) {
//			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
//		}

		Map<LatLng, Tuple<LatLng, Period>> map = CreatePointsWithPeriod(51);

		int count = 0;
		LatLng origin = null;
		for (LatLng point : map.keySet()) {

			if (count > 0) {

				try {
					Period period = ServiceGetter.getRouteTimeFromServiceRoute(origin, point);
					if (period != null)
					{
						pointMap.put(point, period);
					}

					period = ServiceGetter.getRouteTimeFromServiceRoute(origin, map.get(point).getItem1());
					if (period != null)
					{
						pointMap.put(map.get(point).getItem1(), period);
					}

				} catch (Exception ex) {
					throw (ex);
				}
			}
			else {
				if (tuple == null) {
					origin = point;
				}
				else {
					origin = tuple.getItem1();
					pointMap = tuple.getItem2();
				}
			}

			count++;
		}

		tuple = new Tuple<LatLng, Map<LatLng, Period>>(origin, pointMap);

		try
		{
			FileOutputStream fileOut = new FileOutputStream("distanceWithPeriodDeliveryBusiness.ttt");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(tuple);
			out.close();
			fileOut.close();
		} catch (IOException i)
		{
			i.printStackTrace();
		}

		return tuple;
	}

	@SuppressWarnings("unused")
	public static Tuple<LatLng, Map<LatLng, Period>> CreatePointsWithPeriodDeliveryBusiness(Integer quantityPoints) throws Exception {

		Map<LatLng, Period> pointMap = new HashMap<LatLng, Period>();

		Tuple<LatLng, Map<LatLng, Period>> tuple = null;

		try {

			FileInputStream in = new FileInputStream("C:/Users/su.yinhe/logistica/webservice/distanceWithPeriodDeliveryBusiness.ttt");
			if (in != null) {
				ObjectInputStream objectInputStream = new ObjectInputStream(in);
				tuple = (Tuple<LatLng, Map<LatLng, Period>>) objectInputStream.readObject();
				objectInputStream.close();
				in.close();

				if (tuple.getItem2().size() >= quantityPoints) {
					return tuple;
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		}

		LatLng origin;
		if (tuple == null) {
			origin = getRandomPoint();
		}
		else {
			origin = tuple.getItem1();
			pointMap = tuple.getItem2();
		}

		while (pointMap.size() < quantityPoints) {

			try {
				Tuple<LatLng, Period> destination = getDestinationWithPeriodDeliveryBusiness(origin, 0, pointMap);

				if (destination != null)
				{
					pointMap.put(destination.getItem1(), destination.getItem2());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		tuple = new Tuple<LatLng, Map<LatLng, Period>>(origin, pointMap);

		try
		{
			FileOutputStream fileOut = new FileOutputStream("distanceWithPeriodDeliveryBusiness.ttt");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(tuple);
			out.close();
			fileOut.close();
		} catch (IOException i)
		{
			i.printStackTrace();
		}

		return tuple;
	}

	public static Map<LatLng, Tuple<LatLng, Tuple<Period, Calendar>>> CreatePointsWithPeriodTest(Integer quantityPairs) throws Exception {

		Map<LatLng, Tuple<LatLng, Tuple<Period, Calendar>>> pointMap = new HashMap<LatLng, Tuple<LatLng, Tuple<Period, Calendar>>>();

//		try {
//
//			BufferedReader br = new BufferedReader(new FileReader("C:/Users/su.yinhe/logistica/webservice/pointsTest.ttt"));
//			String line;
//			while ((line = br.readLine()) != null) {
//				String[] parts = line.split("=");
//
//				LatLng point = new LatLng(NumberFormat.getNumberInstance().parse(parts[0]).doubleValue(), NumberFormat.getNumberInstance().parse(parts[1]).doubleValue());
//
//				pointMap.put(point, value)
//			}
//			br.close();
//
//		} catch (IOException e) {
//			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//		}

		Calendar dateNow = Calendar.getInstance();

		LatLng origin = new LatLng(-23.40568112270302, -46.68415895263672);
		LatLng destination = new LatLng(-23.56427895103743, -47.01893232543947);
		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 8, 0, 0))));

		origin = new LatLng(-23.48495219578804, -46.85448072631836);
		destination = new LatLng(-23.79742650263333, -46.91618173583984);
		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 10, 10, 0))));

		origin = new LatLng(-23.62642613560707, -46.93334787353515);
		destination = new LatLng(-23.76551202306803, -46.82578360839844);
		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 11, 50, 0))));

		origin = new LatLng(-23.85471698145226, -46.75574576660156);
		destination = new LatLng(-23.74917208724653, -46.44400870605469);
		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 14, 20, 0))));

		// 5
		origin = new LatLng(-23.86350851568398, -46.60743033691406);
		destination = new LatLng(-23.668699449390672, -46.206429360351564);
		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 16, 10, 0))));

		origin = new LatLng(-23.77556634849701, -46.31629264160156);
		destination = new LatLng(-23.461002598059007, -46.25449454589844);
		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 17, 40, 0))));

		origin = new LatLng(-23.55670981619539, -46.13913810058593);
		destination = new LatLng(-23.28325678030005, -46.42478263183593);
		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 21, 30, 0))));

//		origin = new LatLng(-23.34883615855892, -46.18033683105469);
//		destination = new LatLng(-23.44210480420892, -46.59781729980468);
//		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
//				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
//				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 23, 00, 0))));

//		origin = new LatLng(-23.38161370727884, -46.41791617675781);
//		destination = new LatLng(-23.51893895962348, -46.63764273925781);
//		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
//				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
//				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 23, 0, 0))));

		// 10
//		origin = new LatLng(-23.27694936669028, -46.68982779785156);
//		destination = new LatLng(-23.707684581643, -46.60880362792968);
//		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
//				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
//				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 20, 30, 0))));
//
//		origin = new LatLng(-23.56048630516684, -46.71317374511719);
//		destination = new LatLng(-23.630960828223802, -46.411736367187494);
//		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
//				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
//				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 20, 50, 0))));
//
//		origin = new LatLng(-23.68882226117832, -46.70356070800781);
//		destination = new LatLng(-23.58062574621202, -46.35886466308593);
//		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
//				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
//				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 21, 30, 0))));
//
//		origin = new LatLng(-23.71774337118144, -46.48932730957031);
//		destination = new LatLng(-23.49123344160243, -46.44263541503906);
//		pointMap.put(origin, new Tuple<LatLng, Tuple<Period, Calendar>>(destination, new Tuple<Period, Calendar>(
//				ServiceGetter.getRouteTimeFromServiceRoute(origin, destination),
//				new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//						dateNow.get(Calendar.DAY_OF_MONTH) + 1, 21, 50, 0))));

//		try
//		{
//			FileOutputStream fileOut = new FileOutputStream("pointsTest.ttt");
//			ObjectOutputStream out = new ObjectOutputStream(fileOut);
//			out.writeObject(pointMap);
//			out.close();
//			fileOut.close();
//		}
//		catch(IOException i)
//		{
//			i.printStackTrace();
//		}

		return pointMap;
	}

	@SuppressWarnings("unused")
	public static Map<LatLng, Tuple<LatLng, Period>> CreatePointsWithPeriod(Integer quantityPairs){

		Map<LatLng, Tuple<LatLng, Period>> pointMap = new HashMap<LatLng, Tuple<LatLng, Period>>();

		try {

			// FileInputStream fileIn =
			//        new FileInputStream("distanceDict.ttt");

			//			InputStream in = Common.class.getClassLoader().getResourceAsStream("/distanceWithPeriod.ttt");
			//			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("/distanceWithPeriod.ttt");

			FileInputStream in = new FileInputStream("C:/Users/su.yinhe/logistica/webservice/distanceWithPeriod.ttt");
			if (in != null) {
				ObjectInputStream objectInputStream = new ObjectInputStream(in);
				pointMap =  (Map<LatLng, Tuple<LatLng, Period>>) objectInputStream.readObject();
				objectInputStream.close();
				in.close();

				if (pointMap.size() >= quantityPairs) {
					return pointMap;
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}


		while (pointMap.size() < quantityPairs){

			LatLng origin = getRandomPoint();
			if (!pointMap.containsKey(origin)){

				try {
					Tuple<LatLng, Period> destination = getDestinationWithPeriod(origin, 0, pointMap);

					if (destination != null)
					{
						pointMap.put(origin, destination);
					}
					else {
						pointMap.remove(origin); // se nao há destino com periodo apos o limite de ciclos, remove origem
					}
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		try
		{
			FileOutputStream fileOut = new FileOutputStream("distanceWithPeriod.ttt");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(pointMap);
			out.close();
			fileOut.close();
		}
		catch(IOException i)
		{
			i.printStackTrace();
		}

		return pointMap;
	}

	static LatLng getRandomPoint() {
		return new LatLng(-23.54839 + .09 * (Math.random() * 2.0 - 1.0), -46.64035 + .09 * (Math.random() * 2.0 - 1.0));
	}


	static Tuple<LatLng, Period> getDestinationWithPeriod(LatLng origin, int cycle, Map<LatLng, Tuple<LatLng, Period>> pointMap) throws Exception {
		LatLng destination = getRandomPoint();

		while (pointMap.containsKey(destination)) {
			destination = getRandomPoint();
		}

		try {
			Period period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
			if (period != null)
			{
				return new Tuple<LatLng, Period>(destination, period);
			}
		}
		catch (Exception ex) {
			throw (ex);
		}

		// recursao nao maior que 5 ciclos
		if (cycle > 5)
		{
			return null;
		}

		// recursao
		return getDestinationWithPeriod(origin, ++cycle, pointMap);
	}

	static Tuple<LatLng, Period> getDestinationWithPeriodDeliveryBusiness(LatLng origin, int cycle, Map<LatLng, Period> pointMap) throws Exception {
		LatLng destination = getRandomPoint();

		while (pointMap.containsKey(destination)) {
			destination = getRandomPoint();
		}

		try {
			Period period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
			if (period != null)
			{
				return new Tuple<LatLng, Period>(destination, period);
			}
		} catch (Exception ex) {
			throw (ex);
		}

		// recursao nao maior que 5 ciclos
		if (cycle > 5)
		{
			return null;
		}

		// recursao
		return getDestinationWithPeriodDeliveryBusiness(origin, ++cycle, pointMap);
	}
}
