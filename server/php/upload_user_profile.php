<?php
$user_id = $_GET['user_id'];
$target_path = "/var/www/html/raw/".$user_id."/profile.png";

if (move_uploaded_file($_FILES["image"]["tmp_name"], $target_path)) {
    echo(json_encode(array(
        "result"    => true
    )));
} else {
    echo(json_encode(array(
        'result'    => false,
        'code'      => 1,
        'target_path'   => $target_path
    )));
}
?>