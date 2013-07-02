<?php
				// unzip function
function unzip($filename, $unzip_path)
{
	$zip = new ZipArchive;
	$res = $zip->open($filename);

	if ($res === TRUE)
	{
		$zip->extractTo($unzip_path);
		$zip->close();

		return 1;
	}
	else
	{
		return 0;
	}
}
			// parse xml inxo.xml 
function parse_xml($filename)
{
	$allowed_image_type = array('image/pjpeg', 'image/jpeg',
					 'image/JPG', 'image/X-PNG', 
					 'image/PNG', 'image/png',
					 'image/png', 'image/x-png');

	$photoset = simplexml_load_file($filename);

	if( strcmp($photoset->getName(), 'photoset') == 0 )
	{	
		foreach($photoset->children() as $photo)
		{	
			if( strcmp($photo->getName(), 'photo') == 0)
			{
			
				foreach($photo->children() as $info)
				{
					if( strcmp($info->getName(), 'name') == 0 )
					{
						$name = $info;
					}
	
					if( strcmp($info->getName(), 'title') == 0 )
					{
						$title = $info;
					}

					if( strcmp($info->getName(), 'description') == 0 )
					{
						$description = $info;
					}

					if( strcmp($info->getName(), 'tags') == 0 )
					{
						$tags = $info;
					}

					if( strcmp($info->getName(), 'longtitude') == 0 )
					{
						$longtitude = $info;
					}

					if( strcmp($info->getName(), 'latitude') == 0 )
					{
						$latitude = $info;
					}

					if( strcmp($info->getName(), 'public') == 0 )
					{
						$public = $info;
					}
				}
						// generate fandom name by time in nanoseconds
				$photo_name = uniqid();						// rename photo
				rename(TMP_SPACE.$_SESSION['tmp_folder'].'/'.$name, TMP_SPACE.$_SESSION['tmp_folder'].'/'.$photo_name);

				$image_path = TMP_SPACE.$_SESSION['tmp_folder'].'/'.$photo_name;
				$filesize = filesize($image_path);
										// if there is free space
				if( $filesize < space_left($_SESSION['username']) ) 
				{
					$finfo = finfo_open(FILEINFO_MIME_TYPE);	// check if file is a photo
					$uploaded_file_type = finfo_file($finfo, $image_path);
											     // if file a photo
					if(in_array($uploaded_file_type, $allowed_image_type))
					{								// save photo
						if( save_image($title,$description,$tags,$longtitude,$latitude,$public,$photo_name) )
						{
							echo '<p class="error"> Photo: '.$name.' uploaded </p> ';
						}	
					}
					else
					{
						echo '<p class="error"> Empty fields, watch your xml </p> ';
					}
				}
				else
				{
					echo '<p class="error"> You have not enough space </p> ';	
				}
				

			}
			else
			{
				print('invalid xml photo format');
				die();
			}	
		}
	}
	else
	{
		print('invalid xml format photoset');
		die();
	}
}

?>
