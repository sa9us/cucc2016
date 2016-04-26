# DogSensorMonitor
---
## Overview

This project aim to perform a simulation that collecting sensor data of dogs by one server, and provide a web application by another server for visualizing the sensor data, including position, heartbeat, temperature, and so on. 

The web application will automatically create 20 dogs at its start. Users are also allowed to add a new dog by clicking the *Register Dog* button located at the top-left corner of the map. Dogs will only run within a small rectangle at a varied speed which is determined by a Gaussian random function. The heartbeat and temperature will also vary as a dog travels. The faster it runs, the higher heartbeat and temperature will be. Dogs will only travels in straight line. If it hit the boundary of the rectangle, its direction will reflect. All the above varied values will be updated once a second. 

All the information of dogs is provided in this web application. The locations are presented on the map. Other information can be retrieved by clicking the a dog face. To dismiss the detailed information, click the same dog face one more time.

Click the *Show Clustering* button beside *Register Dog* and enter an integer as the number of clusters, the popup dialog will display a table with a column for each cluster displaying the dog’s id and weight.

The server that is responsible for collection sensor data provides several web services:
- Register a new dog: ```POST``` [https://sa9us.herokuapp.com/dogs] 
- Get all dogs: ```GET``` [https://sa9us.herokuapp.com/dogs]
- A blank implementation for DELETE method: ```DELETE``` [https://sa9us.herokuapp.com/dogs]
- Get a specific dog: ```GET``` [https://sa9us.herokuapp.com/dogs/{id}]
- Show dog clustering by KMeans: ```GET``` [https://sa9us.herokuapp.com/dogClusters?k={integer}]
- Get a dogCluster contains dogs' ids and weights: ```GET``` [https://sa9us.herokuapp.com/dogClusters?showWeight&k={integer}]
- A simple visualization of dog position and cluster: ```GET``` [https://sa9us.herokuapp.com/dogClusters/rendered?k={integer}]

Those RESTful services are built depending on Spring Web MVC framework, and should be normally worked on Java 8 environment. 

## Live Demo

Demo Link: [https://dogviz.herokuapp.com]\
The web application is tested on latest Chrome, FireFox, Safari, and an old iPad. 

## Screenshot

![Screenshot](https://github.com/sa9us/cucs2016/blob/master/SCREENSHOT.PNG)

## Deployment

Copy ```DogSensorMonitor.war``` to the ```webapps``` folder of Tomcat in Server #1. Edit Tomcat ```conf/server.xml``` file and add the following line in ```<Host>``` element.
```sh
<Context docBase="DogSensorMonitor" path="" reloadable="true" />
```
Copy ```dogviz``` to the ```webapps``` folder of Tomcat in Server #2. Edit ```dogviz/index.html```, find the first line of ```<body>``` element, and replace the value of the hidden input by the HTTP ROOT of Tomcat of Server #1.

## License

MIT

[https://sa9us.herokuapp.com/dogs]: <https://sa9us.herokuapp.com/dogs>
[https://sa9us.herokuapp.com/dogs/{id}]: <https://sa9us.herokuapp.com/dogs/1>
[https://sa9us.herokuapp.com/dogClusters?k={integer}]: <https://sa9us.herokuapp.com/dogClusters?k=3>
[https://sa9us.herokuapp.com/dogClusters?showWeight&k={integer}]: <https://sa9us.herokuapp.com/dogClusters?showWeight&k=3>
[https://sa9us.herokuapp.com/dogClusters/rendered?k={integer}]: <https://sa9us.herokuapp.com/dogClusters/rendered?k=3>
[https://dogviz.herokuapp.com]: <https://dogviz.herokuapp.com>
