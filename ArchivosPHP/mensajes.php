<?php
#Se recogen todos los parametros
$idPubli = $_POST["idPubli"];
$usuario = $_POST["usuario"];
$texto = $_POST["texto"];
$token = $_POST["token"];
#Se define la cabecera la clave del servicio firebase
$cabecera= array(
'Authorization: key=AAAAz4Gu0pg:APA91bHafJlhIattLCcu9VPofGdFab_HmW-8mAKnCkr_Oix_kCGom8vObP6u1GibJ5EtxxDOSLZQQ6ymAsMa72fNY1M5ykblxc0hu8w2ZX_0ELUYWbf6iz4f4VG266t8sMWbB20-OOp_',
'Content-Type: application/json'
);
#El mensaje con sus respectivos datos y notificación
$msg = array (
    'to'=>"$token",
    'data' => array (
    "idPubli" => "$idPubli",
    "notificacion" => "1"),
    'notification' => array (
    "body" => "$texto",
    "title" => "$usuario",
    "icon" => "ic_stat_ic_notification",
    "click_action"=>"AVISO"
    )
);
#Se devuelven los datos en formato json
$msgJSON= json_encode($msg);

$ch = curl_init(); #inicializar el handler de curl
#indicar el destino de la petición, el servicio FCM de google
curl_setopt( $ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send');
#indicar que la conexión es de tipo POST
curl_setopt( $ch, CURLOPT_POST, true );
#agregar las cabeceras
curl_setopt( $ch, CURLOPT_HTTPHEADER, $cabecera);
#Indicar que se desea recibir la respuesta a la conexión en forma de string
curl_setopt( $ch, CURLOPT_RETURNTRANSFER, true );
#agregar los datos de la petición en formato JSON
curl_setopt( $ch, CURLOPT_POSTFIELDS, $msgJSON );
#ejecutar la llamada
$resultado= curl_exec( $ch );
#cerrar el handler de curl
curl_close( $ch );
if (curl_errno($ch)) {
    print curl_error($ch);
}
echo $resultado;
?>