<?php require_once('/var/www/androidDistributed/config.php'); ?>
<?php	require_once(MYSQL); ?>
<?php 	require_once('includes/make_user_space.php'); ?>
<?php 	include('includes/create_user.php'); ?>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title> Create Account </title>
<link rel="stylesheet" type="text/css" href="style/register.css" />
<script type="text/javascript" src="includes/check_form_fields.js"></script>
</head>

<body>

<?php
include('includes/header.html'); 
mb_internal_encoding("UTF-8");
include('Reform.inc.php');
?>

<?php

if(isset($_POST['submitted']))
{
	$trimmed = array_map('trim', $_POST); // remove white spaces for start and end

	$firstname = $lastname = $email = $username = $pass = FALSE;

		// prevent someone to store javascript to server and mess up client later
		// converts tags to html entitys
	$reform = new Reform; // OWASP anti-xss class
	$firstname = $reform->HtmlEncode($trimmed['firstname']); 
	$lastname = $reform->HtmlEncode($trimmed['lastname']);
	$email = $reform->HtmlEncode($trimmed['email']);
								// escaping special characters for sql use 
	$firstname = mysqli_real_escape_string($dbc, $firstname);
	$lastname = mysqli_real_escape_string($dbc, $lastname);
	$email = mysqli_real_escape_string($dbc, $email);
	$username = mysqli_real_escape_string($dbc, $trimmed['username']);
	$password = mysqli_real_escape_string($dbc, $trimmed['password']);
	$pass = mysqli_real_escape_string($dbc, $trimmed['password']);
		
	if( $firstname && $lastname && $email && $username && $pass )
	{	
		if( create_user($firstname, $lastname, $email, $username, $pass) )
		{
			echo '<p class=""error> Account created successfully </p>
				<p> Thank you for registering </p>
				<a href="login.php"> Click to login </a>';
		}
		else
		{
			echo '<p class=""error> Can not create account </p>';
		}
	}
	else
	{
		echo '<p class=""error> Please re-enter your infos </p>';
	}
}
?>

<h1> Register </h1>

<div id="wrapper">
<form name="registrationForm" action="register.php" method="post" >
	<fieldset>
		<div id="register_form">
		<p><b>Firstname: </b><input id="firstname" type="text" name ="firstname" size="30" maxlenght="30" onClick="clearField('firstname')" value="<?php if(isset($trimmed['firstname'])) echo $trimmed['firstname']; ?>"/></p>
		<p><b>Lastname: </b><input id="lastname" type="text" name ="lastname" size="30" maxlenght="30" onClick="clearField('lastname')" value="<?php if(isset($trimmed['lastname'])) echo $trimmed['lastname']; ?>"/></p>
		<p><b>Email: </b><input id="email" type="text" name ="email" size="30" maxlenght="40" onClick="clearField('email')" value="<?php if(isset($trimmed['email'])) echo $trimmed['email']; ?>"/></p>
		<p><b>Username: </b><input id="username" type="text" name ="username" size="30" maxlenght="30" onClick="clearField('username')" value="<?php if(isset($trimmed['username'])) echo $trimmed['username']; ?>"/></p>

		<p><b>Password: </b><input id="password" type="password" name ="password" size="30" maxlenght="32" onClick="clearField('password')" /></p>
		<p><b>Confirm password: </b><input id="confirm" type="password" name ="confirm" size="30" maxlenght="32" onClick="clearField('confirm')" /></p>
			<div>
				<input type="submit" name="submit" value="Create account" onClick="return checkFields(this);" />
				<input type="hidden" name="submitted" value="TRUE" />
			</div>
		</div>
		<div id="image">
			<img src="images/register.png">
		</div>
	</fieldset>
</form>

<p> We suggest for your own good: </p>
<p>Password must be at least 6 characters and must contain at least one of the following:
		special characters: ~!@#$%^&*_?.</br>
		numbers: 1,2,3,4,5,6,7,8,9,0</br>
</p>

<?php include('includes/footer.html'); ?>

</body>
</html>
