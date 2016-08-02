# Uni Transit Library

An Android transit library for any shuttle service powered by Synchromatics.  Quickly deploy an app for your school with nearby and live-map features built natively for Android. 

## Screenshots and GIFs

TODO 

## Setup

TODO

## Data and Endpoints
As mentioned in the description, this lib is actually compatible with a large number of colleges and universities, all using Synchromatics tracking system.  The library polls the JSON files via Retrofit and parses them for use.  If you wish to use the bus data for another app or bus feed, the endpoints are as such:

* http://www.ucishuttles.com/Region/0/Routes
* http://www.ucishuttles.com/Route/{ROUTE_ID}/Direction/0/Stops
* http://www.ucishuttles.com/Route/{ROUTE_ID}/Stop/{STOP_ID}/Arrivals
* http://www.ucishuttles.com/Route/{ROUTE_ID}/Vehicles

The Intent Service first pulls data from Routes, and uses the route ID to find the stops.  The stop ID is then used to find each individual stop's arrival times.  Vehicle/shuttle data is also pulled from using the route ID.  If these endpoints were to change, they were originally extracted through ucishuttles.com, but can be done through any live feed map.

##Libraries Used:

* Retrofit - http://square.github.io/retrofit/

* GsonConverter

* Gson

* LocationServices

* GoogleMaps 

##License:

UniTransit-Lib is released under the <a href="https://github.com/tripleducke/UniTransit-Lib/blob/master/LICENSE">MIT License</a>.
