<?php
	 require_once("config.php");
	 
	 $connection = connect();
	 $date = date('Y-m-d H:i:s');

	 if(isset($_POST['uri']) && isset($_POST['queue_name'])){
		 $queue_name = $_POST['queue_name'];
		 $uri = $_POST['uri'];
		 
		 $sql = "INSERT IGNORE INTO " . $queue_name . "_song_queue(song_uri)
		 VALUES('" . $uri . "');";
		 $sql_update = "UPDATE spotify_queues SET date_updated = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY) 
		 WHERE name = '" . $queue_name . "';";
		 mysqli_query($connection, $sql);
		 mysqli_query($connection, $sql_update);
	 } else{
		 echo "notset";
	 }
	 
	 /*$queue_name = "lol";
		 $uri = "lol2";
		 
		 $sql = "INSERT IGNORE INTO " . $queue_name . "_song_queue(song_uri)
		 VALUES('" . $uri . "');";
		 $sql_update = "UPDATE spotify_queues SET date_updated = CONVERT('" . $date . "', datetime) 
		 WHERE name = '" . $queue_name . "';";
		 mysqli_query($connection, $sql);
		 mysqli_query($connection, $sql_update);*/
?>