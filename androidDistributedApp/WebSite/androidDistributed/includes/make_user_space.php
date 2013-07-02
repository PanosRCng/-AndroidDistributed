<?php 

include('includes/remove_dir.php');
				
function make_user_space($user) // make user space (public, private, thumbs folders)
{
	$user_dir = USER_SPACE_PUBLIC.$user;

	if(file_exists($user_dir)) // if for some reason trash user folder already exists (db already checked and there is not user
	{													// with this username )
		if( !remove_dir($user_dir) ) // remove it and everything in it
		{
			return 0; // if fail can't register user
		}
	}

	$user_dir = USER_SPACE_PUBLIC.$user;  // if all ok, make user space

	if( mkdir($user_dir , 0777, true) )
	{
		return 1;
	}
	else
	{
		return 0;
	} 
}

?>
