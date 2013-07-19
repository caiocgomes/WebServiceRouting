package com.springapp.mvc;

import com.maplink.framework.routing.vehiclerouting.classes.Tuple;
import com.maplink.framework.routing.vehiclerouting.location.LatLng;
import com.maplink.framework.routing.vehiclerouting.webservice.ServiceGetter;
import com.maplink.framework.routing.vehiclerouting.webservice.route.Point;
import com.maplink.framework.routing.vehiclerouting.webservice.route.RouteDetails;
import com.maplink.framework.routing.vehiclerouting.webservice.route.RouteInfo;
import com.maplink.framework.routing.vehiclerouting.webservice.route.RouteLocator;
import com.maplink.framework.routing.vehiclerouting.webservice.route.RouteOptions;
import com.maplink.framework.routing.vehiclerouting.webservice.route.RouteStop;
import com.maplink.framework.routing.vehiclerouting.webservice.route.RouteSummary;
import com.maplink.framework.routing.vehiclerouting.webservice.route.RouteTotals;
import com.maplink.framework.routing.vehiclerouting.webservice.route.Vehicle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;


/**
 * Created with IntelliJ IDEA.
 * User: caio
 * Date: 16/05/13
 * Time: 16:24
 * To change this template use File | Settings | File Templates.
 */
public class Common {

	//    public static Map<Point,Map<Point,Double>> DeserializerDistanceMatrix(String fileName) {
	//        Object obj = null;
	//        try {
	//            FileInputStream f_in = new FileInputStream(fileName);
	//            ObjectInputStream obj_in =
	//                    new ObjectInputStream(f_in);
	//            obj = obj_in.readObject();
	//        } catch (IOException e) {
	//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	//        } catch (ClassNotFoundException e) {
	//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	//        }
	//        Map<Point,Map<Point,Double>> distanceDict = null;
	//
	//        distanceDict = (HashMap<Point,Map<Point,Double>>)obj;
	//
	//
	//        return distanceDict;
	//
	//
	//    }
	//
	//    public static Double[][] TransformToDouble(Map<Point,Map<Point,Double>> distanceDict){
	//
	//        Point[] points = new Point[distanceDict.keySet().toArray().length];
	//        for (int i= 0 ; i< (distanceDict.keySet().toArray()).length;i++){
	//            points[i] = (Point)(distanceDict.keySet().toArray()[i]);
	//        }
	//
	//
	//        Double[][] distanceMatrix = new Double[points.length][points.length];
	//        for (int i = 0 ; i < points.length ; i++){
	//            for (int j = 0 ; j < points.length ; j++){
	//                distanceMatrix[i][j] = distanceDict.get(points[i]).get(points[j]);
	//            }
	//        }
	//
	//
	//        return distanceMatrix;
	//
	//    }
	//
	//    public static Double[][] TransformToDoubleStraightLine(Map<Point,Map<Point,Double>> distanceDict){
	//        Point[] points = new Point[distanceDict.keySet().toArray().length];
	//        for (int i= 0 ; i< (distanceDict.keySet().toArray()).length;i++){
	//            points[i] = (Point)(distanceDict.keySet().toArray()[i]);
	//        }
	//
	//
	//        Double[][] distanceMatrix = new Double[points.length][points.length];
	//        for (int i = 0 ; i < points.length ; i++){
	//            for (int j = 0 ; j < points.length ; j++){
	//                distanceMatrix[i][j] = Math.pow(Math.pow(points[i].getX()-points[j].getX(),2.) + Math.pow(points[i].getY()-points[j].getY(),2.),.5);
	//            }
	//        }
	//
	//        return distanceMatrix;
	//    }

	public static Map<Point,Map<Point,Double>> CreateRandomMatrix(Integer quantity){
		List<Point> pointList = new ArrayList<Point>();

		for (Integer i = 0; i < quantity ; i++){
			pointList.add(new Point(-46.64035 + .09 * (Math.random() * 2 - 1),  -23.54839 + .09 * (Math.random() * 2 - 1)));
		}

		// oq Ã© o double?
				Map<Point,Map<Point,Double>> mapPoint = new HashMap<Point,Map<Point,Double>>();

				for (Point point1 : pointList){
					for (Point point2 : pointList){
						if (mapPoint.containsKey(point1)){
							mapPoint.get(point1).put(point2,0.0);
						} else{
							mapPoint.put(point1, new HashMap<Point, Double>());
							mapPoint.get(point1).put(point2,0.0);
						}
					}

				}
				return mapPoint;
	}

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
				return pointMap;
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

