<?php session_start(); ?>	

<?php require_once('/var/www/androidDistributed/config.php'); ?>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title> View Report </title>
<link rel="stylesheet" type="text/css" href="style/view.css" />

</head>
<body>

<?php include("includes/header.html"); ?>

<?php
if(isset($_SESSION['username']))
{
	echo('<div id="wrapper_options">
		<div id="options">
			<p> Hi, <b><font size="3">'. $_SESSION['username'].'</font></b></p>
			<a href="profile.php">View Profile</a>
			</br>
			<a href="logout.php">Logout</a>
	     	</div>
	     </div>');
}
?>

<?php

require_once(MYSQL); 


$id = $_GET['experiment_id'];

$dbc = db_connect(); // connect to db

$query='select source_id,value from results where experiment_id='.$id;

$result = mysqli_query($dbc, $query) or trigger_error("Query: $query\n<br/> MySQL Error: ". mysqli_error($dbc) );
			// if have resutls
if(mysqli_num_rows($result) != 0)
{	
	echo('<table border="1" cellspacing="5" cellpadding="5"><tr>');
	for($i=1; $i<=mysqli_num_rows($result); $i++)
	{
		$row = mysqli_fetch_array($result);
		
		echo('<td>');
			echo($row['source_id']);
		echo('</td>');
		echo('<td>');
			echo($row['value']);
		echo('</td>');
			echo('  </tr><tr>');
	}
	echo('  </tr></table>');
}
else
{
	echo '<p class="error"> No results for this experiment </p> ';
}

?>

<?php include('includes/footer.html'); ?> 

</body>
</html>
