<?php
	 require_once("config.php");
	 
	 $connection = connect();
	 
	 // Check whether username is set from android	
     if(isset($_POST['user'])){
		  // Innitialize Variable
	   	  $user = $_POST['user'];
		  
		  // Query database for row exist or not
          $sql = "SELECT COUNT(*) FROM spotify_queues WHERE  host = '" . $user . "'";
		  $stmt = mysqli_query($connection, $sql);
		  $subject = mysqli_fetch_row($stmt);
		  
		  echo $subject[0];
  	}
	
	$user = 'user';
		  
		  // Query database for row exist or not
          $sql = "SELECT COUNT(*) FROM spotify_queues WHERE  host = '" . $user . "'";
		  $stmt = mysqli_query($connection, $sql);
		  $subject = mysqli_fetch_row($stmt);
		  
		  echo $subject[0];
	
	disconnect($connection);
?>