				Tuple<LatLng, Period> destination = getDestinationWithPeriod(origin, 0, pointMap);

				if (destination != null)
				{
					pointMap.put(origin, destination);        
				}
				else {
					pointMap.remove(origin); // se nao há destino com periodo apos o limite de ciclos, remove origem
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


	static Tuple<LatLng, Period> getDestinationWithPeriod(LatLng origin, int cycle, Map<LatLng, Tuple<LatLng, Period>> pointMap) {
		LatLng destination = getRandomPoint();        	
		
		while (pointMap.containsKey(destination)) {
			destination = getRandomPoint(); 
		}
		
		Period period = ServiceGetter.getRouteTimeFromServiceRoute(origin, destination);
		if (period != null)
		{
			return new Tuple<LatLng, Period>(destination, period);        
		}

		// recursao nao maior que 5 ciclos
		if (cycle > 5)
		{
			return null;
		}

		// recursao
		getDestinationWithPeriod(origin, ++cycle, pointMap);

		return null;    
	}

	//    static RouteTotals getRouteTotalsFromServiceRoute(LatLng startPoint, LatLng endPoint) {
	//        final String TOKEN = "yxVibnSHz09lxCOibnSLdwSiNXuiNXNiNJUkNIUkPGoANXomPU==";
	//
	//        Point destinationPoint = new Point(endPoint.getLng(), endPoint.getLat());
	//        Point originPoint = new Point(startPoint.getLng(), startPoint.getLat());       
	//
	//        RouteStop[] routeStops = new RouteStop[] { new RouteStop(originPoint.toString(), originPoint), new RouteStop(destinationPoint.toString(), destinationPoint) };
	//
	//        RouteDetails routeDetails = new RouteDetails();
	//        routeDetails.setDescriptionType(0); //0	Rota urbana,    1	Rota rodoviária
	//        routeDetails.setRouteType(1); // 1	Rota padrão mais curta
	//        routeDetails.setOptimizeRoute(true);
	//
	//        Vehicle vehicle = new Vehicle();
	//        vehicle.setTankCapacity(20);
	//        vehicle.setAverageConsumption(9);
	//        vehicle.setFuelPrice(3);
	//        vehicle.setAverageSpeed(60);
	//        vehicle.setTollFeeCat(2);
	//
	//        RouteOptions routeOptions = new RouteOptions();
	//        routeOptions.setLanguage("portugues");
	//        routeOptions.setRouteDetails(routeDetails);
	//        routeOptions.setVehicle(vehicle);
	//        
	//        RouteTotals routeResponse = null;
	//
	//        try {
	//            routeResponse = new RouteLocator().getRouteSoap().getRouteTotals(routeStops, routeOptions, TOKEN);
	//        } catch (RemoteException e) {
	//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	//        } catch (ServiceException e) {
	//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	//        }
	//
	//        return routeResponse;
	//    }
	//    
	//    public static Double getRouteDistanceFromServiceRoute(LatLng startPoint, LatLng endPoint) {
	//       
	//    	RouteTotals routeResponse = getRouteTotalsFromServiceRoute(startPoint, endPoint);
	//    	
	//    	if (routeResponse != null)
	//    	{
	//    		return routeResponse.getTotalDistance();
	//    	}
	//    	
	//    	return null;
	//    }
	//    
	//    public static Period getRouteTimeFromServiceRoute(LatLng startPoint, LatLng endPoint) {     
	//    	
	//    	RouteTotals routeResponse = getRouteTotalsFromServiceRoute(startPoint, endPoint);
	//    	
	//    	if (routeResponse != null)
	//    	{
	//    		PeriodFormatter formatter = ISOPeriodFormat.standard();      
	//    		return formatter.parsePeriod(routeResponse.getTotalTime()); 
	//    	}
	//    	
	//    	return null;
	//    }


}
