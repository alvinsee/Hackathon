<?php


// array for JSON response
$response = array();


// include db connect class
require_once __DIR__ . '/db_connect.php';

// connecting to db
$db = new DB_CONNECT();

// get all products from users table
$result = mysql_query("SELECT *FROM users") or die(mysql_error());

// check for empty result
if (mysql_num_rows($result) > 0) {
    // looping through all results
    $response["users"] = array();
    
    while ($row = mysql_fetch_array($result)) {
        // temp user array
        $user = array();
        $user["UserID"] = $row["UserID"];
        $user["Forename"] = $row["Forename"];
        $user["Surname"] = $row["Surname"];



        // push single product into final response array
        array_push($response["users"], $user);
    }
    // success
    $response["success"] = 1;

    // echoing JSON response
    echo json_encode($response);
} else {
    // no products found
    $response["success"] = 0;
    $response["message"] = "No userss found";

    // echo no users JSON
    echo json_encode($response);
}
?>
