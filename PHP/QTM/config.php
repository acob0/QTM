<?php

//QTMroot xEyRDjpCF88v_vrC

define("DBSERVER", "localhost");
define("DBUSER", "id5438925_qtmroot");
define("DBPASS", "xEyRDjpCF88v_vrC");
define("DBNAME", "id5438925_qtm");

function connect(){
	$connection = mysqli_connect(DBSERVER, DBUSER, DBPASS, DBNAME);
	return $connection;
}

function disconnect($connection){
		if(isset($connection)){
		mysqli_close($connection);
	}
}

?>