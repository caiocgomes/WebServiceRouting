package com.springapp.mvc;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
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
import com.maplink.framework.routing.vehiclerouting.location.ITimeableTransportableLatLng;
import com.maplink.framework.routing.vehiclerouting.location.LatLng;
import com.maplink.framework.routing.vehiclerouting.location.factory.TimeableTransportableLatLngFactory;
import com.maplink.framework.routing.vehiclerouting.location.requests.TimeableTransportableLatLngRequest;
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
import com.maplink.framework.routing.vehiclerouting.transportable.ITimeableStock;
import com.maplink.framework.routing.vehiclerouting.transportable.ITimeableTransportable;
import com.maplink.framework.routing.vehiclerouting.transportable.factory.TransportableFactory;
import com.maplink.framework.routing.vehiclerouting.transportable.requests.TimeableTransportableRequest;
import com.maplink.framework.routing.vehiclerouting.transporter.ITimeableTransporter;
import com.maplink.framework.routing.vehiclerouting.transporter.ITruck;
import com.maplink.framework.routing.vehiclerouting.transporter.factory.TransporterFactory;
import com.maplink.framework.routing.vehiclerouting.transporter.requests.TimeableTransporterRequest;
import com.maplink.framework.routing.vehiclerouting.transporterConteiner.ITransporterContainer;
import com.maplink.framework.routing.vehiclerouting.transporterConteiner.factory.TransporterContainerFactory;
import com.maplink.framework.routing.vehiclerouting.transporterConteiner.requests.TransporterContainerRequest;
import com.maplink.framework.routing.vehiclerouting.types.DistanceType;
import com.maplink.framework.routing.vehiclerouting.types.TransportableType;

