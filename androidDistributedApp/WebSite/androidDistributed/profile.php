<?php session_start();


if( !isset($_SESSION['session_username']))
{
	print('<p class="error"> Sorry, you have to login first </p>');
	die();
}
?>

<?php require_once('/var/www/androidDistributed/config.php'); ?>
<?php require_once(MYSQL); ?>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title> User Profile </title>
<link rel="stylesheet" type="text/css" href="style/profile.css" />
</head>

<body>
<?php include("includes/header.html"); ?>

<?php
	echo('<div id="wrapper_options">
		<div id="options">
			<p> Hi, <b><font size="3">'. $_SESSION['username'].'</font></b></p>
			<a href="edit_profile.php">Edit Profile</a>
			</br>
			<a href="logout.php">Logout</a>
	     	</div>
	      </div>'); 
?>

<?php

echo '
<div id="wrapper_up">';

echo '
	<div id="infos">
		<b>Username: </b><font size="3">'. $_SESSION['username'] . '</font></br>
		<b>FirstName: </b><font size="3">'. $_SESSION['firstname'] . '</font></br>
		<b>LastName: </b><font size="3">'. $_SESSION['lastname'] . '</font></br>
		<b>Email: </b><font size="3">'. $_SESSION['email'] . '</font></br>
	</div>
</div>';

echo '
<div id="wrapper_down">  
	<div id="upload_button">
		<form action="upload.php" method="post">
			<input type="submit" value="Upload Experiment"/>
		</form>
	</div>';


echo '<div id="pics">
	<p> <b>My Experiments </b> </p>';

$username = $_SESSION['email'];

$dbc = db_connect(); // connect to db
								// query to db to take user experiments
$query='select * from experiments where user_email="'. $username .'"';
$result = mysqli_query($dbc, $query) or trigger_error("Query: $query\n<br/> MySQL Error: ". mysqli_error($dbc) );
			// if query has results
if(mysqli_num_rows($result) != 0)
{								
	for($i=1; $i<=mysqli_num_rows($result); $i++)
	{
		$row = mysqli_fetch_array($result);

		echo '<div>';
		echo '<a href="view_experiment.php?experiment_id='.$row['id'].'"> <img src="images/plugins.png"> </a>';
		echo $row['name'];
		echo '</div>';
	}
}
else
{
	echo '<p class="error"> No uploaded experiments </p> ';
}

echo '</div>
</div>';

mysqli_close($dbc);

?>

<?php include("includes/footer.html"); ?>
</body>
</html>
