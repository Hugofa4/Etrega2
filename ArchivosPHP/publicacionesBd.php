<?php
$DB_SERVER="localhost"; #la dirección del servidor
$DB_USER="Xhfernandez026"; #el usuario para esa base de datos
$DB_PASS="pc3Xfdts"; #la clave para ese usuario
$DB_DATABASE="Xhfernandez026_proyecto"; #la base de datos a la que hay que conectarse
# Se establece la conexión:
$con = mysqli_connect($DB_SERVER, $DB_USER, $DB_PASS, $DB_DATABASE);
#Comprobamos conexión
if (mysqli_connect_errno()) {
    echo 'Error de conexion: ' . mysqli_connect_error();
exit();
}
#Se recogen los parámetros
$metodo = $_POST["metodo"];
if($metodo == 0){
    $imagen = $_POST["imagen"];
    $id = $_POST["id"];
    $resultado = mysqli_query($con,"INSERT INTO Imagenes (ImagenId,Imagen) VALUES ('$id','$imagen')");
    # Comprobar si se ha ejecutado correctamente
    if (!$resultado) {
        echo 'Ha ocurrido algún error: ' . mysqli_error($con);
    }
}
if ($metodo == 1){
    $usuario = $_POST["usuario"];
    $texto = $_POST["texto"];
    $imagen = $_POST["imagen"];
    $resultado = mysqli_query($con,"INSERT INTO Publicaciones (Usuario,Texto,ImagenId) VALUES ('$usuario','$texto','$imagen')");
    # Comprobar si se ha ejecutado correctamente
    if (!$resultado) {
        echo 'Ha ocurrido algún error: ' . mysqli_error($con);
    }
}

?>