<?php

// array for JSON response
$response = array();

// check for required fields
if (isset($_POST['fid']) && isset($_POST['name']) &&isset($_POST['lat']) && isset($_POST['lon'])) {
    $fid = $_POST['fid'];
    $name = $_POST['name'];
    // Search parameters
    $lat = $_POST['lat'];
    $lon = $_POST['lon'];
    //$lat = 37.4846501;
    //$lon = -122.1482915;
    $radius = 0.01;
 
    // include db connect class
    require_once __DIR__ . '/db_connect.php';
 
    // connecting to db
    $db = new DB_CONNECT();
 
    // mysql update row with matched userid
    $result = mysql_query("UPDATE users SET fid = '$fid', name = '$name', lat = '$lat', lon = '$lon' WHERE name = '$name'");
    
    $response["users"] = array();

    // Constants related to the surface of the Earth
    $earths_radius = 6371;
    $surface_distance_coeffient = 111.320;

    // Spherical Law of Cosines
    $distance_formula = "$earths_radius * ACOS( SIN(RADIANS(lat)) * SIN(RADIANS($lat)) + COS(RADIANS(lon - $lon)) * COS(RADIANS(lat)) * COS(RADIANS($lat)) )";

    // Create a bounding box to reduce the scope of our search
    $lng_b1 = $lon - $radius / abs(cos(deg2rad($lat)) * $surface_distance_coeffient);
    $lng_b2 = $lon + $radius / abs(cos(deg2rad($lat)) * $surface_distance_coeffient);
    $lat_b1 = $lat - $radius / $surface_distance_coeffient;
    $lat_b2 = $lat + $radius / $surface_distance_coeffient;

    // Construct our sql statement

    $result2 = mysql_query("SELECT *, ($distance_formula) AS distance
    FROM users
    WHERE (lat BETWEEN $lat_b1 AND $lat_b2) AND (lon BETWEEN $lng_b1 AND $lng_b2)
    HAVING distance < $radius
    ORDER BY distance ASC");

    while ($row = mysql_fetch_array($result2)) {
        //if($row['name'] != '$name'){
        // temp user array
        $user = array();
        $user["UserID"] = $row["UserID"];
        $user["name"] = $row["name"];
        $user["fid"] = $row["fid"];

        // push single product into final response array
        array_push($response["users"], $user);
        //}
    }

    // check if row inserted or not
    if ($result2) {
        // successfully updated
        $response["success"] = 1;
        $response["message"] = "User's coordinate successfully updated.";
 
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
