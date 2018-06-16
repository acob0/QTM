<?php
	 require_once("config.php");
	 
	 $connection = connect();
	 
	 // Check whether username or password is set from android	
     if(isset($_POST['email']) && isset($_POST['password'])){
		  // Innitialize Variable
		  $result='';
	   	  $email = $_POST['email'];
          $password = $_POST['password'];
		  
		  // Query database for row exist or not
          $sql = "SELECT * FROM users WHERE  email = '" . $email . "' AND passwordHash ='" . $password . "'";
		  $stmt = mysqli_query($connection, $sql);
		  $subject = mysqli_fetch_assoc($stmt);
		  
		  if($subject['email'] == $email && $subject['pass'] == $password)
          {
			 $result="true";	
          }  
          else
          {
			  	$result="false";
          }
		  
		  // send result back to android
   		  echo $result;
  	}
	
	/*$result='';
	   	  $email = "admin@admin.com";
          $password = "admin";
		  
		  // Query database for row exist or not
          $sql = "SELECT * FROM users WHERE  email = '" . $email . "' AND pass ='" . $password . "'";
		  $stmt = mysqli_query($connection, $sql);
		  $subject = mysqli_fetch_assoc($stmt);
		  
		  if($subject['email'] == $email && $subject['pass'] == $password && $subject['admin'] == 1){
		      $result="admin";
		  }
          elseif($subject['email'] == $email && $subject['pass'] == $password)
          {
			 $result="true";	
          }  
          else
          {
			  	$result="false";
          }
		  
		  // send result back to android
   		  echo $result;*/
	
	disconnect($connection);
?>