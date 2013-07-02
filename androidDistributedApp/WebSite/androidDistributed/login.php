<?php require_once('/var/www/androidDistributed/config.php'); ?>

<?php session_start() ?>

<html>
<head>
<title> Login screen </title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="style/login.css" />
</head>
<body>

<?php include('includes/header.html'); ?>

<?php

if(isset($_POST['submitter']))
{
	if(isset($_SESSION['session_username']))
	{
		echo('<p class="error"> You are already logged in! </p>');
	}
	else
	{
		require_once(MYSQL); 

		$dbc = db_connect(); // connect to db
					
		if(!empty($_POST['username'])) // if not empty 
		{					// check if length is ok
			if(strlen($_POST['username']) <= 30)
			{						// escaping special characters for sql use
				$username = mysqli_real_escape_string($dbc, $_POST['username']);
			}
			else
			{
				$username = FALSE;
				echo('<p class="error"> Invalid username! </p>');
			}
		}

		if(!empty($_POST['password'])) // if not empty
		{					// check if length is ok
			if(strlen($_POST['password']) <= 32)
			{							// escaping special characters for sql use
				$password = mysqli_real_escape_string($dbc, $_POST['password']);
			}
			else
			{
				$password = FALSE;
				echo('<p class="error"> Invalid password! </p>');
			}
		}
				// if username and password ok
		if($username && $password)
		{					// password is hashed and salted 
			$salt = md5($password);      
			$password = $password.$salt;

			$query = "select * from users where username='$username' AND password = SHA1('$password')" ;

			$result = mysqli_query($dbc, $query) or trigger_error('Query: $query\n </br> MYSQL Error: '. mysqli_error($dbc));
							# if logged in ok, save user infos to session
			if(mysqli_num_rows($result) == 1)
			{
				$row = mysqli_fetch_array($result);

				$_SESSION = array( 'username' => $row['username'], 'email' => $row['email'],
									'firstname' => $row['firstname'],
									 'lastname' => $row['lastname'] );

				$_SESSION['session_username'] = $_SESSION['email'];
			
				mysqli_free_result($result);

				mysqli_close($dbc); // close db connection

				$url = 'index.php';  // redirect to index.php
				ob_end_clean();
				header("Location: $url");
				exit();
			}
			else
			{
				echo('<p class="error"> Login failed </p>');
			}				
		}
		else
		{
			echo('<p class="error"> Please try again </p>');
		}

		mysqli_close($dbc);
	}
}

?>

<div id="wrapper">
	<div id="login_form">
		<h1> Login </h1>
		<form action="login.php" method="post">
			<p><b> Username:  </b>
			<input type="text" size="20" name="username" maxlength="30" /></p>
			<p><b> Password:  </b>
			<input type="password" size="20" name="password" maxlength="32" /></p>
			<div>
				<input type="submit" name="submit" value="Login" />
				<input type="hidden" name="submitter" value="TRUE">
			</div>
		</form>
	</div>
	<div id="image">
		<img src="images/login.png" />
	</div>
</div>

<?php include('includes/footer.html'); ?>
</body>
</html>