@Controller
@RequestMapping("/addContact")
public class RouteExecutor2 {
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
			ITransporterContainer vehicleContainer, int quantityBus, double weight, int cycle) {
		Random randomGenerator = new Random();

		ITimeableTransporter vehicle = vehicleContainer.get(randomGenerator.nextInt(quantityBus));

		// qdo È caminhao, deve-se verificar o limite de peso antes da inserÁ„o
		if (vehicle instanceof ITruck) {
			if (((ITruck) vehicle).getCurrentWeight() + weight < ((ITruck) vehicle).getMaximumWeight()) {
				return vehicle;
			}
		}

		else if (!vehicle.isFull()) {
			return vehicle;
		}

		if (cycle < 10) {
			return getNotFullVehicle(vehicleContainer, quantityBus, weight, ++cycle);
		}

		for (int i = 0; i < quantityBus; i++) {
			vehicle = vehicleContainer.get(i);

			// qdo È caminhao, deve-se verificar o limite de peso antes da inserÁ„o
			if (vehicle instanceof ITruck) {
				if (((ITruck) vehicle).getCurrentWeight() + weight < ((ITruck) vehicle).getMaximumWeight()) {
					return vehicle;
				}
			}

			else if (!vehicle.isFull()) {
				return vehicle;
			}
		}

		return null;
	}

	static void changeSPTransClientsAndContainerTest(ITransporterContainer vehicleContainer, Map<Integer, ITimeableTransportable> idClient_Client,
			int totalPairPoints, int quantityBus)
					throws Exception {

		// obtem uma lista de pares de pontos aleatorios com periodo entre eles
		Map<LatLng, Tuple<LatLng, Tuple<Period, Calendar>>> pointSet = Common.CreatePointsWithPeriodTest(totalPairPoints);

		int count = 1;
		Period twoT; // 2T+30min
		Period maxTolerance = new Period(0, 60, 0, 0);
		Period idealTolerance = new Period(0, 30, 0, 0);
		Period zeroPeriod = new Period(0);

		Calendar dateNow = Calendar.getInstance();
		Calendar busStartDate = new GregorianCalendar(2013,
				dateNow.get(Calendar.MONTH) + 1,
				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 4, 30, 0);
		Calendar busEndDate = new GregorianCalendar(2013,
				dateNow.get(Calendar.MONTH) + 1,
				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 23, 00, 0);

		LatLng busStartPoint = null;
		LatLng busEndPoint = null;

//		List<Calendar> dates = new ArrayList<Calendar>();
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 7, 0, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 8, 30, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 10, 0, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 12, 0, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 13, 0, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 15, 0, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 17, 0, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 18, 30, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 19, 0, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 19, 45, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 20, 30, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 20, 50, 0));
//		dates.add(new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
//				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 21, 10, 0));

		// insere pontos de clientes
		for (LatLng point : pointSet.keySet()) {

			// È feio, mas È apenas para testes
//			if (count >= totalPairPoints) {
//				busStartPoint = point;
//				busEndPoint = pointSet.get(point).getItem1();
//				break;
//			}

//					Calendar startDate = getDate();
//					Calendar endDate = new GregorianCalendar();
//					endDate.setTimeInMillis(startDate.getTimeInMillis()
//							+ pointSet.get(point).getItem2().toStandardDuration()
//							.getMillis());

			Calendar endDate = pointSet.get(point).getItem2().getItem2();

			twoT = pointSet.get(point).getItem2().getItem1().plus(pointSet.get(point).getItem2().getItem1());

			// tempo minimo ideal do ponto inicial
			Calendar startDate = new GregorianCalendar();
			startDate.setTimeInMillis(endDate.getTimeInMillis() - twoT.toStandardDuration().getMillis()
					- idealTolerance.toStandardDuration().getMillis());

			// tempo minimo permitido do ponto inicial
			Calendar startMinDateTime = new GregorianCalendar();
			startMinDateTime.setTimeInMillis(endDate.getTimeInMillis() - twoT.toStandardDuration().getMillis()
					- maxTolerance.toStandardDuration().getMillis());

			// tempo maximo do ponto inicial
			Calendar startMaxDateTime = new GregorianCalendar();
			startMaxDateTime.setTimeInMillis(endDate.getTimeInMillis() - pointSet.get(point).getItem2().getItem1().toStandardDuration().getMillis());

			// tempo minimo ideal do ponto final
			endDate.add(Calendar.MILLISECOND, (int) (-idealTolerance.toStandardDuration().getMillis()));

			idClient_Client.put(count, (ITimeableTransportable) TransportableFactory
					.createObject(new TimeableTransportableRequest(count, TransportableType.Person,
							TimeableTransportableLatLngFactory.createObject(
									new TimeableTransportableLatLngRequest(count, point, new Timeable(startDate, startMinDateTime, startMaxDateTime), false, true)),
									TimeableTransportableLatLngFactory.createObject(
											new TimeableTransportableLatLngRequest(count, pointSet.get(point).getItem1(), new Timeable(endDate, idealTolerance, idealTolerance), true, true)))));

			count++;
		}

		LatLng origin = new LatLng(-23.503179072016273, -46.65936302124023);
		LatLng destination = new LatLng(-23.49373353367442, -46.6415102380371);
		busStartPoint = origin;
		busEndPoint = destination;

		ITimeCalculator timeCalculator = TimeCalculatorFactory.createObject(new TimeCalculatorRequest(DistanceType.Real, true));

		// adiciona veiculos no container
		for (int i = 0; i < quantityBus; i++) {
			vehicleContainer.add((ITimeableTransporter) TransporterFactory.createObject(new TimeableTransporterRequest(
					new Timeable(busStartDate, zeroPeriod, idealTolerance), new Timeable(busEndDate, zeroPeriod, idealTolerance),
					busStartPoint, busEndPoint, timeCalculator)));
		}

	}

	static ITransporterContainer changeSPTransClientsAndContainer(Map<Integer, ITimeableTransportable> idClient_Client,
			List<ITimeableTransportable> waitingList, int totalPairPoints, int quantityBus)
					throws Exception {

		// obtem uma lista de pares de pontos aleatorios com periodo entre eles
		Map<LatLng, Tuple<LatLng, Period>> pointSet = Common.CreatePointsWithPeriod(totalPairPoints + 10);

		int count = 1;
		Period twoT; // 2T+30min
		Period maxTolerance = new Period(0, 60, 0, 0);
		Period idealTolerance = new Period(0, 30, 0, 0);
		Period zeroPeriod = new Period(0);

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
			if (count == totalPairPoints) {
				busStartPoint = point;
				busEndPoint = pointSet.get(point).getItem1();
				count++;
				continue;
			}

//					Calendar startDate = getDate();
//					Calendar endDate = new GregorianCalendar();
//					endDate.setTimeInMillis(startDate.getTimeInMillis()
//							+ pointSet.get(point).getItem2().toStandardDuration()
//							.getMillis());

			Calendar endDate = getDate();

			twoT = pointSet.get(point).getItem2().plus(pointSet.get(point).getItem2());

			// tempo minimo ideal do ponto inicial
			Calendar startDate = new GregorianCalendar();
			startDate.setTimeInMillis(endDate.getTimeInMillis() - twoT.toStandardDuration().getMillis()
					- idealTolerance.toStandardDuration().getMillis());

			// tempo minimo permitido do ponto inicial
			Calendar startMinDateTime = new GregorianCalendar();
			startMinDateTime.setTimeInMillis(endDate.getTimeInMillis() - twoT.toStandardDuration().getMillis()
					- maxTolerance.toStandardDuration().getMillis());

			// tempo maximo do ponto inicial
			Calendar startMaxDateTime = new GregorianCalendar();
			startMaxDateTime.setTimeInMillis(endDate.getTimeInMillis() - pointSet.get(point).getItem2().toStandardDuration().getMillis());

			// tempo minimo ideal do ponto final
			endDate.add(Calendar.MILLISECOND, (int) (-idealTolerance.toStandardDuration().getMillis()));

			// È feio, mas È apenas para testes
			if (count > totalPairPoints) {
				waitingList.add((ITimeableTransportable) TransportableFactory
						.createObject(new TimeableTransportableRequest(count, TransportableType.Person,
								TimeableTransportableLatLngFactory.createObject(
										new TimeableTransportableLatLngRequest(count, point, new Timeable(startDate, startMinDateTime, startMaxDateTime), false, true)),
										TimeableTransportableLatLngFactory.createObject(
												new TimeableTransportableLatLngRequest(count, pointSet.get(point).getItem1(), new Timeable(endDate, idealTolerance, idealTolerance), true, true)))));

				System.out.println("client: " + count + ", " + endDate.getTime() + ", " + pointSet.get(point).getItem2());
				count++;
				continue;
			}

			idClient_Client.put(count, (ITimeableTransportable) TransportableFactory
					.createObject(new TimeableTransportableRequest(count, TransportableType.Person,
							TimeableTransportableLatLngFactory.createObject(
									new TimeableTransportableLatLngRequest(count, point, new Timeable(startDate, startMinDateTime, startMaxDateTime), false, true)),
									TimeableTransportableLatLngFactory.createObject(
											new TimeableTransportableLatLngRequest(count, pointSet.get(point).getItem1(), new Timeable(endDate, idealTolerance, idealTolerance), true, true)))));

			System.out.println("client: " + count + ", " + endDate.getTime() + ", " + pointSet.get(point).getItem2());

			count++;
		}

		ITimeCalculator timeCalculator = TimeCalculatorFactory.createObject(new TimeCalculatorRequest(DistanceType.Real, true));

		IDistanceCalculator calculator = DistanceCalculatorFactory.createObject(new DistanceCalculatorRequest(DistanceType.Real, true));
		ICostCalculator costCalculator = CostCalculatorFactory.createObject(new CostCalculatorRequest(calculator, true, true));
		ITransporterContainer vehicleContainer = (ITransporterContainer) TransporterContainerFactory
				.createObject(new TransporterContainerRequest(costCalculator, waitingList));

		// adiciona veiculos no container
		for (int i = 0; i < quantityBus; i++) {
			vehicleContainer.add((ITimeableTransporter) TransporterFactory.createObject(new TimeableTransporterRequest(
					new Timeable(busStartDate, zeroPeriod, idealTolerance), new Timeable(busEndDate, zeroPeriod, idealTolerance),
					busStartPoint, busEndPoint, timeCalculator)));
		}

		return vehicleContainer;
	}

	static ITransporterContainer changeSPTransClientsAndContainerFromSPTransData(Map<Integer, ITimeableTransportable> idClient_Client,
			List<ITimeableTransportable> waitingList, int totalPairPoints, int quantityBus, Integer busCapacity, PrintWriter writer)
					throws Exception {

//		Map<LatLng, Tuple<LatLng, Period>> pointMap = new HashMap<LatLng, Tuple<LatLng, Period>>();

		new HashMap<LatLng, Tuple<LatLng, Tuple<Period, Calendar>>>();

		Calendar dateNow = Calendar.getInstance();
		int count = 1;
		Period twoT; // 2T+30min
		Period maxTolerance = new Period(0, 60, 0, 0);
		Period idealTolerance = new Period(0, 30, 0, 0);
		Period zeroPeriod = new Period(0);

		LatLng busStartPoint = new LatLng(-23.503179072016273, -46.65936302124023);
		LatLng busEndPoint = busStartPoint;

		ITimeCalculator timeCalculator = TimeCalculatorFactory.createObject(new TimeCalculatorRequest(DistanceType.Real, true));

		BufferedReader br = new BufferedReader(new FileReader("C:/Users/su.yinhe/logistica/webservice/programacaoSPTrans.txt"));
		String line;
		while ((line = br.readLine()) != null) {

			try {
				String[] parts = line.split(";");

				int id = Integer.valueOf(parts[0]);

				// casa
				String[] partsPoint = parts[1].split("#");
				LatLng point1 = new LatLng(Double.parseDouble(partsPoint[0].trim()), Double.parseDouble(partsPoint[1].trim()));

				// compromisso
				partsPoint = parts[2].split("#");
				LatLng point2 = new LatLng(Double.parseDouble(partsPoint[0].trim()), Double.parseDouble(partsPoint[1].trim()));

				boolean isWheelchairUser;
				if (parts[5].trim().equals("Cadeira")) {
					isWheelchairUser = true;
				}
				else {
					isWheelchairUser = false;
				}

				boolean isDouble;
				if (parts[6].trim().equals("Sim")) {
					isDouble = true;
				}
				else {
					isDouble = false;
				}

				String[] dateSplit = parts[3].split(":");

				// tempo do compromisso na ida do arquivo
				Calendar endDate = new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
						dateNow.get(Calendar.DAY_OF_MONTH) + 1, Integer.valueOf(dateSplit[0]), Integer.valueOf(dateSplit[1]), 0);

				Calendar startDate;
				Calendar startMinDateTime;
				Calendar startMaxDateTime;
				Calendar endMaxDateTime = null;
				Calendar endMinDateTime = null;
				Period oneT;
				Period unlimitedTolerance = new Period(0, 1200, 0, 0);

				oneT = timeCalculator.getTime(point1, point2);
				twoT = oneT.plus(oneT);

				if (!parts[3].equals("00:00")) {

					// tempo minimo permitido do ponto inicial
					startMinDateTime = new GregorianCalendar();
					startMinDateTime.setTimeInMillis(endDate.getTimeInMillis() - twoT.toStandardDuration().getMillis()
							- maxTolerance.toStandardDuration().getMillis());

					// tempo minimo ideal do ponto inicial, sem penalidade
					startDate = startMinDateTime;
//					startDate = new GregorianCalendar();
//					startDate.setTimeInMillis(endDate.getTimeInMillis() - twoT.toStandardDuration().getMillis()
//							- idealTolerance.toStandardDuration().getMillis());


					// tempo maximo do ponto inicial
					startMaxDateTime = new GregorianCalendar();
					startMaxDateTime.setTimeInMillis(endDate.getTimeInMillis() - oneT.toStandardDuration().getMillis());

					// tempo minimo ideal do ponto final
					endDate.add(Calendar.MILLISECOND, (int) (-idealTolerance.toStandardDuration().getMillis()));

					// È feio, mas È apenas para testes
					if (count > totalPairPoints) {
						waitingList.add((ITimeableTransportable) TransportableFactory
								.createObject(new TimeableTransportableRequest(id, TransportableType.Person,
										TimeableTransportableLatLngFactory.createObject(
												new TimeableTransportableLatLngRequest(id, point1, new Timeable(startDate, startMinDateTime, startMaxDateTime), false, true)),
												TimeableTransportableLatLngFactory.createObject(
														new TimeableTransportableLatLngRequest(id, point2, new Timeable(endDate, idealTolerance, idealTolerance), true, true)), isWheelchairUser, isDouble)));
					}

					else {
						idClient_Client.put(id, (ITimeableTransportable) TransportableFactory
								.createObject(new TimeableTransportableRequest(id, TransportableType.Person,
										TimeableTransportableLatLngFactory.createObject(
												new TimeableTransportableLatLngRequest(id, point1, new Timeable(startDate, startMinDateTime, startMaxDateTime), false, true)),
												TimeableTransportableLatLngFactory.createObject(
														new TimeableTransportableLatLngRequest(id, point2, new Timeable(endDate, idealTolerance, idealTolerance), true, true)), isWheelchairUser, isDouble)));
					}
				}
				else {
					// tempo de volta para casa
					dateSplit = parts[4].split(":");

					// tempo do ponto final de volta para casa
					endDate = new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
							dateNow.get(Calendar.DAY_OF_MONTH) + 1, Integer.valueOf(dateSplit[0]), Integer.valueOf(dateSplit[1]), 0);

					endDate.add(Calendar.MILLISECOND, (int) (-idealTolerance.toStandardDuration().getMillis()));

					// tempo minimo ideal do ponto inicial
					startDate = new GregorianCalendar(2013,
							dateNow.get(Calendar.MONTH) + 1,
							dateNow.get(Calendar.DAY_OF_MONTH) + 1, 5, 00, 0);

					// tempo minimo permitido do ponto inicial
					startMinDateTime = new GregorianCalendar();
					startMinDateTime.setTimeInMillis(endDate.getTimeInMillis() - twoT.toStandardDuration().getMillis()
							- unlimitedTolerance.toStandardDuration().getMillis());

					// tempo maximo do ponto inicial
					startMaxDateTime = new GregorianCalendar();
					startMaxDateTime.setTimeInMillis(endDate.getTimeInMillis() - oneT.toStandardDuration().getMillis());

					endMaxDateTime = new GregorianCalendar();
					endMaxDateTime.setTimeInMillis(endDate.getTimeInMillis() + idealTolerance.toStandardDuration().getMillis());

					endMinDateTime = new GregorianCalendar();
					endMinDateTime.setTimeInMillis(endDate.getTimeInMillis() - unlimitedTolerance.toStandardDuration().getMillis());

					// tempo minimo ideal do ponto final
					endDate = new GregorianCalendar(2013,
							dateNow.get(Calendar.MONTH) + 1,
							dateNow.get(Calendar.DAY_OF_MONTH) + 1, 7, 00, 0);

					// È feio, mas È apenas para testes
					if (count > totalPairPoints) {
						waitingList.add((ITimeableTransportable) TransportableFactory
								.createObject(new TimeableTransportableRequest(id, TransportableType.Person,
										TimeableTransportableLatLngFactory.createObject(
												new TimeableTransportableLatLngRequest(id, point1, new Timeable(startDate, startMinDateTime, startMaxDateTime), false, true)),
												TimeableTransportableLatLngFactory.createObject(
														new TimeableTransportableLatLngRequest(id, point2, new Timeable(endDate, endMinDateTime, endMaxDateTime), true, true)), isWheelchairUser, isDouble)));
					}

					else {
						idClient_Client.put(id, (ITimeableTransportable) TransportableFactory
								.createObject(new TimeableTransportableRequest(id, TransportableType.Person,
										TimeableTransportableLatLngFactory.createObject(
												new TimeableTransportableLatLngRequest(id, point1, new Timeable(startDate, startMinDateTime, startMaxDateTime), false, true)),
												TimeableTransportableLatLngFactory.createObject(
														new TimeableTransportableLatLngRequest(id, point2, new Timeable(endDate, endMinDateTime, endMaxDateTime), true, true)), isWheelchairUser, isDouble)));
					}
				}

				System.out.println("client: " + id + ", " + oneT + ", " + isWheelchairUser + ", " + isDouble);
				writer.println("client: " + id + ", " + oneT + ", " + isWheelchairUser + ", " + isDouble);

				System.out.println("start: "
						+ startMinDateTime.getTime() + ","
						+ startDate.getTime() + ", "
						+ startMaxDateTime.getTime());
				writer.println("start: "
						+ startMinDateTime.getTime() + ","
						+ startDate.getTime() + ", "
						+ startMaxDateTime.getTime());

				if (endMinDateTime != null) {
					System.out.println("end: "
							+ endMinDateTime.getTime() + ","
							+ endDate.getTime() + ","
							+ endMaxDateTime.getTime());
					writer.println("end: "
							+ endMinDateTime.getTime() + ","
							+ endDate.getTime() + ","
							+ endMaxDateTime.getTime());
				}
				else {
					System.out.println("end: "
							+ endDate.getTime());
					writer.println("end: "
							+ endDate.getTime());
				}

				// volta para casa
				dateSplit = parts[4].split(":");

				oneT = timeCalculator.getTime(point2, point1);

				twoT = oneT.plus(oneT);

				if (!parts[4].equals("00:00")) {
					// tempo minimo permitido do ponto inicial
					startMinDateTime = new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
							dateNow.get(Calendar.DAY_OF_MONTH) + 1, Integer.valueOf(dateSplit[0]), Integer.valueOf(dateSplit[1]), 0);
					// new GregorianCalendar();
					// startMinDateTime.setTimeInMillis(endDate.getTimeInMillis() - twoT.toStandardDuration().getMillis()
					// - maxTolerance.toStandardDuration().getMillis());

					// tempo minimo ideal do ponto inicial
					startDate = new GregorianCalendar();
					startDate.setTimeInMillis(startMinDateTime.getTimeInMillis() + idealTolerance.toStandardDuration().getMillis());

					// tempo maximo do ponto inicial
					startMaxDateTime = new GregorianCalendar();
					startMaxDateTime.setTimeInMillis(startMinDateTime.getTimeInMillis() + unlimitedTolerance.toStandardDuration().getMillis());

					// tempo maximo do ponto final no retorno, sem limite
					endMaxDateTime = new GregorianCalendar();
					endMaxDateTime.setTimeInMillis(startMinDateTime.getTimeInMillis() + twoT.toStandardDuration().getMillis()
							+ unlimitedTolerance.toStandardDuration().getMillis());

//					// tempo minimo ideal do ponto final
//					endDate = new GregorianCalendar();
//					endDate.setTimeInMillis(startMinDateTime.getTimeInMillis() + twoT.toStandardDuration().getMillis()
//							+ idealTolerance.toStandardDuration().getMillis());

					// tempo minimo do ponto final
					endMinDateTime = new GregorianCalendar();
					endMinDateTime.setTimeInMillis(startMinDateTime.getTimeInMillis() + oneT.toStandardDuration().getMillis());

					// sem penalidade
					endDate = endMinDateTime;

					// È feio, mas È apenas para testes
					if (count > totalPairPoints) {
						waitingList.add((ITimeableTransportable) TransportableFactory
								.createObject(new TimeableTransportableRequest(id, TransportableType.Person,
										TimeableTransportableLatLngFactory.createObject(
												new TimeableTransportableLatLngRequest(id, point1, new Timeable(startDate, startMinDateTime, startMaxDateTime), false, true)),
												TimeableTransportableLatLngFactory.createObject(
														new TimeableTransportableLatLngRequest(id, point2, new Timeable(endDate, endMinDateTime, endMaxDateTime), true, true)), isWheelchairUser, isDouble)));
					}
					else {
						idClient_Client.put(id, (ITimeableTransportable) TransportableFactory
								.createObject(new TimeableTransportableRequest(id, TransportableType.Person,
										TimeableTransportableLatLngFactory.createObject(
												new TimeableTransportableLatLngRequest(id, point1, new Timeable(startDate, startMinDateTime, startMaxDateTime), false, true)),
												TimeableTransportableLatLngFactory.createObject(
														new TimeableTransportableLatLngRequest(id, point2, new Timeable(endDate, endMinDateTime, endMaxDateTime), true, true)), isWheelchairUser, isDouble)));

					}
				}
				else {

					// tempo do compromisso
					dateSplit = parts[3].split(":");

					// tempo minimo permitido do ponto inicial
					startMinDateTime = new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1,
							dateNow.get(Calendar.DAY_OF_MONTH) + 1, Integer.valueOf(dateSplit[0]), Integer.valueOf(dateSplit[1]), 0);

					startMinDateTime.add(Calendar.MILLISECOND, (int) (idealTolerance.toStandardDuration().getMillis()));

					// tempo minimo ideal do ponto inicial
					startDate = new GregorianCalendar();
					startDate.setTimeInMillis(startMinDateTime.getTimeInMillis() + idealTolerance.toStandardDuration().getMillis());

					// tempo maximo do ponto inicial
					startMaxDateTime = new GregorianCalendar();
					startMaxDateTime.setTimeInMillis(startMinDateTime.getTimeInMillis() + unlimitedTolerance.toStandardDuration().getMillis());

					// tempo maximo do ponto final no retorno
					endMaxDateTime = new GregorianCalendar();
					endMaxDateTime.setTimeInMillis(startMinDateTime.getTimeInMillis() + twoT.toStandardDuration().getMillis()
							+ unlimitedTolerance.toStandardDuration().getMillis());

					// tempo minimo do ponto final
					endMinDateTime = new GregorianCalendar();
					endMinDateTime.setTimeInMillis(startMinDateTime.getTimeInMillis() + oneT.toStandardDuration().getMillis());

					// sem penalidade
					endDate = endMinDateTime;

					// È feio, mas È apenas para testes
					if (count > totalPairPoints) {
						waitingList.add((ITimeableTransportable) TransportableFactory
								.createObject(new TimeableTransportableRequest(id, TransportableType.Person,
										TimeableTransportableLatLngFactory.createObject(
												new TimeableTransportableLatLngRequest(id, point1, new Timeable(startDate, startMinDateTime, startMaxDateTime), false, true)),
												TimeableTransportableLatLngFactory.createObject(
														new TimeableTransportableLatLngRequest(id, point2, new Timeable(endDate, endMinDateTime, endMaxDateTime), true, true)), isWheelchairUser, isDouble)));
					}
					else {
						idClient_Client.put(id, (ITimeableTransportable) TransportableFactory
								.createObject(new TimeableTransportableRequest(id, TransportableType.Person,
										TimeableTransportableLatLngFactory.createObject(
												new TimeableTransportableLatLngRequest(id, point1, new Timeable(startDate, startMinDateTime, startMaxDateTime), false, true)),
												TimeableTransportableLatLngFactory.createObject(
														new TimeableTransportableLatLngRequest(id, point2, new Timeable(endDate, endMinDateTime, endMaxDateTime), true, true)), isWheelchairUser, isDouble)));

					}

				}

				System.out.println("client: " + id + ", " + oneT + ", " + isWheelchairUser + ", " + isDouble);
				writer.println("client: " + id + ", " + oneT + ", " + isWheelchairUser + ", " + isDouble);

				System.out.println("start: "
						+ startMinDateTime.getTime() + ","
						+ startDate.getTime() + ", "
						+ startMaxDateTime.getTime());
				System.out.println("end: "
						+ endMinDateTime.getTime() + ","
						+ endDate.getTime() + ","
						+ endMaxDateTime.getTime());
				writer.println("start: "
						+ startMinDateTime.getTime() + ","
						+ startDate.getTime() + ", "
						+ startMaxDateTime.getTime());
				writer.println("end: "
						+ endMinDateTime.getTime() + ","
						+ endDate.getTime() + ","
						+ endMaxDateTime.getTime());

				count++;

			} catch (IOException e) {
				e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
			} catch (ClassNotFoundException e) {
				e.printStackTrace();  // To change body of catch statement use File | Settings | File Templates.
			}
		}
		br.close();

		writer.println("countClients: " + count);

