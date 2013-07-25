package com.springapp.mvc;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.joda.time.Period;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.maplink.framework.routing.vehiclerouting.classes.Tuple;
import com.maplink.framework.routing.vehiclerouting.costCalculator.ICostCalculator;
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
import com.maplink.framework.routing.vehiclerouting.timeableFunctions.ITimeCalculator;
import com.maplink.framework.routing.vehiclerouting.timeableFunctions.Timeable;
import com.maplink.framework.routing.vehiclerouting.timeableFunctions.factory.TimeCalculatorFactory;
import com.maplink.framework.routing.vehiclerouting.timeableFunctions.requests.TimeCalculatorRequest;
import com.maplink.framework.routing.vehiclerouting.transportable.ITimeableTransportable;
import com.maplink.framework.routing.vehiclerouting.transportable.factory.TransportableFactory;
import com.maplink.framework.routing.vehiclerouting.transportable.requests.TimeableTransportableRequest;
import com.maplink.framework.routing.vehiclerouting.transporter.ITimeableTransporter;
import com.maplink.framework.routing.vehiclerouting.transporter.factory.TransporterFactory;
import com.maplink.framework.routing.vehiclerouting.transporter.requests.TimeableTransporterRequest;
import com.maplink.framework.routing.vehiclerouting.transporterConteiner.ITransporterContainer;
import com.maplink.framework.routing.vehiclerouting.transporterConteiner.factory.TransporterContainerFactory;
import com.maplink.framework.routing.vehiclerouting.transporterConteiner.requests.TransporterContainerRequest;
import com.maplink.framework.routing.vehiclerouting.types.DistanceType;
import com.maplink.framework.routing.vehiclerouting.types.TransportableType;

@Controller
@RequestMapping("/addContact")
public class RouteExecutor {
	@RequestMapping(method = RequestMethod.POST)
	public String getRoutes(@ModelAttribute("SpringWeb") Route routeParam,
			ModelMap model) throws Exception {

		if (routeParam.getQuantityBus() * routeParam.getBusCapacity() >= routeParam.getQuantityClients()) {
			String ans = start(routeParam.getQuantityClients(), routeParam.getQuantityBus(), routeParam.getBusCapacity());
			model.addAttribute("route", ans);
			return "routes2";
		} else {
			model.addAttribute(
					"info",
					"<emph>n√£o √© possivel realizar essa rota com o n√∫mero de veiculos pedidos</emph>");
			return "redirect:/";
		}
	}

