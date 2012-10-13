<html> 
<head> 
<title>List of users</title> 
</head> 
<body> 
<h4>USERS</h4> 
<?php  


$server = "internal-db.s77737.gridserver.com:3306"; // this is the server address and port 
$username = "db77737_uconnect"; // change this to your mysql username 
$password = "uconnect"; // change this to your mysql password 


$link = mysql_connect ($server, $username, $password) 
or die ("Could not connect"); 

$db = mysql_select_db("db77737_uConnect", $link);

$result = mysql_query("SELECT * FROM users", $link); 
if (!$result) { 
  echo("<p>Error performing query: " . mysql_error() . "</p>"); 
  exit(); 
} 

while ($row = mysql_fetch_array($result, MYSQL_ASSOC)) { 
    echo($row["UserID"] . " "); 
    echo($row["Forename"] . " "); 
    echo($row["Surname"] . " "); 
    echo($row["lon"] . $row["lat"]); 
    echo("<br>");
} 

?> 

</body> 

<?  
mysql_close ($link); 
?> 

</html>