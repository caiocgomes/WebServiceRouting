package com.springapp.mvc;


import com.maplink.framework.IMonteCarlo;
import com.maplink.framework.routing.vehiclerouting.DistanceMatrix;
import com.maplink.framework.routing.vehiclerouting.montecarlo.IMonteCarloDecisionRule;
import com.maplink.framework.routing.vehiclerouting.montecarlo.MetropolisRule;
import com.maplink.framework.routing.vehiclerouting.montecarlo.MonteCarlo;
import com.maplink.framework.routing.vehiclerouting.montecarlo.costFunctions.ICostFunction;
import com.maplink.framework.routing.vehiclerouting.montecarlo.costFunctions.MinimizeMaximumTimeAndDistance;
import com.maplink.framework.routing.vehiclerouting.montecarlo.distanceCalculator.DelayedDistanceMatrix;
import com.maplink.framework.routing.vehiclerouting.montecarlo.distanceCalculator.IDistanceComputer;
import com.maplink.framework.routing.vehiclerouting.montecarlo.distanceCalculator.IDistanceMatrix;
import com.maplink.framework.routing.vehiclerouting.montecarlo.distanceCalculator.StraightLineDistance;
import com.maplink.framework.routing.vehiclerouting.montecarlo.permutator.IPermutator;
import com.maplink.framework.routing.vehiclerouting.montecarlo.permutator.PermutingAnnealingPermutator;
import com.maplink.framework.routing.vehiclerouting.montecarlo.transportable.ITransportable;
import com.maplink.framework.routing.vehiclerouting.montecarlo.transportable.SimpleTimeablePerson;
import com.maplink.framework.routing.vehiclerouting.montecarlo.transporter.ITransporter;
import com.maplink.framework.routing.vehiclerouting.montecarlo.transporter.SimpleBus;
import com.maplink.framework.routing.vehiclerouting.montecarlo.transporterConteiner.ITransporterContainer;
import com.maplink.framework.routing.vehiclerouting.montecarlo.transporterConteiner.SimpleVehicleContainer;
import com.maplink.framework.routing.vehiclerouting.webservice.route.Point;
import com.maplink.framework.routing.vehiclerouting.webservice.route.RouteStop;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: caio
 * Date: 24/05/13
 * Time: 18:34
 * To change this template use File | Settings | File Templates.
 */

@Controller
@RequestMapping("/addContact")
public class routes {
    @RequestMapping(method = RequestMethod.POST)
         public String getRoutes(@ModelAttribute("SpringWeb") Route routeParam, ModelMap model){

        routes route = new routes();

        if (routeParam.getNumberBus()*routeParam.getMaximumClientPerBus() >= routeParam.getNumberStops()){
            String ans = route.start(routeParam.getNumberStops(),routeParam.getNumberBus(),routeParam.getMaximumClientPerBus());
            model.addAttribute("route",ans);
            return "routes2";
        }
        else{
            model.addAttribute("info","<emph>não é possivel realizar essa rota com o número de veiculos pedidos</emph>");
            return "redirect:/";
        }

    }

