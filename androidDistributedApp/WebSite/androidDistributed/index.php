<?php session_start() ?>

<?php require_once('/var/www/config.php'); ?>
<?php require_once(MYSQL); ?>

<html>


<head>
	<title> Android Distributed </title>
	<link rel="stylesheet" type="text/css" href="style/index.css" />
</head>

<?php include('includes/header.html'); ?>

<body>

<?php 

if(isset($_SESSION['session_username']))
{
	echo('<div id="wrapper_options">
		<div id="options">
			<p> Hi, <b><font size="3">'. $_SESSION['username'].' </font></b></p>
			<a href="profile.php">View Profile</a>
			</br>
			<a href="logout.php">Logout</a>
		</div>
	      </div>');
}
else
{
		echo('<div id="options">
		      	<a href="login.php">Login</a>
			</br>
			<a href="register.php">Create Account</a> 
  		     </div>');
}

?>

<?php include('includes/footer.html'); ?> 

</body>
</html>
