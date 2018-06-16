<?php
	 require_once("config.php");
	 
	 $connection = connect();
		  
		  // Query database for row exist or not
     $sql = "SELECT * FROM products";
	 $stmt = mysqli_query($connection, $sql);
	 $subject = mysqli_fetch_assoc($stmt);
		  
	 while($subject = mysqli_fetch_assoc($result_set)) {
		echo $subject["name"] . ";";
		echo $subject["description"] . ";";
		echo $subject["price"] . ";";
		echo $subject["imageurl"] . ";";
	}
?>
