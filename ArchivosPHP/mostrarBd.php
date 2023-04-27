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
$metodo = $_POST['metodo'];
if($metodo == 0){
    #Ejecutar la sentencia SQL
    $query = "SELECT * FROM Publicaciones ORDER BY ImagenId";
    $result = $con-> query ($query);
    #Guardar en un array la respuesta
    while($row = $result->fetch_array()){
        $flag[]=$row;            
    }  
    #Devolver en formato json
    echo json_encode($flag);
    $result->close();
}
if($metodo == 1){
    #Realizar el select que devuelve las imagenes de todas las publicaciones ordenadas por id
    $query = "SELECT Imagen,Publicaciones.ImagenId FROM Publicaciones,Imagenes WHERE Imagenes.ImagenId = Publicaciones.ImagenId ORDER BY ImagenId;";
    $result = $con-> query ($query);
    #Almacenar el resultado
    while($row = $result->fetch_array()){
        $flag[]=$row;            
    }  
    #Devover en formato json
    echo json_encode($flag);
    $result->close();
}
if($metodo == 2){
    #Se recogen los parámetros que faltan
    $idPubli = $_POST["idPubli"];
    #Ejecutar la query
    $query = "SELECT Usuario,Texto,ImagenId FROM Publicaciones WHERE PublicacionId = $idPubli";
    #Almacenar el resultado y devolverlo en formato json
    $result = $con-> query ($query);
    while($row = $result->fetch_array()){
        $flag[]=$row;            
    }  
    echo json_encode($flag);
    $result->close();
}
if($metodo == 3){
    $imagenId = $_POST["imagenId"];
    $query = "SELECT Imagen FROM Imagenes WHERE ImagenId = '$imagenId';";
    $result = $con-> query ($query);
    while($row = $result->fetch_array()){
        $flag[]=$row;            
    }  
    echo json_encode($flag);
    $result->close();
}

?>