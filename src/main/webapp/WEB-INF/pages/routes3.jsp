<html>
	<script type="text/javascript" src="http://services.maplink.com.br/maplinkapi2/api.ashx?v=4&key=ymUFb0RjwnO6N1zXGKpFauF1vwLPTLvrGLSvaBVybCBPTM9qGBu9QR=="></script>
    <link rel="stylesheet" type="text/css" href="http://www.maplink.com.br/Content/Layoutv2.css" media="screen" />
    <body>
      <div id="divMap" style="width: 700px; height: 600px; border: solid 1px black;"></div>
      <div id="divInfo"></div>
      <div id="divInfo2"></div>
   </body>
	<script type="text/javascript">
		
			var divIdName = "divMap";
				var map = new MMap2(divIdName);

				${route}


            var xys = latLongs1;
            var Distance = function (xys,optmize,divInfo,color ){





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
                routeDetails.optimizeRoute = optmize;
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




                routeOptions.routeDetails = routeDetails;
                var routeManager = null;
                var routeManager = new MRouteMannager(map, color);
                var x = divInfo;
                routeManager.createRoute(routeStops1, routeOptions, null, function (response) {
                            console.log(x);
                            function getDistance(routeSummaryResponse) {

                                return routeSummaryResponse.totalDistance;
                            }

                            function getDistanceNew(routeSummaryResponse) {

                                return routeSummaryResponse.totalDistance;
                            }

                            function trunc (n) {
                                return n | 0;
                            }


                            var distance = getDistance(response.routeTotals);
                            var distanceNew = getDistanceNew(response.routeTotals);

                            document.getElementById(divInfo).innerHTML = trunc(distance) + " kms   >> "
                                    +  trunc(distanceNew) + " kms " +
                                    (distance-distanceNew)/distance ;
                        }
                );

            }

            Distance(xys,true,"divInfo",'#FF0000');
            setTimeout(function() {
                Distance(xys,false,"divInfo2",'#0000FF');
            } , 20000);


    </script>
	
		</body>
</html>
