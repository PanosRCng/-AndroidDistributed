<?php session_start();


if( !isset($_SESSION['session_username']))
{
	print('<p class="error"> Sorry, you have to login first </p>');
	die();
}
?>

<?php 
require_once('/var/www/androidDistributed/config.php');
mb_internal_encoding("UTF-8");
include('Reform.inc.php');
?>

<html>
<head>
<title> Edit profile </title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="style/edit_profile.css" />
<script type="text/javascript" src="includes/check_form_fields.js"></script>
</head>

<body>

<?php include('includes/header.html'); ?>

<?php

require_once(MYSQL);

$dbc = db_connect();

if(isset($_POST['submitted']))
{					// remove white space from start and end of infos
	$trimmed = array_map('trim', $_POST);

	$firstname = $lastname = $email = FALSE;

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

	if( $firstname && $lastname && $email ) // if ok
	{							// query to update user profile infos
		$query='update users set firstname="'.$firstname.'", lastname="'.$lastname.'", email="'.$email.'" where email="'. $_SESSION['email'] .'"';
		
		$result = mysqli_query($dbc, $query) or trigger_error("Query: $query \n </br> MYSQL Error: " . mysqli_error($dbc));					// if updated ok
		if(mysqli_affected_rows($dbc) == 1)
		{					// refresh infos to session
			$_SESSION['firstname'] = $firstname;
			$_SESSION['lastname'] = $lastname;
			$_SESSION['email'] = $email;

			echo '<p class="error"> Changes saved </p>';
			echo '<a href="profile.php"> go back to profile </a>';

			mysqli_close($dbc);  // close connection to db

			exit();
		}
		else
		{
			echo '<p class="error"> Changes do not save</p>';
		}		
	}
	else
	{
		echo '<p class="error"> Please re-enter your infos </p>';
	}
}
else		// fetch user profile infos and load them to edit form
{			
	$query='select firstname, lastname, email from users where email="'. $_SESSION['email'] .'"';
		
	$result = mysqli_query($dbc, $query) or trigger_error("Query: $query \n </br> MYSQL Error: " . mysqli_error($dbc));		

	if(mysqli_num_rows($result) == 1)
	{
		$row = mysqli_fetch_array($result);
	}
	else
	{
		echo '<p class="error"> Sorry somthing goes wrong </p> ';
	}
}

mysqli_close($dbc);

?>

<?php 

if(isset($trimmed['firstname']))
{
	$firstname_text = $trimmed['firstname']; 
}
else
{
	$firstname_text = $row['firstname'];
}

if(isset($trimmed['lastname']))
{
	$lastname_text = $trimmed['lastname']; 
}
else
{
	$lastname_text = $row['lastname'];
}

if(isset($trimmed['email']))
{
	$email_text = $trimmed['email']; 
}
else
{
	$email_text = $row['email'];
}

?>

<div align="right">
	<form action="delete_account.php" method="post">
		<input type="submit" name="submit" value="Delete Account" >
		<input type="hidden" name="submitted_delete" value="TRUE" />
	</form>
</div>

<h1> Edit profile </h1>

<form action="edit_profile.php" method="post">
	<fieldset>
		<p><b>Firstname: </b><input id="firstname" type="text" name ="firstname" size="30" maxlenght="30" onClick="clearField('firstname')" value="<?php echo $firstname_text ?>"/></p>
		<p><b>Lastname: </b><input id="lastname" type="text" name ="lastname" size="30" maxlenght="30" onClick="clearField('lastname')" value="<?php echo $lastname_text ?> "/></p>
		<p><b>Email: </b><input id="email" type="text" name ="email" size="30" maxlenght="40" onClick="clearField('email')" value="<?php echo $email_text ?> "/></p>
		<div>
			<input type="submit" name="submit" value="Save changes" onClick="return check_editprofile_Fields(this);" />
			<input type="hidden" name="submitted" value="TRUE" />
		</div>
	</fieldset>
</form>

<?php include('includes/footer.html'); ?>

</body>
</html>
