# CodeCloneDetection
La razón de este proyecto es extender la funcionalidad ya existe de un motor que logra identificar clones de código a partir de 2 fragmentos de código escrito en Java. La extensión logra indicar al motor, un proyecto (carpeta) que contenga archivos .java. Cada uno de estos archivos será procesado, extrayendo cada método y con él, buscar en todos los archivos .java por clones, ya sea tipo 1, 2 o 3.
El procesamiento se realiza por 2 métodos:
* Usando hilos, por medio de una implementación personalizada de SwingWorker
* Por medio de paralelismo, usando Apache Spark, específicamente [Parallelized Collections](https://spark.apache.org/docs/latest/rdd-programming-guide.html#parallelized-collections)
### Software Requerido
* [JAVA SE 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) - Versión del JDK de Java
* [NetBeans 11](https://netbeans.apache.org/download/index.html) - Versión del IDE
* [Neo4J 4.0](https://neo4j.com/download/) - Versión de la base de datos
### Clonar Proyecto
Una vez instaladas las herramientas, se procede a clonar el repositorio
 * Abrir NetBeans ir a Team>Git>Clone
 * En el campo de url del repositorio digitar https://github.com/fsobrado/CodeClonesV2.git
 * En los campos de usuario y clave digitar las credenciales adecuadas
 * Dar clic al botón de siguiente, y seleccionar la casilla de master*
### Instalar Proyecto
Una vez clonado el proyecto en NetBeans
 * Abrir el proyecto
 * Posicionarse sobre la raíz del proyecto (sobre el nombre)
 * Una vez posicionados, dar clic derecho>Properties>
   - Asegurarse que en Sources> la opción de Source/Binary/Format este en 11 (Java 11)
     - En caso de que no tener referenciada la versión de Java 11
       - Seleccionar el boton Manage Java Platforms (click derecho sobre proyecto>Properties>Build>Compile)
       - Add Platform
       - Seleccionar opción Java Standard Edition, luego el botón Next
       - Especificar la locación de la instalación de Java 11, por ejemplo, C:\Program Files\Java
       - Especificar un nombre, por ejemplo, Java 11
       - Finish, luego en la pantalla siguiente Close
       - Seleccionar del combo de opciones la versión recién referenciada (Java 11)
   - Asegurarse que en Build>Compile> la opción Java Platform este JDK 11
 * Cerrar esta ventana presionando el botón de Ok
 * Volver a dar clic derecho sobre el proyecto >Build with Dependencies>
 ### Setup Base de datos
 * Abrir Neo4j Desktop
 * Seleccionar la opción Add Database
 * Seleccionar la opción Create Local Graph
   * En Graph Name digitar Clones
   * En Password digitar clones
   * Seleccionar la versión 4.0.3
   * Clic en botón Create
   * Una vez creada, clic en el botón Start
  ### Ejecutar Aplicación
  Una vez instaladas todas las herramientas (Java SE 11, NetBeans 11,Neo4J 4.0), clonado el proyecto desde GitHub y creada la base de       datos en Neo4J, procedemos a ejecutar la aplicación:
   * Abrir NetBeans 11
   * Clic derecho sobre el proyecto anteriormente descargado
   * Seleccionar la opción Run
   ### Obtención de resultados
   Una vez que la aplicación está corriendo:
   * Tenemos la opción de digitar o seleccionar la ruta en la cual se encuentran los archivos .java a analizar, por lo que 
   proporcionamos una ruta de alguna de las 2 maneras mencionadas.
   * Luego de seleccionar la ruta tenemos 2 opciones para ejecutar
     * Por medio de hilos
     * Por medio de paralelismo usando Apache Spark, el cual ya ha sido incluido en la instalación del proyecto.
   * A la hora de ejecutar cualquiera de las 2 opciones mencionadas, se obtendrá en pantalla los resultados que se van/hayan obtenido:
     * En caso de ejecutarse por hilos los resultados se presentarán en tiempo real
     * En caso de ejecutarse por paralelismo los resultados se presentaran al final del proceso y el orden en que los resultados se             imprimen no se verán bien ya que el paralelismo causa que segmento de procesamiento sean ejecutados en momentos distintos,              causando que el logeo de la información sea un poco impredecible.
   * Luego de ejecutar en cualquier modalidad el proceso, los resultados mostraran al final el tiempo total de ejecución y si todo se        ejecutó satisfactoriamente.
   ### Visualización de resultados
   Una vez que la aplicación ha finalizado el procesamiento (punto anterior), nos dirigimos a Neo4J Desktop:
   * Si la base de datos creada en el punto "Setup Base de datos" no ha sido aun puesta en marcha, lo hacemos presionado el botón Start
   * Luego damos clic en Open
   * Si el proceso de obtención de resultados a logrado encontrar clones, podemos observar en el tab Database Información>Node Labels,        una cantidad de nodos, que corresponden a las Clases>Métodos>Clones que existen.
   * Inicialmente Neo4J corre una consulta donde solo trae los 25 primeros nodos que encuentra, podemos correr otra que nos traiga al        menos 300 nodos:
     - En la parte superior de la pantalla vemos una caja de texto, que dice neo4j$, en el digitamos MATCH (n) RETURN n LIMIT 300, luego        el botón Play
   * Actualmente Neo4J permite visualizar una cantidad máxima de 350 nodos
   ### Notas
   Se puede usar el siguiente proyecto que contiene archivos .java de pruebas, localizados en
   https://github.com/fsobrado/Clones_TestProject.git
   * La instalación es la misma descrita en el paso "Clonar Proyecto" y "Instalar Proyecto", para este proyecto la version de Java puede      ser 8+.
   
     
     
   



