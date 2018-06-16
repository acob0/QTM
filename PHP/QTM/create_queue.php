<?php
	 require_once("config.php");
	 
	 $connection = connect();
	 
	 if(isset($_POST['queue_name'])){
		 $queue_name = $_POST['queue_name'];
		 $sql = "SELECT COUNT(*) FROM spotify_queues WHERE name = '" . $queue_name . "';";
		 $stmt = mysqli_query($connection, $sql);
		 $subject = mysqli_fetch_row($stmt);
		  
		 if($subject[0] == 0 && isset($_POST['duplicate_songs']) && isset($_POST['password']) 
			 && isset($_POST['user']) && isset($_POST['longitude']) && isset($_POST['latitude'])){
			 $duplicate_songs = $_POST['duplicate_songs'];
			 $password = $_POST['password'];
			 $user = $_POST['user'];
			 $longitude = $_POST['longitude'];
			 $latitude = $_POST['latitude'];
			 $date = date('Y-m-d H:i:s');
			 
			 $sql1 = "CREATE TABLE " . $queue_name . "_queue_details (
			 duplicate_songs VARCHAR(3) NOT NULL,
			 pass VARCHAR(50));";
			 $sql2 = "CREATE TABLE " . $queue_name . "_song_queue (
			 id INT(5) PRIMARY KEY AUTO_INCREMENT,
			 song_uri VARCHAR(100));";
			 $sql3 = "INSERT INTO spotify_queues (name, date_updated, host, longitude, latitude)
			 VALUES('" . $queue_name . "', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY), '" . $user . "', '" . $longitude . "', '" . $latitude . "');";
			 $sql4 = "INSERT INTO " . $queue_name . "_queue_details(duplicate_songs, pass)
			 VALUES('" . $duplicate_songs . "', '" . $password . "');";
			 $sql5 = "SELECT * FROM spotify_queues
			 WHERE date_updated <= CONVERT('" . $date . "', datetime);";
			 
			 mysqli_query($connection, $sql1);
			 mysqli_query($connection, $sql2);
			 mysqli_query($connection, $sql3);
			 mysqli_query($connection, $sql4);
			 $stmt1 = mysqli_query($connection, $sql5);
			 
			 
			 while($queues_delete = mysqli_fetch_assoc($stmt1)){
				 $sql_delete_queue1 = "DROP TABLE IF EXISTS " . $queues_delete["name"] . "_queue_details;";
				 $sql_delete_queue2 = "DROP TABLE IF EXISTS " . $queues_delete["name"] . "_song_queue;";
				 $sql_delete_queue3 = "DELETE FROM spotify_queues WHERE name = '" . $queues_delete["name"] . "';";
				 $stmt = mysqli_query($connection, $sql_delete_queue1);
				 $stmt = mysqli_query($connection, $sql_delete_queue2);
				 $stmt = mysqli_query($connection, $sql_delete_queue3);
			 }
			 
			 echo "done";
		 }
		 else{
			 echo "invalid";
		 }
	 }
	 else{
		 echo "notset";
	 }
	 
	 /*$queue_name = "testQueue6";
		 $sql = "SELECT COUNT(*) FROM spotify_queues WHERE name = '" . $queue_name . "';";
		 $stmt = mysqli_query($connection, $sql);
		 $subject = mysqli_fetch_row($stmt);
		 
	 if($subject[0] == 0){
	 $duplicate_songs = "false";
			 $password = "Mypass";
			 $user = "user";
			 $longitude = "5";
			 $latitude = "6";
			 $date = date('Y-m-d H:i:s');
			 $newdate = strtotime('-3 day', strtotime($date));
			 $newdate = date('Y-m-d H:i:s', $newdate);
			 $sql1 = "CREATE TABLE " . $queue_name . "_queue_details (
			 duplicate_songs VARCHAR(3) NOT NULL,
			 pass VARCHAR(50));";
			 $sql2 = "CREATE TABLE " . $queue_name . "_song_queue (
			 song_uri VARCHAR(100));";
			 $sql3 = "INSERT INTO spotify_queues (name, date_updated, host, longitude, latitude)
			 VALUES('" . $queue_name . "', DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 3 DAY), '" . $user . "', '" . $longitude . "', '" . $latitude . "');";
			 $sql4 = "INSERT INTO " . $queue_name . "_queue_details(duplicate_songs, pass)
			 VALUES('" . $duplicate_songs . "', '" . $password . "');";
			 $sql5 = "DELETE FROM spotify_queues
			 WHERE date_updated <= CONVERT('" . $newdate . "', datetime);";
			 $stmt1 = mysqli_query($connection, $sql1);
			 $stmt2 = mysqli_query($connection, $sql2);
			 $stmt3 = mysqli_query($connection, $sql3);
			 $stmt4 = mysqli_query($connection, $sql4);
			 $stmt5 = mysqli_query($connection, $sql5);
	 }else
		 echo "duplicate";*/
?>