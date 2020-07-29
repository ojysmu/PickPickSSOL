<?php
function gen_uuid() {
    return sprintf( '%04x%04x-%04x-%04x-%04x-%04x%04x%04x',
        // 32 bits for "time_low"
        mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff ),

        // 16 bits for "time_mid"
        mt_rand( 0, 0xffff ),

        // 16 bits for "time_hi_and_version",
        // four most significant bits holds version number 4
        mt_rand( 0, 0x0fff ) | 0x4000,

        // 16 bits, 8 bits for "clk_seq_hi_res",
        // 8 bits for "clk_seq_low",
        // two most significant bits holds zero and one for variant DCE1.1
        mt_rand( 0, 0x3fff ) | 0x8000,

        // 48 bits for "node"
        mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff )
    );
}

$user_id = $_POST['user_id'];
$image_id = gen_uuid();
$extension = pathinfo(basename($_FILES["image"]["name"]), PATHINFO_EXTENSION);
$target_path = "/var/www/html/raw/".$user_id."/".$image_id.".".$extension;

if (move_uploaded_file($_FILES["image"]["tmp_name"], $target_path)) {
    echo(json_encode(array(
        "result"    => true,
        "image_id"  => $image_id
    )));
} else {
    echo(json_encode(array(
        'result'    => false,
        'code'      => 1
    )));
}
?>