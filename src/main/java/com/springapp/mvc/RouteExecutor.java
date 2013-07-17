package com.springapp.mvc;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.joda.time.Period;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import com.maplink.framework.routing.vehiclerouting.classes.Tuple;
import com.maplink.framework.routing.vehiclerouting.location.LatLng;
import com.maplink.framework.routing.vehiclerouting.location.TimeableTransportableLatLng;
import com.maplink.framework.routing.vehiclerouting.montecarlo.IMonteCarlo;
import com.maplink.framework.routing.vehiclerouting.montecarlo.IMonteCarloDecisionRule;
import com.maplink.framework.routing.vehiclerouting.montecarlo.MetropolisRule;
import com.maplink.framework.routing.vehiclerouting.montecarlo.MonteCarlo;
import com.maplink.framework.routing.vehiclerouting.montecarlo.Factories.MonteCarloDecisionRuleFactory;
import com.maplink.framework.routing.vehiclerouting.montecarlo.Factories.MonteCarloFactory;
import com.maplink.framework.routing.vehiclerouting.montecarlo.Requests.MonteCarloDecisionRuleRequest;
import com.maplink.framework.routing.vehiclerouting.montecarlo.Requests.MonteCarloRequest;
import com.maplink.framework.routing.vehiclerouting.costCalculator.ICostCalculator;
import com.maplink.framework.routing.vehiclerouting.costCalculator.MinimizeMaximumTimeAndDistance;
import com.maplink.framework.routing.vehiclerouting.costCalculator.Factory.CostCalculatorFactory;
import com.maplink.framework.routing.vehiclerouting.costCalculator.Requests.CostCalculatorRequest;
import com.maplink.framework.routing.vehiclerouting.distanceCalculator.CachedDistanceCalculator;
import com.maplink.framework.routing.vehiclerouting.distanceCalculator.IDistanceCalculator;
import com.maplink.framework.routing.vehiclerouting.distanceCalculator.StraightLineDistanceCalculator;
import com.maplink.framework.routing.vehiclerouting.distanceCalculator.Factory.DistanceCalculatorFactory;
import com.maplink.framework.routing.vehiclerouting.distanceCalculator.Requests.DistanceCalculatorRequest;
import com.maplink.framework.routing.vehiclerouting.permutator.IPermutator;
import com.maplink.framework.routing.vehiclerouting.permutator.PermutingAnnealingPermutator;
import com.maplink.framework.routing.vehiclerouting.timeableFunctions.ITimeCalculator;
import com.maplink.framework.routing.vehiclerouting.timeableFunctions.Timeable;
import com.maplink.framework.routing.vehiclerouting.timeableFunctions.Factory.TimeCalculatorFactory;
import com.maplink.framework.routing.vehiclerouting.timeableFunctions.Requests.TimeCalculatorRequest;
import com.maplink.framework.routing.vehiclerouting.transportable.ITimeableTransportable;
import com.maplink.framework.routing.vehiclerouting.transportable.ITransportable;
import com.maplink.framework.routing.vehiclerouting.transportable.factory.TransportableFactory;
import com.maplink.framework.routing.vehiclerouting.transportable.requests.TimeableTransportableRequest;
import com.maplink.framework.routing.vehiclerouting.transportable.requests.TransportableRequest;
import com.maplink.framework.routing.vehiclerouting.transporter.ITimeableTransporter;
import com.maplink.framework.routing.vehiclerouting.transporter.ITransporter;
import com.maplink.framework.routing.vehiclerouting.transporter.SimpleBus;
import com.maplink.framework.routing.vehiclerouting.transporter.factory.TransporterFactory;
import com.maplink.framework.routing.vehiclerouting.transporter.requests.TimeableTransporterRequest;
import com.maplink.framework.routing.vehiclerouting.transporterConteiner.ITransporterContainer;
import com.maplink.framework.routing.vehiclerouting.transporterConteiner.SimpleVehicleContainer;
import com.maplink.framework.routing.vehiclerouting.transporterConteiner.Factory.TransporterContainerFactory;
import com.maplink.framework.routing.vehiclerouting.transporterConteiner.Requests.TransporterContainerRequest;
import com.maplink.framework.routing.vehiclerouting.types.DistanceType;
import com.maplink.framework.routing.vehiclerouting.types.TransportableType;
import com.maplink.framework.routing.vehiclerouting.webservice.route.Point;
import com.maplink.framework.routing.vehiclerouting.webservice.route.RouteStop;

@Controller
@RequestMapping("/addContact")
public class RouteExecutor {
	@RequestMapping(method = RequestMethod.POST)
	public String getRoutes(@ModelAttribute("SpringWeb") Route routeParam, ModelMap model) throws Exception{		  

		if (routeParam.getQuantityBus()*routeParam.getBusCapacity() >= routeParam.getQuantityClients()){
			String ans = start(routeParam.getQuantityClients(),routeParam.getQuantityBus(),routeParam.getBusCapacity());
			model.addAttribute("route",ans);
			return "routes2";
		}
		else{
			model.addAttribute("info","<emph>n√£o √© possivel realizar essa rota com o n√∫mero de veiculos pedidos</emph>");
			return "redirect:/";
		}
	}
	
