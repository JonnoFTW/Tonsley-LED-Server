Tonsley LED Server
==================

A server that is meant to be used from matlab to run game of life. It will
take connections from the app, and output them on the board. Run the `Tonsley_Game_of_Life3.m`
file in matlab to start the server.

The file format is:

````
x
y
color
rows
````

Where:

* x,y is the x,y coordinates to add the new thing to the board.
* color is the color of the thing you want to put on the board 0 for red, 1 for green and 2 for blue
* rows is a column ordered board with 0 or 1 for on/off states separated by commas. Columns are separated by `-` and 
terminated by a `.`.

You will need to update the value in the matlab file to point it to the location of the java class files for this project.
