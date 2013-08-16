package com.springapp.mvc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.Period;

import com.maplink.framework.routing.vehiclerouting.costCalculator.ICostCalculator;
import com.maplink.framework.routing.vehiclerouting.costCalculator.SPTransMaximizeClients;
import com.maplink.framework.routing.vehiclerouting.costCalculator.TotalDistanceCost;
import com.maplink.framework.routing.vehiclerouting.costCalculator.factory.CostCalculatorFactory;
import com.maplink.framework.routing.vehiclerouting.costCalculator.requests.CostCalculatorRequest;
import com.maplink.framework.routing.vehiclerouting.distanceCalculator.IDistanceCalculator;
import com.maplink.framework.routing.vehiclerouting.distanceCalculator.factory.DistanceCalculatorFactory;
import com.maplink.framework.routing.vehiclerouting.distanceCalculator.requests.DistanceCalculatorRequest;
import com.maplink.framework.routing.vehiclerouting.location.LatLng;
import com.maplink.framework.routing.vehiclerouting.location.TimeableTransportableLatLng;
import com.maplink.framework.routing.vehiclerouting.montecarlo.IMonteCarlo;
import com.maplink.framework.routing.vehiclerouting.montecarlo.IMonteCarloDecisionRule;
import com.maplink.framework.routing.vehiclerouting.montecarlo.factories.MonteCarloDecisionRuleFactory;
import com.maplink.framework.routing.vehiclerouting.montecarlo.factories.MonteCarloFactory;
import com.maplink.framework.routing.vehiclerouting.montecarlo.requests.MonteCarloDecisionRuleRequest;
import com.maplink.framework.routing.vehiclerouting.montecarlo.requests.MonteCarloRequest;
import com.maplink.framework.routing.vehiclerouting.permutator.factory.PermutatorFactory;
import com.maplink.framework.routing.vehiclerouting.permutator.requests.PermutatorRequest;
import com.maplink.framework.routing.vehiclerouting.timeableFunctions.Timeable;
import com.maplink.framework.routing.vehiclerouting.transportable.ITimeableTransportable;
import com.maplink.framework.routing.vehiclerouting.transportable.factory.TransportableFactory;
import com.maplink.framework.routing.vehiclerouting.transportable.requests.TimeableTransportableRequest;
import com.maplink.framework.routing.vehiclerouting.transporter.ITimeableTransporter;
import com.maplink.framework.routing.vehiclerouting.transporterConteiner.ITransporterContainer;
import com.maplink.framework.routing.vehiclerouting.types.DistanceType;
import com.maplink.framework.routing.vehiclerouting.types.TransportableType;
import com.maplink.framework.routing.vehiclerouting.webservice.ServiceGetter;

