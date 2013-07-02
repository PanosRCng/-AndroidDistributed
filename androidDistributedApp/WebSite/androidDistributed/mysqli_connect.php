<?php

DEFINE ('DB_USER', '**********');
DEFINE ('DB_PASSWORD', '*********');
DEFINE ('DB_HOST', '************');
DEFINE ('DB_NAME', 'androidDistributed');

$dbc = @mysqli_connect (DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);

if( !mysqli_set_charset ($dbc , "utf8") )
{
	trigger_error('Could not set character set UTF8: ' . mysqli_connect_error());
}

if(!$dbc)
{
	trigger_error('Could not connect to MySQL: ' . mysqli_connect_error());
}

function db_connect()
{
	$dbc = @mysqli_connect (DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);

	if(!$dbc)
	{
		trigger_error('Could not connect to MySQL: ' . mysqli_connect_error());
	}
	else
	{
		if( mysqli_set_charset ($dbc , "utf8") )
		{
			return $dbc;
		}
	}
}

?>