    public String start(Integer numberOfStoppingPoints, Integer numberOfVehicles,Integer maximumClientPerBus){
        //Integer numberOfStoppingPoints = 60;
        //Integer numberOfVehicles = 1;


        String current = null;
        String currentDir = new File(".").getAbsolutePath();
        System.out.println(currentDir);

        DistanceMatrix distanceMatrixObj = new DistanceMatrix();

        //Map<Point,Map<Point,Double>> distanceDict = distanceMatrixObj.DeserializerDistanceMatrix("myobject2.data");

        Map<Point,Map<Point,Double>> distanceDict =  distanceMatrixObj.CreateRandomMatrix(numberOfStoppingPoints + 2);

        /*
        try
        {
            FileOutputStream fileOut =
                    new FileOutputStream("distanceDict.ttt");
            ObjectOutputStream out =
                    new ObjectOutputStream(fileOut);
            out.writeObject(distanceDict);
            out.close();
            fileOut.close();
        }catch(IOException i)
        {
            i.printStackTrace();
        }

        */


        distanceDict = null;




        try {


           // FileInputStream fileIn =
            //        new FileInputStream("distanceDict.ttt");



            //ObjectInputStream in = new ObjectInputStream(fileIn);
            InputStream in = getClass().getResourceAsStream("/distanceDict.ttt");
            ObjectInputStream objectInputStream = new ObjectInputStream(in);

            distanceDict = (Map<Point,Map<Point,Double>> ) objectInputStream.readObject();
            in.close();
            //fileIn.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        Object[] objectPoint = distanceDict.keySet().toArray();
        //Point[] arrayPoint = (Point[])objectPoint;
        Map<Integer, ITransportable> monteCarloObjectsStore = new HashMap<Integer,ITransportable>();

        for (int i = 0 ; i < numberOfStoppingPoints + 2; i++){
            Object pointObj = objectPoint[i];
            //monteCarloObjectsStore.put(i,new SimplePerson(i + 0,(Point)pointObj));
            Calendar date = new GregorianCalendar(2015,1,1);

            monteCarloObjectsStore.put(i,new SimpleTimeablePerson((Point)pointObj,i, date));
        }



        //Integer numberOfStoppingPoints = distanceDict.keySet().size();


        ITransporterContainer vehicleContainer = new SimpleVehicleContainer();

        ITransporter vehicle = null;

        for (int i = 0 ; i < numberOfVehicles ; i++){
            vehicle = new SimpleBus(maximumClientPerBus);
            vehicle.add(monteCarloObjectsStore.get(0));
            vehicle.add(monteCarloObjectsStore.get(numberOfStoppingPoints));
            vehicleContainer.add(vehicle);
        }

        Integer j = 0;
        vehicle = vehicleContainer.get(j);
        for (int i = 1 ; i < numberOfStoppingPoints; i++){
            if (vehicle.isFull()){
                j++;
                vehicle = vehicleContainer.get(j);

            }

            vehicle.add(1,monteCarloObjectsStore.get(i));


        }







        IMonteCarloDecisionRule decisionRule = new MetropolisRule(0.0, 0.005);
        IPermutator permutation = new PermutingAnnealingPermutator();




        IDistanceComputer distanceComputer = new StraightLineDistance();
        //IDistanceComputer distanceComputer = new RealDistance();
        IDistanceMatrix distanceMatrix = new DelayedDistanceMatrix(distanceComputer);


        //ICostFunction costFunction = new TotalDistanceCost(distanceMatrix);


        ICostFunction costFunction = new MinimizeMaximumTimeAndDistance(distanceMatrix);

        vehicleContainer.setContainerCost(costFunction.computeCost(vehicleContainer));
        IMonteCarlo monteCarlo = new MonteCarlo(decisionRule,permutation,costFunction,vehicleContainer);
        ITransporterContainer optimisedStrategy = ((MonteCarlo)monteCarlo).run();

        StringBuilder csvString = new StringBuilder();
        StringBuilder csvStringFinal = null;
        csvStringFinal = new StringBuilder();

        int count = 1;

        for (ITransporter vehicleRoute : (List<ITransporter>)optimisedStrategy){

            List<ITransportable> monteCarloObjectToShow = (List<ITransportable>)vehicleRoute;

            List<RouteStop> routesList = new ArrayList<RouteStop>();

            csvString = new StringBuilder();



            for (ITransportable monteCarloObject : monteCarloObjectToShow){


                StringBuilder csvPoints =  new StringBuilder();
                if (csvString.length() == 0){
                    csvPoints.append("[").append(monteCarloObject.localization().getX()).append(",").append(monteCarloObject.localization().getY()).append("]");
                }
                else{
                    csvPoints.append(",\n[").append(monteCarloObject.localization().getX()).append(",").append(monteCarloObject.localization().getY()).append("]");
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