//		Calendar dateNow = Calendar.getInstance();
		Calendar busStartDate = new GregorianCalendar(2013,
				dateNow.get(Calendar.MONTH) + 1,
				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 4, 30, 0);
		Calendar busEndDate = new GregorianCalendar(2013,
				dateNow.get(Calendar.MONTH) + 1,
				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 21, 00, 0);

		IDistanceCalculator calculator = DistanceCalculatorFactory.createObject(new DistanceCalculatorRequest(DistanceType.Real, true));
		ICostCalculator costCalculator = CostCalculatorFactory.createObject(new CostCalculatorRequest(calculator, true, false));
		ITransporterContainer vehicleContainer = (ITransporterContainer) TransporterContainerFactory
				.createObject(new TransporterContainerRequest(costCalculator, waitingList));

		// adiciona veiculos no container
		for (int i = 0; i < quantityBus; i++) {
			vehicleContainer.add((ITimeableTransporter) TransporterFactory.createObject(new TimeableTransporterRequest(busCapacity,
					new Timeable(busStartDate, zeroPeriod, idealTolerance), new Timeable(busEndDate, zeroPeriod, idealTolerance),
					busStartPoint, busEndPoint, timeCalculator)));
		}

		return vehicleContainer;
	}

	static ITransporterContainer changeDeliveryBusinessClientsAndContainer(Map<Integer, ITimeableTransportable> idClient_Client,
			int totalPoints, int quantityBus)
					throws Exception {

		// obtem uma origem com uma lista de pontos aleatorios com periodo entre eles
		Tuple<LatLng, Map<LatLng, Period>> pointSet = Common.CreatePointsWithPeriodDeliveryBusiness(totalPoints);

		int count = 1;
//		Period twoT; // 2T+30min

		Random r = new Random();

		Period definedTolerance = new Period(0, 30, 0, 0);
		Period zeroPeriod = new Period(0);

		Calendar dateNow = Calendar.getInstance();
		Calendar busStartDate = new GregorianCalendar(2013,
				dateNow.get(Calendar.MONTH) + 1,
				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 4, 30, 0);
		Calendar busEndDate = new GregorianCalendar(2013,
				dateNow.get(Calendar.MONTH) + 1,
				dateNow.get(Calendar.DAY_OF_MONTH) + 1, 21, 00, 0);

		LatLng busStartPoint = pointSet.getItem1();
		LatLng busEndPoint = pointSet.getItem1();

		// insere pontos de clientes
		for (LatLng point : pointSet.getItem2().keySet()) {

			// È feio, mas È apenas para testes
			if (count >= totalPoints) {
				break;
			}

//			Calendar startDate = busStartDate;
			Calendar endDate = getDate();
			while ((endDate.getTimeInMillis() - busStartDate.getTimeInMillis()) < pointSet.getItem2().get(point).toStandardDuration().getMillis()) {
				endDate = getDate();
			}

			int maxMin = r.nextInt(1440);

			Period maxTolerance = new Period(0, maxMin + 30, 0, 0);
			if (count % 2 < 1) {
				maxTolerance = new Period(0, maxMin + 30, 0, 0);
			}
			else {
				maxTolerance = new Period(0, count + 30, 0, 0);
			}

			idClient_Client.put(count, (ITimeableTransportable) TransportableFactory
					.createObject(new TimeableTransportableRequest(count, r.nextInt(100) + 1.0, TransportableType.Stock,
							TimeableTransportableLatLngFactory.createObject(
									new TimeableTransportableLatLngRequest(count, busStartPoint, new Timeable(busStartDate, definedTolerance, definedTolerance), false, false)),
									TimeableTransportableLatLngFactory.createObject(
											new TimeableTransportableLatLngRequest(count, point, new Timeable(endDate, maxTolerance, maxTolerance), true, false)))));

			System.out.println("client: " + count + ", " + endDate.getTime() + ", " + maxTolerance);

			count++;
		}

		ITimeCalculator timeCalculator = TimeCalculatorFactory.createObject(new TimeCalculatorRequest(DistanceType.Real, true));

		IDistanceCalculator calculator = DistanceCalculatorFactory.createObject(new DistanceCalculatorRequest(DistanceType.Real, true));
		ICostCalculator costCalculator = CostCalculatorFactory.createObject(new CostCalculatorRequest(calculator, false));
		ITransporterContainer vehicleContainer = (ITransporterContainer) TransporterContainerFactory
				.createObject(new TransporterContainerRequest(costCalculator));

		// adiciona veiculos no container
		for (int i = 0; i < quantityBus; i++) {
			vehicleContainer.add((ITimeableTransporter) TransporterFactory.createObject(new TimeableTransporterRequest(
					new Timeable(busStartDate, zeroPeriod, definedTolerance), new Timeable(busEndDate, zeroPeriod, definedTolerance),
					1000.0, busStartPoint, busEndPoint, timeCalculator)));
		}

		return vehicleContainer;
	}

	static void populateVehicles(Map<Integer, ITimeableTransportable> idClient_Client, ITransporterContainer vehicleContainer, int quantityBus) {
		ITimeableTransporter vehicle;
		// distribui os pontos nos diversos onibus do container
		for (int id : idClient_Client.keySet()) {

			double weight = 0;
			if (idClient_Client.get(id) instanceof ITimeableStock) {
				weight = ((ITimeableStock) idClient_Client.get(id)).getWeight();
			}

			vehicle = getNotFullVehicle(vehicleContainer, quantityBus, weight, 0);

			if (vehicle != null) {
				vehicle.put(id, idClient_Client.get(id));
			}
		}
	}

	static String printResults(ITransporterContainer containerWithOptimisedStrategy, PrintWriter writer) {
		StringBuilder csvString;
		StringBuilder csvStringFinal = new StringBuilder();
		int count = 1;
		int countTransportables = 0;

		writer.println("Cost: " + containerWithOptimisedStrategy.getContainerCost());

		for (ITimeableTransporter transporter : containerWithOptimisedStrategy) {

			csvString = new StringBuilder();
			writer.println();
			writer.println("Trajectory: " + count);
			writer.println("TrajectoryEndDateTime: "
					+ transporter.getCurrentDeliveryTime().getTime());
			writer.println("TrajectoryValidation: "
					+ transporter.getLastValidation());
			writer.println("TransportablesQuantity: "
					+ transporter.getTransportableQuantity());

			countTransportables += transporter.getTransportableQuantity();

			if (transporter instanceof ITruck) {
				writer.println("TotalWeight: "
						+ ((ITruck) transporter).getCurrentWeight());
			}

			for (ITimeableTransportableLatLng monteCarloPoint : transporter.getTrajectory()) {

				String arrivalDateTime = "";

				if (monteCarloPoint.getTransporterArrivalDateTime() != null) {
					arrivalDateTime = monteCarloPoint.getTransporterArrivalDateTime().getTime().toString();
				}

				String currentDateTime = "";

				if (monteCarloPoint.getCurrentDeliveryTime() != null) {
					currentDateTime = monteCarloPoint.getCurrentDeliveryTime().getTime().toString();
				}

				if (transporter instanceof ITruck) {
					writer.println("Id: "
							+ monteCarloPoint.getId()
							+ ", index: "
							+ monteCarloPoint.getIndex()
							+ ", latlng: "
							+ monteCarloPoint.getLatLng()
							+ ", isEnd: "
							+ monteCarloPoint.isEnd()
							+ ", tolerance: "
							+ monteCarloPoint.getTime().getAfterTolerance()
							+ ", time: "
							+ monteCarloPoint.getTime().getDateTime().getTime()
							+ ", currentTime: "
							+ monteCarloPoint.getCurrentDeliveryTime().getTime()
							+ ", arrivalTime: "
							+ arrivalDateTime);
				}
				else {
					writer.println("Id: "
							+ monteCarloPoint.getId()
							+ ", index: "
							+ monteCarloPoint.getIndex()
							+ ", latlng: "
							+ monteCarloPoint.getLatLng()
							+ ", isEnd: "
							+ monteCarloPoint.isEnd()
							+ ", maxTime: "
							+ monteCarloPoint.getTime().getMaxDateTime().getTime()
							+ ", currentTime: "
							+ currentDateTime
							+ ", arrivalTime: "
							+ arrivalDateTime);
				}

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

			writer.println("TransportablesTotalQuantity: "
					+ countTransportables);

			csvStringFinal.append("var latLongs")
			.append(Integer.toString(count)).append(" = [")
			.append(csvString).append("];\n").append("traceRoute(")
			.append("latLongs").append(Integer.toString(count))
			.append(",get_random_color())\n\n");
			count++;

		}

		writer.println("Caio: It's ended: Now we plot");
		writer.flush();
		return csvStringFinal.toString();
	}

	@SuppressWarnings("null")
	public static String start(Integer quantityClients, Integer quantityBus,
			Integer busCapacity) throws Exception {

		// Map de clientes
		Map<Integer, ITimeableTransportable> idClient_Client = new HashMap<Integer, ITimeableTransportable>();

		List<ITimeableTransportable> waitingList = new ArrayList<ITimeableTransportable>();

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd_HH-mm-ss");
		String date = dateFormat.format(new Date());
		PrintWriter writer = new PrintWriter(date + "routeExecutor" + ".txt");
		writer.println("quantityClients: " + quantityClients +
				"quantityBus: " + quantityBus +
				"busCapacity: " + busCapacity);

//		ITransporterContainer vehicleContainer = changeSPTransClientsAndContainer(idClient_Client, waitingList, totalPairPoints, quantityBus);
//		ITransporterContainer vehicleContainer = changeDeliveryBusinessClientsAndContainer(vehicleContainer, idClient_Client, 90, quantityBus);
//		ITransporterContainer vehicleContainer = changeSPTransClientsAndContainerTest(vehicleContainer, idClient_Client, 14, 1);
		ITransporterContainer vehicleContainer = changeSPTransClientsAndContainerFromSPTransData(idClient_Client, waitingList, quantityClients, quantityBus, busCapacity, writer);

		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("------------------------");
		System.out.println("Salve empty Container? s/n");
		String response = bufferRead.readLine();
		if (response.equals("s")) {
			try
			{
				FileOutputStream fileOut = new FileOutputStream("vehicleContainerEmpty" + quantityClients + "-" + waitingList.size() + ".ttt");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(vehicleContainer);
				out.close();
				fileOut.close();
			} catch (IOException i)
			{
				i.printStackTrace();
			}
		}

		System.out.println("Salve clients? s/n");
		response = bufferRead.readLine();
		if (response.equals("s")) {
			try
			{
				FileOutputStream fileOut = new FileOutputStream("clients" + quantityClients + "-" + waitingList.size() + ".ttt");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(idClient_Client);
				out.close();
				fileOut.close();
			} catch (IOException i)
			{
				i.printStackTrace();
			}
		}
		System.out.println("------------------------");

		boolean hasStartpoint = true;

		populateVehicles(idClient_Client, vehicleContainer, quantityBus);

//		System.out.println("------------------------");
//		System.out.println("Salve Container? s/n");
//		String response = bufferRead.readLine();
//		if (response.equals("s")) {
//			try
//			{
//				FileOutputStream fileOut = new FileOutputStream("vehicleContainer.ttt");
//				ObjectOutputStream out = new ObjectOutputStream(fileOut);
//				out.writeObject(vehicleContainer);
//				out.close();
//				fileOut.close();
//			} catch (IOException i)
//			{
//				i.printStackTrace();
//			}
//		}
//		System.out.println("------------------------");

		IMonteCarloDecisionRule decisionRule = MonteCarloDecisionRuleFactory.createObject(new MonteCarloDecisionRuleRequest(0.0, 0.005));
		IMonteCarlo monteCarlo = MonteCarloFactory.createObject(new MonteCarloRequest(decisionRule, vehicleContainer,
				PermutatorFactory.createObject(new PermutatorRequest(hasStartpoint))));

		try {
			System.out.println("Start: " + GregorianCalendar.getInstance().getTime());
			writer.println("Start: " + GregorianCalendar.getInstance().getTime());
			ITransporterContainer containerWithOptimisedStrategy = monteCarlo.run();
			System.out.println("End: " + GregorianCalendar.getInstance().getTime());
			writer.println("End: " + GregorianCalendar.getInstance().getTime());

			RouteExecutor2.printResults(containerWithOptimisedStrategy, writer);

			// segunda fase
			IDistanceCalculator distanceCalculator = DistanceCalculatorFactory.createObject(new DistanceCalculatorRequest(DistanceType.Real, true));
			ICostCalculator costCalculator = CostCalculatorFactory.createObject(new CostCalculatorRequest(distanceCalculator, true, false));
			containerWithOptimisedStrategy.setCostCalculator(costCalculator);
			containerWithOptimisedStrategy.getWaitingList().clear();

			monteCarlo = MonteCarloFactory.createObject(new MonteCarloRequest(decisionRule, containerWithOptimisedStrategy,
					PermutatorFactory.createObject(new PermutatorRequest(hasStartpoint))));

			System.out.println("SecondStart: " + GregorianCalendar.getInstance().getTime());
			writer.println("SecondStart: " + GregorianCalendar.getInstance().getTime());
			containerWithOptimisedStrategy = monteCarlo.run();
			System.out.println("SecondEnd: " + GregorianCalendar.getInstance().getTime());
			writer.println("SecondEnd: " + GregorianCalendar.getInstance().getTime());
			// fim da segunda fase

			bufferRead.close();

			String result = printResults(containerWithOptimisedStrategy, writer);
			writer.close();

			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		bufferRead.close();
		writer.close();
		return null;

	}
}
