<?php session_start() ?>

<?php require_once('/var/www/androidDistributed/config.php'); ?>

<html>
<head>
<title> Logout </title>
<link rel="stylesheet" type="text/css" href="style/logout.css" />
</head>

</body>

<?php include('includes/header.html'); ?>

<?php

if(!isset($_SESSION['session_username']))
{
	$url = BASE_URL . 'index.php';

	ob_end_clean();

	header("Location: $url");

	exit();
}
else
{
	$_SESSION = array();

	session_destroy();

	setCookie(session_name(), '', time()-300);
}


print('
<div id="wrapper">
	<div id="logout"><b><font size="3" color="red">Logout successfully</font></b></div>
	<div id="image">
		<img src="images/logout.png" width="100%" />
	</div>
</div>
');

?>

<?php include('includes/footer.html'); ?>

</body>
</html>
