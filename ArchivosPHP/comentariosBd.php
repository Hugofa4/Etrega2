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
#Se recogen los parámetros enviados
$metodo = $_POST["metodo"];
$idPubli = $_POST["idPubli"];
if ($metodo == 0){
    #Ejecutar el select
    $query = "SELECT Comentarios.Usuario,Comentario FROM Comentarios,Publicaciones WHERE Comentarios.PublicacionId = $idPubli GROUP BY Usuario,Comentario";
    $result = $con-> query ($query);
    #Se almacena en una variable el resultado del select
    while($row = $result->fetch_array()){
        $flag[]=$row;            
    }  
    #Se devuelve en formato json
    echo json_encode($flag);
    $result->close();
}
if($metodo == 1){
    #Para este método se necesitan más parámetros
    $usuario = $_POST["usuario"];
    $texto = $_POST["texto"];
    #Ejecutar la sentencia
    $resultado = mysqli_query($con,"INSERT INTO Comentarios VALUES ($idPubli,'$usuario','$texto')");
    # Comprobar si se ha ejecutado correctamente
    if (!$resultado) {
        echo 'Ha ocurrido algún error: ' . mysqli_error($con);
    }
}
?>