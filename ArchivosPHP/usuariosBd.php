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
$metodo = $_POST["metodo"];
$usuario = $_POST["usuario"];
if($metodo == 0){
    $contra = $_POST["contra"];
    $token = $_POST["token"];
    # Ejecutar la sentencia SQL de registro
    $resultado = mysqli_query($con, "INSERT INTO Usuarios VALUES('$usuario','$contra','$token')");
    # Comprobar si se ha ejecutado correctamente
    if (!$resultado) {
        echo 'Ha ocurrido algún error: ' . mysqli_error($con);
    }
}
if($metodo == 1){
    $contra = $_POST["contra"];
    # Ejecutar la sentencia SQL de inicio de sesion
    $resultado = mysqli_query($con, "SELECT Usuario FROM Usuarios WHERE Usuario='$usuario' AND Contra='$contra'");
    # Comprobar si se ha ejecutado correctamente
    if (!$resultado) {
        echo 'Ha ocurrido algún error: ' . mysqli_error($con);
    }
    #Acceder al resultado
    $fila = mysqli_fetch_row($resultado);
    # Generar el array con los resultados con la forma Atributo - Valor
    $arrayresultados = array(
        'usuario' => $fila[0],
    );
    #Devolver el resultado en formato JSON
    echo json_encode($arrayresultados);
}
if($metodo == 2){
    $token = $_POST["token"];
    # Ejecutar la sentencia SQL de actualizar token
    $res = mysqli_query($con, "UPDATE Usuarios SET Token='$token' WHERE Usuario='$usuario'");
    # Comprobar si se ha ejecutado correctamente
    if (!$res) {
        echo 'Ha ocurrido algún error: ' . mysqli_error($con);
    }
}
if ($metodo == 3){
    # Ejecutar la sentencia SQL de inicio de sesion
    $resultado = mysqli_query($con, "SELECT Token FROM Usuarios WHERE Usuario='$usuario'");
    # Comprobar si se ha ejecutado correctamente
    if (!$resultado) {
        echo 'Ha ocurrido algún error: ' . mysqli_error($con);
    }
    #Acceder al resultado
    $fila = mysqli_fetch_row($resultado);
    # Generar el array con los resultados con la forma Atributo - Valor
    $arrayresultados = array(
        'token' => $fila[0],
    );
    #Devolver el resultado en formato JSON
    echo json_encode($arrayresultados);
}

?>