	static Calendar getDate() {		
	
		int hour = randBetween(9, 20);
		int min = randBetween(0, 59);
		int sec = randBetween(0, 59);

		Calendar dateNow = Calendar.getInstance();

		// cria um horario para o dia seguinte
		return new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1, dateNow.get(Calendar.DAY_OF_MONTH) + 1, 
				hour, min, sec);
	}


	static int randBetween(int start, int end) {
		return start + (int)Math.round(Math.random() * (end - start));
	}
	
	static ITimeableTransporter getNotFullVehicle(ITransporterContainer vehicleContainer, int quantityBus) {		
		Random randomGenerator = new Random();
		
		ITimeableTransporter vehicle = vehicleContainer.get(randomGenerator.nextInt(quantityBus));

		if (!vehicle.isFull()) {
			return vehicle;
		}
		
		getNotFullVehicle(vehicleContainer, quantityBus);
		
		return null;
	}

	@SuppressWarnings("unchecked")
	public static String start(Integer quantityClients, Integer quantityBus,Integer busCapacity) throws Exception{
		
		int totalPairPoints = quantityClients + 1; // pares de pontos dos clientes e par de pontos do onibus
		
		// obtem uma lista de pares de pontos aleatorios com periodo entre eles
		Map<LatLng, Tuple<LatLng, Period>> pointSet = Common.CreatePointsWithPeriod(totalPairPoints); 

		// Map de clientes
		Map<Integer, ITimeableTransportable> idClient_Client = new HashMap<Integer,ITimeableTransportable>();
		
		int count = 1;
		Period tolerance = new Period(0, 15, 0, 0); // tolerancia para startPoint e endPoint
		Calendar dateNow = Calendar.getInstance();
		Calendar busStartDate = new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1, dateNow.get(Calendar.DAY_OF_MONTH) + 1, 8, 30, 0);
		Calendar busEndDate = new GregorianCalendar(2013, dateNow.get(Calendar.MONTH) + 1, dateNow.get(Calendar.DAY_OF_MONTH) + 1, 21, 30, 0);
		LatLng busStartPoint = null;
		LatLng busEndPoint = null;
		
		// insere pontos de clientes
		for (LatLng point: pointSet.keySet()){		
			
			// È feio, mas È apenas para testes
			if (count > totalPairPoints)
			{
				busStartPoint = point;
				busEndPoint = pointSet.get(point).getItem1();
			}			

			Calendar startDate = getDate();
			Calendar endDate = new GregorianCalendar();
			endDate.setTimeInMillis(startDate.getTimeInMillis() + pointSet.get(point).getItem2().getMillis());		
			
			idClient_Client.put(count, (ITimeableTransportable)TransportableFactory.CreateObject(new TimeableTransportableRequest(count, point, pointSet.get(point).getItem1(), 
					TransportableType.Person, new Timeable(startDate, tolerance), 
					new Timeable(endDate, tolerance), busStartDate)));	
			
			count++;	
		}
		
		IDistanceCalculator calculator = DistanceCalculatorFactory.CreateObject(new DistanceCalculatorRequest(DistanceType.Real, true));
		ICostCalculator costCalculator = CostCalculatorFactory.CreateObject(new CostCalculatorRequest(calculator));	
		ITransporterContainer vehicleContainer = (ITransporterContainer)TransporterContainerFactory.CreateObject(new TransporterContainerRequest(costCalculator)); 
		
		ITimeCalculator timeCalculator = TimeCalculatorFactory.CreateObject(new TimeCalculatorRequest(DistanceType.Real, true));
		
		// adiciona veiculos no container
		for (int i = 0 ; i < quantityBus ; i++){			
			vehicleContainer.add((ITimeableTransporter)TransporterFactory.CreateObject(new TimeableTransporterRequest(new Timeable(busStartDate, tolerance), 
					new Timeable(busEndDate, tolerance), busStartPoint, busEndPoint, timeCalculator)));
		}
		
		ITimeableTransporter vehicle;		
		// distribui os pontos nos diversos onibus do container
		for (int id: idClient_Client.keySet()){
			
			vehicle = getNotFullVehicle(vehicleContainer, quantityBus);			
			vehicle.add(new TimeableTransportableLatLng(id, idClient_Client.get(id).getStartPoint(), idClient_Client.get(id).getStartPointInfo(), false));
			vehicle.add(new TimeableTransportableLatLng(id, idClient_Client.get(id).getEndPoint(), idClient_Client.get(id).getEndPointInfo(), true));
		}	
				
		IMonteCarloDecisionRule decisionRule = MonteCarloDecisionRuleFactory.CreateObject(new MonteCarloDecisionRuleRequest(0.0, 0.005));
		IMonteCarlo monteCarlo = MonteCarloFactory.CreateObject(new MonteCarloRequest(decisionRule, vehicleContainer, new PermutingAnnealingPermutator()));
		ITransporterContainer containerWithOptimisedStrategy = monteCarlo.run();

		StringBuilder csvString;
		StringBuilder csvStringFinal = new StringBuilder();
		count = 1;

		for (ITimeableTransporter vehicleRoute : containerWithOptimisedStrategy){

//			List<TimeableTransportableLatLng> monteCarloObjectToShow = (List<TimeableTransportableLatLng>)vehicleRoute;
//			List<RouteStop> routesList = new ArrayList<RouteStop>();
			csvString = new StringBuilder();

			for (TimeableTransportableLatLng monteCarloPoint : vehicleRoute){
				StringBuilder csvPoints =  new StringBuilder();
				if (csvString.length() == 0){
					csvPoints.append("[").append(monteCarloPoint.getLatLng().getLng()).append(",").append(monteCarloPoint.getLatLng().getLat()).append("]");
				}
				else{
					csvPoints.append(",\n[").append(monteCarloPoint.getLatLng().getLng()).append(",").append(monteCarloPoint.getLatLng().getLat()).append("]");
				}
				
				csvString.append(csvPoints);
			}

			csvStringFinal.append("var latLongs").append(Integer.toString(count)).append(" = [").append(csvString).append("];\n").append("traceRoute(").append("latLongs").append(Integer.toString(count)).append(",get_random_color())\n\n");
			count ++;

		}
		System.out.println("Caio: It's ended: Now we plot");
		return csvStringFinal.toString();


	}

}
