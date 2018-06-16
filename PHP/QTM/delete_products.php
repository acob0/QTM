<?php
	 require_once("config.php");
	 
	 $connection = connect();
		  
	if(isset($_POST['product_name']){
		$product_name = $_POST['product_name'];
		
		$sql = "DELETE FROM products WHERE name = '" . $product_name . "' LIMIT 1";
		  $stmt = mysqli_query($connection, $sql);
	}
?>
