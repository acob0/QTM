<?php
	 require_once("config.php");
	 
	 $connection = connect();
	 
	 if(isset($_POST['first_name']) && isset($_POST['surname']) && isset($_POST['email']) && isset($_POST['age']) && isset($_POST['password'])){
		  $result='';
		  $first_name = $_POST['first_name'];
		  $surname = $_POST['surname'];
	   	  $email = $_POST['email'];
		  $age = $_POST['age'];
          $password = $_POST['password'];
		  
		  // Query database for row exist or not
          $sql = "INSERT INTO users(first_name, surname, age, email, passwordHash) 
		  VALUES('" . $first_name . "', '" . $surname . "', '" . $age . "', " . $email . ", '" . $password . "');";
		  $stmt = mysqli_query($connect, $sql);
		  $subject = mysqli_fetch_assoc($stmt);
		  
		  return 'Set';
	 }
	 else{
		 return 'Notset';
	 }
?>