<?php

 
// array for JSON response
$response = array();

// check for required fields
if (isset($_POST['Interests']) && isset($_POST['fid']) && isset($_POST['name']) &&isset($_POST['lat']) && isset($_POST['lon'])) {
 
    $fid = $_POST['fid'];
    $name = $_POST['name'];
    $lat = $_POST['lat'];
    $lon = $_POST['lon'];
    $Interests = $_POST['Interests'];
 
    // include db connect class
    require_once __DIR__ . '/db_connect.php';
 
    // connecting to db
    $db = new DB_CONNECT();
 
    // mysql update row with matched userid
    $result = mysql_query("UPDATE users SET Interests = '$Interests', fid = '$fid', name = '$name', lat = '$lat', lon = '$lon' WHERE UserID = 1");
 
    // check if row inserted or not
    if ($result) {
        // successfully updated
        $response["success"] = 1;
        $response["message"] = "Product successfully updated.";
 
        // echoing JSON response
        echo json_encode($response);
    } else {
 
    }
} else {
    // required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field(s) is missing";
 
    // echoing JSON response
    echo json_encode($response);
}
?>