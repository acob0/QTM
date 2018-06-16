<?php
	 require_once("config.php");
	 
	 $connection = connect();
	 
	 if(isset($_POST['user'])){
		 $user = $_POST['user'];
		 $sql = "SELECT * FROM spotify_queues WHERE host = '" . $user . "';";
		 $result = mysqli_query($connection, $sql);
		 
		 while($subject = mysqli_fetch_assoc($result)){
			 echo $subject["name"] . "¦";
		 }
	 }else{
		 echo "notset";
	 }
	 
	 $date = date('Y-m-d H:i:s');
	 $sql2 = "SELECT * FROM spotify_queues
			 WHERE date_updated <= CONVERT('" . $date . "', datetime);";
	 $stmt1 = mysqli_query($connection, $sql2);
			 		 
	 while($queues_delete = mysqli_fetch_assoc($stmt1)){
		 $sql_delete_queue1 = "DROP TABLE IF EXISTS " . $queues_delete["name"] . "_queue_details;";
		 $sql_delete_queue2 = "DROP TABLE IF EXISTS " . $queues_delete["name"] . "_song_queue;";
		 $sql_delete_queue3 = "DELETE FROM spotify_queues WHERE name = '" . $queues_delete["name"] . "';";
		 $stmt = mysqli_query($connection, $sql_delete_queue1);
		 $stmt = mysqli_query($connection, $sql_delete_queue2);
		 $stmt = mysqli_query($connection, $sql_delete_queue3);
	 }
	 
	 /*$user = "kenny331";
		 $sql = "SELECT * FROM spotify_queues WHERE host = '" . $user . "';";
		 $result = mysqli_query($connection, $sql);
		 
		 while($subject = mysqli_fetch_assoc($result)){
			 echo $subject["name"] . "¦";
		 }*/
?>