<?php

define('LIVE', FALSE);

define('EMAIL', 'my_email@hotmail.com');

define('BASE_URL', 'androidDistributed/');

define('MYSQL', '/var/www/androidDistributed/mysqli_connect.php');

define('USER_SPACE_PUBLIC', '/var/www/androidDistributed/dataPlace/');

define('USER_SPACE_PRIVATE', '/var/www/dataPlace/');

define('TMP_SPACE', '/tmp/');

define('MAX_FREE_SPACE', '50000000');

define('UPLOAD_MAX_FILE_SIZE', '25000000');



function my_error_handler($e_number, $e_message, $e_file, $e_line, $e_vars)
{
	$message = "<p> An error occurred in script '$e_file' on line $e_line: $e_message \n <br/>";

	$message .= "<pre>" .print_r($e_vars, 1) . "</pre> \n </p>";

	if(!LIVE)
	{
		echo '<div id="Error">' .$message . '</div><br/>';
	}
	else
	{
		if($e_number != E_NOTICE)
		{
			echo '<div id="Error">A system error occurred. We apologize </div><br />';
		}
	}
}

set_error_handler('my_error_handler');

?>
