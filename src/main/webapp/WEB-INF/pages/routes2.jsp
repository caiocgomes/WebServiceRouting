<%--
  Created by IntelliJ IDEA.
  User: caio
  Date: 29/05/13
  Time: 19:21
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<link rel="stylesheet" type="text/css" href="http://www.maplink.com.br/Content/Layoutv2.css" media="screen" />
<script type="text/javascript" src="http://services.maplink.com.br/maplinkapi2/api.ashx?v=4&key=yJUmbw2jPupFdCg2ymUFa0RkaDOFbK9myKOQT02kaDOFbK9myKOQT03zvLvPSm3g"></script>
<body>
<div id="divMap" style="width: 700px; height: 600px; border: solid 1px black;"></div>
<!--div id="divInfo"></div-->

<br>
<form method="get" action="http://localhost:8080">
    <button type="submit">Voltar</button>
</form>
</body>
<script type="text/javascript">

var divIdName = "divMap";
var map = new MMap2(divIdName);



function get_random_color() {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.round(Math.random() * 15)];
    }
    return color;
}

function traceRoute(xys, colour)
{
    var i=0;
    var routeStops1 = xys.map(function(el){
        var pt = new MPoint(el[0], el[1]);
        var routePoint = new MRoutePoint();
        var rs = new MRouteStop();
        rs.description = i;
        rs.point = pt;
        routePoint.routeStop = rs;
        i=i+1;
        return routePoint;
    });


    var routeDetails = new MRouteDetails();
    routeDetails.optimizeRoute = false;
    routeDetails.descriptionType = 0;
    routeDetails.routeType = 1;

    var vehicle = new MVehicle();
    vehicle.tankCapacity = 20;
    vehicle.averageConsumption = 9;
    vehicle.fuelPrice = 3;
    vehicle.averageSpeed = 60;
    vehicle.tollFeeCat = 2;

    var routeOptions = new MRouteOptions();
    routeOptions.language = "portugues";
    routeOptions.routeDetails = routeDetails;
    routeOptions.vehicle = vehicle;

    var routeManager = new MRouteMannager(map, colour);
    var result = null;
    //routeManager.createRoute(routePoints, routeOptions, null,function(res){result = res;});
    routeManager.createRoute(routeStops1, routeOptions, null, function (response) {
                function getDistance(routeSummaryResponse) {

                    var distance = 0;

                    var pi = Math.PI;

                    var mult = pi * 6400 * 2 / 360;

                    for (var i = 1; i < routeSummaryResponse.length; i++) {
                        var dx = (routeSummaryResponse[i].point.x - routeSummaryResponse[i-1].point.x);
                        var dy = (routeSummaryResponse[i].point.y - routeSummaryResponse[i-1].point.y);
                        distance =  distance + Math.sqrt( dx*dx + dy*dy);
                    }
                    return distance * mult;
                }

                function getDistanceNew(routeSummaryResponse) {

                    var distance = 0;

                    var pi = Math.PI;

                    var mult = pi * 6400 * 2 / 360;

                    for (var i = 1; i < routeSummaryResponse.length; i++) {
                        var dx = (xys[i][0]- xys[i-1][0]);
                        var dy = (xys[i][1]- xys[i-1][1]);
                        distance =  distance + Math.sqrt( dx*dx + dy*dy);
                    }
                    return distance * mult;
                }

                function trunc (n) {
                    return n | 0;
                }


                var distance = getDistance(response.routeSummary);
                var distanceNew = getDistanceNew(response.routeSummary);

                var improv = (distance-distanceNew)/distance;
                improv = trunc(improv*100)/100;

                document.getElementById("divInfo").innerHTML = trunc(distance) + " kms   >> "
                        +  trunc(distanceNew) + " kms " +
                        "improvment " + improv ;
            }
    );


}
${route}

//traceRoute(latLongs1, '#FF0000')
//traceRoute(latLongs2, '#00FF00')
//traceRoute(latLongs3, '#0000FF')



</script>

</body>
</html>
