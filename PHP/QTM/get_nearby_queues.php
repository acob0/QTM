<?php
	 require_once("config.php");
	 
	 $connection = connect();
	 
	 if(isset($_POST['long']) && isset($_POST['lat'])){
		 $long = $_POST['long'];
		 $lat = $_POST['lat'];
		 
		 $sql = "SELECT * FROM spotify_queues";
		 $result = mysqli_query($connection, $sql);
		 
		 while($object = mysqli_fetch_assoc($result)){
			 $in = array($lat, $long);
			 $fi = array($object["latitude"], $object["longitude"]);
			 
			 if(calculateDistance($in, $fi) < 3000){
				 echo $object["name"] . "¦";
			 }
		 }
	 } else{
		 echo "notset";
	 }

	 function calculateDistance($ini, $fin) {
		 $R = 6371000; // km
		 $dLat = deg2rad($fin[0]-$ini[0]);
		 $dLon = deg2rad($fin[1]-$ini[1]);
		 $a = sin($dLat/2) * sin($dLat/2) + cos(deg2rad($ini[0])) * cos(deg2rad($fin[0])) * sin($dLon/2) * sin($dLon/2);
		 $c = 2 * atan2(sqrt($a), sqrt(1-$a));
		 $d = $R * $c;
		 return $d;
	 }
	 /*
	 $long = -0.010062;
		 $lat = 51.5491184;
		 
		 $sql = "SELECT * FROM spotify_queues";
		 $result = mysqli_query($connection, $sql);
		 
		 while($object = mysqli_fetch_assoc($result)){
			 $in = array($lat, $long);
			 $fi = array($object["latitude"], $object["longitude"]);
			 
			 if(calculateDistance($in, $fi) < 3000){
				 echo $object["name"] . "¦";
			 }
		 }
		 */
?>