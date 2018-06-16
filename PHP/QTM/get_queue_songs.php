<?php
	 require_once("config.php");
	 
	 $connection = connect();
	 
	 if(isset($_POST['queue_name'])){
		 $queue_name = $_POST['queue_name'];
		 
		 $sql = "SELECT * FROM " . $queue_name . "_song_queue;";
		 $result = mysqli_query($connection, $sql);
		 
		 while($subject = mysqli_fetch_assoc($result)){
			 echo $subject["song_uri"] . "¦";
		 }
	 }else{
		 echo "notset";
	 }
	 /*
	 		 $queue_name = "lol";
		 
		 $sql = "SELECT * FROM " . $queue_name . "_song_queue;";
		 $result = mysqli_query($connection, $sql);
		 
		 while($subject = mysqli_fetch_assoc($result)){
			 echo $subject["song_uri"] . "¦";
		 }*/
?>