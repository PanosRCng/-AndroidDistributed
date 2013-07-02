<?php

function remove_dir($user_dir) // delete folder
{
	return deleteDirectory($user_dir);
}


function deleteDirectory($dir) // delete directory and everything inside
{
	if (!file_exists($dir))
		 return true;
	if (!is_dir($dir))
		 return unlink($dir);

	foreach (scandir($dir) as $item)
	{
	        if ($item == '.' || $item == '..') continue;

	        if (!deleteDirectory($dir.DIRECTORY_SEPARATOR.$item))
			return false;
	}

	return rmdir($dir);
}

?>
