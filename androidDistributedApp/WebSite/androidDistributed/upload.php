<?php session_start();

if( !isset($_SESSION['session_username']))
{
	die();
}
?>

<?php
require_once('/var/www/androidDistributed/config.php'); 
require_once(MYSQL); 
include('includes/save_experiment.php'); 
include('includes/remove_dir.php'); 
include("upload_functions.php");
mb_internal_encoding("UTF-8");
include('Reform.inc.php');
?>
 
<?php $_SESSION['experiment_up'] = 0; ?>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title> upload experiments </title>
<script type="text/javascript" src="includes/check_form_fields.js"></script>
<link rel="stylesheet" type="text/css" href="style/upload.css" />
</head>
<body>

<?php include("includes/header.html"); ?>

<?php
	echo('<div id="wrapper_options">
		<div id="options">
			<p> Hi, <b><font size="3">'. $_SESSION['username'].'</font></b></p>
			<a href="profile.php">View Profile</a>
			</br>
			<a href="logout.php">Logout</a>
	      	</div>
	      </div>');
?>

<?php

if(isset($_POST['submitted']))
{
	if ($_FILES['upload']['error'] > 0)
  	{
		print('<p class="error"> There was an error, try again </p>');
  	}
	else
	{
		/*
			php.ini
			-> max_file_uploads = 20 -> 1
			-> upload_max_file_size = 200MB -> 25MB
			-> max_execution_time = 30
			-> max_input_time = 60
			-> memory_limit = 128MB
			-> upload_tmp_dir = /tmp
			-> post_max_size = 200MB -> 26MB
		*/
		if(!empty($_FILES['upload']['name']))
		{
			$tmp_folder = uniqid();
			$_SESSION['tmp_folder'] = $tmp_folder;
			$user_tmp_dir = TMP_SPACE.$tmp_folder;

					// if trash tmp userspace exists remove it
			if(file_exists($user_tmp_dir))
			{
				if( !remove_dir($user_tmp_dir) )
				{
					return 0;
				}
			}

							// make tmp user space
			if( mkdir($user_tmp_dir , 0777, true) )
			{
				$experiment_name = uniqid();
							// move experiment to tmp userspace
				move_uploaded_file($_FILES['upload']['tmp_name'], $user_tmp_dir.'/'.$experiment_name);

				$_SESSION['experiment_name'] = $experiment_name;					
				$_SESSION['experiment_up'] = 1;
			}
			else
			{
				echo "There was an error uploading the file, please try again!";
			}


			if( (file_exists($_FILES['upload']['tmp_name'])) && (is_file($_FILES['upload']['tmp_name'])) )
			{
				unlink($_FILES['upload']['tmp_name']);
			}
		}
	}
}

if(isset($_POST['submitted_infos'])) // if submitted photo infos -> "Save" submit button pressed
{
	$trimmed = array_map('trim', $_POST);	// remove spaces from start and end of infos and load to trimmed

	$name = $contextType = $sensorDep = $timeDep = $expires = FALSE;

	$reform = new Reform; // OWASP anti-xss class
	$name = $reform->HtmlEncode($trimmed['name']); 
	$contextType = $reform->HtmlEncode($trimmed['contextType']);

//	$sensorDep = $reform->HtmlEncode($trimmed['sensorDependencies']);
	$sensorDep = $trimmed['sensorDependencies'];

	$timeDep = $reform->HtmlEncode($trimmed['timeDependencies']);
	$expires = $reform->HtmlEncode($trimmed['expires']);

						// escaping special characters for sql use 		
	$name = mysqli_real_escape_string($dbc, $name);
	$contextType = mysqli_real_escape_string($dbc, $contextType);
	$sensorDep = mysqli_real_escape_string($dbc, $sensorDep);
	$timeDep = mysqli_real_escape_string($dbc, $timeDep);
	$expires = mysqli_real_escape_string($dbc, $expires);

	if($name && $contextType && $sensorDep && $timeDep && $expires)
	{
		$experiment_name = $_SESSION['experiment_name'];
						
		if( save_experiment($name, $contextType, $sensorDep, $timeDep, $expires) )
		{
			echo '<p class="error"> Experiment uploaded </p> ';
			$user_tmp_dir = TMP_SPACE.$_SESSION['tmp_folder'];

			remove_dir($user_tmp_dir);
		}
		else
		{
			echo '<p class="error"> Can not upload image </p> ';
		}		
	}
	else
	{
		echo '<p class="error"> Fields must not be empty </p> ';
	}

	mysqli_close($dbc); // close db connection
}

?>

<?php
echo '
<div id="wrapper_up">';
$max_upload = round(UPLOAD_MAX_FILE_SIZE/ 1048576, 2);
echo('
	<div id="upload_field">
		<fieldset><legend> Upload experiment, max file size -> '.$max_upload.' MB:</legend>
			<form enctype="multipart/form-data" action="upload.php" method="post">
				<input type="hidden" name="MAX_FILE_SIZE" value="'.UPLOAD_MAX_FILE_SIZE.'">
					<p> File <input type="file" name="upload"></p>
					<div align="left">
						<input type="submit" name="submit" value="Upload" />
					</div>
					<input type="hidden" name="submitted" value="TRUE" />
			</form>
		</fieldset>
	</div>
</div>');
?>

<?php
	if($_SESSION['experiment_up'] == 1) // if image uploaded to temp space
	{											// view a small thumbnail
		echo '</br><div id="pic"><img src="images/plugins.png"/>'; 

		echo '</br><b><font size="2" color="red"> add some infos to finish upload... </font></b></div>'; 

		include("photo_infos_form.php"); // view a form to input photo infos 
	}
?>

<?php include("includes/footer.html"); ?>


</body>
</html>
