import os
import re
import commands

input = file("datasets-config.txt", "r")
def_dbname = "dblp";
data_dbname = "dblp";
randSeed = "54321";
dataloc = "../../datasets/";
dbgenloc = "dbgen";

# setting GENERATOR_DIR environmental variable for DBgen
os.putenv("GENERATOR_DIR", dbgenloc);


# Drop and create datasets definitions table
query = "DROP TABLE IF EXISTS datasets";
command = 'mysql -u root ' + def_dbname + ' -e "' + query + '"';
commands.getoutput(command);
query = "CREATE TABLE  datasets (\
  name varchar(15) NOT NULL,\
  class varchar(7) NOT NULL,\
  Rsize int NOT NULL,\
  Csize int NOT NULL,\
  Perr int NOT NULL,\
  Eerr int NOT NULL,\
  Pins int NOT NULL,\
  Pdel int NOT NULL,\
  Prep int NOT NULL,\
  Pswp int NOT NULL,\
  Ptkrep int NOT NULL,\
  dist   int default 0,\
  PRIMARY KEY  (name)\
)";
command = 'mysql -u root ' + def_dbname + ' -e "' + query + '"'; 
commands.getoutput(command);

for line in input:
	line = line.strip();	
	pars = re.split('\\s*,\\s*',line)
	if pars[0][0] != "#":
		#for par in pars:
			#print par
		query = 'INSERT INTO '+ def_dbname +'.datasets VALUES (\''\
			+pars[0]+'\', \''+pars[1]+'\', '+pars[2]+', '+pars[3]+', '+pars[4]+', '\
		        +pars[5]+', '+pars[6]+', '+pars[7]+', '+pars[8]+', '+pars[9]+', '\
		        +pars[10]+', '+pars[11] + ')'
		
		command = 'mysql -u root ' + def_dbname + ' -e "' + query + '"'; 
		print command
		commands.getoutput(command);
		
		#command : $GENERATOR_DIR/dbgen -n 5000 -c 500 -s 53421 -i -q -e 90 -u 30 -x 24 -d 24 -r 24 -v 14 -w 14 -o ~/data/cname-datasets/titles-u_90_30_2424241414.txt
		
		
		if pars[11]=='0':			
			
			command = '$GENERATOR_DIR/dbgen -n '+pars[2]+' -c '+pars[3]+' '+\
				   '-s '+randSeed+' -i -q -e '+pars[4]+' -u '+pars[5]+' -x '+pars[6]+' -d '+pars[7]+' '+\
				   '-r '+pars[8]+' -v '+pars[9]+' -w '+pars[10]+' -o '+dataloc+pars[0];
			
		else:
			command = '$GENERATOR_DIR/dbgen -n '+pars[2]+' -c '+pars[3]+' '+\
							   '-s '+randSeed+' -i -q -e '+pars[4]+' -u '+pars[5]+' -x '+pars[6]+' -d '+pars[6]+' '+\
				   '-r '+pars[8]+' -v '+pars[9]+' -w '+pars[10]+' -z 0.5 -o '+dataloc+pars[0];
		
                print command;
		print commands.getoutput(command);
		
		print "Dataset " + pars[0] + " created."
		
		#DROP TABLE IF EXISTS `tst`;
		#CREATE TABLE  `tst` (
		#  `tid` bigint,
		#  `id` bigint,
		#  `clean` varchar(100),
		#  `string` varchar(100),
		#  PRIMARY KEY  (`tid`)
		#);
		query = "DROP TABLE IF EXISTS tst";
		command = 'mysql -u root ' + data_dbname + ' -e "' + query + '"'; 
		commands.getoutput(command);
		
		query = "CREATE TABLE  tst ( " +\
		  	"tid bigint, " +\
		  	"id bigint, " +\
		  	"clean varchar(100), " +\
		  	"string varchar(100), " +\
		  	"PRIMARY KEY  (tid)" +\
		  	")";
		command = 'mysql -u root ' + data_dbname + ' -e "' + query + '"'; 
		commands.getoutput(command);
		
		query= "LOAD DATA LOCAL INFILE " +\
		       "'" + dataloc + pars[0] + "'" + \
		       " INTO TABLE tst " + \
		       " FIELDS TERMINATED BY ':' " + \
		       " LINES TERMINATED BY '\\n' " + \
		       " (tid, id, clean, string)";
		command = 'mysql -u root ' + data_dbname + ' -e "' + query + '"'; 
		commands.getoutput(command);
		
		
		query = "DROP TABLE IF EXISTS " + data_dbname + "." + pars[0];
		command = 'mysql -u root ' + data_dbname + ' -e "' + query + '"'; 
		commands.getoutput(command);
		
		query = "CREATE TABLE " + data_dbname + "." + pars[0] + " (" +\
		  	" tid bigint," +\
		  	" id bigint," +\
		  	" string varchar(100)," +\
		  	" PRIMARY KEY  (tid)" +\
		  	" )";
		command = 'mysql -u root ' + data_dbname + ' -e "' + query + '"'; 
		commands.getoutput(command);
		
		query = "INSERT INTO " + data_dbname + "." + pars[0] + " (" +\
			" SELECT tid, id, string " +\
			" FROM tst )";
		command = 'mysql -u root ' + data_dbname + ' -e "' + query + '"'; 
		commands.getoutput(command);
		#print query;
		
		print "Dataset " + pars[0] + " loaded in database " + data_dbname + "."

#input.close()




