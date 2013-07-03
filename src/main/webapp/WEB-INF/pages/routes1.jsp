<html>
	<script type="text/javascript" src="http://services.maplink.com.br/maplinkapi2/api.ashx?v=4&key=ymUFb0RjwnO6N1zXGKpFauF1vwLPTLvrGLSvaBVybCBPTM9qGBu9QR=="></script>
	<body>
      <div id="divMap" style="width: 700px; height: 600px; border: solid 1px black;"></div>
      <div id="divInfo"></div>
   </body>
	<script type="text/javascript">
		
			var divIdName = "divMap";
				var map = new MMap2(divIdName);

				${route}

				
                var xys = latLongs1;
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
				routeDetails.optimizeRoute = true;
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

				var routeManager = new MRouteMannager(map, '#FF0000');
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
					
					document.getElementById("divInfo").innerHTML = trunc(distance) + " kms   >> " 
						+  trunc(distanceNew) + " kms " + 
							(distance-distanceNew)/distance ;
					}
				);
		
	</script>
	
		</body>
</html>