public class Test {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

//		testMonteCarloSerializable();
//		testProblem();
//		String string = testMonteCarloEmptyContainer(300, 600, 82, 8);
//		System.out.println(string);
		String string = testMonteCarloEmptyContainerDemo(60, 40, 6, 8);
		System.out.println(string);
	}

	static void testProblem() throws Exception {
		try {

			FileInputStream in = new FileInputStream("C:/Users/su.yinhe/logistica/webservice/problem.ttt");
			if (in != null) {
				ObjectInputStream objectInputStream = new ObjectInputStream(in);
				ITimeableTransporter transporter = (ITimeableTransporter) objectInputStream.readObject();
				objectInputStream.close();
				in.close();

				transporter.isValid();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	static String testMonteCarloEmptyContainerDemo(int quantityClients, int waitingListSize, int quantityBus, int busCapacity) throws Exception {
		try {

			FileInputStream in = new FileInputStream("C:/Users/su.yinhe/logistica/webservice/vehicleContainerEmpty" + quantityClients + "-"
					+ waitingListSize + ".ttt");
			if (in != null) {
				ObjectInputStream objectInputStream = new ObjectInputStream(in);
				ITransporterContainer vehicleContainer = (ITransporterContainer) objectInputStream.readObject();
				objectInputStream.close();
				in.close();

				in = new FileInputStream("C:/Users/su.yinhe/logistica/webservice/clients" + quantityClients + "-" + waitingListSize + ".ttt");
				if (in != null) {
					objectInputStream = new ObjectInputStream(in);
					Map<Integer, ITimeableTransportable> idClient_Client = (Map<Integer, ITimeableTransportable>) objectInputStream.readObject();
					objectInputStream.close();
					in.close();

					SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd_HH-mm-ss");
					String date = dateFormat.format(new Date());
					PrintWriter writer = new PrintWriter(date + "test" + ".txt");
					writer.println("quantityClients: " + quantityClients +
							"quantityBus: " + quantityBus +
							"busCapacity: " + busCapacity);

					Calendar aux = new GregorianCalendar();
//					int millis = 10800000;
					int millis = 54000000;

					// altera horario dos clientes
					Map<Integer, ITimeableTransportable> newIdClient_Client = new HashMap<Integer, ITimeableTransportable>();
					for (Integer id : idClient_Client.keySet()) {
						writer.println("client: " + id);
						writer.println("start: " + idClient_Client.get(id).getStartPointInfo().getTime().getMinDateTime().getTime() + ","
								+ idClient_Client.get(id).getStartPointInfo().getTime().getDateTime().getTime()
								+ ", " + idClient_Client.get(id).getStartPointInfo().getTime().getMaxDateTime().getTime());
						writer.println("end: " + idClient_Client.get(id).getEndPointInfo().getTime().getMinDateTime().getTime() + ","
								+ idClient_Client.get(id).getEndPointInfo().getTime().getDateTime().getTime()
								+ ", " + idClient_Client.get(id).getEndPointInfo().getTime().getMaxDateTime().getTime());

						aux.setTimeInMillis(idClient_Client.get(id).getStartPointInfo().getTime().getMinDateTime().getTimeInMillis() - millis);
						Calendar startMin = (Calendar) aux.clone();

						aux.setTimeInMillis(idClient_Client.get(id).getStartPointInfo().getTime().getMaxDateTime().getTimeInMillis() + millis);
						Calendar startMax = (Calendar) aux.clone();

						aux.setTimeInMillis(idClient_Client.get(id).getEndPointInfo().getTime().getMinDateTime().getTimeInMillis() - millis);
						Calendar endMin = (Calendar) aux.clone();

						aux.setTimeInMillis(idClient_Client.get(id).getEndPointInfo().getTime().getMaxDateTime().getTimeInMillis() + millis);
						Calendar endMax = (Calendar) aux.clone();

						newIdClient_Client.put(id, (ITimeableTransportable) TransportableFactory
								.createObject(new TimeableTransportableRequest(id, TransportableType.Person,
										new TimeableTransportableLatLng(id, idClient_Client.get(id).getStartPointInfo().getLatLng(),
												new Timeable(idClient_Client.get(id).getStartPointInfo().getTime().getDateTime(),
														startMin, startMax), false),
														new TimeableTransportableLatLng(id, idClient_Client.get(id).getEndPointInfo().getLatLng(),
																new Timeable(idClient_Client.get(id).getEndPointInfo().getTime().getDateTime(),
																		endMin, endMax), true))));

						writer.println("newStart: " + newIdClient_Client.get(id).getStartPointInfo().getTime().getMinDateTime().getTime() + ","
								+ newIdClient_Client.get(id).getStartPointInfo().getTime().getDateTime().getTime()
								+ ", " + newIdClient_Client.get(id).getStartPointInfo().getTime().getMaxDateTime().getTime());
						writer.println("newEnd: " + newIdClient_Client.get(id).getEndPointInfo().getTime().getMinDateTime().getTime() + ","
								+ newIdClient_Client.get(id).getEndPointInfo().getTime().getDateTime().getTime()
								+ ", " + newIdClient_Client.get(id).getEndPointInfo().getTime().getMaxDateTime().getTime());
					}

					// altera horario dos usuarios da lista de espera
					for (int i = 0; i < vehicleContainer.getWaitingList().size(); i++) {

						ITimeableTransportable transp = vehicleContainer.getWaitingList().get(i);

						writer.println("client: " + transp.getId());
						writer.println("start: " + transp.getStartPointInfo().getTime().getMinDateTime().getTime() + ","
								+ transp.getStartPointInfo().getTime().getDateTime().getTime()
								+ ", " + transp.getStartPointInfo().getTime().getMaxDateTime().getTime());
						writer.println("end: " + transp.getEndPointInfo().getTime().getMinDateTime().getTime() + ","
								+ transp.getEndPointInfo().getTime().getDateTime().getTime()
								+ ", " + transp.getEndPointInfo().getTime().getMaxDateTime().getTime());

						aux.setTimeInMillis(transp.getStartPointInfo().getTime().getMinDateTime().getTimeInMillis() - millis);
						Calendar startMin = (Calendar) aux.clone();

						aux.setTimeInMillis(transp.getStartPointInfo().getTime().getMaxDateTime().getTimeInMillis() + millis);
						Calendar startMax = (Calendar) aux.clone();

						aux.setTimeInMillis(transp.getEndPointInfo().getTime().getMinDateTime().getTimeInMillis() - millis);
						Calendar endMin = (Calendar) aux.clone();

						aux.setTimeInMillis(transp.getEndPointInfo().getTime().getMaxDateTime().getTimeInMillis() + millis);
						Calendar endMax = (Calendar) aux.clone();

						vehicleContainer.getWaitingList().remove(i);
						vehicleContainer.getWaitingList().add((ITimeableTransportable) TransportableFactory
								.createObject(new TimeableTransportableRequest(transp.getId(), TransportableType.Person,
										new TimeableTransportableLatLng(transp.getId(), transp.getStartPointInfo().getLatLng(),
												new Timeable(transp.getStartPointInfo().getTime().getDateTime(),
														startMin, startMax), false),
														new TimeableTransportableLatLng(transp.getId(), transp.getEndPointInfo().getLatLng(),
																new Timeable(transp.getEndPointInfo().getTime().getDateTime(),
																		endMin, endMax), true))));

						writer.println("newStart: "
								+ vehicleContainer.getWaitingList().get(vehicleContainer.getWaitingList().size() - 1).getStartPointInfo().getTime().getMinDateTime().getTime()
								+ ","
								+ vehicleContainer.getWaitingList().get(vehicleContainer.getWaitingList().size() - 1).getStartPointInfo().getTime().getDateTime().getTime()
								+ ", "
								+ vehicleContainer.getWaitingList().get(vehicleContainer.getWaitingList().size() - 1).getStartPointInfo().getTime().getMaxDateTime().getTime());
						writer.println("newEnd: "
								+ vehicleContainer.getWaitingList().get(vehicleContainer.getWaitingList().size() - 1).getEndPointInfo().getTime().getMinDateTime().getTime()
								+ ","
								+ vehicleContainer.getWaitingList().get(vehicleContainer.getWaitingList().size() - 1).getEndPointInfo().getTime().getDateTime().getTime()
								+ ", "
								+ vehicleContainer.getWaitingList().get(vehicleContainer.getWaitingList().size() - 1).getEndPointInfo().getTime().getMaxDateTime().getTime());
					}

					writer.flush();

					boolean hasStartpoint = true;

					RouteExecutor2.populateVehicles(newIdClient_Client, vehicleContainer, quantityBus);

					// seta um distanceCalculator com cache mais atualizado
					IDistanceCalculator distanceCalculator = DistanceCalculatorFactory.createObject(new DistanceCalculatorRequest(DistanceType.Real, true));
					ICostCalculator costCalculator = new SPTransMaximizeClients(distanceCalculator, null); // sem factory apenas para esse teste
					vehicleContainer.setCostCalculator(costCalculator);

					IMonteCarloDecisionRule decisionRule = MonteCarloDecisionRuleFactory.createObject(new MonteCarloDecisionRuleRequest(0.0, 0.005));
					IMonteCarlo monteCarlo = MonteCarloFactory.createObject(new MonteCarloRequest(decisionRule, vehicleContainer,
							PermutatorFactory.createObject(new PermutatorRequest(hasStartpoint))));

					System.out.println("Start: " + GregorianCalendar.getInstance().getTime());
					ITransporterContainer containerWithOptimisedStrategy = monteCarlo.run();
					System.out.println("End: " + GregorianCalendar.getInstance().getTime());

					RouteExecutor2.printResults(containerWithOptimisedStrategy, writer);
					writer.flush();

					// segunda fase
					distanceCalculator = DistanceCalculatorFactory.createObject(new DistanceCalculatorRequest(DistanceType.Real, true));
					costCalculator = new TotalDistanceCost(distanceCalculator); // sem factory apenas para esse teste
					containerWithOptimisedStrategy.setCostCalculator(costCalculator);
					containerWithOptimisedStrategy.getWaitingList().clear();

					monteCarlo = MonteCarloFactory.createObject(new MonteCarloRequest(decisionRule, containerWithOptimisedStrategy,
							PermutatorFactory.createObject(new PermutatorRequest(hasStartpoint))));

					System.out.println("SecondStart: " + GregorianCalendar.getInstance().getTime());
					containerWithOptimisedStrategy = monteCarlo.run();
					System.out.println("SecondEnd: " + GregorianCalendar.getInstance().getTime());
					// fim da segunda fase

					String result = RouteExecutor2.printResults(containerWithOptimisedStrategy, writer);
					writer.close();

					return result;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		}

		return null;
	}

	static String testMonteCarloEmptyContainer(int quantityClients, int waitingListSize, int quantityBus, int busCapacity) {
		try {

			FileInputStream in = new FileInputStream("C:/Users/su.yinhe/logistica/webservice/vehicleContainerEmpty" + quantityClients + "-"
					+ waitingListSize + ".ttt");
			if (in != null) {
				ObjectInputStream objectInputStream = new ObjectInputStream(in);
				ITransporterContainer vehicleContainer = (ITransporterContainer) objectInputStream.readObject();
				objectInputStream.close();
				in.close();

				in = new FileInputStream("C:/Users/su.yinhe/logistica/webservice/clients" + quantityClients + "-" + waitingListSize + ".ttt");
				if (in != null) {
					objectInputStream = new ObjectInputStream(in);
					Map<Integer, ITimeableTransportable> idClient_Client = (Map<Integer, ITimeableTransportable>) objectInputStream.readObject();
					objectInputStream.close();
					in.close();

					SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd_HH-mm-ss");
					String date = dateFormat.format(new Date());
					PrintWriter writer = new PrintWriter(date + "test" + ".txt");
					writer.println("quantityClients: " + quantityClients +
							"quantityBus: " + quantityBus +
							"busCapacity: " + busCapacity);

					for (Integer id : idClient_Client.keySet()) {
						writer.println("client: " + id + ", " + idClient_Client.get(id).getStartPointInfo().getTime().getDateTime().getTime() + ", "
								+ idClient_Client.get(id).getEndPointInfo().getTime().getDateTime().getTime());
					}

					writer.flush();

					boolean hasStartpoint = true;

					RouteExecutor2.populateVehicles(idClient_Client, vehicleContainer, quantityBus);

					IMonteCarloDecisionRule decisionRule = MonteCarloDecisionRuleFactory.createObject(new MonteCarloDecisionRuleRequest(0.0, 0.005));
					IMonteCarlo monteCarlo = MonteCarloFactory.createObject(new MonteCarloRequest(decisionRule, vehicleContainer,
							PermutatorFactory.createObject(new PermutatorRequest(hasStartpoint))));

					try {

						System.out.println("Start: " + GregorianCalendar.getInstance().getTime());
						ITransporterContainer containerWithOptimisedStrategy = monteCarlo.run();
						System.out.println("End: " + GregorianCalendar.getInstance().getTime());

						RouteExecutor2.printResults(containerWithOptimisedStrategy, writer);
						writer.flush();

						// segunda fase
						IDistanceCalculator distanceCalculator = DistanceCalculatorFactory.createObject(new DistanceCalculatorRequest(DistanceType.Real, true));
						ICostCalculator costCalculator = CostCalculatorFactory.createObject(new CostCalculatorRequest(distanceCalculator, null, true, false));
						containerWithOptimisedStrategy.setCostCalculator(costCalculator);
						containerWithOptimisedStrategy.getWaitingList().clear();

						monteCarlo = MonteCarloFactory.createObject(new MonteCarloRequest(decisionRule, containerWithOptimisedStrategy,
								PermutatorFactory.createObject(new PermutatorRequest(hasStartpoint))));

						System.out.println("SecondStart: " + GregorianCalendar.getInstance().getTime());
						containerWithOptimisedStrategy = monteCarlo.run();
						System.out.println("SecondEnd: " + GregorianCalendar.getInstance().getTime());
						// fim da segunda fase

						String result = RouteExecutor2.printResults(containerWithOptimisedStrategy, writer);
						writer.close();

						return result;
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		}

		return null;
	}

	static void testMonteCarloSerializable() {
		try {

			FileInputStream in = new FileInputStream("C:/Users/su.yinhe/logistica/webservice/vehicleContainer.ttt");
			if (in != null) {
				ObjectInputStream objectInputStream = new ObjectInputStream(in);
				ITransporterContainer vehicleContainer = (ITransporterContainer) objectInputStream.readObject();
				objectInputStream.close();
				in.close();

				SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd_HH-mm-ss");
				String date = dateFormat.format(new Date());
				PrintWriter writer = new PrintWriter(date + "test" + ".txt");

//				Period tolerance = new Period(0, 1200, 0, 0);
//
//				for (ITimeableTransporter transp : vehicleContainer) {
//					for (TimeableTransportableLatLng transportable : transp.getTrajectory()) {
//						transportable.getTime().setTolerance(tolerance);
//					}
//				}

				IMonteCarloDecisionRule decisionRule = MonteCarloDecisionRuleFactory.createObject(new MonteCarloDecisionRuleRequest(0.0, 0.005));
				IMonteCarlo monteCarlo = MonteCarloFactory.createObject(new MonteCarloRequest(decisionRule, vehicleContainer,
						PermutatorFactory.createObject(new PermutatorRequest(true))));

				try {
					System.out.println("Start: " + GregorianCalendar.getInstance().getTime());
					ITransporterContainer containerWithOptimisedStrategy = monteCarlo.run();
					System.out.println("End: " + GregorianCalendar.getInstance().getTime());

					RouteExecutor2.printResults(containerWithOptimisedStrategy, writer);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				writer.close();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		} catch (ClassNotFoundException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
		}

	}

	static void testManualPoints() throws Exception {
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
