<?php
	 require_once("config.php");
	 
	 $connection = connect();
	 
	 if(isset($_POST['id']) && isset($_POST['display_name']) && isset($_POST['email']) && isset($_POST['birthdate']) && isset($_POST['country']) && isset($_POST['product'])){
		  $id = $_POST['id'];
		  $display_name = $_POST['display_name'];
	   	  $email = $_POST['email'];
		  $birthdate = $_POST['birthdate'];
          $country = $_POST['country'];
		  $product = $_POST['product'];
		  
		  $sql_check = "SELECT * FROM spotify_users WHERE id = '" . $id . "';";
		  $stmt_check = $stmt = mysqli_query($connection, $sql_check);
		  $subject = mysqli_fetch_assoc($stmt_check);
		  
		  if(empty($subject)){
		  
          $sql = "INSERT INTO spotify_users(id, display_name, email, birthdate, country, product) 
		  VALUES('" . $id . "', '" . $display_name . "', '" . $email . "', '" . $birthdate . "', '" . $country . "', '" . $product . "');";
		  $stmt = mysqli_query($connection, $sql);
		  
		  echo 'Set';
		  }
		  else{
			  echo 'Cool B)';
		  }
	 }
	 else{
		 echo 'Notset';
	 }
	 //TEST CODE
	 /*$id = 1;
		  $display_name = 'me';
	   	  $email = 'me@me.com';
		  $birthdate = '03-05-1996';
          $country = 'GB';
		  $product = 'free';
		  
		  $sql_check = "SELECT * FROM spotify_users WHERE id = '" . $id . "';";
		  $stmt_check = $stmt = mysqli_query($connection, $sql_check);
		  $subject = mysqli_fetch_assoc($stmt_check);
		  
		  if(empty($subject)){
		  
          $sql = "INSERT INTO spotify_users(id, display_name, email, birthdate, country, product) 
		  VALUES('" . $id . "', '" . $display_name . "', '" . $email . "', '" . $birthdate . "', '" . $country . "', '" . $product . "');";
		  $stmt = mysqli_query($connection, $sql);
		  
		  echo 'Set';
		  }
		  else{
			  echo 'Cool B)';
		  }*/
?>