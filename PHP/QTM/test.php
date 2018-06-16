<?php
	 require_once("config.php");
	 
	 $connection = connect();
	 $date = date('Y-m-d H:i:s', strtotime("-3 days"));
	 echo $date;
	 $sql5 = "SELECT * FROM spotify_queues
			 WHERE date_updated <= CONVERT('" . $date . "', datetime);";
			 $stmt5 = mysqli_query($connection, $sql5);
			 
			 
			 while($queues_delete = mysqli_fetch_assoc($stmt5)){
				 $sql_delete_queue1 = "DROP TABLE IF EXISTS " . $queues_delete["name"] . "_queue_details;";
				 $sql_delete_queue2 = "DROP TABLE IF EXISTS " . $queues_delete["name"] . "_song_queue;";
				 $sql_delete_queue3 = "DELETE FROM spotify_queues WHERE name = '" . $queues_delete["name"] . "';";
				 mysqli_query($connection, $sql_delete_queue1);
				 mysqli_query($connection, $sql_delete_queue2);
				 mysqli_query($connection, $sql_delete_queue3);
			 }
?>