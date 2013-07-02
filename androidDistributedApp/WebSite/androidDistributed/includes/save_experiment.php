<?php

function save_experiment($name, $contextType, $sensorDep, $timeDep, $expires)
{				// if user logged in
	if(isset($_SESSION['username']))
	{
		$userEmail = $_SESSION['email'];
		$url = 'http://83.212.115.57/androidDistributed/dataPlace/'.$_SESSION['username'].'/'.$contextType.'.jar';

		$dbc = db_connect();
								// query to db to insert experiment entry
		$query = "insert into experiments (contextType, user_email, name, sensorDependencies,
	 			timeDependencies, expires, url) values('$contextType',
		 			'$userEmail', '$name','$sensorDep', '$timeDep', '$expires', '$url')";
		$result=mysqli_query($dbc, $query) or trigger_error("Query: $query\n<br/> MySQL Error: ". mysqli_error($dbc) );
						// if experiment entry added
		if(mysqli_affected_rows($dbc) == 1)
		{
			$factoryClass = $contextType.'.PluginFactory';
			$installUrl = '/dynamix/'.$contextType.'_9.47.1.jar';

			                                                                // query to db to insert experiment entry to plugins
                	$query = "insert into plugins (plugin_id, runtimeFactoryClass, name, description,
                                installUrl) values('$contextType',
                                        '$factoryClass', '$name','$name', '$installUrl')";
                	$result=mysqli_query($dbc, $query) or trigger_error("Query: $query\n<br/> MySQL Error: ". mysqli_error($dbc) );


			$experiment_tmp_path = TMP_SPACE.$_SESSION['tmp_folder'].'/'.$_SESSION['experiment_name'];
			$dest_path = USER_SPACE_PUBLIC.$_SESSION['username'].'/'.$contextType.'.jar';

			rename($experiment_tmp_path, $dest_path);

			$_SESSION['experiment_up'] = 0;
		}
		else
		{
			return 0;
		}

		mysqli_close($dbc);

		return 1;
	}
	else
	{
		return 0;
	}
}

?>