	static Calendar getDate() {

		int hour = randBetween(7, 19);
		int min = randBetween(0, 59);
		int sec = randBetween(0, 59);

		Calendar dateNow = Calendar.getInstance();

		// cria um horario para o dia seguinte
		return new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
				dateNow.get(Calendar.DAY_OF_MONTH) + 1, hour, min, sec);
	}

	static int randBetween(int start, int end) {
		return start + (int) Math.round(Math.random() * (end - start));
	}

	static ITimeableTransporter getNotFullVehicle(
			ITransporterContainer vehicleContainer, int quantityBus) {
		Random randomGenerator = new Random();

		ITimeableTransporter vehicle = vehicleContainer.get(randomGenerator.nextInt(quantityBus));

		if (!vehicle.isFull()) {
			return vehicle;
		}

		getNotFullVehicle(vehicleContainer, quantityBus);

		return null;
	}

	public static String start(Integer quantityClients, Integer quantityBus,
			Integer busCapacity) throws Exception {

//		int totalPairPoints = quantityClients + 1; // pares de pontos dos clientes e par de pontos do onibus
		int totalPairPoints = 40 + 1; // pares de pontos dos clientes e par de pontos do onibus

		// obtem uma lista de pares de pontos aleatorios com periodo entre eles
		Map<LatLng, Tuple<LatLng, Period>> pointSet = Common.CreatePointsWithPeriod(totalPairPoints);

//		Set<LatLng> setTest = new HashSet<LatLng>();
//		for (LatLng point : pointSet.keySet()) {
//			if (setTest.contains(point)) {
//				System.out.println("1: " + point + ", 2: "
//						+ pointSet.get(point).getItem1() + ", period: "
//						+ pointSet.get(point).getItem2());
//			} else {
//				setTest.add(point);
//			}
//			// System.out.println("1: " + point + ", 2: " +
//			// pointSet.get(point).getItem1() + ", period: " +
//			// pointSet.get(point).getItem2());
//		}

		// Map de clientes
		Map<Integer, ITimeableTransportable> idClient_Client = new HashMap<Integer, ITimeableTransportable>();

		int count = 1;
//		Period startAfterTolerance = new Period(0);
		Period zeroPeriod = new Period(0);
		Period twoT; // 2T+30min
		Period tolerance = new Period(0, 60, 0, 0);
		Calendar dateNow = Calendar.getInstance();
		Calendar busStartDate = new GregorianCalendar(2013,
				dateNow.get(Calendar.MONTH) + 1,
				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 4, 30, 0);
		Calendar busEndDate = new GregorianCalendar(2013,
				dateNow.get(Calendar.MONTH) + 1,
				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 21, 00, 0);
		LatLng busStartPoint = null;
		LatLng busEndPoint = null;

		// insere pontos de clientes
		for (LatLng point : pointSet.keySet()) {

			// È feio, mas È apenas para testes
			if (count >= totalPairPoints) {
				busStartPoint = point;
				busEndPoint = pointSet.get(point).getItem1();
				break;
			}

//			Calendar startDate = getDate();
//			Calendar endDate = new GregorianCalendar();
//			endDate.setTimeInMillis(startDate.getTimeInMillis()
//					+ pointSet.get(point).getItem2().toStandardDuration()
//					.getMillis());

			Calendar endDate = getDate();

			twoT = pointSet.get(point).getItem2().plus(pointSet.get(point).getItem2());

			Calendar startDate = new GregorianCalendar();
			startDate.setTimeInMillis(endDate.getTimeInMillis() - twoT.toStandardDuration().getMillis());

			Calendar startMinDateTime = new GregorianCalendar();
			startMinDateTime.setTimeInMillis(endDate.getTimeInMillis() - twoT.toStandardDuration().getMillis()
					- tolerance.toStandardDuration().getMillis());

			Calendar startMaxDateTime = new GregorianCalendar();
			startMaxDateTime.setTimeInMillis(endDate.getTimeInMillis() - pointSet.get(point).getItem2().toStandardDuration().getMillis());

			idClient_Client.put(count, (ITimeableTransportable) TransportableFactory
					.createObject(new TimeableTransportableRequest(count, TransportableType.Person,
							new TimeableTransportableLatLng(count, point, new Timeable(startDate, startMinDateTime, startMaxDateTime), false),
							new TimeableTransportableLatLng(count, pointSet.get(point).getItem1(), new Timeable(endDate, tolerance, zeroPeriod), true))));

			count++;
		}

		IDistanceCalculator calculator = DistanceCalculatorFactory.createObject(new DistanceCalculatorRequest(DistanceType.Real, true));
		ICostCalculator costCalculator = CostCalculatorFactory.createObject(new CostCalculatorRequest(calculator));
		ITransporterContainer vehicleContainer = (ITransporterContainer) TransporterContainerFactory
				.createObject(new TransporterContainerRequest(costCalculator));

		ITimeCalculator timeCalculator = TimeCalculatorFactory.createObject(new TimeCalculatorRequest(DistanceType.Real, true));

		// adiciona veiculos no container
		for (int i = 0; i < quantityBus; i++) {
			vehicleContainer.add((ITimeableTransporter) TransporterFactory.createObject(
					new TimeableTransporterRequest(new Timeable(busStartDate, zeroPeriod, tolerance),
							new Timeable(busEndDate, zeroPeriod, tolerance), busStartPoint, busEndPoint, timeCalculator)));
		}

		ITimeableTransporter vehicle;
		// distribui os pontos nos diversos onibus do container
		for (int id : idClient_Client.keySet()) {

			vehicle = getNotFullVehicle(vehicleContainer, quantityBus);
			vehicle.put(id, idClient_Client.get(id));
		}

		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("------------------------");
		System.out.println("Salve Container? s/n");
		String response = bufferRead.readLine();
		if (response.equals("s")) {
			try
			{
				FileOutputStream fileOut = new FileOutputStream("vehicleContainer.ttt");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(vehicleContainer);
				out.close();
				fileOut.close();
			} catch (IOException i)
			{
				i.printStackTrace();
			}
		}
		System.out.println("------------------------");

		IMonteCarloDecisionRule decisionRule = MonteCarloDecisionRuleFactory.createObject(new MonteCarloDecisionRuleRequest(0.0, 0.005));
		IMonteCarlo monteCarlo = MonteCarloFactory.createObject(new MonteCarloRequest(decisionRule, vehicleContainer,
				PermutatorFactory.createObject(new PermutatorRequest())));

		try {
			System.out.println("Start: " + GregorianCalendar.getInstance().getTime());
			ITransporterContainer containerWithOptimisedStrategy = monteCarlo.run();
			System.out.println("End: " + GregorianCalendar.getInstance().getTime());

			StringBuilder csvString;
			StringBuilder csvStringFinal = new StringBuilder();
			count = 1;

			System.out.println("Cost: " + containerWithOptimisedStrategy.getContainerCost());

			for (ITimeableTransporter transporter : containerWithOptimisedStrategy) {

				csvString = new StringBuilder();
				System.out.println("Trajectory: " + count);
				System.out.println("TrajectoryEndDateTime: "
						+ transporter.getCurrentDeliveryTime().getTime());
				System.out.println("TrajectoryValidation: "
						+ transporter.getLastValidation());

				for (TimeableTransportableLatLng monteCarloPoint : transporter.getTrajectory()) {
					System.out.println("Id: "
							+ monteCarloPoint.getId()
							+ ", index: "
							+ monteCarloPoint.getIndex()
							+ ", latlng: "
							+ monteCarloPoint.getLatLng()
							+ ", isEnd: "
							+ monteCarloPoint.isEnd()
							+ ", originalDateTime: "
							+ monteCarloPoint.getTime().getDateTime().getTime()
							+ ", arrivalDateTime: "
							+ monteCarloPoint.getCurrentDeliveryTime()
							.getTime());

					StringBuilder csvPoints = new StringBuilder();
					if (csvString.length() == 0) {
						csvPoints.append("[")
						.append(monteCarloPoint.getLatLng().getLng())
						.append(",")
						.append(monteCarloPoint.getLatLng().getLat())
						.append("]");
					} else {
						csvPoints.append(",\n[")
						.append(monteCarloPoint.getLatLng().getLng())
						.append(",")
						.append(monteCarloPoint.getLatLng().getLat())
						.append("]");
					}

					csvString.append(csvPoints);
				}

				csvStringFinal.append("var latLongs")
				.append(Integer.toString(count)).append(" = [")
				.append(csvString).append("];\n").append("traceRoute(")
				.append("latLongs").append(Integer.toString(count))
				.append(",get_random_color())\n\n");
				count++;

			}

			bufferRead.close();

			System.out.println("Caio: It's ended: Now we plot");
			return csvStringFinal.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		bufferRead.close();
		return null;

	}
}
