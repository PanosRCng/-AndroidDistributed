<h3> Add Infos </h3>

<fieldset>
<form action="upload.php" method="post">

<div id="wrapper_down">
	<div id="infos">

		<p><b> Plugin Name: </b>
			<input id="name" type="text" name="name" size="50" max_length="30" onClick="clearField('name')"
	        	           value="<?php if(isset($trimmed['name'])) echo $trimmed['name'] ?>"/>
		</p>
		<p><b> ContextType: </b>
			<input id="contextType" type="text" name="contextType" size="50" max_length="30"
				value="<?php if(isset($trimmed['contextType'])) echo $trimmed['contextType'] ?>"/>
		</p>

		<p><b> Sensor Dependencies: </b>
			<input id="sensorDependencies" type="text" name="sensorDependencies" size="50" max_length="30"
				value="<?php if(isset($trimmed['sensorDependencies'])) echo $trimmed['sensorDependencies'] ?>"/>
		</p>

		<p><b> Time Dependencies: </b>
			<input id="timeDependencies" type="text" name="timeDependencies" size="50" max_length="30"
				value="<?php if(isset($trimmed['timeDependencies'])) echo $trimmed['timeDependencies'] ?>"/>
		</p>

		<p><b> Expires: </b>
			<input id="expires" type="text" name="expires" size="50" max_length="30"
				value="<?php if(isset($trimmed['expires'])) echo $trimmed['expires'] ?>"/>
		</p>

<div align="left">
	<input type="submit" name="submit_infos" value="Save"/>
</div>

	<input type="hidden" name="submitted_infos" value="TRUE" />
</form>
</fieldset>
