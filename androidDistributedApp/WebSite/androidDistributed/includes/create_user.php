<?php

function create_user($firstname, $lastname, $email, $username, $pass)
{			// connect to db
	$dbc = db_connect();
						// query to check if user already exists
	$query = "select email from users where email='$email'";
	$result = mysqli_query($dbc, $query) or trigger_error("Query: $query \n </br> MYSQL Error: " . mysqli_error($dbc));
				// if 0 resutls, create user
	if( mysqli_num_rows($result)==0 )
	{			
		$salt = md5($pass);	// store password hashed and salted
		$pass = $pass.$salt;
									// query to db to create user entry		
		$query = "insert into users (username, firstname, lastname, email, password)
					 values ('$username', '$firstname', '$lastname', '$email', SHA1('$pass'))";
		$result = mysqli_query($dbc, $query) or trigger_error("Query: $query \n </br> 
									MYSQL Error: " . mysqli_error($dbc));		
					// if user entry created
		if(mysqli_affected_rows($dbc) == 1)
		{				// if user space created		
			if( make_user_space($username) )
			{	
				return 1;
			}		
		}
	}
			// close connection to db
	mysqli_close($dbc);

	return 0;
}

?>
