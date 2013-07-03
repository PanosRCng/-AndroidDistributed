try:
	import pygmaps 
except ImportError:
	print 'can not import modules'
	sys.exit(1) # exit on import module failed

def read_results(filename):

	wifis = {}

	f = open(filename, 'rU')
	lines = f.readlines()
	f.close()

	for line in lines:
	
		line_parts = line.split('\t')[1]

		if len(line_parts) == 1:
			continue

		data = line_parts.split(" ")

		bssid = data[1]
		lat = data[2]
		
		if lat != "-1":
			loc = data[3]
		else:
			continue		

		level = data[len(data)-1]
		frequency = data[len(data)-2]

		capabilities = "-"

		if ('WEP' in data[4] ) | ( 'WPA' in data[4] ):
			capabilities = data[4]

		infos =  lat + " " + loc + "\t" + " " + capabilities + " " + frequency + " " + level

		wifis[bssid] = infos

	return wifis


def mapResults(wifis):

	mymap = pygmaps.maps(38.236832, 21.749783, 16)

	mymap.setgrids(38.24209, 38.244382, 0.001, 21.700201, 21.809721, 0.001)

	for i in wifis:

		wifi = wifis[i]

		position = wifi.split("\t")[0]
		infos = wifi.split("\t")[1]

		lat = float( position.split(" ")[0] )
		loc = float( position.split(" ")[1] )

		if 'WEP' in infos:
			mymap.addradpoint(lat, loc, 70, "#FF0000")

		if 'WPA' in infos:
			mymap.addradpoint(lat, loc, 50, "#0000FF")

	mymap.draw('mymap.html')
	


###################################################################

wifis = read_results("results")

mapResults(wifis)

